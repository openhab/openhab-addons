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
package org.openhab.binding.hdpowerview;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.ShadePositionKind;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.Shade;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;

import com.google.gson.JsonParseException;

/**
 * Unit tests for HD PowerView binding
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewJUnitTests {

    public static final Pattern VALID_IP_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    /*
     * load a test JSON string from a file
     */
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

    @Test
    public void testCommunicationWithHub() {
        /*
         * NOTE: in order to actually run these tests you must have a hub physically
         * available, and its IP address must be correctly configured in the
         * "hubIPAddress" string constant e.g. "192.168.1.123"
         */
        String hubIPAddress = "192.168.1.xxx";

        /*
         * NOTE: set allowShadeMovementCommands = true if you accept physically moving
         * the shades
         */
        boolean allowShadeMovementCommands = false;

        if (VALID_IP_ADDRESS.matcher(hubIPAddress).matches()) {
            // initialize stuff
            Client client = ClientBuilder.newClient();
            assertNotNull(client);
            // client.register(new Logger());
            HDPowerViewWebTargets webTargets = new HDPowerViewWebTargets(client, hubIPAddress);
            assertNotNull(webTargets);

            // ==== exercise some code ====
            ShadePosition test;
            PercentType pos;

            // shade fully up
            test = ShadePosition.create(ShadePositionKind.PRIMARY, 0);
            assertNotNull(test);
            pos = test.getPercent(ShadePositionKind.PRIMARY);
            assertNotNull(pos);
            assertEquals(0, pos.intValue());
            pos = test.getPercent(ShadePositionKind.VANE);
            assertNotNull(pos);
            assertEquals(0, pos.intValue());

            // shade fully down (method 1)
            test = ShadePosition.create(ShadePositionKind.PRIMARY, 100);
            assertNotNull(test);
            pos = test.getPercent(ShadePositionKind.PRIMARY);
            assertNotNull(pos);
            assertEquals(100, pos.intValue());
            pos = test.getPercent(ShadePositionKind.VANE);
            assertNotNull(pos);
            assertEquals(0, pos.intValue());

            // shade fully down (method 2)
            test = ShadePosition.create(ShadePositionKind.VANE, 0);
            assertNotNull(test);
            pos = test.getPercent(ShadePositionKind.PRIMARY);
            assertNotNull(pos);
            assertEquals(100, pos.intValue());
            pos = test.getPercent(ShadePositionKind.VANE);
            assertNotNull(pos);
            assertEquals(0, pos.intValue());

            // shade fully down (method 2) and vane fully open
            test = ShadePosition.create(ShadePositionKind.VANE, 100);
            assertNotNull(test);
            pos = test.getPercent(ShadePositionKind.PRIMARY);
            assertNotNull(pos);
            assertEquals(100, pos.intValue());
            pos = test.getPercent(ShadePositionKind.VANE);
            assertNotNull(pos);
            assertEquals(100, pos.intValue());

            @Nullable
            String shadeId = null;
            @Nullable
            ShadePosition shadePos = null;
            @Nullable
            Shades shades = null;

            // test the JSON parsing for a duette top down bottom up shade
            try {
                ShadeData shadeData = null;
                String json = loadJson("duette");
                assertNotNull(json);
                assertNotEquals("", json);
                shades = webTargets.gson.fromJson(json, Shades.class);
                assertNotNull(shades);
                assertEquals(1, shades.shadeData.size());
                shadeData = shades.shadeData.get(0);
                assertEquals("Gardin 1", shadeData.getName());
                assertEquals("63778", shadeData.id);
                shadePos = shadeData.positions;
                assertNotNull(shadePos);
                assertEquals(ShadePositionKind.PRIMARY, shadePos.getPosKind());
                assertEquals(59, shadePos.getPercent(ShadePositionKind.PRIMARY).intValue());
                assertEquals(65, shadePos.getPercent(ShadePositionKind.SECONDARY).intValue());
                assertEquals(0, shadePos.getPercent(ShadePositionKind.VANE).intValue());
            } catch (JsonParseException e) {
                fail(e.getMessage());
            }

            // ==== get all shades ====
            try {
                shades = webTargets.getShades();
                assertNotNull(shades);
                assertTrue(shades.shadeData.size() > 0);
                assertTrue(shades.shadeData.get(0).getName().length() > 0);
                shadePos = shades.shadeData.get(0).positions;
                assertNotNull(shadePos);
                shadeId = shades.shadeData.get(0).id;
                assertNotNull(shadeId);
                for (ShadeData shadeData : shades.shadeData) {
                    String shadeName = shadeData.getName();
                    assertNotNull(shadeName);
                }
            } catch (JsonParseException | ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            // ==== get all scenes ====
            int sceneId = 0;
            try {
                Scenes scenes = webTargets.getScenes();
                assertNotNull(scenes);
                assertTrue(scenes.sceneData.size() > 0);
                sceneId = scenes.sceneData.get(0).id;
                assertTrue(sceneId > 0);
                for (Scene scene : scenes.sceneData) {
                    String sceneName = scene.getName();
                    assertNotNull(sceneName);
                }
            } catch (JsonParseException | ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            // ==== refresh a specific shade ====
            @Nullable
            Shade shade = null;
            try {
                assertNotNull(shadeId);
                shade = webTargets.refreshShade(shadeId);
                assertNotNull(shade);
            } catch (ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            // ==== move a specific shade ====
            if (allowShadeMovementCommands) {
                try {
                    assertNotNull(shadeId);
                    assertNotNull(shade);
                    @Nullable
                    ShadeData shadeData = shade.shade;
                    assertNotNull(shadeData);
                    ShadePosition positions = shadeData.positions;
                    assertNotNull(positions);
                    ShadePositionKind kind = positions.getPosKind();
                    assertNotNull(kind);
                    int position = positions.getPercent(kind).intValue();
                    position = position + ((position <= 10) ? 5 : -5);
                    ShadePosition newPos = ShadePosition.create(kind, position);
                    assertNotNull(newPos);
                    shade = webTargets.moveShade(shadeId, newPos);
                    assertNotNull(shade);
                } catch (ProcessingException | HubMaintenanceException e) {
                    fail(e.getMessage());
                }
            }

            // ==== activate a specific scene ====
            if (allowShadeMovementCommands) {
                try {
                    assertNotNull(sceneId);
                    webTargets.activateScene(sceneId);
                } catch (ProcessingException | HubMaintenanceException e) {
                    fail(e.getMessage());
                }
            }
        }
    }
}
