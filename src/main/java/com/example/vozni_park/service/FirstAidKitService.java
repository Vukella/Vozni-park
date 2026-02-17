package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.FirstAidKitResponseDTO;
import com.example.vozni_park.entity.FirstAidKit;
import com.example.vozni_park.mapper.FirstAidKitMapper;
import com.example.vozni_park.repository.FirstAidKitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FirstAidKitService {

    private final FirstAidKitRepository firstAidKitRepository;
    private final FirstAidKitMapper firstAidKitMapper;

    @Transactional(readOnly = true)
    public List<FirstAidKitResponseDTO> getAllFirstAidKits() {
        return firstAidKitMapper.toResponseDTOList(firstAidKitRepository.findAll());
    }

    @Transactional(readOnly = true)
    public FirstAidKitResponseDTO getFirstAidKitById(Long id) {
        FirstAidKit kit = firstAidKitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("First aid kit not found with id: " + id));
        return firstAidKitMapper.toResponseDTO(kit);
    }

    @Transactional(readOnly = true)
    public List<FirstAidKitResponseDTO> getFirstAidKitsByStatus(String status) {
        return firstAidKitMapper.toResponseDTOList(firstAidKitRepository.findByStatus(status));
    }

    @Transactional(readOnly = true)
    public List<FirstAidKitResponseDTO> getExpiringSoon(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return firstAidKitMapper.toResponseDTOList(
                firstAidKitRepository.findExpiringSoon(LocalDate.now(), cutoff) // ← changed
        );
    }

    @Transactional(readOnly = true)
    public List<FirstAidKitResponseDTO> getExpired() {
        return firstAidKitMapper.toResponseDTOList(
                firstAidKitRepository.findExpired(LocalDate.now()) // ← changed
        );
    }

    public FirstAidKitResponseDTO createFirstAidKit(FirstAidKit firstAidKit) {
        return firstAidKitMapper.toResponseDTO(firstAidKitRepository.save(firstAidKit));
    }

    public FirstAidKitResponseDTO updateFirstAidKit(Long id, FirstAidKit kitDetails) {
        FirstAidKit kit = firstAidKitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("First aid kit not found with id: " + id));
        kit.setExpiryDate(kitDetails.getExpiryDate());
        kit.setStatus(kitDetails.getStatus());
        kit.setStatusCode(kitDetails.getStatusCode());
        return firstAidKitMapper.toResponseDTO(firstAidKitRepository.save(kit));
    }

    public void deleteFirstAidKit(Long id) {
        if (!firstAidKitRepository.existsById(id)) {
            throw new IllegalArgumentException("First aid kit not found with id: " + id);
        }
        firstAidKitRepository.deleteById(id);
    }
}