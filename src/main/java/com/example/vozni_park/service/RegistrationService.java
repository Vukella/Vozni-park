package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.RegistrationResponseDTO;
import com.example.vozni_park.entity.Registration;
import com.example.vozni_park.mapper.RegistrationMapper;
import com.example.vozni_park.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final RegistrationMapper registrationMapper;

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getAllRegistrations() {
        return registrationMapper.toResponseDTOList(registrationRepository.findAll());
    }

    @Transactional(readOnly = true)
    public RegistrationResponseDTO getRegistrationById(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + id));
        return registrationMapper.toResponseDTO(registration);
    }

    @Transactional(readOnly = true)
    public RegistrationResponseDTO getRegistrationByNumber(String registrationNumber) {
        Registration registration = registrationRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with number: " + registrationNumber));
        return registrationMapper.toResponseDTO(registration);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getRegistrationsByStatus(String status) {
        return registrationMapper.toResponseDTOList(registrationRepository.findByStatus(status));
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getExpiringSoon(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return registrationMapper.toResponseDTOList(
                registrationRepository.findExpiringSoon(LocalDate.now(), cutoff) // ← changed
        );
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getExpired() {
        return registrationMapper.toResponseDTOList(
                registrationRepository.findExpired(LocalDate.now()) // ← changed
        );
    }

    public RegistrationResponseDTO createRegistration(Registration registration) {
        return registrationMapper.toResponseDTO(registrationRepository.save(registration));
    }

    public RegistrationResponseDTO updateRegistration(Long id, Registration registrationDetails) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + id));
        registration.setRegistrationNumber(registrationDetails.getRegistrationNumber());
        registration.setDateFrom(registrationDetails.getDateFrom());
        registration.setDateTo(registrationDetails.getDateTo());
        registration.setPolicyNumber(registrationDetails.getPolicyNumber());
        registration.setStatus(registrationDetails.getStatus());
        registration.setStatusCode(registrationDetails.getStatusCode());
        return registrationMapper.toResponseDTO(registrationRepository.save(registration));
    }

    public void deleteRegistration(Long id) {
        if (!registrationRepository.existsById(id)) {
            throw new IllegalArgumentException("Registration not found with id: " + id);
        }
        registrationRepository.deleteById(id);
    }
}