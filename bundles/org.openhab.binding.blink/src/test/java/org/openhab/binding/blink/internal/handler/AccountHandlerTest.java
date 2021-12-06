package org.openhab.binding.blink.internal.handler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.BridgeImpl;
import org.osgi.service.http.HttpService;
import com.google.gson.Gson;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
class AccountHandlerTest extends JavaTest {

    private static final String CLIENT_ID = "CLIENT_1234";
    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("blink", "account");
    @Mock @NonNullByDefault({}) HttpService httpService;
    @Mock @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    @Mock @NonNullByDefault({}) AccountService accountService;
    @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    @Mock @NonNullByDefault({}) ExpiringCache<BlinkHomescreen> cache;
    @Spy Bridge bridge = new BridgeImpl(THING_TYPE_UID, CLIENT_ID);

    @NonNullByDefault({}) AccountHandler accountHandler;

    @BeforeEach
    void setup() {
        when(httpClientFactory.getCommonHttpClient()).thenReturn(new HttpClient());
        Configuration config = new Configuration();
        config.put("email", "dasschaf@hurz.com");
        config.put("password", "derwolf");
        config.put("refreshInterval", 30);
        when(bridge.getConfiguration()).thenReturn(config);
        accountHandler = spy(new AccountHandler(bridge, httpService, httpClientFactory, new Gson()));
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
            assertThat(accountHandler.accountServlet, is(notNullValue()));
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
        accountHandler.dispose();
        verify(accountHandler).cleanup();
        verify(servlet).dispose();
        assertThat(accountHandler.accountServlet, is(nullValue()));
    }

    @Test
    void testSetOnlineCacheCreatedAndStatusOnline() throws NoSuchFieldException, IllegalAccessException {
        accountHandler.setCallback(callback);
        AccountConfiguration config = new AccountConfiguration();
        config.refreshInterval = 30;
        accountHandler.config = config;
        accountHandler.setOnline();
        // cache set
        assertThat(accountHandler.homescreenCache, is(notNullValue()));
        // cache expiry
        Field expiry = ExpiringCache.class.getDeclaredField("expiry");
        expiry.setAccessible(true);
        assertThat(expiry.get(accountHandler.homescreenCache), is((long) config.refreshInterval * 1000 * 1000 * 1000));
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
    void testGetDevicesRefreshRefreshesCache() {
        accountHandler.homescreenCache = cache;
        when(cache.refreshValue()).thenReturn(testBlinkHomescreen());
        when(cache.getValue()).thenReturn(testBlinkHomescreen());
        accountHandler.getDevices(true);
        verify(cache).refreshValue();
        accountHandler.getDevices(false);
        verify(cache).getValue();
    }

    @Test
    void testLoadDevicesReturnsNullOnException() throws IOException {
        accountHandler.blinkService = accountService;
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        doThrow(IOException.class).when(accountService).getDevices(ArgumentMatchers.any(BlinkAccount.class));
        assertThat(accountHandler.loadDevices(), is(nullValue()));
    }

    @Test
    void testLoadDevicesCallsService() throws IOException {
        accountHandler.blinkService = accountService;
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen expected = testBlinkHomescreen();
        doReturn(expected).when(accountService).getDevices(ArgumentMatchers.any(BlinkAccount.class));
        @Nullable BlinkHomescreen actual = accountHandler.loadDevices();
        verify(accountService).getDevices(accountHandler.blinkAccount);
        assertThat(actual, is(expected));
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
        BlinkHomescreen homescreen = testBlinkHomescreen();
        BlinkCamera apiCamera = new BlinkCamera(123L, 456L);
        apiCamera.battery = "ok";
        doReturn(apiCamera).when(accountHandler).getCameraState(any(), anyBoolean());
        assertThat(accountHandler.getBattery(new CameraConfiguration()), is(OnOffType.OFF));
    }

    @Test
    void testGetBatteryStatusLowON() throws IOException {
        accountHandler.blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen homescreen = testBlinkHomescreen();
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
