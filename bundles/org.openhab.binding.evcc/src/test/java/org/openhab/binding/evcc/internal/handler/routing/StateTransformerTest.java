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
package org.openhab.binding.evcc.internal.handler.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Tests for the {@link StateTransformer} implementations that normalize raw evcc JSON into the flat, channel-ready
 * structure used by the handlers. Since both the full-state initialization path and the partial websocket update path
 * now share the same transformer, these tests pin the transformation that guarantees both paths produce identical
 * channels.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
class StateTransformerTest {

    private static JsonArray array(Number... values) {
        JsonArray array = new JsonArray();
        for (Number value : values) {
            array.add(value);
        }
        return array;
    }

    @Test
    void gridTransformerFlattensScalarsAndExpandsPhaseArrays() {
        JsonObject grid = new JsonObject();
        grid.addProperty("power", 1234);
        grid.addProperty("energy", 42);
        grid.add("currents", array(6, 7, 8));
        grid.add("voltages", array(230, 231, 229));
        grid.add("powers", array(10, 20, 30));

        JsonObject result = new GridStateTransformer().transform(grid);

        assertEquals(1234, result.get("gridPower").getAsInt());
        assertEquals(42, result.get("gridEnergy").getAsInt());
        assertEquals(6, result.get("gridCurrentL1").getAsInt());
        assertEquals(8, result.get("gridCurrentL3").getAsInt());
        assertEquals(230, result.get("gridVoltageL1").getAsInt());
        assertEquals(30, result.get("gridPowerL3").getAsInt());
        assertFalse(result.has("currents"));
        assertFalse(result.has("power"));
    }

    @Test
    void loadpointTransformerRenamesKeysAndExpandsChargePhases() {
        JsonObject loadpoint = new JsonObject();
        loadpoint.addProperty("chargeCurrent", 16);
        loadpoint.addProperty("vehiclePresent", true);
        loadpoint.addProperty("phases", 3);
        loadpoint.add("chargeCurrents", array(6, 7, 8));
        loadpoint.add("chargeVoltages", array(230, 231, 229));
        loadpoint.addProperty("chargePower", 3600);

        JsonObject result = new LoadpointStateTransformer().transform(loadpoint);

        assertEquals(16, result.get("offeredCurrent").getAsInt());
        assertTrue(result.get("connected").getAsBoolean());
        assertEquals(3, result.get("phasesConfigured").getAsInt());
        assertEquals(6, result.get("chargeCurrentL1").getAsInt());
        assertEquals(231, result.get("chargeVoltageL2").getAsInt());
        assertEquals(3600, result.get("chargePower").getAsInt());
        assertFalse(result.has("chargeCurrent"));
        assertFalse(result.has("chargeCurrents"));
        assertFalse(result.has("vehiclePresent"));
    }

    @Test
    void loadpointTransformerLeavesSourceUntouched() {
        JsonObject loadpoint = new JsonObject();
        loadpoint.addProperty("chargeCurrent", 16);

        new LoadpointStateTransformer().transform(loadpoint);

        assertTrue(loadpoint.has("chargeCurrent"));
        assertFalse(loadpoint.has("offeredCurrent"));
    }

    @Test
    void heatingTransformerMapsTemperatureFieldsOntoSocKeys() {
        JsonObject heating = new JsonObject();
        heating.addProperty("effectiveLimitSoc", 60);
        heating.addProperty("vehicleSoc", 55);

        JsonObject result = new HeatingStateTransformer().transform(heating);

        assertEquals(60, result.get("effectiveLimitTemperature").getAsInt());
        assertEquals(55, result.get("vehicleTemperature").getAsInt());
        assertFalse(result.has("effectiveLimitSoc"));
        assertFalse(result.has("vehicleSoc"));
    }

    @Test
    void heatingTransformerResolvesApiKeyForCommands() {
        assertEquals("effectiveLimitSoc", HeatingStateTransformer.toApiKey("effectiveLimitTemperature"));
        assertEquals("vehicleSoc", HeatingStateTransformer.toApiKey("vehicleTemperature"));
    }
}
