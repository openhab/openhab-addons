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
package org.openhab.binding.lgthinq.lgservices.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherCapability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class to validate DishWasher capability detection.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class DishWasherCapabilityTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDishWasherCapabilityFormat() throws IOException, LGThinqException {
        ClassLoader classLoader = getClass().getClassLoader();
        assertNotNull(classLoader);
        try (InputStream inputStream = classLoader.getResourceAsStream("thinq-dishwasher.json")) {
            assertNotNull(inputStream, "thinq-dishwasher.json not found in resources");
            JsonNode mapper = objectMapper.readTree(inputStream);
            DishWasherCapability dwCap = CapabilityFactory.getInstance().create(mapper, DishWasherCapability.class);
            assertNotNull(dwCap);
            assertEquals(DeviceTypes.DISH_WASHER, dwCap.getDeviceType());
            assertEquals(LGAPIVerion.V2_0, dwCap.getDeviceVersion());
            // This is the critical assertion
            assertEquals(MonitoringResultFormat.BINARY_FORMAT, dwCap.getMonitoringDataFormat(),
                    "DishWasher with BINARY(BYTE) in Monitoring/type should be identified as BINARY_FORMAT");
        }
    }
}
