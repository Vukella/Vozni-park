package com.example.vozni_park.service;

import com.example.vozni_park.entity.Role;
import com.example.vozni_park.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    /**
     * Get all roles
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * Get role by ID
     */
    @Transactional(readOnly = true)
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    /**
     * Get role by name
     */
    @Transactional(readOnly = true)
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
    
    /**
     * Create new role
     */
    public Role createRole(Role role) {
        // Validation: Check if role with same name already exists
        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("Role with name '" + role.getName() + "' already exists");
        }
        
        return roleRepository.save(role);
    }
    
    /**
     * Update existing role
     */
    public Role updateRole(Long id, Role roleDetails) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        
        // Check if new name conflicts with existing role
        if (!role.getName().equals(roleDetails.getName()) && 
            roleRepository.existsByName(roleDetails.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDetails.getName() + "' already exists");
        }
        
        role.setName(roleDetails.getName());
        role.setDescription(roleDetails.getDescription());
        
        return roleRepository.save(role);
    }
    
    /**
     * Delete role by ID
     */
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role not found with id: " + id);
        }
        
        roleRepository.deleteById(id);
    }
    
    /**
     * Check if role exists
     */
    @Transactional(readOnly = true)
    public boolean roleExists(Long id) {
        return roleRepository.existsById(id);
    }
}
