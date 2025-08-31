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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_CURRENCY;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_DIMENSIONLESS;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_ELECTRIC_CURRENT;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_EMISSION_INTENSITY;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_ENERGY;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_ENERGY_PRICE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_LENGTH;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_POWER;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_TEMPERATURE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.NUMBER_TIME;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.evcc.internal.handler.EvccBaseThingHandler.ItemTypeUnit;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link EvccBaseThingHandlerTest} is responsible for testing the BaseThingHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccBaseThingHandlerTest {

    @SuppressWarnings("null")
    private Thing thing = mock(Thing.class);

    @SuppressWarnings("null")
    private final ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);

    private BaseThingHandlerTestClass handler = new BaseThingHandlerTestClass(thing, channelTypeRegistry);

    @SuppressWarnings("null")
    @BeforeEach
    public void setUp() {
        thing = mock(Thing.class);
        handler = spy(new BaseThingHandlerTestClass(thing, channelTypeRegistry));
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "battery"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
    }

    @Nested
    class UpdateStatesFromApiResponseTests {
        @Test
        public void updateFromEvccStateNotInitializedDoesNothing() {
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
        public void updateFromEvccStateEmptyStateDoesNothing() {
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
            assertTrue(handler.setItemValueCalled);
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
            assertTrue(handler.setItemValueCalled);
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
            assertFalse(handler.setItemValueCalled);
            assertFalse(handler.updateThingCalled);
            assertTrue(handler.updateStatusCalled); // Status is updated even if nothing else happens
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

            assertTrue(handler.setItemValueCalled);
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

            assertTrue(handler.setItemValueCalled);
        }

        @SuppressWarnings("null")
        @Test
        public void handleCommandWithRefreshTypeAndMissingValue() {
            ChannelUID channelUID = new ChannelUID("test:thing:uid:battery-capacity");
            RefreshType command = RefreshType.REFRESH;

            JsonObject cachedState = new JsonObject(); // no "capacity" key
            doReturn(cachedState).when(handler).getStateFromCachedState(any());

            handler.handleCommand(channelUID, command);

            assertFalse(handler.setItemValueCalled);
            assertFalse(handler.logUnknownChannelXmlCalled);
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
            when(thing.getProperties()).thenReturn(Map.of("type", "battery"));

            String key = "soc";
            String result = handler.getThingKey(key);

            assertEquals("battery-soc", result);
        }

        @Test
        public void getThingKeyWithHeatingType() {
            when(thing.getProperties()).thenReturn(Map.of("type", "heating"));

            String key = "capacity";
            String result = handler.getThingKey(key);

            assertEquals("loadpoint-capacity", result);
        }

        @Test
        public void getThingKeyWithDefaultType() {
            when(thing.getProperties()).thenReturn(Map.of("type", "loadpoint"));

            String key = "someKey";
            String result = handler.getThingKey(key);

            assertEquals("loadpoint-some-key", result);
        }
    }

    @Nested
    class SetItemValueTests {

        static Stream<Arguments> provideItemTypesWithExpectedStateClass() {
            return Stream.of(Arguments.of(NUMBER_DIMENSIONLESS, QuantityType.class),
                    Arguments.of(NUMBER_ELECTRIC_CURRENT, QuantityType.class),
                    Arguments.of(NUMBER_EMISSION_INTENSITY, QuantityType.class),
                    Arguments.of(NUMBER_ENERGY, QuantityType.class), Arguments.of(NUMBER_LENGTH, QuantityType.class),
                    Arguments.of(NUMBER_POWER, QuantityType.class), Arguments.of(NUMBER_TIME, QuantityType.class),
                    Arguments.of(NUMBER_TEMPERATURE, QuantityType.class),
                    Arguments.of(CoreItemFactory.NUMBER, DecimalType.class),
                    Arguments.of(NUMBER_CURRENCY, DecimalType.class),
                    Arguments.of(NUMBER_ENERGY_PRICE, DecimalType.class),
                    Arguments.of(CoreItemFactory.STRING, StringType.class),
                    Arguments.of(CoreItemFactory.SWITCH, OnOffType.class));
        }

        @SuppressWarnings("null")
        @ParameterizedTest
        @MethodSource("provideItemTypesWithExpectedStateClass")
        void setItemValueWithVariousTypes(String itemType, Class<?> expectedStateClass) {
            ChannelUID channelUID = new ChannelUID("test:thing:uid:dummy");
            JsonElement value = itemType.equals(CoreItemFactory.STRING) ? new JsonPrimitive("OK")
                    : itemType.equals(CoreItemFactory.SWITCH) ? new JsonPrimitive(true) : new JsonPrimitive(12.5);

            ChannelType mockChannelType = mock(ChannelType.class);
            when(mockChannelType.getItemType()).thenReturn(itemType);
            if (NUMBER_TEMPERATURE.equals(itemType)) {
                when(mockChannelType.getUnitHint()).thenReturn("°C");
            }
            when(channelTypeRegistry.getChannelType(any())).thenReturn(mockChannelType);

            ItemTypeUnit itemTypeUnit = new ItemTypeUnit(mockChannelType, Units.ONE);

            handler.setItemValue(itemTypeUnit, channelUID, value);

            assertTrue(handler.updateStateCalled);
            assertEquals(channelUID, handler.lastChannelUID);
            assertEquals(expectedStateClass, handler.lastState.getClass());
        }
    }
}
