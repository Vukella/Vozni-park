package com.example.vozni_park.entity;

import com.example.vozni_park.entity.embeddable.UserLocationId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocation {

    @EmbeddedId
    private UserLocationId id;

    // Relationships
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @MapsId("locationId")
    @JoinColumn(name = "location_id", nullable = false)
    private LocationUnit location;
}
