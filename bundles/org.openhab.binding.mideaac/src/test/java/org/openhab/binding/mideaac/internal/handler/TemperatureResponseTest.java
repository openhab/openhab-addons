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
 * The {@link TemperatureResponseTest} tests the methods in the HumidityResponse class
 * against an example response string.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class TemperatureResponseTest {
    @org.jupnp.registry.event.Before

    // From OH forum; Midea topic July 2025
    byte[] data = HexFormat.of().parseHex("A10000000000000000000A0A0A64FF000031050000000000000000000000003CD5DB");

    TemperatureResponse response = new TemperatureResponse(data);

    /**
     * Humidity Test 0XA1
     */
    @Test
    public void testGetHumidity() {
        assertEquals(49, response.getHumidity());
    }

    /**
     * Indoor Temperature Test 0xA1
     */
    @Test
    public void testGetIndoorTemperature() {
        assertEquals(25.5, response.getIndoorTemperature(), 0.0001);
    }

    /**
     * Outdoor Temperature Test 0xA1
     */
    @Test
    public void testGetOutdoorTemperature() {
        assertEquals(0.0, response.getOutdoorTemperature(), 0.0001);
    }

    /**
     * Get Current work time in minutes 0xA1
     */
    @Test
    public void testGetCurrentWorkTime() {
        assertEquals(15010, response.getCurrentWorkTime());
    }
}
