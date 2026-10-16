package com.example.vozni_park.repository;

import com.example.vozni_park.entity.FireExtinguisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FireExtinguisherRepository extends JpaRepository<FireExtinguisher, Long> {
}
