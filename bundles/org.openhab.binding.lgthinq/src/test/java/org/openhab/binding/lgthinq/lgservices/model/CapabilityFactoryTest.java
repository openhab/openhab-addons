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
package org.openhab.binding.lgthinq.lgservices.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.handler.JsonUtils;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link CapabilityFactoryTest}
 *
 * @author Nemer Daud - Initial contribution
 */
class CapabilityFactoryTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void create() throws IOException, LGThinqException {
        ClassLoader classLoader = JsonUtils.class.getClassLoader();
        assertNotNull(classLoader);
        URL fileUrl = classLoader.getResource("thinq-washer-v2-cap.json");
        assertNotNull(fileUrl);
        File capFile = new File(fileUrl.getFile());
        JsonNode mapper = objectMapper.readTree(capFile);
        WasherDryerCapability wpCap = (WasherDryerCapability) CapabilityFactory.getInstance().create(mapper,
                WasherDryerCapability.class);
        assertNotNull(wpCap);
        assertEquals(40, wpCap.getCourses().size());
        assertTrue(wpCap.getRinseFeat().getValuesMapping().size() > 1);
        assertTrue(wpCap.getSpinFeat().getValuesMapping().size() > 1);
        assertTrue(wpCap.getSoilWash().getValuesMapping().size() > 1);
        assertTrue(wpCap.getTemperatureFeat().getValuesMapping().size() > 1);
        assertTrue(wpCap.hasDoorLook());
    }
}
