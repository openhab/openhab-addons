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
package org.openhab.binding.hue.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip1.BridgeConfig;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.UpdateStatusV2;

import com.google.gson.Gson;

/**
 * JUnit test for v1 API Bridge configuration DTO.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class BridgeConfigDtoTest {

    private static final Gson GSON = new Gson();

    /**
     * Load the test JSON payload string from a file
     */
    private String load(String fileName) {
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
    void testBridgeConfig() {
        String json = load("bridge_v1");
        BridgeConfig bridgeConfig = GSON.fromJson(json, BridgeConfig.class);
        assertNotNull(bridgeConfig);
        assertEquals("2071280000", bridgeConfig.getSoftwareVersion());
        Map<String, @Nullable UpdateStatusV2> map = bridgeConfig.getUpdateStatusMap();
        assertEquals(2, map.size());
        assertEquals(UpdateStatusV2.NO_UPDATE, map.get("bridge"));
    }
}
