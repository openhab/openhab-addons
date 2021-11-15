/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import static org.openhab.binding.hdpowerview.internal.api.ActuatorClass.*;
import static org.openhab.binding.hdpowerview.internal.api.CoordinateSystem.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.api.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections.SceneCollection;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.Shade;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Unit tests for HD PowerView binding
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * @author Jacob Laursen - Add support for scene groups
 */
@NonNullByDefault
public class HDPowerViewJUnitTests {

    private static final Pattern VALID_IP_V4_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    /*
     * load a test JSON string from a file
     */
    private String loadJson(String fileName) {
        try {
            return Files.readAllLines(Paths.get(String.format("src/test/resources/%s.json", fileName))).stream()
                    .collect(Collectors.joining());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }

    /**
     * Run a series of ONLINE tests on the communication with a hub
     *
     * @param hubIPAddress must be a valid hub IP address to run the
     *            tests on; or an INVALID IP address to
     *            suppress the tests
     * @param allowShadeMovementCommands set to true if you accept that the tests
     *            shall physically move the shades
     */
    @Test
    public void testOnlineCommunication() {
        /*
         * NOTE: in order to actually run these tests you must have a hub physically
         * available, and its IP address must be correctly configured in the
         * "hubIPAddress" string constant e.g. "192.168.1.123"
         */
        String hubIPAddress = "192.168.1.xxx";

        /*
         * NOTE: set allowShadeMovementCommands = true if you accept physically moving
         * the shades during these tests
         */
        boolean allowShadeMovementCommands = false;

        if (VALID_IP_V4_ADDRESS.matcher(hubIPAddress).matches()) {
            // ==== initialize stuff ====
            HttpClient client = new HttpClient();
            assertNotNull(client);

            // ==== start the client ====
            try {
                client.start();
                assertTrue(client.isStarted());
            } catch (Exception e) {
                fail(e.getMessage());
            }

            HDPowerViewWebTargets webTargets = new HDPowerViewWebTargets(client, hubIPAddress);
            assertNotNull(webTargets);

            // ==== exercise some code ====
            ShadePosition test;
            State pos;

            // shade fully up
            test = ShadePosition.create(ZERO_IS_CLOSED, 0);
            assertNotNull(test);
            pos = test.getState(PRIMARY_ACTUATOR, ZERO_IS_CLOSED);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(0, ((PercentType) pos).intValue());
            pos = test.getState(PRIMARY_ACTUATOR, VANE_COORDS);
            assertTrue(UnDefType.UNDEF.equals(pos));

            // shade fully down (method 1)
            test = ShadePosition.create(ZERO_IS_CLOSED, 100);
            assertNotNull(test);
            pos = test.getState(PRIMARY_ACTUATOR, ZERO_IS_CLOSED);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(100, ((PercentType) pos).intValue());
            pos = test.getState(PRIMARY_ACTUATOR, VANE_COORDS);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(0, ((PercentType) pos).intValue());

            // shade fully down (method 2)
            test = ShadePosition.create(VANE_COORDS, 0);
            assertNotNull(test);
            pos = test.getState(PRIMARY_ACTUATOR, ZERO_IS_CLOSED);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(100, ((PercentType) pos).intValue());
            pos = test.getState(PRIMARY_ACTUATOR, VANE_COORDS);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(0, ((PercentType) pos).intValue());

            // shade fully down (method 2) and vane fully open
            test = ShadePosition.create(VANE_COORDS, 100);
            assertNotNull(test);
            pos = test.getState(PRIMARY_ACTUATOR, ZERO_IS_CLOSED);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(100, ((PercentType) pos).intValue());
            pos = test.getState(PRIMARY_ACTUATOR, VANE_COORDS);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(100, ((PercentType) pos).intValue());

            int shadeId = 0;
            @Nullable
            ShadePosition shadePos = null;
            @Nullable
            Shades shadesX = null;

            // ==== get all shades ====
            try {
                shadesX = webTargets.getShades();
                assertNotNull(shadesX);
                @Nullable
                List<ShadeData> shadesData = shadesX.shadeData;
                assertNotNull(shadesData);
                assertTrue(!shadesData.isEmpty());
                @Nullable
                ShadeData shadeData;
                shadeData = shadesData.get(0);
                assertNotNull(shadeData);
                assertTrue(shadeData.getName().length() > 0);
                shadePos = shadeData.positions;
                assertNotNull(shadePos);
                @Nullable
                ShadeData shadeZero = shadesData.get(0);
                assertNotNull(shadeZero);
                shadeId = shadeZero.id;
                assertNotEquals(0, shadeId);

                for (ShadeData shadexData : shadesData) {
                    String shadeName = shadexData.getName();
                    assertNotNull(shadeName);
                }
            } catch (JsonParseException | HubProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            // ==== get all scenes ====
            int sceneId = 0;
            try {
                Scenes scenes = webTargets.getScenes();
                assertNotNull(scenes);
                @Nullable
                List<Scene> scenesData = scenes.sceneData;
                assertNotNull(scenesData);
                assertTrue(!scenesData.isEmpty());
                @Nullable
                Scene sceneZero = scenesData.get(0);
                assertNotNull(sceneZero);
                sceneId = sceneZero.id;
                assertTrue(sceneId > 0);

                for (Scene scene : scenesData) {
                    String sceneName = scene.getName();
                    assertNotNull(sceneName);
                }
            } catch (JsonParseException | HubProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            // ==== refresh a specific shade ====
            @Nullable
            Shade shade = null;
            try {
                assertNotEquals(0, shadeId);
                shade = webTargets.refreshShadePosition(shadeId);
                assertNotNull(shade);
            } catch (HubProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            // ==== move a specific shade ====
            try {
                assertNotEquals(0, shadeId);
                assertNotNull(shade);
                @Nullable
                ShadeData shadeData = shade.shade;
                assertNotNull(shadeData);
                ShadePosition positions = shadeData.positions;
                assertNotNull(positions);
                CoordinateSystem coordSys = positions.getCoordinateSystem(PRIMARY_ACTUATOR);
                assertNotNull(coordSys);

                pos = positions.getState(PRIMARY_ACTUATOR, coordSys);
                assertEquals(PercentType.class, pos.getClass());

                pos = positions.getState(PRIMARY_ACTUATOR, ZERO_IS_CLOSED);
                assertEquals(PercentType.class, pos.getClass());

                int position = ((PercentType) pos).intValue();
                position = position + ((position <= 10) ? 5 : -5);

                ShadePosition newPos = ShadePosition.create(ZERO_IS_CLOSED, position);
                assertNotNull(newPos);

                if (allowShadeMovementCommands) {
                    webTargets.moveShade(shadeId, newPos);
                }
            } catch (HubProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            // ==== activate a specific scene ====
            if (allowShadeMovementCommands) {
                try {
                    assertNotNull(sceneId);
                    webTargets.activateScene(sceneId);
                } catch (HubProcessingException | HubMaintenanceException e) {
                    fail(e.getMessage());
                }
            }

            // ==== test stop command ====
            if (allowShadeMovementCommands) {
                try {
                    assertNotNull(sceneId);
                    webTargets.stopShade(shadeId);
                } catch (HubProcessingException | HubMaintenanceException e) {
                    fail(e.getMessage());
                }
            }

            // ==== stop the client ====
            if (client.isRunning()) {
                try {
                    client.stop();
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        }
    }

    /**
     * Test generic JSON shades response
     */
    @Test
    public void shadeResponseIsParsedCorrectly() throws JsonParseException {
        final Gson gson = new Gson();
        @Nullable
        Shades shades;
        String json = loadJson("shades");
        assertNotEquals("", json);
        shades = gson.fromJson(json, Shades.class);
        assertNotNull(shades);
    }

    /**
     * Test generic JSON scene response
     */
    @Test
    public void sceneResponseIsParsedCorrectly() throws JsonParseException {
        final Gson gson = new Gson();
        String json = loadJson("scenes");
        assertNotEquals("", json);

        @Nullable
        Scenes scenes = gson.fromJson(json, Scenes.class);
        assertNotNull(scenes);

        @Nullable
        List<Scene> sceneData = scenes.sceneData;
        assertNotNull(sceneData);

        assertEquals(4, sceneData.size());
        @Nullable
        Scene scene = sceneData.get(0);
        assertEquals("Door Open", scene.getName());
        assertEquals(18097, scene.id);
    }

    /**
     * Test generic JSON scene collection response
     */
    @Test
    public void sceneCollectionResponseIsParsedCorrectly() throws JsonParseException {
        final Gson gson = new Gson();
        String json = loadJson("sceneCollections");
        assertNotEquals("", json);

        @Nullable
        SceneCollections sceneCollections = gson.fromJson(json, SceneCollections.class);
        assertNotNull(sceneCollections);
        @Nullable
        List<SceneCollection> sceneCollectionData = sceneCollections.sceneCollectionData;
        assertNotNull(sceneCollectionData);

        assertEquals(1, sceneCollectionData.size());
        @Nullable
        SceneCollection sceneCollection = sceneCollectionData.get(0);
        assertEquals("Børn op", sceneCollection.getName());
        assertEquals(27119, sceneCollection.id);
    }

    /**
     * Test the JSON parsing for a duette top down bottom up shade
     */
    @Test
    public void duetteTopDownBottomUpShadeIsParsedCorrectly() throws JsonParseException {
        final Gson gson = new Gson();
        String json = loadJson("duette");
        assertNotEquals("", json);

        @Nullable
        Shades shades = gson.fromJson(json, Shades.class);
        assertNotNull(shades);
        @Nullable
        List<ShadeData> shadesData = shades.shadeData;
        assertNotNull(shadesData);

        assertEquals(1, shadesData.size());
        @Nullable
        ShadeData shadeData = shadesData.get(0);
        assertNotNull(shadeData);

        assertEquals("Gardin 1", shadeData.getName());
        assertEquals(63778, shadeData.id);

        ShadePosition shadePos = shadeData.positions;
        assertNotNull(shadePos);
        assertEquals(ZERO_IS_CLOSED, shadePos.getCoordinateSystem(PRIMARY_ACTUATOR));

        State pos = shadePos.getState(PRIMARY_ACTUATOR, ZERO_IS_CLOSED);
        assertEquals(PercentType.class, pos.getClass());
        assertEquals(59, ((PercentType) pos).intValue());

        pos = shadePos.getState(SECONDARY_ACTUATOR, ZERO_IS_OPEN);
        assertEquals(PercentType.class, pos.getClass());
        assertEquals(35, ((PercentType) pos).intValue());

        pos = shadePos.getState(PRIMARY_ACTUATOR, VANE_COORDS);
        assertEquals(UnDefType.class, pos.getClass());

        assertEquals(3, shadeData.batteryStatus);

        assertEquals(4, shadeData.signalStrength);
    }
}
