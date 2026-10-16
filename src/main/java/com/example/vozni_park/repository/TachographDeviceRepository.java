package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TachographDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TachographDeviceRepository extends JpaRepository<TachographDevice, Long> {
}
