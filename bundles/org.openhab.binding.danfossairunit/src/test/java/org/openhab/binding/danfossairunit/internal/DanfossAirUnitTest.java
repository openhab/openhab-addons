/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.binding.danfossairunit.internal.Commands.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.test.java.JavaTest;

/**
 * This class provides test cases for {@link DanfossAirUnit}
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class DanfossAirUnitTest extends JavaTest {

    private CommunicationController communicationController;

    @BeforeEach
    private void setUp() {
        this.communicationController = mock(CommunicationController.class);
    }

    @Test
    public void getUnitNameIsReturned() throws IOException {
        byte[] response = new byte[] { (byte) 0x05, (byte) 'w', (byte) '2', (byte) '/', (byte) 'a', (byte) '2' };
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, UNIT_NAME)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals("w2/a2", airUnit.getUnitName());
    }

    @Test
    public void getHumidityWhenNearestNeighborIsBelowRoundsDown() throws IOException {
        byte[] response = new byte[] { (byte) 0x64 };
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, HUMIDITY)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(new QuantityType<>("39.2 %"), airUnit.getHumidity());
    }

    @Test
    public void getHumidityWhenNearestNeighborIsAboveRoundsUp() throws IOException {
        byte[] response = new byte[] { (byte) 0x67 };
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, HUMIDITY)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(new QuantityType<>("40.4 %"), airUnit.getHumidity());
    }

    @Test
    public void getSupplyTemperatureWhenNearestNeighborIsBelowRoundsDown()
            throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { (byte) 0x09, (byte) 0xf0 }; // 0x09f0 = 2544 => 25.44
        when(this.communicationController.sendRobustRequest(REGISTER_4_READ, SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(new QuantityType<>("25.4 °C"), airUnit.getSupplyTemperature());
    }

    @Test
    public void getSupplyTemperatureWhenBothNeighborsAreEquidistantRoundsUp()
            throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { (byte) 0x09, (byte) 0xf1 }; // 0x09f1 = 2545 => 25.45
        when(this.communicationController.sendRobustRequest(REGISTER_4_READ, SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(new QuantityType<>("25.5 °C"), airUnit.getSupplyTemperature());
    }

    @Test
    public void getSupplyTemperatureWhenBelowValidRangeThrows() throws IOException {
        byte[] response = new byte[] { (byte) 0x94, (byte) 0xf8 }; // 0x94f8 = -27400 => -274
        when(this.communicationController.sendRobustRequest(REGISTER_4_READ, SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getSupplyTemperature());
    }

    @Test
    public void getSupplyTemperatureWhenAboveValidRangeThrows() throws IOException {
        byte[] response = new byte[] { (byte) 0x27, (byte) 0x11 }; // 0x2711 = 10001 => 100,01
        when(this.communicationController.sendRobustRequest(REGISTER_4_READ, SUPPLY_TEMPERATURE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getSupplyTemperature());
    }

    @Test
    public void getCurrentTimeWhenWellFormattedIsParsed() throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { (byte) 0x03, (byte) 0x02, (byte) 0x0f, (byte) 0x1d, (byte) 0x08, (byte) 0x15 }; // 29.08.21
                                                                                                                       // 15:02:03
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, CURRENT_TIME)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(new DateTimeType(ZonedDateTime.of(2021, 8, 29, 15, 2, 3, 0, ZoneId.systemDefault())),
                airUnit.getCurrentTime());
    }

    @Test
    public void getCurrentTimeWhenInvalidDateThrows() throws IOException {
        byte[] response = new byte[] { (byte) 0x03, (byte) 0x02, (byte) 0x0f, (byte) 0x20, (byte) 0x08, (byte) 0x15 }; // 32.08.21
                                                                                                                       // 15:02:03
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, CURRENT_TIME)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getCurrentTime());
    }

    @Test
    public void getBoostWhenZeroIsOff() throws IOException {
        byte[] response = new byte[] { (byte) 0x00 };
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, BOOST)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(OnOffType.OFF, airUnit.getBoost());
    }

    @Test
    public void getBoostWhenNonZeroIsOn() throws IOException {
        byte[] response = new byte[] { (byte) 0x66 };
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, BOOST)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(OnOffType.ON, airUnit.getBoost());
    }

    @Test
    public void getManualFanStepWhenWithinValidRangeIsConvertedIntoPercent()
            throws IOException, UnexpectedResponseValueException {
        byte[] response = new byte[] { (byte) 0x05 };
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, MANUAL_FAN_SPEED_STEP))
                .thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(new PercentType(50), airUnit.getManualFanStep());
    }

    @Test
    public void getManualFanStepWhenOutOfRangeThrows() throws IOException {
        byte[] response = new byte[] { (byte) 0x0b };
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, MANUAL_FAN_SPEED_STEP))
                .thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertThrows(UnexpectedResponseValueException.class, () -> airUnit.getManualFanStep());
    }

    @Test
    public void getFilterLifeWhenNearestNeighborIsBelowRoundsDown() throws IOException {
        byte[] response = new byte[] { (byte) 0xf0 };
        when(this.communicationController.sendRobustRequest(REGISTER_1_READ, FILTER_LIFE)).thenReturn(response);
        var airUnit = new DanfossAirUnit(communicationController);
        assertEquals(new DecimalType("94.1"), airUnit.getFilterLife());
    }
}
