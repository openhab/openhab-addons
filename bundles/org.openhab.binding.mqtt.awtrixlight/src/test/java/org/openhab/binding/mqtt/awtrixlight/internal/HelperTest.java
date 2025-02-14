/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.awtrixlight.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.awtrixlight.internal.app.AwtrixApp;

/**
 * Test cases for the {@link Helper} service.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class HelperTest {

    @Test
    public void convertJson() {
        String message = "{\"bat\":100,\"bat_raw\":670,\"type\":0,\"lux\":958,\"ldr_raw\":975,\"ram\":148568,\"bri\":10,\"temp\":24,\"hum\":41,\"uptime\":50092,\"wifi_signal\":-53,\"messages\":2,\"version\":\"0.83\",\"indicator1\":false,\"indicator2\":false,\"indicator3\":false,\"app\":\"Temperature\"}";

        Map<String, Object> convertedJson = Helper.decodeStatsJson(message);
        assertEquals(0, 17 - convertedJson.size());

        String[] statsProperties = { FIELD_BRIDGE_BATTERY, FIELD_BRIDGE_BATTERY_RAW, FIELD_BRIDGE_FIRMWARE,
                FIELD_BRIDGE_TYPE, FIELD_BRIDGE_LUX, FIELD_BRIDGE_LDR_RAW, FIELD_BRIDGE_RAM, FIELD_BRIDGE_BRIGHTNESS,
                FIELD_BRIDGE_TEMPERATURE, FIELD_BRIDGE_HUMIDITY, FIELD_BRIDGE_UPTIME, FIELD_BRIDGE_WIFI_SIGNAL,
                FIELD_BRIDGE_MESSAGES, FIELD_BRIDGE_INDICATOR1, FIELD_BRIDGE_INDICATOR2, FIELD_BRIDGE_INDICATOR3,
                FIELD_BRIDGE_APP };

        for (String s : statsProperties) {
            assertTrue(convertedJson.containsKey(s));
            String[] stringProperties = { FIELD_BRIDGE_APP, FIELD_BRIDGE_FIRMWARE };
            String[] booleanProperties = { FIELD_BRIDGE_INDICATOR1, FIELD_BRIDGE_INDICATOR2, FIELD_BRIDGE_INDICATOR3 };
            if (Arrays.stream(stringProperties).anyMatch(s::equals)) {
                Object prop = convertedJson.get(s);
                assertNotNull(prop);
                assertEquals(String.class, prop.getClass());
            } else if (Arrays.stream(booleanProperties).anyMatch(s::equals)) {
                Object prop = convertedJson.get(s);
                assertNotNull(prop);
                assertEquals(Boolean.class, prop.getClass());
            } else {
                Object prop = convertedJson.get(s);
                assertNotNull(prop);
                assertEquals(BigDecimal.class, prop.getClass());
            }
        }
    }

    @Test
    public void encodeJson() {
        HashMap<String, Object> inputMap = new HashMap<>();
        inputMap.put("Test1", "Test1");
        inputMap.put("Test2", 100);
        inputMap.put("Test3", -100);
        inputMap.put("Test4", true);
        inputMap.put("Test5", false);

        String json = Helper.encodeJson(inputMap);

        assertTrue(json.contains("{"));
        assertTrue(json.contains("\"Test1\":\"Test1\""));
        assertTrue(json.contains("\"Test2\":100"));
        assertTrue(json.contains("\"Test3\":-100"));
        assertTrue(json.contains("\"Test4\":true"));
        assertTrue(json.contains("\"Test5\":false"));
        assertTrue(json.contains("}"));
        assertEquals(4, json.chars().filter(ch -> ch == ',').count());
    }

    @Test
    public void convertImage() {
        String imageMessage = "[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14276351,0,0,0,0,0,0,0,0,0,0,0,16777215,0,16777215,0,0,16777215,0,0,16777215,0,16777215,0,0,0,0,0,0,0,0,16777215,13094911,9017343,0,0,0,0,0,0,0,0,0,0,16777215,0,16777215,0,16777215,16777215,0,0,0,0,16777215,0,0,0,0,0,0,0,16777215,14276351,13094911,9017343,6582015,0,0,0,0,0,0,0,0,0,16777215,16777215,16777215,0,0,16777215,0,0,0,16777215,0,0,0,0,0,0,0,0,14276351,13094911,13094911,6582015,6582015,0,0,0,0,0,0,0,0,0,0,0,16777215,0,0,16777215,0,0,16777215,0,0,0,0,0,0,0,0,0,14276351,9017343,9017343,6582015,3030679,0,0,0,0,0,0,0,0,0,0,0,16777215,0,16777215,16777215,16777215,0,16777215,0,16777215,0,0,0,0,0,0,0,0,6582015,6582015,3030679,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]";
        Helper.decodeImage(imageMessage);
    }

    @Test
    public void mappingRoundTrip() {
        AwtrixApp app = new AwtrixApp();
        String appConfig = app.getAppConfig();

        Map<String, Object> convertJson = Helper.decodeAppJson(appConfig).getAppParams();
        String appConfig2 = Helper.encodeJson(convertJson);
        assertEquals(appConfig, appConfig2);

        app.updateFields(convertJson);
        String appConfig3 = app.getAppConfig();
        assertEquals(appConfig, appConfig3);

        app.setText("Test");
        // app.setRepeat(new BigDecimal(10));
        BigDecimal[] color = { new BigDecimal(255), new BigDecimal(255), new BigDecimal(255) };
        app.setColor(color);
        app.setEffect("Radar");
        app.setEffectSpeed(new BigDecimal(80));
        String appConfig4 = app.getAppConfig();
        assertNotEquals(appConfig, appConfig4);

        AwtrixApp app2 = new AwtrixApp();
        Map<String, Object> convertJson4 = Helper.decodeAppJson(appConfig4).getAppParams();
        app2.updateFields(convertJson4);
        String appConfig5 = app2.getAppConfig();
        assertEquals(appConfig4, appConfig5);

        app.updateFields(convertJson);
        String appConfig6 = app.getAppConfig();
        assertEquals(appConfig, appConfig6);
    }

    @Test
    public void trimArray() {
        BigDecimal[] untrimmed = { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN, new BigDecimal(1000) };
        BigDecimal[] trimmed = Helper.leftTrim(untrimmed, 2);

        assertTrue(trimmed.length == 2);
        assertEquals(trimmed[0], untrimmed[untrimmed.length - 2]);
        assertEquals(trimmed[1], untrimmed[untrimmed.length - 1]);
    }
}
