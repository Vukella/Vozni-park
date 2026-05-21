package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nis_card")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NisCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nis_card")
    private Long idNisCard;

    @Column(name = "card_number", length = 30, nullable = false)
    private String cardNumber;

    @OneToOne(mappedBy = "nisCard")
    private Vehicle vehicle;
}
