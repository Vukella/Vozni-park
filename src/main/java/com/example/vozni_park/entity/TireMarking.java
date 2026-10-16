package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tire_marking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TireMarking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tire_marking")
    private Long idTireMarking;

    @Column(name = "name", length = 100)
    private String name;

    /**
     * Tire size code, e.g. "205/55R16"
     */
    @Column(name = "marking", length = 100)
    private String marking;
}
