package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tachograph_card")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TachographCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tachograph_card")
    private Long idTachographCard;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "status_code")
    private Integer statusCode;

    @OneToOne(mappedBy = "tachographCard")
    private DriversLicense driversLicense;
}
