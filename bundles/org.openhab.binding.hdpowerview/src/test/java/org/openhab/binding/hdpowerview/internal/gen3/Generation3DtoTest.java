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
package org.openhab.binding.hdpowerview.internal.gen3;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewJUnitTests;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Scene;
import org.openhab.binding.hdpowerview.internal.dto.gen3.SceneEvent;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Shade;
import org.openhab.binding.hdpowerview.internal.dto.gen3.ShadeEvent;
import org.openhab.binding.hdpowerview.internal.dto.gen3.ShadePosition;
import org.openhab.binding.hdpowerview.internal.handler.ShadeThingHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Unit tests for Generation 3 DTO's.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Generation3DtoTest {

    private final Gson gson = new Gson();
    private final ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();

    private String loadJson(String filename) throws IOException {
        try (InputStream inputStream = HDPowerViewJUnitTests.class.getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("inputstream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * Test JSON scene event response.
     */
    @Test
    public void testSceneEventParsing() throws IOException {
        String json = loadJson("gen3/scene-event.json");
        SceneEvent sceneEvent = gson.fromJson(json, SceneEvent.class);
        assertNotNull(sceneEvent);
        Scene scene = sceneEvent.getScene();
        assertNotNull(scene);
        assertEquals("Open All Office Shades\n Open All Office Shades", scene.getName());
        assertEquals(234, scene.getId());
    }

    /**
     * Test JSON scenes response.
     */
    @Test
    public void testScenesParsing() throws IOException {
        String json = loadJson("gen3/scenes.json");
        List<Scene> sceneList = List.of(gson.fromJson(json, Scene[].class));
        assertNotNull(sceneList);
        assertEquals(1, sceneList.size());
        Scene scene = sceneList.get(0);
        assertEquals("Open All Office Shades\n ABC", scene.getName());
        assertEquals(234, scene.getId());
    }

    /**
     * Test JSON shade event response.
     */
    @Test
    public void testShadeEventParsing() throws IOException {
        String json = loadJson("gen3/shade-event.json");
        ShadeEvent shadeEvent = gson.fromJson(json, ShadeEvent.class);
        assertNotNull(shadeEvent);
        ShadePosition position = shadeEvent.getCurrentPositions();
        assertNotNull(position);
        assertEquals(PercentType.valueOf("99"), position.getState(CoordinateSystem.PRIMARY_POSITION));
        assertEquals(PercentType.valueOf("98"), position.getState(CoordinateSystem.SECONDARY_POSITION));
        assertEquals(PercentType.ZERO, position.getState(CoordinateSystem.VANE_TILT_POSITION));
    }

    /**
     * Test JSON shade position setting.
     */
    @Test
    public void testShadePositions() {
        ShadePosition pos;

        pos = new ShadePosition();
        pos.setPosition(CoordinateSystem.PRIMARY_POSITION, new PercentType(11));
        assertEquals(PercentType.valueOf("11"), pos.getState(CoordinateSystem.PRIMARY_POSITION));
        assertEquals(UnDefType.UNDEF, pos.getState(CoordinateSystem.SECONDARY_POSITION));
        assertEquals(UnDefType.UNDEF, pos.getState(CoordinateSystem.VANE_TILT_POSITION));

        pos = new ShadePosition();
        pos.setPosition(CoordinateSystem.PRIMARY_POSITION, new PercentType(11));
        pos.setPosition(CoordinateSystem.SECONDARY_POSITION, new PercentType(22));
        pos.setPosition(CoordinateSystem.VANE_TILT_POSITION, new PercentType(33));
        assertEquals(PercentType.valueOf("11"), pos.getState(CoordinateSystem.PRIMARY_POSITION));
        assertEquals(PercentType.valueOf("22"), pos.getState(CoordinateSystem.SECONDARY_POSITION));
        assertEquals(PercentType.valueOf("33"), pos.getState(CoordinateSystem.VANE_TILT_POSITION));
    }

    /**
     * Test JSON shades response.
     */
    @Test
    public void testShadesParsing() throws IOException {
        String json = loadJson("gen3/shades.json");
        List<Shade> shadeList = List.of(gson.fromJson(json, Shade[].class));
        assertNotNull(shadeList);
        assertEquals(2, shadeList.size());
        Shade shadeData = shadeList.get(0);
        assertEquals("Upper Left", shadeData.getName());
        assertEquals(2, shadeData.getId());
        assertFalse(shadeData.isMainsPowered());
        assertEquals(new DecimalType(66), shadeData.getBatteryLevel());
        assertEquals(OnOffType.OFF, shadeData.getLowBattery());
        ShadePosition positions = shadeData.getShadePositions();
        assertNotNull(positions);
        Integer caps = shadeData.getCapabilities();
        assertNotNull(caps);
        Capabilities capabilities = db.getCapabilities(caps);
        assertTrue(capabilities.supportsPrimary());
        assertFalse(capabilities.supportsSecondary());
        assertFalse(capabilities.supportsTilt180());
        assertFalse(capabilities.supportsTiltAnywhere());
        assertFalse(capabilities.supportsTiltOnClosed());

        shadeData = shadeList.get(1);
        assertEquals(3, shadeData.getId());
        assertTrue(shadeData.isMainsPowered());
        positions = shadeData.getShadePositions();
        assertNotNull(positions);
        caps = shadeData.getCapabilities();
        assertNotNull(caps);
        capabilities = db.getCapabilities(caps);
        assertTrue(capabilities.supportsPrimary());
        assertFalse(capabilities.supportsSecondary());
        assertFalse(capabilities.supportsTilt180());
        assertFalse(capabilities.supportsTiltAnywhere());
        assertTrue(capabilities.supportsTiltOnClosed());
    }

    /**
     * Test sending properties and dynamic channel values to a shade handler.
     */
    @Test
    public void testShadeHandlerPropertiesAndChannels() throws IOException {
        ThingTypeUID thingTypeUID = new ThingTypeUID("hdpowerview:shade");
        ThingUID thingUID = new ThingUID(thingTypeUID, "test");

        List<Channel> channels = new ArrayList<Channel>();
        for (String channelId : Set.of(CHANNEL_SHADE_POSITION, CHANNEL_SHADE_SECONDARY_POSITION, CHANNEL_SHADE_VANE,
                CHANNEL_SHADE_BATTERY_LEVEL, CHANNEL_SHADE_LOW_BATTERY, CHANNEL_SHADE_SIGNAL_STRENGTH)) {
            ChannelUID channelUID = new ChannelUID(thingUID, channelId);
            channels.add(ChannelBuilder.create(channelUID).build());
        }

        String json = loadJson("gen3/shades.json");
        List<Shade> shadeList = List.of(gson.fromJson(json, Shade[].class));
        assertNotNull(shadeList);
        assertEquals(2, shadeList.size());

        Thing thing = ThingBuilder.create(thingTypeUID, thingUID).withChannels(channels).build();
        ShadeThingHandler shadeThingHandler;
        Shade shadeData;

        /*
         * Use the first JSON Shade entry.
         * It should support 4 dynamic channels.
         */
        shadeThingHandler = new ShadeThingHandler(thing);
        shadeThingHandler.setCallback(mock(ThingHandlerCallback.class));
        shadeData = shadeList.get(0).setId(0);
        assertTrue(shadeData.hasFullState());
        shadeThingHandler.notify(shadeData);
        Thing handlerThing = shadeThingHandler.getThing();
        assertEquals("Duette (6)", handlerThing.getProperties().get("type"));
        assertEquals("battery", handlerThing.getProperties().get("powerType"));
        assertEquals("3.0.359", handlerThing.getProperties().get("firmwareVersion"));
        assertEquals(new QuantityType<>(-50, Units.DECIBEL_MILLIWATTS), shadeData.getSignalStrength());
        assertEquals(4, handlerThing.getChannels().size());

        /*
         * Use the second JSON Shade entry.
         * It should support only 3 dynamic channels.
         */
        shadeThingHandler = new ShadeThingHandler(thing);
        shadeThingHandler.setCallback(mock(ThingHandlerCallback.class));
        shadeData = shadeList.get(1).setId(0);
        assertTrue(shadeData.hasFullState());
        shadeThingHandler.notify(shadeData);
        handlerThing = shadeThingHandler.getThing();
        assertEquals("Silhouette (23)", handlerThing.getProperties().get("type"));
        assertEquals("hardwired", handlerThing.getProperties().get("powerType"));
        assertEquals("3.0.359", handlerThing.getProperties().get("firmwareVersion"));
        assertEquals(new QuantityType<>(-51, Units.DECIBEL_MILLIWATTS), shadeData.getSignalStrength());
        assertEquals(3, handlerThing.getChannels().size());
    }

    /**
     * Test sending state change events to shade handler.
     */
    @Test
    public void testShadeHandlerEvents() throws IOException {
        ThingTypeUID thingTypeUID = new ThingTypeUID("hdpowerview:shade");
        ThingUID thingUID = new ThingUID(thingTypeUID, "test");

        List<Channel> channels = new ArrayList<Channel>();
        for (String channelId : Set.of(CHANNEL_SHADE_POSITION, CHANNEL_SHADE_SECONDARY_POSITION, CHANNEL_SHADE_VANE,
                CHANNEL_SHADE_BATTERY_LEVEL, CHANNEL_SHADE_LOW_BATTERY, CHANNEL_SHADE_SIGNAL_STRENGTH)) {
            ChannelUID channelUID = new ChannelUID(thingUID, channelId);
            channels.add(ChannelBuilder.create(channelUID).build());
        }

        String json = loadJson("gen3/shades.json");
        List<Shade> shadeList = List.of(gson.fromJson(json, Shade[].class));
        assertNotNull(shadeList);
        assertEquals(2, shadeList.size());

        Thing thing = ThingBuilder.create(thingTypeUID, thingUID).withChannels(channels).build();
        ShadeThingHandler shadeThingHandler;
        Shade shadeData;

        /*
         * Use the second JSON Shade entry, which only has a primary channel.
         */
        shadeThingHandler = new ShadeThingHandler(thing);
        shadeThingHandler.setCallback(mock(ThingHandlerCallback.class));
        shadeData = shadeList.get(1).setId(0);
        shadeThingHandler.notify(shadeData);

        /*
         * And try to update it with an event that has all 3 channels.
         */
        json = loadJson("gen3/shade-event.json");
        ShadeEvent event = gson.fromJson(json, ShadeEvent.class);
        assertNotNull(event);
        shadeData = new Shade().setId(0).setShadePosition(event.getCurrentPositions()).setPartialState();
        assertFalse(shadeData.hasFullState());
        shadeThingHandler.notify(shadeData);
    }
}
