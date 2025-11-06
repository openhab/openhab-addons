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
package org.openhab.binding.mideaac.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link HumidityResponseTest} tests the methods in the HumidityResponse class
 * against an example response string.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class HumidityResponseTest {
    @org.jupnp.registry.event.Before

    // From OH forum; Midea topic July 2025
    byte[] data = HexFormat.of().parseHex("A01240660000003C00000000003600000000000000004234EA");
    HumidityResponse response = new HumidityResponse(data);

    /**
     * Humidity Test
     */
    @Test
    public void testGetHumidity() {
        assertEquals(54, response.getHumidity());
    }

    /**
     * Power State
     */
    @Test
    public void testPowerState() {
        assertEquals(false, response.getPowerState());
    }

    /**
     * Target Temperature from 0XA0 Message
     */
    @Test
    public void testTargetTemperature() {
        assertEquals(21.0, response.getTargetTemperature());
    }

    /**
     * Operational mode from 0XA0 Message
     */
    @Test
    public void testOperationalMode() {
        assertEquals(CommandBase.OperationalMode.COOL, response.getOperationalMode());
    }

    /**
     * Fan Speed from 0XA0 Message
     */
    @Test
    public void testFanSpeed() {
        assertEquals(CommandBase.FanSpeed.AUTO3, response.getFanSpeed());
    }

    /**
     * Swing Mode from 0XA0 Message
     */
    @Test
    public void testSwingMode() {
        assertEquals(CommandBase.SwingMode.VERTICAL3, response.getSwingMode());
    }
}
