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
package org.openhab.binding.hdpowerview.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.dto.BatteryKind;
import org.openhab.binding.hdpowerview.internal.dto.Scene;
import org.openhab.binding.hdpowerview.internal.dto.SceneCollection;
import org.openhab.binding.hdpowerview.internal.dto.ShadeData;
import org.openhab.binding.hdpowerview.internal.dto.ShadePosition;
import org.openhab.binding.hdpowerview.internal.dto.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.dto.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.dto.responses.Shades;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Unit tests for HD PowerView binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * @author Jacob Laursen - Add support for scene groups
 */
@NonNullByDefault
public class HDPowerViewJUnitTests {

    private Gson gson = new Gson();

    private <T> T getObjectFromJson(String filename, Class<T> clazz) throws IOException {
        try (InputStream inputStream = HDPowerViewJUnitTests.class.getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("inputstream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            return Objects.requireNonNull(gson.fromJson(json, clazz));
        }
    }

    /**
     * Test generic JSON shades response.
     */
    @Test
    public void shadeNameIsDecoded() throws IOException {
        Shades shades = getObjectFromJson("shades.json", Shades.class);
        List<ShadeData> shadeData = shades.shadeData;
        assertNotNull(shadeData);
        assertEquals(3, shadeData.size());
        ShadeData shade = shadeData.get(0);
        assertEquals("Shade 2", shade.getName());
    }

    /**
     * Test the BatteryKind decoding.
     */
    @Test
    public void testBatteryKind() throws IOException {
        Shades shades = getObjectFromJson("shades.json", Shades.class);
        List<ShadeData> shadeData = shades.shadeData;
        assertNotNull(shadeData);
        ShadeData shade = shadeData.get(0);
        assertEquals(BatteryKind.HARDWIRED_POWER_SUPPLY, shade.getBatteryKind());
        shade = shadeData.get(1);
        assertEquals(BatteryKind.ERROR_UNKNOWN, shade.getBatteryKind());
    }

    /**
     * Test generic JSON scene response.
     */
    @Test
    public void sceneNameIsDecoded() throws IOException {
        Scenes scenes = getObjectFromJson("scenes.json", Scenes.class);
        List<Scene> sceneData = scenes.sceneData;
        assertNotNull(sceneData);
        assertEquals(4, sceneData.size());
        Scene scene = sceneData.get(0);
        assertEquals("Door Open", scene.getName());
    }

    /**
     * Test generic JSON scene collection response.
     */
    @Test
    public void sceneCollectionNameIsDecoded() throws IOException {
        SceneCollections sceneCollections = getObjectFromJson("sceneCollections.json", SceneCollections.class);

        List<SceneCollection> sceneCollectionData = sceneCollections.sceneCollectionData;
        assertNotNull(sceneCollectionData);
        assertEquals(1, sceneCollectionData.size());

        SceneCollection sceneCollection = sceneCollectionData.get(0);
        assertEquals("Børn op", sceneCollection.getName());
    }

    /**
     * Test the JSON parsing for a duette top down bottom up shade.
     */
    @Test
    public void duetteTopDownBottomUpShadeIsParsedCorrectly() throws IOException {
        Shades shades = getObjectFromJson("duette.json", Shades.class);
        List<ShadeData> shadesData = shades.shadeData;
        assertNotNull(shadesData);

        assertEquals(1, shadesData.size());
        ShadeData shadeData = shadesData.get(0);
        assertNotNull(shadeData);

        assertEquals("Gardin 1", shadeData.getName());
        assertEquals(63778, shadeData.id);

        ShadePosition shadePos = shadeData.positions;
        assertNotNull(shadePos);

        Integer capabilitiesValue = shadeData.capabilities;
        assertNotNull(capabilitiesValue);
        assertEquals(7, capabilitiesValue.intValue());
        ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();
        Capabilities capabilities = db.getCapabilities(capabilitiesValue);

        State pos = shadePos.getState(capabilities, PRIMARY_POSITION);
        assertEquals(PercentType.class, pos.getClass());
        assertEquals(59, ((PercentType) pos).intValue());

        pos = shadePos.getState(capabilities, SECONDARY_POSITION);
        assertEquals(PercentType.class, pos.getClass());
        assertEquals(35, ((PercentType) pos).intValue());

        pos = shadePos.getState(capabilities, VANE_TILT_POSITION);
        assertEquals(UnDefType.class, pos.getClass());

        assertEquals(3, shadeData.batteryStatus);

        assertEquals(4, shadeData.signalStrength);

        assertEquals(8, shadeData.type);

        assertTrue(db.isTypeInDatabase(shadeData.type));
        assertTrue(db.isCapabilitiesInDatabase(capabilitiesValue.intValue()));

        assertEquals(db.getType(shadeData.type).getCapabilities(), capabilitiesValue.intValue());

        assertTrue(db.getCapabilities(capabilitiesValue.intValue()).supportsSecondary());
        assertNotEquals(db.getType(shadeData.type).getCapabilities(), capabilitiesValue.intValue() + 1);

        // ==== when changing position1, position2 value is not changed (vice-versa) ====
        ShadePosition shadePosition = shadeData.positions;
        assertNotNull(shadePosition);
        // ==== position2 ====
        State position2Old = shadePosition.getState(capabilities, SECONDARY_POSITION);
        shadePosition.setPosition(capabilities, PRIMARY_POSITION, 99);
        State position2New = shadePosition.getState(capabilities, SECONDARY_POSITION);
        assertEquals(PercentType.class, position2Old.getClass());
        assertEquals(PercentType.class, position2New.getClass());
        assertEquals(((PercentType) position2Old).intValue(), ((PercentType) position2New).intValue());

        // ==== position2 ====
        State position1Old = shadePosition.getState(capabilities, PRIMARY_POSITION);
        shadePosition.setPosition(capabilities, SECONDARY_POSITION, 99);
        State position1New = shadePosition.getState(capabilities, PRIMARY_POSITION);
        assertEquals(PercentType.class, position1Old.getClass());
        assertEquals(PercentType.class, position1New.getClass());
        assertEquals(((PercentType) position1Old).intValue(), ((PercentType) position1New).intValue());
    }
}
