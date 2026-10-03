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
package org.openhab.binding.shelly.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.Mode.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.RGBX.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api.ShellyApiLightUtil.ShellyLightApiComponent;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest.Shelly2RpcRequestParams;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Tests for the contract between the {@link ShellyLightHandler#updateRemoteDeviceFromLightModel}
 * method (which is called with a {@link ShellyLightModel} instance) and the resulting HTTP request
 * URL produced by the Gen2 {@link Shelly2ApiRpc} API implementation.
 * 
 * This test exists to allow a future refactoring of the updateRemoteDeviceFromLightModel method so
 * that it is driven directly by the light model, while ensuring the end-to-end contract between the
 * light model state and the resulting HTTP traffic will not be broken.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ShellyLightModelToGen2RpcContractTest {

    @ParameterizedTest
    @MethodSource("contractProvider")
    void updateRemoteDeviceFromLightModelProducesExpectedRpc(ThingTypeUID thingTypeUID, String deviceProfile,
            int channelGroupSuffix, ModelSetup modelSetup, List<ExpectedRpc> expectedCalls) throws Exception {

        Gen2Harness harness = Gen2Harness.create(thingTypeUID, deviceProfile);
        ShellyLightModel model = ShellyLightModel.create(harness.handler, channelGroupSuffix,
                harness.handler.getProfile(), 10.0);

        model.acquire();
        try {
            modelSetup.apply(model);
            harness.handler.updateRemoteDeviceFromLightModel(model);
        } finally {
            model.release(false);
        }

        assertEquals(expectedCalls.size(), harness.calls.size(), "unexpected number of RPC calls");

        for (int i = 0; i < expectedCalls.size(); i++) {
            RpcCall actual = harness.calls.get(i);
            ExpectedRpc expected = expectedCalls.get(i);
            assertEquals(expected.method, actual.method, "unexpected RPC method at call " + i);
            assertNotNull(actual.params, "missing params at call " + i);
            Shelly2RpcRequestParams params = actual.params;
            assertNotNull(params, "missing params at call " + i);
            expected.assertion.assertMatches(params);
        }
    }

    private static Stream<Arguments> contractProvider() {
        // @formatter:off
        return Stream.of(
            Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM, SHELLY2_PROFILE_RGBW, 0, 
                (ModelSetup) model -> {
                    model.setMode(COLOR);
                    model.setColor(R, 11);
                    model.setColor(G, 22);
                    model.setColor(B, 33);
                    model.setColor(CW, 44);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_RGBW_SET, params -> {
                    assertEquals(Integer.valueOf(0), params.id);
                    assertArrayEquals(new Integer[] { 11, 22, 33 }, params.rgb);
                    assertEquals(Integer.valueOf(44), params.white);
                    assertOnly(params, "id", "rgb", "white");
                }))),

            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, SHELLY2_PROFILE_RGB, 0, 
                (ModelSetup) model -> {
                    model.setMode(COLOR);
                    model.setColor(R, 123);
                    model.setColor(G, 0);
                    model.setColor(B, 0);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_RGB_SET, params -> {
                    assertEquals(Integer.valueOf(0), params.id);
                    assertArrayEquals(new Integer[] { 123, 0, 0 }, params.rgb);
                    assertOnly(params, "id", "rgb");
                }))),

            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, SHELLY2_PROFILE_RGBCCT, 1, 
                (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setColorTemp(3815);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_CCT_SET, params -> {
                    assertEquals(Integer.valueOf(0), params.id);
                    assertEquals(Integer.valueOf(3815), params.ct);
                    assertOnly(params, "id", "ct");
                }))),

            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, SHELLY2_PROFILE_RGBX2LIGHT, 2, 
                (ModelSetup) model -> {
                    model.setBrightness(70);
                    model.setOnOff(true);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_LIGHT_SET, params -> {
                    assertEquals(Integer.valueOf(2), params.id);
                    assertEquals(Integer.valueOf(70), params.brightness);
                    assertEquals(Boolean.TRUE, params.on);
                    assertOnly(params, "id", "brightness", "on");
                }))),

            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, SHELLY2_PROFILE_CCTX2, 2, 
                (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setColorTemp(3815);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_CCT_SET, params -> {
                    assertEquals(Integer.valueOf(1), params.id);
                    assertEquals(Integer.valueOf(3815), params.ct);
                    assertOnly(params, "id", "ct");
                }))),

            Arguments.of(THING_TYPE_SHELLYPLUSDUOBULB, SHELLY2_PROFILE_LIGHT, 1,
                (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setColorTemp(3815);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_CCT_SET, params -> {
                    assertEquals(Integer.valueOf(0), params.id);
                    assertEquals(Integer.valueOf(3815), params.ct);
                    assertOnly(params, "id", "ct");
                }))),

            Arguments.of(THING_TYPE_SHELLYPLUSCOLORBULB, SHELLY2_PROFILE_RGB, 0,
                (ModelSetup) model -> {
                    model.setMode(COLOR);
                    model.setColor(R, 0);
                    model.setColor(G, 0);
                    model.setColor(B, 255);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_RGBCCT_SET, params -> {
                    assertEquals(Integer.valueOf(0), params.id);
                    assertArrayEquals(new Integer[] { 0, 0, 255 }, params.rgb);
                    assertEquals(Boolean.TRUE, params.on);
                    assertOnly(params, "id", "rgb", "on");
                }))),
                
            Arguments.of(THING_TYPE_SHELLYPLUSCOLORBULB, SHELLY2_PROFILE_RGB, 0, 
                (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.release(false);
                    model.acquire();
                    model.setOnOff(true);
                    model.setMode(COLOR);
                    model.setColor(R, 12);
                    model.setColor(G, 34);
                    model.setColor(B, 56);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_RGBCCT_SET, params -> {
                    assertEquals("rgb", params.mode);
                    assertEquals(Integer.valueOf(0), params.id);
                    assertArrayEquals(new Integer[] { 12, 34, 56 }, params.rgb);
                    assertEquals(Boolean.TRUE, params.on);
                    assertOnly(params, "mode", "id", "rgb", "on");
                }))),

            Arguments.of(THING_TYPE_SHELLYPLUSCOLORBULB, SHELLY2_PROFILE_RGB, 0, 
                (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setColorTemp(3815);
                    model.setOnOff(true);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_RGBCCT_SET, params -> {
                    assertEquals("cct", params.mode);
                    assertEquals(Integer.valueOf(0), params.id);
                    assertEquals(Integer.valueOf(3815), params.ct);
                    assertEquals(Boolean.TRUE, params.on);
                    assertOnly(params, "id", "ct", "on", "mode");
                }))),

            Arguments.of(THING_TYPE_SHELLYPLUSCOLORBULB, SHELLY2_PROFILE_RGB, 0, 
                (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setColorTemp(3815);
                    model.setOnOff(true);
                }, List.of(new ExpectedRpc(SHELLYRPC_METHOD_RGBCCT_SET, params -> {
                    assertEquals(Integer.valueOf(0), params.id);
                    assertEquals("cct", params.mode);
                    assertEquals(Integer.valueOf(3815), params.ct);
                    assertEquals(Boolean.TRUE, params.on);
                    assertOnly(params, "id", "ct", "on", "mode");
                }))));
        // @formatter:on
    }

    @FunctionalInterface
    interface ModelSetup {
        void apply(ShellyLightModel model) throws Exception;
    }

    @FunctionalInterface
    interface RpcAssert {
        void assertMatches(Shelly2RpcRequestParams params) throws Exception;
    }

    static final class ExpectedRpc {
        final String method;
        final RpcAssert assertion;

        ExpectedRpc(String method, RpcAssert assertion) {
            this.method = method;
            this.assertion = assertion;
        }
    }

    static final class RpcCall {
        final String method;
        final @Nullable Shelly2RpcRequestParams params;

        RpcCall(String method, @Nullable Shelly2RpcRequestParams params) {
            this.method = method;
            this.params = params;
        }
    }

    static final class Gen2Harness {
        final ShellyTestLightHandler handler;
        final List<RpcCall> calls = new ArrayList<>();

        private Gen2Harness(ShellyTestLightHandler handler) {
            this.handler = handler;
        }

        static Gen2Harness create(ThingTypeUID thingTypeUID, String deviceProfile) throws ReflectiveOperationException {
            ShellyTestLightHandler handler = ShellyTestLightHandler.create(thingTypeUID);
            ShellyDeviceProfile profile = handler.getProfile();
            profile.device.profile = deviceProfile;
            profile.initialized = true;
            initProfileLights(profile, thingTypeUID, deviceProfile);

            Gen2Harness harness = new Gen2Harness(handler);
            setApi(handler, new RecordingRpcApi(handler, testConfig(), harness));
            return harness;
        }

        @SuppressWarnings("null")
        private static void setApi(ShellyTestLightHandler handler, Shelly2ApiRpc api)
                throws ReflectiveOperationException {
            Field apiField = handler.getClass().getSuperclass().getSuperclass().getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(handler, api);
        }

        private static ShellyApiConfiguration testConfig() {
            ShellyBindingConfiguration raw = ShellyBindingConfiguration
                    .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.50"));
            ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
            return new ShellyApiConfiguration(bindingConfig, "test-realm", "192.168.1.100");
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

        private static void initProfileLights(ShellyDeviceProfile profile, ThingTypeUID thingTypeUID,
                String deviceProfile) {
            profile.isRGBW2 = thingTypeUID.equals(THING_TYPE_SHELLYPLUSRGBWPM)
                    || thingTypeUID.equals(THING_TYPE_SHELLYPRORGBWWPM);
            profile.isDuo = thingTypeUID.equals(THING_TYPE_SHELLYPLUSDUOBULB)
                    || thingTypeUID.equals(THING_TYPE_SHELLYPLUSCOLORBULB);
            profile.isRGBCCT = thingTypeUID.equals(THING_TYPE_SHELLYPLUSCOLORBULB)
                    || SHELLY2_PROFILE_RGBCCT.equals(deviceProfile);
            profile.inColor = SHELLY2_PROFILE_RGB.equals(deviceProfile) || SHELLY2_PROFILE_RGBW.equals(deviceProfile)
                    || SHELLY2_PROFILE_RGBCCT.equals(deviceProfile);

            ArrayList<ShellySettingsRgbwLight> lights = new ArrayList<>();

            if (thingTypeUID.equals(THING_TYPE_SHELLYPLUSRGBWPM)) {
                if (SHELLY2_PROFILE_RGB.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.RGB));
                } else if (SHELLY2_PROFILE_RGBW.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.RGBW));
                } else if (SHELLY2_PROFILE_LIGHT.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.LIGHT));
                    lights.add(taggedLight(ShellyLightApiComponent.LIGHT));
                }
            } else if (thingTypeUID.equals(THING_TYPE_SHELLYPRORGBWWPM)) {
                if (SHELLY2_PROFILE_RGB.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.RGB));
                } else if (SHELLY2_PROFILE_RGBW.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.RGBW));
                } else if (SHELLY2_PROFILE_LIGHT.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.LIGHT));
                    lights.add(taggedLight(ShellyLightApiComponent.LIGHT));
                } else if (SHELLY2_PROFILE_RGBCCT.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.RGB));
                    lights.add(taggedLight(ShellyLightApiComponent.CCT));
                } else if (SHELLY2_PROFILE_RGBX2LIGHT.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.RGB));
                    lights.add(taggedLight(ShellyLightApiComponent.LIGHT));
                    lights.add(taggedLight(ShellyLightApiComponent.LIGHT));
                } else if (SHELLY2_PROFILE_CCTX2.equals(deviceProfile)) {
                    lights.add(taggedLight(ShellyLightApiComponent.CCT));
                    lights.add(taggedLight(ShellyLightApiComponent.CCT));
                }
            } else if (thingTypeUID.equals(THING_TYPE_SHELLYPLUSDUOBULB)) {
                lights.add(taggedLight(ShellyLightApiComponent.CCT));
                profile.inColor = false;
            } else if (thingTypeUID.equals(THING_TYPE_SHELLYPLUSCOLORBULB)) {
                lights.add(taggedLight(ShellyLightApiComponent.RGB));
                profile.status.lights = new ArrayList<>();
                ShellySettingsLight statusLight = new ShellySettingsLight();
                statusLight.ison = true;
                profile.status.lights.add(statusLight);
            }

            profile.settings.lights = lights;
        }

        private static ShellySettingsRgbwLight taggedLight(ShellyLightApiComponent apiComponent) {
            ShellySettingsRgbwLight light = new ShellySettingsRgbwLight();
            light.apiComponent = apiComponent;
            return light;
        }
    }

    static final class RecordingRpcApi extends Shelly2ApiRpc {
        private final Gen2Harness harness;

        RecordingRpcApi(ShellyThingInterface thing, ShellyApiConfiguration config, Gen2Harness harness) {
            super("test", mock(ShellyThingTable.class), thing, config, mock(WebSocketClient.class),
                    mock(ScheduledExecutorService.class));
            this.harness = harness;
        }

        @Override
        public <T> T apiRequest(String method, @Nullable Object params, Class<T> classOfT) {
            harness.calls.add(new RpcCall(method,
                    params instanceof Shelly2RpcRequestParams ? (Shelly2RpcRequestParams) params : null));
            try {
                return classOfT.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Unable to create fallback instance", e);
            }
        }
    }

    private static void assertOnly(Shelly2RpcRequestParams params, String... allowedNonNullFields) {
        List<String> allowed = List.of(allowedNonNullFields);
        assertFieldNullUnlessAllowed(params, "id", params.id, allowed);
        assertFieldNullUnlessAllowed(params, "on", params.on, allowed);
        assertFieldNullUnlessAllowed(params, "brightness", params.brightness, allowed);
        assertFieldNullUnlessAllowed(params, "white", params.white, allowed);
        assertFieldNullUnlessAllowed(params, "ct", params.ct, allowed);
        assertFieldNullUnlessAllowed(params, "rgb", params.rgb, allowed);
        assertFieldNullUnlessAllowed(params, "mode", params.mode, allowed);
        assertFieldNullUnlessAllowed(params, "config", params.config, allowed);
        assertFieldNullUnlessAllowed(params, "toggleAfter", params.toggleAfter, allowed);
        assertFieldNullUnlessAllowed(params, "user", params.user, allowed);
        assertFieldNullUnlessAllowed(params, "realm", params.realm, allowed);
        assertFieldNullUnlessAllowed(params, "ha1", params.ha1, allowed);
        assertFieldNullUnlessAllowed(params, "enable", params.enable, allowed);
        assertFieldNullUnlessAllowed(params, "url", params.url, allowed);
        assertFieldNullUnlessAllowed(params, "stage", params.stage, allowed);
        assertFieldNullUnlessAllowed(params, "data", params.data, allowed);
        assertFieldNullUnlessAllowed(params, "pos", params.pos, allowed);
        assertFieldNullUnlessAllowed(params, "output", params.output, allowed);
    }

    private static void assertFieldNullUnlessAllowed(Shelly2RpcRequestParams params, String fieldName, Object value,
            List<String> allowed) {
        if (!allowed.contains(fieldName)) {
            assertNull(value, "expected params." + fieldName + " to be null");
        }
    }
}
