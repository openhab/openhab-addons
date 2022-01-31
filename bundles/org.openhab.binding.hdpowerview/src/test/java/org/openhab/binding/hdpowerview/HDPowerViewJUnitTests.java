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
import static org.openhab.binding.hdpowerview.internal.api.CoordinateSystem.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections.SceneCollection;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Unit tests for HD PowerView binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * @author Jacob Laursen - Add support for scene groups
 */
@NonNullByDefault
public class HDPowerViewJUnitTests {

    private static final Pattern VALID_IP_V4_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    /*
     * load a test JSON string from a file.
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
     * Run a series of ONLINE tests on the communication with a hub.
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

            int shadeId = 0;
            ShadePosition shadePos = null;
            Shades shadesX = null;

            // ==== get all shades ====
            try {
                shadesX = webTargets.getShades();
                assertNotNull(shadesX);
                List<ShadeData> shadesData = shadesX.shadeData;
                assertNotNull(shadesData);

                if (shadesData != null) {
                    assertTrue(!shadesData.isEmpty());
                    ShadeData shadeData;
                    shadeData = shadesData.get(0);
                    assertNotNull(shadeData);
                    assertTrue(shadeData.getName().length() > 0);
                    shadePos = shadeData.positions;
                    assertNotNull(shadePos);
                    ShadeData shadeZero = shadesData.get(0);
                    assertNotNull(shadeZero);
                    shadeId = shadeZero.id;
                    assertNotEquals(0, shadeId);

                    for (ShadeData shadexData : shadesData) {
                        String shadeName = shadexData.getName();
                        assertNotNull(shadeName);
                    }
                }
            } catch (HubException e) {
                fail(e.getMessage());
            }

            // ==== get all scenes ====
            int sceneId = 0;
            try {
                Scenes scenes = webTargets.getScenes();
                assertNotNull(scenes);

                List<Scene> scenesData = scenes.sceneData;
                assertNotNull(scenesData);

                if (scenesData != null) {
                    assertTrue(!scenesData.isEmpty());
                    Scene sceneZero = scenesData.get(0);
                    assertNotNull(sceneZero);
                    sceneId = sceneZero.id;
                    assertTrue(sceneId > 0);

                    for (Scene scene : scenesData) {
                        String sceneName = scene.getName();
                        assertNotNull(sceneName);
                    }
                }
            } catch (HubException e) {
                fail(e.getMessage());
            }

            // ==== refresh a specific shade ====
            ShadeData shadeData = null;
            try {
                assertNotEquals(0, shadeId);
                shadeData = webTargets.refreshShadePosition(shadeId);
            } catch (HubException e) {
                fail(e.getMessage());
            }

            // ==== move a specific shade ====
            try {
                assertNotEquals(0, shadeId);

                if (shadeData != null) {
                    ShadePosition positions = shadeData.positions;
                    assertNotNull(positions);
                    Integer capabilitiesValue = shadeData.capabilities;
                    assertNotNull(capabilitiesValue);

                    if (positions != null && capabilitiesValue != null) {
                        Capabilities capabilities = new ShadeCapabilitiesDatabase()
                                .getCapabilities(capabilitiesValue.intValue());

                        State pos = positions.getState(capabilities, PRIMARY_POSITION);
                        assertEquals(PercentType.class, pos.getClass());

                        int position = ((PercentType) pos).intValue();
                        position = position + ((position <= 10) ? 5 : -5);

                        ShadePosition targetPosition = new ShadePosition().setPosition(capabilities, PRIMARY_POSITION,
                                position);
                        assertNotNull(targetPosition);

                        if (allowShadeMovementCommands) {
                            webTargets.moveShade(shadeId, targetPosition);

                            ShadeData newData = webTargets.getShade(shadeId);
                            ShadePosition actualPosition = newData.positions;
                            assertNotNull(actualPosition);
                            if (actualPosition != null) {
                                assertEquals(targetPosition.getState(capabilities, PRIMARY_POSITION),
                                        actualPosition.getState(capabilities, PRIMARY_POSITION));
                            }
                        }
                    }
                }
            } catch (HubException e) {
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
                } catch (HubException e) {
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
     * Test generic JSON shades response.
     */
    @Test
    public void shadeResponseIsParsedCorrectly() throws JsonParseException {
        final Gson gson = new Gson();
        Shades shades;
        String json = loadJson("shades");
        assertNotEquals("", json);
        shades = gson.fromJson(json, Shades.class);
        assertNotNull(shades);
    }

