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

import java.util.regex.Pattern;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.Shade;

import com.google.gson.JsonParseException;

/**
 * Unit tests
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewJUnitTests {

    public static final Pattern VALID_IP_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    /*
     * NOTE: in order to actually run these physical tests you must (obviously) have
     * a hub physically available, and its IP address must be correctly configured
     * in the "hubIPAddress" string constant below..
     */
    @Test
    public void testCommunicationWithHub() {
        String hubIPAddress = "192.168.1.xxx"; // e.g. "192.168.1.123"

        if (VALID_IP_ADDRESS.matcher(hubIPAddress).matches()) {
            HDPowerViewWebTargets webTargets = new HDPowerViewWebTargets(ClientBuilder.newClient(), hubIPAddress);

            String shadeId = null;
            ShadePosition shadePos = null;
            try {
                Shades shades = webTargets.getShades();
                assertNotNull(shades);
                assertTrue(shades.shadeData.size() > 0);
                assertTrue(shades.shadeData.get(0).getName().length() > 0);
                shadePos = shades.shadeData.get(0).positions;
                assertNotNull(shadePos);
                shadeId = shades.shadeData.get(0).id;
                assertNotNull(shadeId);
                for (Shade shade : shades.shadeData) {
                    String shadeName = shade.getName();
                    assertNotNull(shadeName);
                }
            } catch (JsonParseException | ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

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

            try {
                assertNotNull(shadeId);
                assertNotNull(shadePos);
                // TODO shadePos.position1 = shadePos.position1 + ((shadePos.position1 <= 1000) ? 1000 : -1000);
                webTargets.moveShade(shadeId, shadePos);
            } catch (ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            try {
                webTargets.activateScene(sceneId);
            } catch (ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }
        }
    }
}
