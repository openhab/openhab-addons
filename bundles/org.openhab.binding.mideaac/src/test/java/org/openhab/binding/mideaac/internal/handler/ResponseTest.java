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
 * The {@link ResponseTest} tests the methods in the Response class
 * against an example response string.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ResponseTest {
    @org.jupnp.registry.event.Before

    byte[] data = HexFormat.of().parseHex("C00042668387123C00000460FF0C7000000000320000F9ECDB");
    private int version = 3;
    Response response = new Response(data, version);

    /**
     * Power State Test
     */
    @Test
    public void testGetPowerState() {
        boolean actualPowerState = response.getPowerState();
        assertEquals(false, actualPowerState);
    }

    /**
     * Prompt Tone Test
     */
    @Test
    public void testGetPromptTone() {
        assertEquals(false, response.getPromptTone());
    }

    /**
     * Appliance Error Test
     */
    @Test
    public void testGetApplianceError() {
        assertEquals(false, response.getApplianceError());
    }

    /**
     * Target Temperature Test
     */
    @Test
    public void testGetTargetTemperature() {
        assertEquals(18, response.getTargetTemperature());
    }

    /**
     * Operational Mode Test
     */
    @Test
    public void testGetOperationalMode() {
        CommandBase.OperationalMode mode = response.getOperationalMode();
        assertEquals(CommandBase.OperationalMode.COOL, mode);
    }

    /**
     * Fan Speed Test
     */
    @Test
    public void testGetFanSpeed() {
        CommandBase.FanSpeed fanSpeed = response.getFanSpeed();
        assertEquals(CommandBase.FanSpeed.AUTO3, fanSpeed);
    }

    /**
     * On timer Test
     */
    @Test
    public void testGetOnTimer() {
        Timer status = response.getOnTimer();
        String expectedString = "enabled: true, hours: 0, minutes: 59";
        assertEquals(expectedString, status.toString());
    }

    /**
     * Off timer Test
     */
    @Test
    public void testGetOffTimer() {
        Timer status = response.getOffTimer();
        String expectedString = "enabled: true, hours: 1, minutes: 58";
        assertEquals(expectedString, status.toString());
    }

    /**
     * Swing mode Test
     */
    @Test
    public void testGetSwingMode() {
        CommandBase.SwingMode swing = response.getSwingMode();
        assertEquals(CommandBase.SwingMode.VERTICAL3, swing);
    }

    /**
     * Auxiliary Heat Status Test
     */
    @Test
    public void testGetAuxHeat() {
        assertEquals(false, response.getAuxHeat());
    }

    /**
     * Eco Mode Test
     */
    @Test
    public void testGetEcoMode() {
        assertEquals(false, response.getEcoMode());
    }

    /**
     * Sleep Function Test
     */
    @Test
    public void testGetSleepFunction() {
        assertEquals(false, response.getSleepFunction());
    }

    /**
     * Turbo Mode Test
     */
    @Test
    public void testGetTurboMode() {
        assertEquals(false, response.getTurboMode());
    }

    /**
     * Fahrenheit Display Test
     */
    @Test
    public void testGetFahrenheit() {
        assertEquals(true, response.getFahrenheit());
    }

    /**
     * Indoor Temperature Test
     */
    @Test
    public void testGetIndoorTemperature() {
        assertEquals(23, response.getIndoorTemperature());
    }

    /**
     * Outdoor Temperature Test
     */
    @Test
    public void testGetOutdoorTemperature() {
        assertEquals(0, response.getOutdoorTemperature());
    }

    /**
     * LED Display Test
     */
    @Test
    public void testDisplayOn() {
        assertEquals(false, response.getDisplayOn());
    }

    /**
     * Humidity Test
     */
    @Test
    public void testGetHumidity() {
        assertEquals(50, response.getTargetHumidity());
    }
}
