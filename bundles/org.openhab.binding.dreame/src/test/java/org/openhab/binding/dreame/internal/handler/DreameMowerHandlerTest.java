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
package org.openhab.binding.dreame.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dreame.internal.DreameBindingConstants.CHANNEL_BATTERY_LEVEL;
import static org.openhab.binding.dreame.internal.DreameBindingConstants.CHANNEL_DND;
import static org.openhab.binding.dreame.internal.DreameBindingConstants.CHANNEL_DND_ACTIVE;
import static org.openhab.binding.dreame.internal.DreameBindingConstants.CHANNEL_ERROR_CODE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Tests mower handler update ordering.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameMowerHandlerTest {

    @Test
    void parsesAndDeduplicatesZoneIds() {
        assertEquals(java.util.List.of(1, 2), DreameMowerHandler.parseZoneIds("1, 2,1"));
        assertThrows(IllegalArgumentException.class, () -> DreameMowerHandler.parseZoneIds("0"));
        assertThrows(NumberFormatException.class, () -> DreameMowerHandler.parseZoneIds("garden"));
    }

    @Test
    void rejectsOnlyRestPropertiesSupersededByMqtt() {
        assertTrue(DreameMowerHandler.isPollPropertyCurrent(10, null));
        assertTrue(DreameMowerHandler.isPollPropertyCurrent(10, 10L));
        assertFalse(DreameMowerHandler.isPollPropertyCurrent(10, 11L));
    }

    @Test
    void identifiesMova1000MechanicalCuttingHeight() {
        assertFalse(DreameMowerHandler.supportsElectronicCuttingHeight("mova.mower.g2405c"));
        assertTrue(DreameMowerHandler.supportsElectronicCuttingHeight("dreame.mower.g2540d"));
    }

    @Test
    void partialPropertyResponseKeepsPreviouslyReportedStates() {
        TestMowerHandler handler = new TestMowerHandler();
        DreameStatus initial = new DreameStatus();
        initial.put(DreameProperty.ERROR, new JsonPrimitive(48));
        initial.put(DreameProperty.DND, new JsonPrimitive(true));
        handler.updateStatusChannels(initial, null);

        DreameStatus partial = new DreameStatus();
        partial.put(DreameProperty.BATTERY_LEVEL, new JsonPrimitive(91));
        handler.updateStatusChannels(partial, null);

        assertEquals(new StringType("48"), handler.states.get(CHANNEL_ERROR_CODE));
        assertEquals(OnOffType.ON, handler.states.get(CHANNEL_DND));
        assertEquals(new DecimalType(91), handler.states.get(CHANNEL_BATTERY_LEVEL));
    }

    @Test
    void readsDndStateFromTaskConfiguration() {
        assertEquals(true, DreameMowerHandler
                .dndTaskEnabled("[{\"id\":1,\"en\":true,\"st\":\"22:00\",\"et\":\"08:00\",\"wk\":127,\"ss\":0}]"));
        assertEquals(false, DreameMowerHandler.dndTaskEnabled("[{\"id\":1,\"en\":false}]"));
        assertEquals(null, DreameMowerHandler.dndTaskEnabled("[]"));
        assertEquals(null, DreameMowerHandler.dndTaskEnabled("invalid"));
    }

    @Test
    void readsMovaDndStatusAndIgnoresLegacyPropertyAfterwards() {
        TestMowerHandler handler = new TestMowerHandler();
        DreameStatus movaStatus = new DreameStatus();
        movaStatus.put(DreameProperty.DND_STATUS, JsonParser.parseString("{\"start\":1320,\"end\":480,\"value\":1}"));
        handler.updateStatusChannels(movaStatus, null);
        assertEquals(OnOffType.ON, handler.states.get(CHANNEL_DND));

        DreameStatus legacyStatus = new DreameStatus();
        legacyStatus.put(DreameProperty.DND, new JsonPrimitive(false));
        handler.updateStatusChannels(legacyStatus, null);
        assertEquals(OnOffType.ON, handler.states.get(CHANNEL_DND));

        movaStatus.put(DreameProperty.DND_STATUS, JsonParser.parseString("{\"start\":1320,\"end\":480,\"value\":0}"));
        handler.updateStatusChannels(movaStatus, null);
        assertEquals(OnOffType.OFF, handler.states.get(CHANNEL_DND));
        assertEquals(OnOffType.OFF, handler.states.get(CHANNEL_DND_ACTIVE));
    }

    @Test
    void rejectsMalformedMovaDndStateAndDisablesUnverifiedWrites() {
        assertEquals(null, DreameMowerHandler.dndStatusEnabled(JsonParser.parseString("{\"value\":2}")));
        assertEquals(null, DreameMowerHandler.dndStatusEnabled(JsonParser.parseString("{\"value\":1.5}")));
        assertEquals(null, DreameMowerHandler.dndStatusEnabled(JsonParser.parseString("{\"value\":true}")));
        assertEquals(null, DreameMowerHandler.dndStatusEnabled(JsonParser.parseString("[]")));
        assertFalse(DreameMowerHandler.supportsDndWriting("mova.mower.g2584d"));
        assertTrue(DreameMowerHandler.supportsDndWriting("dreame.mower.g2540d"));
    }

    @Test
    void derivesEffectiveDndStateAcrossMidnight() {
        DreameMowerHandler.DndSchedule overnight = new DreameMowerHandler.DndSchedule(true, 1320, 480);
        assertTrue(DreameMowerHandler.isDndActive(overnight, 1320));
        assertTrue(DreameMowerHandler.isDndActive(overnight, 1439));
        assertTrue(DreameMowerHandler.isDndActive(overnight, 0));
        assertTrue(DreameMowerHandler.isDndActive(overnight, 479));
        assertFalse(DreameMowerHandler.isDndActive(overnight, 480));
        assertFalse(DreameMowerHandler.isDndActive(overnight, 900));
        assertFalse(DreameMowerHandler.isDndActive(new DreameMowerHandler.DndSchedule(false, 1320, 480), 60));
    }

    @Test
    void parsesAndValidatesMovaDndSchedule() {
        assertEquals(new DreameMowerHandler.DndSchedule(true, 1320, 480), DreameMowerHandler
                .dndStatusSchedule(JsonParser.parseString("{\"start\":1320,\"end\":480,\"value\":1}")));
        assertEquals(null, DreameMowerHandler
                .dndStatusSchedule(JsonParser.parseString("{\"start\":1440,\"end\":480,\"value\":1}")));
        assertEquals(null, DreameMowerHandler.dndStatusSchedule(JsonParser.parseString("{\"end\":480,\"value\":1}")));
    }

    private static class TestMowerHandler extends DreameMowerHandler {
        private final Map<String, State> states = new HashMap<>();

        TestMowerHandler() {
            super(Objects.requireNonNull(mock(Thing.class)));
        }

        @Override
        protected void updateState(String channel, State state) {
            states.put(channel, state);
        }
    }
}
