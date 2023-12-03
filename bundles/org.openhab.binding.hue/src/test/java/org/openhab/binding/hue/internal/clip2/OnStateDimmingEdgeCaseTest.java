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
package org.openhab.binding.hue.internal.clip2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip2.Dimming;
import org.openhab.binding.hue.internal.api.dto.clip2.OnState;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.Resources;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.Setters;
import org.openhab.binding.hue.internal.api.serialization.InstantDeserializer;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JUnit test for edge cases of OnState and Dimming event and cache resources.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class OnStateDimmingEdgeCaseTest {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer())
            .create();

    private static final PercentType PERCENT_TYPE_FIFTY = new PercentType(50);

    private String loadJson(String fileName) {
        try (FileReader file = new FileReader(String.format("src/test/resources/%s.json", fileName));
                BufferedReader reader = new BufferedReader(file)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }

    private Resource loadEventResource(String fileName) {
        Resources resources = GSON.fromJson(loadJson(fileName), Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        assertEquals(1, list.size());
        Resource item = list.get(0);
        assertEquals(ResourceType.LIGHT, item.getType());
        return item.markAsSparse();
    }

    /**
     * Process a Dimming.brightness == 50 event when the cache contains no OnState element:
     *
     * <li>brightness state == Dimming.brightness
     * <li>on-off state == NULL
     */
    @Test
    void testDimming50CacheEmpty() {
        Resource cachedResource = new Resource(ResourceType.LIGHT);
        Resource eventResource = loadEventResource("light_event_dimming_50");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(PERCENT_TYPE_FIFTY, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(UnDefType.NULL, onOff);
    }

    /**
     * Process a Dimming.brightness == 50 event when the cache contains an OnState == on element:
     *
     * <li>brightness state == Dimming.brightness
     * <li>on-off state == ON
     */
    @Test
    void testDimming50CacheOn() {
        Resource cachedResource = new Resource(ResourceType.LIGHT).setOnState(new OnState().setOn(true));
        Resource eventResource = loadEventResource("light_event_dimming_50");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(PERCENT_TYPE_FIFTY, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.ON, onOff);
    }

    /**
     * Process a Dimming.brightness == 50 event when the cache contains an OnState == off element:
     *
     * <li>brightness state == ZERO
     * <li>on-off state == OFF
     */
    @Test
    void testDimming50CacheOff() {
        Resource cachedResource = new Resource(ResourceType.LIGHT).setOnState(new OnState().setOn(false));
        Resource eventResource = loadEventResource("light_event_dimming_50");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(PercentType.ZERO, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.OFF, onOff);
    }

    /**
     * Process a Dimming.brightness == 0 event when the cache contains no OnState element:
     *
     * <li>brightness state == Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL
     * <li>on-off state == NULL
     */
    @Test
    void testDimming0CacheEmpty() {
        Resource cachedResource = new Resource(ResourceType.LIGHT);
        Resource eventResource = loadEventResource("light_event_dimming_0");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertTrue(brightness instanceof PercentType);
        assertEquals(Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL, ((PercentType) brightness).doubleValue());
        State onOff = eventResource.getOnOffState();
        assertEquals(UnDefType.NULL, onOff);
    }

    /**
     * Process a Dimming.brightness == 0 event when the cache contains an OnState == on element:
     *
     * <li>brightness state == Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL
     * <li>on-off state == ON
     */
    @Test
    void testDimming0CacheOn() {
        Resource cachedResource = new Resource(ResourceType.LIGHT).setOnState(new OnState().setOn(true));
        Resource eventResource = loadEventResource("light_event_dimming_0");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertTrue(brightness instanceof PercentType);
        assertEquals(Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL, ((PercentType) brightness).doubleValue());
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.ON, onOff);
    }

    /**
     * Process a Dimming.brightness == 0 event when the cache contains an OnState == off element:
     *
     * <li>brightness state == ZERO
     * <li>on-off state == OFF
     */
    @Test
    void testDimming0CacheOff() {
        Resource cachedResource = new Resource(ResourceType.LIGHT).setOnState(new OnState().setOn(false));
        Resource eventResource = loadEventResource("light_event_dimming_0");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(PercentType.ZERO, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.OFF, onOff);
    }

    /**
     * Process an OnState == on event when the cache contains no Dimming element:
     *
     * <li>brightness state == NULL
     * <li>on-off state == ON
     */
    @Test
    void testOnCacheEmpty() {
        Resource cachedResource = new Resource(ResourceType.LIGHT);
        Resource eventResource = loadEventResource("light_event_on");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(UnDefType.NULL, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.ON, onOff);
    }

    /**
     * Process an OnState == on event when the cache contains a Dimming.brightness == 50 element:
     *
     * <li>brightness state == Dimming.brightness
     * <li>on-off state == ON
     */
    @Test
    void testOnCacheDimming50() {
        Resource cachedResource = new Resource(ResourceType.LIGHT).setDimming(new Dimming().setBrightness(50));
        Resource eventResource = loadEventResource("light_event_on");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(PERCENT_TYPE_FIFTY, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.ON, onOff);
    }

    /**
     * Process an OnState == on event when the cache contains a Dimming.brightness == 0 element:
     *
     * <li>brightness state == Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL
     * <li>on-off state == ON
     */
    @Test
    void testOnCacheDimming0() {
        Resource cachedResource = new Resource(ResourceType.LIGHT).setDimming(new Dimming().setBrightness(0));
        Resource eventResource = loadEventResource("light_event_on");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertTrue(brightness instanceof PercentType);
        assertEquals(Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL, ((PercentType) brightness).doubleValue());
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.ON, onOff);
    }

    /**
     * Process an OnState == off event when the cache contains no Dimming element:
     *
     * <li>brightness state == NULL
     * <li>on-off state == OFF
     */
    @Test
    void testOffCacheEmpty() {
        Resource cachedResource = new Resource(ResourceType.LIGHT);
        Resource eventResource = loadEventResource("light_event_off");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(UnDefType.NULL, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.OFF, onOff);
    }

    /**
     * Process an OnState == off event when the cache contains a Dimming.brightness == 50 element:
     *
     * <li>brightness state == ZERO
     * <li>on-off state == OFF
     */
    @Test
    void testOffCacheDimming50() {
        Resource cachedResource = new Resource(ResourceType.LIGHT).setDimming(new Dimming().setBrightness(50));
        Resource eventResource = loadEventResource("light_event_off");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(PercentType.ZERO, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.OFF, onOff);
    }

    /**
     * Process an OnState == off event when the cache contains a Dimming.brightness == 0 element:
     *
     * <li>brightness state == ZERO
     * <li>on-off state == OFF
     */
    @Test
    void testOffCacheDimming0() {
        Resource cachedResource = new Resource(ResourceType.LIGHT).setDimming(new Dimming().setBrightness(0));
        Resource eventResource = loadEventResource("light_event_off");
        Setters.setResource(eventResource, cachedResource);

        State brightness = eventResource.getBrightnessState();
        assertEquals(PercentType.ZERO, brightness);
        State onOff = eventResource.getOnOffState();
        assertEquals(OnOffType.OFF, onOff);
    }
}
