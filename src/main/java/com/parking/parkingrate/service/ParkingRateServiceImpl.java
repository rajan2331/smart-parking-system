package com.parking.parkingrate.service;


import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.common.exception.ParkingRateNotFoundException;
import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingrate.dto.ParkingRateResponse;
import com.parking.parkingrate.entity.ParkingRate;
import com.parking.parkingrate.mapper.ParkingRateMapper;
import com.parking.parkingrate.repository.ParkingRateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParkingRateServiceImpl implements ParkingRateService {

    private final ParkingRateRepository parkingRateRepository;
    private final ParkingRateMapper parkingRateMapper;

    @Override
    public BigDecimal getHourlyRate(ParkingSpotType spotType) {

        ParkingRate rate = parkingRateRepository
                .findBySpotType(spotType)
                .orElseThrow(() ->
                        new ParkingRateNotFoundException(
                                "Parking rate not configured for " + spotType));

        return rate.getHourlyRate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingRateResponse> getAllRates() {
        return parkingRateRepository.findAll().stream()
                .sorted(Comparator.comparing(ParkingRate::getSpotType))
                .map(parkingRateMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ParkingRateResponse updateRate(ParkingSpotType spotType, BigDecimal hourlyRate) {
        ParkingRate rate = parkingRateRepository.findBySpotType(spotType)
                .orElseGet(() -> {
                    ParkingRate created = new ParkingRate();
                    created.setSpotType(spotType);
                    return created;
                });
        rate.setHourlyRate(hourlyRate);
        return parkingRateMapper.toResponse(parkingRateRepository.save(rate));
    }
}
