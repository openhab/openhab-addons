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

import java.io.IOException;
import java.util.regex.Pattern;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * Unit tests
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewJUnitTests {

    public static final Pattern VALID_IP_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    public class Logger implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            System.out.println(requestContext.getMethod() + " " + requestContext.getUri());
            if (requestContext.hasEntity()) {
                System.out.println(">> " + requestContext.getEntity().toString());
            }
        }
    }

    /*
     * NOTE: in order to actually run these physical tests you must (obviously) have
     * a hub physically available, and its IP address must be correctly configured
     * in the "hubIPAddress" string constant below..
     */
    @Test
    public void testCommunicationWithHub() {
        String hubIPAddress = "192.168.1.xxx"; // e.g. "192.168.1.123"

        if (VALID_IP_ADDRESS.matcher(hubIPAddress).matches()) {
            Client client = ClientBuilder.newClient();
            assertNotNull(client);
            client.register(new Logger());

            HDPowerViewWebTargets webTargets = new HDPowerViewWebTargets(client, hubIPAddress);
            assertNotNull(webTargets);

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
                for (ShadeData shadeData : shades.shadeData) {
                    String shadeName = shadeData.getName();
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
            
            @Nullable
            Shade shade = null;
            try {
                assertNotNull(shadeId);
                shade = webTargets.refreshShade(shadeId);
                assertNotNull(shade);
            } catch (ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            try {
                assertNotNull(shadeId);
                assertNotNull(shade);
                ShadePositionKind kind = shade.shade.positions.getPosKind();
                assertNotNull(kind);
                int position = shade.shade.positions.getPercent(kind).intValue();
                position = position + ((position <= 10) ? 5 : -5);
                ShadePosition newPos = ShadePosition.create(kind, position);
                assertNotNull(newPos);
                shade = webTargets.moveShade(shadeId, newPos);
                assertNotNull(shade);
            } catch (ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }

            try {
                assertNotNull(sceneId);
                webTargets.activateScene(sceneId);
            } catch (ProcessingException | HubMaintenanceException e) {
                fail(e.getMessage());
            }
        }
    }
}
