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
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.Mode.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.RGBX.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Tests for the contract between the {@link ShellyLightHandler#updateRemoteDeviceFromLightModel}
 * method (which is called with a {@link ShellyLightModel} instance) and the resulting HTTP request
 * URL produced by the Gen1 {@link Shelly1HttpApi} API implementation.
 * 
 * This test exists to allow a future refactoring of the updateRemoteDeviceFromLightModel method so
 * that it is driven directly by the light model, while ensuring the end-to-end contract between the
 * light model state and the resulting HTTP traffic will not be broken.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ShellyLightModelToGen1HttpContractTest {

    @ParameterizedTest
    @MethodSource("contractProvider")
    void updateRemoteDeviceFromLightModelProducesExpectedHttpRequests(ThingTypeUID thingTypeUID, int channelGroupSuffix,
            ModelSetup setup, List<String> expectedUrls) throws Exception {

        Gen1Harness harness = Gen1Harness.create(thingTypeUID);
        ShellyLightModel model = ShellyLightModel.create(harness.handler, channelGroupSuffix,
                harness.handler.getProfile(), 10.0);

        model.acquire();
        try {
            setup.apply(model);
            harness.handler.updateRemoteDeviceFromLightModel(model);
        } finally {
            model.release(false);
        }

        assertEquals(expectedUrls, harness.urls);
    }

    private static Stream<Arguments> contractProvider() {
        // @formatter:off
        return Stream.of(
                Arguments.of(THING_TYPE_SHELLYBULB, 0, (ModelSetup) model -> {
                    model.setMode(COLOR);
                    model.setColor(R, 255);
                    model.setColor(G, 0);
                    model.setColor(B, 0);
                    model.setColor(CW, 0);
                },
                List.of("/color/0?blue=0&green=0&red=255&white=0")),

                Arguments.of(THING_TYPE_SHELLYDUORGBW, 0, (ModelSetup) model -> {
                    model.setMode(COLOR);
                    model.setColor(R, 255);
                    model.setColor(G, 255);
                    model.setColor(B, 0);
                    model.setColor(CW, 0);
                    model.setGain(23);
                },
                List.of("/light/0?blue=0&gain=23&green=255&red=255&turn=on&white=0")),

                Arguments.of(THING_TYPE_SHELLYDUO, 0, (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setBrightness(42);
                    model.setOnOff(true);
                }, 
                List.of("/light/0?brightness=42&turn=on")),

                Arguments.of(THING_TYPE_SHELLYVINTAGE, 0, (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setBrightness(25);
                    model.setOnOff(true);
                }, List.of("/light/0?brightness=25&turn=on")),

                Arguments.of(THING_TYPE_SHELLYBULB, 0, (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setColorTemp(3815);
                },
                List.of("/settings?mode=white", "/white/0?temp=3815")),

                Arguments.of(THING_TYPE_SHELLYRGBW2_COLOR, 0, (ModelSetup) model -> {
                    model.setMode(COLOR);
                    model.setColor(R, 0);
                    model.setColor(G, 0);
                    model.setColor(B, 255);
                    model.setColor(CW, 0);
                },
                List.of("/color/0?blue=255&green=0&red=0&white=0")),
                
                Arguments.of(THING_TYPE_SHELLYBULB, 0, (ModelSetup) model -> {
                    model.setOnOff(true);
                    model.setGain(67);
                },
                List.of("/color/0?blue=0&gain=67&green=0&red=0&turn=on&white=0")), 

                Arguments.of(THING_TYPE_SHELLYBULB, 0, (ModelSetup) model -> {
                    model.setOnOff(true);
                    model.setEffect(2);
                },
                List.of("/color/0?effect=2")),
                
                Arguments.of(THING_TYPE_SHELLYRGBW2_WHITE, 1, (ModelSetup) model -> {
                    model.setMode(WHITE);
                    model.setBrightness(73);
                    model.setOnOff(true);
                }, 
                List.of("/white/0?brightness=73&turn=on")));
        // @formatter:on
    }

    @FunctionalInterface
    interface ModelSetup {
        void apply(ShellyLightModel model) throws Exception;
    }

    static final class Gen1Harness {
        final ShellyTestLightHandler handler;
        final List<String> urls;

        private Gen1Harness(ShellyTestLightHandler handler, List<String> urls) {
            this.handler = handler;
            this.urls = urls;
        }

        static Gen1Harness create(ThingTypeUID thingTypeUID) throws ReflectiveOperationException {
            ShellyTestLightHandler handler = ShellyTestLightHandler.create(thingTypeUID);
            List<String> urls = new ArrayList<>();
            setApi(handler, new RecordingGen1Api(handler.getProfile(), urls));
            return new Gen1Harness(handler, urls);
        }

        @SuppressWarnings("null")
        private static void setApi(ShellyTestLightHandler handler, Shelly1HttpApi api)
                throws ReflectiveOperationException {
            Field apiField = handler.getClass().getSuperclass().getSuperclass().getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(handler, api);
        }
    }

    static final class RecordingGen1Api extends Shelly1HttpApi {
        final List<String> urls;

        RecordingGen1Api(ShellyDeviceProfile profile, List<String> urls) {
            super("test", testConfig(), mock(HttpClient.class));
            this.urls = urls;
            setProfile(profile);
            if (this.profile.device.mode == null) {
                this.profile.device.mode = "";
            }
        }

        @SuppressWarnings("null")
        private void setProfile(ShellyDeviceProfile profile) {
            try {
                Field profileField = getClass().getSuperclass().getSuperclass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(this, profile);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void setLightMode(String mode) throws org.openhab.binding.shelly.internal.api.ShellyApiException {
            if (profile.device.mode == null) {
                profile.device.mode = "";
            }
            if (!mode.isEmpty() && !profile.device.mode.equals(mode)) {
                setLightSetting(SHELLY_API_MODE, mode);
                profile.device.mode = mode;
                profile.inColor = profile.isLight && mode.equalsIgnoreCase(SHELLY_MODE_COLOR);
            }
        }

        @Override
        protected String httpRequest(String uri) {
            urls.add(uri);
            return "";
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
    }
}
