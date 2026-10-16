package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "fire_extinguisher")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FireExtinguisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fire_extinguisher")
    private Long idFireExtinguisher;

    @Column(name = "serial_number", length = 50, nullable = false)
    private String serialNumber;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "status_code")
    private Integer statusCode;

    @OneToOne(mappedBy = "fireExtinguisher")
    private Vehicle vehicle;
}
