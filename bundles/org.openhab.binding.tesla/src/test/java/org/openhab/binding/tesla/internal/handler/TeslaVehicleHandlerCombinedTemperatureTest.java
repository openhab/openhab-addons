/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tesla.internal.protocol.dto.ClimateState;

import com.google.gson.Gson;

/**
 * Tests the combined temperature of {@link TeslaVehicleHandler}.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@NonNullByDefault
public class TeslaVehicleHandlerCombinedTemperatureTest {

    private final Gson gson = new Gson();

    @Test
    public void averagesDriverAndPassengerSettings() {
        assertEquals(new BigDecimal("21.0"), combinedTemperature(22.0, 20.0));
    }

    @Test
    public void equalSettingsGiveThatSetting() {
        assertEquals(new BigDecimal("19.5"), combinedTemperature(19.5, 19.5));
    }

    @Test
    public void roundsToOneDecimal() {
        assertEquals(new BigDecimal("20.8"), combinedTemperature(21.5, 20.0));
    }

    private BigDecimal combinedTemperature(double driver, double passenger) {
        ClimateState climateState = gson.fromJson(
                "{\"driver_temp_setting\":" + driver + ",\"passenger_temp_setting\":" + passenger + "}",
                ClimateState.class);
        assertNotNull(climateState);
        return TeslaVehicleHandler.combinedTemperature(climateState);
    }
}
