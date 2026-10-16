package com.example.vozni_park.service;

import com.example.vozni_park.entity.TravelOrder;
import com.example.vozni_park.entity.TravelOrderReportSnapshot;
import com.example.vozni_park.entity.TravelOrderVehicle;
import com.example.vozni_park.event.TravelOrderClosedEvent;
import com.example.vozni_park.repository.TravelOrderReportSnapshotRepository;
import com.example.vozni_park.repository.TravelOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Builds and persists a travel_order_report_snapshot row whenever a travel order
 * is closed. Triggered via TravelOrderClosedEvent published by TravelOrderService.
 *
 * Uses AFTER_COMMIT phase so the snapshot is only created after the travel order
 * transaction has committed successfully — no partial snapshots on rollback.
 *
 * Uses REQUIRES_NEW propagation because the original transaction is already
 * committed at AFTER_COMMIT phase; we need a fresh transaction for the INSERT.
 *
 * Used by Experiment C: the benchmark queries this table instead of running
 * the normalized multi-JOIN query on the live tables.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TravelOrderSnapshotService {

    private final TravelOrderRepository travelOrderRepository;
    private final TravelOrderReportSnapshotRepository snapshotRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTravelOrderClosed(TravelOrderClosedEvent event) {
        Long travelOrderId = event.travelOrderId();
        log.info("Building snapshot for travel order {}", travelOrderId);

        // Skip if snapshot already exists (idempotent — safe to call multiple times)
        if (snapshotRepository.existsByTravelOrderId(travelOrderId)) {
            log.warn("Snapshot already exists for travel order {} — skipping", travelOrderId);
            return;
        }

        TravelOrder travelOrder = travelOrderRepository.findById(travelOrderId)
                .orElseThrow(() -> new IllegalStateException(
                        "TravelOrder not found for snapshot: " + travelOrderId));

        TravelOrderReportSnapshot snapshot = new TravelOrderReportSnapshot();
        snapshot.setTravelOrderId(travelOrderId);
        snapshot.setTravelOrderNumber(travelOrder.getTravelOrderNumber());
        snapshot.setWorkOrderNumber(travelOrder.getWorkOrderNumber());
        snapshot.setDateFrom(travelOrder.getDateFrom());
        snapshot.setDateTo(travelOrder.getDateTo());
        snapshot.setStatus(travelOrder.getStatus());
        snapshot.setSnapshotCreatedAt(LocalDateTime.now());

        // Location name
        if (travelOrder.getLocation() != null) {
            snapshot.setLocationName(travelOrder.getLocation().getLocationName());
        }

        // Mileage and distance
        if (travelOrder.getStartingMileage() != null) {
            snapshot.setStartingMileage(travelOrder.getStartingMileage().intValue());
        }
        if (travelOrder.getEndingMileage() != null) {
            snapshot.setEndingMileage(travelOrder.getEndingMileage().intValue());
        }
        if (travelOrder.getStartingMileage() != null && travelOrder.getEndingMileage() != null) {
            long distance = travelOrder.getEndingMileage() - travelOrder.getStartingMileage();
            snapshot.setDistanceTraveled((int) distance);
        }

        // Vehicle display name and registration (first vehicle on the order)
        if (travelOrder.getTravelOrderVehicles() != null && !travelOrder.getTravelOrderVehicles().isEmpty()) {
            TravelOrderVehicle firstLink = travelOrder.getTravelOrderVehicles().get(0);
            var vehicle = firstLink.getVehicle();
            if (vehicle != null) {
                // Use the already-maintained denormalized field from Experiment A
                snapshot.setVehicleDisplayName(vehicle.getVehicleDisplayName());

                if (vehicle.getRegistration() != null) {
                    snapshot.setRegistrationNumber(vehicle.getRegistration().getRegistrationNumber());
                }
            }
        }

        // Driver names — comma-separated full names
        if (travelOrder.getDriverTravelOrders() != null && !travelOrder.getDriverTravelOrders().isEmpty()) {
            String names = travelOrder.getDriverTravelOrders().stream()
                    .map(link -> link.getDriver() != null ? link.getDriver().getFullName() : "")
                    .filter(name -> !name.isBlank())
                    .collect(Collectors.joining(", "));
            snapshot.setDriverNames(names);
        }

        // Fuel aggregates — queried via native SQL through the join table
        // Defaults to ZERO if no fuel receipts exist for this travel order
        BigDecimal[] fuelTotals = resolveFuelTotals(travelOrderId);
        snapshot.setTotalLiters(fuelTotals[0]);
        snapshot.setTotalFuelCost(fuelTotals[1]);

        snapshotRepository.save(snapshot);
        log.info("Snapshot created for travel order {}", travelOrderId);
    }

    /**
     * Returns [totalLiters, totalFuelCost] for the given travel order.
     * Returns [ZERO, ZERO] if no fuel receipts are linked.
     */
    private BigDecimal[] resolveFuelTotals(Long travelOrderId) {
        try {
            // Use JPQL via the TravelOrderRepository or fall back to zero if not available
            // Fuel receipt aggregation is done inline to avoid pulling all receipts into memory
            Object[] result = travelOrderRepository.findFuelTotalsForTravelOrder(travelOrderId);
            if (result != null && result.length == 2) {
                BigDecimal liters = result[0] != null ? new BigDecimal(result[0].toString()) : BigDecimal.ZERO;
                BigDecimal cost   = result[1] != null ? new BigDecimal(result[1].toString()) : BigDecimal.ZERO;
                return new BigDecimal[]{ liters, cost };
            }
        } catch (Exception e) {
            log.warn("Could not resolve fuel totals for travel order {} — defaulting to zero. Reason: {}",
                    travelOrderId, e.getMessage());
        }
        return new BigDecimal[]{ BigDecimal.ZERO, BigDecimal.ZERO };
    }
}
