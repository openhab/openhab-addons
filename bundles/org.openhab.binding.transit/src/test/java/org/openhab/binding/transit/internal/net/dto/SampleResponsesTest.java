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
package org.openhab.binding.transit.internal.net.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.transit.internal.net.dto.PlanResult.Itinerary;
import org.openhab.binding.transit.internal.net.dto.PlanResult.Leg;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult.MergedItinerary;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult.RouteDeparture;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult.ScheduleItem;

import com.google.gson.Gson;

/**
 * The {@link SampleResponsesTest} parses real responses of the Transit API v4 (Stuttgart, VVS).
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class SampleResponsesTest {

    private final Gson gson = new Gson();

    private <T> T parse(String resource, Class<T> type) throws IOException {
        try (InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream(resource))) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return Objects.requireNonNull(gson.fromJson(json, type));
        }
    }

    @Test
    public void testStopDepartures() throws IOException {
        StopDeparturesResult result = parse("stop_departures.json", StopDeparturesResult.class);
        RouteDeparture route = result.getRouteDepartures().get(0);
        assertEquals("43", route.getRouteShortName());
        assertEquals("VVSDE:2298", route.getGlobalStopId());
        assertEquals("VVSDE:247174", route.getGlobalRouteId());
        assertEquals("c31924", route.getRouteColor());
        assertEquals("Bus", route.getModeKey());
        assertEquals("VVS|Stuttgart", route.getRouteNetworkId());
        assertTrue(route.getAlerts().isEmpty());

        MergedItinerary itinerary = route.getMergedItineraries().get(0);
        List<ScheduleItem> items = itinerary.getScheduleItems();
        assertEquals(9, items.size());
        ScheduleItem first = items.get(0);
        assertEquals("Feuersee", itinerary.getHeadsign(first));
        assertEquals(Boolean.TRUE, first.getIsRealTime());
        assertEquals(Boolean.FALSE, first.getIsLast());
        assertEquals(Boolean.TRUE, items.get(8).getIsLast());
        assertEquals("VVSDE:53038040:44:4:11", first.getTripSearchKey());
    }

    @Test
    public void testRouteDetails() throws IOException {
        RouteDetailsResult result = parse("route_details.json", RouteDetailsResult.class);
        RouteDetailsResult.Route route = Objects.requireNonNull(result.route);
        assertEquals("43", route.routeShortName);
        assertEquals("c31924", route.routeColor);
        assertEquals("Bus", route.modeKey);
        assertEquals("VVS|Stuttgart", route.routeNetworkId);
        assertEquals(0, Objects.requireNonNull(route.alerts).size());
    }

    @Test
    public void testPlan() throws IOException {
        PlanResult result = parse("plan.json", PlanResult.class);
        assertEquals(2, result.getResults().size());

        // both itineraries are upcoming, the first one arrives earlier although it takes longer
        Itinerary itinerary = Objects.requireNonNull(result.getEarliestArrival(1792918900L));
        assertEquals(445L, itinerary.getDuration());
        assertEquals(1792919353L, Objects.requireNonNull(itinerary.getEndTime()).getEpochSecond());

        List<Leg> transitLegs = itinerary.getTransitLegs();
        assertEquals(1, transitLegs.size());
        Leg leg = transitLegs.get(0);
        // the leg is served by U6, U7 and U12, the first departure is a U6
        assertEquals("U6", leg.getRouteShortName());
        assertEquals("VVSDE:53038040:6121:4:16", Objects.requireNonNull(leg.getFirstDeparture()).getTripSearchKey());

        // after the first itinerary has started only the second one is left
        Itinerary later = Objects.requireNonNull(result.getEarliestArrival(1792919000L));
        assertEquals(420L, later.getDuration());
        assertEquals("44", later.getTransitLegs().get(0).getRouteShortName());

        assertNull(result.getEarliestArrival(1792920000L));
    }

    @Test
    public void testEstimatePlanDuration() throws IOException {
        EstimatePlanDurationResult result = parse("estimate_plan_duration.json", EstimatePlanDurationResult.class);
        assertEquals(445.0, result.getDuration());
        assertEquals("Transit", result.getType());
    }

    @Test
    public void testVehicles() throws IOException {
        VehiclesResult result = parse("vehicles.json", VehiclesResult.class);
        assertEquals(7, result.getVehicles().size());
        VehiclesResult.Vehicle vehicle = result.getVehicles().get(0);
        assertEquals("VVSDE:247174:316|7284_I", vehicle.getVehicleId());
        assertEquals(48.809547, vehicle.getLatitude());
        assertEquals(9.182034, vehicle.getLongitude());
        assertEquals(0, vehicle.getDirectionId());
        assertEquals(1792918590L, Objects.requireNonNull(vehicle.getUpdatedAt()).getEpochSecond());
        assertNull(vehicle.getOccupancyStatus());
        assertNull(vehicle.getWheelchairAccessible());
    }

    @Test
    public void testNearbyStops() throws IOException {
        StopsResult result = parse("nearby_stops.json", StopsResult.class);
        assertEquals(28, result.getStops().size());
        StopsResult.Stop stop = result.getStops().get(0);
        assertEquals("VVSDE:2394", stop.getGlobalStopId());
        assertEquals("Schlossplatz", stop.getStopName());
        assertEquals("Stuttgart", stop.getCityName());
    }

    @Test
    public void testSearchStops() throws IOException {
        StopsResult result = parse("search_stops.json", StopsResult.class);
        assertFalse(result.getStops().isEmpty());
        StopsResult.Stop stop = result.getStops().get(0);
        assertEquals("VVSDE:26686", stop.getGlobalStopId());
        assertEquals("Hauptbahnhof (A.-Klett-Platz)", stop.getStopName());
    }
}
