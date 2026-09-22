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
package org.openhab.binding.evcc.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelTypeRegistry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Tests for {@link EvccBaseThingHandler#addPhaseChannels}, the array-to-per-phase-channel
 * flattening used by every handler that reports 3-phase measurements (grid currents/voltages,
 * battery/loadpoint charge phases). This transformation directly feeds the routed "grid"/"battery"
 * update objects that {@link EvccSiteHandlerTest} verifies are mapped to the correct channel IDs,
 * so its correctness is a prerequisite for routing to reach the right channels with the right
 * values.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
class EvccBaseThingHandlerPhaseChannelsTest {

    private final EvccBaseThingHandler handler = new EvccSiteHandler(mock(Thing.class),
            mock(ChannelTypeRegistry.class));

    @Test
    void addPhaseChannelsMapsArrayEntriesToOneIndexedPhaseKeys() {
        JsonObject state = new JsonObject();
        JsonArray currents = new JsonArray();
        currents.add(6);
        currents.add(7);
        currents.add(8);

        handler.addPhaseChannels(state, currents, "grid", "Current");

        assertEquals(6, state.get("gridCurrentL1").getAsInt());
        assertEquals(7, state.get("gridCurrentL2").getAsInt());
        assertEquals(8, state.get("gridCurrentL3").getAsInt());
    }

    @Test
    void addPhaseChannelsUsesPrefixAndDatapointToBuildChannelKey() {
        JsonObject state = new JsonObject();
        JsonArray voltages = new JsonArray();
        voltages.add(230.0);
        voltages.add(231.0);

        handler.addPhaseChannels(state, voltages, "charge", "Voltage");

        assertEquals(230.0, state.get("chargeVoltageL1").getAsDouble());
        assertEquals(231.0, state.get("chargeVoltageL2").getAsDouble());
        assertFalse(state.has("chargeVoltageL3"));
    }

    @Test
    void addPhaseChannelsWithEmptyArrayAddsNoChannels() {
        JsonObject state = new JsonObject();

        handler.addPhaseChannels(state, new JsonArray(), "grid", "Power");

        assertEquals(0, state.size());
    }
}
