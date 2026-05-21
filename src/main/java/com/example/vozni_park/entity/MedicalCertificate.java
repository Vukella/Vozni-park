package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "medical_certificate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medical_certificate")
    private Long idMedicalCertificate;

    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "status_code")
    private Integer statusCode;

    @OneToOne(mappedBy = "medicalCertificate")
    private Driver driver;
}
