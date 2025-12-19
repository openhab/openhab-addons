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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.onecta.internal.constants.OnectaGatewayConstants.*;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.service.DataTransportService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
public class OnectaGatewayHandlerTest {

    private OnectaGatewayHandler handler;

    @Mock
    private ThingHandlerCallback callbackMock;

    @Mock
    private Thing thingMock;

    @Mock
    private DataTransportService dataTransServiceMock;

    @Mock
    private ChannelUID channelUIDMock;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.setProperties(Map.of("unitID", "ThisIsDummyID", "refreshDelay", "10"));
        when(thingMock.getConfiguration()).thenReturn(thingConfiguration);
        handler = new OnectaGatewayHandler(thingMock);
        handler.setCallback(callbackMock);

        handler.handleCommand(channelUIDMock, OpenClosedType.OPEN);
        // add Mock dataTransServiceMock to handler
        Field privateDataTransServiceField = OnectaGatewayHandler.class.getDeclaredField("dataTransService");
        privateDataTransServiceField.setAccessible(true);
        privateDataTransServiceField.set(handler, dataTransServiceMock);

        lenient().when(thingMock.getUID()).thenReturn(new ThingUID("onecta", "indoorUnit", "bridge"));
    }

    @AfterEach
    public void tearDown() {
        // Free any resources, like open database connections, files etc.
        handler.dispose();
    }

    @Test
    public void initializeShouldCallTheCallback() {
        // we expect the handler#initialize method to call the callbackMock during execution and
        // pass it the thingMock and a ThingStatusInfo object containing the ThingStatus of the thingMock.
        when(dataTransServiceMock.isAvailable()).thenReturn(true);
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(thingMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
    }

    @Test
    public void refreshDeviceOkTest() {

        when(dataTransServiceMock.isAvailable()).thenReturn(true);

        when(dataTransServiceMock.getDaylightSavingTimeEnabled()).thenReturn(true);
        when(dataTransServiceMock.getFirmwareVerion()).thenReturn("Version123");
        when(dataTransServiceMock.getIsFirmwareUpdateSupported()).thenReturn(true);
        when(dataTransServiceMock.getIsInErrorState()).thenReturn(true);
        when(dataTransServiceMock.getIsLedEnabled()).thenReturn(true);
        when(dataTransServiceMock.getRegionCode()).thenReturn("RegionNL");
        when(dataTransServiceMock.getSerialNumber()).thenReturn("Serial1234");
        when(dataTransServiceMock.getSsid()).thenReturn("SSID_abcdef");
        when(dataTransServiceMock.getTimeZone()).thenReturn("Timezone_nl");
        when(dataTransServiceMock.getWifiConectionSSid()).thenReturn("WifiConection");
        when(dataTransServiceMock.getWifiConectionStrength()).thenReturn(19);

        handler.refreshDevice();

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_GW_DAYLIGHTSAVINGENABLED), OnOffType.from("ON"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_FIRMWAREVERSION),
                new StringType("Version123"));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_GW_IS_FIRMWAREUPDATE_SUPPORTED), OnOffType.from("ON"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_IS_IN_ERROR_STATE),
                OnOffType.from("ON"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_LED_ENABLED),
                OnOffType.from("ON"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_REGION_CODE),
                new StringType("RegionNL"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_SERIAL_NUMBER),
                new StringType("Serial1234"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_SSID),
                new StringType("SSID_abcdef"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_TIME_ZONE),
                new StringType("Timezone_nl"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_WIFICONNENTION_SSID),
                new StringType("WifiConection"));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_GW_WIFICONNENTION_STRENGTH), new DecimalType(19));
    }

    @Test
    public void refreshDeviceUndefTest() {
        when(dataTransServiceMock.isAvailable()).thenReturn(true);

        when(dataTransServiceMock.getDaylightSavingTimeEnabled()).thenReturn(null);
        when(dataTransServiceMock.getFirmwareVerion()).thenReturn(null);
        when(dataTransServiceMock.getIsFirmwareUpdateSupported()).thenReturn(null);
        when(dataTransServiceMock.getIsInErrorState()).thenReturn(null);
        when(dataTransServiceMock.getIsLedEnabled()).thenReturn(null);
        when(dataTransServiceMock.getRegionCode()).thenReturn(null);
        when(dataTransServiceMock.getSerialNumber()).thenReturn(null);
        when(dataTransServiceMock.getSsid()).thenReturn(null);
        when(dataTransServiceMock.getTimeZone()).thenReturn(null);
        when(dataTransServiceMock.getWifiConectionSSid()).thenReturn(null);
        when(dataTransServiceMock.getWifiConectionStrength()).thenReturn(null);

        handler.refreshDevice();

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        verify(callbackMock, times(1))
                .stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_DAYLIGHTSAVINGENABLED), UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_FIRMWAREVERSION),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_GW_IS_FIRMWAREUPDATE_SUPPORTED), UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_IS_IN_ERROR_STATE),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_LED_ENABLED),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_REGION_CODE),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_SERIAL_NUMBER),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_SSID),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_TIME_ZONE),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_WIFICONNENTION_SSID),
                UnDefType.UNDEF);
        verify(callbackMock, times(1))
                .stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_GW_WIFICONNENTION_STRENGTH), UnDefType.UNDEF);
    }
}
