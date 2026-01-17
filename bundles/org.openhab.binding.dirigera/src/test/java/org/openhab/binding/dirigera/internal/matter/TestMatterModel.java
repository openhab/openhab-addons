/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.matter;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.THING_TYPE_MATTER_ENVIRONMENT_SENSOR;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.FileReader;
import org.openhab.binding.dirigera.internal.ResourceReader;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;

/**
 * {@link TestMatterModel} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestMatterModel {

    @BeforeEach
    void setup() {
        // Make sure to use the ResourceReader as ResourceProvider
        ResourceReader.setProvider(new FileReader());
    }

    @Test
    void testThingTypes() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        // TIMMERFLOTTE environment sensor
        String timmerflotteId = "94f3d9d7-95ee-496d-9b83-2d5de9a7c2c1_1";
        ThingTypeUID environmentSensorId = gateway.model().identifyDeviceFromModel(timmerflotteId);
        assertEquals(THING_TYPE_MATTER_ENVIRONMENT_SENSOR, environmentSensorId, "TIMMERFLOTTE TTUID");
    }
}
