/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.deconz;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.openhab.binding.deconz.internal.BindingConstants.*;
import static org.openhab.core.thing.internal.ThingManagerImpl.PROPERTY_THING_TYPE_VERSION;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.deconz.internal.DeconzDynamicCommandDescriptionProvider;
import org.openhab.binding.deconz.internal.DeconzDynamicStateDescriptionProvider;
import org.openhab.binding.deconz.internal.dto.LightMessage;
import org.openhab.binding.deconz.internal.handler.LightThingHandler;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.binding.deconz.internal.types.ThermostatModeGsonTypeAdapter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class provides tests for deconz lights
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class LightsTest {
    private @NonNullByDefault({}) Gson gson;

    private @Mock @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;
    private @Mock @NonNullByDefault({}) DeconzDynamicStateDescriptionProvider stateDescriptionProvider;
    private @Mock @NonNullByDefault({}) DeconzDynamicCommandDescriptionProvider commandDescriptionProvider;

    @BeforeEach
    public void initialize() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ThermostatMode.class, new ThermostatModeGsonTypeAdapter());
        gson = gsonBuilder.create();
    }

    @Test
    public void colorTemperatureLightUpdateTest() throws IOException {
        LightMessage lightMessage = DeconzTest.getObjectFromJson("colortemperature.json", LightMessage.class, gson);
        assertNotNull(lightMessage);

        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUIDBri = new ChannelUID(thingUID, CHANNEL_BRIGHTNESS);
        ChannelUID channelUIDCt = new ChannelUID(thingUID, CHANNEL_COLOR_TEMPERATURE);

        Thing light = ThingBuilder.create(THING_TYPE_COLOR_TEMPERATURE_LIGHT, thingUID)
                .withProperties(Map.of(PROPERTY_THING_TYPE_VERSION, "1"))
                .withChannel(ChannelBuilder.create(channelUIDBri, "Dimmer").build())
                .withChannel(ChannelBuilder.create(channelUIDCt, "Number").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson, stateDescriptionProvider,
                commandDescriptionProvider);
        lightThingHandler.setCallback(thingHandlerCallback);

        lightThingHandler.messageReceived(lightMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUIDBri), eq(new PercentType("21")));
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUIDCt), eq(new DecimalType("2500")));
    }

    @Test
    public void colorTemperatureLightStateDescriptionProviderTest() {
        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUIDBri = new ChannelUID(thingUID, CHANNEL_BRIGHTNESS);
        ChannelUID channelUIDCt = new ChannelUID(thingUID, CHANNEL_COLOR_TEMPERATURE);

        Map<String, String> properties = new HashMap<>();
        properties.put(PROPERTY_CT_MAX, "500");
        properties.put(PROPERTY_CT_MIN, "200");
        properties.put(PROPERTY_THING_TYPE_VERSION, "1");

        Thing light = ThingBuilder.create(THING_TYPE_COLOR_TEMPERATURE_LIGHT, thingUID).withProperties(properties)
                .withChannel(ChannelBuilder.create(channelUIDBri, "Dimmer").build())
                .withChannel(ChannelBuilder.create(channelUIDCt, "Number").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson, stateDescriptionProvider,
                commandDescriptionProvider) {
            // avoid warning when initializing
            @Override
            public @Nullable Bridge getBridge() {
                return null;
            }
        };

        lightThingHandler.initialize();

        Mockito.verify(stateDescriptionProvider).setDescriptionFragment(eq(channelUIDCt), any());
    }

    @Test
    public void dimmableLightUpdateTest() throws IOException {
        LightMessage lightMessage = DeconzTest.getObjectFromJson("dimmable.json", LightMessage.class, gson);
        assertNotNull(lightMessage);

        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUIDBri = new ChannelUID(thingUID, CHANNEL_BRIGHTNESS);

        Thing light = ThingBuilder.create(THING_TYPE_DIMMABLE_LIGHT, thingUID)
                .withProperties(Map.of(PROPERTY_THING_TYPE_VERSION, "1"))
                .withChannel(ChannelBuilder.create(channelUIDBri, "Dimmer").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson, stateDescriptionProvider,
                commandDescriptionProvider);
        lightThingHandler.setCallback(thingHandlerCallback);

        lightThingHandler.messageReceived(lightMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUIDBri), eq(new PercentType("38")));
    }

    @Test
    public void dimmableLightOverrangeUpdateTest() throws IOException {
        LightMessage lightMessage = DeconzTest.getObjectFromJson("dimmable_overrange.json", LightMessage.class, gson);
        assertNotNull(lightMessage);

        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUIDBri = new ChannelUID(thingUID, CHANNEL_BRIGHTNESS);

        Thing light = ThingBuilder.create(THING_TYPE_DIMMABLE_LIGHT, thingUID)
                .withProperties(Map.of(PROPERTY_THING_TYPE_VERSION, "1"))
                .withChannel(ChannelBuilder.create(channelUIDBri, "Dimmer").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson, stateDescriptionProvider,
                commandDescriptionProvider);
        lightThingHandler.setCallback(thingHandlerCallback);

        lightThingHandler.messageReceived(lightMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUIDBri), eq(new PercentType("100")));
    }

    @Test
    public void dimmableLightUnderrangeUpdateTest() throws IOException {
        LightMessage lightMessage = DeconzTest.getObjectFromJson("dimmable_underrange.json", LightMessage.class, gson);
        assertNotNull(lightMessage);

        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUIDBri = new ChannelUID(thingUID, CHANNEL_BRIGHTNESS);

        Thing light = ThingBuilder.create(THING_TYPE_DIMMABLE_LIGHT, thingUID)
                .withProperties(Map.of(PROPERTY_THING_TYPE_VERSION, "1"))
                .withChannel(ChannelBuilder.create(channelUIDBri, "Dimmer").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson, stateDescriptionProvider,
                commandDescriptionProvider);
        lightThingHandler.setCallback(thingHandlerCallback);

        lightThingHandler.messageReceived(lightMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUIDBri), eq(new PercentType("0")));
    }

    @Test
    public void windowCoveringUpdateTest() throws IOException {
        LightMessage lightMessage = DeconzTest.getObjectFromJson("windowcovering.json", LightMessage.class, gson);
        assertNotNull(lightMessage);

        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUIDPos = new ChannelUID(thingUID, CHANNEL_POSITION);

        Thing light = ThingBuilder.create(THING_TYPE_WINDOW_COVERING, thingUID)
                .withProperties(Map.of(PROPERTY_THING_TYPE_VERSION, "1"))
                .withChannel(ChannelBuilder.create(channelUIDPos, "Rollershutter").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson, stateDescriptionProvider,
                commandDescriptionProvider);
        lightThingHandler.setCallback(thingHandlerCallback);

        lightThingHandler.messageReceived(lightMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUIDPos), eq(new PercentType("41")));
    }
}
