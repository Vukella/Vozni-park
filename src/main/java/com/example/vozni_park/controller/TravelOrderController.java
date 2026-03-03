package com.example.vozni_park.controller;

import com.example.vozni_park.dto.request.TravelOrderRequestDTO;
import com.example.vozni_park.dto.response.TravelOrderResponseDTO;
import com.example.vozni_park.service.TravelOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/travel-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Travel Order Management", description = "APIs for managing travel orders")
public class TravelOrderController {

    private final TravelOrderService travelOrderService;

    @GetMapping
    @Operation(summary = "Get all travel orders")
    public ResponseEntity<List<TravelOrderResponseDTO>> getAllTravelOrders() {
        return ResponseEntity.ok(travelOrderService.getAllTravelOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get travel order by ID")
    public ResponseEntity<TravelOrderResponseDTO> getTravelOrderById(@PathVariable Long id) {
        return travelOrderService.getTravelOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/work-order/{workOrderNumber}")
    @Operation(summary = "Get travel order by work order number")
    public ResponseEntity<TravelOrderResponseDTO> getTravelOrderByWorkOrderNumber(@PathVariable String workOrderNumber) {
        return travelOrderService.getTravelOrderByWorkOrderNumber(workOrderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get travel orders by status")
    public ResponseEntity<List<TravelOrderResponseDTO>> getTravelOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByStatus(status));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get travel orders by location")
    public ResponseEntity<List<TravelOrderResponseDTO>> getTravelOrdersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByLocation(locationId));
    }

    @GetMapping("/location/{locationId}/active")
    @Operation(summary = "Get active travel orders by location")
    public ResponseEntity<List<TravelOrderResponseDTO>> getActiveTravelOrdersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(travelOrderService.getActiveTravelOrdersByLocation(locationId));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get travel orders by driver")
    public ResponseEntity<List<TravelOrderResponseDTO>> getTravelOrdersByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByDriver(driverId));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get travel orders by vehicle")
    public ResponseEntity<List<TravelOrderResponseDTO>> getTravelOrdersByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByVehicle(vehicleId));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get travel orders by date range")
    public ResponseEntity<List<TravelOrderResponseDTO>> getTravelOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByDateRange(startDate, endDate));
    }

    @PostMapping
    @Operation(summary = "Create new travel order")
    public ResponseEntity<?> createTravelOrder(
            @Valid @RequestBody TravelOrderRequestDTO travelOrderDTO,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();
            TravelOrderResponseDTO created = travelOrderService.createTravelOrder(travelOrderDTO, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update travel order")
    public ResponseEntity<?> updateTravelOrder(@PathVariable Long id, @Valid @RequestBody TravelOrderRequestDTO travelOrderDTO) {
        try {
            TravelOrderResponseDTO updated = travelOrderService.updateTravelOrder(id, travelOrderDTO);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update travel order status")
    public ResponseEntity<?> updateTravelOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            TravelOrderResponseDTO updated = travelOrderService.updateTravelOrderStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete travel order")
    public ResponseEntity<?> completeTravelOrder(
            @PathVariable Long id,
            @RequestParam Long endingMileage) {
        try {
            TravelOrderResponseDTO completed = travelOrderService.completeTravelOrder(id, endingMileage);
            return ResponseEntity.ok(completed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete travel order")
    public ResponseEntity<?> deleteTravelOrder(@PathVariable Long id) {
        try {
            travelOrderService.deleteTravelOrder(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}