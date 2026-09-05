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

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

@NonNullByDefault
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

        List<StopDeparturesResult.RouteDeparture> routeDepartures = result.getRouteDepartures();
        assertNotNull(routeDepartures);
        assertEquals(1, routeDepartures.size());

        // Assert route details
        StopDeparturesResult.RouteDeparture route = routeDepartures.get(0);
        assertEquals("105", route.getRouteShortName());
        assertEquals("Local Route", route.getRouteLongName());

        // Assert itinerary
        List<StopDeparturesResult.Itinerary> itineraries = route.getItineraries();
        assertNotNull(itineraries);
        assertEquals(1, itineraries.size());

        StopDeparturesResult.Itinerary itinerary = itineraries.get(0);
        assertEquals("Vendome", itinerary.getHeadsign());

        // Assert schedule item
        List<StopDeparturesResult.ScheduleItem> scheduleItems = itinerary.getScheduleItems();
        assertNotNull(scheduleItems);
        assertEquals(1, scheduleItems.size());

        StopDeparturesResult.ScheduleItem item = scheduleItems.get(0);

        // Assert Instant conversion using local variables to satisfy null analysis
        @Nullable
        Instant depTime = item.getDepartureTime();
        assertNotNull(depTime);
        assertEquals(1700000100L, depTime.getEpochSecond());

        @Nullable
        Instant scheduledDepTime = item.getScheduledDepartureTime();
        assertNotNull(scheduledDepTime);
        assertEquals(1700000000L, scheduledDepTime.getEpochSecond());

        @Nullable
        Boolean isRealTime = item.getIsRealTime();
        assertNotNull(isRealTime);
        assertTrue(isRealTime);

        @Nullable
        Integer wheelchair = item.getWheelchairAccessible();
        assertNotNull(wheelchair);
        assertEquals(1, wheelchair.intValue());
    }
}
