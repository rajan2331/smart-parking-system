package com.parking.parkingrate.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParkingRateRequest {

    @NotNull
    @DecimalMin(value = "0.00", message = "must not be negative")
    @Digits(integer = 8, fraction = 2) // matches NUMERIC(10,2)
    private BigDecimal hourlyRate;
}
