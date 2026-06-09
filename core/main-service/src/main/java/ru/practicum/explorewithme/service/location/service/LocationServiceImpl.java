package ru.practicum.explorewithme.service.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.location.dal.LocationRepository;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.mapper.LocationMapper;
import ru.practicum.explorewithme.service.location.model.Location;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Transactional
    @Override
    public LocationDto createLocation(NewLocationRequest request) {
        if (locationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Локация с именем " + request.getName() + "уже существует");
        }
        Location location = LocationMapper.toEntity(request);
        location = locationRepository.save(location);
        return LocationMapper.toDto(location);
    }

    @Transactional
    @Override
    public LocationDto updateLocation(Long locId, UpdateLocationRequest request) {
        Location location = locationRepository.findById(locId).orElseThrow(() -> new NotFoundException("Локация " + locId + " не найдена"));
        if (request.getName() != null && locationRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), locId)) {
            throw new ConflictException("Название для локации " + request.getName() + " уже существует");
        }
        LocationMapper.updateEntity(request, location);
        location = locationRepository.save(location);
        return LocationMapper.toDto(location);
    }

    @Transactional
    @Override
    public void deleteLocation(Long locId) {
        Location location = locationRepository.findById(locId).orElseThrow(() -> new NotFoundException("Локация " + locId + " не найдена"));
        locationRepository.delete(location);
    }

    @Override
    public LocationDto getLocationById(Long locId) {
        Location location = locationRepository.findById(locId).orElseThrow(() -> new NotFoundException("Локация " + locId + " не найдена"));
        return LocationMapper.toDto(location);
    }

    @Override
    public List<LocationDto> getAllLocations(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        Page<Location> page = locationRepository.findAll(pageable);
        return page.getContent().stream().map(LocationMapper::toDto).toList();
    }
}
