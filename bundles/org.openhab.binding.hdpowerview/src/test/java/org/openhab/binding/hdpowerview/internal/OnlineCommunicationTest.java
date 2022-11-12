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
package org.openhab.binding.hdpowerview.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem.*;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.dto.Scene;
import org.openhab.binding.hdpowerview.internal.dto.ShadeData;
import org.openhab.binding.hdpowerview.internal.dto.ShadePosition;
import org.openhab.binding.hdpowerview.internal.dto.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.dto.responses.Shades;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;

/**
 * Unit tests for HD PowerView binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class OnlineCommunicationTest {

    private static final Pattern VALID_IP_V4_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

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

                assertTrue(!scenesData.isEmpty());
                Scene sceneZero = scenesData.get(0);
                assertNotNull(sceneZero);
                sceneId = sceneZero.id;
                assertTrue(sceneId > 0);

                for (Scene scene : scenesData) {
                    String sceneName = scene.getName();
                    assertNotNull(sceneName);
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
                        assertEquals(targetPosition.getState(capabilities, PRIMARY_POSITION),
                                actualPosition.getState(capabilities, PRIMARY_POSITION));
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
}
