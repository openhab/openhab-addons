/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal.timetable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.deutschebahn.internal.EventType;
import org.openhab.binding.deutschebahn.internal.TimetableStopFilter;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;

/**
 * Tests for the {@link TimetableLoader}.
 * 
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public class TimetableLoaderTest implements TimetablesV1ImplTestHelper {

    @Test
    public void testLoadRequiredStopCount() throws Exception {
        final TimetablesApiTestModule timeTableTestModule = this.createApiWithTestdata();
        final TimeproviderStub timeProvider = new TimeproviderStub();
        final TimetableLoader loader = new TimetableLoader(timeTableTestModule.getApi(), TimetableStopFilter.ALL,
                EventType.DEPARTURE, timeProvider, EVA_LEHRTE, 20);

        timeProvider.time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 30);

        final List<TimetableStop> stops = loader.getTimetableStops();
        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09",
                        "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/10",
                        "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/11"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(), empty());

        assertThat(stops, hasSize(21));
        assertEquals("-5296516961807204721-2108160906-5", stops.get(0).getId());
        assertEquals("-3222259045572671319-2108161155-1", stops.get(20).getId());

        // when requesting again no further call to plan is made, because required stops are available.
        final List<TimetableStop> stops02 = loader.getTimetableStops();
        assertThat(stops02, hasSize(21));
        assertThat(timeTableTestModule.getRequestedPlanUrls(), hasSize(3));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(), hasSize(1));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(), empty());
    }

    @Test
    public void testLoadNewDataIfRequired() throws Exception {
        final TimetablesApiTestModule timeTableTestModule = this.createApiWithTestdata();
        final TimeproviderStub timeProvider = new TimeproviderStub();
        final TimetableLoader loader = new TimetableLoader(timeTableTestModule.getApi(), TimetableStopFilter.ALL,
                EventType.DEPARTURE, timeProvider, EVA_LEHRTE, 8);

        timeProvider.time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 0);

        final List<TimetableStop> stops = loader.getTimetableStops();
        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(), empty());

        assertThat(stops, hasSize(8));
        assertEquals("1763676552526687479-2108160847-6", stops.get(0).getId());
        assertEquals("8681599812964340829-2108160955-1", stops.get(7).getId());

        // Move clock ahead for 30 minutes, so that some of the fetched data is in past and new plan data must be
        // requested
        timeProvider.moveAhead(30 * 60);

        final List<TimetableStop> stops02 = loader.getTimetableStops();
        assertThat(stops02, hasSize(13));
        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09",
                        "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/10"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226",
                        "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(), empty());

        assertEquals("-5296516961807204721-2108160906-5", stops02.get(0).getId());
        assertEquals("-3376513334056532423-2108161055-1", stops02.get(12).getId());
    }

    @Test
    public void testRequestUpdates() throws Exception {
        final TimetablesApiTestModule timeTableTestModule = this.createApiWithTestdata();
        final TimeproviderStub timeProvider = new TimeproviderStub();
        final TimetableLoader loader = new TimetableLoader(timeTableTestModule.getApi(), TimetableStopFilter.ALL,
                EventType.DEPARTURE, timeProvider, EVA_LEHRTE, 1);

        timeProvider.time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 30);

        // First call - plan and full changes are requested.
        loader.getTimetableStops();
        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(), empty());

        // Changes are updated only every 30 seconds, so move clock ahead 20 seconds, no request is made
        timeProvider.moveAhead(20);
        loader.getTimetableStops();

        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(), empty());

        // Move ahead 10 seconds, so recent changes are fetched
        timeProvider.moveAhead(10);
        loader.getTimetableStops();
        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/rchg/8000226"));

        // Move again ahead 30 seconds, recent changes are fetched again
        timeProvider.moveAhead(30);
        loader.getTimetableStops();
        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/rchg/8000226",
                        "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/rchg/8000226"));

        // If recent change were not updated last 120 seconds the full changes must be requested
        timeProvider.moveAhead(120);
        loader.getTimetableStops();
        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226",
                        "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/rchg/8000226",
                        "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/rchg/8000226"));
    }

    @Test
    public void testReturnOnlyArrivals() throws Exception {
        final TimetablesApiTestModule timeTableTestModule = this.createApiWithTestdata();
        final TimeproviderStub timeProvider = new TimeproviderStub();
        final TimetableLoader loader = new TimetableLoader(timeTableTestModule.getApi(), TimetableStopFilter.ARRIVALS,
                EventType.ARRIVAL, timeProvider, EVA_LEHRTE, 20);

        // Simulate that only one url is available
        timeTableTestModule.addAvailableUrl(
                "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09");

        timeProvider.time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 0);

        final List<TimetableStop> stops = loader.getTimetableStops();

        // File contains 8 stops, but 2 are only departures
        assertThat(stops, hasSize(6));
        assertEquals("1763676552526687479-2108160847-6", stops.get(0).getId());
        assertEquals("-735649762452915464-2108160912-6", stops.get(5).getId());
    }

    @Test
    public void testReturnOnlyDepartures() throws Exception {
        final TimetablesApiTestModule timeTableTestModule = this.createApiWithTestdata();
        final TimeproviderStub timeProvider = new TimeproviderStub();
        final TimetableLoader loader = new TimetableLoader(timeTableTestModule.getApi(), TimetableStopFilter.DEPARTURES,
                EventType.DEPARTURE, timeProvider, EVA_LEHRTE, 20);

        // Simulate that only one url is available
        timeTableTestModule.addAvailableUrl(
                "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09");

        timeProvider.time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 0);

        final List<TimetableStop> stops = loader.getTimetableStops();

        // File contains 8 stops, but 2 are only arrivals
        assertThat(stops, hasSize(6));
        assertEquals("-94442819435724762-2108160916-1", stops.get(0).getId());
        assertEquals("8681599812964340829-2108160955-1", stops.get(5).getId());
    }

    @Test
    public void testRemoveEntryOnlyIfChangedTimeIsInPast() throws Exception {
        final TimetablesApiTestModule timeTableTestModule = this.createApiWithTestdata();
        final TimeproviderStub timeProvider = new TimeproviderStub();
        final TimetableLoader loader = new TimetableLoader(timeTableTestModule.getApi(), TimetableStopFilter.DEPARTURES,
                EventType.DEPARTURE, timeProvider, EVA_LEHRTE, 1);

        timeProvider.time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 35);

        final List<TimetableStop> stops = loader.getTimetableStops();
        assertThat(timeTableTestModule.getRequestedPlanUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/210816/09"));
        assertThat(timeTableTestModule.getRequestedFullChangesUrls(),
                contains("https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/fchg/8000226"));
        assertThat(timeTableTestModule.getRequestedRecentChangesUrls(), empty());

        // Stop -5296516961807204721-2108160906-5 has its planned time at 9:34, but its included because its changed
        // time is 9:42
        assertThat(stops, hasSize(4));
        assertEquals("-5296516961807204721-2108160906-5", stops.get(0).getId());
        assertEquals("2108160942", stops.get(0).getDp().getCt());
        assertEquals("8681599812964340829-2108160955-1", stops.get(3).getId());
    }
}
