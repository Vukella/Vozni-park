package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle")
    private Long idVehicle;

    @Column(name = "sap_number")
    private Long sapNumber;

    @Column(name = "chassis_number", length = 50)
    private String chassisNumber;

    @Column(name = "engine_number")
    private Long engineNumber;

    @Column(name = "tag_serial_number")
    private Long tagSerialNumber;

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    @Column(name = "engine_displacement", precision = 10, scale = 2)
    private BigDecimal engineDisplacement;

    @Column(name = "power")
    private Integer power;

    @Column(name = "tire_marking", length = 50)
    private String tireMarking;

    @Column(name = "fire_extinguisher_serial_number")
    private Long fireExtinguisherSerialNumber;

    @Column(name = "vehicle_status", length = 20)
    private String vehicleStatus;

    @Column(name = "status_code")
    private Integer statusCode;

    // Foreign Key columns (for insertable/updatable control)
    @Column(name = "registration_id", insertable = false, updatable = false)
    private Long registrationId;

    @Column(name = "fuel_type_id", insertable = false, updatable = false)
    private Long fuelTypeId;

    @Column(name = "first_aid_kit_id", insertable = false, updatable = false)
    private Long firstAidKitId;

    @Column(name = "vehicle_model_id", insertable = false, updatable = false)
    private Long vehicleModelId;

    // Relationships
    @OneToOne
    @JoinColumn(name = "registration_id")
    private Registration registration;

    @ManyToOne
    @JoinColumn(name = "fuel_type_id")
    private FuelType fuelType;

    @OneToOne
    @JoinColumn(name = "first_aid_kit_id")
    private FirstAidKit firstAidKit;

    @ManyToOne
    @JoinColumn(name = "vehicle_model_id")
    private VehicleModel vehicleModel;

    @OneToOne(mappedBy = "vehicle")
    private VehicleLocation vehicleLocation;

    @OneToMany(mappedBy = "vehicle")
    private List<TravelOrderVehicle> travelOrderVehicles;
}
