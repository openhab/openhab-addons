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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_ENERGY;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link EvccBaseThingHandlerTest} is responsible for testing the EvccBaseThingHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccBaseThingHandlerTest {

    @SuppressWarnings("null")
    private final Thing thing = mock(Thing.class);

    @SuppressWarnings("null")
    private final ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);

    private BaseThingHandlerTestClass handler = new BaseThingHandlerTestClass(thing, channelTypeRegistry);

    @SuppressWarnings("null")
    @BeforeEach
    public void setUp() {
        handler = spy(new BaseThingHandlerTestClass(thing, channelTypeRegistry));
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "battery"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("index")).thenReturn("0");
        when(configuration.get("id")).thenReturn("vehicle_1");
        when(thing.getConfiguration()).thenReturn(configuration);
    }

    @Nested
    class UpdateStatesFromApiResponseTests {
        @Test
        public void updateFromEvccStateNotInitializedDoesNothing() {
            handler.isInitialized = false;
            JsonObject state = new JsonObject();
            handler.updateStatesFromApiResponse(state);
            assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
            assertFalse(handler.createChannelCalled);
            assertFalse(handler.updateThingCalled);
            assertFalse(handler.updateStatusCalled);
            assertEquals(ThingStatus.UNKNOWN, handler.lastUpdatedStatus);
        }

        @Test
        public void updateFromEvccStateEmptyStateDoesNothing() {
            handler.isInitialized = true;
            JsonObject state = new JsonObject();
            handler.updateStatesFromApiResponse(state);
            assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
            assertFalse(handler.createChannelCalled);
            assertFalse(handler.updateThingCalled);
            // Status should not be updated for empty state
            assertFalse(handler.updateStatusCalled);
            assertEquals(ThingStatus.UNKNOWN, handler.lastUpdatedStatus);
        }

        @SuppressWarnings("null")
        @Test
        public void updateFromEvccStateWithPrimitiveValueCreatesChannelAndSetsItemValue() {
            handler.isInitialized = true;
            JsonObject state = new JsonObject();
            state.add("capacity", new JsonPrimitive(5.5));
            // Channel does not exist
            when(thing.getChannel(anyString())).thenReturn(null);
            ChannelType mockChannelType = mock(ChannelType.class);
            when(mockChannelType.getItemType()).thenReturn(NUMBER_ENERGY);
            when(channelTypeRegistry.getChannelType(any())).thenReturn(mockChannelType);

            handler.updateStatesFromApiResponse(state);
            assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
            assertTrue(handler.createChannelCalled);
            assertTrue(handler.updateThingCalled);
            assertTrue(handler.updateStatusCalled);
            assertEquals(ThingStatus.ONLINE, handler.lastUpdatedStatus);
        }

        @Test
        public void updateFromEvccStateWithExistingChannelDoesNotCreateChannel() {
            handler.isInitialized = true;
            JsonObject state = new JsonObject();
            state.add("capacity", new JsonPrimitive(5.5));
            @SuppressWarnings("null")
            Channel mockChannel = mock(Channel.class);
            when(thing.getChannel(anyString())).thenReturn(mockChannel);

            handler.updateStatesFromApiResponse(state);

            assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
            assertFalse(handler.createChannelCalled);
            assertFalse(handler.updateThingCalled); // Should not update thing if channel exists
            assertTrue(handler.updateStatusCalled);
            assertEquals(ThingStatus.ONLINE, handler.lastUpdatedStatus);
        }

        @Test
        public void updateFromEvccStateSkipsNonPrimitiveValues() {
            handler.isInitialized = true;
            JsonObject state = new JsonObject();
            JsonObject nonPrimitive = new JsonObject();
            nonPrimitive.addProperty("foo", "bar");
            state.add("complexKey", nonPrimitive);

            handler.updateStatesFromApiResponse(state);

            assertTrue(handler.prepareApiResponseForChannelStateUpdateCalled);
            assertFalse(handler.createChannelCalled);
            assertFalse(handler.updateThingCalled);
            assertTrue(handler.updateStatusCalled); // Status is updated even if nothing else happens
            assertEquals(ThingStatus.ONLINE, handler.lastUpdatedStatus);
        }

        @Test
        void updateStatesFromApiResponseWithNullValueDoesNothing() {
            handler.isInitialized = true;
            JsonObject state = new JsonObject();
            state.add("capacity", null); // Null value
            handler.updateStatesFromApiResponse(state);
            assertFalse(handler.createChannelCalled);
            assertFalse(handler.updateThingCalled);
            assertTrue(handler.updateStatusCalled);
            assertEquals(ThingStatus.ONLINE, handler.lastUpdatedStatus);
        }
    }

    @Nested
    class HandleCommandTests {
        @SuppressWarnings("null")
        @Test
        public void handleCommandWithNumberItemType() {
            ChannelUID channelUID = new ChannelUID("test:thing:uid:battery-capacity");
            RefreshType command = RefreshType.REFRESH;

            JsonObject cachedState = new JsonObject();
            cachedState.add("capacity", new JsonPrimitive(42.0));

            ChannelType mockChannelType = mock(ChannelType.class);
            when(mockChannelType.getItemType()).thenReturn(NUMBER_ENERGY);
            when(channelTypeRegistry.getChannelType(any())).thenReturn(mockChannelType);

            doReturn(cachedState).when(handler).getStateFromCachedState(any());
            handler.bridgeHandler = mock(EvccBridgeHandler.class);

            handler.handleCommand(channelUID, command);
        }

        @SuppressWarnings("null")
        @Test
        public void handleCommandWithRefreshTypeAndValidValue() {
            ChannelUID channelUID = new ChannelUID("test:thing:uid:battery-capacity");
            RefreshType command = RefreshType.REFRESH;

            JsonObject cachedState = new JsonObject();
            cachedState.add("capacity", new JsonPrimitive(42.0));

            ChannelType mockChannelType = mock(ChannelType.class);
            when(mockChannelType.getItemType()).thenReturn(NUMBER_ENERGY);
            when(channelTypeRegistry.getChannelType(any())).thenReturn(mockChannelType);

            doReturn(cachedState).when(handler).getStateFromCachedState(any());
            handler.bridgeHandler = mock(EvccBridgeHandler.class);

            handler.handleCommand(channelUID, command);
        }

        @SuppressWarnings("null")
        @Test
        public void handleCommandWithRefreshTypeAndMissingValue() {
            ChannelUID channelUID = new ChannelUID("test:thing:uid:battery-capacity");
            RefreshType command = RefreshType.REFRESH;

            JsonObject cachedState = new JsonObject(); // no "capacity" key
            doReturn(cachedState).when(handler).getStateFromCachedState(any());

            handler.handleCommand(channelUID, command);
            assertFalse(handler.logUnknownChannelXmlCalled);
        }

        @SuppressWarnings("null")
        @Test
        void handleCommandWithNonRefreshTypeDoesNothing() {
            ChannelUID channelUID = new ChannelUID("test:thing:uid:battery-capacity");
            Command command = mock(org.openhab.core.types.Command.class);
            handler.handleCommand(channelUID, command);
        }
    }

    @Test
    public void testCreateChannelWithUnknownItemType() {
        JsonElement value = new JsonPrimitive(5.5);
        @SuppressWarnings("null")
        ThingBuilder builder = mock(ThingBuilder.class);

        @SuppressWarnings("null")
        ChannelType mockChannelType = mock(ChannelType.class);
        when(mockChannelType.getItemType()).thenReturn("Unknown");
        when(channelTypeRegistry.getChannelType(any())).thenReturn(mockChannelType);

        handler.createChannel("capacity", value);

        assertTrue(handler.createChannelCalled);
        verify(builder, never()).withChannel(any());
    }

    @Nested
    class GetThingKeyTests {
        @Test
        public void getThingKeyWithBatteryTypeAndSpecialKey() {
            handler.type = "battery";

            String key = "soc";
            String result = handler.getThingKey(key);

            assertEquals("battery-soc", result);
        }

        @Test
        public void getThingKeyWithHeatingType() {
            handler.type = "loadpoint";

            String key = "capacity";
            String result = handler.getThingKey(key);

            assertEquals("loadpoint-capacity", result);
        }

        @Test
        public void getThingKeyWithDefaultType() {
            handler.type = "loadpoint";

            String key = "someKey";
            String result = handler.getThingKey(key);

            assertEquals("loadpoint-some-key", result);
        }
    }

    @SuppressWarnings("null")
    @Test
    void logUnknownChannelXmlAsyncIsCalledAsynchronously() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        doAnswer(invocation -> {
            future.complete(true);
            return null;
        }).when(handler).logUnknownChannelXml(anyString(), anyString());

        handler.logUnknownChannelXmlAsync("testKey", "testType");

        try {
            future.get(2, SECONDS);
        } catch (Exception e) {
            fail("logUnknownChannelXml was not called asynchronously");
        }
    }
}
