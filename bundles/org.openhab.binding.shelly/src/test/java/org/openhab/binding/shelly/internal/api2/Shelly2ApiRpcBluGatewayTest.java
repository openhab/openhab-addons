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
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_GETCONFIG;
import static org.openhab.binding.shelly.internal.api2.ShellyBluJsonDTO.SHELLY2_BLU_GWSCRIPT;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDevice;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
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
 * {@code enableBluGateway} thing configuration, not just installed when it's enabled.
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

    private record InstallCall(String script, boolean install) {
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

    private StubApiRpc buildApi(Shelly2GetConfigResult configResult, boolean enableBluGateway) throws Exception {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initFromThingType(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true; // skip the firstInit status-fetch branch
        profile.alwaysOn = true;

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

        return new StubApiRpc(thing, thingTable, config, configResult);
    }

    /**
     * Testable subclass that intercepts {@link #apiRequest} to return a fixed
     * {@link Shelly2GetConfigResult} without making real HTTP/WebSocket calls, and records
     * {@link #installScript} invocations instead of actually managing scripts on a device.
     */
    private static class StubApiRpc extends Shelly2ApiRpc {
        private final Shelly2GetConfigResult configResult;
        final List<InstallCall> installCalls = new ArrayList<>();

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