    /**
     * Test generic JSON scene response.
     */
    @Test
    public void sceneResponseIsParsedCorrectly() throws JsonParseException {
        final Gson gson = new Gson();
        String json = loadJson("scenes");
        assertNotEquals("", json);

        Scenes scenes = gson.fromJson(json, Scenes.class);
        assertNotNull(scenes);
        if (scenes != null) {
            List<Scene> sceneData = scenes.sceneData;
            assertNotNull(sceneData);
            if (sceneData != null) {
                assertEquals(4, sceneData.size());
                Scene scene = sceneData.get(0);
                assertEquals("Door Open", scene.getName());
                assertEquals(18097, scene.id);
            }
        }
    }

    /**
     * Test generic JSON scene collection response.
     */
    @Test
    public void sceneCollectionResponseIsParsedCorrectly() throws JsonParseException {
        final Gson gson = new Gson();
        String json = loadJson("sceneCollections");
        assertNotEquals("", json);

        SceneCollections sceneCollections = gson.fromJson(json, SceneCollections.class);
        assertNotNull(sceneCollections);

        if (sceneCollections != null) {
            List<SceneCollection> sceneCollectionData = sceneCollections.sceneCollectionData;
            assertNotNull(sceneCollectionData);
            if (sceneCollectionData != null) {
                assertEquals(1, sceneCollectionData.size());

                SceneCollection sceneCollection = sceneCollectionData.get(0);
                assertEquals("Børn op", sceneCollection.getName());
                assertEquals(27119, sceneCollection.id);
            }
        }
    }

    /**
     * Test the JSON parsing for a duette top down bottom up shade.
     */
    @Test
    public void duetteTopDownBottomUpShadeIsParsedCorrectly() throws JsonParseException {
        final Gson gson = new Gson();
        String json = loadJson("duette");
        assertNotEquals("", json);

        Shades shades = gson.fromJson(json, Shades.class);
        assertNotNull(shades);
        if (shades != null) {
            List<ShadeData> shadesData = shades.shadeData;
            assertNotNull(shadesData);

            if (shadesData != null) {
                assertEquals(1, shadesData.size());
                ShadeData shadeData = shadesData.get(0);
                assertNotNull(shadeData);

                assertEquals("Gardin 1", shadeData.getName());
                assertEquals(63778, shadeData.id);

                ShadePosition shadePos = shadeData.positions;
                assertNotNull(shadePos);

                if (shadePos != null) {
                    Integer capabilitiesValue = shadeData.capabilities;
                    assertNotNull(capabilitiesValue);
                    if (capabilitiesValue != null) {
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
                        if (shadePosition != null) {
                            // ==== position2 ====
                            State position2Old = shadePosition.getState(capabilities, SECONDARY_POSITION);
                            shadePosition.setPosition(capabilities, PRIMARY_POSITION, 99);
                            State position2New = shadePosition.getState(capabilities, SECONDARY_POSITION);
                            assertEquals(PercentType.class, position2Old.getClass());
                            assertEquals(PercentType.class, position2New.getClass());
                            assertEquals(((PercentType) position2Old).intValue(),
                                    ((PercentType) position2New).intValue());

                            // ==== position2 ====
                            State position1Old = shadePosition.getState(capabilities, PRIMARY_POSITION);
                            shadePosition.setPosition(capabilities, SECONDARY_POSITION, 99);
                            State position1New = shadePosition.getState(capabilities, PRIMARY_POSITION);
                            assertEquals(PercentType.class, position1Old.getClass());
                            assertEquals(PercentType.class, position1New.getClass());
                            assertEquals(((PercentType) position1Old).intValue(),
                                    ((PercentType) position1New).intValue());
                        }
                    }
                }
            }
        }
    }
}
