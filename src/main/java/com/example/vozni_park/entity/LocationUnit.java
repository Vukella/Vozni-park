package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "location_unit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_location_unit")
    private Long idLocationUnit;

    @Column(name = "location_address", length = 500)
    private String locationAddress;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    // Relationships
    @OneToMany(mappedBy = "locationUnit")
    private List<VehicleLocation> vehicleLocations;

    @OneToMany(mappedBy = "locationUnit")
    private List<DriverLocation> driverLocations;

    @OneToMany(mappedBy = "location")
    private List<TravelOrder> travelOrders;

    @OneToMany(mappedBy = "location")
    private List<UserLocation> userLocations;
}
