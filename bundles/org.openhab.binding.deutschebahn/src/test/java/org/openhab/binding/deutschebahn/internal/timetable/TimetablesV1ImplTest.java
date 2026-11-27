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
package org.openhab.binding.deutschebahn.internal.timetable;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Timetable;

/**
 * Tests for {@link TimetablesV1Impl}
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public class TimetablesV1ImplTest implements TimetablesV1ImplTestHelper {

    @Test
    public void testGetDataForLehrte() throws Exception {
        TimetablesV1Api timeTableApi = createApiWithTestdata().getApi();

        Date time = createDate(2021, 8, 16, 9, 22);

        Timetable timeTable = timeTableApi.getPlan(EVA_LEHRTE, time);
        assertNotNull(timeTable);
        assertEquals(8, timeTable.getS().size());
    }

    @Test
    public void testGetNonExistingData() throws Exception {
        TimetablesV1Api timeTableApi = createApiWithTestdata().getApi();

        Date time = createDate(2021, 8, 16, 9, 22);

        Timetable timeTable = timeTableApi.getPlan("ABCDEF", time);
        assertNotNull(timeTable);
        assertEquals(0, timeTable.getS().size());
    }

    @Test
    public void testGetDataForHannoverHBF() throws Exception {
        TimetablesV1Api timeTableApi = createApiWithTestdata().getApi();

        Date time = createDate(2021, 10, 14, 11, 0);

        Timetable timeTable = timeTableApi.getPlan(EVA_HANNOVER_HBF, time);
        assertNotNull(timeTable);
        assertEquals(50, timeTable.getS().size());

        Timetable changes = timeTableApi.getFullChanges(EVA_HANNOVER_HBF);
        assertNotNull(changes);
        assertEquals(730, changes.getS().size());
    }

    @Test
    public void testPlanRequestUsesConfiguredTimeZoneInWinter() throws Exception {
        assertPlanUrl("2022-02-23T13:26:00Z",
                "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/220223/14");
    }

    @Test
    public void testPlanRequestUsesConfiguredTimeZoneInSummer() throws Exception {
        assertPlanUrl("2022-08-23T12:26:00Z",
                "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/220823/14");
    }

    @Test
    public void testPlanRequestUsesLocalDateAtMidnight() throws Exception {
        assertPlanUrl("2022-02-23T23:30:00Z",
                "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/8000226/220224/00");
    }

    private void assertPlanUrl(String instant, String expectedUrl) throws Exception {
        TimetablesApiTestModule testModule = createApiWithTestdata();

        testModule.getApi().getPlan(EVA_LEHRTE, Date.from(Instant.parse(instant)));

        assertEquals(List.of(expectedUrl), testModule.getRequestedPlanUrls());
    }
}
