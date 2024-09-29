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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

        Date time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 22).getTime();

        Timetable timeTable = timeTableApi.getPlan(EVA_LEHRTE, time);
        assertNotNull(timeTable);
        assertEquals(8, timeTable.getS().size());
    }

    @Test
    public void testGetNonExistingData() throws Exception {
        TimetablesV1Api timeTableApi = createApiWithTestdata().getApi();

        Date time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 22).getTime();

        Timetable timeTable = timeTableApi.getPlan("ABCDEF", time);
        assertNotNull(timeTable);
        assertEquals(0, timeTable.getS().size());
    }

    @Test
    public void testGetDataForHannoverHBF() throws Exception {
        TimetablesV1Api timeTableApi = createApiWithTestdata().getApi();

        Date time = new GregorianCalendar(2021, Calendar.OCTOBER, 14, 11, 0).getTime();

        Timetable timeTable = timeTableApi.getPlan(EVA_HANNOVER_HBF, time);
        assertNotNull(timeTable);
        assertEquals(50, timeTable.getS().size());

        Timetable changes = timeTableApi.getFullChanges(EVA_HANNOVER_HBF);
        assertNotNull(changes);
        assertEquals(730, changes.getS().size());
    }
}
