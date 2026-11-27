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

package org.openhab.binding.emerald.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

public class EmeraldMqttParsingTest {

    private EmeraldHWSHandler handler;

    @BeforeEach
    public void setup() {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getUID()).thenReturn(new ThingUID("emerald:hws:account:test-hws"));
        handler = new EmeraldHWSHandler(thing);
    }

    @Test
    public void testMalformedAndUnexpectedPayloadTypesDoNotThrow() {
        // Syntax error
        assertDoesNotThrow(() -> handler.updateFromMqtt("not a json string"));

        // Valid JSON array with unexpected primitive data types
        String unexpectedTypes = """
                [
                  { "msg_id": "1234" },
                  { "temp_current": "invalid_number", "switch": [1, 2, 3], "fault": true }
                ]
                """;
        assertDoesNotThrow(() -> handler.updateFromMqtt(unexpectedTypes));

        // Normal payload
        String standardPayload = """
                [
                  { "msg_id": "5678" },
                  { "temp_current": 55, "temp_set": 60, "switch": 1, "mode": 1 }
                ]
                """;
        assertDoesNotThrow(() -> handler.updateFromMqtt(standardPayload));
    }
}
