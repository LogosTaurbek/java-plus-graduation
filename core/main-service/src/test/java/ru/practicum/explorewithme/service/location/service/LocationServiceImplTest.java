package ru.practicum.explorewithme.service.location.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.location.dal.LocationRepository;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.model.Location;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    private NewLocationRequest request;
    private Location location;
    private Location location2;
    private UpdateLocationRequest updateRequest;
    private Location updatedLocation;
    Long locId;

    @BeforeEach
    void setUp() {
        locId = 1L;

        request = NewLocationRequest.builder()
                .name("Moscow")
                .lat(55.7558f)
                .lon(37.6173f)
                .radius(300.00f)
                .build();

        location = Location.builder()
                .name("Moscow")
                .id(1L)
                .lat(55.7558f)
                .lon(37.6173f)
                .radius(300.00f)
                .build();

        location2 = Location.builder()
                .name("Saint Petersburg")
                .id(2L)
                .lat(59.9375f)
                .lon(30 / 3350f)
                .radius(5.00f)
                .build();

        updateRequest = UpdateLocationRequest.builder()
                .name("Moscva")
                .lat(55.7560f)
                .lon(37.6175f)
                .radius(350.00f)
                .build();

        updatedLocation = Location.builder()
                .id(1L)
                .name("Moscva")
                .lat(55.7560f)
                .lon(37.6175f)
                .radius(350.00f)
                .build();
    }

    //    создать локацию
    @Test
    void shouldCreateLocation() {
        when(locationRepository.save(any(Location.class)))
                .thenReturn(location);

        LocationDto result = locationService.createLocation(request);

        assertThat(result.getId()).isEqualTo(locId);
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getLon()).isEqualTo(request.getLon());
        assertThat(result.getLat()).isEqualTo(request.getLat());
        assertThat(result.getRadius()).isEqualTo(request.getRadius());
    }

    @Test
    void shouldUpdateLocation() {
        when(locationRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(location));

        when(locationRepository.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong()))
                .thenReturn(false);

        when(locationRepository.save(any(Location.class)))
                .thenReturn(updatedLocation);

        LocationDto result = locationService.updateLocation(locId, updateRequest);

        assertThat(result.getName()).isEqualTo(updateRequest.getName());
        assertThat(result.getLon()).isEqualTo(updateRequest.getLon());
        assertThat(result.getLat()).isEqualTo(updateRequest.getLat());
        assertThat(result.getRadius()).isEqualTo(updateRequest.getRadius());
    }

    // обновление локации  - локация не найдена
    @Test
    void shouldThrowExceptionWhenLocationNotFound() {
        when(locationRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.updateLocation(locId, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Локация " + locId + " не найдена");

        verify(locationRepository, never()).save(any());
    }

    // обновление локации - название уже существует
    @Test
    void shouldThrowExceptionWhenLocationNameExists() {
        when(locationRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(location));

        when(locationRepository.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> locationService.updateLocation(locId, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Название для локации " + updateRequest.getName() + " уже существует");

        verify(locationRepository, never()).save(any());
    }

    // удаление локации
    @Test
    void shouldDeleteUser() {
        when(locationRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(location));

        locationService.deleteLocation(locId);

        verify(locationRepository).delete(location);
    }

    // получение локации
    @Test
    void shouldReturnLocation() {
        when(locationRepository.findById(locId))
                .thenReturn(Optional.ofNullable(location));

        LocationDto result = locationService.getLocationById(locId);

        assertThat(result.getId()).isEqualTo(location.getId());
        assertThat(result.getName()).isEqualTo(location.getName());
        assertThat(result.getLon()).isEqualTo(location.getLon());
        assertThat(result.getLat()).isEqualTo(location.getLat());
        assertThat(result.getRadius()).isEqualTo(location.getRadius());

        verify(locationRepository, times(1)).findById(locId);
    }

    // получение списка локаций
    @Test
    void shouldReturnLocationList() {
        int from = 0;
        int size = 10;
        Page<Location> page = new PageImpl<>(List.of(location, location2));
        when(locationRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        List<LocationDto> result = locationService.getAllLocations(from, size);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.getFirst().getId()).isEqualTo(location.getId());
        assertThat(result.get(1).getId()).isEqualTo(location2.getId());
    }

}
