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
import static org.openhab.binding.hdpowerview.internal.api.PosKind.*;
import static org.openhab.binding.hdpowerview.internal.api.PosSeq.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.api.PosKind;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
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

    public static final Pattern VALID_IP_V4_ADDRESS = Pattern
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

        if (VALID_IP_V4_ADDRESS.matcher(hubIPAddress).matches()) {
            // initialize stuff
            Client client = ClientBuilder.newClient();
            assertNotNull(client);
            // client.register(new Logger());
            HDPowerViewWebTargets webTargets = new HDPowerViewWebTargets(client, hubIPAddress);
            assertNotNull(webTargets);

            // ==== exercise some code ====
            ShadePosition test;
            State pos;

            // shade fully up
            test = ShadePosition.create(REGULAR, 0);
            assertNotNull(test);
            pos = test.getState(PRIMARY, REGULAR);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(0, ((PercentType) pos).intValue());
            pos = test.getState(PRIMARY, VANE);
            assertTrue(UnDefType.UNDEF.equals(pos));

            // shade fully down (method 1)
            test = ShadePosition.create(REGULAR, 100);
            assertNotNull(test);
            pos = test.getState(PRIMARY, REGULAR);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(100, ((PercentType) pos).intValue());
            pos = test.getState(PRIMARY, VANE);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(0, ((PercentType) pos).intValue());

            // shade fully down (method 2)
            test = ShadePosition.create(VANE, 0);
            assertNotNull(test);
            pos = test.getState(PRIMARY, REGULAR);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(100, ((PercentType) pos).intValue());
            pos = test.getState(PRIMARY, VANE);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(0, ((PercentType) pos).intValue());

            // shade fully down (method 2) and vane fully open
            test = ShadePosition.create(VANE, 100);
            assertNotNull(test);
            pos = test.getState(PRIMARY, REGULAR);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(100, ((PercentType) pos).intValue());
            pos = test.getState(PRIMARY, VANE);
            assertEquals(PercentType.class, pos.getClass());
            assertEquals(100, ((PercentType) pos).intValue());

            @Nullable
            String shadeId = null;
            @Nullable
            ShadePosition shadePos = null;
            @Nullable
            Shades shades = null;

            // test the JSON parsing for a duette top down bottom up shade
            try {
                @Nullable
                ShadeData shadeData = null;
                String json = loadJson("duette");
                assertNotNull(json);
                assertNotEquals("", json);

                shades = webTargets.gson.fromJson(json, Shades.class);
                assertNotNull(shades);
                @Nullable
                List<ShadeData> shadesData = shades.shadeData;
                assertNotNull(shadesData);

                assertEquals(1, shadesData.size());
                shadeData = shadesData.get(0);
                assertNotNull(shadeData);

                assertEquals("Gardin 1", shadeData.getName());
                assertEquals("63778", shadeData.id);

                shadePos = shadeData.positions;
                assertNotNull(shadePos);
                assertEquals(REGULAR, shadePos.getPosKind());

                pos = shadePos.getState(PRIMARY, REGULAR);
                assertEquals(PercentType.class, pos.getClass());
                assertEquals(59, ((PercentType) pos).intValue());

                pos = shadePos.getState(SECONDARY, INVERTED);
                assertEquals(PercentType.class, pos.getClass());
                assertEquals(65, ((PercentType) pos).intValue());

                pos = shadePos.getState(PRIMARY, VANE);
                assertEquals(UnDefType.class, pos.getClass());
            } catch (JsonParseException e) {
                fail(e.getMessage());
            }

            // ==== get all shades ====
            try {
                shades = webTargets.getShades();
                assertNotNull(shades);
                @Nullable
                List<ShadeData> shadesData = shades.shadeData;
                assertNotNull(shadesData);
                assertTrue(shadesData.size() > 0);
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
                assertNotNull(shadeId);

                for (ShadeData shadexData : shadesData) {
                    String shadeName = shadexData.getName();
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
                @Nullable
                List<Scene> scenesData = scenes.sceneData;
                assertNotNull(scenesData);
                assertTrue(scenesData.size() > 0);
                @Nullable
                Scene sceneZero = scenesData.get(0);
                assertNotNull(sceneZero);
                sceneId = sceneZero.id;
                assertTrue(sceneId > 0);

                for (Scene scene : scenesData) {
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
            try {
                assertNotNull(shadeId);
                assertNotNull(shade);
                @Nullable
                ShadeData shadeData = shade.shade;
                assertNotNull(shadeData);
                ShadePosition positions = shadeData.positions;
                assertNotNull(positions);
                PosKind kind = positions.getPosKind();
                assertNotNull(kind);

                pos = positions.getState(PRIMARY, kind);
                assertEquals(PercentType.class, pos.getClass());

                int position = ((PercentType) pos).intValue();
                position = position + ((position <= 10) ? 5 : -5);

                ShadePosition newPos = ShadePosition.create(kind, position);
                assertNotNull(newPos);

                if (allowShadeMovementCommands) {
                    shade = webTargets.moveShade(shadeId, newPos);
                    assertNotNull(shade);
                }
            } catch (ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
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
