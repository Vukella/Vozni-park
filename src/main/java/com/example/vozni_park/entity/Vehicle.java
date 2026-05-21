package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // ── Existing columns ──────────────────────────────────────────────

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

    /** Legacy column — serial number stored as plain value. Replaced by fire_extinguisher relationship. */
    @Column(name = "fire_extinguisher_serial_number")
    private Long fireExtinguisherSerialNumber;

    @Column(name = "vehicle_status", length = 20)
    private String vehicleStatus;

    @Column(name = "status_code")
    private Integer statusCode;

    // ── New physical columns (changeset 721) ──────────────────────────

    @Column(name = "mass", precision = 10, scale = 2)
    private BigDecimal mass;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "load_capacity")
    private Integer loadCapacity;

    @Column(name = "total_mass")
    private Integer totalMass;

    @Column(name = "number_of_seats")
    private Integer numberOfSeats;

    @Column(name = "first_registration_date")
    private LocalDate firstRegistrationDate;

    /**
     * 1 = vehicle is on operational leasing.
     */
    @Column(name = "operational_leasing")
    private Integer operationalLeasing = 0;

    /**
     * 1 = vehicle is permanently decommissioned and removed from all operational queries.
     * This is a flag, not a status code — decommissioned vehicles do not appear
     * in vehicle selection lists regardless of other conditions.
     */
    @Column(name = "decommissioned")
    private Integer decommissioned = 0;

    @Column(name = "decommission_date")
    private LocalDate decommissionDate;

    @Column(name = "note", length = 300)
    private String note;

    // ── FK columns for new relationships (insertable=false, updatable=false) ─

    @Column(name = "registration_id", insertable = false, updatable = false)
    private Long registrationId;

    @Column(name = "fuel_type_id", insertable = false, updatable = false)
    private Long fuelTypeId;

    @Column(name = "first_aid_kit_id", insertable = false, updatable = false)
    private Long firstAidKitId;

    @Column(name = "vehicle_model_id", insertable = false, updatable = false)
    private Long vehicleModelId;

    @Column(name = "vehicle_type_id", insertable = false, updatable = false)
    private Long vehicleTypeId;

    @Column(name = "vehicle_subtype_id", insertable = false, updatable = false)
    private Long vehicleSubtypeId;

    @Column(name = "tachograph_device_id", insertable = false, updatable = false)
    private Long tachographDeviceId;

    @Column(name = "casco_insurance_id", insertable = false, updatable = false)
    private Long cascoInsuranceId;

    @Column(name = "fire_extinguisher_id", insertable = false, updatable = false)
    private Long fireExtinguisherId;

    @Column(name = "nis_card_id", insertable = false, updatable = false)
    private Long nisCardId;

    // ── Existing relationships ─────────────────────────────────────────

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

    // ── New relationships (changeset 722) ─────────────────────────────

    @OneToOne
    @JoinColumn(name = "casco_insurance_id")
    private CascoInsurance cascoInsurance;

    @OneToOne
    @JoinColumn(name = "tachograph_device_id")
    private TachographDevice tachographDevice;

    @OneToOne
    @JoinColumn(name = "fire_extinguisher_id")
    private FireExtinguisher fireExtinguisher;

    @OneToOne
    @JoinColumn(name = "nis_card_id")
    private NisCard nisCard;

    @OneToMany(mappedBy = "vehicle")
    private List<VehicleFault> vehicleFaults;
}
