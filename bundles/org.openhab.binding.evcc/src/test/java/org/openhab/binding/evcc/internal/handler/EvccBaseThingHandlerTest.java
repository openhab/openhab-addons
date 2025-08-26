/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.*;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link EvccBaseThingHandlerTest} is responsible for testing the BaseThingHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
public class EvccBaseThingHandlerTest {

    private Thing thing;
    private TestEvccBaseThingHandler handler;

    // Concrete subclass for testing
    private static class TestEvccBaseThingHandler extends EvccBaseThingHandler {
        public boolean setItemValueCalled = false;
        public boolean createChannelCalled = false;
        public boolean updateThingCalled = false;
        public boolean updateStatusCalled = false;
        public boolean prepareApiResponseForChannelStateUpdateCalled = true;
        public ThingStatus lastUpdatedStatus = ThingStatus.UNKNOWN;

        public TestEvccBaseThingHandler(Thing thing, ChannelTypeRegistry registry) {
            super(thing, registry);
        }

        @Override
        protected void updateThing(@NonNull Thing thing) {
            updateThingCalled = true;
        }

        @Override
        protected void updateStatus(@NonNull ThingStatus status) {
            lastUpdatedStatus = status;
            updateStatusCalled = true;
        }

        @Override
        protected void createChannel(@NonNull String thingKey, @NonNull ThingBuilder builder,
                @NonNull JsonElement value) {
            createChannelCalled = true;
        }

        @Override
        protected void setItemValue(@NonNull ItemTypeUnit itemTypeUnit, @NonNull ChannelUID channelUID,
                @NonNull JsonElement value) {
            setItemValueCalled = true;
        }

        @Override
        public void prepareApiResponseForChannelStateUpdate(@NonNull JsonObject state) {
            prepareApiResponseForChannelStateUpdateCalled = true;
            super.updateStatesFromApiResponse(state);
        }

        @NonNull
        @Override
        public JsonObject getStateFromCachedState(@NonNull JsonObject state) {
            return new JsonObject();
        }
    }

    @BeforeEach
    public void setUp() {
        thing = mock(Thing.class);
        ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);
        handler = spy(new TestEvccBaseThingHandler(thing, channelTypeRegistry));
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "battery"));
        when(thing.getChannels()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testUpdateFromEvccStateNotInitializedDoesNothing() {
        handler.isInitialized = false;
        JsonObject state = new JsonObject();
        handler.updateStatesFromApiResponse(state);
        assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
        assertFalse(handler.setItemValueCalled);
        assertFalse(handler.createChannelCalled);
        assertFalse(handler.updateThingCalled);
        assertFalse(handler.updateStatusCalled);
        assertEquals(ThingStatus.UNKNOWN, handler.lastUpdatedStatus);
    }

    @Test
    public void testUpdateFromEvccStateEmptyStateDoesNothing() {
        handler.isInitialized = true;
        JsonObject state = new JsonObject();
        handler.updateStatesFromApiResponse(state);
        assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
        assertFalse(handler.setItemValueCalled);
        assertFalse(handler.createChannelCalled);
        assertFalse(handler.updateThingCalled);
        // Status should not be updated for empty state
        assertFalse(handler.updateStatusCalled);
        assertEquals(ThingStatus.UNKNOWN, handler.lastUpdatedStatus);
    }

    @Test
    public void testUpdateFromEvccStateWithPrimitiveValueCreatesChannelAndSetsItemValue() {
        handler.isInitialized = true;
        JsonObject state = new JsonObject();
        state.add("capacity", new JsonPrimitive(5.5));
        // Channel does not exist
        when(thing.getChannel(anyString())).thenReturn(null);

        handler.updateStatesFromApiResponse(state);
        assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
        assertTrue(handler.createChannelCalled);
        assertTrue(handler.setItemValueCalled);
        assertTrue(handler.updateThingCalled);
        assertTrue(handler.updateStatusCalled);
        assertEquals(ThingStatus.ONLINE, handler.lastUpdatedStatus);
    }

    @Test
    public void testUpdateFromEvccStateWithExistingChannelDoesNotCreateChannel() {
        handler.isInitialized = true;
        JsonObject state = new JsonObject();
        state.add("capacity", new JsonPrimitive(5.5));
        Channel mockChannel = mock(Channel.class);
        when(thing.getChannel(anyString())).thenReturn(mockChannel);

        handler.updateStatesFromApiResponse(state);

        assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
        assertFalse(handler.createChannelCalled);
        assertTrue(handler.setItemValueCalled);
        assertFalse(handler.updateThingCalled); // Should not update thing if channel exists
        assertTrue(handler.updateStatusCalled);
        assertEquals(ThingStatus.ONLINE, handler.lastUpdatedStatus);
    }

    @Test
    public void testUpdateFromEvccStateSkipsNonPrimitiveValues() {
        handler.isInitialized = true;
        JsonObject state = new JsonObject();
        JsonObject nonPrimitive = new JsonObject();
        nonPrimitive.addProperty("foo", "bar");
        state.add("complexKey", nonPrimitive);

        handler.updateStatesFromApiResponse(state);

        assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
        assertFalse(handler.createChannelCalled);
        assertFalse(handler.setItemValueCalled);
        assertFalse(handler.updateThingCalled);
        assertTrue(handler.updateStatusCalled); // Status is updated even if nothing else happens
        assertEquals(ThingStatus.ONLINE, handler.lastUpdatedStatus);
    }
}
