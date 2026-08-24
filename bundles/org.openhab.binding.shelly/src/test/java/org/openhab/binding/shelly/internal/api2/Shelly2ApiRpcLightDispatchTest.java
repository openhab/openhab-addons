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
import static org.openhab.binding.shelly.internal.api.ShellyApiLightUtil.*;
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
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusLight;
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
 * {@code on=false} is sent instead of {@code brightness=0}), {@code setLightParm} delegating to
 * {@code setLightParms} for the primary on/off channel, and {@code setAutoTimer} picking
 * {@code RGBW.SetConfig}/{@code RGB.SetConfig}/{@code Light.SetConfig} the same way. Also covers
 * {@link ShellyDeviceProfile#getLightComponentId} on hybrid profiles (rgbcct, rgbx2light), where a
 * settings.lights index doesn't equal the on-device RPC component id.
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
        final List<Object> cannedResponses = new ArrayList<>();
        private int cannedIndex = 0;

        StubApiRpc(ShellyThingInterface thing, ShellyApiConfiguration config) {
            super("test", Mockito.mock(ShellyThingTable.class), thing, config, Mockito.mock(WebSocketClient.class),
                    Mockito.mock(ScheduledExecutorService.class));
        }

        @Override
        public <T> T apiRequest(String method, @Nullable Object params, Class<T> classOfT) throws ShellyApiException {
            calledMethods.add(method);
            if (params instanceof Shelly2RpcRequestParams) {
                calledParams.add((Shelly2RpcRequestParams) params);
            }
            if (cannedIndex < cannedResponses.size()) {
                return classOfT.cast(cannedResponses.get(cannedIndex++));
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

    private static ShellySettingsRgbwLight taggedLight(ShellyLightApiComponent apiComponent) {
        ShellySettingsRgbwLight light = new ShellySettingsRgbwLight();
        light.apiComponent = apiComponent;
        return light;
    }

    private ShellyDeviceProfile colorModeProfile(String rawProfile) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyplusrgbwpm"));
        profile.isRGBW2 = true;
        profile.inColor = true;
        profile.device.profile = rawProfile;
        ShellyLightApiComponent tag = SHELLY2_PROFILE_RGB.equals(rawProfile) ? ShellyLightApiComponent.RGB
                : ShellyLightApiComponent.RGBW;
        ArrayList<ShellySettingsRgbwLight> lights = new ArrayList<>();
        lights.add(taggedLight(tag));
        profile.settings.lights = lights;
        return profile;
    }

    private ShellyDeviceProfile lightModeProfile(int numChannels) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyplusrgbwpm"));
        profile.isRGBW2 = true;
        profile.inColor = false;
        profile.device.profile = SHELLY2_PROFILE_LIGHT;
        ArrayList<ShellySettingsRgbwLight> lights = new ArrayList<>();
        for (int i = 0; i < numChannels; i++) {
            lights.add(taggedLight(ShellyLightApiComponent.LIGHT));
        }
        profile.settings.lights = lights;
        return profile;
    }

    private ShellyDeviceProfile cctx2Profile(int numChannels) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyprorgbwwpm"));
        profile.isRGBW2 = true;
        profile.inColor = false;
        profile.device.profile = SHELLY2_PROFILE_CCTX2;
        ArrayList<ShellySettingsRgbwLight> lights = new ArrayList<>();
        for (int i = 0; i < numChannels; i++) {
            lights.add(taggedLight(ShellyLightApiComponent.CCT));
        }
        profile.settings.lights = lights;
        return profile;
    }

    private ShellyDeviceProfile rgbcctProfile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyprorgbwwpm"));
        profile.isRGBW2 = true;
        profile.inColor = true;
        profile.device.profile = SHELLY2_PROFILE_RGBCCT;
        ArrayList<ShellySettingsRgbwLight> lights = new ArrayList<>();
        lights.add(taggedLight(ShellyLightApiComponent.RGB)); // settings.lights[0] = RGB:0
        lights.add(taggedLight(ShellyLightApiComponent.CCT)); // settings.lights[1] = CCT:0
        profile.settings.lights = lights;
        return profile;
    }

    private ShellyDeviceProfile rgbx2lightProfile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyprorgbwwpm"));
        profile.isRGBW2 = true;
        profile.inColor = true;
        profile.device.profile = SHELLY2_PROFILE_RGBX2LIGHT;
        ArrayList<ShellySettingsRgbwLight> lights = new ArrayList<>();
        lights.add(taggedLight(ShellyLightApiComponent.RGB)); // settings.lights[0] = RGB:0
        lights.add(taggedLight(ShellyLightApiComponent.LIGHT)); // settings.lights[1] = Light:0
        lights.add(taggedLight(ShellyLightApiComponent.LIGHT)); // settings.lights[2] = Light:1
        profile.settings.lights = lights;
        return profile;
    }

    private StubApiRpc newRpc(ShellyDeviceProfile profile) {
        when(thing.getProfile()).thenReturn(profile);
        return new StubApiRpc(thing, testConfig());
    }

    @Test
    void setLightParmsRgbwProfileSendsRgbwSetWithWhite() throws ShellyApiException {
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
    void setLightParmsRgbProfileSendsRgbSetWithoutWhite() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGB));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_RED, "10", SHELLY_COLOR_GREEN, "20", SHELLY_COLOR_BLUE, "30",
                SHELLY_COLOR_WHITE, "40"));

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_RGB_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.rgb, is(new Integer[] { 10, 20, 30 }));
        assertThat(params.white, is(nullValue()));
    }

    @Test
    void setLightParmsLightProfileSendsLightSet() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(4));
        rpc.setLightParms(2, Map.of(SHELLY_COLOR_BRIGHTNESS, "55"));

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_LIGHT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.id, is(2));
        assertThat(params.brightness, is(55));
        assertThat(params.on, is(true));
    }

    @Test
    void setLightParmsBrightnessZeroTurnsOffInsteadOfSendingZero() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(1));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_BRIGHTNESS, "0", SHELLY_LIGHT_TURN, SHELLY_API_ON));

        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(nullValue()));
        assertThat(params.on, is(false));
    }

    @Test
    void setBrightnessZeroSendsOnFalseWithoutBrightnessField() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(1));
        rpc.setBrightness(0, 0, false);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_LIGHT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(nullValue()));
        assertThat(params.on, is(false));
    }

    @Test
    void setBrightnessPositiveWithAutoOnSendsBrightnessAndOnTrue() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(1));
        rpc.setBrightness(0, 42, true);

        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(42));
        assertThat(params.on, is(true));
    }

    @Test
    void setBrightnessPositiveWithoutAutoOnSendsBrightnessWithoutTouchingOnState() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(1));
        rpc.setBrightness(0, 42, false);

        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(42));
        assertThat(params.on, is(nullValue()));
    }

    @Test
    void getLightStatusRgbwProfileCallsRgbwGetStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGBW));
        rpc.getLightStatus();

        assertThat(rpc.calledMethods, is(List.of(SHELLYRPC_METHOD_RGBW_STATUS)));
    }

    @Test
    void getLightStatusRgbProfileCallsRgbGetStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGB));
        rpc.getLightStatus();

        assertThat(rpc.calledMethods, is(List.of(SHELLYRPC_METHOD_RGB_STATUS)));
    }

    @Test
    void getLightStatusLightProfileLoopsLightGetStatusPerChannel() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(4));
        rpc.getLightStatus();

        assertThat(rpc.calledMethods, is(List.of(SHELLYRPC_METHOD_LIGHT_STATUS, SHELLYRPC_METHOD_LIGHT_STATUS,
                SHELLYRPC_METHOD_LIGHT_STATUS, SHELLYRPC_METHOD_LIGHT_STATUS)));
        assertThat(rpc.calledParams.get(0).id, is(0));
        assertThat(rpc.calledParams.get(1).id, is(1));
        assertThat(rpc.calledParams.get(2).id, is(2));
        assertThat(rpc.calledParams.get(3).id, is(3));
    }

    @Test
    void setLightParmDelegatesToSetLightParms() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGBW));
        rpc.setLightParm(0, SHELLY_LIGHT_TURN, SHELLY_API_ON);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_RGBW_SET));
        assertThat(rpc.lastParams().on, is(true));
    }

    @Test
    void setAutoTimerRgbwProfileSendsRgbwSetConfig() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGBW));
        rpc.setAutoTimer(0, SHELLY_TIMER_AUTOON, 30);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_RGBW_SETCONFIG));
        assertThat(rpc.lastParams().config.autoOn, is(true));
        assertThat(rpc.lastParams().config.autoOnDelay, is(30.0));
    }

    @Test
    void setAutoTimerRgbProfileSendsRgbSetConfig() throws ShellyApiException {
        StubApiRpc rpc = newRpc(colorModeProfile(SHELLY2_PROFILE_RGB));
        rpc.setAutoTimer(0, SHELLY_TIMER_AUTOOFF, 15);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_RGB_SETCONFIG));
        assertThat(rpc.lastParams().config.autoOff, is(true));
        assertThat(rpc.lastParams().config.autoOffDelay, is(15.0));
    }

    @Test
    void setAutoTimerLightProfileSendsLightSetConfig() throws ShellyApiException {
        StubApiRpc rpc = newRpc(lightModeProfile(4));
        rpc.setAutoTimer(2, SHELLY_TIMER_AUTOON, 10);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_LIGHT_SETCONFIG));
        assertThat(rpc.lastParams().config.autoOn, is(true));
    }

    @Test
    void setLightParmsCctx2ProfileSendsCctSetWithCt() throws ShellyApiException {
        StubApiRpc rpc = newRpc(cctx2Profile(2));
        rpc.setLightParms(1, Map.of(SHELLY_COLOR_TEMP, "4200", SHELLY_LIGHT_TURN, SHELLY_API_ON));

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_CCT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.id, is(1));
        assertThat(params.ct, is(4200));
        assertThat(params.on, is(true));
    }

    @Test
    void setBrightnessCctx2ProfileSendsCctSet() throws ShellyApiException {
        StubApiRpc rpc = newRpc(cctx2Profile(2));
        rpc.setBrightness(1, 42, true);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_CCT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.brightness, is(42));
        assertThat(params.on, is(true));
    }

    @Test
    void getLightStatusCctx2ProfileLoopsCctGetStatusPerChannel() throws ShellyApiException {
        StubApiRpc rpc = newRpc(cctx2Profile(2));
        rpc.getLightStatus();

        assertThat(rpc.calledMethods, is(List.of(SHELLYRPC_METHOD_CCT_STATUS, SHELLYRPC_METHOD_CCT_STATUS)));
        assertThat(rpc.calledParams.get(0).id, is(0));
        assertThat(rpc.calledParams.get(1).id, is(1));
    }

    @Test
    void getLightStatusCctx2ProfilePropagatesCtIntoLightChannelTemp() throws ShellyApiException {
        StubApiRpc rpc = newRpc(cctx2Profile(2));
        Shelly2DeviceStatusLight ls0 = new Shelly2DeviceStatusLight();
        ls0.ct = 2700;
        Shelly2DeviceStatusLight ls1 = new Shelly2DeviceStatusLight();
        ls1.ct = 6500;
        rpc.cannedResponses.add(ls0);
        rpc.cannedResponses.add(ls1);

        ShellyStatusLight status = rpc.getLightStatus();

        assertThat(status.lights.get(0).temp, is(2700));
        assertThat(status.lights.get(1).temp, is(6500));
    }

    @Test
    void getLightStatusIndexedCctx2ProfileCallsCctGetStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(cctx2Profile(2));
        rpc.getLightStatus(1);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_CCT_STATUS));
        assertThat(rpc.lastParams().id, is(1));
    }

    @Test
    void getLightStatusRgbcctProfilePollsRgbAndCctWithComponentLocalIds() throws ShellyApiException {
        StubApiRpc rpc = newRpc(rgbcctProfile());
        rpc.getLightStatus();

        assertThat(rpc.calledMethods, is(List.of(SHELLYRPC_METHOD_RGB_STATUS, SHELLYRPC_METHOD_CCT_STATUS)));
        assertThat(rpc.calledParams.get(0).id, is(0)); // settings.lights[0] -> RGB:0
        assertThat(rpc.calledParams.get(1).id, is(0)); // settings.lights[1] -> CCT:0, not CCT:1
    }

    @Test
    void getLightStatusIndexedRgbcctSecondaryChannelCallsCctGetStatusWithComponentLocalId() throws ShellyApiException {
        StubApiRpc rpc = newRpc(rgbcctProfile());
        rpc.getLightStatus(1);

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_CCT_STATUS));
        assertThat(rpc.lastParams().id, is(0)); // settings.lights[1] -> CCT:0
    }

    @Test
    void setBrightnessRgbcctSecondaryChannelSendsCctSetWithComponentLocalId() throws ShellyApiException {
        StubApiRpc rpc = newRpc(rgbcctProfile());
        rpc.setBrightness(1, 42, true); // settings.lights[1] -> CCT:0

        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_CCT_SET));
        assertThat(rpc.lastParams().id, is(0));
    }

    @Test
    void setLightParmsRgbx2lightSecondaryChannelsSendLightSetWithComponentLocalIds() throws ShellyApiException {
        StubApiRpc rpc = newRpc(rgbx2lightProfile());

        rpc.setLightParms(1, Map.of(SHELLY_COLOR_BRIGHTNESS, "50")); // settings.lights[1] -> Light:0
        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_LIGHT_SET));
        assertThat(rpc.lastParams().id, is(0));

        rpc.setLightParms(2, Map.of(SHELLY_COLOR_BRIGHTNESS, "50")); // settings.lights[2] -> Light:1
        assertThat(rpc.lastMethod(), is(SHELLYRPC_METHOD_LIGHT_SET));
        assertThat(rpc.lastParams().id, is(1));
    }

    @Test
    void getLightStatusRgbx2lightProfilePollsRgbAndBothLightComponentsWithComponentLocalIds()
            throws ShellyApiException {
        StubApiRpc rpc = newRpc(rgbx2lightProfile());
        rpc.getLightStatus();

        assertThat(rpc.calledMethods,
                is(List.of(SHELLYRPC_METHOD_RGB_STATUS, SHELLYRPC_METHOD_LIGHT_STATUS, SHELLYRPC_METHOD_LIGHT_STATUS)));
        assertThat(rpc.calledParams.get(0).id, is(0)); // settings.lights[0] -> RGB:0
        assertThat(rpc.calledParams.get(1).id, is(0)); // settings.lights[1] -> Light:0
        assertThat(rpc.calledParams.get(2).id, is(1)); // settings.lights[2] -> Light:1, not Light:2
    }
}
