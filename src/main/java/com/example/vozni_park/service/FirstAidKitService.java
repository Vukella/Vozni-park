package com.example.vozni_park.service;

import com.example.vozni_park.entity.FirstAidKit;
import com.example.vozni_park.repository.FirstAidKitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FirstAidKitService {
    
    private final FirstAidKitRepository firstAidKitRepository;
    
    @Transactional(readOnly = true)
    public List<FirstAidKit> getAllFirstAidKits() {
        return firstAidKitRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<FirstAidKit> getFirstAidKitById(Long id) {
        return firstAidKitRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<FirstAidKit> getFirstAidKitsByStatus(String status) {
        return firstAidKitRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<FirstAidKit> getExpiringSoon(int days) {
        LocalDate now = LocalDate.now();
        return firstAidKitRepository.findExpiringSoon(now, now.plusDays(days));
    }
    
    @Transactional(readOnly = true)
    public List<FirstAidKit> getExpired() {
        return firstAidKitRepository.findExpired(LocalDate.now());
    }
    
    public FirstAidKit createFirstAidKit(FirstAidKit firstAidKit) {
        return firstAidKitRepository.save(firstAidKit);
    }
    
    public FirstAidKit updateFirstAidKit(Long id, FirstAidKit kitDetails) {
        FirstAidKit kit = firstAidKitRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("First aid kit not found with id: " + id));
        
        kit.setExpiryDate(kitDetails.getExpiryDate());
        kit.setStatus(kitDetails.getStatus());
        kit.setStatusCode(kitDetails.getStatusCode());
        
        return firstAidKitRepository.save(kit);
    }
    
    public void deleteFirstAidKit(Long id) {
        if (!firstAidKitRepository.existsById(id)) {
            throw new IllegalArgumentException("First aid kit not found with id: " + id);
        }
        firstAidKitRepository.deleteById(id);
    }
}
