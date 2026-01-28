package com.example.vozni_park.mapper;

import com.example.vozni_park.dto.request.VehicleRequestDTO;
import com.example.vozni_park.dto.response.VehicleResponseDTO;
import com.example.vozni_park.dto.summary.*;
import com.example.vozni_park.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleMapper {

    public VehicleResponseDTO toResponseDTO(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setIdVehicle(vehicle.getIdVehicle());
        dto.setSapNumber(vehicle.getSapNumber());
        dto.setChassisNumber(vehicle.getChassisNumber());
        dto.setEngineNumber(vehicle.getEngineNumber());
        dto.setTagSerialNumber(vehicle.getTagSerialNumber());
        dto.setYearOfManufacture(vehicle.getYearOfManufacture());
        dto.setEngineDisplacement(vehicle.getEngineDisplacement());
        dto.setPower(vehicle.getPower());
        dto.setTireMarking(vehicle.getTireMarking());
        dto.setFireExtinguisherSerialNumber(vehicle.getFireExtinguisherSerialNumber());
        dto.setVehicleStatus(vehicle.getVehicleStatus());
        dto.setStatusCode(vehicle.getStatusCode());

        // Map Registration (if present)
        if (vehicle.getRegistration() != null) {
            Registration reg = vehicle.getRegistration();
            VehicleResponseDTO.RegistrationSummaryDTO regDTO = new VehicleResponseDTO.RegistrationSummaryDTO();
            regDTO.setIdRegistration(reg.getIdRegistration());
            regDTO.setRegistrationNumber(reg.getRegistrationNumber());
            regDTO.setDateFrom(reg.getDateFrom());
            regDTO.setDateTo(reg.getDateTo());
            regDTO.setStatus(reg.getStatus());
            dto.setRegistration(regDTO);
        }

        // Map FuelType (if present)
        if (vehicle.getFuelType() != null) {
            FuelType fuelType = vehicle.getFuelType();
            dto.setFuelType(new FuelTypeSummaryDTO(
                    fuelType.getIdFuelType(),
                    fuelType.getName()
            ));
        }

        // Map FirstAidKit (if present)
        if (vehicle.getFirstAidKit() != null) {
            FirstAidKit kit = vehicle.getFirstAidKit();
            VehicleResponseDTO.FirstAidKitSummaryDTO kitDTO = new VehicleResponseDTO.FirstAidKitSummaryDTO();
            kitDTO.setIdFirstAidKit(kit.getIdFirstAidKit());
            kitDTO.setExpiryDate(kit.getExpiryDate());
            kitDTO.setStatus(kit.getStatus());
            dto.setFirstAidKit(kitDTO);
        }

        // Map VehicleModel (if present)
        if (vehicle.getVehicleModel() != null) {
            VehicleModel model = vehicle.getVehicleModel();
            VehicleModelSummaryDTO modelDTO = new VehicleModelSummaryDTO();
            modelDTO.setIdVehicleModel(model.getIdVehicleModel());
            modelDTO.setName(model.getName());

            // Map Brand within VehicleModel
            if (model.getBrand() != null) {
                Brand brand = model.getBrand();
                modelDTO.setBrand(new BrandSummaryDTO(
                        brand.getIdBrand(),
                        brand.getName()
                ));
            }
            dto.setVehicleModel(modelDTO);
        }

        // Map Location (if present)
        if (vehicle.getVehicleLocation() != null &&
                vehicle.getVehicleLocation().getLocationUnit() != null) {
            LocationUnit location = vehicle.getVehicleLocation().getLocationUnit();
            dto.setLocation(new LocationUnitSummaryDTO(
                    location.getIdLocationUnit(),
                    location.getLocationName(),
                    location.getLocationAddress()
            ));
        }

        return dto;
    }

    /**
     * Convert VehicleRequestDTO to Vehicle entity
     */
    public Vehicle toEntity(VehicleRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setSapNumber(dto.getSapNumber());
        vehicle.setChassisNumber(dto.getChassisNumber());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setTagSerialNumber(dto.getTagSerialNumber());
        vehicle.setYearOfManufacture(dto.getYearOfManufacture());
        vehicle.setEngineDisplacement(dto.getEngineDisplacement());
        vehicle.setPower(dto.getPower());
        vehicle.setTireMarking(dto.getTireMarking());
        vehicle.setFireExtinguisherSerialNumber(dto.getFireExtinguisherSerialNumber());
        vehicle.setVehicleStatus(dto.getVehicleStatus());
        vehicle.setStatusCode(dto.getStatusCode());

        // Set foreign key IDs (entities will be set by service layer)
        vehicle.setRegistrationId(dto.getRegistrationId());
        vehicle.setFuelTypeId(dto.getFuelTypeId());
        vehicle.setFirstAidKitId(dto.getFirstAidKitId());
        vehicle.setVehicleModelId(dto.getVehicleModelId());

        return vehicle;
    }

    /**
     * Update existing Vehicle entity from VehicleRequestDTO
     */
    public void updateEntity(Vehicle vehicle, VehicleRequestDTO dto) {
        if (vehicle == null || dto == null) {
            return;
        }

        vehicle.setSapNumber(dto.getSapNumber());
        vehicle.setChassisNumber(dto.getChassisNumber());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setTagSerialNumber(dto.getTagSerialNumber());
        vehicle.setYearOfManufacture(dto.getYearOfManufacture());
        vehicle.setEngineDisplacement(dto.getEngineDisplacement());
        vehicle.setPower(dto.getPower());
        vehicle.setTireMarking(dto.getTireMarking());
        vehicle.setFireExtinguisherSerialNumber(dto.getFireExtinguisherSerialNumber());
        vehicle.setVehicleStatus(dto.getVehicleStatus());
        vehicle.setStatusCode(dto.getStatusCode());
        vehicle.setRegistrationId(dto.getRegistrationId());
        vehicle.setFuelTypeId(dto.getFuelTypeId());
        vehicle.setFirstAidKitId(dto.getFirstAidKitId());
        vehicle.setVehicleModelId(dto.getVehicleModelId());
    }

    /**
     * Convert list of Vehicle entities to list of VehicleResponseDTOs
     */
    public List<VehicleResponseDTO> toResponseDTOList(List<Vehicle> vehicles) {
        if (vehicles == null) {
            return new ArrayList<>();
        }
        return vehicles.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Vehicle to VehicleSummaryDTO (lightweight)
     */
    public VehicleSummaryDTO toSummaryDTO(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        VehicleSummaryDTO dto = new VehicleSummaryDTO();
        dto.setIdVehicle(vehicle.getIdVehicle());
        dto.setSapNumber(vehicle.getSapNumber());
        dto.setChassisNumber(vehicle.getChassisNumber());
        dto.setVehicleStatus(vehicle.getVehicleStatus());

        if (vehicle.getVehicleModel() != null) {
            VehicleModel model = vehicle.getVehicleModel();
            VehicleModelSummaryDTO modelDTO = new VehicleModelSummaryDTO();
            modelDTO.setIdVehicleModel(model.getIdVehicleModel());
            modelDTO.setName(model.getName());

            if (model.getBrand() != null) {
                Brand brand = model.getBrand();
                modelDTO.setBrand(new BrandSummaryDTO(
                        brand.getIdBrand(),
                        brand.getName()
                ));
            }
            dto.setVehicleModel(modelDTO);
        }

        return dto;
    }
}

