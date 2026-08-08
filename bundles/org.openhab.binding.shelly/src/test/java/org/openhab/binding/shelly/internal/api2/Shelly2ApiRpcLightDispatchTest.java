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
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest.Shelly2RpcRequestParams;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Covers the Plus RGBW PM dispatch fixes in {@link Shelly2ApiRpc}: {@code setLightParms}/{@code setBrightness}
 * picking {@code RGBW.Set}/{@code RGB.Set}/{@code Light.Set} based on {@link ShellyDeviceProfile#inColor} and the
 * raw device profile, and {@code getLightStatus} picking {@code RGBW.GetStatus}/{@code RGB.GetStatus} vs. looping
 * {@code Light.GetStatus} per channel. Also covers the brightness=0 turn-off fix (Gen2 firmware clamps 0 to 1%, so
 * {@code on=false} is sent instead of {@code brightness=0}).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class Shelly2ApiRpcLightDispatchTest {

    @Mock
    private @NonNullByDefault({}) ShellyThingInterface thing;

    private static class StubApiRpc extends Shelly2ApiRpc {
        final List<String> calledMethods = new ArrayList<>();
        final List<Shelly2RpcRequestParams> calledParams = new ArrayList<>();

        StubApiRpc(ShellyThingInterface thing, ShellyApiConfiguration config) {
            super("test", Mockito.mock(ShellyThingTable.class), thing, config, Mockito.mock(WebSocketClient.class),
                    Mockito.mock(ScheduledExecutorService.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T apiRequest(String method, @Nullable Object params, Class<T> classOfT) throws ShellyApiException {
            calledMethods.add(method);
            if (params instanceof Shelly2RpcRequestParams) {
                calledParams.add((Shelly2RpcRequestParams) params);
            }
            try {
                return classOfT.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new ShellyApiException("test stub error: " + e);
            }
        }

        String lastMethod() {
            return calledMethods.get(calledMethods.size() - 1);
        }

        Shelly2RpcRequestParams lastParams() {
            return calledParams.get(calledParams.size() - 1);
        }
    }

    private ShellyApiConfiguration testConfig() {
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

    private ShellyDeviceProfile colorModeProfile(String rawProfile) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyplusrgbwpm"));
        profile.isRGBW2 = true;
        profile.inColor = true;
        profile.device.profile = rawProfile;
        return profile;
    }

    private ShellyDeviceProfile lightModeProfile(int numChannels) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyplusrgbwpm"));
        profile.isRGBW2 = true;
        profile.inColor = false;
        profile.device.profile = SHELLY2_PROFILE_LIGHT;
        ArrayList<ShellySettingsRgbwLight> lights = new ArrayList<>();
        for (int i = 0; i < numChannels; i++) {
            lights.add(new ShellySettingsRgbwLight());
        }
        profile.settings.lights = lights;
        return profile;
    }

    private StubApiRpc newRpc(ShellyDeviceProfile profile) {
        when(thing.getProfile()).thenReturn(profile);
        return new StubApiRpc(thing, testConfig());
    }

    @Test
    void setLightParms_rgbwProfile_sendsRgbwSetWithWhite() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGBW));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_RED, "10", SHELLY_COLOR_GREEN, "20", SHELLY_COLOR_BLUE, "30",
                SHELLY_COLOR_WHITE, "40", SHELLY_LIGHT_TURN, SHELLY_API_ON));

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_RGBW_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.rgb, is(new Integer[] { 10, 20, 30 }));
        assertThat(params.white, is(40));
        assertThat(params.on, is(true));
    }

    @Test
    void setLightParms_rgbProfile_sendsRgbSetWithoutWhite() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGB));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_RED, "10", SHELLY_COLOR_GREEN, "20", SHELLY_COLOR_BLUE, "30",
                SHELLY_COLOR_WHITE, "40"));

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_RGB_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.rgb, is(new Integer[] { 10, 20, 30 }));
        assertThat(params.white, is(nullValue()));
    }

    @Test
    void setLightParms_lightProfile_sendsLightSet() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(4));
        rpc.setLightParms(2, Map.of(SHELLY_COLOR_BRIGHTNESS, "55"));

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_LIGHT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.id, is(2));
        assertThat(params.brightness, is(55));
        assertThat(params.on, is(true));
    }

    @Test
    void setLightParms_brightnessZero_turnsOffInsteadOfSendingZero() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(1));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_BRIGHTNESS, "0", SHELLY_LIGHT_TURN, SHELLY_API_ON));

        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(nullValue()));
        assertThat(params.on, is(false));
    }

    @Test
    void setBrightness_zero_sendsOnFalseWithoutBrightnessField() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(1));
        rpc.setBrightness(0, 0, false);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_LIGHT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(nullValue()));
        assertThat(params.on, is(false));
    }

    @Test
    void setBrightness_positiveWithAutoOn_sendsBrightnessAndOnTrue() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(1));
        rpc.setBrightness(0, 42, true);

        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(42));
        assertThat(params.on, is(true));
    }

    @Test
    void setBrightness_positiveWithoutAutoOn_sendsBrightnessWithoutTouchingOnState() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(1));
        rpc.setBrightness(0, 42, false);

        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(42));
        assertThat(params.on, is(nullValue()));
    }

    @Test
    void getLightStatus_rgbwProfile_callsRgbwGetStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGBW));
        rpc.getLightStatus();

        assertThat(rpc.calledMethods, is(List.of(SHELLYRPC_METHOD_RGBW_STATUS)));
    }

    @Test
    void getLightStatus_rgbProfile_callsRgbGetStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGB));
        rpc.getLightStatus();

        assertThat(rpc.calledMethods, is(List.of(SHELLYRPC_METHOD_RGB_STATUS)));
    }

    @Test
    void getLightStatus_lightProfile_loopsLightGetStatusPerChannel() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(4));
        rpc.getLightStatus();

        assertThat(rpc.calledMethods, is(List.of(SHELLYRPC_METHOD_LIGHT_STATUS, SHELLYRPC_METHOD_LIGHT_STATUS,
                SHELLYRPC_METHOD_LIGHT_STATUS, SHELLYRPC_METHOD_LIGHT_STATUS)));
        assertThat(rpc.calledParams.get(0).id, is(0));
        assertThat(rpc.calledParams.get(1).id, is(1));
        assertThat(rpc.calledParams.get(2).id, is(2));
        assertThat(rpc.calledParams.get(3).id, is(3));
    }
}
