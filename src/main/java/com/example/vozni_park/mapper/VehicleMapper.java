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
        if (vehicle == null) return null;

        VehicleResponseDTO dto = new VehicleResponseDTO();

        // Core identity
        dto.setIdVehicle(vehicle.getIdVehicle());
        dto.setSapNumber(vehicle.getSapNumber());
        dto.setChassisNumber(vehicle.getChassisNumber());
        dto.setEngineNumber(vehicle.getEngineNumber());
        dto.setTagSerialNumber(vehicle.getTagSerialNumber());
        dto.setYearOfManufacture(vehicle.getYearOfManufacture());
        dto.setEngineDisplacement(vehicle.getEngineDisplacement());
        dto.setPower(vehicle.getPower());

        // Status
        dto.setVehicleStatus(vehicle.getVehicleStatus());
        dto.setStatusCode(vehicle.getStatusCode());

        // Physical attributes (006 expansion)
        dto.setMass(vehicle.getMass());
        dto.setColor(vehicle.getColor());
        dto.setLoadCapacity(vehicle.getLoadCapacity());
        dto.setTotalMass(vehicle.getTotalMass());
        dto.setNumberOfSeats(vehicle.getNumberOfSeats());
        dto.setFirstRegistrationDate(vehicle.getFirstRegistrationDate());
        dto.setOperationalLeasing(vehicle.getOperationalLeasing());
        dto.setDecommissioned(vehicle.getDecommissioned());
        dto.setDecommissionDate(vehicle.getDecommissionDate());
        dto.setNote(vehicle.getNote());

        // Legacy fields
        dto.setTireMarking(vehicle.getTireMarking());
        dto.setFireExtinguisherSerialNumber(vehicle.getFireExtinguisherSerialNumber());

        // Registration
        if (vehicle.getRegistration() != null) {
            Registration reg = vehicle.getRegistration();
            VehicleResponseDTO.RegistrationSummaryDTO regDTO = new VehicleResponseDTO.RegistrationSummaryDTO();
            regDTO.setIdRegistration(reg.getIdRegistration());
            regDTO.setRegistrationNumber(reg.getRegistrationNumber());
            regDTO.setDateFrom(reg.getDateFrom());
            regDTO.setDateTo(reg.getDateTo());
            regDTO.setStatus(reg.getStatus());
            regDTO.setStatusCode(reg.getStatusCode());
            dto.setRegistration(regDTO);
        }

        // Fuel type
        if (vehicle.getFuelType() != null) {
            dto.setFuelType(new FuelTypeSummaryDTO(
                    vehicle.getFuelType().getIdFuelType(),
                    vehicle.getFuelType().getName()
            ));
        }

        // First aid kit
        if (vehicle.getFirstAidKit() != null) {
            FirstAidKit kit = vehicle.getFirstAidKit();
            VehicleResponseDTO.FirstAidKitSummaryDTO kitDTO = new VehicleResponseDTO.FirstAidKitSummaryDTO();
            kitDTO.setIdFirstAidKit(kit.getIdFirstAidKit());
            kitDTO.setExpiryDate(kit.getExpiryDate());
            kitDTO.setStatus(kit.getStatus());
            kitDTO.setStatusCode(kit.getStatusCode());
            dto.setFirstAidKit(kitDTO);
        }

        // Casco insurance (new)
        if (vehicle.getCascoInsurance() != null) {
            CascoInsurance casco = vehicle.getCascoInsurance();
            VehicleResponseDTO.CascoInsuranceSummaryDTO cascoDTO = new VehicleResponseDTO.CascoInsuranceSummaryDTO();
            cascoDTO.setIdCascoInsurance(casco.getIdCascoInsurance());
            cascoDTO.setContractNumber(casco.getContractNumber());
            cascoDTO.setPolicyNumber(casco.getPolicyNumber());
            cascoDTO.setDateFrom(casco.getDateFrom());
            cascoDTO.setDateTo(casco.getDateTo());
            cascoDTO.setStatus(casco.getStatus());
            cascoDTO.setStatusCode(casco.getStatusCode());
            dto.setCascoInsurance(cascoDTO);
        }

        // Tachograph device (new)
        if (vehicle.getTachographDevice() != null) {
            TachographDevice tach = vehicle.getTachographDevice();
            VehicleResponseDTO.TachographDeviceSummaryDTO tachDTO = new VehicleResponseDTO.TachographDeviceSummaryDTO();
            tachDTO.setIdTachographDevice(tach.getIdTachographDevice());
            tachDTO.setDeviceType(tach.getDeviceType());
            tachDTO.setSerialNumber(tach.getSerialNumber());
            tachDTO.setDateFrom(tach.getDateFrom());
            tachDTO.setDateTo(tach.getDateTo());
            tachDTO.setStatus(tach.getStatus());
            tachDTO.setStatusCode(tach.getStatusCode());
            dto.setTachographDevice(tachDTO);
        }

        // Fire extinguisher (new entity, replaces legacy serial number)
        if (vehicle.getFireExtinguisher() != null) {
            FireExtinguisher fe = vehicle.getFireExtinguisher();
            VehicleResponseDTO.FireExtinguisherSummaryDTO feDTO = new VehicleResponseDTO.FireExtinguisherSummaryDTO();
            feDTO.setIdFireExtinguisher(fe.getIdFireExtinguisher());
            feDTO.setSerialNumber(fe.getSerialNumber());
            feDTO.setDateFrom(fe.getDateFrom());
            feDTO.setDateTo(fe.getDateTo());
            feDTO.setStatus(fe.getStatus());
            feDTO.setStatusCode(fe.getStatusCode());
            dto.setFireExtinguisher(feDTO);
        }

        // Vehicle model + brand
        if (vehicle.getVehicleModel() != null) {
            VehicleModel model = vehicle.getVehicleModel();
            VehicleModelSummaryDTO modelDTO = new VehicleModelSummaryDTO();
            modelDTO.setIdVehicleModel(model.getIdVehicleModel());
            modelDTO.setName(model.getName());
            if (model.getBrand() != null) {
                modelDTO.setBrand(new BrandSummaryDTO(
                        model.getBrand().getIdBrand(),
                        model.getBrand().getName()
                ));
            }
            dto.setVehicleModel(modelDTO);
        }

        // Location
        if (vehicle.getVehicleLocation() != null &&
                vehicle.getVehicleLocation().getLocationUnit() != null) {
            LocationUnit loc = vehicle.getVehicleLocation().getLocationUnit();
            dto.setLocation(new LocationUnitSummaryDTO(
                    loc.getIdLocationUnit(),
                    loc.getLocationName(),
                    loc.getLocationAddress()
            ));
        }

        return dto;
    }

    public Vehicle toEntity(VehicleRequestDTO dto) {
        if (dto == null) return null;

        Vehicle vehicle = new Vehicle();
        mapRequestFields(vehicle, dto);
        return vehicle;
    }

    public void updateEntity(Vehicle vehicle, VehicleRequestDTO dto) {
        if (vehicle == null || dto == null) return;
        mapRequestFields(vehicle, dto);
    }

    public List<VehicleResponseDTO> toResponseDTOList(List<Vehicle> vehicles) {
        if (vehicles == null) return new ArrayList<>();
        return vehicles.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public VehicleSummaryDTO toSummaryDTO(Vehicle vehicle) {
        if (vehicle == null) return null;

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
                modelDTO.setBrand(new BrandSummaryDTO(
                        model.getBrand().getIdBrand(),
                        model.getBrand().getName()
                ));
            }
            dto.setVehicleModel(modelDTO);
        }

        return dto;
    }

    // ── Private helper ────────────────────────────────────────────────

    /**
     * Maps all writable request fields onto a Vehicle entity.
     * Status and statusCode are intentionally NOT mapped here —
     * they are managed exclusively by StatusCalculationService.
     */
    private void mapRequestFields(Vehicle vehicle, VehicleRequestDTO dto) {
        vehicle.setSapNumber(dto.getSapNumber());
        vehicle.setChassisNumber(dto.getChassisNumber());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setTagSerialNumber(dto.getTagSerialNumber());
        vehicle.setYearOfManufacture(dto.getYearOfManufacture());
        vehicle.setEngineDisplacement(dto.getEngineDisplacement());
        vehicle.setPower(dto.getPower());

        // Physical attributes
        vehicle.setMass(dto.getMass());
        vehicle.setColor(dto.getColor());
        vehicle.setLoadCapacity(dto.getLoadCapacity());
        vehicle.setTotalMass(dto.getTotalMass());
        vehicle.setNumberOfSeats(dto.getNumberOfSeats());
        vehicle.setFirstRegistrationDate(dto.getFirstRegistrationDate());
        vehicle.setOperationalLeasing(dto.getOperationalLeasing());
        vehicle.setNote(dto.getNote());

        // Legacy
        vehicle.setTireMarking(dto.getTireMarking());
        vehicle.setFireExtinguisherSerialNumber(dto.getFireExtinguisherSerialNumber());

        // FK IDs (entities resolved by service layer)
        vehicle.setRegistrationId(dto.getRegistrationId());
        vehicle.setFuelTypeId(dto.getFuelTypeId());
        vehicle.setFirstAidKitId(dto.getFirstAidKitId());
        vehicle.setVehicleModelId(dto.getVehicleModelId());
        vehicle.setVehicleTypeId(dto.getVehicleTypeId());
        vehicle.setVehicleSubtypeId(dto.getVehicleSubtypeId());
        vehicle.setCascoInsuranceId(dto.getCascoInsuranceId());
        vehicle.setTachographDeviceId(dto.getTachographDeviceId());
        vehicle.setFireExtinguisherId(dto.getFireExtinguisherId());
        vehicle.setNisCardId(dto.getNisCardId());
    }
}
