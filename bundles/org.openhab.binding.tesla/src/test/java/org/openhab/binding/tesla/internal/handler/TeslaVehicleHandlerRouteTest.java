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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tesla.internal.protocol.dto.DriveState;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests how {@link TeslaVehicleHandler} passes the navigation fields of the drive state on to the channels.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@NonNullByDefault
public class TeslaVehicleHandlerRouteTest {

    private final Gson gson = new Gson();

    @Test
    public void keepsActiveRoute() {
        JsonObject json = toJson("{\"shift_state\":\"D\",\"active_route_destination\":\"Home\","
                + "\"active_route_latitude\":51.1,\"active_route_longitude\":13.3,"
                + "\"active_route_miles_to_arrival\":2.59,\"active_route_minutes_to_arrival\":8.3,"
                + "\"active_route_traffic_minutes_delay\":0}");

        assertEquals("Home", json.get("active_route_destination").getAsString());
        assertEquals(51.1, json.get("active_route_latitude").getAsDouble());
        assertEquals(13.3, json.get("active_route_longitude").getAsDouble());
        assertEquals(2.59, json.get("active_route_miles_to_arrival").getAsDouble());
        assertEquals(8.3, json.get("active_route_minutes_to_arrival").getAsDouble());
        assertEquals(0, json.get("active_route_traffic_minutes_delay").getAsDouble());
    }

    @Test
    public void clearsRouteWhenNoRouteIsActive() {
        JsonObject json = toJson("{\"shift_state\":\"P\"}");

        for (String field : new String[] { "active_route_destination", "active_route_latitude",
                "active_route_longitude", "active_route_miles_to_arrival", "active_route_minutes_to_arrival",
                "active_route_traffic_minutes_delay" }) {
            assertTrue(json.get(field).isJsonNull(), field);
        }
        assertEquals("P", json.get("shift_state").getAsString());
    }

    private JsonObject toJson(String driveStateJson) {
        DriveState driveState = gson.fromJson(driveStateJson, DriveState.class);
        assertNotNull(driveState);
        return TeslaVehicleHandler.toJsonWithRouteCleared(gson, driveState);
    }
}
