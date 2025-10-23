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
import static org.openhab.binding.onecta.internal.OnectaIndoorUnitConstants.*;

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
public class OnectaIndoorUnitHandlerTest {

    private OnectaIndoorUnitHandler handler;

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
        handler = new OnectaIndoorUnitHandler(thingMock);
        handler.setCallback(callbackMock);

        handler.handleCommand(channelUIDMock, OpenClosedType.OPEN);
        // add Mock dataTransServiceMock to handler
        Field privateDataTransServiceField = OnectaIndoorUnitHandler.class.getDeclaredField("dataTransService");
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

        when(dataTransServiceMock.getModelInfo()).thenReturn("DaikinModel");
        when(dataTransServiceMock.getSoftwareVersion()).thenReturn("1-2-3");
        when(dataTransServiceMock.getEepromVerion()).thenReturn("2012351");
        when(dataTransServiceMock.getDryKeepSetting()).thenReturn("ON");
        when(dataTransServiceMock.getFanMotorRotationSpeed()).thenReturn(19.2);
        when(dataTransServiceMock.getDeltaD()).thenReturn(20.2);
        when(dataTransServiceMock.getHeatExchangerTemperature()).thenReturn(21.2);
        when(dataTransServiceMock.getSuctionTemperature()).thenReturn(22.2);

        handler.refreshDevice();

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_ISKEEPDRY),
                OnOffType.from("ON"));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_FANSPEED),
                new DecimalType(19.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_DELTAD),
                new DecimalType(20.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_HEATEXCHANGETEMP),
                new DecimalType(21.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_SUCTIONTEMP),
                new DecimalType(22.2));
    }

    @Test
    public void refreshDeviceUndefTest() {
        when(dataTransServiceMock.isAvailable()).thenReturn(true);

        when(dataTransServiceMock.getModelInfo()).thenReturn(null);
        when(dataTransServiceMock.getSoftwareVersion()).thenReturn(null);
        when(dataTransServiceMock.getEepromVerion()).thenReturn(null);
        when(dataTransServiceMock.getDryKeepSetting()).thenReturn(null);
        when(dataTransServiceMock.getFanMotorRotationSpeed()).thenReturn(null);
        when(dataTransServiceMock.getDeltaD()).thenReturn(null);
        when(dataTransServiceMock.getHeatExchangerTemperature()).thenReturn(null);
        when(dataTransServiceMock.getSuctionTemperature()).thenReturn(null);

        handler.refreshDevice();

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_ISKEEPDRY),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_FANSPEED),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_DELTAD),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_HEATEXCHANGETEMP),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_SUCTIONTEMP),
                UnDefType.UNDEF);
    }
}
