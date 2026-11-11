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
import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.handler.JsonUtils;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link HeatPumpCapabilityTest} verifies that an AWHP (Heat Pump) capability descriptor, which the binding treats
 * as an AC sub-type, is correctly recognized and parsed.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class HeatPumpCapabilityTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createHeatPumpCapability() throws IOException, LGThinqException {
        ClassLoader classLoader = JsonUtils.class.getClassLoader();
        assertNotNull(classLoader);
        try (InputStream inputStream = classLoader.getResourceAsStream("thinq-heatpump-v2-cap.json")) {
            assertNotNull(inputStream);
            JsonNode rootNode = objectMapper.readTree(inputStream);
            ACCapability cap = CapabilityFactory.getInstance().create(rootNode, ACCapability.class);
            assertNotNull(cap);

            // an AWHP must be recognized as a HEAT_PUMP and parsed through the V2 factory
            assertEquals(DeviceTypes.HEAT_PUMP, cap.getDeviceType());
            assertEquals(LGAPIVerion.V2_0, cap.getDeviceVersion());

            // supported operation modes must be resolved to their device command keys
            assertEquals(CAP_ACHP_OP_MODE_COOL_KEY, cap.getOpMode().get("0"));
            assertEquals(CAP_ACHP_OP_MODE_HEAT_KEY, cap.getOpMode().get("4"));
        }
    }
}
