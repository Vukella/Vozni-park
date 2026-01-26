package com.example.vozni_park.entity;

import com.example.vozni_park.entity.embeddable.TravelOrderCounterId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travel_order_counter")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelOrderCounter {

    @EmbeddedId
    private TravelOrderCounterId id;

    @Column(name = "last_number", nullable = false)
    private Integer lastNumber;

    // Relationships
    @ManyToOne
    @MapsId("locationId")
    @JoinColumn(name = "location_id", nullable = false)
    private LocationUnit location;
}
