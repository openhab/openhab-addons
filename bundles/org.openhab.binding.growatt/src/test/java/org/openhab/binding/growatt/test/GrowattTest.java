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
package org.openhab.binding.growatt.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.openhab.binding.growatt.internal.GrowattChannels;
import org.openhab.binding.growatt.internal.GrowattChannels.UoM;
import org.openhab.binding.growatt.internal.cloud.ApiException;
import org.openhab.binding.growatt.internal.cloud.GrowattCloud;
import org.openhab.binding.growatt.internal.config.GrowattInverterConfiguration;
import org.openhab.binding.growatt.internal.dto.GrottDevice;
import org.openhab.binding.growatt.internal.dto.GrottValues;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link GrowattTest} is a JUnit test suite for the Growatt binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattTest {

    private final Gson gson = new Gson();

    /**
     * Load a (JSON) string from a file
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

    /**
     * Load a GrottValues class from a JSON payload.
     *
     * @param fileName the file containing the JSON payload.
     * @return a GrottValues DTO.
     */
    private GrottValues loadGrottValues(String fileName) {
        String json = load(fileName);
        GrottDevice device = gson.fromJson(json, GrottDevice.class);
        assertNotNull(device);
        GrottValues grottValues = device.getValues();
        assertNotNull(grottValues);
        return grottValues;
    }

    @Test
    void testGrottValuesAccessibility() {
        testGrottValuesAccessibility("simple");
        testGrottValuesAccessibility("sph");
    }

    /**
     * For the given JSON file, test that GrottValues implements the same fields as the Map returned from
     * GrowattChannels.getMap(). Test that all fields can be accessed and that they are either null or an Integer
     * instance.
     *
     * @param fileName the name of the JSON file to be tested.
     */
    private void testGrottValuesAccessibility(String fileName) {
        GrottValues grottValues = loadGrottValues(fileName);

        List<String> fields = Arrays.asList(GrottValues.class.getFields()).stream().map(f -> f.getName())
                .collect(Collectors.toList());

        // test that the GrottValues DTO has identical field names to the CHANNEL_ID_UOM_MAP channel ids
        for (String channel : GrowattChannels.getMap().keySet()) {
            assertTrue(fields.contains(GrottValues.getFieldName(channel)));
        }

        // test that the CHANNEL_ID_UOM_MAP has identical channel ids to the GrottValues DTO field names
        for (String field : fields) {
            assertTrue(GrowattChannels.getMap().containsKey(GrottValues.getChannelId(field)));
        }

        // test that the CHANNEL_ID_UOM_MAP and the GrottValues DTO have the same number of fields resp. channel ids
        assertEquals(fields.size(), GrowattChannels.getMap().size());

        for (Entry<String, UoM> entry : GrowattChannels.getMap().entrySet()) {
            String channelId = entry.getKey();
            Field field;
            // test that the field can be accessed
            try {
                field = GrottValues.class.getField(GrottValues.getFieldName(channelId));
            } catch (NoSuchFieldException e) {
                fail(e.getMessage());
                continue;
            } catch (SecurityException e) {
                fail(e.getMessage());
                continue;
            }
            // test that the field value is either null or an Integer
            try {
                Object value = field.get(grottValues);
                assertTrue(value == null || (value instanceof Integer));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                fail(e.getMessage());
                continue;
            }
        }
    }

    /**
     * Spot checks to test that GrottValues is loaded with the correct contents from the "simple" JSON file.
     */
    @Test
    void testGrottValuesContents() {
        GrottValues grottValues = loadGrottValues("simple");

        assertEquals(1, grottValues.system_status);
        assertEquals(1622, grottValues.pv_power);
        assertEquals(4997, grottValues.grid_frequency);
        assertEquals(2353, grottValues.grid_voltage_r);
        assertEquals(7, grottValues.inverter_current_r);
        assertEquals(1460, grottValues.inverter_power);
        assertEquals(1460, grottValues.inverter_power_r);
        assertEquals(273, grottValues.pv_temperature);
        assertEquals(87, grottValues.inverter_energy_today);
        assertEquals(43265, grottValues.inverter_energy_total);
        assertEquals(90, grottValues.pv1_energy_today);
        assertEquals(45453, grottValues.pv1_energy_total);
        assertEquals(45453, grottValues.pv_energy_total);
        assertEquals(0, grottValues.pv2_voltage);
        assertEquals(0, grottValues.pv2_current);
        assertEquals(0, grottValues.pv2_power);
        assertEquals(65503878, grottValues.total_work_time);

        Map<String, QuantityType<?>> channelStates = null;
        try {
            channelStates = grottValues.getChannelStates();
        } catch (NoSuchFieldException e) {
            fail(e.getMessage());
        } catch (SecurityException e) {
            fail(e.getMessage());
        } catch (IllegalAccessException e) {
            fail(e.getMessage());
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }

        assertNotNull(channelStates);
        assertEquals(29, channelStates.size());

        channelStates.forEach((channelId, state) -> {
            assertTrue(state instanceof QuantityType<?>);
        });

        assertEquals(QuantityType.ONE, channelStates.get("system-status"));
        assertEquals(QuantityType.valueOf(162.2, Units.WATT), channelStates.get("pv-power"));
        assertEquals(QuantityType.valueOf(49.97, Units.HERTZ), channelStates.get("grid-frequency"));
        assertEquals(QuantityType.valueOf(235.3, Units.VOLT), channelStates.get("grid-voltage-r"));
        assertEquals(QuantityType.valueOf(0.7, Units.AMPERE), channelStates.get("inverter-current-r"));
        assertEquals(QuantityType.valueOf(146, Units.WATT), channelStates.get("inverter-power"));
        assertEquals(QuantityType.valueOf(146, Units.WATT), channelStates.get("inverter-power-r"));
        assertEquals(QuantityType.valueOf(27.3, SIUnits.CELSIUS), channelStates.get("pv-temperature"));
        assertEquals(QuantityType.valueOf(8.7, Units.KILOWATT_HOUR), channelStates.get("inverter-energy-today"));
        assertEquals(QuantityType.valueOf(4326.5, Units.KILOWATT_HOUR), channelStates.get("inverter-energy-total"));
        assertEquals(QuantityType.valueOf(9, Units.KILOWATT_HOUR), channelStates.get("pv1-energy-today"));
        assertEquals(QuantityType.valueOf(4545.3, Units.KILOWATT_HOUR), channelStates.get("pv1-energy-total"));
        assertEquals(QuantityType.valueOf(4545.3, Units.KILOWATT_HOUR), channelStates.get("pv-energy-total"));
        assertEquals(QuantityType.valueOf(0, Units.VOLT), channelStates.get("pv2-voltage"));
        assertEquals(QuantityType.valueOf(0, Units.AMPERE), channelStates.get("pv2-current"));
        assertEquals(QuantityType.valueOf(0, Units.WATT), channelStates.get("pv2-power"));
        State state = channelStates.get("total-work-time");
        assertTrue(state instanceof QuantityType<?>);
        if (state instanceof QuantityType<?>) {
            QuantityType<?> seconds = ((QuantityType<?>) state).toUnit(Units.SECOND);
            assertNotNull(seconds);
            assertEquals(QuantityType.valueOf(32751939, Units.SECOND).doubleValue(), seconds.doubleValue(), 0.1);
        }

        assertNull(channelStates.get("aardvark"));
    }

    @Test
    void testJsonFieldsMappedToDto() {
        testJsonFieldsMappedToDto("simple");
        testJsonFieldsMappedToDto("sph");
    }

    /**
     * For the given JSON test file name, check that each field in its JSON is mapped to precisely one field in the
     * values DTO.
     *
     * @param fileName the name of the JSON file to be tested.
     */
    private void testJsonFieldsMappedToDto(String fileName) {
        Field[] fields = GrottValues.class.getFields();
        String json = load(fileName);
        JsonParser.parseString(json).getAsJsonObject().get("values").getAsJsonObject().entrySet().forEach(e -> {
            String key = e.getKey();
            if (!"datalogserial".equals(key) && !"pvserial".equals(key)) {
                JsonObject testJsonObject = new JsonObject();
                testJsonObject.add(key, e.getValue());
                GrottValues testDto = gson.fromJson(testJsonObject, GrottValues.class);
                int mappedFieldCount = 0;
                for (Field field : fields) {
                    try {
                        if (field.get(testDto) != null) {
                            mappedFieldCount++;
                        }
                    } catch (IllegalAccessException | IllegalArgumentException ex) {
                        fail("Exception");
                    }
                }
                assertEquals(1, mappedFieldCount);
            }
        });
    }

    /**
     * Test the Growatt remote cloud API server.
     * Will not run unless actual user credentials are provided.
     */
    @Test
    void testServer() {
        GrowattInverterConfiguration configuration = new GrowattInverterConfiguration();
        /*
         * To test on an actual inverter, populate its plant data and user credentials below.
         *
         * configuration.deviceId = "aa";
         * configuration.userName = "bb";
         * configuration.password ="cc";
         * configuration.userId = "dd";
         * configuration.plantId = "ee";
         */

        if (configuration.userName == null) {
            return;
        }

        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.createHttpClient(anyString(), any(SslContextFactory.Client.class)))
                .thenReturn(new HttpClient(new SslContextFactory.Client(true)));

        try (GrowattCloud api = new GrowattCloud(configuration, httpClientFactory)) {
            try {
                assertFalse(api.getPlantList().isEmpty());
            } catch (ApiException e) {
                fail(e);
            }
            try {
                assertFalse(api.getPlantInfo().isEmpty());
            } catch (ApiException e) {
                fail(e);
            }

            int chargingPower = 97;
            int targetSOC = 23;
            boolean allowAcCharging = false;
            LocalTime startTime = LocalTime.of(1, 16);
            LocalTime stopTime = LocalTime.of(2, 17);
            boolean programEnable = false;
            try {
                assertFalse(api.setupChargingProgram(chargingPower, targetSOC, allowAcCharging, startTime, stopTime,
                        programEnable).isEmpty());
            } catch (ApiException e) {
                fail(e);
            }
            try {
                Map<String, JsonElement> result = api.getMixAllSettings();
                assertFalse(result.isEmpty());
                assertEquals(chargingPower, GrowattCloud.mapGetInteger(result, GrowattCloud.CHARGE_PROGRAM_POWER));
                assertEquals(targetSOC, GrowattCloud.mapGetInteger(result, GrowattCloud.CHARGE_PROGRAM_TARGET_SOC));
                assertEquals(allowAcCharging,
                        GrowattCloud.mapGetBoolean(result, GrowattCloud.CHARGE_PROGRAM_ALLOW_AC_CHARGING));
                assertEquals(startTime, GrowattCloud.mapGetLocalTime(result, GrowattCloud.CHARGE_PROGRAM_START_TIME));
                assertEquals(stopTime, GrowattCloud.mapGetLocalTime(result, GrowattCloud.CHARGE_PROGRAM_STOP_TIME));
                assertEquals(programEnable, GrowattCloud.mapGetBoolean(result, GrowattCloud.CHARGE_PROGRAM_ENABLE));
            } catch (ApiException e) {
                fail(e);
            }

            chargingPower = 100;
            targetSOC = 20;
            allowAcCharging = true;
            startTime = LocalTime.of(0, 15);
            stopTime = LocalTime.of(6, 45);
            programEnable = true;
            try {
                assertFalse(api.setupChargingProgram(chargingPower, targetSOC, allowAcCharging, startTime, stopTime,
                        programEnable).isEmpty());
            } catch (ApiException e) {
                fail(e);
            }
            try {
                Map<String, JsonElement> result = api.getMixAllSettings();
                assertFalse(result.isEmpty());
                assertEquals(chargingPower, GrowattCloud.mapGetInteger(result, GrowattCloud.CHARGE_PROGRAM_POWER));
                assertEquals(targetSOC, GrowattCloud.mapGetInteger(result, GrowattCloud.CHARGE_PROGRAM_TARGET_SOC));
                assertEquals(allowAcCharging,
                        GrowattCloud.mapGetBoolean(result, GrowattCloud.CHARGE_PROGRAM_ALLOW_AC_CHARGING));
                assertEquals(startTime, GrowattCloud.mapGetLocalTime(result, GrowattCloud.CHARGE_PROGRAM_START_TIME));
                assertEquals(stopTime, GrowattCloud.mapGetLocalTime(result, GrowattCloud.CHARGE_PROGRAM_STOP_TIME));
                assertEquals(programEnable, GrowattCloud.mapGetBoolean(result, GrowattCloud.CHARGE_PROGRAM_ENABLE));
            } catch (ApiException e) {
                fail(e);
            }
        } catch (Exception e) {
            fail(e);
        }
    }
}
