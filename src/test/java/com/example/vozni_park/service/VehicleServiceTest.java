package com.example.vozni_park.service;

import com.example.vozni_park.dto.request.VehicleRequestDTO;
import com.example.vozni_park.dto.response.VehicleResponseDTO;
import com.example.vozni_park.entity.*;
import com.example.vozni_park.mapper.VehicleMapper;
import com.example.vozni_park.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private VehicleModelRepository vehicleModelRepository;
    @Mock private FuelTypeRepository fuelTypeRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private FirstAidKitRepository firstAidKitRepository;
    @Mock private LocationFilterService locationFilterService;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;
    private VehicleResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle();
        vehicle.setIdVehicle(1L);
        vehicle.setSapNumber(123456L);
        vehicle.setVehicleStatus("Active");

        responseDTO = new VehicleResponseDTO();
        responseDTO.setIdVehicle(1L);
        responseDTO.setSapNumber(123456L);
    }

    @Test
    void getAllVehicles_asSuperAdmin_returnsAllVehicles() {
        when(locationFilterService.isSuperAdmin()).thenReturn(true);
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponseDTOList(List.of(vehicle))).thenReturn(List.of(responseDTO));

        List<VehicleResponseDTO> result = vehicleService.getAllVehicles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSapNumber()).isEqualTo(123456L);
        verify(vehicleRepository).findAll();
        verify(vehicleRepository, never()).findByLocationIds(any());
    }

    @Test
    void getAllVehicles_asLocalAdmin_returnsFilteredVehicles() {
        when(locationFilterService.isSuperAdmin()).thenReturn(false);
        when(locationFilterService.getCurrentUserLocationIds()).thenReturn(List.of(3L, 4L));
        when(vehicleRepository.findByLocationIds(List.of(3L, 4L))).thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponseDTOList(List.of(vehicle))).thenReturn(List.of(responseDTO));

        List<VehicleResponseDTO> result = vehicleService.getAllVehicles();

        assertThat(result).hasSize(1);
        verify(vehicleRepository).findByLocationIds(List.of(3L, 4L));
        verify(vehicleRepository, never()).findAll();
    }

    @Test
    void getVehicleById_asSuperAdmin_returnsVehicle() {
        when(locationFilterService.isSuperAdmin()).thenReturn(true);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponseDTO(vehicle)).thenReturn(responseDTO);

        Optional<VehicleResponseDTO> result = vehicleService.getVehicleById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getIdVehicle()).isEqualTo(1L);
    }

    @Test
    void getVehicleById_nonExistingId_returnsEmpty() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<VehicleResponseDTO> result = vehicleService.getVehicleById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createVehicle_duplicateSapNumber_throwsException() {
        VehicleRequestDTO requestDTO = new VehicleRequestDTO();
        requestDTO.setSapNumber(123456L);

        when(vehicleRepository.existsBySapNumber(123456L)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SAP number");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void deleteVehicle_existingId_callsDeleteById() {
        when(locationFilterService.isSuperAdmin()).thenReturn(true);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void deleteVehicle_nonExistingId_throwsException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.deleteVehicle(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).deleteById(any());
    }
}