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
package org.openhab.binding.tesla.internal.protocol.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests that {@link ClimateState} keeps the heater values the vehicle handler turns into channel updates.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@NonNullByDefault
public class ClimateStateTest {

    private final Gson gson = new Gson();

    @Test
    public void keepsRearSeatAndSteeringWheelHeaters() {
        JsonObject json = roundTrip("{\"seat_heater_rear_left\":2,\"seat_heater_rear_right\":0,"
                + "\"seat_heater_rear_center\":1,\"steering_wheel_heater\":true}");

        assertEquals(2, json.get("seat_heater_rear_left").getAsInt());
        assertEquals(0, json.get("seat_heater_rear_right").getAsInt());
        assertEquals(1, json.get("seat_heater_rear_center").getAsInt());
        assertTrue(json.get("steering_wheel_heater").getAsBoolean());
    }

    @Test
    public void leavesOutHeatersTheVehicleDoesNotReport() {
        JsonObject json = roundTrip("{\"seat_heater_left\":1}");

        assertFalse(json.has("seat_heater_rear_center"));
        assertFalse(json.has("seat_heater_rear_left_back"));
        assertFalse(json.has("seat_heater_rear_right_back"));
        assertFalse(json.has("steering_wheel_heater"));
    }

    // same conversion as TeslaVehicleHandler.parseAndUpdate() before the channels are updated
    private JsonObject roundTrip(String climateStateJson) {
        ClimateState climateState = gson.fromJson(climateStateJson, ClimateState.class);
        return gson.toJsonTree(climateState, ClimateState.class).getAsJsonObject();
    }
}
