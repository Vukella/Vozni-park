package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "travel_order",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_travel_order_work_order_number", columnNames = "work_order_number"),
                @UniqueConstraint(name = "uq_travel_order_location_number", columnNames = {"location_id", "travel_order_number"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_travel_order")
    private Long idTravelOrder;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "work_order_number", length = 30, unique = true)
    private String workOrderNumber;

    @Column(name = "travel_order_number", length = 20)
    private String travelOrderNumber;

    @Column(name = "starting_mileage")
    private Long startingMileage;

    @Column(name = "ending_mileage")
    private Long endingMileage;

    /**
     * Human-readable status label. Values: ACTIVE, UNFINISHED, COMPLETED, CANCELLED.
     * Managed exclusively by StatusCalculationService — never set directly in other services.
     */
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    /**
     * Numeric status code for queries. Values: 0=CANCELLED, 1=ACTIVE, 2=UNFINISHED, 3=COMPLETED.
     * Managed exclusively by StatusCalculationService — never set directly in other services.
     */
    @Column(name = "status_code")
    private Integer statusCode = 1;

    @CreationTimestamp
    @Column(name = "creation_time", updatable = false)
    private LocalDateTime creationTime;

    @Column(name = "created_by_user_id", insertable = false, updatable = false)
    private Long createdByUserId;

    @Column(name = "travel_order_type_id", insertable = false, updatable = false)
    private Long travelOrderTypeId;

    // ── Relationships ─────────────────────────────────────────────────

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private AppUser createdByUser;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private LocationUnit location;

    @ManyToOne
    @JoinColumn(name = "travel_order_type_id")
    private TravelOrderType travelOrderType;

    @OneToMany(mappedBy = "travelOrder")
    private List<DriverTravelOrder> driverTravelOrders;

    @OneToMany(mappedBy = "travelOrder")
    private List<TravelOrderVehicle> travelOrderVehicles;
}
