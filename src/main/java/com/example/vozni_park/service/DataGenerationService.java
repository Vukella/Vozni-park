package com.example.vozni_park.service;

import com.example.vozni_park.dto.DataGenerationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates large-scale benchmark data for the denormalization performance experiments.
 *
 * Uses JDBC batch inserts (not JPA) to efficiently insert 50,000+ rows.
 * JPA would be orders of magnitude slower for this volume due to entity lifecycle overhead.
 *
 * ── Small dataset (50k orders) ─────────────────────────────────────────────
 *   Prefix:    BENCH-*
 *   Locations: BenchmarkLocation-001..100  (shared with large dataset)
 *   Vehicles:  SAP 900_000 – 900_999
 *   Drivers:   SAP 800_000 – 800_999
 *
 * ── Large dataset (1M orders) ──────────────────────────────────────────────
 *   Prefix:    BENCH-LARGE-*
 *   Locations: BenchmarkLocation-001..100  (reused from small dataset)
 *   Vehicles:  SAP 910_000 – 910_999
 *   Drivers:   SAP 810_000 – 810_999
 *
 * Cleanup methods are independent: clearBenchmarkData() deletes only BENCH-*
 * rows; clearLargeBenchmarkData() deletes only BENCH-LARGE-* rows.
 *
 * Run large benchmarks with extra heap:
 *   java -Xmx2g -jar target\benchmarks.jar ".*Large.*"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataGenerationService {

    private final JdbcTemplate jdbcTemplate;

    // ── Small dataset constants ───────────────────────────────────────────────

    private static final int  BATCH_SIZE        = 1_000;
    public  static final int  NUM_LOCATIONS      = 100;
    public  static final int  NUM_VEHICLES       = 1_000;
    public  static final int  NUM_DRIVERS        = 1_000;
    public  static final int  NUM_TRAVEL_ORDERS  = 50_000;

    /** SAP number range reserved for small-dataset benchmark vehicles. */
    private static final long VEHICLE_SAP_START  = 900_000L;
    /** SAP number range reserved for small-dataset benchmark drivers. */
    private static final long DRIVER_SAP_START   = 800_000L;

    // ── Large dataset constants ───────────────────────────────────────────────

    public  static final int  NUM_TRAVEL_ORDERS_LARGE = 1_000_000;
    private static final String LARGE_PREFIX           = "BENCH-LARGE-";

    /** SAP range for large-dataset vehicles — must not overlap small-dataset range. */
    private static final long VEHICLE_SAP_START_LARGE  = 910_000L;
    /** SAP range for large-dataset drivers — must not overlap small-dataset range. */
    private static final long DRIVER_SAP_START_LARGE   = 810_000L;

    // ═════════════════════════════════════════════════════════════════════════
    // PUBLIC API — SMALL DATASET (50k)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Generates all small-dataset benchmark data in a single transaction.
     * Skips silently if benchmark data is already present (idempotent).
     */
    @Transactional
    public DataGenerationResult generateBenchmarkData() {
        long existingOrders = queryCount(
                "SELECT COUNT(*) FROM travel_order WHERE work_order_number LIKE 'BENCH-%'"
                        + " AND work_order_number NOT LIKE 'BENCH-LARGE-%'");
        if (existingOrders > 0) {
            log.info("Small benchmark data already present ({} orders) — skipping", existingOrders);
            return DataGenerationResult.alreadyExists(existingOrders);
        }

        log.info("=== Starting small benchmark data generation ===");
        long startTime = System.currentTimeMillis();
        Random rng = new Random(42L);

        // Prerequisites
        List<Long> fuelTypeIds     = jdbcTemplate.queryForList("SELECT id_fuel_type FROM fuel_type", Long.class);
        List<Long> vehicleModelIds = jdbcTemplate.queryForList("SELECT id_vehicle_model FROM vehicle_model", Long.class);
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id_user FROM app_user ORDER BY id_user LIMIT 1", Long.class);

        validatePrerequisites(fuelTypeIds, vehicleModelIds, userId);

        // Step 1: Locations
        int locationsGenerated = generateLocations();
        List<Long> locationIds = getBenchmarkLocationIds();
        log.info("Step 1 — {} locations", locationsGenerated);

        // Step 2: Vehicles
        int vehiclesGenerated = generateVehicles(locationIds, fuelTypeIds, vehicleModelIds,
                VEHICLE_SAP_START, NUM_VEHICLES);
        List<Long> vehicleIds = getVehicleIds(VEHICLE_SAP_START, NUM_VEHICLES);
        log.info("Step 2 — {} vehicles", vehiclesGenerated);

        // Step 3: Drivers
        int driversGenerated = generateDrivers(locationIds, DRIVER_SAP_START, NUM_DRIVERS);
        List<Long> driverIds = getDriverIds(DRIVER_SAP_START, NUM_DRIVERS);
        log.info("Step 3 — {} drivers", driversGenerated);

        // Step 4: Travel orders + links
        int ordersGenerated = generateTravelOrders(
                locationIds, vehicleIds, driverIds, userId, rng,
                NUM_TRAVEL_ORDERS, "BENCH-");
        log.info("Step 4 — {} travel orders", ordersGenerated);

        // Step 5: Update total_distance_traveled
        updateTotalDistanceTraveled(VEHICLE_SAP_START, NUM_VEHICLES, "BENCH-%", "BENCH-LARGE-%");
        log.info("Step 5 — total_distance_traveled updated");

        // Step 6: Snapshots
        int snapshotsGenerated = generateSnapshots("BENCH-%", "BENCH-LARGE-%");
        log.info("Step 6 — {} snapshots", snapshotsGenerated);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("=== Small benchmark complete in {}ms ===", elapsed);
        return new DataGenerationResult(
                locationsGenerated, vehiclesGenerated, driversGenerated,
                ordersGenerated, snapshotsGenerated, elapsed, false);
    }

    /**
     * Removes all small-dataset benchmark data in FK-safe order.
     * Does NOT affect large-dataset (BENCH-LARGE-*) data.
     */
    @Transactional
    public void clearBenchmarkData() {
        log.info("=== Clearing small benchmark data ===");
        deleteOrderRelatedData("BENCH-%", "BENCH-LARGE-%");
        jdbcTemplate.update("DELETE FROM vehicle WHERE sap_number BETWEEN ? AND ?",
                VEHICLE_SAP_START, VEHICLE_SAP_START + NUM_VEHICLES - 1);
        jdbcTemplate.update("DELETE FROM driver  WHERE sap_number BETWEEN ? AND ?",
                DRIVER_SAP_START, DRIVER_SAP_START + NUM_DRIVERS - 1);
        // Locations are shared — only delete if large dataset is also absent
        long largeOrders = queryCount(
                "SELECT COUNT(*) FROM travel_order WHERE work_order_number LIKE 'BENCH-LARGE-%'");
        if (largeOrders == 0) {
            jdbcTemplate.update("DELETE FROM location_unit WHERE location_name LIKE 'BenchmarkLocation-%'");
        }
        log.info("=== Small benchmark data cleared ===");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUBLIC API — LARGE DATASET (1M)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Generates the large-dataset benchmark data (1,000,000 travel orders).
     *
     * Requires the small dataset's BenchmarkLocation-* rows to already exist
     * (those locations are reused). Call generateBenchmarkData() first if needed.
     *
     * Run the corresponding benchmarks with extra JVM heap:
     *   java -Xmx2g -jar target\benchmarks.jar ".*Large.*"
     */
    @Transactional
    public DataGenerationResult generateLargeBenchmarkData() {
        long existingOrders = queryCount(
                "SELECT COUNT(*) FROM travel_order WHERE work_order_number LIKE 'BENCH-LARGE-%'");
        if (existingOrders > 0) {
            log.info("Large benchmark data already present ({} orders) — skipping", existingOrders);
            return DataGenerationResult.alreadyExists(existingOrders);
        }

        log.info("=== Starting large benchmark data generation (1M orders) ===");
        long startTime = System.currentTimeMillis();
        Random rng = new Random(99L); // different seed from small dataset

        // Prerequisites
        List<Long> fuelTypeIds     = jdbcTemplate.queryForList("SELECT id_fuel_type FROM fuel_type", Long.class);
        List<Long> vehicleModelIds = jdbcTemplate.queryForList("SELECT id_vehicle_model FROM vehicle_model", Long.class);
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id_user FROM app_user ORDER BY id_user LIMIT 1", Long.class);

        validatePrerequisites(fuelTypeIds, vehicleModelIds, userId);

        // Reuse existing BenchmarkLocation-* rows (created by small dataset)
        List<Long> locationIds = getBenchmarkLocationIds();
        if (locationIds.isEmpty()) {
            throw new IllegalStateException(
                    "BenchmarkLocation-* rows not found. Run generateBenchmarkData() first.");
        }
        log.info("Step 1 — reusing {} existing BenchmarkLocation rows", locationIds.size());

        // Step 2: Large-dataset vehicles
        int vehiclesGenerated = generateVehicles(locationIds, fuelTypeIds, vehicleModelIds,
                VEHICLE_SAP_START_LARGE, NUM_VEHICLES);
        List<Long> vehicleIds = getVehicleIds(VEHICLE_SAP_START_LARGE, NUM_VEHICLES);
        log.info("Step 2 — {} large vehicles (SAP {}-{})",
                vehiclesGenerated, VEHICLE_SAP_START_LARGE, VEHICLE_SAP_START_LARGE + NUM_VEHICLES - 1);

        // Step 3: Large-dataset drivers
        int driversGenerated = generateDrivers(locationIds, DRIVER_SAP_START_LARGE, NUM_DRIVERS);
        List<Long> driverIds = getDriverIds(DRIVER_SAP_START_LARGE, NUM_DRIVERS);
        log.info("Step 3 — {} large drivers (SAP {}-{})",
                driversGenerated, DRIVER_SAP_START_LARGE, DRIVER_SAP_START_LARGE + NUM_DRIVERS - 1);

        // Step 4: 1M travel orders
        int ordersGenerated = generateTravelOrders(
                locationIds, vehicleIds, driverIds, userId, rng,
                NUM_TRAVEL_ORDERS_LARGE, LARGE_PREFIX);
        log.info("Step 4 — {} large travel orders", ordersGenerated);

        // Step 5: Update total_distance_traveled for large vehicles
        updateTotalDistanceTraveled(VEHICLE_SAP_START_LARGE, NUM_VEHICLES, "BENCH-LARGE-%", null);
        log.info("Step 5 — total_distance_traveled updated for large vehicles");

        // Step 6: Snapshots for large travel orders
        int snapshotsGenerated = generateLargeSnapshots();
        log.info("Step 6 — {} large snapshots", snapshotsGenerated);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("=== Large benchmark complete in {}ms ({} minutes) ===",
                elapsed, elapsed / 60_000);
        return new DataGenerationResult(
                0, vehiclesGenerated, driversGenerated,
                ordersGenerated, snapshotsGenerated, elapsed, false);
    }

    /**
     * Removes all large-dataset benchmark data in FK-safe order.
     * Does NOT affect small-dataset (BENCH-*) data or BenchmarkLocation rows.
     */
    @Transactional
    public void clearLargeBenchmarkData() {
        log.info("=== Clearing large benchmark data ===");
        deleteOrderRelatedData("BENCH-LARGE-%", null);
        jdbcTemplate.update("DELETE FROM vehicle WHERE sap_number BETWEEN ? AND ?",
                VEHICLE_SAP_START_LARGE, VEHICLE_SAP_START_LARGE + NUM_VEHICLES - 1);
        jdbcTemplate.update("DELETE FROM driver  WHERE sap_number BETWEEN ? AND ?",
                DRIVER_SAP_START_LARGE, DRIVER_SAP_START_LARGE + NUM_DRIVERS - 1);
        log.info("=== Large benchmark data cleared ===");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PRIVATE GENERATION STEPS
    // ═════════════════════════════════════════════════════════════════════════

    private int generateLocations() {
        long existing = queryCount(
                "SELECT COUNT(*) FROM location_unit WHERE location_name LIKE 'BenchmarkLocation-%'");
        int toGenerate = (int) (NUM_LOCATIONS - existing);
        if (toGenerate <= 0) return 0;

        List<Object[]> batch = new ArrayList<>();
        for (long i = existing + 1; i <= NUM_LOCATIONS; i++) {
            batch.add(new Object[]{
                    String.format("BenchmarkLocation-%03d", i),
                    String.format("Benchmark Street %d, Test City", i)
            });
        }
        batchInsert("INSERT INTO location_unit (location_name, location_address) VALUES (?, ?)", batch);
        return toGenerate;
    }

    /**
     * Generates vehicles for the given SAP range.
     * vehicle_display_name is pre-computed as "Brand Model (BM-NNNNN)".
     * total_distance_traveled is initialised to 0 and updated in a later step.
     */
    private int generateVehicles(List<Long> locationIds,
                                 List<Long> fuelTypeIds,
                                 List<Long> vehicleModelIds,
                                 long sapStart,
                                 int count) {
        List<String> brandModelNames = jdbcTemplate.queryForList(
                "SELECT CONCAT(b.name, ' ', vm.name) FROM vehicle_model vm " +
                        "JOIN brand b ON b.id_brand = vm.brand_id", String.class);

        List<Object[]> vehicleBatch = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long sap        = sapStart + i;
            Long fuelTypeId = fuelTypeIds.get(i % fuelTypeIds.size());
            Long modelId    = vehicleModelIds.get(i % vehicleModelIds.size());
            String base     = brandModelNames.isEmpty()
                    ? "Test Brand Test Model"
                    : brandModelNames.get(i % brandModelNames.size());
            String displayName = base + " (BM-" + String.format("%05d", i + 1) + ")";

            vehicleBatch.add(new Object[]{
                    sap, modelId, fuelTypeId, "AVAILABLE", 1, displayName, 0L
            });
        }
        batchInsert(
                "INSERT INTO vehicle (sap_number, vehicle_model_id, fuel_type_id, " +
                        "vehicle_status, status_code, vehicle_display_name, total_distance_traveled) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                vehicleBatch);

        List<Long> newVehicleIds = getVehicleIds(sapStart, count);
        List<Object[]> locationBatch = new ArrayList<>();
        for (int i = 0; i < newVehicleIds.size(); i++) {
            locationBatch.add(new Object[]{
                    newVehicleIds.get(i),
                    locationIds.get(i % locationIds.size())
            });
        }
        batchInsert("INSERT INTO vehicle_location (vehicle_id, location_unit_id) VALUES (?, ?)",
                locationBatch);
        return vehicleBatch.size();
    }

    private int generateDrivers(List<Long> locationIds, long sapStart, int count) {
        String[] firstNames = {"Marko", "Nikola", "Petar", "Stefan", "Jovan",
                "Milan", "Dragan", "Aleksandar", "Vladimir", "Nenad"};
        String[] lastNames  = {"Petrović", "Jovanović", "Nikolić", "Đorđević", "Popović",
                "Stanković", "Ilić", "Pavlović", "Marković", "Lazić"};

        List<Object[]> driverBatch = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            driverBatch.add(new Object[]{
                    sapStart + i,
                    lastNames[i % lastNames.length] + " " + firstNames[i % firstNames.length],
                    "AVAILABLE", 1, 0
            });
        }
        batchInsert(
                "INSERT INTO driver (sap_number, full_name, status, status_code, license_revoked) " +
                        "VALUES (?, ?, ?, ?, ?)",
                driverBatch);

        List<Long> newDriverIds = getDriverIds(sapStart, count);
        List<Object[]> locationBatch = new ArrayList<>();
        for (int i = 0; i < newDriverIds.size(); i++) {
            locationBatch.add(new Object[]{
                    newDriverIds.get(i),
                    locationIds.get(i % locationIds.size())
            });
        }
        batchInsert("INSERT INTO driver_location (driver_id, location_unit_id) VALUES (?, ?)",
                locationBatch);
        return driverBatch.size();
    }

    /**
     * Generates travel orders distributed evenly across all provided locations.
     *
     * @param prefix           work_order_number prefix (e.g. "BENCH-" or "BENCH-LARGE-")
     * @param numOrders        total number of orders to generate
     */
    private int generateTravelOrders(List<Long> locationIds,
                                     List<Long> vehicleIds,
                                     List<Long> driverIds,
                                     Long userId,
                                     Random rng,
                                     int numOrders,
                                     String prefix) {
        int ordersPerLocation = numOrders / locationIds.size();
        LocalDate baseDate    = LocalDate.of(2024, 1, 1);
        Timestamp createdAt   = Timestamp.valueOf(LocalDateTime.now());

        List<Object[]> orderBatch = new ArrayList<>();
        int globalIdx = 0;

        for (int locIdx = 0; locIdx < locationIds.size(); locIdx++) {
            Long locationId = locationIds.get(locIdx);
            for (int seq = 1; seq <= ordersPerLocation; seq++) {
                globalIdx++;
                int daysOffset       = rng.nextInt(365);
                LocalDate dateFrom   = baseDate.plusDays(daysOffset);
                LocalDate dateTo     = dateFrom.plusDays(1 + rng.nextInt(14));
                long startingMileage = 10_000L + rng.nextInt(490_000);
                long endingMileage   = startingMileage + 50 + rng.nextInt(951);

                orderBatch.add(new Object[]{
                        String.format("%s%05d", prefix, seq),  // travel_order_number (per location)
                        prefix + globalIdx,                    // work_order_number (globally unique)
                        locationId,
                        userId,
                        "COMPLETED",
                        3,
                        startingMileage,
                        endingMileage,
                        Date.valueOf(dateFrom),
                        Date.valueOf(dateTo),
                        createdAt
                });
            }
        }

        batchInsert(
                "INSERT INTO travel_order (travel_order_number, work_order_number, location_id, " +
                        "created_by_user_id, status, status_code, starting_mileage, ending_mileage, " +
                        "date_from, date_to, creation_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                orderBatch);

        // Query back generated order IDs
        List<Long> orderIds = jdbcTemplate.queryForList(
                "SELECT id_travel_order FROM travel_order " +
                        "WHERE work_order_number LIKE ? ORDER BY id_travel_order",
                Long.class, prefix + "%");

        // travel_order_vehicle
        List<Object[]> tovBatch = new ArrayList<>();
        for (int i = 0; i < orderIds.size(); i++) {
            tovBatch.add(new Object[]{orderIds.get(i), vehicleIds.get(i % vehicleIds.size())});
        }
        batchInsert("INSERT INTO travel_order_vehicle (travel_order_id, vehicle_id) VALUES (?, ?)",
                tovBatch);

        // driver_travel_order
        List<Object[]> dtoBatch = new ArrayList<>();
        for (int i = 0; i < orderIds.size(); i++) {
            dtoBatch.add(new Object[]{orderIds.get(i), driverIds.get(i % driverIds.size())});
        }
        batchInsert("INSERT INTO driver_travel_order (travel_order_id, driver_id) VALUES (?, ?)",
                dtoBatch);

        return orderBatch.size();
    }

    /**
     * Bulk-updates total_distance_traveled for all vehicles in the given SAP range.
     *
     * @param notLike  if non-null, excludes orders matching this pattern from the SUM
     *                 (used when small/large datasets share location filters)
     */
    private void updateTotalDistanceTraveled(long sapStart, int count,
                                             String likePattern, String notLike) {
        String notLikeClause = (notLike != null) ? "AND tor.work_order_number NOT LIKE ?" : "";
        String sql = String.format("""
                UPDATE vehicle v
                INNER JOIN (
                    SELECT tov.vehicle_id,
                           SUM(tor.ending_mileage - tor.starting_mileage) AS total_dist
                    FROM   travel_order_vehicle tov
                    INNER JOIN travel_order tor ON tor.id_travel_order = tov.travel_order_id
                    WHERE  tor.work_order_number LIKE ?
                      AND  tor.status = 'COMPLETED'
                    %s
                    GROUP BY tov.vehicle_id
                ) calc ON calc.vehicle_id = v.id_vehicle
                SET v.total_distance_traveled = calc.total_dist
                WHERE v.sap_number BETWEEN ? AND ?
                """, notLikeClause);

        if (notLike != null) {
            jdbcTemplate.update(sql, likePattern, notLike, sapStart, sapStart + count - 1);
        } else {
            jdbcTemplate.update(sql, likePattern, sapStart, sapStart + count - 1);
        }
    }

    /** Generates snapshots for the small dataset (BENCH-* but not BENCH-LARGE-*). */
    private int generateSnapshots(String likePattern, String notLike) {
        jdbcTemplate.update("""
                INSERT INTO travel_order_report_snapshot
                    (travel_order_id, travel_order_number, work_order_number,
                     date_from, date_to, status, location_name,
                     vehicle_display_name, registration_number, driver_names,
                     starting_mileage, ending_mileage, distance_traveled,
                     total_liters, total_fuel_cost, snapshot_created_at)
                SELECT
                    tor.id_travel_order,
                    tor.travel_order_number,
                    tor.work_order_number,
                    tor.date_from,
                    tor.date_to,
                    tor.status,
                    lu.location_name,
                    MAX(v.vehicle_display_name),
                    NULL,
                    GROUP_CONCAT(DISTINCT d.full_name ORDER BY d.id_driver SEPARATOR ', '),
                    tor.starting_mileage,
                    tor.ending_mileage,
                    (tor.ending_mileage - tor.starting_mileage),
                    0, 0, NOW()
                FROM travel_order tor
                LEFT JOIN location_unit lu ON lu.id_location_unit = tor.location_id
                LEFT JOIN travel_order_vehicle tov ON tov.travel_order_id = tor.id_travel_order
                LEFT JOIN vehicle v ON v.id_vehicle = tov.vehicle_id
                LEFT JOIN driver_travel_order dto ON dto.travel_order_id = tor.id_travel_order
                LEFT JOIN driver d ON d.id_driver = dto.driver_id
                LEFT JOIN travel_order_report_snapshot existing
                    ON existing.travel_order_id = tor.id_travel_order
                WHERE tor.work_order_number LIKE ?
                  AND tor.work_order_number NOT LIKE ?
                  AND existing.travel_order_id IS NULL
                GROUP BY
                    tor.id_travel_order, tor.travel_order_number, tor.work_order_number,
                    tor.date_from, tor.date_to, tor.status, lu.location_name,
                    tor.starting_mileage, tor.ending_mileage
                """, likePattern, notLike);
        return (int) queryCount("SELECT COUNT(*) FROM travel_order_report_snapshot");
    }

    /** Generates snapshots for the large dataset (BENCH-LARGE-* only). */
    private int generateLargeSnapshots() {
        jdbcTemplate.update("""
                INSERT INTO travel_order_report_snapshot
                    (travel_order_id, travel_order_number, work_order_number,
                     date_from, date_to, status, location_name,
                     vehicle_display_name, registration_number, driver_names,
                     starting_mileage, ending_mileage, distance_traveled,
                     total_liters, total_fuel_cost, snapshot_created_at)
                SELECT
                    tor.id_travel_order,
                    tor.travel_order_number,
                    tor.work_order_number,
                    tor.date_from,
                    tor.date_to,
                    tor.status,
                    lu.location_name,
                    MAX(v.vehicle_display_name),
                    NULL,
                    GROUP_CONCAT(DISTINCT d.full_name ORDER BY d.id_driver SEPARATOR ', '),
                    tor.starting_mileage,
                    tor.ending_mileage,
                    (tor.ending_mileage - tor.starting_mileage),
                    0, 0, NOW()
                FROM travel_order tor
                LEFT JOIN location_unit lu ON lu.id_location_unit = tor.location_id
                LEFT JOIN travel_order_vehicle tov ON tov.travel_order_id = tor.id_travel_order
                LEFT JOIN vehicle v ON v.id_vehicle = tov.vehicle_id
                LEFT JOIN driver_travel_order dto ON dto.travel_order_id = tor.id_travel_order
                LEFT JOIN driver d ON d.id_driver = dto.driver_id
                LEFT JOIN travel_order_report_snapshot existing
                    ON existing.travel_order_id = tor.id_travel_order
                WHERE tor.work_order_number LIKE 'BENCH-LARGE-%'
                  AND existing.travel_order_id IS NULL
                GROUP BY
                    tor.id_travel_order, tor.travel_order_number, tor.work_order_number,
                    tor.date_from, tor.date_to, tor.status, lu.location_name,
                    tor.starting_mileage, tor.ending_mileage
                """);
        return (int) queryCount(
                "SELECT COUNT(*) FROM travel_order_report_snapshot tors " +
                        "INNER JOIN travel_order tor ON tor.id_travel_order = tors.travel_order_id " +
                        "WHERE tor.work_order_number LIKE 'BENCH-LARGE-%'");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Deletes travel orders and all dependent junction/snapshot rows.
     *
     * @param likePattern  work_order_number LIKE pattern to delete
     * @param notLike      if non-null, excludes this pattern (e.g. protect large dataset
     *                     when clearing small, or vice-versa)
     */
    private void deleteOrderRelatedData(String likePattern, String notLike) {
        String whereClause = notLike != null
                ? "WHERE tor.work_order_number LIKE ? AND tor.work_order_number NOT LIKE ?"
                : "WHERE tor.work_order_number LIKE ?";

        // vehicle_location rows for this dataset's vehicles are deleted separately

        jdbcTemplate.update(String.format("""
                DELETE tors FROM travel_order_report_snapshot tors
                INNER JOIN travel_order tor ON tor.id_travel_order = tors.travel_order_id
                %s""", whereClause),
                notLike != null ? new Object[]{likePattern, notLike} : new Object[]{likePattern});

        jdbcTemplate.update(String.format("""
                DELETE dto FROM driver_travel_order dto
                INNER JOIN travel_order tor ON tor.id_travel_order = dto.travel_order_id
                %s""", whereClause),
                notLike != null ? new Object[]{likePattern, notLike} : new Object[]{likePattern});

        jdbcTemplate.update(String.format("""
                DELETE tov FROM travel_order_vehicle tov
                INNER JOIN travel_order tor ON tor.id_travel_order = tov.travel_order_id
                %s""", whereClause),
                notLike != null ? new Object[]{likePattern, notLike} : new Object[]{likePattern});

        if (notLike != null) {
            jdbcTemplate.update(
                    "DELETE FROM travel_order WHERE work_order_number LIKE ? " +
                            "AND work_order_number NOT LIKE ?",
                    likePattern, notLike);
        } else {
            jdbcTemplate.update(
                    "DELETE FROM travel_order WHERE work_order_number LIKE ?",
                    likePattern);
        }

        // vehicle_location and driver_location for this SAP range
        long sapStartVeh = likePattern.startsWith("BENCH-LARGE") ? VEHICLE_SAP_START_LARGE : VEHICLE_SAP_START;
        long sapStartDrv = likePattern.startsWith("BENCH-LARGE") ? DRIVER_SAP_START_LARGE  : DRIVER_SAP_START;

        jdbcTemplate.update("""
                DELETE vl FROM vehicle_location vl
                INNER JOIN vehicle v ON v.id_vehicle = vl.vehicle_id
                WHERE v.sap_number BETWEEN ? AND ?""",
                sapStartVeh, sapStartVeh + NUM_VEHICLES - 1);

        jdbcTemplate.update("""
                DELETE dl FROM driver_location dl
                INNER JOIN driver d ON d.id_driver = dl.driver_id
                WHERE d.sap_number BETWEEN ? AND ?""",
                sapStartDrv, sapStartDrv + NUM_DRIVERS - 1);
    }

    private List<Long> getBenchmarkLocationIds() {
        return jdbcTemplate.queryForList(
                "SELECT id_location_unit FROM location_unit " +
                        "WHERE location_name LIKE 'BenchmarkLocation-%' ORDER BY id_location_unit",
                Long.class);
    }

    private List<Long> getVehicleIds(long sapStart, int count) {
        return jdbcTemplate.queryForList(
                "SELECT id_vehicle FROM vehicle WHERE sap_number BETWEEN ? AND ? ORDER BY id_vehicle",
                Long.class, sapStart, sapStart + count - 1);
    }

    private List<Long> getDriverIds(long sapStart, int count) {
        return jdbcTemplate.queryForList(
                "SELECT id_driver FROM driver WHERE sap_number BETWEEN ? AND ? ORDER BY id_driver",
                Long.class, sapStart, sapStart + count - 1);
    }

    private void validatePrerequisites(List<Long> fuelTypeIds,
                                       List<Long> vehicleModelIds,
                                       Long userId) {
        if (fuelTypeIds.isEmpty() || vehicleModelIds.isEmpty() || userId == null) {
            throw new IllegalStateException(
                    "Cannot generate benchmark data: seed data is missing. " +
                            "Ensure fuel_type, vehicle_model, and app_user have at least one row each.");
        }
    }

    private void batchInsert(String sql, List<Object[]> batch) {
        if (batch.isEmpty()) return;
        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
            List<Object[]> chunk = batch.subList(i, Math.min(i + BATCH_SIZE, batch.size()));
            jdbcTemplate.batchUpdate(sql, chunk);
        }
    }

    private long queryCount(String sql) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}