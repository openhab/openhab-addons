/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.danfossairunit.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.danfossairunit.internal.protocol.Parameter;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * This class provides test cases for {@link DanfossAirUnit}
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class DanfossAirUnitTest {

    private @NonNullByDefault({}) @Mock CommunicationController communicationController;
    private @NonNullByDefault({}) @Mock TimeZoneProvider timeZoneProvider;

    @Test
    void getUnitNameIsReturned() throws IOException {
        byte[] response = new byte[] { 0x05, (byte) 'w', (byte) '2', (byte) '/', (byte) 'a', (byte) '2' };
        when(communicationController.sendRobustRequest(Parameter.UNIT_NAME)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals("w2/a2", airUnit.getUnitName());
    }

    @Test
    void getHumidityWhenNearestNeighborIsBelowRoundsDown() throws IOException {
        byte[] response = new byte[] { (byte) 0x64 };
        when(communicationController.sendRobustRequest(Parameter.HUMIDITY)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(new QuantityType<>("39.2 %"), airUnit.getHumidity());
    }

    @Test
    void getHumidityWhenNearestNeighborIsAboveRoundsUp() throws IOException {
        byte[] response = new byte[] { (byte) 0x67 };
        when(communicationController.sendRobustRequest(Parameter.HUMIDITY)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(new QuantityType<>("40.4 %"), airUnit.getHumidity());
    }

    @Test
    void getSupplyTemperatureWhenNearestNeighborIsBelowRoundsDown()
            throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { 0x09, (byte) 0xf0 }; // 0x09f0 = 2544 => 25.44
        when(communicationController.sendRobustRequest(Parameter.SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(new QuantityType<>("25.4 °C"), airUnit.getSupplyTemperature());
    }

    @Test
    void getSupplyTemperatureWhenResponseTooShortThrows() throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { 0x09 };
        when(communicationController.sendRobustRequest(Parameter.SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getSupplyTemperature());
    }

    @Test
    void getSupplyTemperatureWhenBothNeighborsAreEquidistantRoundsUp()
            throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { 0x09, (byte) 0xf1 }; // 0x09f1 = 2545 => 25.45
        when(communicationController.sendRobustRequest(Parameter.SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(new QuantityType<>("25.5 °C"), airUnit.getSupplyTemperature());
    }

    @Test
    void getSupplyTemperatureWhenBelowValidRangeThrows() throws IOException {
        byte[] response = new byte[] { (byte) 0x94, (byte) 0xf8 }; // 0x94f8 = -27400 => -274
        when(communicationController.sendRobustRequest(Parameter.SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getSupplyTemperature());
    }

    @Test
    void getSupplyTemperatureWhenAboveValidRangeThrows() throws IOException {
        byte[] response = new byte[] { 0x27, 0x11 }; // 0x2711 = 10001 => 100,01
        when(communicationController.sendRobustRequest(Parameter.SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getSupplyTemperature());
    }

    @Test
    void getCurrentTimeWhenWellFormattedIsParsed() throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { 0x03, 0x02, 0x0f, 0x1d, 0x08, 0x15 }; // 29.08.21
                                                                             // 15:02:03
        when(communicationController.sendRobustRequest(Parameter.CURRENT_TIME)).thenReturn(response);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.of("Europe/Copenhagen"));
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(new DateTimeType(ZonedDateTime.of(2021, 8, 29, 15, 2, 3, 0, ZoneId.of("Europe/Copenhagen"))),
                airUnit.getCurrentTime());
    }

    @Test
    void getCurrentTimeWhenInvalidDateThrows() throws IOException {
        byte[] response = new byte[] { 0x03, 0x02, 0x0f, 0x20, 0x08, 0x15 }; // 32.08.21
                                                                             // 15:02:03
        when(communicationController.sendRobustRequest(Parameter.CURRENT_TIME)).thenReturn(response);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.of("Europe/Copenhagen"));
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getCurrentTime());
    }

    @Test
    void getCurrentTimeWhenResponseTooShortThrows() throws IOException {
        byte[] response = new byte[] { 0x03, 0x02, 0x0f, 0x20, 0x08 };
        when(communicationController.sendRobustRequest(Parameter.CURRENT_TIME)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getCurrentTime());
    }

    @Test
    void getBoostWhenZeroIsOff() throws IOException {
        byte[] response = new byte[] { 0x00 };
        when(communicationController.sendRobustRequest(Parameter.BOOST)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(OnOffType.OFF, airUnit.getBoost());
    }

    @Test
    void getBoostWhenNonZeroIsOn() throws IOException {
        byte[] response = new byte[] { (byte) 0x66 };
        when(communicationController.sendRobustRequest(Parameter.BOOST)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(OnOffType.ON, airUnit.getBoost());
    }

    @Test
    void getManualFanStepWhenWithinValidRangeIsConvertedIntoPercent()
            throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { 0x05 };
        when(communicationController.sendRobustRequest(Parameter.MANUAL_FAN_SPEED_STEP)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(new PercentType(50), airUnit.getManualFanStep());
    }

    @Test
    void getSupplyFanSpeedIsReturnedAsRPM() throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { 0x04, (byte) 0xda };
        when(communicationController.sendRobustRequest(Parameter.SUPPLY_FAN_SPEED)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(new QuantityType<>(1242, Units.RPM), airUnit.getSupplyFanSpeed());
    }

    @Test
    void getManualFanStepWhenOutOfRangeThrows() throws IOException {
        byte[] response = new byte[] { 0x0b };
        when(communicationController.sendRobustRequest(Parameter.MANUAL_FAN_SPEED_STEP)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getManualFanStep());
    }

    @Test
    void getFilterLifeWhenNearestNeighborIsBelowRoundsDown() throws IOException {
        byte[] response = new byte[] { (byte) 0xf0 };
        when(communicationController.sendRobustRequest(Parameter.FILTER_LIFE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals(new DecimalType("94.1"), airUnit.getFilterLife());
    }

    @Test
    void getCCMSerialNumber() throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { 0x00, (byte) 0xbc, 0x61, 0x4e };
        when(communicationController.sendRobustRequest(Parameter.CCM_SERIAL_NUMBER)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertEquals("12345678", airUnit.getCCMSerialNumber());
    }

    @Test
    void getCCMSerialNumberWhenResponseTooShortThrows() throws IOException {
        byte[] response = new byte[] { 0x00, (byte) 0xbc, 0x61 };
        when(communicationController.sendRobustRequest(Parameter.CCM_SERIAL_NUMBER)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController, timeZoneProvider);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getCCMSerialNumber());
    }
}
