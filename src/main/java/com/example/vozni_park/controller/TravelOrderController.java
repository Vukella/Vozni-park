package com.example.vozni_park.controller;

import com.example.vozni_park.entity.TravelOrder;
import com.example.vozni_park.service.TravelOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/travel-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TravelOrderController {
    
    private final TravelOrderService travelOrderService;
    
    @GetMapping
    public ResponseEntity<List<TravelOrder>> getAllTravelOrders() {
        return ResponseEntity.ok(travelOrderService.getAllTravelOrders());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TravelOrder> getTravelOrderById(@PathVariable Long id) {
        return travelOrderService.getTravelOrderById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/work-order/{workOrderNumber}")
    public ResponseEntity<TravelOrder> getTravelOrderByWorkOrderNumber(@PathVariable String workOrderNumber) {
        return travelOrderService.getTravelOrderByWorkOrderNumber(workOrderNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TravelOrder>> getTravelOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByStatus(status));
    }
    
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<TravelOrder>> getTravelOrdersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByLocation(locationId));
    }
    
    @GetMapping("/location/{locationId}/active")
    public ResponseEntity<List<TravelOrder>> getActiveTravelOrdersByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(travelOrderService.getActiveTravelOrdersByLocation(locationId));
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TravelOrder>> getTravelOrdersByDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByDriver(driverId));
    }
    
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<TravelOrder>> getTravelOrdersByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByVehicle(vehicleId));
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<TravelOrder>> getTravelOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(travelOrderService.getTravelOrdersByDateRange(startDate, endDate));
    }
    
    @PostMapping
    public ResponseEntity<?> createTravelOrder(@RequestBody TravelOrder travelOrder) {
        try {
            TravelOrder created = travelOrderService.createTravelOrder(travelOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTravelOrder(@PathVariable Long id, @RequestBody TravelOrder travelOrder) {
        try {
            TravelOrder updated = travelOrderService.updateTravelOrder(id, travelOrder);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTravelOrderStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            TravelOrder updated = travelOrderService.updateTravelOrderStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeTravelOrder(
            @PathVariable Long id, 
            @RequestParam Long endingMileage) {
        try {
            TravelOrder completed = travelOrderService.completeTravelOrder(id, endingMileage);
            return ResponseEntity.ok(completed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTravelOrder(@PathVariable Long id) {
        try {
            travelOrderService.deleteTravelOrder(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
