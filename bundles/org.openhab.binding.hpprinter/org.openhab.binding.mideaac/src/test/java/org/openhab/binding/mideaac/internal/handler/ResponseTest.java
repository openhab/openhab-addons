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
package org.openhab.binding.mideaac.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link ResponseTest} extracts the AC device response and
 * compares them to the expected result.
 *
 * @author Robert Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ResponseTest {
    @org.jupnp.registry.event.Before

    byte[] data = HexFormat.of().parseHex("C00042668387123C00000460FF007000000000000000F9ECDB");
    private int version = 3;
    String responseType = "query";
    byte bodyType = (byte) 0xC0;
    Response response = new Response(data, version, responseType, bodyType);

    @Test
    public void testGetPowerState() {
        boolean actualPowerState = response.getPowerState();
        assertEquals(false, actualPowerState);
    }

    @Test
    public void testGetImmodeResume() {
        boolean actualImmodeResume = response.getImmodeResume();
        assertEquals(false, actualImmodeResume);
    }

    @Test
    public void testGetTimerMode() {
        assertEquals(false, response.getTimerMode());
    }

    @Test
    public void testGetPromptTone() {
        assertEquals(false, response.getPromptTone());
    }

    @Test
    public void testGetApplianceError() {
        assertEquals(false, response.getApplianceError());
    }

    @Test
    public void testGetTargetTemperature() {
        assertEquals(18, response.getTargetTemperature());
    }

    @Test
    public void testGetOperationalMode() {
        CommandBase.OperationalMode mode = response.getOperationalMode();
        assertEquals(CommandBase.OperationalMode.COOL, mode);
    }

    @Test
    public void testGetFanSpeed() {
        CommandBase.FanSpeed fanSpeed = response.getFanSpeed();
        assertEquals(CommandBase.FanSpeed.AUTO3, fanSpeed);
    }

    @Test
    public void testGetOnTimer() {
        Timer status = response.getOnTimer();
        String expectedString = "enabled: true, hours: 0, minutes: 59";
        assertEquals(expectedString, status.toString());
    }

    @Test
    public void testGetOffTimer() {
        Timer status = response.getOffTimer();
        String expectedString = "enabled: true, hours: 1, minutes: 58";
        assertEquals(expectedString, status.toString());
    }

    @Test
    public void testGetSwingMode() {
        CommandBase.SwingMode swing = response.getSwingMode();
        assertEquals(CommandBase.SwingMode.VERTICAL3, swing);
    }

    @Test
    public void testGetCozySleep() {
        assertEquals(0, response.getCozySleep());
    }

    @Test
    public void testGetSave() {
        assertEquals(false, response.getSave());
    }

    @Test
    public void testGetLowFrequencyFan() {
        assertEquals(false, response.getLowFrequencyFan());
    }

    @Test
    public void testGetSuperFan() {
        assertEquals(false, response.getSuperFan());
    }

    @Test
    public void testGetFeelOwn() {
        assertEquals(false, response.getFeelOwn());
    }

    @Test
    public void testGetChildSleepMode() {
        assertEquals(false, response.getChildSleepMode());
    }

    @Test
    public void testExchangeAir() {
        assertEquals(false, response.getExchangeAir());
    }

    @Test
    public void testGetDryClean() {
        assertEquals(false, response.getDryClean());
    }

    @Test
    public void testGetAuxHeat() {
        assertEquals(false, response.getAuxHeat());
    }

    @Test
    public void testGetEcoMode() {
        assertEquals(false, response.getEcoMode());
    }

    @Test
    public void testGetCleanUp() {
        assertEquals(false, response.getCleanUp());
    }

    @Test
    public void testGetTempUnit() {
        assertEquals(false, response.getTempUnit());
    }

    @Test
    public void testGetSleepFunction() {
        assertEquals(false, response.getSleepFunction());
    }

    @Test
    public void testGetTurboMode() {
        assertEquals(false, response.getTurboMode());
    }

    @Test
    public void testGetFahrenheit() {
        assertEquals(true, response.getFahrenheit());
    }

    @Test
    public void testGetCatchCold() {
        assertEquals(false, response.getCatchCold());
    }

    @Test
    public void testGetNightLight() {
        assertEquals(false, response.getNightLight());
    }

    @Test
    public void testGetPeakElec() {
        assertEquals(false, response.getPeakElec());
    }

    @Test
    public void testGetNaturalFan() {
        assertEquals(false, response.getNaturalFan());
    }

    @Test
    public void testGetIndoorTemperature() {
        assertEquals(23, response.getIndoorTemperature());
    }

    @Test
    public void testGetOutdoorTemperature() {
        assertEquals(0, response.getOutdoorTemperature());
    }

    @Test
    public void testDisplayOn() {
        assertEquals(false, response.getDisplayOn());
    }

    @Test
    public void testGetHumidity() {
        assertEquals(0, response.getHumidity());
    }
}
