package com.example.vozni_park.repository;

import com.example.vozni_park.entity.SystemRefresh;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemRefreshRepository extends JpaRepository<SystemRefresh, String> {

    /**
     * Acquires a pessimistic write lock on the row before reading it.
     * Any other instance that tries to run the same job will block here
     * until the first one commits, at which point it will see the updated
     * last_run_ts and skip the job.
     *
     * This is the distributed coordination mechanism used by StatusRefreshService.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM SystemRefresh r WHERE r.keyName = :keyName")
    Optional<SystemRefresh> findByKeyNameWithLock(@Param("keyName") String keyName);
}
