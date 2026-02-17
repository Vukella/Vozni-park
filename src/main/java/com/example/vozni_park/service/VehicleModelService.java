package com.example.vozni_park.service;

import com.example.vozni_park.dto.response.VehicleModelResponseDTO;
import com.example.vozni_park.entity.VehicleModel;
import com.example.vozni_park.mapper.VehicleModelMapper;
import com.example.vozni_park.repository.BrandRepository;
import com.example.vozni_park.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;
    private final BrandRepository brandRepository;
    private final VehicleModelMapper vehicleModelMapper;

    @Transactional(readOnly = true)
    public List<VehicleModelResponseDTO> getAllVehicleModels() {
        return vehicleModelMapper.toResponseDTOList(vehicleModelRepository.findAll());
    }

    @Transactional(readOnly = true)
    public VehicleModelResponseDTO getVehicleModelById(Long id) {
        VehicleModel model = vehicleModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle model not found with id: " + id));
        return vehicleModelMapper.toResponseDTO(model);
    }

    @Transactional(readOnly = true)
    public List<VehicleModelResponseDTO> getVehicleModelsByBrandId(Long brandId) {
        return vehicleModelMapper.toResponseDTOList(vehicleModelRepository.findByBrandId(brandId));
    }

    public VehicleModelResponseDTO createVehicleModel(VehicleModel vehicleModel) {
        if (!brandRepository.existsById(vehicleModel.getBrandId())) {
            throw new IllegalArgumentException("Brand not found with id: " + vehicleModel.getBrandId());
        }
        VehicleModel saved = vehicleModelRepository.save(vehicleModel);
        // Re-fetch so the brand relationship is populated for the mapper
        return vehicleModelMapper.toResponseDTO(
                vehicleModelRepository.findById(saved.getIdVehicleModel()).orElse(saved)
        );
    }

    public VehicleModelResponseDTO updateVehicleModel(Long id, VehicleModel vehicleModelDetails) {
        VehicleModel model = vehicleModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle model not found with id: " + id));
        if (!brandRepository.existsById(vehicleModelDetails.getBrandId())) {
            throw new IllegalArgumentException("Brand not found with id: " + vehicleModelDetails.getBrandId());
        }
        model.setName(vehicleModelDetails.getName());
        model.setBrandId(vehicleModelDetails.getBrandId());
        vehicleModelRepository.save(model);
        // Re-fetch so the brand relationship is populated for the mapper
        return vehicleModelMapper.toResponseDTO(
                vehicleModelRepository.findById(id).orElse(model)
        );
    }

    public void deleteVehicleModel(Long id) {
        if (!vehicleModelRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle model not found with id: " + id);
        }
        vehicleModelRepository.deleteById(id);
    }
}