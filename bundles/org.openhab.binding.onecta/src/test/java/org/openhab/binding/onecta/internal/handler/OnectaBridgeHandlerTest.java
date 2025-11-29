/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.handler;

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

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
public class OnectaBridgeHandlerTest {

    public static final String USERID = "Userid";
    public static final String PASSWORD = "Password";
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

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        bridgeProperties.put(CONFIG_PAR_REFRESHINTERVAL, "10");
        bridgeProperties.put(CONFIG_PAR_UNITID, UNITID);

        thingConfiguration.setProperties(bridgeProperties);
        lenient().when(bridgeMock.getConfiguration()).thenReturn(thingConfiguration);

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
    public void pollDevicesOnlineTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            DaikinCommunicationException, NoSuchFieldException {

        Method privateMethod = OnectaBridgeHandler.class.getDeclaredMethod("pollDevices");
        privateMethod.setAccessible(true);

        List<Thing> things = new java.util.ArrayList<>(List.of());

        things.add(new DummyThing(THING_TYPE_CLIMATECONTROL, onectaDeviceHandlerMock, ThingStatus.ONLINE));
        things.add(new DummyThing(THING_TYPE_GATEWAY, onectaGatewayHandlerMock, ThingStatus.ONLINE));
        things.add(new DummyThing(THING_TYPE_WATERTANK, onectaWaterTankHandlerMock, ThingStatus.ONLINE));

        when(handler.getThing().getThings()).thenReturn(things);
        when(handler.getThing().getStatus()).thenReturn(ThingStatus.ONLINE);

        privateMethod.invoke(handler);

        verify(onectaConnectionClientMock).refreshUnitsData();
        verify(onectaDeviceHandlerMock).refreshDevice();
        verify(onectaGatewayHandlerMock).refreshDevice();
        verify(onectaWaterTankHandlerMock).refreshDevice();
    }

    @Test
    public void pollDevicesOfflineTest() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, DaikinCommunicationException, NoSuchFieldException {

        Method privateMethod = OnectaBridgeHandler.class.getDeclaredMethod("pollDevices");
        privateMethod.setAccessible(true);

        when(handler.getThing().getStatus()).thenReturn(ThingStatus.OFFLINE);

        List<Thing> things = new java.util.ArrayList<>(List.of());

        things.add(new DummyThing(THING_TYPE_CLIMATECONTROL, onectaDeviceHandlerMock, ThingStatus.OFFLINE));
        things.add(new DummyThing(THING_TYPE_GATEWAY, onectaGatewayHandlerMock, ThingStatus.OFFLINE));
        things.add(new DummyThing(THING_TYPE_WATERTANK, onectaWaterTankHandlerMock, ThingStatus.OFFLINE));

        handler.getThing();

        privateMethod.invoke(handler);

        // verify(callbackMock).statusUpdated(eq(bridgeMock), argThat(arg ->
        // arg.getStatus().equals(ThingStatus.OFFLINE)));
        verify(onectaConnectionClientMock, times(0)).refreshUnitsData();
        verify(onectaDeviceHandlerMock, times(0)).refreshDevice();
        verify(onectaGatewayHandlerMock, times(0)).refreshDevice();
        verify(onectaWaterTankHandlerMock, times(0)).refreshDevice();
    }
}
