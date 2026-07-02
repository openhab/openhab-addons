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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusEmData;
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

    /** GetConfig with em1:0 only — single-clamp device (EM Mini G4) */
    private static Shelly2GetConfigResult withEm10Only(Gson gson) {
        return parseConfig(gson, "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}," + "\"em1:0\":{\"id\":0}}");
    }

    /** GetConfig with em1:0 + em1:1 — two-clamp device (Pro EM-50) */
    private static Shelly2GetConfigResult withEm10AndEm11(Gson gson) {
        return parseConfig(gson,
                "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}," + "\"em1:0\":{\"id\":0},\"em1:1\":{\"id\":1}}");
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

    @Test
    void discoveryProfileMarkedInitialized() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.initialized, is(true));
    }

    @Test
    void discoverySingleRelayNumMetersFallsBackToRelayCount() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withSwitch0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numRelays, is(1));
        assertThat(profile.numMeters, is(1));
    }

    @Test
    void discoveryTwoRelaysNumMetersFromRelayCount() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withSwitch01(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numRelays, is(2));
        assertThat(profile.numMeters, is(2));
    }

    @Test
    void discoveryNoRelaysNoMetersNumMetersZero() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numRelays, is(0));
        assertThat(profile.numMeters, is(0));
    }

    @Test
    void discoveryPM10PresentNumMetersIsOne() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withPm10(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numMeters, is(1));
    }

    @Test
    void discoveryEm0PresentNumMetersIsThree() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withEm0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numMeters, is(3));
    }

    @Test
    void discoveryEm10AloneNumMetersIsOne() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withEm10Only(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numMeters, is(1));
    }

    @Test
    void discoveryEm10AndEm11NumMetersIsTwo() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withEm10AndEm11(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.numMeters, is(2));
    }

    @ParameterizedTest(name = "{0} → numMeters={1}")
    @CsvSource({
            // Only types present in THING_TYPE_CAP_NUM_METERS — IDs from ShellyDevices ThingTypeUID definitions
            "shellypro3em,    3", "shellyplus3em63, 3", "shellyproem50,   2", "shellyem3,       3",
            "shellypro2,      0", "shellypro3,      0" })
    void discoveryNumMetersFromCapabilityMap(String thingTypeId, int expectedNumMeters) throws ShellyApiException {
        ThingTypeUID uid = new ThingTypeUID("shelly", thingTypeId);
        Gson gson = new Gson();
        // No pm10/em0/em10 in config — numMeters must come from the capability map
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(uid, deviceInfo());
        assertThat("thingType=" + thingTypeId, profile.numMeters, is(expectedNumMeters));
    }

    @Test
    void discoveryCapabilityMapTakesPriorityOverPm10() throws ShellyApiException {
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
    void discoveryRollerNumMetersFromRollerCount() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withCover0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.isRoller, is(true));
        assertThat(profile.numRollers, is(1));
        // No pm10/em0/em10 → else branch: roller count
        assertThat(profile.numMeters, is(1));
    }

    @Test
    void discoveryCB0PresentIsCBTrue() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withCb0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.isCB, is(true));
    }

    @Test
    void discoveryCB0PresentUsesBreakersettings() throws ShellyApiException {
        // When isCB is true, fillBreakerSettings should populate relays, not fillRelaySettings.
        // The cb:0 entry makes a single "relay" (breaker) appear in the relay list.
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withCb0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        assertThat(profile.isCB, is(true));
        assertThat(profile.numRelays, is(1));
    }

    @Test
    void discoveryDevInfoArgumentPopulatesProfileDevice() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellySettingsDevice dev = deviceInfo();
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, dev);
        assertThat(profile.device.type, is("SNSW-001P16EU"));
        assertThat(profile.device.hostname, is("shellyplus1pm-aabbcc"));
    }

    @Test
    void discoveryStatusListsSizedToNumMeters() throws ShellyApiException {
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withSwitch01(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYUNKNOWN, deviceInfo());
        // Gen2 uses emeters (pre-sized to numMeters); meters is Gen1-only and is never allocated for Gen2
        assertThat(profile.status.emeters.size(), is(profile.numMeters));
        assertThat(profile.status.meters, is(nullValue()));
    }

    @Test
    void discoveryGetDeviceProfileMarksProfileInitialized() throws ShellyApiException {
        // initProfile() populates the profile; getDeviceProfile() is responsible for setting profile.initialized.
        // We test this through the discovery path since initProfile is shared.
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), minimalConfig(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYPLUS1PM, deviceInfo());
        assertThat(profile.initialized, is(true));
    }

    @Test
    void initProfilePreservesEmetersWhenCountUnchanged() throws ShellyApiException {
        // Regression test for the race condition in ShellyBaseHandler.refreshStatus():
        // api.getStatus() populates emeter.totalReturned, then getProfile(refreshSettings=true)
        // calls initProfile() which must NOT reset the list when the meter count is unchanged.
        Gson gson = new Gson();
        StubApiClient client = new StubApiClient(discoveryConfig(), withEm0(gson));
        ShellyDeviceProfile profile = client.getDeviceProfile(THING_TYPE_SHELLYPRO3EM, deviceInfo());
        assertThat(profile.status.emeters.size(), is(3));

        // Simulate api.getStatus() populating totalReturned
        profile.status.emeters.get(0).totalReturned = 500.0;
        profile.status.emeters.get(1).totalReturned = 300.0;
        profile.status.emeters.get(2).totalReturned = 200.0;

        // Second call simulates getProfile(refreshSettings=true) in the same refreshStatus() cycle
        client.getDeviceProfile(THING_TYPE_SHELLYPRO3EM, deviceInfo());

        assertThat("phase A totalReturned preserved", profile.status.emeters.get(0).totalReturned, is(500.0));
        assertThat("phase B totalReturned preserved", profile.status.emeters.get(1).totalReturned, is(300.0));
        assertThat("phase C totalReturned preserved", profile.status.emeters.get(2).totalReturned, is(200.0));
    }

    @Test
    void emdata0TotalRetKWHDeserializedFromJson() {
        Gson gson = new Gson();
        String json = "{\"a_total_act_ret_energy\":500.0,\"b_total_act_ret_energy\":300.0,\"c_total_act_ret_energy\":200.0,"
                + "\"total_act_ret\":1.5,\"total_act\":5.0}";
        Shelly2DeviceStatusEmData emData = gson.fromJson(json, Shelly2DeviceStatusEmData.class);
        assertNotNull(emData);
        assertThat("total_act_ret maps to totalRetKWH", emData.totalRetKWH, is(1.5));
        assertThat("a_total_act_ret_energy maps to aRetTotal", emData.aRetTotal, is(500.0));
        assertThat("b_total_act_ret_energy maps to bRetTotal", emData.bRetTotal, is(300.0));
        assertThat("c_total_act_ret_energy maps to cRetTotal", emData.cRetTotal, is(200.0));
    }

    @Test
    void emdata0TotalRetKWHAbsentInJsonIsNull() {
        Gson gson = new Gson();
        // Simulates a WebSocket NotifyStatus payload where emdata:0 is absent — total_act_ret must be null
        String json = "{\"a_total_act_ret_energy\":500.0,\"total_act\":5.0}";
        Shelly2DeviceStatusEmData emData = gson.fromJson(json, Shelly2DeviceStatusEmData.class);
        assertNotNull(emData);
        assertThat("absent total_act_ret deserializes to null", emData.totalRetKWH, is(nullValue()));
    }

    @Test
    void emdata0InDeviceStatusResultTotalRetKWHRoundtrip() {
        // Verify total_act_ret survives the double-serialization used in Shelly2ApiRpc.asyncApiRequest()
        // (Gson deserializes result as Object/LinkedTreeMap, then re-serializes to JSON, then re-parses as DTO).
        Gson gson = new Gson();
        String resultJson = "{\"emdata:0\":{\"a_total_act_ret_energy\":500.0,\"b_total_act_ret_energy\":300.0,"
                + "\"c_total_act_ret_energy\":200.0,\"total_act_ret\":1.5,\"total_act\":5.0}}";
        // Simulate the double-serialization: Object → JSON → DTO
        Object raw = gson.fromJson(resultJson, Object.class);
        String reserialised = gson.toJson(raw);
        Shelly2DeviceStatusResult result = Objects
                .requireNonNull(gson.fromJson(reserialised, Shelly2DeviceStatusResult.class));
        Shelly2DeviceStatusEmData emdata0 = result.emdata0;
        assertThat(emdata0, is(notNullValue()));
        assertThat(emdata0.totalRetKWH, is(1.5));
    }
}
