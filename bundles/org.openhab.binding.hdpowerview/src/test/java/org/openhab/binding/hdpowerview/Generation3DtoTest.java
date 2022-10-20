/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal._v3.HDPowerViewWebTargetsV3;
import org.openhab.binding.hdpowerview.internal.api.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.api.ShadeData;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api._v3.ShadePositionV3;
import org.openhab.binding.hdpowerview.internal.api.responses.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvent;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.UnDefType;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * Unit tests for Generation 3 DTO's.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Generation3DtoTest {

    private final HDPowerViewWebTargets webTargets = new HDPowerViewWebTargetsV3(new HttpClient(),
            Mockito.mock(ClientBuilder.class), Mockito.mock(SseEventSourceFactory.class), "");
    private static final ShadeCapabilitiesDatabase DB = new ShadeCapabilitiesDatabase();

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
     * Test JSON scenes response.
     */
    @Test
    public void testScenesParsing() throws IOException {
        try {
            Method method = webTargets.getClass().getDeclaredMethod("toScenes", String.class);
            method.setAccessible(true);
            String json = loadJson("_v3/scenes.json");
            Object result = method.invoke(webTargets, json);
            assertTrue(result instanceof Scenes);
            List<Scene> sceneData = ((Scenes) result).sceneData;
            assertNotNull(sceneData);
            assertEquals(1, sceneData.size());
            Scene scene = sceneData.get(0);
            assertEquals("Open All Office Shades\n ABC", scene.getName());
            assertEquals(234, scene.id);
            assertEquals(3, scene.version());
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test JSON shades response.
     */
    @Test
    public void testShadesParsing() throws IOException {
        try {
            Method method = webTargets.getClass().getDeclaredMethod("toShades", String.class);
            method.setAccessible(true);
            String json = loadJson("_v3/shades.json");
            Object result = method.invoke(webTargets, json);
            assertTrue(result instanceof Shades);
            List<ShadeData> shadeDataList = ((Shades) result).shadeData;
            assertNotNull(shadeDataList);
            assertEquals(1, shadeDataList.size());
            ShadeData shadeData = shadeDataList.get(0);
            assertEquals("Shade 2 ABC", shadeData.getName());
            assertEquals(789, shadeData.id);
            assertEquals(3, shadeData.version());
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test JSON automation response.
     */
    @Test
    public void testAutomationParsing() throws IOException {
        try {
            Method method = webTargets.getClass().getDeclaredMethod("toScheduledEvents", String.class);
            method.setAccessible(true);
            String json = loadJson("_v3/automations.json");
            Object result = method.invoke(webTargets, json);
            assertTrue(result instanceof ScheduledEvents);
            List<ScheduledEvent> scheduledEventList = ((ScheduledEvents) result).scheduledEventData;
            assertNotNull(scheduledEventList);
            assertEquals(1, scheduledEventList.size());
            ScheduledEvent scheduledEvent = scheduledEventList.get(0);
            assertEquals(33, scheduledEvent.id);
            assertTrue(scheduledEvent.enabled);
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test JSON shade event response.
     */
    @Test
    public void testShadeEventParsing() throws IOException {
        try {
            Method method = webTargets.getClass().getDeclaredMethod("toShadeData2", String.class);
            method.setAccessible(true);
            String json = loadJson("_v3/shade-event.json");
            Object result = method.invoke(webTargets, json);
            assertTrue(result instanceof ShadeData);
            ShadeData shadeData = ((ShadeData) result);
            assertEquals(3, shadeData.version());
            ShadePosition position = shadeData.positions;
            assertNotNull(position);
            assertEquals(PercentType.valueOf("99"),
                    position.getState(DB.getCapabilities(0), CoordinateSystem.PRIMARY_POSITION));
            assertEquals(PercentType.valueOf("98"),
                    position.getState(DB.getCapabilities(0), CoordinateSystem.SECONDARY_POSITION));
            assertEquals(PercentType.ZERO,
                    position.getState(DB.getCapabilities(0), CoordinateSystem.VANE_TILT_POSITION));
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test JSON shade position setting.
     */
    @Test
    public void testShadePositions() {
        ShadePositionV3 pos;
        Capabilities caps;

        caps = DB.getCapabilities(0); // test with only primary support
        pos = new ShadePositionV3();
        pos.setPosition(caps, CoordinateSystem.PRIMARY_POSITION, 11);
        pos.setPosition(caps, CoordinateSystem.SECONDARY_POSITION, 22);
        pos.setPosition(caps, CoordinateSystem.VANE_TILT_POSITION, 33);
        assertEquals(PercentType.valueOf("11"), pos.getState(caps, CoordinateSystem.PRIMARY_POSITION));
        assertEquals(UnDefType.UNDEF, pos.getState(caps, CoordinateSystem.SECONDARY_POSITION));
        assertEquals(UnDefType.UNDEF, pos.getState(caps, CoordinateSystem.VANE_TILT_POSITION));

        caps = DB.getCapabilities(9);// test with primary, secondary, and tilt support
        pos = new ShadePositionV3();
        pos.setPosition(caps, CoordinateSystem.PRIMARY_POSITION, 11);
        pos.setPosition(caps, CoordinateSystem.SECONDARY_POSITION, 22);
        pos.setPosition(caps, CoordinateSystem.VANE_TILT_POSITION, 33);
        assertEquals(PercentType.valueOf("11"), pos.getState(caps, CoordinateSystem.PRIMARY_POSITION));
        assertEquals(PercentType.valueOf("22"), pos.getState(caps, CoordinateSystem.SECONDARY_POSITION));
        assertEquals(PercentType.valueOf("33"), pos.getState(caps, CoordinateSystem.VANE_TILT_POSITION));
    }
}
