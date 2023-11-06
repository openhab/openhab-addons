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
package org.openhab.binding.foobot.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.foobot.internal.FoobotApiConnector;
import org.openhab.binding.foobot.internal.FoobotApiException;
import org.openhab.binding.foobot.internal.json.FoobotJsonData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Thing;

/**
 * Unit test for {@link FoobotDeviceHandler}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class FoobotDeviceHandlerTest {

    private @Mock Thing thing;
    private final FoobotApiConnector connector = new FoobotApiConnector() {
        @Override
        protected String request(String url, String apiKey) throws FoobotApiException {
            try (InputStream stream = getClass().getResourceAsStream("../sensors.json")) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new AssertionError(e.getMessage());
            }
        };
    };
    private final FoobotDeviceHandler handler = new FoobotDeviceHandler(thing, connector);

    @Test
    public void testSensorDataToState() throws IOException, FoobotApiException {
        final FoobotJsonData sensorData = connector.getSensorData("1234");

        assertNotNull(sensorData, "No sensor data read");
        Objects.requireNonNull(sensorData);
        assertEquals(handler.sensorDataToState("temperature", sensorData), new QuantityType(12.345, SIUnits.CELSIUS));
        assertEquals(handler.sensorDataToState("gpi", sensorData), new DecimalType(5.6789012));
    }
}
