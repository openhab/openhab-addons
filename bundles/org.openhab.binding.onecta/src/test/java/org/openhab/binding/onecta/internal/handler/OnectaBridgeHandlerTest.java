package org.openhab.binding.onecta.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.DummyThing;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.ThingHandlerCallback;

@ExtendWith(MockitoExtension.class)
public class OnectaBridgeHandlerTest {

    public static final String USERID = "Userid";
    public static final String PASSWORD = "Password";
    public static final String REFRESH_TOKEN = "ThisIsARefreshToken";
    public static final String UNITID = "ThisIsAUnitID";
    private OnectaBridgeHandler handler;
    Map<String, Object> bridgeProperties = new HashMap<>();
    private Configuration thingConfiguration = new Configuration();

    @Mock
    private ThingHandlerCallback callbackMock;

    @Mock
    private Bridge bridgeMock;

    @Mock
    private OnectaConnectionClient onectaConnectionClientMock;

    @Mock
    private OnectaDeviceHandler onectaDeviceHandlerMock;
    @Mock
    private OnectaGatewayHandler onectaGatewayHandlerMock;
    @Mock
    private OnectaWaterTankHandler onectaWaterTankHandlerMock;
    @Mock
    private OnectaIndoorUnitHandler onectaIndoorUnitHandlerMock;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        bridgeProperties.put(CHANNEL_REFRESH_TOKEN, REFRESH_TOKEN);
        bridgeProperties.put(CHANNEL_USERID, USERID);
        bridgeProperties.put(CHANNEL_PASSWORD, PASSWORD);
        bridgeProperties.put(CHANNEL_REFRESHINTERVAL, "10");
        bridgeProperties.put(CHANNEL_UNITID, UNITID);

        thingConfiguration.setProperties(bridgeProperties);
        when(bridgeMock.getConfiguration()).thenReturn(thingConfiguration);

        handler = new OnectaBridgeHandler(bridgeMock);
        handler.setCallback(callbackMock);

