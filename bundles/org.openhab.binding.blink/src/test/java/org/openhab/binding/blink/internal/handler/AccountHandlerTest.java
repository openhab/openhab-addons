/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.blink.internal.handler;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.blink.internal.BlinkTestUtil;
import org.openhab.binding.blink.internal.config.AccountConfiguration;
import org.openhab.binding.blink.internal.config.CameraConfiguration;
import org.openhab.binding.blink.internal.discovery.BlinkDiscoveryService;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCamera;
import org.openhab.binding.blink.internal.dto.BlinkHomescreen;
import org.openhab.binding.blink.internal.dto.BlinkNetwork;
import org.openhab.binding.blink.internal.service.AccountService;
import org.openhab.binding.blink.internal.servlet.AccountVerificationServlet;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import com.google.gson.Gson;

/**
 * Test class.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
class AccountHandlerTest extends JavaTest {

    private static final String CLIENT_ID = "CLIENT_1234";
    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("blink", "account");
    @Mock
    @NonNullByDefault({})
    HttpService httpService;
    @Mock
    @NonNullByDefault({})
    HttpClientFactory httpClientFactory;
    @Mock
    @NonNullByDefault({})
    AccountService accountService;
    @Mock
    @NonNullByDefault({})
    ThingHandlerCallback callback;
    @Mock
    @NonNullByDefault({})
    BundleContext bundleContext;
    @Mock
    @NonNullByDefault({})
    NetworkAddressService networkAddressService;
    @Spy
    Bridge bridge = new BridgeImpl(THING_TYPE_UID, CLIENT_ID);

    @NonNullByDefault({})
    AccountHandler accountHandler;

    @BeforeEach
    void setup() {
        when(httpClientFactory.getCommonHttpClient()).thenReturn(new HttpClient());
        Configuration config = new Configuration();
        config.put("email", "dasschaf@hurz.com");
        config.put("password", "derwolf");
        config.put("refreshInterval", 30);
        when(bridge.getConfiguration()).thenReturn(config);
        accountHandler = spy(new AccountHandler(bridge, httpService, bundleContext, networkAddressService,
                httpClientFactory, new Gson()));
    }

    @Test
    void test2FACompletedInitialization() throws IOException {
        accountHandler.blinkService = accountService;
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        account.account.client_verification_required = false;
        doReturn(account).when(accountService).login(any(), anyString(), anyBoolean());
        doCallRealMethod().when(accountService).generateClientId();
        accountHandler.setCallback(callback);
        accountHandler.initialize();
        waitForAssert(() -> {
            if (accountHandler.accountServlet == null)
                fail("accountServlet is null");
            ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
            verify(callback, atLeastOnce()).statusUpdated(eq(bridge), statusCaptor.capture());
            assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
        });
    }

    @Test
    void testNewClientIdCauses2FASend() throws IOException {
        accountHandler.blinkService = accountService;
        doAnswer(invocation -> {
            BlinkAccount account = BlinkTestUtil.testBlinkAccount();
            account.account.client_verification_required = invocation.getArgument(2);
            return account;
        }).when(accountService).login(any(), anyString(), anyBoolean());
        doCallRealMethod().when(accountService).generateClientId();
        accountHandler.setCallback(callback);
        accountHandler.initialize();
        waitForAssert(() -> {
            try {
                verify(accountService).generateClientId();
                verify(accountService).login(eq(accountHandler.config), anyString(), eq(true));
                ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
                verify(callback, atLeastOnce()).statusUpdated(eq(bridge), statusCaptor.capture());
                assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
                assertThat(statusCaptor.getValue().getStatusDetail(), is(ThingStatusDetail.CONFIGURATION_PENDING));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        });
    }

    @Test
    void testExistingClientIdDoesNotCause2FASend() throws IOException {
        accountHandler.blinkService = accountService;
        doAnswer(invocation -> {
            BlinkAccount account = BlinkTestUtil.testBlinkAccount();
            account.account.client_verification_required = invocation.getArgument(2);
            return account;
        }).when(accountService).login(any(), anyString(), anyBoolean());
        Map<String, String> thingProps = Map.of("generatedClientId", CLIENT_ID);
        doReturn(thingProps).when(bridge).getProperties();
        accountHandler.initialize();
        waitForAssert(() -> {
            try {
                verify(accountService, never()).generateClientId();
                verify(accountService).login(eq(accountHandler.config), anyString(), eq(false));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        });
    }

    @Test
    void testDispose() {
        AccountVerificationServlet servlet = mock(AccountVerificationServlet.class);
        accountHandler.accountServlet = servlet;
        accountHandler.blinkService = accountService;
        accountHandler.dispose();
        verify(accountHandler).cleanup();
        verify(servlet).dispose();
        verify(accountService).dispose();
        // noinspection ConstantConditions
        assertThat(accountHandler.accountServlet, is(nullValue()));
    }

    @Test
    void testSetOnlineCacheCreatedAndStatusOnline() throws NoSuchFieldException, IllegalAccessException, IOException {
        accountHandler.setCallback(callback);
        AccountConfiguration config = new AccountConfiguration();
        accountHandler.config = config;
        accountHandler.config.refreshInterval = 30;
        accountHandler.blinkService = accountService;
        BlinkHomescreen homescreen = testBlinkHomescreen();
        doReturn(homescreen).when(accountService).getDevices(any());
        accountHandler.setOnline();
        // cache set
        // noinspection ConstantConditions
        assertThat(accountHandler.cachedHomescreen, is(sameInstance(homescreen)));
        // jobs created
        assertThat(accountHandler.refreshStateJob, is(notNullValue()));
        assertThat(accountHandler.refreshTokenJob, is(notNullValue()));
        // cache expiry
        long diffInterval = accountHandler.config.refreshInterval
                - accountHandler.refreshStateJob.getDelay(TimeUnit.SECONDS);
        assertThat("Delay is not within five seconds of refreshInterval", diffInterval, is(lessThan(5L)));
        // thing online
        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(any(), statusCaptor.capture());
        assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
    }

    BlinkHomescreen testBlinkHomescreen() {
        BlinkHomescreen homescreen = new BlinkHomescreen();
        homescreen.networks = new ArrayList<>();
        homescreen.cameras = new ArrayList<>();
        return homescreen;
    }

    @Test
    void testRefreshStateLoadsDevices() {
        accountHandler.config = new AccountConfiguration();
        accountHandler.config.refreshInterval = 120;
        assertThat(accountHandler.refreshStateJob, is(nullValue()));
        doNothing().when(accountHandler).loadDevices();
        accountHandler.refreshState(false);
        verify(accountHandler).loadDevices();
        assertThat(accountHandler.refreshStateJob, is(notNullValue()));
    }

    @Test
    void testGetDevicesRefreshRefreshesCache() {
        doNothing().when(accountHandler).refreshState(anyBoolean());
        accountHandler.getDevices(true);
        verify(accountHandler, times(1)).refreshState(false);
        accountHandler.getDevices(false);
        verify(accountHandler, times(1)).refreshState(false);
    }

    @Test
    void testLoadDevicesCachesNullOnException() throws IOException {
        accountHandler.blinkService = accountService;
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        doThrow(IOException.class).when(accountService).getDevices(ArgumentMatchers.any(BlinkAccount.class));
        accountHandler.loadDevices();
        // noinspection ConstantConditions
        assertThat(accountHandler.cachedHomescreen, is(nullValue()));
    }

    @Test
    void testLoadDevicesCallsServiceAndThings() throws IOException {
        CameraHandler cameraHandler = mock(CameraHandler.class);
        Thing camera = new ThingImpl(CameraHandlerTest.THING_TYPE_UID, "camera");
        camera.setHandler(cameraHandler);
        doReturn(List.of(camera)).when(bridge).getThings();
        accountHandler.blinkService = accountService;
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen expected = testBlinkHomescreen();
        doReturn(expected).when(accountService).getDevices(ArgumentMatchers.any(BlinkAccount.class));
        accountHandler.loadDevices();
        verify(accountService).getDevices(accountHandler.blinkAccount);
        verify(cameraHandler).handleHomescreenUpdate();
        // noinspection ConstantConditions
        assertThat(accountHandler.cachedHomescreen, is(expected));
    }

    @Test
    void testCameraStateErrorWhenNoAccountIsSet() {
        BlinkHomescreen homescreen = testBlinkHomescreen();
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        CameraConfiguration camera = new CameraConfiguration();
        assertThrows(IOException.class, () -> accountHandler.getCameraState(camera, true));
    }

    @Test
    void testCameraStateErrorWhenNoHomescreenOrNoCameras() {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        doReturn(null).when(accountHandler).getDevices(anyBoolean());
        CameraConfiguration camera = new CameraConfiguration();
        IOException exception = assertThrows(IOException.class, () -> accountHandler.getCameraState(camera, true));
        assertThat(exception.getMessage(), is("No cameras found for account"));
        BlinkHomescreen homescreen = testBlinkHomescreen();
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        exception = assertThrows(IOException.class, () -> accountHandler.getCameraState(camera, true));
        assertThat(exception.getMessage(), is("No cameras found for account"));
    }

    @Test
    void testCameraStateErrorWhenCameraNotFound() {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen homescreen = testBlinkHomescreen();
        homescreen.cameras = List.of(new BlinkCamera(123L, 567L), new BlinkCamera(123L, 567L));
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        CameraConfiguration camera = new CameraConfiguration();
        camera.cameraId = 678L;
        camera.networkId = 123L;
        IOException exception = assertThrows(IOException.class, () -> accountHandler.getCameraState(camera, true));
        assertThat(exception.getMessage(), is("Unknown camera"));
    }

    @Test
    void testCameraStateErrorWhenTwoCamerasWithSameIdInSameNetwork() {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen homescreen = testBlinkHomescreen();
        homescreen.cameras = List.of(new BlinkCamera(123L, 567L), new BlinkCamera(123L, 567L));
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        CameraConfiguration camera = new CameraConfiguration();
        camera.cameraId = 567L;
        camera.networkId = 123L;
        IOException exception = assertThrows(IOException.class, () -> accountHandler.getCameraState(camera, true));
        assertThat(exception.getMessage(), Matchers.startsWith("More than one"));
    }

    @Test
    void testCameraStateResultWhenTwoCamerasWithSameIdInDifferentNetworks() throws IOException {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen homescreen = testBlinkHomescreen();
        homescreen.cameras = List.of(new BlinkCamera(123L, 567L), new BlinkCamera(234L, 567L));
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        CameraConfiguration camera = new CameraConfiguration();
        camera.cameraId = 567L;
        camera.networkId = 123L;
        assertThat(accountHandler.getCameraState(camera, true).id, is(camera.cameraId));
    }

    @Test
    void testNetworkStateErrorWhenNoAccountIsSet() {
        BlinkHomescreen homescreen = testBlinkHomescreen();
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        assertThrows(IOException.class, () -> accountHandler.getNetworkState("123", true));
    }

    @Test
    void testNetworkStateErrorWhenNoHomescreenOrNoNetworks() {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        doReturn(null).when(accountHandler).getDevices(anyBoolean());
        IOException exception = assertThrows(IOException.class, () -> accountHandler.getNetworkState("123", true));
        assertThat(exception.getMessage(), is("No networks found for account"));
        BlinkHomescreen homescreen = testBlinkHomescreen();
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        exception = assertThrows(IOException.class, () -> accountHandler.getNetworkState("123", true));
        assertThat(exception.getMessage(), is("No networks found for account"));
    }

    @Test
    void testNetworkStateErrorWhenNetworkNotFound() {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen homescreen = testBlinkHomescreen();
        homescreen.networks = List.of(new BlinkNetwork(123L), new BlinkNetwork(234L));
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        IOException exception = assertThrows(IOException.class, () -> accountHandler.getNetworkState("789", true));
        assertThat(exception.getMessage(), is("Unknown network"));
    }

    @Test
    void testNetworkStateErrorWhenTwoNetworksWithTheSameId() {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen homescreen = testBlinkHomescreen();
        homescreen.networks = List.of(new BlinkNetwork(123L), new BlinkNetwork(234L), new BlinkNetwork(234L));
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        IOException exception = assertThrows(IOException.class, () -> accountHandler.getNetworkState("234", true));
        assertThat(exception.getMessage(), Matchers.startsWith("More than one"));
    }

    @Test
    void testNetworkStateErrorOnNumberFormatException() {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen homescreen = testBlinkHomescreen();
        homescreen.networks = List.of(new BlinkNetwork(123L), new BlinkNetwork(234L), new BlinkNetwork(234L));
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        IOException exception = assertThrows(IOException.class, () -> accountHandler.getNetworkState("hurz", true));
        assertThat(exception.getMessage(), Matchers.startsWith("Unknown network"));
    }

    @Test
    void testNetworkStateResult() throws IOException {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen homescreen = testBlinkHomescreen();
        homescreen.networks = List.of(new BlinkNetwork(123L), new BlinkNetwork(234L));
        doReturn(homescreen).when(accountHandler).getDevices(anyBoolean());
        assertThat(accountHandler.getNetworkState("123", true).id, is(123L));
    }

    @Test
    void testGetBatteryStatusLowOFF() throws IOException {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkCamera apiCamera = new BlinkCamera(123L, 456L);
        apiCamera.battery = "ok";
        doReturn(apiCamera).when(accountHandler).getCameraState(any(), anyBoolean());
        assertThat(accountHandler.getBattery(new CameraConfiguration()), is(OnOffType.OFF));
    }

    @Test
    void testGetBatteryStatusLowON() throws IOException {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkCamera apiCamera = new BlinkCamera(123L, 456L);
        apiCamera.battery = "somethingelse";
        doReturn(apiCamera).when(accountHandler).getCameraState(any(), anyBoolean());
        assertThat(accountHandler.getBattery(new CameraConfiguration()), is(OnOffType.ON));
    }

    @Test
    void testDiscoveryServiceRegistration() {
        assertThat(accountHandler.getServices(), contains(BlinkDiscoveryService.class));
    }
}
