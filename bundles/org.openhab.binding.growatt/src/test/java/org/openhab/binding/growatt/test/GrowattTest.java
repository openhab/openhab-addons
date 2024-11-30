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
package org.openhab.binding.growatt.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.net.ssl.SSLSession;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.openhab.binding.growatt.internal.GrowattChannels;
import org.openhab.binding.growatt.internal.GrowattChannels.UoM;
import org.openhab.binding.growatt.internal.cloud.GrowattCloud;
import org.openhab.binding.growatt.internal.config.GrowattBridgeConfiguration;
import org.openhab.binding.growatt.internal.dto.GrottDevice;
import org.openhab.binding.growatt.internal.dto.GrottValues;
import org.openhab.binding.growatt.internal.dto.helper.GrottIntegerDeserializer;
import org.openhab.binding.growatt.internal.dto.helper.GrottValuesHelper;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private final Gson gson = new GsonBuilder().registerTypeAdapter(Integer.class, new GrottIntegerDeserializer())
            .create();

    /**
     * Load a (JSON) string from a file
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    private String load(String fileName) throws FileNotFoundException, IOException {
        try (FileReader file = new FileReader(String.format("src/test/resources/%s.json", fileName));
                BufferedReader reader = new BufferedReader(file)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        }
    }

    /**
     * Load a GrottValues class from a JSON payload.
     *
     * @param fileName the file containing the JSON payload.
     * @return a GrottValues DTO.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private GrottValues loadGrottValues(String fileName) throws FileNotFoundException, IOException {
        String json = load(fileName);
        GrottDevice device = gson.fromJson(json, GrottDevice.class);
        assertNotNull(device);
        GrottValues grottValues = device.getValues();
        assertNotNull(grottValues);
        return grottValues;
    }

    @Test
    void testGrottValuesAccessibility() throws FileNotFoundException, IOException {
        testGrottValuesAccessibility("simple");
        testGrottValuesAccessibility("sph");
        testGrottValuesAccessibility("spf");
        testGrottValuesAccessibility("mid");
        testGrottValuesAccessibility("meter");
    }

    /**
     * For the given JSON file, test that GrottValues implements the same fields as the Map returned from
     * GrowattChannels.getMap(). Test that all fields can be accessed and that they are either null or an Integer
     * instance.
     *
     * @param fileName the name of the JSON file to be tested.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void testGrottValuesAccessibility(String fileName) throws FileNotFoundException, IOException {
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
        List<String> errors = new ArrayList<>();

        for (Entry<String, UoM> entry : GrowattChannels.getMap().entrySet()) {
            String channelId = entry.getKey();
            Field field;
            // test that the field can be accessed
            try {
                field = GrottValues.class.getField(GrottValues.getFieldName(channelId));
            } catch (NoSuchFieldException | SecurityException e) {
                String msg = e.getMessage();
                errors.add(msg != null ? msg : e.getClass().getName());
                continue;
            }
            // test that the field value is either null or an Integer
            try {
                Object value = field.get(grottValues);
                assertTrue(value == null || (value instanceof Integer));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                String msg = e.getMessage();
                errors.add(msg != null ? msg : e.getClass().getName());
                continue;
            }
        }
        if (!errors.isEmpty()) {
            fail(errors.toString());
        }
    }

    /**
     * Spot checks to test that GrottValues is loaded with the correct contents from the "simple" JSON file.
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    @Test
    void testGrottValuesContents() throws FileNotFoundException, IOException, NoSuchFieldException, SecurityException,
            IllegalAccessException, IllegalArgumentException {
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
        channelStates = GrottValuesHelper.getChannelStates(grottValues);

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
    void testJsonFieldsMappedToDto() throws FileNotFoundException, IOException {
        testJsonFieldsMappedToDto("simple");
        testJsonFieldsMappedToDto("sph");
        testJsonFieldsMappedToDto("spf");
        testJsonFieldsMappedToDto("meter");
        testJsonFieldsMappedToDto("mid");
    }

    /**
     * For the given JSON test file name, check that each field in its JSON is mapped to precisely one field in the
     * values DTO.
     *
     * @param fileName the name of the JSON file to be tested.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void testJsonFieldsMappedToDto(String fileName) throws FileNotFoundException, IOException {
        Field[] fields = GrottValues.class.getFields();
        String json = load(fileName);
        JsonParser.parseString(json).getAsJsonObject().get("values").getAsJsonObject().entrySet().forEach(e -> {
            String key = e.getKey();
            assertTrue(GrowattChannels.UNUSED_FIELDS.containsKey(fileName));
            if (!Objects.requireNonNull(GrowattChannels.UNUSED_FIELDS.get(fileName)).contains(key)) {
                JsonObject testJsonObject = new JsonObject();
                testJsonObject.add(key, e.getValue());
                GrottValues testDto = gson.fromJson(testJsonObject, GrottValues.class);
                int mappedFieldCount = 0;
                List<String> errors = new ArrayList<>();
                for (Field field : fields) {
                    try {
                        if (field.get(testDto) != null) {
                            mappedFieldCount++;
                        }
                    } catch (IllegalAccessException | IllegalArgumentException ex) {
                        String msg = ex.getMessage();
                        errors.add(msg != null ? msg : ex.getClass().getName());
                    }
                }
                if (!errors.isEmpty()) {
                    fail(errors.toString());
                }
                assertEquals(1, mappedFieldCount);
            }
        });
    }

    /**
     * Test the Growatt remote cloud API server.
     * Will not run unless actual user credentials are provided.
     *
     * @throws Exception
     */
    @Test
    void testServer() throws Exception {
        GrowattBridgeConfiguration configuration = new GrowattBridgeConfiguration();
        String deviceId = "";

        /*
         * To test on an actual inverter, populate its plant data and user credentials below.
         *
         * configuration.userName = "aa";
         * configuration.password ="bb";
         * deviceId = "cc";
         *
         */

        if (configuration.userName == null) {
            return;
        }

        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client.Client();
        sslContextFactory.setHostnameVerifier((@Nullable String host, @Nullable SSLSession session) -> true);
        sslContextFactory.setValidatePeerCerts(false);

        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.createHttpClient(anyString())).thenReturn(new HttpClient(sslContextFactory));

        try (GrowattCloud api = new GrowattCloud(configuration, httpClientFactory)) {
            Integer programMode = GrowattCloud.ProgramMode.BATTERY_FIRST.ordinal();
            Integer chargingPower = 97;
            Integer targetSOC = 23;
            Boolean allowAcCharging = false;
            String startTime = "01:16";
            String stopTime = "02:17";
            Boolean programEnable = false;
            api.setupBatteryProgram(deviceId, programMode, chargingPower, targetSOC, allowAcCharging, startTime,
                    stopTime, programEnable);
            Map<String, JsonElement> result = api.getDeviceSettings(deviceId);
            assertFalse(result.isEmpty());
            assertEquals(chargingPower, GrowattCloud.mapGetInteger(result, GrowattCloud.CHARGE_PROGRAM_POWER));
            assertEquals(targetSOC, GrowattCloud.mapGetInteger(result, GrowattCloud.CHARGE_PROGRAM_TARGET_SOC));
            assertEquals(allowAcCharging,
                    GrowattCloud.mapGetBoolean(result, GrowattCloud.CHARGE_PROGRAM_ALLOW_AC_CHARGING));
            assertEquals(GrowattCloud.localTimeOf(startTime),
                    GrowattCloud.mapGetLocalTime(result, GrowattCloud.CHARGE_PROGRAM_START_TIME));
            assertEquals(GrowattCloud.localTimeOf(stopTime),
                    GrowattCloud.mapGetLocalTime(result, GrowattCloud.CHARGE_PROGRAM_STOP_TIME));
            assertEquals(programEnable, GrowattCloud.mapGetBoolean(result, GrowattCloud.CHARGE_PROGRAM_ENABLE));

            chargingPower = 100;
            targetSOC = 20;
            allowAcCharging = true;
            startTime = "00:15";
            stopTime = "06:45";
            programEnable = true;
            api.setupBatteryProgram(deviceId, programMode, chargingPower, targetSOC, allowAcCharging, startTime,
                    stopTime, programEnable);
            result = api.getDeviceSettings(deviceId);
            assertFalse(result.isEmpty());
            assertEquals(chargingPower, GrowattCloud.mapGetInteger(result, GrowattCloud.CHARGE_PROGRAM_POWER));
            assertEquals(targetSOC, GrowattCloud.mapGetInteger(result, GrowattCloud.CHARGE_PROGRAM_TARGET_SOC));
            assertEquals(allowAcCharging,
                    GrowattCloud.mapGetBoolean(result, GrowattCloud.CHARGE_PROGRAM_ALLOW_AC_CHARGING));
            assertEquals(GrowattCloud.localTimeOf(startTime),
                    GrowattCloud.mapGetLocalTime(result, GrowattCloud.CHARGE_PROGRAM_START_TIME));
            assertEquals(GrowattCloud.localTimeOf(stopTime),
                    GrowattCloud.mapGetLocalTime(result, GrowattCloud.CHARGE_PROGRAM_STOP_TIME));
            assertEquals(programEnable, GrowattCloud.mapGetBoolean(result, GrowattCloud.CHARGE_PROGRAM_ENABLE));
        }
    }

    @Test
    void testThreePhaseGrottValuesContents() throws FileNotFoundException, IOException, NoSuchFieldException,
            SecurityException, IllegalAccessException, IllegalArgumentException {
        GrottValues grottValues = loadGrottValues("mid");
        assertNotNull(grottValues);

        Map<String, QuantityType<?>> channelStates = GrottValuesHelper.getChannelStates(grottValues);
        assertNotNull(channelStates);
        assertEquals(85, channelStates.size());

        assertEquals(QuantityType.valueOf(-36.5, Units.WATT), channelStates.get("inverter-power"));
        assertEquals(QuantityType.valueOf(11, Units.PERCENT), channelStates.get("battery-soc"));
        assertEquals(QuantityType.valueOf(408.4, Units.VOLT), channelStates.get("grid-voltage-rs"));
        assertEquals(QuantityType.valueOf(326.5, Units.VOLT), channelStates.get("n-bus-voltage"));
        assertEquals(QuantityType.valueOf(404.1, Units.VOLT), channelStates.get("battery-voltage"));
    }

    @Test
    void testMeterGrottValuesContents() throws FileNotFoundException, IOException, NoSuchFieldException,
            SecurityException, IllegalAccessException, IllegalArgumentException {
        GrottValues grottValues = loadGrottValues("meter");
        assertNotNull(grottValues);

        Map<String, QuantityType<?>> channelStates = GrottValuesHelper.getChannelStates(grottValues);
        assertNotNull(channelStates);
        assertEquals(18, channelStates.size());

        assertEquals(QuantityType.valueOf(809.8, Units.WATT), channelStates.get("import-power"));
        assertEquals(QuantityType.valueOf(171.0, Units.WATT), channelStates.get("import-power-s"));
        assertEquals(QuantityType.valueOf(237.4, Units.VOLT), channelStates.get("grid-voltage-s"));
        assertEquals(QuantityType.valueOf(409.5, Units.VOLT), channelStates.get("grid-voltage-rs"));
        assertEquals(QuantityType.valueOf(1.5, Units.AMPERE), channelStates.get("inverter-current-s"));
    }
}
