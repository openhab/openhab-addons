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
package org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the mapping of washer/dryer operation states and times.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class WasherDryerSnapshotTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void v2StatesAreMappedIndependentlyRegardlessOfFieldOrder() throws JsonProcessingException {
        assertStates("""
                {
                  "state": "RUNNING",
                  "preState": "DETECTING",
                  "processState": "WASHING"
                }
                """);
        assertStates("""
                {
                  "processState": "WASHING",
                  "preState": "DETECTING",
                  "state": "RUNNING"
                }
                """);
    }

    @Test
    void v1StatesAreMappedIndependently() throws JsonProcessingException {
        WasherDryerSnapshot snapshot = mapper.readValue("""
                {
                  "State": "RUNNING",
                  "PreState": "DETECTING",
                  "ProcessState": "WASHING"
                }
                """, WasherDryerSnapshot.class);

        assertEquals("RUNNING", snapshot.getState());
        assertEquals("DETECTING", snapshot.getPreviousState());
        assertEquals("WASHING", snapshot.getProcessState());
    }

    @Test
    void missingProcessStateIsNotReplacedWithPreviousState() throws JsonProcessingException {
        WasherDryerSnapshot snapshot = mapper.readValue("""
                {
                  "state": "RUNNING",
                  "preState": "DETECTING"
                }
                """, WasherDryerSnapshot.class);

        assertEquals("RUNNING", snapshot.getState());
        assertEquals("DETECTING", snapshot.getPreviousState());
        assertTrue(snapshot.getProcessState().isEmpty());
    }

    /**
     * Some washers (e.g. F4X9009TWCE) report the whole duration in the minute field instead of keeping it in 0-59.
     */
    @ParameterizedTest
    @CsvSource({ //
            "0, 65, 01:05", //
            "1, 5, 01:05", //
            "0, 59, 00:59", //
            "0, 0, 00:00", //
            "2, 130, 04:10" })
    void remainingAndReserveTimesAreNormalized(int hour, int minute, String expected) throws JsonProcessingException {
        WasherDryerSnapshot v2 = mapper.readValue(String.format("""
                {
                  "remainTimeHour": %d,
                  "remainTimeMinute": %d,
                  "reserveTimeHour": %d,
                  "reserveTimeMinute": %d
                }
                """, hour, minute, hour, minute), WasherDryerSnapshot.class);
        assertEquals(expected, v2.getRemainingTime());
        assertEquals(expected, v2.getReserveTime());

        WasherDryerSnapshot v1 = mapper.readValue(String.format("""
                {
                  "Remain_Time_H": %d,
                  "Remain_Time_M": %d
                }
                """, hour, minute), WasherDryerSnapshot.class);
        assertEquals(expected, v1.getRemainingTime());
    }

    @Test
    void missingHourFieldUsesTotalMinutes() throws JsonProcessingException {
        WasherDryerSnapshot snapshot = mapper.readValue("""
                {
                  "remainTimeMinute": 65
                }
                """, WasherDryerSnapshot.class);

        assertEquals("01:05", snapshot.getRemainingTime());
    }

    private void assertStates(String json) throws JsonProcessingException {
        WasherDryerSnapshot snapshot = mapper.readValue(json, WasherDryerSnapshot.class);
        assertEquals("RUNNING", snapshot.getState());
        assertEquals("DETECTING", snapshot.getPreviousState());
        assertEquals("WASHING", snapshot.getProcessState());
    }
}
