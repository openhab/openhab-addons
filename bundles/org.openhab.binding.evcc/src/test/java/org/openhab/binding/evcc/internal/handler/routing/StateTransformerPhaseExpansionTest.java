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
package org.openhab.binding.evcc.internal.handler.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Tests for {@link StateTransformer#expandPhases}, the array-to-per-phase-channel flattening shared by every
 * transformer that reports 3-phase measurements (grid currents/voltages/powers, loadpoint charge phases). This
 * transformation feeds the routed "grid"/"loadpoint" update objects that the handler tests verify are mapped to the
 * correct channel IDs, so its correctness is a prerequisite for routing to reach the right channels with the right
 * values.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
class StateTransformerPhaseExpansionTest {

    @Test
    void expandPhasesMapsArrayEntriesToOneIndexedPhaseKeys() {
        JsonObject state = new JsonObject();
        JsonArray currents = new JsonArray();
        currents.add(6);
        currents.add(7);
        currents.add(8);

        StateTransformer.expandPhases(state, currents, "grid", "Current");

        assertEquals(6, state.get("gridCurrentL1").getAsInt());
        assertEquals(7, state.get("gridCurrentL2").getAsInt());
        assertEquals(8, state.get("gridCurrentL3").getAsInt());
    }

    @Test
    void expandPhasesUsesPrefixAndDatapointToBuildChannelKey() {
        JsonObject state = new JsonObject();
        JsonArray voltages = new JsonArray();
        voltages.add(230.0);
        voltages.add(231.0);

        StateTransformer.expandPhases(state, voltages, "charge", "Voltage");

        assertEquals(230.0, state.get("chargeVoltageL1").getAsDouble());
        assertEquals(231.0, state.get("chargeVoltageL2").getAsDouble());
        assertFalse(state.has("chargeVoltageL3"));
    }

    @Test
    void expandPhasesWithEmptyArrayAddsNoChannels() {
        JsonObject state = new JsonObject();

        StateTransformer.expandPhases(state, new JsonArray(), "grid", "Power");

        assertEquals(0, state.size());
    }
}
