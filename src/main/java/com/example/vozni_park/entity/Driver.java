package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "driver")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_driver")
    private Long idDriver;

    @Column(name = "sap_number", nullable = false, unique = true)
    private Long sapNumber;

    @Column(name = "full_name", length = 60)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "status_code")
    private Integer statusCode;

    // Relationships
    @OneToOne(mappedBy = "driver")
    private DriverLocation driverLocation;

    @OneToMany(mappedBy = "driver")
    private List<DriverLicenseAssignment> driverLicenseAssignments;

    @OneToMany(mappedBy = "driver")
    private List<DriverTravelOrder> driverTravelOrders;
}
