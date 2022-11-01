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
package org.openhab.binding.hdpowerview.internal.gen3;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.GatewayWebTargets;
import org.openhab.binding.hdpowerview.internal.HDPowerViewJUnitTests;
import org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Scene;
import org.openhab.binding.hdpowerview.internal.dto.gen3.SceneEvent;
import org.openhab.binding.hdpowerview.internal.dto.gen3.Shade;
import org.openhab.binding.hdpowerview.internal.dto.gen3.ShadeEvent;
import org.openhab.binding.hdpowerview.internal.dto.gen3.ShadePosition;
import org.openhab.core.library.types.PercentType;
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
        List<Scene> sceneList = gson.fromJson(json, GatewayWebTargets.LIST_SCENES);
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
        pos.setPosition(CoordinateSystem.PRIMARY_POSITION, 11);
        assertEquals(PercentType.valueOf("11"), pos.getState(CoordinateSystem.PRIMARY_POSITION));
        assertEquals(UnDefType.UNDEF, pos.getState(CoordinateSystem.SECONDARY_POSITION));
        assertEquals(UnDefType.UNDEF, pos.getState(CoordinateSystem.VANE_TILT_POSITION));

        pos = new ShadePosition();
        pos.setPosition(CoordinateSystem.PRIMARY_POSITION, 11);
        pos.setPosition(CoordinateSystem.SECONDARY_POSITION, 22);
        pos.setPosition(CoordinateSystem.VANE_TILT_POSITION, 33);
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
        List<Shade> shadeList = gson.fromJson(json, GatewayWebTargets.LIST_SHADES);
        assertNotNull(shadeList);
        assertEquals(1, shadeList.size());
        Shade shadeData = shadeList.get(0);
        assertEquals("Shade 2 ABC", shadeData.getName());
        assertEquals(789, shadeData.getId());
    }
}
