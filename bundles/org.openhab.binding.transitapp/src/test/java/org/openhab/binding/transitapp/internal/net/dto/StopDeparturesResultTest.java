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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * The {@link StopDeparturesResultTest} tests parsing of a Transit API v4 stop_departures response.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class StopDeparturesResultTest {

    private final Gson gson = new Gson();

    // Shortened excerpt of a real /v4/public/stop_departures response (VVSDE:2298, line 43)
    private static final String JSON = """
            {
              "route_departures": [
                {
                  "global_route_id": "VVSDE:247174",
                  "global_stop_id": "VVSDE:2298",
                  "route_short_name": "43",
                  "route_long_name": "Pragsattel - Rotebühlplatz (Stadtmitte) - Feuersee",
                  "merged_itineraries": [
                    {
                      "direction_id": 0,
                      "itineraries": [
                        {
                          "direction_id": 0,
                          "headsign": "Feuersee",
                          "internal_itinerary_id": "48"
                        }
                      ],
                      "schedule_items": [
                        {
                          "arrival_time": 1791395160,
                          "departure_time": 1791395160,
                          "internal_itinerary_id": "48",
                          "is_cancelled": false,
                          "is_last": false,
                          "is_real_time": true,
                          "rt_trip_id": "de:vvs:30043_:.vvs-30-43.1.T0.88.j26",
                          "scheduled_arrival_time": 1791395040,
                          "scheduled_departure_time": 1791395040,
                          "trip_search_key": "VVSDE:52903558:48:1:86",
                          "wheelchair_accessible": 0
                        },
                        {
                          "arrival_time": 1791400440,
                          "departure_time": 1791400440,
                          "internal_itinerary_id": "48",
                          "is_cancelled": false,
                          "is_last": false,
                          "is_real_time": false,
                          "rt_trip_id": "de:vvs:30043_:.vvs-30-43.1.T0.97.j26",
                          "scheduled_arrival_time": 1791400440,
                          "scheduled_departure_time": 1791400440,
                          "trip_search_key": "VVSDE:52903558:48:1:95",
                          "wheelchair_accessible": 0
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

    @Test
    public void testV4JsonParsing() {
        StopDeparturesResult result = Objects.requireNonNull(gson.fromJson(JSON, StopDeparturesResult.class));

        List<StopDeparturesResult.RouteDeparture> routeDepartures = result.getRouteDepartures();
        assertEquals(1, routeDepartures.size());

        StopDeparturesResult.RouteDeparture route = routeDepartures.get(0);
        assertEquals("43", route.getRouteShortName());
        assertEquals("Pragsattel - Rotebühlplatz (Stadtmitte) - Feuersee", route.getRouteLongName());

        List<StopDeparturesResult.MergedItinerary> mergedItineraries = route.getMergedItineraries();
        assertEquals(1, mergedItineraries.size());
        StopDeparturesResult.MergedItinerary mergedItinerary = mergedItineraries.get(0);

        List<StopDeparturesResult.Itinerary> itineraries = mergedItinerary.getItineraries();
        assertEquals(1, itineraries.size());
        assertEquals("Feuersee", itineraries.get(0).getHeadsign());
        assertEquals("48", itineraries.get(0).getInternalItineraryId());

        List<StopDeparturesResult.ScheduleItem> scheduleItems = mergedItinerary.getScheduleItems();
        assertEquals(2, scheduleItems.size());

        StopDeparturesResult.ScheduleItem item = scheduleItems.get(0);

        Instant depTime = Objects.requireNonNull(item.getDepartureTime());
        assertEquals(1791395160L, depTime.getEpochSecond());

        Instant scheduledDepTime = Objects.requireNonNull(item.getScheduledDepartureTime());
        assertEquals(1791395040L, scheduledDepTime.getEpochSecond());

        Instant arrTime = Objects.requireNonNull(item.getArrivalTime());
        assertEquals(1791395160L, arrTime.getEpochSecond());

        Instant scheduledArrTime = Objects.requireNonNull(item.getScheduledArrivalTime());
        assertEquals(1791395040L, scheduledArrTime.getEpochSecond());

        assertTrue(Objects.requireNonNull(item.getIsRealTime()));
        assertFalse(Objects.requireNonNull(item.getIsCancelled()));
        assertEquals("de:vvs:30043_:.vvs-30-43.1.T0.88.j26", item.getRtTripId());
        assertEquals("VVSDE:52903558:48:1:86", item.getTripSearchKey());
        assertEquals("48", item.getInternalItineraryId());
        assertEquals(0, Objects.requireNonNull(item.getWheelchairAccessible()).intValue());

        StopDeparturesResult.ScheduleItem lastItem = scheduleItems.get(1);
        assertFalse(Objects.requireNonNull(lastItem.getIsRealTime()));
    }
}
