/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.deconz.internal.dto.LightMessage;
import org.openhab.binding.deconz.internal.handler.LightThingHandler;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.binding.deconz.internal.types.ThermostatModeGsonTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class provides tests for deconz lights
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class LightsTest {
    private @NonNullByDefault({}) Gson gson;

    @Mock
    private @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;

    @Before
    public void initialize() {
        initMocks(this);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ThermostatMode.class, new ThermostatModeGsonTypeAdapter());
        gson = gsonBuilder.create();
    }

    @Test
    public void colorTemperatureLightUpdateTest() throws IOException {
        LightMessage lightMessage = DeconzTest.getObjectFromJson("colortemperature.json", LightMessage.class, gson);
        Assert.assertNotNull(lightMessage);

        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUID_bri = new ChannelUID(thingUID, CHANNEL_BRIGHTNESS);
        ChannelUID channelUID_ct = new ChannelUID(thingUID, CHANNEL_COLOR_TEMPERATURE);

        Thing light = ThingBuilder.create(THING_TYPE_COLOR_TEMPERATURE_LIGHT, thingUID)
                .withChannel(ChannelBuilder.create(channelUID_bri, "Dimmer").build())
                .withChannel(ChannelBuilder.create(channelUID_ct, "Number").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson);
        lightThingHandler.setCallback(thingHandlerCallback);

        lightThingHandler.messageReceived("", lightMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUID_bri), eq(new PercentType("21")));
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUID_ct), eq(new DecimalType("87.03170028818444")));
    }

    @Test
    public void dimmableLightUpdateTest() throws IOException {
        LightMessage lightMessage = DeconzTest.getObjectFromJson("dimmable.json", LightMessage.class, gson);
        Assert.assertNotNull(lightMessage);

        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUID_bri = new ChannelUID(thingUID, CHANNEL_BRIGHTNESS);

        Thing light = ThingBuilder.create(THING_TYPE_DIMMABLE_LIGHT, thingUID)
                .withChannel(ChannelBuilder.create(channelUID_bri, "Dimmer").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson);
        lightThingHandler.setCallback(thingHandlerCallback);

        lightThingHandler.messageReceived("", lightMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUID_bri), eq(new PercentType("38")));
    }

    @Test
    public void windowCoveringUpdateTest() throws IOException {
        LightMessage lightMessage = DeconzTest.getObjectFromJson("windowcovering.json", LightMessage.class, gson);
        Assert.assertNotNull(lightMessage);

        ThingUID thingUID = new ThingUID("deconz", "light");
        ChannelUID channelUID_pos = new ChannelUID(thingUID, CHANNEL_POSITION);

        Thing light = ThingBuilder.create(THING_TYPE_WINDOW_COVERING, thingUID)
                .withChannel(ChannelBuilder.create(channelUID_pos, "Rollershutter").build()).build();
        LightThingHandler lightThingHandler = new LightThingHandler(light, gson);
        lightThingHandler.setCallback(thingHandlerCallback);

        lightThingHandler.messageReceived("", lightMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUID_pos), eq(new PercentType("41")));
    }
}
