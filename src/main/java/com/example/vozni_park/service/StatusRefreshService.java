package com.example.vozni_park.service;

import com.example.vozni_park.entity.SystemRefresh;
import com.example.vozni_park.entity.TravelOrder;
import com.example.vozni_park.repository.DriverRepository;
import com.example.vozni_park.repository.DriversLicenseRepository;
import com.example.vozni_park.repository.SystemRefreshRepository;
import com.example.vozni_park.repository.TravelOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Daily refresh service for status recalculation.
 *
 * Problem this solves: BIU triggers (in the Oracle version) and service-layer
 * recalculations (in our version) keep status correct at the moment of write.
 * But travel orders that expire overnight and documents that expire at midnight
 * receive no write event — their status becomes stale until something touches them.
 *
 * This service runs once per day (2:00 AM by default) and:
 *   1. Flips ACTIVE travel orders whose date_to has passed to UNFINISHED,
 *      then cascades status recalculation to all linked vehicles and drivers.
 *   2. (Future: refreshes document statuses, triggers expiry notifications.)
 *
 * Multi-instance safety: the @Scheduled annotation fires on every running
 * instance simultaneously. The distributed lock on system_refresh ensures
 * only one instance performs the actual work per day — the others acquire
 * the lock, see that last_run_ts is already today, and exit immediately.
 *
 * This is the Java/Spring equivalent of the Oracle SP_LAZY_REFRESH_PN_EXPIRE
 * mechanism documented in the work version's Status Management reference.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatusRefreshService {

    static final String REFRESH_KEY = "STATUS_REFRESH";

    private final SystemRefreshRepository systemRefreshRepository;
    private final TravelOrderRepository travelOrderRepository;
    private final StatusCalculationService statusCalculationService;
    private final DriverRepository driverRepository;
    private final DriversLicenseRepository driversLicenseRepository;

    /**
     * Scheduled entry point. Runs daily at 02:00.
     * The actual work is delegated to runRefreshWithLock() which is
     * @Transactional so the pessimistic lock is held for the duration.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledDailyRefresh() {
        log.info("Daily status refresh triggered by scheduler");
        try {
            runRefreshWithLock();
        } catch (Exception e) {
            log.error("Daily status refresh failed", e);
        }
    }

    /**
     * Can also be called manually (e.g. from an admin endpoint for testing).
     * The lock still prevents concurrent runs.
     */
    public void triggerManualRefresh() {
        log.info("Daily status refresh triggered manually");
        runRefreshWithLock();
    }

    /**
     * Acquires distributed lock, checks if refresh already ran today,
     * then executes the two refresh phases.
     *
     * The @Transactional here ensures:
     *   - The pessimistic lock on system_refresh is held until commit.
     *   - Any competing instance blocks on findByKeyNameWithLock() and,
     *     after this transaction commits, will see last_run_ts = today
     *     and skip doing any work.
     */
    @Transactional
    public void runRefreshWithLock() {
        // Acquire distributed lock
        SystemRefresh lock = systemRefreshRepository
                .findByKeyNameWithLock(REFRESH_KEY)
                .orElseGet(() -> {
                    SystemRefresh newLock = new SystemRefresh();
                    newLock.setKeyName(REFRESH_KEY);
                    return systemRefreshRepository.save(newLock);
                });

        // Guard: skip if already ran today
        if (lock.getLastRunTs() != null &&
                lock.getLastRunTs().toLocalDate().equals(LocalDate.now())) {
            log.debug("Status refresh already ran today at {} — skipping",
                    lock.getLastRunTs());
            return;
        }

        log.info("Running daily status refresh for {}", LocalDate.now());
        int expiredOrdersCount = 0;

        try {
            // Phase 1: expire overdue travel orders
            expiredOrdersCount = expireOverdueTravelOrders();

            // Phase 2: (reserved for document expiry notifications — will be
            // implemented when the notification service is added)

            // Record successful run
            lock.setLastRunTs(LocalDateTime.now());
            lock.setLastRunStatus("OK");
            lock.setLastRunMessage(String.format(
                    "Expired %d travel orders", expiredOrdersCount));
            lock.setLastRunBy("scheduler");
            systemRefreshRepository.save(lock);

            log.info("Daily status refresh completed. Expired travel orders: {}",
                    expiredOrdersCount);

        } catch (Exception e) {
            lock.setLastRunTs(LocalDateTime.now());
            lock.setLastRunStatus("ERROR");
            lock.setLastRunMessage(e.getMessage());
            lock.setLastRunBy("scheduler");
            systemRefreshRepository.save(lock);
            throw e;
        }
    }

    // ── Private refresh phases ─────────────────────────────────────────

    /**
     * Phase 1: Find all ACTIVE travel orders whose date_to has passed
     * and no ending mileage has been entered. Transition them to UNFINISHED
     * and cascade status recalculation to all linked vehicles and drivers.
     *
     * This is the equivalent of Oracle SP_UPDATE_EXPIRED_TRAVEL_ORDERS
     * followed by TRG_PUTNI_NALOG_AUD_PROPAGATE.
     */
    private int expireOverdueTravelOrders() {
        List<TravelOrder> expired = travelOrderRepository
                .findExpiredActiveTravelOrders(LocalDate.now());

        if (expired.isEmpty()) {
            log.debug("No overdue travel orders to expire");
            return 0;
        }

        log.info("Expiring {} overdue travel orders", expired.size());

        for (TravelOrder travelOrder : expired) {
            try {
                // markTravelOrderUnfinished handles status update + cascade
                statusCalculationService.markTravelOrderUnfinished(travelOrder);
                log.debug("Travel order {} → UNFINISHED", travelOrder.getIdTravelOrder());
            } catch (Exception e) {
                log.error("Failed to expire travel order {}: {}",
                        travelOrder.getIdTravelOrder(), e.getMessage());
                // Continue with remaining orders — one failure should not abort the whole batch
            }
        }

        return expired.size();
    }
}
