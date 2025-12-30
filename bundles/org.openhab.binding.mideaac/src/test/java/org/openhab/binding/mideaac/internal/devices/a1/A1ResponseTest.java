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
package org.openhab.binding.mideaac.internal.devices.a1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link A1ResponseTest} tests the methods in the A1Response class
 * against an example A1response string.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class A1ResponseTest {
    @org.jupnp.registry.event.Before

    byte[] data = HexFormat.of().parseHex("C80104507F7F003700000000000000001E64000000003A67C2");
    A1Response response = new A1Response(data);

    /**
     * Power State Test
     */
    @Test
    public void testGetPowerState() {
        boolean actualPowerState = response.getPowerState();
        assertEquals(true, actualPowerState);
    }

    /**
     * Operational Mode Test
     */
    @Test
    public void testGetA1OperationalMode() {
        A1StringCommands.A1OperationalMode mode = response.getA1OperationalMode();
        assertEquals(A1StringCommands.A1OperationalMode.CLOTHES_DRY, mode);
    }

    /**
     * Fan Speed Test
     */
    @Test
    public void testGetFanSpeed() {
        A1StringCommands.A1FanSpeed fanSpeed = response.getA1FanSpeed();
        assertEquals(A1StringCommands.A1FanSpeed.HIGH, fanSpeed);
    }

    /**
     * Target Humidity Test
     */
    @Test
    public void testGetTargetHumidity() {
        assertEquals(55, response.getMaximumHumidity());
    }

    /**
     * Indoor Temperature Test
     */
    @Test
    public void testGetIndoorTemperature() {
        assertEquals(25, response.getIndoorTemperature());
    }

    /**
     * Room Humidity Test (actual humidity)
     */
    @Test
    public void testGetHumidity() {
        assertEquals(30, response.getCurrentHumidity());
    }
}
