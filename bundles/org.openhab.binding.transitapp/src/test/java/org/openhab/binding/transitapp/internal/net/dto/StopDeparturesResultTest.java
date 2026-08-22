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
package org.openhab.binding.transitapp.internal.net.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

public class StopDeparturesResultTest {

    private final Gson gson = new Gson();

    @Test
    public void testV4JsonParsing() {
        // Simulated JSON fixture matching the Transit API v4 response schema
        String json = "{\n" + "  \"route_departures\": [\n" + "    {\n" + "      \"route_short_name\": \"105\",\n"
                + "      \"route_long_name\": \"Local Route\",\n" + "      \"itineraries\": [\n" + "        {\n"
                + "          \"headsign\": \"Vendome\",\n" + "          \"schedule_items\": [\n" + "            {\n"
                + "              \"departure_time\": 1700000100,\n" + "              \"is_real_time\": true,\n"
                + "              \"scheduled_departure_time\": 1700000000,\n"
                + "              \"wheelchair_accessible\": 1\n" + "            }\n" + "          ]\n" + "        }\n"
                + "      ]\n" + "    }\n" + "  ]\n" + "}";

        StopDeparturesResult result = gson.fromJson(json, StopDeparturesResult.class);

        // Assert root object
        assertNotNull(result);
        assertEquals(1, result.getRouteDepartures().size());

        // Assert route details
        StopDeparturesResult.RouteDeparture route = result.getRouteDepartures().get(0);
        assertEquals("105", route.getRouteShortName());
        assertEquals("Local Route", route.getRouteLongName());

        // Assert itinerary
        assertEquals(1, route.getItineraries().size());
        StopDeparturesResult.Itinerary itinerary = route.getItineraries().get(0);
        assertEquals("Vendome", itinerary.getHeadsign());

        // Assert schedule item and Instant conversion
        assertEquals(1, itinerary.getScheduleItems().size());
        StopDeparturesResult.ScheduleItem item = itinerary.getScheduleItems().get(0);

        assertNotNull(item.getDepartureTime());
        assertEquals(1700000100L, item.getDepartureTime().getEpochSecond());

        assertNotNull(item.getScheduledDepartureTime());
        assertEquals(1700000000L, item.getScheduledDepartureTime().getEpochSecond());

        assertTrue(item.isRealTime());
        assertEquals(1, item.getWheelchairAccessible());
    }
}
