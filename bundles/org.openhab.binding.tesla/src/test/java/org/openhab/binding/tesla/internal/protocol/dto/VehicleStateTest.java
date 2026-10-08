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
 * Tests that {@link VehicleState} only passes on the occupancy the vehicle reports.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@NonNullByDefault
public class VehicleStateTest {

    private final Gson gson = new Gson();

    @Test
    public void keepsReportedUserPresence() {
        assertTrue(roundTrip("{\"is_user_present\":true}").get("is_user_present").getAsBoolean());
        assertFalse(roundTrip("{\"is_user_present\":false}").get("is_user_present").getAsBoolean());
    }

    @Test
    public void leavesOutUserPresenceTheVehicleDoesNotReport() {
        assertFalse(roundTrip("{\"locked\":true}").has("is_user_present"));
    }

    // same conversion as TeslaVehicleHandler.parseAndUpdate() before the channels are updated
    private JsonObject roundTrip(String vehicleStateJson) {
        VehicleState vehicleState = gson.fromJson(vehicleStateJson, VehicleState.class);
        return gson.toJsonTree(vehicleState, VehicleState.class).getAsJsonObject();
    }
}
