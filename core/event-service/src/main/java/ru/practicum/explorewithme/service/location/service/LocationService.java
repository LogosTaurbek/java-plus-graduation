package ru.practicum.explorewithme.service.location.service;

import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;

import java.util.List;

public interface LocationService {
    LocationDto createLocation(NewLocationRequest request);

    LocationDto updateLocation(Long locId, UpdateLocationRequest request);

    void deleteLocation(Long locId);

    LocationDto getLocationById(Long locId);

    List<LocationDto> getAllLocations(int from, int size);
}
