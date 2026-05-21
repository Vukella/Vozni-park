package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TravelOrderFuelReceipt;
import com.example.vozni_park.entity.embeddable.TravelOrderFuelReceiptId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelOrderFuelReceiptRepository
        extends JpaRepository<TravelOrderFuelReceipt, TravelOrderFuelReceiptId> {

    @Query("SELECT t FROM TravelOrderFuelReceipt t WHERE t.travelOrder.idTravelOrder = :travelOrderId")
    List<TravelOrderFuelReceipt> findByTravelOrderId(@Param("travelOrderId") Long travelOrderId);
}