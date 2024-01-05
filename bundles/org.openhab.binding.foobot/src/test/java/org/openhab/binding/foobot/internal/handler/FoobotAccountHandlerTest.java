/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.foobot.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.foobot.internal.FoobotApiConnector;
import org.openhab.binding.foobot.internal.FoobotApiException;
import org.openhab.binding.foobot.internal.json.FoobotDevice;
import org.openhab.core.thing.Bridge;

/**
 * Unit test for {@link FoobotAccountHandler}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class FoobotAccountHandlerTest {

    private @Mock Bridge bridge;
    private final FoobotApiConnector connector = new FoobotApiConnector() {
        @Override
        protected String request(String url, String apiKey) throws FoobotApiException {
            try (InputStream stream = getClass().getResourceAsStream("../devices.json")) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new AssertionError(e.getMessage());
            }
        }
    };
    private final FoobotAccountHandler handler = new FoobotAccountHandler(bridge, connector);

    @Test
    public void testSensorDataToState() throws IOException, FoobotApiException {
        final List<FoobotDevice> deviceList = handler.getDeviceList();

        assertFalse(deviceList.isEmpty(), "Device list should not return empty");
        assertEquals(deviceList.get(0).getUuid(), "1234567890ABCDEF");
    }
}