        // add Mock dataTransServiceMock to handler
        Field privateDataTransServiceField = OnectaBridgeHandler.class.getDeclaredField("onectaConnectionClient");
        privateDataTransServiceField.setAccessible(true);
        privateDataTransServiceField.set(handler, onectaConnectionClientMock);
    }

    @AfterEach
    public void tearDown() {
        handler.dispose();
    }

    @Test
    public void initializeShouldCallTheCallbackOffline() throws DaikinCommunicationException, InterruptedException {
        when(onectaConnectionClientMock.isOnline()).thenReturn(false);
        handler.initialize();

        Thread.sleep(500);
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(onectaConnectionClientMock).startConnecton(eq(USERID), eq(PASSWORD), eq(REFRESH_TOKEN));
    }

    @Test
    public void initializeShouldCallTheCallbackOfflineByException()
            throws DaikinCommunicationException, InterruptedException {
        doThrow(new DaikinCommunicationException("Connection failed")).when(onectaConnectionClientMock)
                .startConnecton(anyString(), anyString(), anyString());

        handler.initialize();
        Thread.sleep(500);
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(onectaConnectionClientMock).startConnecton(eq(USERID), eq(PASSWORD), eq(REFRESH_TOKEN));
    }

    @Test
    public void initializeShouldCallTheCallbackOnline() throws DaikinCommunicationException, InterruptedException {
        when(onectaConnectionClientMock.isOnline()).thenReturn(true);
        handler.initialize();
        Thread.sleep(500);
        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
        verify(onectaConnectionClientMock).startConnecton(eq(USERID), eq(PASSWORD), eq(REFRESH_TOKEN));
    }

    @Test
    public void pollDevicesOnlineTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            DaikinCommunicationException, NoSuchFieldException {

        Method privateMethod = OnectaBridgeHandler.class.getDeclaredMethod("pollDevices");
        privateMethod.setAccessible(true);

        when(onectaConnectionClientMock.isOnline()).thenReturn(true);
        when(onectaConnectionClientMock.getRefreshToken()).thenReturn(REFRESH_TOKEN);

        when(bridgeMock.getConfiguration()).thenReturn(thingConfiguration);

        List<Thing> things = new java.util.ArrayList<>(List.of());

        things.add(new DummyThing(DEVICE_THING_TYPE, onectaDeviceHandlerMock, ThingStatus.ONLINE));
        things.add(new DummyThing(GATEWAY_THING_TYPE, onectaGatewayHandlerMock, ThingStatus.ONLINE));
        things.add(new DummyThing(WATERTANK_THING_TYPE, onectaWaterTankHandlerMock, ThingStatus.ONLINE));
        things.add(new DummyThing(INDOORUNIT_THING_TYPE, onectaIndoorUnitHandlerMock, ThingStatus.ONLINE));

        when(handler.getThing().getThings()).thenReturn(things);

        handler.getThing();

        privateMethod.invoke(handler);

        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
        assertEquals(REFRESH_TOKEN, handler.getThing().getConfiguration().get(CHANNEL_REFRESH_TOKEN));
        verify(onectaConnectionClientMock).refreshUnitsData(eq(handler.getThing()));
        verify(onectaDeviceHandlerMock).refreshDevice();
        verify(onectaGatewayHandlerMock).refreshDevice();
        verify(onectaWaterTankHandlerMock).refreshDevice();
        verify(onectaIndoorUnitHandlerMock).refreshDevice();
    }

    @Test
    public void pollDevicesOfflineTest() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, DaikinCommunicationException, NoSuchFieldException {

        Method privateMethod = OnectaBridgeHandler.class.getDeclaredMethod("pollDevices");
        privateMethod.setAccessible(true);

        when(onectaConnectionClientMock.isOnline()).thenReturn(false);

        when(bridgeMock.getConfiguration()).thenReturn(thingConfiguration);

        List<Thing> things = new java.util.ArrayList<>(List.of());

        things.add(new DummyThing(DEVICE_THING_TYPE, onectaDeviceHandlerMock, ThingStatus.OFFLINE));
        things.add(new DummyThing(GATEWAY_THING_TYPE, onectaGatewayHandlerMock, ThingStatus.OFFLINE));
        things.add(new DummyThing(WATERTANK_THING_TYPE, onectaWaterTankHandlerMock, ThingStatus.OFFLINE));
        things.add(new DummyThing(INDOORUNIT_THING_TYPE, onectaIndoorUnitHandlerMock, ThingStatus.OFFLINE));

        when(handler.getThing().getThings()).thenReturn(things);

        handler.getThing();

        privateMethod.invoke(handler);

        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        assertEquals(REFRESH_TOKEN, handler.getThing().getConfiguration().get(CHANNEL_REFRESH_TOKEN));
        verify(onectaConnectionClientMock).refreshUnitsData(eq(handler.getThing()));
        verify(onectaDeviceHandlerMock, times(0)).refreshDevice();
        verify(onectaGatewayHandlerMock, times(0)).refreshDevice();
        verify(onectaWaterTankHandlerMock, times(0)).refreshDevice();
        verify(onectaIndoorUnitHandlerMock, times(0)).refreshDevice();
    }

    @Test
    public void pollDevicesOfflineExceptionTest() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, DaikinCommunicationException, NoSuchFieldException {

        Method privateMethod = OnectaBridgeHandler.class.getDeclaredMethod("pollDevices");
        privateMethod.setAccessible(true);

        when(onectaConnectionClientMock.isOnline()).thenReturn(true);

        when(bridgeMock.getConfiguration()).thenReturn(thingConfiguration);
        when(onectaConnectionClientMock.getRefreshToken()).thenReturn(REFRESH_TOKEN);

        doThrow(new DaikinCommunicationException("Connection failed")).when(onectaConnectionClientMock)
                .refreshUnitsData(eq(handler.getThing()));

        handler.getThing();

        privateMethod.invoke(handler);

        verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
        assertEquals(REFRESH_TOKEN, handler.getThing().getConfiguration().get(CHANNEL_REFRESH_TOKEN));
        verify(onectaConnectionClientMock).refreshUnitsData(eq(handler.getThing()));
        verify(onectaDeviceHandlerMock, times(0)).refreshDevice();
        verify(onectaGatewayHandlerMock, times(0)).refreshDevice();
        verify(onectaWaterTankHandlerMock, times(0)).refreshDevice();
        verify(onectaIndoorUnitHandlerMock, times(0)).refreshDevice();
    }
}
