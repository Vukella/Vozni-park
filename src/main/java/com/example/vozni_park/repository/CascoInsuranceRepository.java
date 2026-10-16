// ── CascoInsuranceRepository ──────────────────────────────────────────
package com.example.vozni_park.repository;

import com.example.vozni_park.entity.CascoInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CascoInsuranceRepository extends JpaRepository<CascoInsurance, Long> {
}
