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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
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
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2RGBCCTStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcRequest.Shelly2RpcRequestParams;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;

/**
 * Transport-stubbed dispatch coverage for the Gen3 Duo Bulb ({@code cct:0} only) and Multicolor Bulb
 * ({@code rgbcct:0}) profiles: verifies the actual RPC method/component selected by {@link Shelly2ApiRpc}'s public
 * entry points for status, turn, brightness, timer and parameter commands, distinguished purely by
 * {@link ShellyDeviceProfile#isRGBCCT}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class Shelly2ApiRpcDuoBulbDispatchTest {

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
            if (params instanceof Shelly2RpcRequestParams requestParams) {
                if (SHELLYRPC_METHOD_RGBCCT_SET.equals(method) && requestParams.on == null
                        && requestParams.brightness == null) {
                    throw new ShellyApiException("RGBCCT.Set requires at least one of on or brightness");
                }
                if (SHELLYRPC_METHOD_CCT_SET.equals(method) && requestParams.on == null
                        && requestParams.brightness == null && requestParams.ct == null) {
                    throw new ShellyApiException("CCT.Set requires at least one of on, brightness or ct");
                }
                calledParams.add(requestParams);
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

        String firstMethod() {
            return calledMethods.get(0);
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

    private ShellyDeviceProfile duoBulbProfile() {
        return new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSDUOBULB);
    }

    private ShellyDeviceProfile multicolorBulbProfile(boolean inColor) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSCOLORBULB);
        profile.inColor = inColor;
        return profile;
    }

    private StubApiRpc newRpc(ShellyDeviceProfile profile) {
        when(thing.getProfile()).thenReturn(profile);
        return new StubApiRpc(thing, testConfig());
    }

    @Test
    void getLightStatusDuoBulbCallsCctStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        rpc.getLightStatus();
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_CCT_STATUS));
    }

    @Test
    void getLightStatusMulticolorBulbCallsRgbcctStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(true));
        rpc.getLightStatus();
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_STATUS));
    }

    @Test
    void setLightTurnDuoBulbCallsCctSet() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        rpc.setLightTurn(0, SHELLY_API_ON);
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_CCT_SET));
        assertThat(rpc.calledParams.get(0).on, is(true));
    }

    @Test
    void setLightTurnMulticolorBulbCallsRgbcctSet() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(true));
        rpc.setLightTurn(0, SHELLY_API_OFF);
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_SET));
        assertThat(rpc.calledParams.get(0).on, is(false));
    }

    @Test
    void setBrightnessDuoBulbCallsCctSet() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        rpc.setBrightness(0, 42, true);
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_CCT_SET));
        assertThat(rpc.calledParams.get(0).brightness, is(42));
        assertThat(rpc.calledParams.get(0).on, is(true));
    }

    @Test
    void setBrightnessMulticolorBulbCallsRgbcctSet() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(true));
        rpc.setBrightness(0, 42, true);
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_SET));
        assertThat(rpc.calledParams.get(0).brightness, is(42));
    }

    @Test
    void setAutoTimerDuoBulbCallsCctSetConfig() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        rpc.setAutoTimer(0, SHELLY_TIMER_AUTOON, 30);
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_CCT_SETCONFIG));
        assertThat(rpc.lastParams().config.autoOn, is(true));
        assertThat(rpc.lastParams().config.autoOnDelay, is(30.0));
    }

    @Test
    void setAutoTimerMulticolorBulbCallsRgbcctSetConfig() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(true));
        rpc.setAutoTimer(0, SHELLY_TIMER_AUTOOFF, 15);
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_SETCONFIG));
        assertThat(rpc.lastParams().config.autoOff, is(true));
        assertThat(rpc.lastParams().config.autoOffDelay, is(15.0));
    }

    @Test
    void setLightParmsDuoBulbSendsCctSetWithColorTemp() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_TEMP, "4200", SHELLY_LIGHT_TURN, SHELLY_API_ON));

        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_CCT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.id, is(0));
        assertThat(params.ct, is(4200));
        assertThat(params.on, is(true));
    }

    @Test
    void setLightParmsMulticolorBulbInColorSendsRgbcctSetWithRgb() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(true));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_RED, "10", SHELLY_COLOR_GREEN, "20", SHELLY_COLOR_BLUE, "30"));

        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.id, is(0));
        assertThat(params.rgb, is(new Integer[] { 10, 20, 30 }));
        assertThat(params.on, is(true));
    }

    @Test
    void setLightParmsMulticolorBulbInColorModeSendsRgbcctSetWithBrightness() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(true));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_RED, "10", SHELLY_COLOR_GREEN, "20", SHELLY_COLOR_BLUE, "30",
                SHELLY_COLOR_BRIGHTNESS, "42"));

        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.rgb, is(new Integer[] { 10, 20, 30 }));
        assertThat(params.brightness, is(42));
        assertThat(params.on, is(true));
    }

    @Test
    void setLightParmsMulticolorBulbRgbOnlyRepeatsCurrentPowerState() throws ShellyApiException {
        ShellyDeviceProfile profile = multicolorBulbProfile(true);
        ShellySettingsLight light = new ShellySettingsLight();
        light.ison = false;
        profile.status.lights = new ArrayList<>(List.of(light));
        StubApiRpc rpc = newRpc(profile);
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_RED, "10", SHELLY_COLOR_GREEN, "20", SHELLY_COLOR_BLUE, "30"));

        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.rgb, is(new Integer[] { 10, 20, 30 }));
        assertThat(params.on, is(false));
    }

    @Test
    void setLightParmsDuoBulbWithoutCctParametersSendsNothing() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_RED, "10"));

        assertThat(rpc.calledMethods.size(), is(0));
    }

    @Test
    void setLightParmsMulticolorBulbInWhiteModeSendsRgbcctSetWithColorTemp() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(false));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_TEMP, "3000"));

        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.id, is(0));
        assertThat(params.ct, is(3000));
        assertThat(params.on, is(true));
    }

    @Test
    void setLightParmsMulticolorBulbKeepsExplicitOffWithColorTemp() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(false));
        rpc.setLightParms(0, Map.of(SHELLY_COLOR_TEMP, "3000", SHELLY_LIGHT_TURN, SHELLY_API_OFF));

        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.ct, is(3000));
        assertThat(params.on, is(false));
    }

    @Test
    void setLightParmsMulticolorBulbCombinesModeSwitchAndColorTempInOneRequest() throws ShellyApiException {
        ShellyDeviceProfile profile = multicolorBulbProfile(true);
        StubApiRpc rpc = newRpc(profile);
        rpc.setLightParms(0, Map.of(SHELLY_API_MODE, SHELLY_MODE_WHITE, SHELLY_COLOR_TEMP, "3000"));

        assertThat(rpc.calledMethods.size(), is(1));
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_SET));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.mode, is(SHELLY_RGBCCT_MODE_CCT));
        assertThat(params.ct, is(3000));
        assertThat(params.rgb, is(nullValue()));
        assertThat(params.on, is(true));
        assertThat(profile.inColor, is(false));
        assertThat(profile.device.mode, is(SHELLY_MODE_WHITE));
    }

    @Test
    void setLightParmsMulticolorBulbCombinesModeSwitchAndRgbInOneRequest() throws ShellyApiException {
        ShellyDeviceProfile profile = multicolorBulbProfile(false);
        StubApiRpc rpc = newRpc(profile);
        rpc.setLightParms(0, Map.of(SHELLY_API_MODE, SHELLY_MODE_COLOR, SHELLY_COLOR_RED, "10", SHELLY_COLOR_GREEN,
                "20", SHELLY_COLOR_BLUE, "30", SHELLY_COLOR_BRIGHTNESS, "50"));

        assertThat(rpc.calledMethods.size(), is(1));
        Shelly2RpcRequestParams params = rpc.lastParams();
        assertThat(params.mode, is(SHELLY_RGBCCT_MODE_RGB));
        assertThat(params.rgb, is(new Integer[] { 10, 20, 30 }));
        assertThat(params.ct, is(nullValue()));
        assertThat(params.brightness, is(50));
        assertThat(params.on, is(true));
        assertThat(profile.inColor, is(true));
    }

    @Test
    void setLightModeMulticolorBulbSendsRgbcctSetWithMode() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(false));
        rpc.setLightMode(SHELLY_MODE_COLOR);

        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_SET));
        assertThat(rpc.lastParams().mode, is(SHELLY_RGBCCT_MODE_RGB));
        assertThat(rpc.lastParams().on, is(true));
    }

    @Test
    void setLightModeDuoBulbThrowsNotImplemented() {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        assertThrows(ShellyApiException.class, () -> rpc.setLightMode(SHELLY_MODE_COLOR));
    }

    @Test
    void getLightStatusIndexedDuoBulbCallsCctStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        rpc.getLightStatus(0);
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_CCT_STATUS));
    }

    @Test
    void getLightStatusIndexedMulticolorBulbCallsRgbcctStatus() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(true));
        rpc.getLightStatus(0);
        assertThat(rpc.firstMethod(), is(SHELLYRPC_METHOD_RGBCCT_STATUS));
    }

    @Test
    void getLightStatusDuoBulbPropagatesBrightnessAndTemp() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        Shelly2DeviceStatusLight canned = new Shelly2DeviceStatusLight();
        canned.output = true;
        canned.brightness = 55.0;
        canned.ct = 3200;
        rpc.cannedResponses.add(canned);

        ShellyStatusLight status = rpc.getLightStatus();

        assertThat(status.ison, is(true));
        assertThat(status.lights.get(0).ison, is(true));
        assertThat(status.lights.get(0).brightness, is(55));
        assertThat(status.lights.get(0).temp, is(3200));
    }

    @Test
    void getLightStatusDuoBulbHasNoTimerWhenTimerStartedAtAbsent() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        Shelly2DeviceStatusLight canned = new Shelly2DeviceStatusLight();
        canned.output = true;
        canned.brightness = 55.0;
        canned.ct = 3200;
        rpc.cannedResponses.add(canned);

        ShellyStatusLight status = rpc.getLightStatus();

        assertThat(status.lights.get(0).hasTimer, is(false));
        assertThat(status.lights.get(0).timerDuration, is(nullValue()));
    }

    @Test
    void getLightStatusDuoBulbPropagatesTimerStarted() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        Shelly2DeviceStatusLight canned = new Shelly2DeviceStatusLight();
        canned.output = true;
        canned.brightness = 55.0;
        canned.ct = 3200;
        canned.timerStartedAt = 1000.0;
        rpc.cannedResponses.add(canned);

        ShellyStatusLight status = rpc.getLightStatus();

        assertThat(status.lights.get(0).hasTimer, is(true));
    }

    @Test
    void getLightStatusMulticolorBulbPropagatesTimerStarted() throws ShellyApiException {
        StubApiRpc rpc = newRpc(multicolorBulbProfile(false));
        Shelly2RGBCCTStatus canned = new Shelly2RGBCCTStatus();
        canned.output = true;
        canned.brightness = 55.0;
        canned.ct = 3200;
        canned.timerStartedAt = 1000.0;
        rpc.cannedResponses.add(canned);

        ShellyStatusLight status = rpc.getLightStatus();

        assertThat(status.lights.get(0).hasTimer, is(true));
    }

    @Test
    void getLightStatusIndexedDuoBulbPropagatesTimerFields() throws ShellyApiException {
        StubApiRpc rpc = newRpc(duoBulbProfile());
        Shelly2DeviceStatusLight canned = new Shelly2DeviceStatusLight();
        canned.output = true;
        canned.brightness = 55.0;
        canned.ct = 3200;
        canned.timerStartedAt = 1000.0;
        rpc.cannedResponses.add(canned);

        ShellyShortLightStatus status = rpc.getLightStatus(0);

        assertThat(status.hasTimer, is(true));
    }
}
