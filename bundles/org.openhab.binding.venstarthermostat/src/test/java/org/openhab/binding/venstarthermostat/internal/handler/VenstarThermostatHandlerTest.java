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
package org.openhab.binding.venstarthermostat.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarInfoData;
import org.openhab.binding.venstarthermostat.internal.dto.VenstarSystemState;
import org.openhab.core.thing.Thing;

/**
 * Tests parsing of the /query/info reply.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class VenstarThermostatHandlerTest {
    private static final String INFO = """
            {"name":"Master Bedroom","mode":0,"state":0,"fan":0,"fanstate":0,"tempunits":0,"schedule":0,
            "schedulepart":255,"away":0,"spacetemp":78.0,"heattemp":60.0,"cooltemp":69.0}""";

    private final VenstarThermostatHandler handler = new VenstarThermostatHandler(mock(Thing.class));

    @Test
    void parsesCompleteReply() throws Exception {
        VenstarInfoData info = handler.parseInfoData(INFO);
        assertEquals(60.0, info.getHeattemp());
        assertEquals(69.0, info.getCooltemp());
        assertEquals(VenstarSystemState.IDLE, info.getSystemState());
    }

    @Test
    void rejectsErrorReplyWithReason() {
        Exception e = assertThrows(Exception.class,
                () -> handler.parseInfoData("{\"error\":true,\"reason\":\"Busy\"}"));
        assertEquals("Busy", e.getMessage());
    }

    @Test
    void rejectsReplyWithoutSetpoints() {
        String partial = INFO.replace(",\"heattemp\":60.0,\"cooltemp\":69.0", "");
        Exception e = assertThrows(Exception.class, () -> handler.parseInfoData(partial));
        assertEquals("Invalid response from thermostat", e.getMessage());
    }

    @Test
    void rejectsReplyWithoutTempunits() {
        String partial = INFO.replace("\"tempunits\":0,", "");
        Exception e = assertThrows(Exception.class, () -> handler.parseInfoData(partial));
        assertEquals("Invalid response from thermostat", e.getMessage());
    }

    @Test
    void rejectsEmptyReply() {
        assertThrows(Exception.class, () -> handler.parseInfoData(""));
    }

    @Test
    void rejectsUnknownEnumValue() {
        assertThrows(Exception.class, () -> handler.parseInfoData(INFO.replace("\"state\":0", "\"state\":99")));
    }
}
