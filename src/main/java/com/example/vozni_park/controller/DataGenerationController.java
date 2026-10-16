package com.example.vozni_park.controller;

import com.example.vozni_park.dto.DataGenerationResult;
import com.example.vozni_park.service.DataGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for benchmark data management.
 *
 * Endpoints:
 *   POST   /api/benchmark/generate  — generates 50k travel orders and all related data
 *   DELETE /api/benchmark/data      — removes all benchmark data (safe to call if empty)
 *
 * These endpoints are intended for local/dev use only and should not be
 * exposed in a production deployment.
 */
@RestController
@RequestMapping("/api/benchmark")
@RequiredArgsConstructor
@Slf4j
public class DataGenerationController {

    private final DataGenerationService dataGenerationService;

    /**
     * Triggers bulk benchmark data generation.
     * Idempotent — returns immediately if data already exists without re-generating.
     *
     * Expected runtime: 15–60 seconds depending on hardware.
     *
     * Response fields:
     *   locationsGenerated    — number of location_unit rows inserted
     *   vehiclesGenerated     — number of vehicle rows inserted
     *   driversGenerated      — number of driver rows inserted
     *   travelOrdersGenerated — number of travel_order rows inserted (target: 50,000)
     *   snapshotsGenerated    — number of travel_order_report_snapshot rows inserted
     *   elapsedMillis         — wall-clock time for the entire generation
     *   alreadyExists         — true if generation was skipped (data was present)
     */
    @PostMapping("/generate")
    public ResponseEntity<DataGenerationResult> generate() {
        log.info("POST /api/benchmark/generate — starting data generation");
        DataGenerationResult result = dataGenerationService.generateBenchmarkData();

        if (result.alreadyExists()) {
            log.info("Benchmark data already present — returning existing count");
        } else {
            log.info("Generation complete — {} orders in {}ms",
                    result.travelOrdersGenerated(), result.elapsedMillis());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Removes all benchmark data from the database.
     * Deletes in FK-safe order. Safe to call even if no benchmark data exists.
     */
    @DeleteMapping("/data")
    public ResponseEntity<Map<String, String>> clearData() {
        log.info("DELETE /api/benchmark/data — clearing benchmark data");
        dataGenerationService.clearBenchmarkData();
        return ResponseEntity.ok(Map.of("status", "cleared"));
    }

    @PostMapping("/generate-large")
    public ResponseEntity<DataGenerationResult> generateLarge() {
        return ResponseEntity.ok(dataGenerationService.generateLargeBenchmarkData());
    }

    @DeleteMapping("/data-large")
    public ResponseEntity<Void> clearLargeData() {
        dataGenerationService.clearLargeBenchmarkData();
        return ResponseEntity.noContent().build();
    }
}