package com.example.vozni_park.repository;

import com.example.vozni_park.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    
    // Custom query methods
    Optional<AppUser> findByUsername(String username);
    
    List<AppUser> findByRoleId(Long roleId);
    
    List<AppUser> findByIsActive(Boolean isActive);
    
    // Find users by location
    @Query("SELECT u FROM AppUser u JOIN u.userLocations ul WHERE ul.location.idLocationUnit = :locationId")
    List<AppUser> findByLocationId(Long locationId);
    
    // Find users by role name
    @Query("SELECT u FROM AppUser u WHERE u.role.name = :roleName")
    List<AppUser> findByRoleName(String roleName);
    
    boolean existsByUsername(String username);
}
