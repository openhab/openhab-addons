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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the mapping of washer/dryer operation states.
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

    private void assertStates(String json) throws JsonProcessingException {
        WasherDryerSnapshot snapshot = mapper.readValue(json, WasherDryerSnapshot.class);
        assertEquals("RUNNING", snapshot.getState());
        assertEquals("DETECTING", snapshot.getPreviousState());
        assertEquals("WASHING", snapshot.getProcessState());
    }
}
