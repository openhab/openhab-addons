/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.robonect.internal.RobonectBindingConstants;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.MowerStatus;
import org.openhab.binding.robonect.internal.model.NextTimer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.Timer;
import org.openhab.binding.robonect.internal.model.Wlan;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The goal of this class is to test RobonectHandler in isolation.
 *
 * @author Marco Meyer - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RobonectHandlerTest {

    private RobonectHandler subject;

    private @Mock Thing robonectThingMock;
    private @Mock RobonectClient robonectClientMock;
    private @Mock ThingHandlerCallback callbackMock;
    private @Mock HttpClientFactory httpClientFactoryMock;
    private @Mock TimeZoneProvider timezoneProvider;

    @BeforeEach
    public void setUp() {
        Mockito.when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));
        Mockito.when(timezoneProvider.getTimeZone()).thenReturn(ZoneId.of("Europe/Berlin"));

        subject = new RobonectHandler(robonectThingMock, httpClientFactoryMock, timezoneProvider);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
    }

    @Test
    public void shouldUpdateNextTimerChannelWithDateTimeState() throws InterruptedException {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        Timer timer = new Timer();
        timer.setStatus(Timer.TimerMode.ACTIVE);
        NextTimer nextTimer = new NextTimer();
        nextTimer.setDate("01.05.2017");
        nextTimer.setTime("19:00:00");
        nextTimer.setUnix("1493665200");
        timer.setNext(nextTimer);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_TIMER_NEXT_TIMER),
                RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_TIMER_NEXT_TIMER)),
                stateCaptor.capture());

        State value = stateCaptor.getValue();
        assertTrue(value instanceof DateTimeType);

        ZonedDateTime zdt = ((DateTimeType) value).getZonedDateTime();
        assertEquals(1, zdt.getDayOfMonth());
        assertEquals(2017, zdt.getYear());
        assertEquals(Month.MAY, zdt.getMonth());
        assertEquals(19, zdt.getHour());
        assertEquals(0, zdt.getMinute());
        assertEquals(0, zdt.getSecond());
    }

    @Test
    public void shouldUpdateErrorChannelsIfErrorStatusReturned() throws InterruptedException {
        ArgumentCaptor<State> errorCodeCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> errorMessageCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> errorDateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        ErrorEntry error = new ErrorEntry();
        error.setDate("01.05.2017");
        error.setTime("19:00:00");
        error.setUnix("1493665200");
        error.setErrorCode(Integer.valueOf(22));
        error.setErrorMessage("Dummy Message");
        mowerInfo.getStatus().setStatus(MowerStatus.ERROR_STATUS);
        mowerInfo.setError(error);
        ErrorList errorList = new ErrorList();
        errorList.setSuccessful(true);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectClientMock.errorList()).thenReturn(errorList);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_CODE)),
                errorCodeCaptor.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_MESSAGE)),
                errorMessageCaptor.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_DATE)),
                errorDateCaptor.capture());

        State errorDate = errorDateCaptor.getValue();
        assertTrue(errorDate instanceof DateTimeType);

        ZonedDateTime zdt = ((DateTimeType) errorDate).getZonedDateTime();
        assertEquals(1, zdt.getDayOfMonth());
        assertEquals(2017, zdt.getYear());
        assertEquals(Month.MAY, zdt.getMonth());
        assertEquals(19, zdt.getHour());
        assertEquals(0, zdt.getMinute());
        assertEquals(0, zdt.getSecond());

        State errorMessage = errorMessageCaptor.getValue();
        assertTrue(errorMessage instanceof StringType);
        StringType msgStringType = (StringType) errorMessage;
        assertEquals("Dummy Message", msgStringType.toFullString());

        State errorCode = errorCodeCaptor.getValue();
        assertTrue(errorCode instanceof DecimalType);
        DecimalType codeDecimaltype = (DecimalType) errorCode;
        assertEquals(22, codeDecimaltype.intValue());
    }

    @Test
    public void shouldResetErrorStateIfNoErrorInStatusUpdate() throws InterruptedException {
        ArgumentCaptor<State> errorCodeCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> errorMessageCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> errorDateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        mowerInfo.getStatus().setStatus(MowerStatus.MOWING);
        mowerInfo.setError(null);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_CODE)),
                errorCodeCaptor.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_MESSAGE)),
                errorMessageCaptor.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_DATE)),
                errorDateCaptor.capture());

        assertEquals(errorCodeCaptor.getValue(), UnDefType.UNDEF);
        assertEquals(errorMessageCaptor.getValue(), UnDefType.UNDEF);
        assertEquals(errorDateCaptor.getValue(), UnDefType.UNDEF);
    }

    @Test
    public void shouldUpdateNumericStateOnMowerStatusRefresh() throws InterruptedException {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        mowerInfo.getStatus().setStatus(MowerStatus.MOWING);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS)),
                stateCaptor.capture());

        State value = stateCaptor.getValue();
        assertTrue(value instanceof DecimalType);
        DecimalType status = (DecimalType) value;

        assertEquals(MowerStatus.MOWING.getStatusCode(), status.intValue());
    }

    @Test
    public void shouldUpdateAllChannels() {
        ArgumentCaptor<State> stateCaptorName = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorBattery = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorStatus = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorDuration = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorHours = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorMode = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorStarted = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorWlan = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        ErrorList errorList = new ErrorList();
        errorList.setSuccessful(true);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectClientMock.errorList()).thenReturn(errorList);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_MOWER_NAME)),
                stateCaptorName.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_BATTERY)),
                stateCaptorBattery.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS)),
                stateCaptorStatus.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_DURATION)),
                stateCaptorDuration.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_HOURS)),
                stateCaptorHours.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_MODE)),
                stateCaptorMode.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_MOWER_START)),
                stateCaptorStarted.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_WLAN_SIGNAL)),
                stateCaptorWlan.capture());

        assertEquals("Mowy", stateCaptorName.getValue().toFullString());
        assertEquals(99, ((DecimalType) stateCaptorBattery.getValue()).intValue());
        assertEquals(4, ((DecimalType) stateCaptorStatus.getValue()).intValue());
        assertEquals(55, ((QuantityType<?>) stateCaptorDuration.getValue()).intValue());
        assertEquals(22, ((QuantityType<?>) stateCaptorHours.getValue()).intValue());
        assertEquals(MowerMode.AUTO.name(), stateCaptorMode.getValue().toFullString());
        assertEquals(OnOffType.ON, stateCaptorStarted.getValue());
        assertEquals(-88, ((DecimalType) stateCaptorWlan.getValue()).intValue());
    }

    private MowerInfo createSuccessfulMowerInfoResponse() {
        MowerInfo mowerInfo = new MowerInfo();
        Timer timer = new Timer();
        timer.setStatus(Timer.TimerMode.ACTIVE);
        NextTimer nextTimer = new NextTimer();
        nextTimer.setDate("01.05.2017");
        nextTimer.setTime("19:00:00");
        nextTimer.setUnix("1493665200");
        timer.setNext(nextTimer);
        mowerInfo.setTimer(timer);
        Status status = new Status();
        status.setBattery(99);
        status.setDuration(55);
        status.setHours(22);
        status.setMode(MowerMode.AUTO);
        status.setStatus(MowerStatus.CHARGING);
        mowerInfo.setStatus(status);
        mowerInfo.setName("Mowy");
        Wlan wlan = new Wlan();
        wlan.setSignal(-88);
        mowerInfo.setWlan(wlan);
        mowerInfo.setSuccessful(true);
        mowerInfo.getStatus().setStopped(false);
        return mowerInfo;
    }
}
