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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusSys;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RelayStatus;
import org.openhab.binding.shelly.internal.util.ShellyUtils;

import com.google.gson.Gson;

/**
 * Tests the JSON mapping of the Wall Display specific components of {@link Shelly2DeviceStatusResult}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2WallDisplayStatusJsonTest {

    private static final String DEVICE_STATUS_PLAYING = """
            {"ble":{"mac":"00:00:46:B8:EB:41"},"cloud":{"connected":true},"mqtt":{"connected":false},
            "temperature:0":{"id":0,"tC":26.2,"tF":79},"humidity:0":{"id":0,"rh":59},
            "illuminance:0":{"id":0,"lux":5,"illumination":"twilight"},
            "switch:0":{"id":0,"output":false,"source":"Auto power on (restore_last)"},
            "input:0":{"id":0,"state":false},
            "sys":{"id":"ShellyWallDisplay-000822525745","mac":"000822525745","model":"SAWD-0A1XX10EU1","gen":2,
            "fw_id":"20260803-150127/2.7.4-b7b6bc07","ver":"2.7.4","app":"WallDisplay","auth_en":false,
            "uptime":352884,"restart_required":false,"unixtime":1788166009,
            "relay_in_thermostat":false,"relays_in_thermostat":[],"sensor_in_thermostat":false,
            "awaiting_auth_code":false,"ch":["switch:0"]},
            "wifi":{"sta_ip":"192.168.60.115","status":"got ip","ssid":"MiOt","rssi":-55},
            "media":{"playback":{"enable":true,"buffering":false,"volume":6,"media_type":"RADIO",
            "media_meta":{"title":"Radio Regenbogen 2","thumb":"https://cdn.example.com/logo.png"}},
            "total_size":3885854,"total_size_h":"3.706 MB",
            "item_counts":{"audio":0,"alert":0,"ringtone":10,"photo":0,"video":0}},
            "devicepower:0":{"id":0,"external":{"present":true}},
            "devicepower:1":{"id":1,"device":"XB61819872196631","battery":{"percent":99}}}
            """;

    private static final String DEVICE_STATUS_PAUSED = DEVICE_STATUS_PLAYING
            .replace("\"mqtt\":{\"connected\":false}", "\"mqtt\":{\"connected\":true}")
            .replace("\"playback\":{\"enable\":true", "\"playback\":{\"enable\":false");

    private final Gson gson = new Gson();

    @Test
    void thermostatUsageFlagsAndSecondPowerSourceAreMapped() throws ShellyApiException {
        String json = """
                {"sys":{"relay_in_thermostat":true,"relays_in_thermostat":[],"sensor_in_thermostat":false},
                "devicepower:0":{"id":0,"external":{"present":true}},
                "devicepower:1":{"id":1,"battery":{"V":2.9,"percent":74}}}
                """;

        Shelly2DeviceStatusResult status = ShellyUtils.fromJson(gson, json, Shelly2DeviceStatusResult.class);
        Shelly2DeviceStatusSys sys = status.sys;
        assertNotNull(sys);
        assertThat(sys.relayInThermostat, is(equalTo(Boolean.TRUE)));
        assertThat(sys.sensorInThermostat, is(equalTo(Boolean.FALSE)));

        Shelly2DeviceStatusPower power1 = status.devicepower1;
        assertNotNull(power1);
        assertNotNull(power1.battery);
        assertThat(power1.battery.percent, is(equalTo(74.0)));
    }

    @Test
    void statusWithoutSecondPowerSourceLeavesFieldsNull() throws ShellyApiException {
        Shelly2DeviceStatusResult status = ShellyUtils.fromJson(gson, "{\"switch:0\":{\"id\":0,\"output\":true}}",
                Shelly2DeviceStatusResult.class);

        assertThat(status.sys, is(nullValue()));
        assertThat(status.devicepower1, is(nullValue()));
    }

    @Test
    void deviceStatusWithoutThermostatReportsUsageFlagsFalse() throws ShellyApiException {
        Shelly2DeviceStatusResult status = ShellyUtils.fromJson(gson, DEVICE_STATUS_PAUSED,
                Shelly2DeviceStatusResult.class);

        Shelly2DeviceStatusSys sys = status.sys;
        assertNotNull(sys);
        assertThat(sys.relayInThermostat, is(equalTo(Boolean.FALSE)));
        assertThat(sys.sensorInThermostat, is(equalTo(Boolean.FALSE)));
    }

    @Test
    void deviceStatusReportsAttachedSensorBatteryButNoPowerMeter() throws ShellyApiException {
        Shelly2DeviceStatusResult status = ShellyUtils.fromJson(gson, DEVICE_STATUS_PLAYING,
                Shelly2DeviceStatusResult.class);

        Shelly2DeviceStatusPower power1 = status.devicepower1;
        assertNotNull(power1);
        assertNotNull(power1.battery);
        assertThat(power1.battery.percent, is(equalTo(99.0)));

        Shelly2RelayStatus relay = status.switch0;
        assertNotNull(relay);
        assertThat(relay.output, is(equalTo(Boolean.FALSE)));
        assertThat(relay.apower, is(nullValue()));
        assertThat(relay.aenergy, is(nullValue()));
        assertThat(relay.voltage, is(nullValue()));
        assertThat(relay.current, is(nullValue()));
    }
}
