/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.mockito.ArgumentMatchers.eq;
import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.handler.SensorThingHandler;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class provides tests for deconz sensors
 *
 * @author Jan N. Klug - Initial contribution
 * @author Lukas Agethen - Added Thermostat
 * @author Philipp Schneider - Added air quality sensor
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class SensorsTest {
    private @NonNullByDefault({}) Gson gson;

    private @Mock @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;

    @BeforeEach
    public void initialize() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gson = gsonBuilder.create();
    }

    @Test
    public void carbonmonoxideSensorUpdateTest() throws IOException {
        SensorMessage sensorMessage = DeconzTest.getObjectFromJson("carbonmonoxide.json", SensorMessage.class, gson);
        assertNotNull(sensorMessage);

        ThingUID thingUID = new ThingUID("deconz", "sensor");
        ChannelUID channelUID = new ChannelUID(thingUID, "carbonmonoxide");
        Thing sensor = ThingBuilder.create(THING_TYPE_CARBONMONOXIDE_SENSOR, thingUID)
                .withChannel(ChannelBuilder.create(channelUID, "Switch").build()).build();
        SensorThingHandler sensorThingHandler = new SensorThingHandler(sensor, gson);
        sensorThingHandler.setCallback(thingHandlerCallback);

        sensorThingHandler.messageReceived(sensorMessage);
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUID), eq(OnOffType.ON));
    }

    @Test
    public void airQualitySensorUpdateTest() throws IOException {
        // ARRANGE
        SensorMessage sensorMessage = DeconzTest.getObjectFromJson("airquality.json", SensorMessage.class, gson);
        assertNotNull(sensorMessage);

        ThingUID thingUID = new ThingUID("deconz", "sensor");
        ChannelUID channelUID = new ChannelUID(thingUID, "airquality");
        Thing sensor = ThingBuilder.create(THING_TYPE_AIRQUALITY_SENSOR, thingUID)
                .withChannel(ChannelBuilder.create(channelUID, "String").build()).build();
        SensorThingHandler sensorThingHandler = new SensorThingHandler(sensor, gson);
        sensorThingHandler.setCallback(thingHandlerCallback);

        // ACT
        sensorThingHandler.messageReceived(sensorMessage);

        // ASSERT
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUID), eq(StringType.valueOf("good")));
    }

    @Test
    public void airQualityPpbSensorUpdateTest() throws IOException {
        // ARRANGE
        SensorMessage sensorMessage = DeconzTest.getObjectFromJson("airquality.json", SensorMessage.class, gson);
        assertNotNull(sensorMessage);

        ThingUID thingUID = new ThingUID("deconz", "sensor");
        ChannelUID channelUID = new ChannelUID(thingUID, "airqualityppb");
        Thing sensor = ThingBuilder.create(THING_TYPE_AIRQUALITY_SENSOR, thingUID)
                .withChannel(ChannelBuilder.create(channelUID, "Number").build()).build();
        SensorThingHandler sensorThingHandler = new SensorThingHandler(sensor, gson);
        sensorThingHandler.setCallback(thingHandlerCallback);

        // ACT
        sensorThingHandler.messageReceived(sensorMessage);

        // ASSERT
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelUID), eq(new QuantityType<>("129 ppb")));
    }

    @Test
    public void fireSensorUpdateTest() throws IOException {
        SensorMessage sensorMessage = DeconzTest.getObjectFromJson("fire.json", SensorMessage.class, gson);
        assertNotNull(sensorMessage);

        ThingUID thingUID = new ThingUID("deconz", "sensor");
        ChannelUID channelBatteryLevelUID = new ChannelUID(thingUID, CHANNEL_BATTERY_LEVEL);
        ChannelUID channelFireUID = new ChannelUID(thingUID, CHANNEL_FIRE);
        ChannelUID channelTamperedUID = new ChannelUID(thingUID, CHANNEL_TAMPERED);
        ChannelUID channelLastSeenUID = new ChannelUID(thingUID, CHANNEL_LAST_SEEN);

        Thing sensor = ThingBuilder.create(THING_TYPE_FIRE_SENSOR, thingUID)
                .withChannel(ChannelBuilder.create(channelBatteryLevelUID, "Number").build())
                .withChannel(ChannelBuilder.create(channelFireUID, "Switch").build())
                .withChannel(ChannelBuilder.create(channelTamperedUID, "Switch").build())
                .withChannel(ChannelBuilder.create(channelLastSeenUID, "DateTime").build()).build();
        SensorThingHandler sensorThingHandler = new SensorThingHandler(sensor, gson);
        sensorThingHandler.setCallback(thingHandlerCallback);

        sensorThingHandler.messageReceived(sensorMessage);

        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelFireUID), eq(OnOffType.OFF));
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(channelBatteryLevelUID), eq(new DecimalType(98)));
    }
}
