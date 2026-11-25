package com.parking.parkingfee.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.parking.parking.entity.ParkingSpotType;
import com.parking.parkingrate.service.ParkingRateService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StandardParkingFeeStrategyTest {

    @Mock ParkingRateService parkingRateService;
    @InjectMocks StandardParkingFeeStrategy strategy;

    @BeforeEach
    void setUp() {
        when(parkingRateService.getHourlyRate(ParkingSpotType.STANDARD)).thenReturn(new BigDecimal("10.00"));
    }

    @ParameterizedTest(name = "{0} minutes -> {1}")
    @CsvSource({
            "0,   10.00",   // minimum charge is one hour
            "1,   10.00",
            "60,  10.00",
            "61,  20.00",   // partial hours round up
            "150, 30.00"
    })
    void chargesPerStartedHourWithOneHourMinimum(long minutes, String expected) {
        LocalDateTime entry = LocalDateTime.of(2026, 1, 1, 10, 0);

        BigDecimal fee = strategy.calculate(ParkingSpotType.STANDARD, entry, entry.plusMinutes(minutes));

        assertThat(fee).isEqualByComparingTo(expected);
    }
}
