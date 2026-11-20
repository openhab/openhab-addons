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
package org.openhab.binding.myvaillant.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests merging the API responses into {@link SystemData}.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
class SystemDataTest {

    static JsonObject load(String name) throws IOException {
        try (InputStream stream = SystemDataTest.class.getResourceAsStream(name)) {
            assertNotNull(stream, name);
            return JsonUtil
                    .toSnakeCase(JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)))
                    .getAsJsonObject();
        }
    }

    static SystemData createSystemData() throws IOException {
        JsonObject home = JsonParser.parseString("{\"home_name\":\"Zuhause\",\"system_id\":\"sys\"}").getAsJsonObject();
        return new SystemData("sys", ZoneId.of("Europe/Berlin"), home, load("system.json"), load("current_system.json"),
                load("rts.json"), load("mpc.json"), true, null);
    }

    @Test
    void mergesConfigurationStateAndProperties() throws IOException {
        SystemData data = createSystemData();
        JsonObject system = data.getSystem();
        assertEquals(4.5, JsonUtil.getDouble(system, "outdoor_temperature"));
        assertEquals("VRC720", JsonUtil.getString(system, "controller_type"));
        assertEquals(-26.0, JsonUtil.getDouble(system, "continuous_heating_start_setpoint"));
        assertEquals(41.0, JsonUtil.getDouble(system, "cylinder_temperature_sensor_top_c_h"));
        assertNull(JsonUtil.getInstant(system, "manual_cooling_end_date"));

        JsonObject zone = data.getZones().get(0);
        assertNotNull(zone);
        assertEquals(19.75, JsonUtil.getDouble(zone, "current_room_temperature"));
        assertEquals(0, JsonUtil.getInteger(zone, "associated_circuit_index"));
        assertEquals("MANUAL", JsonUtil.getString(JsonUtil.getObject(zone, "heating"), "operation_mode_heating"));
        assertEquals(Instant.parse("2026-11-20T11:00:00Z"), JsonUtil.getInstant(zone, "quick_veto_end_date_time"));

        JsonObject circuit = data.getCircuits().get(0);
        assertEquals("HEATING", JsonUtil.getString(circuit, "circuit_state"));
        assertEquals(1.0, JsonUtil.getDouble(circuit, "heating_curve"));
        assertEquals("NON", JsonUtil.getString(circuit, "room_temperature_control_mode"));

        JsonObject dhw = data.getDomesticHotWater().get(255);
        assertEquals(48.5, JsonUtil.getDouble(dhw, "current_dhw_temperature"));
        assertEquals(70.0, JsonUtil.getDouble(dhw, "max_setpoint"));
        assertEquals("Zuhause", data.getHomeName());
    }

    @Test
    void skipsInactiveZones() throws IOException {
        assertEquals(List.of(0), List.copyOf(createSystemData().getZones().keySet()));
    }

    @Test
    void attachesStatisticsAndPowerToDevices() throws IOException {
        SystemData data = createSystemData();
        assertEquals(2, data.getDevices().size());

        MyVaillantDevice heatPump = data.getDevice("hp-uuid");
        assertNotNull(heatPump);
        assertEquals("primary_heat_generator", heatPump.type());
        assertEquals("aroTHERM plus", heatPump.getName());
        assertEquals(1234, heatPump.getOnOffCycles());
        assertEquals(90, heatPump.getOperationTimeMinutes());
        assertEquals(850.0, heatPump.getCurrentPower());
        assertEquals(4, heatPump.getEnergyDataTypes().size());
        assertTrue(heatPump.getEnergyDataTypes().contains(new EnergyDataType("HEATING", "EARNED_ENVIRONMENT_ENERGY")));

        MyVaillantDevice backupHeater = data.getDevice("backup-uuid");
        assertNotNull(backupHeater);
        assertEquals("Hydraulic Station", backupHeater.getName());
        assertNull(backupHeater.getOnOffCycles());
        assertEquals(0.0, backupHeater.getCurrentPower());
    }

    @Test
    void energyChannelIds() {
        EnergyDataType type = new EnergyDataType("DOMESTIC_HOT_WATER", "CONSUMED_ELECTRICAL_ENERGY");
        assertEquals("consumed-electrical-energy-domestic-hot-water", type.getChannelId());
        assertEquals("Consumed Electrical Energy Domestic Hot Water Today", type.getLabel());
        assertEquals("heat-generated-heating", new EnergyDataType("HEATING", "HEAT_GENERATED").getChannelId());
    }
}
