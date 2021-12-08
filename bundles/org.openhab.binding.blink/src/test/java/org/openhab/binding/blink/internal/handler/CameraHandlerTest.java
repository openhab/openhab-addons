package org.openhab.binding.blink.internal.handler;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.blink.internal.BlinkTestUtil;
import org.openhab.binding.blink.internal.config.CameraConfiguration;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCamera;
import org.openhab.binding.blink.internal.service.CameraService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.Gson;

@SuppressWarnings("ConstantConditions")
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class CameraHandlerTest {

    private static final String CAMERA_ID = "123";
    private static final String NETWORK_ID = "567";
    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("blink", "camera");
    private static final ChannelUID CHANNEL_CAMERA_TEMPERATURE = new ChannelUID(new ThingUID(THING_TYPE_UID, CAMERA_ID),
            "temperature");
    private static final ChannelUID CHANNEL_CAMERA_BATTERY = new ChannelUID(new ThingUID(THING_TYPE_UID, CAMERA_ID),
            "battery");
    private static final ChannelUID CHANNEL_CAMERA_MOTIONDETECTION = new ChannelUID(
            new ThingUID(THING_TYPE_UID, CAMERA_ID), "motiondetection");
    private static final ChannelUID CHANNEL_CAMERA_SETTHUMBNAIL = new ChannelUID(
            new ThingUID(THING_TYPE_UID, CAMERA_ID), "setThumbnail");
    private static final ChannelUID CHANNEL_CAMERA_GETTHUMBNAIL = new ChannelUID(
            new ThingUID(THING_TYPE_UID, CAMERA_ID), "getThumbnail");
    @NonNullByDefault({})
    CameraHandler cameraHandler;
    @Mock
    @NonNullByDefault({})
    ThingHandlerCallback callback;

    @Spy
    Thing thing = new ThingImpl(THING_TYPE_UID, CAMERA_ID);
    @Mock
    @NonNullByDefault({})
    HttpClientFactory httpClientFactory;
    @Mock
    @NonNullByDefault({})
    Bridge account;
    @Mock
    @NonNullByDefault({})
    AccountHandler accountHandler;

    @BeforeEach
    void setup() {
        when(httpClientFactory.getCommonHttpClient()).thenReturn(new HttpClient());
        Configuration config = new Configuration();
        config.put("cameraId", CAMERA_ID);
        config.put("networkId", NETWORK_ID);
        when(thing.getConfiguration()).thenReturn(config);
        cameraHandler = new CameraHandler(thing, httpClientFactory, new Gson()) {
            @SuppressWarnings("ConstantConditions")
            @Override
            protected @Nullable Bridge getBridge() {
                return account;
            }
        };
        cameraHandler.setCallback(callback);
        cameraHandler.initialize();
    }

    @Test
    void testInitialize() {
        assertThat(cameraHandler.config, is(notNullValue()));
        assertThat(cameraHandler.config.cameraId, is(Long.parseLong(CAMERA_ID)));
        assertThat(cameraHandler.config.networkId, is(Long.parseLong(NETWORK_ID)));
        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(eq(thing), statusCaptor.capture());
        assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    void testSetOfflineWhenBridgeIsNull() {
        cameraHandler.handleCommand(new ChannelUID("1:2:3:4"), RefreshType.REFRESH);
        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(thing), statusCaptor.capture());
        assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
    }

    @Test
    void testSetOfflineOnHandleCommandException() throws IOException {
        doReturn(accountHandler).when(account).getHandler();
        doThrow(IOException.class).when(accountHandler).getTemperature(any());
        cameraHandler.handleCommand(CHANNEL_CAMERA_TEMPERATURE, RefreshType.REFRESH);
        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(thing), statusCaptor.capture());
        assertThat(statusCaptor.getValue().getStatus(), is(ThingStatus.OFFLINE));
    }

    @Test
    void testRefreshTemperatureChannel() throws IOException {
        doReturn(accountHandler).when(account).getHandler();
        double toBeReturned = 25.0;
        doReturn(toBeReturned).when(accountHandler).getTemperature(any());
        cameraHandler.handleCommand(CHANNEL_CAMERA_TEMPERATURE, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(accountHandler).getTemperature(cameraHandler.config);
        verify(callback).stateUpdated(eq(CHANNEL_CAMERA_TEMPERATURE), stateCaptor.capture());
        assertThat(stateCaptor.getValue(), is(new DecimalType(toBeReturned)));
    }

    @Test
    void testRefreshBatteryChannel() throws IOException {
        doReturn(accountHandler).when(account).getHandler();
        doReturn(OnOffType.ON).when(accountHandler).getBattery(any());
        cameraHandler.handleCommand(CHANNEL_CAMERA_BATTERY, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(accountHandler).getBattery(cameraHandler.config);
        verify(callback).stateUpdated(eq(CHANNEL_CAMERA_BATTERY), stateCaptor.capture());
        assertThat(stateCaptor.getValue(), is(OnOffType.ON));
    }

    @Test
    void testRefreshMotionDetectionChannel() throws IOException {
        doReturn(accountHandler).when(account).getHandler();
        doReturn(OnOffType.ON).when(accountHandler).getMotionDetection(any(), eq(false));
        cameraHandler.handleCommand(CHANNEL_CAMERA_MOTIONDETECTION, RefreshType.REFRESH);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(accountHandler).getMotionDetection(cameraHandler.config, false);
        verify(callback).stateUpdated(eq(CHANNEL_CAMERA_MOTIONDETECTION), stateCaptor.capture());
        assertThat(stateCaptor.getValue(), is(OnOffType.ON));
    }

    @Test
    void testOnOffMotionDetectionChannel() throws IOException {
        doReturn(accountHandler).when(account).getHandler();
        BlinkAccount blinkAccount = BlinkTestUtil.testBlinkAccount();
        doReturn(blinkAccount).when(accountHandler).getBlinkAccount();
        CameraService cameraService = mock(CameraService.class);
        cameraHandler.cameraService = cameraService;
        doReturn(123L).when(cameraService).motionDetection(ArgumentMatchers.any(BlinkAccount.class),
                ArgumentMatchers.any(CameraConfiguration.class), anyBoolean());
        cameraHandler.handleCommand(CHANNEL_CAMERA_MOTIONDETECTION, OnOffType.ON);
        verify(cameraService).motionDetection(blinkAccount, cameraHandler.config, true);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(CHANNEL_CAMERA_MOTIONDETECTION), stateCaptor.capture());
        assertThat(stateCaptor.getValue(), is(OnOffType.ON));
    }

    @Test
    void testSetThumbnailChannel() throws IOException {
        doReturn(accountHandler).when(account).getHandler();
        BlinkAccount blinkAccount = BlinkTestUtil.testBlinkAccount();
        doReturn(blinkAccount).when(accountHandler).getBlinkAccount();
        CameraService cameraService = mock(CameraService.class);
        cameraHandler.cameraService = cameraService;
        doReturn(123L).when(cameraService).createThumbnail(ArgumentMatchers.any(BlinkAccount.class),
                ArgumentMatchers.any(CameraConfiguration.class));
        cameraHandler.handleCommand(CHANNEL_CAMERA_SETTHUMBNAIL, OnOffType.ON);
        verify(cameraService).createThumbnail(blinkAccount, cameraHandler.config);
    }

    @Test
    void testGetThumbnailChannel() throws IOException {
        doReturn(accountHandler).when(account).getHandler();
        BlinkAccount blinkAccount = BlinkTestUtil.testBlinkAccount();
        doReturn(blinkAccount).when(accountHandler).getBlinkAccount();
        CameraService cameraService = mock(CameraService.class);
        cameraHandler.cameraService = cameraService;
        BlinkCamera camera = new BlinkCamera(123L, 234L);
        camera.thumbnail = "/full/path/to/thumbnail.jpg";
        doReturn(camera).when(accountHandler).getCameraState(ArgumentMatchers.any(CameraConfiguration.class), eq(true));
        byte[] bytes = "expected".getBytes(StandardCharsets.UTF_8);
        RawType expected = new RawType(bytes, "image/jpeg");
        doReturn(bytes).when(cameraService).getThumbnail(ArgumentMatchers.any(BlinkAccount.class), anyString());
        cameraHandler.handleCommand(CHANNEL_CAMERA_GETTHUMBNAIL, RefreshType.REFRESH);
        verify(accountHandler).getCameraState(cameraHandler.config, true);
        verify(cameraService).getThumbnail(blinkAccount, camera.thumbnail);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(CHANNEL_CAMERA_GETTHUMBNAIL), stateCaptor.capture());
        assertThat(stateCaptor.getValue(), is(expected));
    }
}
