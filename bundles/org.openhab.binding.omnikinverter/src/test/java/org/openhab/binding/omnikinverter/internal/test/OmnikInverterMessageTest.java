/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.omnikinverter.internal.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.omnikinverter.internal.OmnikInverterMessage;

/**
 * @author Hans van den Bogert - Initial contribution
 */
public class OmnikInverterMessageTest {

    private OmnikInverterMessage message;

    @BeforeEach
    public void setUp() throws IOException {
        File file = new File("src/test/resources/omnik.output");
        message = new OmnikInverterMessage(Files.readAllBytes(file.toPath()));
    }

    @Test
    public void testGetPower() {
        assertEquals(137.0, message.getPower(), 0.01);
    }

    @Test
    public void testGetPowerAC1() {
        assertEquals(137.0, message.getPowerAC1(), 0.01);
    }

    @Test
    public void testGetPowerAC2() {
        assertEquals(-1.0, message.getPowerAC2(), 0.01);
    }

    @Test
    public void testGetPowerAC3() {
        assertEquals(-1.0, message.getPowerAC3(), 0.01);
    }

    @Test
    public void testGetVoltageAC1() {
        assertEquals(236.0, message.getVoltageAC1(), 0.01);
    }

    @Test
    public void testGetVoltageAC2() {
        assertEquals(-0.1, message.getVoltageAC2(), 0.01);
    }

    @Test
    public void testGetVoltageAC3() {
        assertEquals(-0.1, message.getVoltageAC3(), 0.01);
    }

    @Test
    public void testGetCurrentAC1() {
        assertEquals(0.5, message.getCurrentAC1(), 0.01);
    }

    @Test
    public void testGetCurrentAC2() {
        assertEquals(-0.1, message.getCurrentAC2(), 0.01);
    }

    @Test
    public void testGetCurrentAC3() {
        assertEquals(-0.1, message.getCurrentAC3(), 0.01);
    }

    @Test
    public void testGetFrequencyAC1() {
        assertEquals(50.06, message.getFrequencyAC1(), 0.01);
    }

    @Test
    public void testGetFrequencyAC2() {
        assertEquals(-0.01, message.getFrequencyAC2(), 0.01);
    }

    @Test
    public void testGetFrequencyAC3() {
        assertEquals(-0.01, message.getFrequencyAC3(), 0.01);
    }

    @Test
    public void testGetCurrentPV1() {
        assertEquals(0.5, message.getCurrentPV1(), 0.01);
    }

    @Test
    public void testGetCurrentPV2() {
        assertEquals(0.6, message.getCurrentPV2(), 0.01);
    }

    @Test
    public void testGetCurrentPV3() {
        assertEquals(-0.1, message.getCurrentPV3(), 0.01);
    }

    @Test
    public void testGetVoltagePV1() {
        assertEquals(160.0, message.getVoltagePV1(), 0.01);
    }

    @Test
    public void testGetVoltagePV2() {
        assertEquals(131.9, message.getVoltagePV2(), 0.01);
    }

    @Test
    public void testGetVoltagePV3() {
        assertEquals(-0.1, message.getVoltagePV3(), 0.01);
    }

    @Test
    public void testGetTotalEnergy() {
        assertEquals(12412.7, message.getTotalEnergy(), 0.01);
    }

    @Test
    public void testGetEnergyToday() {
        assertEquals(11.13, message.getEnergyToday(), 0.01);
    }

    @Test
    public void testGetTemperature() {
        assertEquals(31.7, message.getTemperature(), 0.01);
    }

    @Test
    public void testGetHoursTotal() {
        assertEquals(17693, message.getHoursTotal(), 0.01);
    }
}
