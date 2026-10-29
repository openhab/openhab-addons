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
package org.openhab.binding.transit.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.transit.internal.handler.TransitStopHandler.Departure;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult;

import com.google.gson.Gson;

/**
 * The {@link DepartureOrderingTest} tests how the stop handler merges and sorts the departures of several routes and
 * stops.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class DepartureOrderingTest {

    private final Gson gson = new Gson();

    // two stops (as requested with a comma-separated globalStopId), the later departure is listed first
    private static final String JSON = """
            {
              "route_departures": [
                {
                  "global_stop_id": "VVSDE:2298",
                  "route_short_name": "43",
                  "merged_itineraries": [
                    {
                      "itineraries": [ { "internal_itinerary_id": "44", "headsign": "Feuersee" } ],
                      "schedule_items": [
                        { "departure_time": 1700000600, "internal_itinerary_id": "44" },
                        { "departure_time": 1600000000, "internal_itinerary_id": "44" }
                      ]
                    }
                  ]
                },
                {
                  "global_stop_id": "VVSDE:2394",
                  "route_short_name": "U6",
                  "merged_itineraries": [
                    {
                      "itineraries": [ { "internal_itinerary_id": "1", "headsign": "Flughafen" } ],
                      "schedule_items": [
                        { "departure_time": 1700000300, "internal_itinerary_id": "1" }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

    @Test
    public void testDepartureOrdering() {
        StopDeparturesResult result = Objects.requireNonNull(gson.fromJson(JSON, StopDeparturesResult.class));

        List<Departure> departures = TransitStopHandler.flattenDepartures(result.getRouteDepartures(), 1700000000L);

        // the departure in the past is dropped, the others are sorted across both stops
        assertEquals(2, departures.size());
        assertEquals("U6", departures.get(0).route().getRouteShortName());
        assertEquals("VVSDE:2394", departures.get(0).route().getGlobalStopId());
        assertEquals("Flughafen", departures.get(0).itinerary().getHeadsign(departures.get(0).schedule()));
        assertEquals("43", departures.get(1).route().getRouteShortName());
        assertEquals(1700000600L, departures.get(1).departureTime().getEpochSecond());
    }

    @Test
    public void testNormalizeList() {
        assertEquals("VVSDE:2298,VVSDE:2394", TransitPollingHandler.normalizeList(" VVSDE:2298 , ,VVSDE:2394,"));
        assertEquals("BC Transit West Kootenay|West Kootenay",
                TransitPollingHandler.normalizeList(" BC Transit West Kootenay|West Kootenay "));
        assertEquals("", TransitPollingHandler.normalizeList(" , "));
    }
}
