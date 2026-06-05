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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_GETCONFIG;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

/**
 * Tests for {@link Shelly2ApiClient#getDeviceProfile} and the shared
 * {@link Shelly2ApiClient#initProfile} helper that both the discovery and the thing-handler
 * paths delegate to.
 *
 * <p>
 * The discovery path ({@link Shelly2ApiClient#getDeviceProfile}) is tested directly.
 * The thing-handler path ({@link Shelly2ApiRpc#getDeviceProfile}) shares the same
 * {@code initProfile} logic, so the core profile-population assertions are valid for both.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2GetDeviceProfileTest {

    private static final String LOCAL_IP = "192.168.1.50";
    private static final String DEVICE_IP = "192.168.1.100";

    // ── helpers ─────────────────────────────────────────────────────────────────

    private ShellyApiConfiguration discoveryConfig() {
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

    private static Shelly2GetConfigResult parseConfig(Gson gson, String json) {
        return Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));
    }

    /** Minimal valid GetConfig JSON — only the fields that initProfile requires non-null */
    private static Shelly2GetConfigResult minimalConfig(Gson gson) {
        return parseConfig(gson, "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}}");
    }

    /** GetConfig with one switch:0 relay present */
    private static Shelly2GetConfigResult withSwitch0(Gson gson) {
        return parseConfig(gson, "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{},"
                + "\"switch:0\":{\"id\":0,\"in_mode\":\"momentary\"}}");
    }

    /** GetConfig with two switches */
    private static Shelly2GetConfigResult withSwitch01(Gson gson) {
        return parseConfig(gson,
                "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{},"
                        + "\"switch:0\":{\"id\":0,\"in_mode\":\"momentary\"},"
                        + "\"switch:1\":{\"id\":1,\"in_mode\":\"momentary\"}}");
    }

    /** GetConfig with pm1:0 present (Mini PM) */
    private static Shelly2GetConfigResult withPm10(Gson gson) {
        return parseConfig(gson, "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}," + "\"pm1:0\":{\"id\":0}}");
    }

    /** GetConfig with em:0 present (Pro 3EM) */
    private static Shelly2GetConfigResult withEm0(Gson gson) {
        return parseConfig(gson, "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}," + "\"em:0\":{\"id\":0}}");
    }

    /** GetConfig with em1:0 present (EM50 clamps) */
    private static Shelly2GetConfigResult withEm10(Gson gson) {
        return parseConfig(gson, "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}," + "\"em1:0\":{\"id\":0}}");
    }

    /** GetConfig with cover:0 (roller device) */
    private static Shelly2GetConfigResult withCover0(Gson gson) {
        // ui_data must be present (even empty) so fillRollerFavorites doesn't NPE on uiData.cover
        // in_mode must be present so mapValue in addRollerSettings doesn't receive null
        return parseConfig(gson, "{\"sys\":{\"device\":{},\"location\":{},\"ui_data\":{}}," + "\"wifi\":{},"
                + "\"cover:0\":{\"in_mode\":\"single\",\"invert_directions\":false}}");
    }

    /** GetConfig with cb:0 present (Pro CB) */
    private static Shelly2GetConfigResult withCb0(Gson gson) {
        return parseConfig(gson, "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}," + "\"cb:0\":{\"id\":0}}");
    }

    private ShellySettingsDevice deviceInfo() {
        ShellySettingsDevice dev = new ShellySettingsDevice();
        dev.type = "SNSW-001P16EU";
        dev.hostname = "shellyplus1pm-aabbcc";
        dev.fw = "1.2.3";
        dev.gen = 2;
        return dev;
    }

    /**
     * Testable subclass that intercepts {@link #apiRequest} to return a fixed
     * {@link Shelly2GetConfigResult} without making real HTTP calls.
     */
    private static class StubApiClient extends Shelly2ApiClient {
        private final Shelly2GetConfigResult configResult;

        StubApiClient(ShellyApiConfiguration config, Shelly2GetConfigResult configResult) {
            super("test", config, Mockito.mock(HttpClient.class));
            this.configResult = configResult;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T apiRequest(String method, @Nullable Object params, Class<T> classOfT) throws ShellyApiException {
            if (SHELLYRPC_METHOD_GETCONFIG.equals(method)) {
                return (T) configResult;
            }
            throw new ShellyApiException("Unexpected apiRequest in test: " + method);
        }
    }

    // ── discovery path ──────────────────────────────────────────────────────────

    @Test
    void discovery_profileMarkedInitialized() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.initialized, is(true));
    }

    @Test
    void discovery_singleRelay_numMetersFallsBackToRelayCount() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withSwitch0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numRelays, is(1));
        assertThat(profile.numMeters, is(1));
    }

    @Test
    void discovery_twoRelays_numMetersFromRelayCount() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withSwitch01(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numRelays, is(2));
        assertThat(profile.numMeters, is(2));
    }

    @Test
    void discovery_noRelaysNoMeters_numMetersZero() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numRelays, is(0));
        assertThat(profile.numMeters, is(0));
    }

    @Test
    void discovery_pm10Present_numMetersIsOne() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withPm10(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numMeters, is(1));
    }

    @Test
    void discovery_em0Present_numMetersIsThree() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withEm0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numMeters, is(3));
    }

    @Test
    void discovery_em10Present_numMetersIsTwo() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withEm10(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numMeters, is(2));
    }

    @ParameterizedTest(name = "{0} → numMeters={1}")
    @CsvSource({
            // Only types present in THING_TYPE_CAP_NUM_METERS — IDs from ShellyDevices ThingTypeUID definitions
            "shellypro3em,    3", "shellyplus3em63, 3", "shellyproem50,   2", "shellyem3,       3",
            "shellypro2,      0", "shellypro3,      0" })
    void discovery_numMetersFromCapabilityMap(String thingTypeId, int expectedNumMeters) throws ShellyApiException {
        ThingTypeUID uid = new ThingTypeUID("shelly", thingTypeId);
        Gson gson = new Gson();
        // No pm10/em0/em10 in config — numMeters must come from the capability map
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(uid, deviceInfo());
        assertThat("thingType=" + thingTypeId, profile.numMeters, is(expectedNumMeters));
    }

    @Test
    void discovery_capabilityMapTakesPriorityOverPm10() throws ShellyApiException {
        // THING_TYPE_SHELLYPRO3EM maps to 3 meters in the capability map; pm10 would give 1
        Gson gson = new Gson();
        // Fabricate a config with pm10 AND a 3EM type — capability map wins
        Shelly2GetConfigResult dc = parseConfig(gson,
                "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}," + "\"pm1:0\":{\"id\":0}}");
        StubApiClient client = new StubApiClient(discoveryConfig(), dc);
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYPRO3EM, deviceInfo());
        assertThat(profile.numMeters, is(3));
    }

    @Test
    void discovery_roller_numMetersFromRollerCount() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withCover0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.isRoller, is(true));
        assertThat(profile.numRollers, is(1));
        // No pm10/em0/em10 → else branch: roller count
        assertThat(profile.numMeters, is(1));
    }

    @Test
    void discovery_cb0Present_isCBTrue() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withCb0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.isCB, is(true));
    }

    @Test
    void discovery_cb0Present_usesBreakersettings() throws ShellyApiException {
        // When isCB is true, fillBreakerSettings should populate relays, not fillRelaySettings.
        // The cb:0 entry makes a single "relay" (breaker) appear in the relay list.
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withCb0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.isCB, is(true));
        assertThat(profile.numRelays, is(1));
    }

    @Test
    void discovery_devInfoArgument_populatesProfileDevice() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellySettingsDevice dev = deviceInfo();
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, dev);
        assertThat(profile.device.type, is("SNSW-001P16EU"));
        assertThat(profile.device.hostname, is("shellyplus1pm-aabbcc"));
    }

    @Test
    void discovery_statusListsSizedToNumMeters() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withSwitch01(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.status.meters.size(), is(profile.numMeters));
        assertThat(profile.status.emeters.size(), is(profile.numMeters));
    }

    @Test
    void discovery_getDeviceProfile_marksProfileInitialized() throws ShellyApiException {
        // initProfile() populates the profile; getDeviceProfile() is responsible for setting profile.initialized.
        // We test this through the discovery path since initProfile is shared.
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYPLUS1PM, deviceInfo());
        assertThat(profile.initialized, is(true));
    }
}
