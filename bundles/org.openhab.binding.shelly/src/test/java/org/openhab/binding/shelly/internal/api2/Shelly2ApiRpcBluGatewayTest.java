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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.BUNDLE_RESOURCE_SCRIPTS;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_GETCONFIG;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_SCRIPT_GETCODE;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_SCRIPT_LIST;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_SCRIPT_SETCONFIG;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_SCRIPT_START;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_SCRIPT_STOP;
import static org.openhab.binding.shelly.internal.api2.ShellyBluJsonDTO.SHELLY2_BLU_GWSCRIPT;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.ShellyScriptListResponse;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.ShellyScriptListResponse.ShellyScriptListEntry;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.ShellyScriptResponse;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;

/**
 * Tests for {@link Shelly2ApiRpc#getDeviceProfile}'s BLU Gateway script lifecycle: the
 * {@code oh-blu-scanner.js} script must be removed from the device when the user disables the
 * {@code enableBluGateway} thing configuration, not just installed when it's enabled. A scan
 * ({@link Shelly2ApiRpc#startScan}) restarts the script, so the gateway announces known BLU devices again.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class Shelly2ApiRpcBluGatewayTest {

    @Test
    void disablingBluGatewayRemovesScript() throws Exception {
        StubApiRpc api = buildApi(configWithBle(), false);

        api.getDeviceProfile(THING_TYPE_SHELLYPLUS1PM, deviceInfo());

        assertThat(api.installCalls, contains(new InstallCall(SHELLY2_BLU_GWSCRIPT, false)));
    }

    @Test
    void enablingBluGatewayOnFw20InstallsScript() throws Exception {
        StubApiRpc api = buildApi(configWithBle(), true);

        api.getDeviceProfile(THING_TYPE_SHELLYPLUS1PM, deviceInfoFw20());

        assertThat(api.installCalls, contains(new InstallCall(SHELLY2_BLU_GWSCRIPT, true)));
    }

    @Test
    void noBleConfigDoesNotTouchScript() throws Exception {
        StubApiRpc api = buildApi(configWithoutBle(), false);

        api.getDeviceProfile(THING_TYPE_SHELLYPLUS1PM, deviceInfo());

        assertThat(api.installCalls, empty());
    }

    @Test
    void scanRestartsScriptOnGateway() throws Exception {
        StubApiRpc api = buildApi(configWithBle(), true);

        api.startScan();

        assertThat(api.scanCalls, contains(new ScanCall(SHELLY2_BLU_GWSCRIPT, true, true)));
    }

    @Test
    void scanWithBluGatewayDisabledRemovesScript() throws Exception {
        StubApiRpc api = buildApi(configWithBle(), false);

        api.startScan();

        assertThat(api.scanCalls, contains(new ScanCall(SHELLY2_BLU_GWSCRIPT, false, true)));
    }

    @Test
    void scanOnBluDeviceDoesNotTouchScript() throws Exception {
        StubApiRpc api = buildApi(configWithBle(), true, true);

        api.startScan();

        assertThat(api.scanCalls, empty());
    }

    @Test
    void installWithRestartRestartsRunningScript() throws Exception {
        ScriptApiRpc api = buildScriptApi(true);

        api.installScript(SHELLY2_BLU_GWSCRIPT, true, true);

        assertThat(api.methods, contains(SHELLYRPC_METHOD_SCRIPT_LIST, SHELLYRPC_METHOD_SCRIPT_GETCODE,
                SHELLYRPC_METHOD_SCRIPT_SETCONFIG, SHELLYRPC_METHOD_SCRIPT_STOP, SHELLYRPC_METHOD_SCRIPT_START));
    }

    @Test
    void installWithoutRestartLeavesRunningScript() throws Exception {
        ScriptApiRpc api = buildScriptApi(true);

        api.installScript(SHELLY2_BLU_GWSCRIPT, true);

        assertThat(api.methods, contains(SHELLYRPC_METHOD_SCRIPT_LIST, SHELLYRPC_METHOD_SCRIPT_GETCODE,
                SHELLYRPC_METHOD_SCRIPT_SETCONFIG));
    }

    @Test
    void installWithRestartStartsStoppedScript() throws Exception {
        ScriptApiRpc api = buildScriptApi(false);

        api.installScript(SHELLY2_BLU_GWSCRIPT, true, true);

        assertThat(api.methods, contains(SHELLYRPC_METHOD_SCRIPT_LIST, SHELLYRPC_METHOD_SCRIPT_GETCODE,
                SHELLYRPC_METHOD_SCRIPT_SETCONFIG, SHELLYRPC_METHOD_SCRIPT_START));
    }

    private record InstallCall(String script, boolean install) {
    }

    private record ScanCall(String script, boolean install, boolean restart) {
    }

    private static ShellySettingsDevice deviceInfo() {
        ShellySettingsDevice dev = new ShellySettingsDevice();
        dev.type = "SNSW-001P16EU";
        dev.hostname = "shellyplus1pm-aabbcc";
        dev.fw = "20230913-112003/v1.2.3-gcb84623";
        dev.gen = 2;
        return dev;
    }

    private static ShellySettingsDevice deviceInfoFw20() {
        ShellySettingsDevice dev = deviceInfo();
        dev.fw = "20240913-112003/v2.0.0-gcb84623";
        return dev;
    }

    private static Shelly2GetConfigResult configWithBle() {
        Gson gson = new Gson();
        return Objects.requireNonNull(gson.fromJson("{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{},\"ble\":{}}",
                Shelly2GetConfigResult.class));
    }

    private static Shelly2GetConfigResult configWithoutBle() {
        Gson gson = new Gson();
        return Objects.requireNonNull(
                gson.fromJson("{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}}", Shelly2GetConfigResult.class));
    }

    private record Parts(ShellyThingInterface thing, ShellyThingTable thingTable, ShellyApiConfiguration config) {
    }

    private StubApiRpc buildApi(Shelly2GetConfigResult configResult, boolean enableBluGateway) throws Exception {
        return buildApi(configResult, enableBluGateway, false);
    }

    private StubApiRpc buildApi(Shelly2GetConfigResult configResult, boolean enableBluGateway, boolean isBlu)
            throws Exception {
        Parts parts = buildParts(enableBluGateway, isBlu);
        return new StubApiRpc(parts.thing(), parts.thingTable(), parts.config(), configResult);
    }

    private ScriptApiRpc buildScriptApi(boolean running) throws Exception {
        return new ScriptApiRpc(buildParts(true, false), running, bundledScript());
    }

    private Parts buildParts(boolean enableBluGateway, boolean isBlu) throws Exception {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initFromThingType(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true; // skip the firstInit status-fetch branch
        profile.alwaysOn = true;
        profile.isBlu = isBlu;

        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        Thing ohThing = mock(Thing.class);
        when(ohThing.getUID()).thenReturn(new ThingUID(THING_TYPE_SHELLYPLUS1PM, "test"));

        HttpClient httpClient = mock(HttpClient.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(httpClient);
        when(thing.getProfile()).thenReturn(profile);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-rpc", "");
        setField(config, "enableBluGateway", enableBluGateway);

        return new Parts(thing, thingTable, config);
    }

    private static String bundledScript() throws Exception {
        ClassLoader classLoader = Objects.requireNonNull(Shelly2ApiRpc.class.getClassLoader());
        try (InputStream in = classLoader.getResourceAsStream(BUNDLE_RESOURCE_SCRIPTS + "/" + SHELLY2_BLU_GWSCRIPT)) {
            return new BufferedReader(new InputStreamReader(Objects.requireNonNull(in), StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    /**
     * Testable subclass that intercepts {@link #apiRequest} to return a fixed
     * {@link Shelly2GetConfigResult} without making real HTTP/WebSocket calls, and records
     * {@link #installScript} invocations instead of actually managing scripts on a device.
     */
    private static class StubApiRpc extends Shelly2ApiRpc {
        private final Shelly2GetConfigResult configResult;
        final List<InstallCall> installCalls = new ArrayList<>();
        final List<ScanCall> scanCalls = new ArrayList<>();

        StubApiRpc(ShellyThingInterface thing, ShellyThingTable thingTable, ShellyApiConfiguration config,
                Shelly2GetConfigResult configResult) {
            super("test-rpc", thingTable, thing, config, mock(WebSocketClient.class),
                    mock(ScheduledExecutorService.class));
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

        @Override
        protected void installScript(String script, boolean install) {
            installCalls.add(new InstallCall(script, install));
        }

        @Override
        protected void installScript(String script, boolean install, boolean restart) {
            scanCalls.add(new ScanCall(script, install, restart));
        }
    }

    /**
     * Testable subclass that answers the script related RPC calls of the real
     * {@link #installScript(String, boolean, boolean)} and records the RPC methods it uses. The device reports one
     * installed script, running or not, whose code equals the bundled one.
     */
    private static class ScriptApiRpc extends Shelly2ApiRpc {
        final List<String> methods = new ArrayList<>();
        private final ShellyScriptListResponse scriptList = new ShellyScriptListResponse();
        private final ShellyScriptResponse scriptCode = new ShellyScriptResponse();

        ScriptApiRpc(Parts parts, boolean running, String code) {
            super("test-rpc", parts.thingTable(), parts.thing(), parts.config(), mock(WebSocketClient.class),
                    mock(ScheduledExecutorService.class));
            ShellyScriptListEntry entry = new ShellyScriptListEntry();
            entry.id = 1;
            entry.name = SHELLY2_BLU_GWSCRIPT;
            entry.enable = true;
            entry.running = running;
            scriptList.scripts = new ArrayList<>(List.of(entry));
            scriptCode.data = code;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T apiRequest(String method, @Nullable Object params, Class<T> classOfT) throws ShellyApiException {
            methods.add(method);
            if (SHELLYRPC_METHOD_SCRIPT_LIST.equals(method)) {
                return (T) scriptList;
            }
            if (SHELLYRPC_METHOD_SCRIPT_GETCODE.equals(method)) {
                return (T) scriptCode;
            }
            if (classOfT == String.class) {
                return (T) "";
            }
            try {
                return classOfT.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new ShellyApiException("Unexpected apiRequest in test: " + method);
            }
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
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
}
