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
import static org.openhab.binding.onecta.internal.constants.OnectaClimateControlConstants.*;
import static org.openhab.binding.onecta.internal.constants.OnectaWaterTankConstants.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.service.ChannelsRefreshDelay;
import org.openhab.binding.onecta.internal.service.DataTransportService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.*;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
public class OnectaDeviceHandlerTest {

    private OnectaDeviceHandler handler;

    @Mock
    private ThingHandlerCallback callbackMock;

    @Mock
    private Thing thingMock;

    @Mock
    private DataTransportService dataTransServiceMock;

    @Mock
    private ChannelsRefreshDelay channelsRefreshDelayMock;

    private JsonObject jsonObject;

    @BeforeEach
    public void setUp()
            throws NoSuchFieldException, IllegalAccessException, FileNotFoundException, NoSuchMethodException {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.setProperties(Map.of("unitID", "ThisIsDummyID", "refreshDelay", "10"));
        when(thingMock.getConfiguration()).thenReturn(thingConfiguration);
        handler = new OnectaDeviceHandler(thingMock);
        handler.setCallback(callbackMock);

        // add Mock dataTransServiceMock to handler
        Field privateDataTransServiceField = OnectaDeviceHandler.class.getDeclaredField("dataTransService");
        privateDataTransServiceField.setAccessible(true);
        privateDataTransServiceField.set(handler, dataTransServiceMock);

        // add Mock channelsRefreshDelayMock to handler
        Field privateChannelsRefreshDelayField = OnectaDeviceHandler.class.getDeclaredField("channelsRefreshDelay");
        privateChannelsRefreshDelayField.setAccessible(true);
        privateChannelsRefreshDelayField.set(handler, channelsRefreshDelayMock);

        lenient().when(thingMock.getUID()).thenReturn(new ThingUID("onecta", "device", "bridge"));

        Gson gson = new Gson();
        Reader reader = new FileReader("src/test/resources/airco.json");
        jsonObject = gson.fromJson(reader, JsonObject.class);
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

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_POWER)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_POWERFULMODE)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_OPERATIONMODE)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMP)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPMIN)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPMAX)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPSTEP)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP)).thenReturn(true);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANSPEED)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_ECONOMODE)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_STREAMER)).thenReturn(true);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT_HOR)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT_VER)).thenReturn(true);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_HOLIDAYMODE)).thenReturn(true);

        when(dataTransServiceMock.getPowerOnOff()).thenReturn("ON");
        when(dataTransServiceMock.getPowerfulModeOnOff()).thenReturn("ON");
        when(dataTransServiceMock.getCurrentOperationMode()).thenReturn(Enums.OperationMode.HEAT);
        when(dataTransServiceMock.getCurrentTemperatureSet()).thenReturn(19.2);
        when(dataTransServiceMock.getCurrentTemperatureSetMin()).thenReturn(10.2);
        when(dataTransServiceMock.getCurrentTemperatureSetMax()).thenReturn(20.2);
        when(dataTransServiceMock.getCurrentTemperatureSetStep()).thenReturn(0.5);

        when(dataTransServiceMock.getSetpointLeavingWaterTemperature()).thenReturn(21.2);
        when(dataTransServiceMock.getSetpointLeavingWaterOffset()).thenReturn(15.5);

        when(dataTransServiceMock.getCurrentFanDirection()).thenReturn(Enums.FanMovement.VERTICAL_AND_HORIZONTAL);
        when(dataTransServiceMock.getCurrentFanspeed()).thenReturn(Enums.FanSpeed.LEVEL_3);
        when(dataTransServiceMock.getEconoMode()).thenReturn("ON");
        when(dataTransServiceMock.getStreamerMode()).thenReturn("ON");

        when(dataTransServiceMock.getCurrentFanDirectionHor()).thenReturn(Enums.FanMovementHor.SWING);
        when(dataTransServiceMock.getCurrentFanDirectionVer()).thenReturn(Enums.FanMovementVer.WINDNICE);

        when(dataTransServiceMock.getHolidayMode()).thenReturn("ON");

        // Test updatestate where no DelayPassed is required.
        when(dataTransServiceMock.getIndoorTemperature()).thenReturn(12.5);
        when(dataTransServiceMock.getOutdoorTemperature()).thenReturn(13.5);
        when(dataTransServiceMock.getLeavingWaterTemperature()).thenReturn(14.5);
        when(dataTransServiceMock.getIndoorHumidity()).thenReturn(25);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        when(dataTransServiceMock.getTimeStamp())
                .thenReturn(ZonedDateTime.parse("2024-03-31T09:03:09.879Z", formatter));

        when(dataTransServiceMock.getConsumptionCoolingDay()).thenReturn(new Float[] { 1.0f, 2.0f, 3.0f });
        when(dataTransServiceMock.getConsumptionCoolingWeek()).thenReturn(new Float[] { 1.0f, 2.0f, 3.0f });
        when(dataTransServiceMock.getConsumptionCoolingMonth()).thenReturn(new Float[] { 1.0f, 2.0f, 3.0f });

        when(dataTransServiceMock.getConsumptionHeatingDay()).thenReturn(new Float[] { 1.0f, 2.0f, 3.0f });
        when(dataTransServiceMock.getConsumptionHeatingWeek()).thenReturn(new Float[] { 1.0f, 2.0f, 3.0f });
        when(dataTransServiceMock.getConsumptionHeatingMonth()).thenReturn(new Float[] { 1.0f, 2.0f, 3.0f });

        handler.refreshDevice();

        // Energy consumption Cooling Day
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_DAY, 0)),
                new DecimalType(1.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_DAY, 1)),
                new DecimalType(2.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_DAY, 2)),
                new DecimalType(3.0));

        // Energy consumption Cooling Week
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_WEEK, 0)),
                new DecimalType(1.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_WEEK, 1)),
                new DecimalType(2.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_WEEK, 2)),
                new DecimalType(3.0));

        // Energy consumption Cooling Week
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_MONTH, 0)),
                new DecimalType(1.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_MONTH, 1)),
                new DecimalType(2.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_COOLING_MONTH, 2)),
                new DecimalType(3.0));

        // Energy consumption Heating Day
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_DAY, 0)),
                new DecimalType(1.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_DAY, 1)),
                new DecimalType(2.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_DAY, 2)),
                new DecimalType(3.0));

        // Energy consumption Heating Week
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_WEEK, 0)),
                new DecimalType(1.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_WEEK, 1)),
                new DecimalType(2.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_WEEK, 2)),
                new DecimalType(3.0));

        // Energy consumption Heating Week
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_MONTH, 0)),
                new DecimalType(1.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_MONTH, 1)),
                new DecimalType(2.0));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), String.format(CHANNEL_AC_ENERGY_HEATING_MONTH, 2)),
                new DecimalType(3.0));

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_HWT_POWER),
                OnOffType.from("ON"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_POWERFULMODE),
                OnOffType.from("ON"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_OPERATIONMODE),
                new StringType("HEAT"));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMP),
                new DecimalType(19.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPMIN),
                new DecimalType(10.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPMAX),
                new DecimalType(20.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPSTEP),
                new DecimalType(0.5));

        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP), new DecimalType(21.2));
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET), new DecimalType(15.5));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT),
                new StringType("VERTICAL_AND_HORIZONTAL"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANSPEED),
                new StringType("LEVEL_3"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_ECONOMODE),
                OnOffType.from("ON"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_STREAMER),
                OnOffType.from("ON"));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT_HOR),
                new StringType("SWING"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT_VER),
                new StringType("WINDNICE"));
        verify(callbackMock).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_HOLIDAYMODE),
                OnOffType.from("ON"));

        // Test updatestate where no DelayPassed is required.
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_INDOOR_TEMP),
                new DecimalType(12.5));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_OUTDOOR_TEMP),
                new DecimalType(13.5));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_LEAVINGWATER_TEMP),
                new DecimalType(14.5));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_INDOOR_HUMIDITY),
                new DecimalType(25));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TIMESTAMP),
                new DateTimeType("2024-03-31T09:03:09.879Z"));
    }

    @Test
    public void refreshDeviceUndefTest() {
        when(dataTransServiceMock.isAvailable()).thenReturn(true);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_POWER)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_POWERFULMODE)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_OPERATIONMODE)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMP)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPMIN)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPMAX)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPSTEP)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP)).thenReturn(true);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANSPEED)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_ECONOMODE)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_STREAMER)).thenReturn(true);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT_HOR)).thenReturn(true);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT_VER)).thenReturn(true);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_HOLIDAYMODE)).thenReturn(true);

        when(dataTransServiceMock.getPowerOnOff()).thenReturn(null);
        when(dataTransServiceMock.getPowerfulModeOnOff()).thenReturn(null);
        when(dataTransServiceMock.getCurrentOperationMode()).thenReturn(null);
        when(dataTransServiceMock.getCurrentTemperatureSet()).thenReturn(null);
        when(dataTransServiceMock.getCurrentTemperatureSetMin()).thenReturn(null);
        when(dataTransServiceMock.getCurrentTemperatureSetMax()).thenReturn(null);
        when(dataTransServiceMock.getCurrentTemperatureSetStep()).thenReturn(null);

        when(dataTransServiceMock.getSetpointLeavingWaterTemperature()).thenReturn(null);
        when(dataTransServiceMock.getSetpointLeavingWaterOffset()).thenReturn(null);

        when(dataTransServiceMock.getCurrentFanDirection()).thenReturn(null);
        when(dataTransServiceMock.getCurrentFanspeed()).thenReturn(null);
        when(dataTransServiceMock.getEconoMode()).thenReturn(null);
        when(dataTransServiceMock.getStreamerMode()).thenReturn(null);

        when(dataTransServiceMock.getCurrentFanDirectionHor()).thenReturn(null);
        when(dataTransServiceMock.getCurrentFanDirectionVer()).thenReturn(null);

        when(dataTransServiceMock.getHolidayMode()).thenReturn(null);

        // Test updatestate where no DelayPassed is required.
        when(dataTransServiceMock.getIndoorTemperature()).thenReturn(null);
        when(dataTransServiceMock.getOutdoorTemperature()).thenReturn(null);
        when(dataTransServiceMock.getLeavingWaterTemperature()).thenReturn(null);
        when(dataTransServiceMock.getIndoorHumidity()).thenReturn(null);

        when(dataTransServiceMock.getTimeStamp()).thenReturn(null);

        handler.refreshDevice();

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_HWT_POWER),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_POWERFULMODE),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_OPERATIONMODE),
                UnDefType.UNDEF);

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMP),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPMIN),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPMAX),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPSTEP),
                UnDefType.UNDEF);

        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP), UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET), UnDefType.UNDEF);

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANSPEED),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_ECONOMODE),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_STREAMER),
                UnDefType.UNDEF);

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT_HOR),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT_VER),
                UnDefType.UNDEF);
        verify(callbackMock).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_HOLIDAYMODE), UnDefType.UNDEF);

        // Test updatestate where no DelayPassed is required.
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_INDOOR_TEMP),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_OUTDOOR_TEMP),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_LEAVINGWATER_TEMP),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_INDOOR_HUMIDITY),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TIMESTAMP),
                UnDefType.UNDEF);

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
    }

    @Test
    public void refreshDeviceDelayNotPassedTest() {
        when(dataTransServiceMock.isAvailable()).thenReturn(true);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_POWER)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_POWERFULMODE)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_OPERATIONMODE)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMP)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPMIN)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPMAX)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_TEMPSTEP)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP)).thenReturn(false);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANSPEED)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_ECONOMODE)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_STREAMER)).thenReturn(false);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT_HOR)).thenReturn(false);
        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_FANMOVEMENT_VER)).thenReturn(false);

        when(channelsRefreshDelayMock.isDelayPassed(CHANNEL_AC_HOLIDAYMODE)).thenReturn(false);

        lenient().when(dataTransServiceMock.getPowerOnOff()).thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getPowerfulModeOnOff())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getCurrentOperationMode())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getCurrentTemperatureSet())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getCurrentTemperatureSetMin())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getCurrentTemperatureSetMax())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getCurrentTemperatureSetStep())
                .thenThrow(new RuntimeException("Simulating exception"));

        lenient().when(dataTransServiceMock.getSetpointLeavingWaterTemperature())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getSetpointLeavingWaterOffset())
                .thenThrow(new RuntimeException("Simulating exception"));

        lenient().when(dataTransServiceMock.getCurrentFanDirection())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getCurrentFanspeed())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getEconoMode()).thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getStreamerMode()).thenThrow(new RuntimeException("Simulating exception"));

        lenient().when(dataTransServiceMock.getCurrentFanDirectionHor())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getCurrentFanDirectionVer())
                .thenThrow(new RuntimeException("Simulating exception"));

        lenient().when(dataTransServiceMock.getHolidayMode()).thenThrow(new RuntimeException("Simulating exception"));

        handler.refreshDevice();

        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_HWT_POWER),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_POWERFULMODE),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_OPERATIONMODE),
                UnDefType.UNDEF);

        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMP),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPMIN),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPMAX),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_TEMPSTEP),
                UnDefType.UNDEF);

        verify(callbackMock, times(0)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP), UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(
                new ChannelUID(thingMock.getUID(), CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET), UnDefType.UNDEF);

        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANSPEED),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_ECONOMODE),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_STREAMER),
                UnDefType.UNDEF);

        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT_HOR),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_FANMOVEMENT_VER),
                UnDefType.UNDEF);
        verify(callbackMock, times(0)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_AC_HOLIDAYMODE),
                UnDefType.UNDEF);
    }

    @Test
    public void handleCommandTest() {
        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_POWER), OnOffType.ON);
        verify(dataTransServiceMock).setPowerOnOff(Enums.OnOff.ON);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_POWERFULMODE), OnOffType.ON);
        verify(dataTransServiceMock).setPowerfulModeOnOff(Enums.OnOff.ON);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_OPERATIONMODE),
                new StringType("DEHUMIDIFIER"));
        verify(dataTransServiceMock).setCurrentOperationMode(Enums.OperationMode.DEHUMIDIFIER);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_TEMP), new QuantityType<>("25"));
        verify(dataTransServiceMock).setCurrentTemperatureSet(25);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_FANSPEED), new StringType("LEVEL_3"));
        verify(dataTransServiceMock).setFanSpeed(Enums.FanSpeed.LEVEL_3);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_FANMOVEMENT),
                new StringType("VERTICAL_AND_HORIZONTAL"));
        verify(dataTransServiceMock).setCurrentFanDirection(Enums.FanMovement.VERTICAL_AND_HORIZONTAL);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_FANMOVEMENT_HOR),
                new StringType("SWING"));
        verify(dataTransServiceMock).setCurrentFanDirectionHor(Enums.FanMovementHor.SWING);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_FANMOVEMENT_VER),
                new StringType("WINDNICE"));
        verify(dataTransServiceMock).setCurrentFanDirectionVer(Enums.FanMovementVer.WINDNICE);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_ECONOMODE), OnOffType.ON);
        verify(dataTransServiceMock).setEconoMode(Enums.OnOff.ON);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_STREAMER), OnOffType.ON);
        verify(dataTransServiceMock).setStreamerMode(Enums.OnOff.ON);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_HOLIDAYMODE), OnOffType.ON);
        verify(dataTransServiceMock).setHolidayMode(Enums.OnOff.ON);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET),
                new QuantityType<>("27"));
        verify(dataTransServiceMock).setSetpointLeavingWaterOffset(27);

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP),
                new QuantityType<>("28"));
        verify(dataTransServiceMock).setSetpointLeavingWaterTemperature(28);

        verify(callbackMock, times(13)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        doThrow(new RuntimeException("Simulating exception")).when(channelsRefreshDelayMock).add(anyString());

        handler.handleCommand(new ChannelUID(new ThingUID("1:2:3"), CHANNEL_AC_POWER), OnOffType.ON);

        verify(callbackMock, times(1)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.OFFLINE)));
    }
}
