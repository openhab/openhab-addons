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
}
