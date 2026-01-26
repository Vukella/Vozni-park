package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "first_aid_kit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirstAidKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_first_aid_kit")
    private Long idFirstAidKit;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "status_code")
    private Integer statusCode;

    // Relationships
    @OneToOne(mappedBy = "firstAidKit")
    private Vehicle vehicle;
}
