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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatusTemp;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;

import com.google.gson.Gson;

/**
 * Tests for the Gen2 addon sensor read-error path in {@link Shelly2ApiClient}.
 *
 * <p>
 * Verifies that when a DS18B20 or AM2301 addon sensor reports {@code "errors":["read"]} (firmware
 * sends {@code "tC":null} alongside the error), the binding:
 * <ul>
 * <li>correctly deserialises the {@code errors} array from the DTO,</li>
 * <li>leaves {@link ShellySettingsStatus#extTemperature} / {@code extHumidity} / {@code extVoltage}
 * null so that no channel update is emitted and {@code sensors#lastUpdate} is not advanced.</li>
 * </ul>
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
public class Shelly2AddonStatusTest {

    private static final String LOCAL_IP = "192.168.1.50";
    private static final String DEVICE_IP = "192.168.1.100";

    // ── helpers ──────────────────────────────────────────────────────────────────

    private ShellyApiConfiguration testConfig() {
        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, LOCAL_IP));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
        return new ShellyApiConfiguration(bindingConfig, "test-realm", DEVICE_IP);
    }

    private static NetworkAddressService nullNas() {
        return new NetworkAddressService() {
            @Override
            public @Nullable String getPrimaryIpv4HostAddress() {
                return null;
            }

            @Override
            public @Nullable String getConfiguredBroadcastAddress() {
                return null;
            }

            @Override
            public boolean isUseOnlyOneAddress() {
                return false;
            }

            @Override
            public boolean isUseIPv6() {
                return false;
            }

            @Override
            public void addNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }

            @Override
            public void removeNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }
        };
    }

    private ShellyThingInterface mockRelayThing() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.isSensor = false;
        profile.hasBattery = false;
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(profile);
        when(handler.areChannelsCreated()).thenReturn(false);
        when(handler.updateChannel(anyString(), anyString(), any())).thenReturn(false);
        return handler;
    }

    /**
     * Testable subclass of {@link Shelly2ApiClient} that injects a mock {@link ShellyThingInterface}
     * and exposes the protected {@link #fillDeviceStatus} for unit testing.
     */
    private static class TestableApiClient extends Shelly2ApiClient {
        TestableApiClient(ShellyApiConfiguration config, ShellyThingInterface thing) {
            super("test-thing", config, Mockito.mock(HttpClient.class));
            this.thing = thing;
        }

        @Override
        public <T> T apiRequest(String method, @Nullable Object params, Class<T> classOfT) throws ShellyApiException {
            throw new ShellyApiException("Not expected in addon test: " + method);
        }

        public boolean testFillDeviceStatus(ShellySettingsStatus status, Shelly2DeviceStatusResult result)
                throws ShellyApiException {
            return fillDeviceStatus(status, result, false);
        }
    }

    private TestableApiClient newClient() {
        return new TestableApiClient(testConfig(), mockRelayThing());
    }

    // ── DTO parsing ───────────────────────────────────────────────────────────────

    @Test
    void temperatureStatusTemp_readError_parsesErrorsFieldAndNullTc() {
        Gson gson = new Gson();
        Shelly2DeviceStatusTemp t = Objects
                .requireNonNull(gson.fromJson("{\"tC\":null,\"errors\":[\"read\"]}", Shelly2DeviceStatusTemp.class));
        assertThat(t.tC, is(nullValue()));
        assertThat(t.errors, is(not(nullValue())));
        assertThat(t.errors.contains("read"), is(true));
    }

    @Test
    void temperatureStatusTemp_normalReading_noErrorsField() {
        Gson gson = new Gson();
        Shelly2DeviceStatusTemp t = Objects
                .requireNonNull(gson.fromJson("{\"tC\":22.5,\"tF\":72.5}", Shelly2DeviceStatusTemp.class));
        assertThat(t.tC, is(22.5));
        assertThat(t.errors, is(nullValue()));
    }

    // ── fillDeviceStatus — temperature ────────────────────────────────────────────

    @Test
    void fillDeviceStatus_tempReadError_extTemperatureNull() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.temperature100 = result.new Shelly2DeviceStatusTempId();
        result.temperature100.id = 100;
        result.temperature100.errors = new ArrayList<>(List.of("read"));

        ShellySettingsStatus status = new ShellySettingsStatus();
        newClient().testFillDeviceStatus(status, result);

        assertThat("sensor read error must leave extTemperature null", status.extTemperature, is(nullValue()));
    }

    @Test
    void fillDeviceStatus_tempValid_extTemperaturePopulated() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.temperature100 = result.new Shelly2DeviceStatusTempId();
        result.temperature100.id = 100;
        result.temperature100.tC = 22.5;

        ShellySettingsStatus status = new ShellySettingsStatus();
        newClient().testFillDeviceStatus(status, result);

        assertThat("valid temp must set extTemperature", status.extTemperature, is(not(nullValue())));
        assertThat(status.extTemperature.sensor1, is(not(nullValue())));
        assertThat(status.extTemperature.sensor1.tC, is(22.5));
    }

    @Test
    void fillDeviceStatus_mixedSensors_errorSlotNullValidSlotSet() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        // sensor 100 errors — sensor 101 is valid
        result.temperature100 = result.new Shelly2DeviceStatusTempId();
        result.temperature100.id = 100;
        result.temperature100.errors = new ArrayList<>(List.of("read"));
        result.temperature101 = result.new Shelly2DeviceStatusTempId();
        result.temperature101.id = 101;
        result.temperature101.tC = 18.0;

        ShellySettingsStatus status = new ShellySettingsStatus();
        newClient().testFillDeviceStatus(status, result);

        // extTemperature must be set because sensor101 is valid
        assertThat("at least one valid sensor must set extTemperature", status.extTemperature, is(not(nullValue())));
        assertThat("errored slot must be null", status.extTemperature.sensor1, is(nullValue()));
        assertThat("valid slot must be set", status.extTemperature.sensor2, is(not(nullValue())));
        assertThat(status.extTemperature.sensor2.tC, is(18.0));
    }

    // ── fillDeviceStatus — humidity ───────────────────────────────────────────────

    @Test
    void fillDeviceStatus_humidityReadError_extHumidityNull() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.humidity100 = result.new Shelly2DeviceStatusHumidity();
        result.humidity100.id = 100;
        result.humidity100.errors = new ArrayList<>(List.of("read"));

        ShellySettingsStatus status = new ShellySettingsStatus();
        newClient().testFillDeviceStatus(status, result);

        assertThat("humidity read error must leave extHumidity null", status.extHumidity, is(nullValue()));
    }

    @Test
    void fillDeviceStatus_humidityValid_extHumidityPopulated() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.humidity100 = result.new Shelly2DeviceStatusHumidity();
        result.humidity100.id = 100;
        result.humidity100.rh = 55.0;

        ShellySettingsStatus status = new ShellySettingsStatus();
        newClient().testFillDeviceStatus(status, result);

        assertThat("valid humidity must set extHumidity", status.extHumidity, is(not(nullValue())));
    }

    // ── fillDeviceStatus — voltage ────────────────────────────────────────────────

    @Test
    void fillDeviceStatus_voltageReadError_extVoltageNull() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.voltmeter100 = result.new Shelly2DeviceStatusVoltage();
        result.voltmeter100.id = 100;
        result.voltmeter100.errors = new ArrayList<>(List.of("read"));

        ShellySettingsStatus status = new ShellySettingsStatus();
        newClient().testFillDeviceStatus(status, result);

        assertThat("voltage read error must leave extVoltage null", status.extVoltage, is(nullValue()));
    }

    @Test
    void fillDeviceStatus_voltageValid_extVoltagePopulated() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.voltmeter100 = result.new Shelly2DeviceStatusVoltage();
        result.voltmeter100.id = 100;
        result.voltmeter100.voltage = 3.3;

        ShellySettingsStatus status = new ShellySettingsStatus();
        newClient().testFillDeviceStatus(status, result);

        assertThat("valid voltage must set extVoltage", status.extVoltage, is(not(nullValue())));
    }
}
