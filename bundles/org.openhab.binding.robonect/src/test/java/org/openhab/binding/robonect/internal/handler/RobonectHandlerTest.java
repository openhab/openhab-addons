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

import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.MowerStatus;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.NextTimer;
import org.openhab.binding.robonect.internal.model.PushInfo;
import org.openhab.binding.robonect.internal.model.RemoteInfo;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.Timer;
import org.openhab.binding.robonect.internal.model.TimerInfo;
import org.openhab.binding.robonect.internal.model.TimersInfo;
import org.openhab.binding.robonect.internal.model.WeatherInfo;
import org.openhab.binding.robonect.internal.model.WireInfo;
import org.openhab.binding.robonect.internal.model.Wlan;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The goal of this class is to test RobonectHandler in isolation.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Dynamic channel and duration tests
 * @author Nico Brüttner - Command response and delayed refresh tests
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
    private @Mock ScheduledExecutorService commandScheduler;
    private @Mock ScheduledFuture<?> commandRefreshJob;
    private @Mock org.eclipse.jetty.client.HttpClient httpClient;

    @BeforeEach
    public void setUp() {
        Mockito.when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));
        Mockito.when(timeZoneProvider.getTimeZone()).thenReturn(TIME_ZONE);
        when(httpClientFactoryMock.createHttpClient(anyString())).thenReturn(httpClient);
        doReturn(commandRefreshJob).when(commandScheduler).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));

        subject = new RobonectHandler(robonectThingMock, httpClientFactoryMock, timeZoneProvider, null,
                commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
    }

    @AfterEach
    public void tearDown() {
        subject.dispose();
    }

    @Test
    public void shouldCreateChannelsForEveryWireSensorAndBattery() {
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3")).build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
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
                {"name":"Way home","enter":false,"leave":true}}}}
                """, PushInfo.class);
        when(robonectClientMock.getPushInfo()).thenReturn(push);
        when(robonectClientMock.updatePush("trigger12", "3"))
                .thenReturn(new ModelParser().parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "push-trigger-12-way_home-enter"), OnOffType.ON);
        verify(robonectClientMock).updatePush("trigger12", "3");
    }

    @Test
    public void shouldPreserveEnterSettingWhenSendingLeaveCommand() {
        when(robonectClientMock.getPushInfo()).thenReturn(new ModelParser().parse("""
                {"successful":true,"push":{"trigger":{"trigger12":
                {"name":"Way home","enter":true,"leave":false}}}}
                """, PushInfo.class));
        when(robonectClientMock.updatePush("trigger12", "3"))
                .thenReturn(new ModelParser().parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "push-trigger-12-way_home-leave"), OnOffType.ON);
        verify(robonectClientMock).updatePush("trigger12", "3");
    }

    @Test
    public void shouldUpdateAndTruncateRemoteStartName() {
        RemoteInfo response = new RemoteInfo();
        response.setSuccessful(true);
        when(robonectClientMock.updateRemoteStart(eq(2), eq("1234567890123456"), isNull(Integer.class),
                isNull(Integer.class))).thenReturn(response);
        when(robonectClientMock.getRemoteInfo()).thenReturn(response);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "remote-2-name"),
                new StringType("12345678901234567890"));

        verify(robonectClientMock).updateRemoteStart(eq(2), eq("1234567890123456"), isNull(Integer.class),
                isNull(Integer.class));
    }

    @Test
    public void shouldIgnoreOutOfRangeRemoteStartDistance() {
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "remote-2-distance"),
                new QuantityType<>(301, org.openhab.core.library.unit.SIUnits.METRE));

        verify(robonectClientMock, never()).updateRemoteStart(anyInt(), any(), any(), any());
    }

    @Test
    public void shouldCreatePushTriggerChannelsBeyondTen() {
        ThingUID uid = new ThingUID("1:2:3");
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid)
                .withChannels(ChannelBuilder.create(new ChannelUID(uid, "push-trigger-0-name"), "String").build(),
                        ChannelBuilder.create(new ChannelUID(uid, "push-trigger-12-enter"), "Switch").build())
                .build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        when(robonectClientMock.getPushInfo()).thenReturn(new ModelParser().parse("""
                {"successful":true,"push":{"trigger":{"interval":30,
                "trigger0":{"name":"Parked","enter":true,"leave":false},
                "trigger12":{"name":"WAY Home","enter":false,"leave":true}}}}
                """, PushInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "push-interval"), RefreshType.REFRESH);
        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(callbackMock).thingUpdated(updated.capture());
        assertNotNull(updated.getValue().getChannel("push-trigger-0-parked-enter"));
        assertNotNull(updated.getValue().getChannel("push-trigger-12-way_home-leave"));
        assertEquals(4, updated.getValue().getChannels().size());
        assertNull(updated.getValue().getChannel("push-trigger-0-name"));
        assertNull(updated.getValue().getChannel("push-trigger-12-enter"));
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "push-trigger-12-way_home-leave"), OnOffType.ON);

        when(robonectClientMock.getPushInfo())
                .thenReturn(new ModelParser().parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-interval"), RefreshType.REFRESH);
        verify(callbackMock).thingUpdated(any());

        when(robonectClientMock.getPushInfo()).thenReturn(new ModelParser().parse("""
                {"successful":true,"push":{"trigger":{
                "trigger12":{"name":"Mowing","enter":true,"leave":false}}}}
                """, PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-interval"), RefreshType.REFRESH);
        verify(callbackMock, times(2)).thingUpdated(updated.capture());
        assertNull(updated.getValue().getChannel("push-trigger-12-way_home-leave"));
        assertNotNull(updated.getValue().getChannel("push-trigger-12-mowing-enter"));
        when(robonectClientMock.updatePush("trigger12", "1"))
                .thenReturn(new ModelParser().parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-trigger-12-way_home-enter"), OnOffType.ON);
        verify(robonectClientMock).updatePush("trigger12", "1");
    }

    @Test
    public void shouldKeepDuplicatePushTriggerNamesDistinctAndIgnoreInvalidNames() {
        ThingUID uid = new ThingUID("1:2:3");
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        when(robonectClientMock.getPushInfo()).thenReturn(new ModelParser().parse("""
                {"successful":true,"push":{"trigger":{
                "trigger0":{"name":"Parked","enter":true,"leave":false},
                "trigger1":{"name":"Way home","enter":true,"leave":false},
                "trigger2":{"name":"WAY_HOME","enter":false,"leave":true},
                "trigger3":{"enter":false,"leave":true},
                "trigger4":{"name":" ","enter":false,"leave":true},
                "trigger5":{"name":"Invalid:name","enter":false,"leave":true}}}}
                """, PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-interval"), RefreshType.REFRESH);
        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(callbackMock).thingUpdated(updated.capture());
        assertEquals(6, updated.getValue().getChannels().size());
        assertNotNull(updated.getValue().getChannel("push-trigger-0-parked-enter"));
        assertNotNull(updated.getValue().getChannel("push-trigger-1-way_home-enter"));
        assertNotNull(updated.getValue().getChannel("push-trigger-2-way_home-enter"));
        when(robonectClientMock.updatePush("trigger2", "3"))
                .thenReturn(new ModelParser().parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-trigger-2-way_home-enter"), OnOffType.ON);
        verify(robonectClientMock).updatePush("trigger2", "3");
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
        ZonedDateTime jobStart = ((DateTimeType) jobStartCaptor.getValue()).getZonedDateTime(TIME_ZONE);
        assertEquals(1970, jobStart.getYear());
        assertEquals(1, jobStart.getDayOfYear());
        assertEquals(10, jobStart.getHour());
        assertEquals(0, jobStart.getMinute());
        ZonedDateTime jobEnd = ((DateTimeType) jobEndCaptor.getValue()).getZonedDateTime(TIME_ZONE);
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
        RobonectAnswer response = new ModelParser().parse("{\"successful\":true}", RobonectAnswer.class);

        when(robonectClientMock.resetErrors()).thenReturn(response);
        when(robonectClientMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_RESET_ERROR),
                OnOffType.ON);

        verify(robonectClientMock).resetErrors();
        verify(callbackMock, never()).stateUpdated(any(), any());
        verify(robonectClientMock, never()).getMowerInfo();
        runScheduledRefresh();
        verify(robonectClientMock).getMowerInfo();
    }

    @Test
    public void shouldReadStatusImmediatelyAfterStartCommand() {
        RobonectAnswer response = new RobonectAnswer();
        response.setSuccessful(true);
        when(robonectClientMock.start()).thenReturn(response);
        when(robonectClientMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_MOWER_START),
                OnOffType.ON);

        verify(robonectClientMock).start();
        var order = inOrder(robonectClientMock);
        order.verify(robonectClientMock).start();
        order.verify(robonectClientMock).getMowerInfo();
        verifyNoInteractions(commandScheduler);
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

    @Test
    public void shouldApplyPartialWeatherResponseBeforeDelayedRead() {
        WeatherInfo result = new ModelParser().parse("""
                {"successful":true,"service":{"enable":true}}
                """, WeatherInfo.class);
        when(robonectClientMock.updateWeather("service", "1")).thenReturn(result);
        when(robonectClientMock.getWeatherInfo()).thenReturn(result);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-enabled"), OnOffType.ON);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-enabled"), OnOffType.ON);
        verify(robonectClientMock, never()).getWeatherInfo();
        runScheduledRefresh();
        verify(robonectClientMock).getWeatherInfo();
    }

    @Test
    public void shouldApplyRemoteResponseBeforeDelayedRead() {
        RemoteInfo result = new ModelParser().parse("""
                {"successful":true,"remotestart_2":{"name":"Garden"}}
                """, RemoteInfo.class);
        when(robonectClientMock.updateRemoteStart(eq(2), eq("Garden"), isNull(), isNull())).thenReturn(result);
        when(robonectClientMock.getRemoteInfo()).thenReturn(result);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "remote-2-name"), new StringType("Garden"));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "remote-2-name"),
                new StringType("Garden"));
        verify(robonectClientMock, never()).getRemoteInfo();
        runScheduledRefresh();
        verify(robonectClientMock).getRemoteInfo();
    }

    @Test
    public void shouldApplyTimerResponseAndStillScheduleRead() {
        TimerInfo result = new ModelParser().parse("""
                {"successful":true,"timer":{"id":1,"enabled":true}}
                """, TimerInfo.class);
        when(robonectClientMock.updateTimer(1, "enable", "1")).thenReturn(result);
        when(robonectClientMock.getTimersInfo()).thenReturn(new ModelParser().parse("""
                {"successful":false}
                """, TimersInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "timer-1-enabled"), OnOffType.ON);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "timer-1-enabled"), OnOffType.ON);
        verify(robonectClientMock, never()).getTimersInfo();
        runScheduledRefresh();
        verify(robonectClientMock).getTimersInfo();
    }

    @Test
    public void shouldScheduleReadWhenSuccessfulResponseHasNoState() {
        when(robonectClientMock.updateWeather("service", "1"))
                .thenReturn(new ModelParser().parse("{\"successful\":true}", WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-enabled"), OnOffType.ON);
        verify(callbackMock, never()).stateUpdated(any(), any());
        verify(robonectClientMock, never()).getWeatherInfo();
        verify(commandScheduler).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void shouldApplyDoorResponseBeforeDelayedRead() {
        DoorInfo result = new ModelParser().parse("""
                {"successful":true,"door":{"enabled":true,"state":{"open":true,"close":false}}}
                """, DoorInfo.class);
        when(robonectClientMock.setDoorDirection(Direction.OPEN)).thenReturn(result);
        when(robonectClientMock.getDoorInfo()).thenReturn(result);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "door-state"), UpDownType.UP);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "door-state"),
                org.openhab.core.library.types.PercentType.ZERO);
        verify(robonectClientMock, never()).getDoorInfo();
        runScheduledRefresh();
        verify(robonectClientMock).getDoorInfo();
    }

    @Test
    public void shouldApplyNameResponseBeforeDelayedStatusRead() {
        when(robonectClientMock.setName("Garden mower"))
                .thenReturn(new ModelParser().parse("{\"successful\":true,\"name\":\"Garden mower\"}", Name.class));
        when(robonectClientMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "name"), new StringType("Garden mower"));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "name"),
                new StringType("Garden mower"));
        verify(robonectClientMock, never()).getMowerInfo();
        runScheduledRefresh();
        verify(robonectClientMock).getMowerInfo();
    }

    @Test
    public void shouldApplyPartialPushWriteWithoutRemovingOtherTriggers() {
        ThingUID uid = new ThingUID("1:2:3");
        subject = new RobonectHandler(ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build(),
                httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        PushInfo full = new ModelParser().parse("""
                {"successful":true,"push":{"trigger":{
                "trigger0":{"name":"Parked","enter":false,"leave":false},
                "trigger1":{"name":"Mowing","enter":false,"leave":true}}}}
                """, PushInfo.class);
        when(robonectClientMock.getPushInfo()).thenReturn(full);
        subject.handleCommand(new ChannelUID(uid, "push-interval"), RefreshType.REFRESH);
        clearInvocations(robonectClientMock, callbackMock);
        when(robonectClientMock.updatePush("trigger1", "3")).thenReturn(new ModelParser().parse("""
                {"successful":true,"push":{"trigger":{
                "trigger1":{"name":"Mowing","enter":true}}}}
                """, PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-trigger-1-mowing-enter"), OnOffType.ON);
        verify(robonectClientMock).getPushInfo();
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "push-trigger-1-mowing-enter"), OnOffType.ON);
        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(uid, "push-trigger-1-mowing-leave")), any());
        assertNotNull(subject.getThing().getChannel("push-trigger-0-parked-enter"));
        runScheduledRefresh();
        verify(robonectClientMock, times(2)).getPushInfo();
    }

    @Test
    public void shouldReadStatusImmediatelyAfterModeAndStopWrites() {
        when(robonectClientMock.setMode(ModeCommand.Mode.HOME)).thenReturn(null);
        when(robonectClientMock.stop()).thenReturn(null);
        when(robonectClientMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "mode"), new StringType("HOME"));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "start"), OnOffType.OFF);
        var order = inOrder(robonectClientMock);
        order.verify(robonectClientMock).setMode(ModeCommand.Mode.HOME);
        order.verify(robonectClientMock).getMowerInfo();
        order.verify(robonectClientMock).stop();
        order.verify(robonectClientMock).getMowerInfo();
        verifyNoInteractions(commandScheduler);
    }

    @Test
    public void shouldIgnoreSupersededReadsAndCancelReadsOnDispose() {
        when(robonectClientMock.updateWeather("service", "1"))
                .thenReturn(new ModelParser().parse("{\"successful\":true}", WeatherInfo.class));
        ChannelUID channel = new ChannelUID(new ThingUID("1:2:3"), "weather-enabled");
        subject.handleCommand(channel, OnOffType.ON);
        subject.handleCommand(channel, OnOffType.ON);
        var refreshes = ArgumentCaptor.forClass(Runnable.class);
        verify(commandScheduler, times(2)).schedule(refreshes.capture(), eq(5L), eq(TimeUnit.SECONDS));
        verify(commandRefreshJob).cancel(true);
        refreshes.getAllValues().getFirst().run();
        verify(robonectClientMock, never()).getWeatherInfo();
        subject.dispose();
        refreshes.getAllValues().getLast().run();
        verify(robonectClientMock, never()).getWeatherInfo();
        verify(commandRefreshJob, times(2)).cancel(true);
    }

    @Test
    public void shouldNotPublishReadResponseIfDisposedDuringRequest() {
        WeatherInfo result = new ModelParser().parse("""
                {"successful":true,"service":{"enable":true}}
                """, WeatherInfo.class);
        when(robonectClientMock.updateWeather("service", "1")).thenReturn(result);
        when(robonectClientMock.getWeatherInfo()).thenAnswer(invocation -> {
            subject.dispose();
            return result;
        });
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-enabled"), OnOffType.ON);
        clearInvocations(callbackMock);
        runScheduledRefresh();
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    @Test
    public void shouldNotScheduleReadAfterRejectedWrite() {
        when(robonectClientMock.updateWeather("service", "1"))
                .thenReturn(new ModelParser().parse("{\"successful\":false}", WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-enabled"), OnOffType.ON);
        verifyNoInteractions(commandScheduler);
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    @Test
    public void shouldCreateAndRemovePushModeChannelWithUnofficialApi() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        var channel = subject.getThing().getChannel("push-mode");
        assertNotNull(channel);
        assertEquals("Number", channel.getAcceptedItemType());
        subject.configureUnofficialChannels(true);
        assertEquals(1, subject.getThing().getChannels().size());
        subject.configureUnofficialChannels(false);
        assertNull(subject.getThing().getChannel("push-mode"));
        verifyNoInteractions(robonectClientMock);
    }

    @Test
    public void shouldRefreshAndCommandPushModeOnlyWhenUnofficialApiEnabled() {
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        subject.handleCommand(uid, RefreshType.REFRESH);
        subject.handleCommand(uid, new DecimalType(2));
        verifyNoInteractions(robonectClientMock);
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        when(robonectClientMock.getPushModeViaWebInterface()).thenReturn(1);
        subject.handleCommand(uid, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(uid, new DecimalType(1));
        when(robonectClientMock.updatePushModeViaWebInterface(2)).thenReturn(2);
        subject.handleCommand(uid, new DecimalType(2));
        verify(callbackMock).stateUpdated(uid, new DecimalType(2));
        verify(robonectClientMock).getPushModeViaWebInterface();
        verifyNoInteractions(commandScheduler);
        verify(robonectClientMock, never()).getPushInfo();
    }

    @Test
    public void shouldRejectNonIntegerAndOutOfRangePushModeCommands() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        for (String value : new String[] { "-1", "3", "1.5", "2147483648" }) {
            subject.handleCommand(uid, new DecimalType(value));
        }
        verifyNoInteractions(robonectClientMock, commandScheduler);
    }

    @Test
    public void shouldLeaveMissingPushModeUndefinedAndRecoverOnLaterRead() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        when(robonectClientMock.getPushModeViaWebInterface()).thenReturn(null, 0);
        subject.handleCommand(uid, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(uid, UnDefType.UNDEF);
        subject.handleCommand(uid, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(uid, new DecimalType(0));
    }

    @Test
    public void shouldApplyMissingPushModeWriteResponseWithoutSchedulingRead() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        when(robonectClientMock.updatePushModeViaWebInterface(2)).thenReturn(null);
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        subject.handleCommand(uid, new DecimalType(2));
        verify(callbackMock).stateUpdated(uid, UnDefType.UNDEF);
        verifyNoInteractions(commandScheduler);
        verify(robonectClientMock, never()).getPushModeViaWebInterface();
    }

    private void useRealThingForPushMode() {
        subject = new RobonectHandler(
                ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3")).build(),
                httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
    }

    @Test
    public void shouldDiscardPushModeResponseWhenApiConfigurationChangesDuringRequest() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        when(robonectClientMock.getPushModeViaWebInterface()).thenAnswer(invocation -> {
            subject.configureUnofficialChannels(false);
            subject.configureUnofficialChannels(true);
            return 2;
        });
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "push-mode"), RefreshType.REFRESH);
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "00:00", "08:15", "23:59" })
    public void shouldPreserveTimerLocalTimeOnReadAndWrite(String time) {
        ThingUID uid = new ThingUID("1:2:3");
        subject = new RobonectHandler(ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build(),
                httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        when(robonectClientMock.getTimersInfo()).thenReturn(new ModelParser().parse("""
                {"successful":true,"timer":[{"id":1,"start":"%s","end":"%s"}]}
                """.formatted(time, time), TimersInfo.class));
        ChannelUID start = new ChannelUID(uid, "timer-1-start");
        subject.handleCommand(start, RefreshType.REFRESH);
        var value = ArgumentCaptor.forClass(State.class);
        verify(callbackMock).stateUpdated(eq(start), value.capture());
        DateTimeType state = (DateTimeType) value.getValue();
        assertEquals(LocalTime.parse(time), state.getZonedDateTime(TIME_ZONE).toLocalTime());
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "timer-1-end"), state);
        when(robonectClientMock.updateTimer(1, "start", time))
                .thenReturn(new ModelParser().parse("{\"successful\":true}", TimerInfo.class));
        subject.handleCommand(start, state);
        verify(robonectClientMock).updateTimer(1, "start", time);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 7 })
    public void shouldConvertTimerCommandsInBindingTimeZoneInWinterAndSummer(int month) {
        when(robonectClientMock.updateTimer(1, "start", "08:15"))
                .thenReturn(new ModelParser().parse("{\"successful\":true}", TimerInfo.class));
        DateTimeType command = new DateTimeType(ZonedDateTime.of(2026, month, 15, 8, 15, 0, 0, TIME_ZONE).toInstant());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "timer-1-start"), command);
        verify(robonectClientMock).updateTimer(1, "start", "08:15");
    }

    private void runScheduledRefresh() {
        var refresh = ArgumentCaptor.forClass(Runnable.class);
        verify(commandScheduler).schedule(refresh.capture(), eq(5L), eq(TimeUnit.SECONDS));
        refresh.getValue().run();
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
