package com.example.vozni_park.service;

import com.example.vozni_park.entity.Registration;
import com.example.vozni_park.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {
    
    private final RegistrationRepository registrationRepository;
    
    @Transactional(readOnly = true)
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Registration> getRegistrationById(Long id) {
        return registrationRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Registration> getRegistrationByNumber(String registrationNumber) {
        return registrationRepository.findByRegistrationNumber(registrationNumber);
    }
    
    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsByStatus(String status) {
        return registrationRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Registration> getExpiringSoon(int days) {
        LocalDate now = LocalDate.now();
        return registrationRepository.findExpiringSoon(now, now.plusDays(days));
    }
    
    @Transactional(readOnly = true)
    public List<Registration> getExpired() {
        return registrationRepository.findExpired(LocalDate.now());
    }
    
    public Registration createRegistration(Registration registration) {
        if (registration.getDateTo() != null && registration.getDateFrom() != null &&
            registration.getDateTo().isBefore(registration.getDateFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        return registrationRepository.save(registration);
    }
    
    public Registration updateRegistration(Long id, Registration registrationDetails) {
        Registration registration = registrationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + id));
        
        if (registrationDetails.getDateTo() != null && registrationDetails.getDateFrom() != null &&
            registrationDetails.getDateTo().isBefore(registrationDetails.getDateFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        
        registration.setRegistrationNumber(registrationDetails.getRegistrationNumber());
        registration.setDateFrom(registrationDetails.getDateFrom());
        registration.setDateTo(registrationDetails.getDateTo());
        registration.setPolicyNumber(registrationDetails.getPolicyNumber());
        registration.setStatus(registrationDetails.getStatus());
        registration.setStatusCode(registrationDetails.getStatusCode());
        
        return registrationRepository.save(registration);
    }
    
    public void deleteRegistration(Long id) {
        if (!registrationRepository.existsById(id)) {
            throw new IllegalArgumentException("Registration not found with id: " + id);
        }
        registrationRepository.deleteById(id);
    }
}
