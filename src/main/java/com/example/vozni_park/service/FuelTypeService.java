package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.FuelTypeResponseDTO;
import com.example.vozni_park.entity.FuelType;
import com.example.vozni_park.mapper.FuelTypeMapper;
import com.example.vozni_park.repository.FuelTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FuelTypeService {

    private final FuelTypeRepository fuelTypeRepository;
    private final FuelTypeMapper fuelTypeMapper;

    @Transactional(readOnly = true)
    public List<FuelTypeResponseDTO> getAllFuelTypes() {
        return fuelTypeMapper.toResponseDTOList(fuelTypeRepository.findAll());
    }

    @Transactional(readOnly = true)
    public FuelTypeResponseDTO getFuelTypeById(Long id) {
        FuelType fuelType = fuelTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fuel type not found with id: " + id));
        return fuelTypeMapper.toResponseDTO(fuelType);
    }

    public FuelTypeResponseDTO createFuelType(String name) {
        if (fuelTypeRepository.existsByName(name)) {
            throw new IllegalArgumentException("Fuel type with name '" + name + "' already exists");
        }
        FuelType fuelType = new FuelType();
        fuelType.setName(name);
        return fuelTypeMapper.toResponseDTO(fuelTypeRepository.save(fuelType));
    }

    public FuelTypeResponseDTO updateFuelType(Long id, String name) {
        FuelType fuelType = fuelTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fuel type not found with id: " + id));
        if (!fuelType.getName().equals(name) && fuelTypeRepository.existsByName(name)) {
            throw new IllegalArgumentException("Fuel type with name '" + name + "' already exists");
        }
        fuelType.setName(name);
        return fuelTypeMapper.toResponseDTO(fuelTypeRepository.save(fuelType));
    }

    public void deleteFuelType(Long id) {
        if (!fuelTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Fuel type not found with id: " + id);
        }
        fuelTypeRepository.deleteById(id);
    }
}