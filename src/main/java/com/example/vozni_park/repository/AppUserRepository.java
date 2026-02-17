package com.example.vozni_park.repository;

import com.example.vozni_park.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    /**
     * Find user by username with eagerly fetched relationships
     * Used for authentication to avoid lazy loading issues
     */
    @Query("SELECT u FROM AppUser u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.userLocations ul " +
            "LEFT JOIN FETCH ul.location " +  // ✅ FIXED: location, not locationUnit
            "WHERE u.username = :username")
    Optional<AppUser> findByUsernameWithRelations(@Param("username") String username);

    /**
     * Find user by ID with relationships eagerly loaded
     */
    @Query("SELECT u FROM AppUser u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.userLocations ul " +
            "LEFT JOIN FETCH ul.location " +  // ✅ FIXED: location, not locationUnit
            "WHERE u.idUser = :userId")
    Optional<AppUser> findByIdWithRelations(@Param("userId") Long userId);

    boolean existsByUsername(String username);

    List<AppUser> findByRoleId(Long roleId);

    @Query("SELECT u FROM AppUser u WHERE u.role.name = :roleName")
    List<AppUser> findByRoleName(@Param("roleName") String roleName);

    List<AppUser> findByIsActive(Boolean isActive);

    @Query("SELECT u FROM AppUser u JOIN u.userLocations ul WHERE ul.location.idLocationUnit = :locationId")  // ✅ FIXED
    List<AppUser> findByLocationId(@Param("locationId") Long locationId);
}