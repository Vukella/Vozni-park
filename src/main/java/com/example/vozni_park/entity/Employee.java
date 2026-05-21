package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zaposleni")
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ZAPOSLENI")
    private Long idEmployee;

    @Column(name = "SAP_NUMBER", nullable = false, unique = true)
    private Long sapNumber;

    @Column(name = "FULL_NAME", nullable = false, length = 60)
    private String fullName;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Column(name = "DEPARTMENT", length = 50)
    private String department;

    @Column(name = "POSITION", length = 50)
    private String position;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Integer isActive = 1;

    @Column(name = "organizational_unit", length = 100)
    private String organizationalUnit;

    @Column(name = "job_grade", length = 50)
    private String jobGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;
}