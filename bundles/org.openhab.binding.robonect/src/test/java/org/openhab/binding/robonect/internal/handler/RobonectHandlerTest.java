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
import org.openhab.binding.robonect.internal.model.AjaxInfo;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.MowerStatus;
import org.openhab.binding.robonect.internal.model.NextTimer;
import org.openhab.binding.robonect.internal.model.PushInfo;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.Timer;
import org.openhab.binding.robonect.internal.model.WireInfo;
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
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The goal of this class is to test RobonectHandler in isolation.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Dynamic channel and duration tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RobonectHandlerTest {

    private static final ZoneId TIME_ZONE = ZoneId.of("Europe/Berlin");

    private RobonectHandler subject;

    private @Mock Thing robonectThingMock;
    private @Mock RobonectClient robonectClientMock;
    private @Mock ThingHandlerCallback callbackMock;
    private @Mock HttpClientFactory httpClientFactoryMock;
    private @Mock TimeZoneProvider timeZoneProvider;

    @BeforeEach
    public void setUp() {
        Mockito.when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));
        Mockito.when(timeZoneProvider.getTimeZone()).thenReturn(TIME_ZONE);

        subject = new RobonectHandler(robonectThingMock, httpClientFactoryMock, timeZoneProvider);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
    }

    @Test
    public void shouldCreateChannelsForEveryWireSensorAndBattery() {
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3")).build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        ModelParser parser = new ModelParser();
        when(robonectClientMock.getWireInfo()).thenReturn(parser.parse("""
                {"successful":true,"sensors":[{"description":"Left","quality":70},
                {"description":"Right","quality":80},{"description":"Rear","quality":90},
                {"description":"Extra","quality":60},{"description":"Fifth","quality":50}]}
                """, WireInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "wire-sensor-1-label"), RefreshType.REFRESH);
        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(callbackMock).thingUpdated(updated.capture());
        assertNotNull(updated.getValue().getChannel("wire-sensor-5-label"));

        when(robonectClientMock.getBatteryInfo()).thenReturn(parser.parse("""
                {"successful":true,"batteries":[{"id":0,"charge":70},{"id":1,"charge":80}]}
                """, BatteryInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "battery-charge"), RefreshType.REFRESH);
        verify(callbackMock, atLeastOnce()).thingUpdated(updated.capture());
        assertNotNull(updated.getValue().getChannel("battery-2-charge"));
        assertNotNull(updated.getValue().getChannel("wire-sensor-5-label"));
        verify(callbackMock).stateUpdated(new ChannelUID(thing.getUID(), "battery-charge"),
                new QuantityType<>(70, org.openhab.core.library.unit.Units.PERCENT));

        when(robonectClientMock.getWireInfo()).thenReturn(parser.parse("""
                {"successful":true,"sensors":[{"description":"Left","quality":70}]}
                """, WireInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "wire-sensor-1-label"), RefreshType.REFRESH);
        verify(callbackMock, atLeastOnce()).thingUpdated(updated.capture());
        assertNull(updated.getValue().getChannel("wire-sensor-5-label"));
        assertNotNull(updated.getValue().getChannel("battery-2-charge"));
    }

    @Test
    public void shouldPreserveOtherPushTriggerSettingWhenSendingCommand() {
        PushInfo push = new ModelParser().parse("""
                {"successful":true,"push":{"trigger":{"trigger12":
                {"name":"Mowing","enter":false,"leave":true}}}}
                """, PushInfo.class);
        when(robonectClientMock.getPushInfo()).thenReturn(push);
        when(robonectClientMock.updatePush("trigger12", "3"))
                .thenReturn(new ModelParser().parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "push-trigger-12-enter"), OnOffType.ON);
        verify(robonectClientMock).updatePush("trigger12", "3");
    }

    @Test
    public void shouldCreatePushTriggerChannelsBeyondTen() {
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3")).build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        when(robonectClientMock.getPushInfo()).thenReturn(new ModelParser().parse("""
                {"successful":true,"push":{"trigger":{"interval":30,
                "trigger0":{"name":"Parked","enter":true,"leave":false},
                "trigger12":{"name":"Mowing","enter":false,"leave":true}}}}
                """, PushInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "push-trigger-0-name"), RefreshType.REFRESH);
        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(callbackMock).thingUpdated(updated.capture());
        assertNotNull(updated.getValue().getChannel("push-trigger-12-leave"));
    }

    @Test
    public void shouldPublishRecentDurationsAndStatistics() {
        HourInfo hours = new ModelParser().parse("""
                {"successful":true,"seek":[{"duration":2},{"duration":4},{"duration":6}],
                "mowing":[{"duration":10},{"duration":20}]}
                """, HourInfo.class);
        when(robonectClientMock.getHourInfo()).thenReturn(hours);
        ThingUID uid = new ThingUID("1:2:3");
        subject.handleCommand(new ChannelUID(uid, "statistics-seek"), RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "statistics-seek"), new StringType("2, 4, 6"));
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "statistics-mowing"), new StringType("10, 20"));
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "statistics-seek-average"),
                new QuantityType<>(4, org.openhab.core.library.unit.Units.MINUTE));
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "statistics-seek-min"),
                new QuantityType<>(2, org.openhab.core.library.unit.Units.MINUTE));
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "statistics-seek-max"),
                new QuantityType<>(6, org.openhab.core.library.unit.Units.MINUTE));
    }

    @Test
    public void shouldPublishHomeAndPreventionCode() {
        MowerInfo info = new ModelParser().parse("""
                {"successful":true,"name":"Mower","status":{"status":2,"mode":0,
                "stopped":false,"battery":50,"hours":1,"duration":2,"distance":0,
                "home":true,"prevent":3}}
                """, MowerInfo.class);
        when(robonectClientMock.getMowerInfo()).thenReturn(info);
        ThingUID uid = new ThingUID("1:2:3");
        subject.handleCommand(new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS_HOME), RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS_HOME),
                OnOffType.ON);
        verify(callbackMock).stateUpdated(new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS_PREVENT),
                new DecimalType(3));
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

        ZonedDateTime zdt = ((DateTimeType) value).getZonedDateTime(TIME_ZONE);
        assertEquals(1, zdt.getDayOfMonth());
        assertEquals(2017, zdt.getYear());
        assertEquals(Month.MAY, zdt.getMonth());
        assertEquals(19, zdt.getHour());
        assertEquals(0, zdt.getMinute());
        assertEquals(0, zdt.getSecond());
    }

    @Test
    public void shouldIgnoreIncompleteMowerStatus() {
        MowerInfo incompleteInfo = new MowerInfo();
        incompleteInfo.setSuccessful(true);
        when(robonectClientMock.getMowerInfo()).thenReturn(incompleteInfo);

        assertDoesNotThrow(() -> subject.handleCommand(
                new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS), RefreshType.REFRESH));
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

        ZonedDateTime zdt = ((DateTimeType) errorDate).getZonedDateTime(TIME_ZONE);
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
    public void shouldUseAjaxUiInfoForJobTimes() {
        ArgumentCaptor<State> jobStartCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> jobEndCaptor = ArgumentCaptor.forClass(State.class);
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        AjaxInfo ajaxInfo = new AjaxInfo();
        ajaxInfo.setTimerMenuText("Job");
        ajaxInfo.setControlMenuText("10:00 - 12:00");

        subject.setAjaxUiScraping(true);
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectClientMock.getAjaxInfo()).thenReturn(ajaxInfo);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                RefreshType.REFRESH);

        verify(callbackMock).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_JOB)), eq(OnOffType.ON));
        verify(callbackMock).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_JOB_START)),
                jobStartCaptor.capture());
        verify(callbackMock).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_JOB_END)),
                jobEndCaptor.capture());
        ZonedDateTime jobStart = ((DateTimeType) jobStartCaptor.getValue()).getZonedDateTime(ZoneId.of("UTC"));
        assertEquals(1970, jobStart.getYear());
        assertEquals(1, jobStart.getDayOfYear());
        assertEquals(10, jobStart.getHour());
        assertEquals(0, jobStart.getMinute());
        ZonedDateTime jobEnd = ((DateTimeType) jobEndCaptor.getValue()).getZonedDateTime(ZoneId.of("UTC"));
        assertEquals(1970, jobEnd.getYear());
        assertEquals(1, jobEnd.getDayOfYear());
        assertEquals(12, jobEnd.getHour());
        assertEquals(0, jobEnd.getMinute());
        verify(callbackMock).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_MODE)),
                eq(new StringType("AUTO")));
    }

    @Test
    public void shouldUseAjaxUiInfoForEndOfDay() {
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        mowerInfo.getStatus().setMode(MowerMode.HOME);
        AjaxInfo ajaxInfo = new AjaxInfo();
        ajaxInfo.setControlMenuText("End of day");

        subject.setAjaxUiScraping(true);
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectClientMock.getAjaxInfo()).thenReturn(ajaxInfo);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                RefreshType.REFRESH);

        verify(callbackMock).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_MODE)),
                eq(new StringType("EOD")));
    }

    @Test
    public void shouldResetErrorsOnOnCommand() {
        RobonectAnswer response = new RobonectAnswer();
        response.setSuccessful(true);

        when(robonectClientMock.resetErrors()).thenReturn(response);
        when(robonectClientMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_RESET_ERROR),
                OnOffType.ON);

        verify(robonectClientMock).resetErrors();
        verify(robonectClientMock).getMowerInfo();
    }

    @Test
    public void shouldSendStartCommandWithoutReadingTheCurrentState() {
        RobonectAnswer response = new RobonectAnswer();
        response.setSuccessful(true);
        when(robonectClientMock.start()).thenReturn(response);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_MOWER_START),
                OnOffType.ON);

        verify(robonectClientMock).start();
        verify(robonectClientMock, never()).getMowerInfo();
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
        ArgumentCaptor<State> stateCaptorWlanStrength = ArgumentCaptor.forClass(State.class);

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
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_WLAN_SIGNAL_STRENGTH)),
                stateCaptorWlanStrength.capture());

        assertEquals("Mowy", stateCaptorName.getValue().toFullString());
        assertEquals(99, ((DecimalType) stateCaptorBattery.getValue()).intValue());
        assertEquals(4, ((DecimalType) stateCaptorStatus.getValue()).intValue());
        assertEquals(55, ((QuantityType<?>) stateCaptorDuration.getValue()).intValue());
        assertEquals(22, ((QuantityType<?>) stateCaptorHours.getValue()).intValue());
        assertEquals(MowerMode.AUTO.name(), stateCaptorMode.getValue().toFullString());
        assertEquals(OnOffType.ON, stateCaptorStarted.getValue());
        assertEquals(1, ((DecimalType) stateCaptorWlan.getValue()).intValue());
        assertEquals(-88, ((DecimalType) stateCaptorWlanStrength.getValue()).intValue());
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
