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

import java.time.Duration;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.robonect.internal.RobonectBindingConstants;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.RobonectDiagnosticsApi;
import org.openhab.binding.robonect.internal.RobonectMowerApi;
import org.openhab.binding.robonect.internal.RobonectPushApi;
import org.openhab.binding.robonect.internal.RobonectRemoteStartApi;
import org.openhab.binding.robonect.internal.RobonectTimerApi;
import org.openhab.binding.robonect.internal.RobonectWeatherApi;
import org.openhab.binding.robonect.internal.model.AjaxInfo;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.ExtInfo;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.MotorInfo;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.MowerStatus;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.NextTimer;
import org.openhab.binding.robonect.internal.model.PortalInfo;
import org.openhab.binding.robonect.internal.model.PushInfo;
import org.openhab.binding.robonect.internal.model.RemoteInfo;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.Timer;
import org.openhab.binding.robonect.internal.model.TimerInfo;
import org.openhab.binding.robonect.internal.model.TimersInfo;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.WeatherInfo;
import org.openhab.binding.robonect.internal.model.WireInfo;
import org.openhab.binding.robonect.internal.model.Wlan;
import org.openhab.binding.robonect.internal.model.WlanInfo;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonSyntaxException;

/**
 * The goal of this class is to test RobonectHandler in isolation.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Channel, command, and polling coverage
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RobonectHandlerTest {

    private static final ZoneId TIME_ZONE = ZoneId.of("Europe/Berlin");

    private RobonectHandler subject;

    private @Mock Thing robonectThingMock;
    private @Mock RobonectClient robonectClientMock;
    private @Mock RobonectMowerApi mowerApiMock;
    private @Mock RobonectDiagnosticsApi diagnosticsApiMock;
    private @Mock RobonectPushApi pushApiMock;
    private @Mock RobonectRemoteStartApi remoteStartApiMock;
    private @Mock RobonectTimerApi timerApiMock;
    private @Mock RobonectWeatherApi weatherApiMock;
    private @Mock ThingHandlerCallback callbackMock;
    private @Mock HttpClientFactory httpClientFactoryMock;
    private @Mock TimeZoneProvider timeZoneProvider;
    private @Mock ScheduledExecutorService commandScheduler;
    private @Mock ScheduledFuture<?> commandRefreshJob;
    private @Mock ScheduledFuture<?> normalPollingJob;
    private @Mock ScheduledFuture<?> detailedPollingJob;
    private @Mock org.eclipse.jetty.client.HttpClient httpClient;

    @BeforeEach
    public void setUp() {
        when(robonectClientMock.mower()).thenReturn(mowerApiMock);
        when(robonectClientMock.diagnostics()).thenReturn(diagnosticsApiMock);
        when(robonectClientMock.push()).thenReturn(pushApiMock);
        when(robonectClientMock.remoteStart()).thenReturn(remoteStartApiMock);
        when(robonectClientMock.timer()).thenReturn(timerApiMock);
        when(robonectClientMock.weather()).thenReturn(weatherApiMock);
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

    @ParameterizedTest
    @CsvSource({ "1, false", "61, false", "61, true" })
    public void shouldPollNormalAndDetailedChannelsAtIndependentIntervals(int interval, boolean unofficialApi) {
        List<Runnable> pollers = initializePolling(
                Map.of("detailedPollInterval", interval, "useUnofficialApi", unofficialApi));
        verify(commandScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(7L), eq(TimeUnit.SECONDS));
        verify(commandScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq((long) interval),
                eq(TimeUnit.SECONDS));

        pollers.getFirst().run();
        verify(diagnosticsApiMock).getVersionInfo();
        verify(mowerApiMock).getMowerInfo();
        verify(diagnosticsApiMock).getWlanInfo();
        verify(diagnosticsApiMock, never()).getBatteryInfo();
        clearInvocations(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);

        pollers.getLast().run();
        verify(pushApiMock, times(unofficialApi ? 1 : 0)).getPushModeViaWebInterface();
        verify(diagnosticsApiMock).getBatteryInfo();
        verify(diagnosticsApiMock).getMotorInfo();
        verify(diagnosticsApiMock).getHourInfo();
        verify(remoteStartApiMock).getRemoteInfo();
        verify(diagnosticsApiMock).getWireInfo();
        verify(diagnosticsApiMock).getPortalInfo();
        verify(timerApiMock).getTimersInfo();
        verify(weatherApiMock).getWeatherInfo();
        verify(weatherApiMock, times(unofficialApi ? 1 : 0)).getWeatherIntervalViaWebInterface();
        verify(pushApiMock).getExtInfo();
        verify(pushApiMock).getPushInfo();
        verify(mowerApiMock).getDoorInfo();
        verifyNoMoreInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
    }

    @Test
    public void shouldNotScheduleDetailedPollingByDefault() {
        assertEquals(1, initializePolling(Map.of()).size());
        verify(commandScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), eq(0L), anyLong(),
                eq(TimeUnit.SECONDS));
    }

    @Test
    public void shouldReadVersionOnlyOnFirstNormalPollUntilCommunicationFailure() {
        Runnable poller = initializePolling(Map.of()).getFirst();
        poller.run();
        poller.run();
        verify(diagnosticsApiMock).getVersionInfo();
        verify(mowerApiMock, times(2)).getMowerInfo();
        verify(diagnosticsApiMock, times(2)).getWlanInfo();

        when(mowerApiMock.getMowerInfo()).thenThrow(new RobonectCommunicationException("Unavailable"))
                .thenReturn(createSuccessfulMowerInfoResponse());
        poller.run();
        poller.run();
        verify(diagnosticsApiMock, times(2)).getVersionInfo();
        verify(mowerApiMock, times(4)).getMowerInfo();
        verify(diagnosticsApiMock, times(3)).getWlanInfo();
    }

    @Test
    public void shouldNotScheduleDetailedPollingWhenIntervalIsZeroEvenWithUnofficialApiEnabled() {
        List<Runnable> pollers = initializePolling(Map.of("detailedPollInterval", 0, "useUnofficialApi", true));
        assertEquals(1, pollers.size());
        verify(commandScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), eq(0L), anyLong(),
                eq(TimeUnit.SECONDS));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
    }

    @Test
    public void shouldCancelBothPollersOnDisposeAndIgnoreCancelledRuns() {
        List<Runnable> pollers = initializePolling(Map.of("detailedPollInterval", 61));
        subject.dispose();
        verify(normalPollingJob).cancel(true);
        verify(detailedPollingJob).cancel(true);
        clearInvocations(callbackMock);
        pollers.forEach(Runnable::run);
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, callbackMock);
    }

    @Test
    public void shouldReplaceBothPollingSchedulesOnReinitialization() {
        List<Runnable> pollers = initializePolling(Map.of("detailedPollInterval", 61));
        Configuration config = new Configuration(subject.getThing().getConfiguration().getProperties());
        config.put("detailedPollInterval", 91);
        subject.thingUpdated(ThingBuilder.create(subject.getThing()).withConfiguration(config).build());
        subject.setRobonectClient(robonectClientMock);
        verify(normalPollingJob).cancel(true);
        verify(detailedPollingJob).cancel(true);
        verify(commandScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(91L), eq(TimeUnit.SECONDS));
        clearInvocations(callbackMock);
        pollers.forEach(Runnable::run);
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, callbackMock);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldEnableOrDisableDetailedPollingWhenIntervalChanges(boolean initiallyEnabled) {
        List<Runnable> pollers = initializePolling(Map.of("detailedPollInterval", initiallyEnabled ? 61 : 0));
        Configuration config = new Configuration(subject.getThing().getConfiguration().getProperties());
        config.put("detailedPollInterval", initiallyEnabled ? 0 : 61);
        clearInvocations(commandScheduler);

        subject.thingUpdated(ThingBuilder.create(subject.getThing()).withConfiguration(config).build());
        subject.setRobonectClient(robonectClientMock);

        verify(normalPollingJob).cancel(true);
        verify(detailedPollingJob, times(initiallyEnabled ? 1 : 0)).cancel(true);
        verify(commandScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(7L), eq(TimeUnit.SECONDS));
        verify(commandScheduler, times(initiallyEnabled ? 0 : 1)).scheduleWithFixedDelay(any(Runnable.class), eq(0L),
                eq(61L), eq(TimeUnit.SECONDS));
        clearInvocations(callbackMock);
        pollers.forEach(Runnable::run);
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, callbackMock);

        if (!initiallyEnabled) {
            var tasks = ArgumentCaptor.forClass(Runnable.class);
            verify(commandScheduler).scheduleWithFixedDelay(tasks.capture(), eq(0L), eq(61L), eq(TimeUnit.SECONDS));
            tasks.getValue().run();
            verify(diagnosticsApiMock).getBatteryInfo();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldRecoverNormalPollingAfterCommunicationOrJsonFailure(boolean malformedJson) {
        List<Runnable> pollers = initializePolling(Map.of("detailedPollInterval", 61));
        RuntimeException failure = malformedJson ? new JsonSyntaxException("Malformed response")
                : new RobonectCommunicationException("Unavailable");
        when(mowerApiMock.getMowerInfo()).thenThrow(failure).thenReturn(createSuccessfulMowerInfoResponse());
        pollers.getFirst().run();
        pollers.getLast().run();
        pollers.getFirst().run();
        verify(mowerApiMock, times(2)).getMowerInfo();
        verify(diagnosticsApiMock).getBatteryInfo();
        verify(callbackMock, atLeastOnce()).statusUpdated(eq(subject.getThing()),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        if (!malformedJson) {
            verify(callbackMock).statusUpdated(eq(subject.getThing()),
                    argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
            verify(diagnosticsApiMock, times(2)).getVersionInfo();
        }
    }

    @Test
    public void shouldStopDetailedPollingAndDiscardResponseAfterReinitialization() {
        List<Runnable> pollers = initializePolling(Map.of("detailedPollInterval", 61));
        BatteryInfo result = ModelParser.parse("""
                {"successful":true,"batteries":[{"charge":65}]}
                """, BatteryInfo.class);
        when(diagnosticsApiMock.getBatteryInfo()).thenAnswer(invocation -> {
            subject.initialize();
            subject.setRobonectClient(robonectClientMock);
            clearInvocations(callbackMock);
            return result;
        });
        pollers.getLast().run();
        verify(diagnosticsApiMock).getBatteryInfo();
        verifyNoMoreInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
        verifyNoInteractions(callbackMock);
    }

    @ParameterizedTest
    @ValueSource(strings = { "serialize", "dispose", "interrupt" })
    @Timeout(30)
    public void shouldSerializePollersAndCancelWaitingPoller(String action) throws Exception {
        boolean dispose = "dispose".equals(action);
        boolean interrupt = "interrupt".equals(action);
        List<Runnable> pollers = initializePolling(Map.of("detailedPollInterval", 61));
        CountDownLatch statusEntered = new CountDownLatch(1);
        CountDownLatch releaseStatus = new CountDownLatch(1);
        AtomicReference<Thread> detailedThread = new AtomicReference<>();
        when(mowerApiMock.getMowerInfo()).thenAnswer(invocation -> {
            statusEntered.countDown();
            assertTrue(releaseStatus.await(10, TimeUnit.SECONDS));
            return createSuccessfulMowerInfoResponse();
        });
        var executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> normal = executor.submit(pollers.getFirst());
            assertTrue(statusEntered.await(10, TimeUnit.SECONDS));
            Future<?> detailed = executor.submit(() -> {
                detailedThread.set(Thread.currentThread());
                pollers.getLast().run();
            });
            assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
                while (true) {
                    Thread thread = detailedThread.get();
                    if (detailed.isDone()) {
                        detailed.get();
                        fail("Detailed polling completed while normal polling was blocked");
                    }
                    if (thread != null && thread.getState() == Thread.State.WAITING) {
                        break;
                    }
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
                }
            });
            verify(diagnosticsApiMock, never()).getBatteryInfo();
            if (dispose) {
                subject.dispose();
                clearInvocations(callbackMock);
            } else if (interrupt) {
                Objects.requireNonNull(detailedThread.get()).interrupt();
                detailed.get(10, TimeUnit.SECONDS);
            }
            releaseStatus.countDown();
            normal.get(10, TimeUnit.SECONDS);
            detailed.get(10, TimeUnit.SECONDS);
            if (dispose) {
                verifyNoInteractions(callbackMock);
                verify(diagnosticsApiMock, never()).getWlanInfo();
                verify(diagnosticsApiMock, never()).getBatteryInfo();
            } else if (interrupt) {
                verify(diagnosticsApiMock).getWlanInfo();
                verify(diagnosticsApiMock, never()).getBatteryInfo();
            } else {
                var order = inOrder(mowerApiMock, diagnosticsApiMock);
                order.verify(mowerApiMock).getMowerInfo();
                order.verify(diagnosticsApiMock).getWlanInfo();
                order.verify(diagnosticsApiMock).getBatteryInfo();
            }
        } finally {
            releaseStatus.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private List<Runnable> initializePolling(Map<String, Object> parameters) {
        Map<String, Object> config = new HashMap<>(parameters);
        config.put("host", "localhost");
        config.put("pollInterval", 7);
        config.put("offlineTimeout", 30);
        config.put("timezone", TIME_ZONE.getId());
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3"))
                .withConfiguration(new Configuration(config)).build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        doReturn(normalPollingJob, detailedPollingJob).when(commandScheduler)
                .scheduleWithFixedDelay(any(Runnable.class), eq(0L), anyLong(), eq(TimeUnit.SECONDS));
        subject.initialize();
        subject.setRobonectClient(robonectClientMock);
        when(diagnosticsApiMock.getVersionInfo()).thenReturn(new VersionInfo());
        when(mowerApiMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());

        when(diagnosticsApiMock.getWlanInfo()).thenReturn(ModelParser.parse("{\"successful\":true}", WlanInfo.class));
        when(diagnosticsApiMock.getBatteryInfo())
                .thenReturn(ModelParser.parse("{\"successful\":true}", BatteryInfo.class));
        when(diagnosticsApiMock.getMotorInfo()).thenReturn(ModelParser.parse("{\"successful\":true}", MotorInfo.class));
        when(diagnosticsApiMock.getHourInfo()).thenReturn(ModelParser.parse("{\"successful\":true}", HourInfo.class));
        when(remoteStartApiMock.getRemoteInfo())
                .thenReturn(ModelParser.parse("{\"successful\":true}", RemoteInfo.class));
        when(diagnosticsApiMock.getWireInfo()).thenReturn(ModelParser.parse("{\"successful\":true}", WireInfo.class));
        when(diagnosticsApiMock.getPortalInfo())
                .thenReturn(ModelParser.parse("{\"successful\":true}", PortalInfo.class));
        when(timerApiMock.getTimersInfo()).thenReturn(ModelParser.parse("{\"successful\":true}", TimersInfo.class));
        when(weatherApiMock.getWeatherInfo())
                .thenReturn(ModelParser.parse("{\"successful\":true,\"service\":{}}", WeatherInfo.class));
        when(pushApiMock.getExtInfo()).thenReturn(ModelParser.parse("{\"successful\":true}", ExtInfo.class));
        when(pushApiMock.getPushInfo()).thenReturn(ModelParser.parse("{\"successful\":true}", PushInfo.class));
        when(mowerApiMock.getDoorInfo()).thenReturn(ModelParser.parse("{\"successful\":true}", DoorInfo.class));
        var tasks = ArgumentCaptor.forClass(Runnable.class);
        verify(commandScheduler, atLeastOnce()).scheduleWithFixedDelay(tasks.capture(), eq(0L), anyLong(),
                eq(TimeUnit.SECONDS));
        clearInvocations(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, callbackMock);
        return tasks.getAllValues();
    }

    @Test
    public void shouldCreateChannelsForEveryWireSensorAndBattery() {
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3")).build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);

        when(diagnosticsApiMock.getWireInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"sensors":[{"description":"Left","quality":70,"g2":0},
                {"description":"Right","quality":80,"g2":-25},{"description":"Rear","quality":90},
                {"description":"Extra","quality":60},{"description":"Fifth","quality":50,"g2":50}]}
                """, WireInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "wire-sensor-1-g2"), RefreshType.REFRESH);
        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(callbackMock).thingUpdated(updated.capture());
        assertNotNull(updated.getValue().getChannel("wire-sensor-5-label"));
        assertNotNull(updated.getValue().getChannel("wire-sensor-5-g2"));
        assertEquals("Wire Sensor 5 Signal G2",
                Objects.requireNonNull(updated.getValue().getChannel("wire-sensor-5-g2")).getLabel());
        verify(callbackMock).stateUpdated(new ChannelUID(thing.getUID(), "wire-sensor-1-g2"), new DecimalType(0));
        verify(callbackMock).stateUpdated(new ChannelUID(thing.getUID(), "wire-sensor-2-g2"), new DecimalType(-25));
        verify(callbackMock).stateUpdated(new ChannelUID(thing.getUID(), "wire-sensor-5-g2"), new DecimalType(50));
        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(thing.getUID(), "wire-sensor-3-g2")), any());

        when(diagnosticsApiMock.getBatteryInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"batteries":[{"id":0,"charge":70},{"id":1,"charge":80}]}
                """, BatteryInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "battery-1-charge"), RefreshType.REFRESH);
        verify(callbackMock, atLeastOnce()).thingUpdated(updated.capture());
        assertNotNull(updated.getValue().getChannel("battery-2-charge"));
        assertEquals("Battery 2 Charge",
                Objects.requireNonNull(updated.getValue().getChannel("battery-2-charge")).getLabel());
        assertNotNull(updated.getValue().getChannel("wire-sensor-5-label"));
        verify(callbackMock).stateUpdated(new ChannelUID(thing.getUID(), "battery-1-charge"),
                new QuantityType<>(70, org.openhab.core.library.unit.Units.PERCENT));
        verify(callbackMock).stateUpdated(new ChannelUID(thing.getUID(), "battery-2-charge"),
                new QuantityType<>(80, Units.PERCENT));

        when(diagnosticsApiMock.getWireInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"sensors":[{"description":"Left","quality":70}]}
                """, WireInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "wire-sensor-1-label"), RefreshType.REFRESH);
        verify(callbackMock, atLeastOnce()).thingUpdated(updated.capture());
        assertNull(updated.getValue().getChannel("wire-sensor-5-label"));
        assertNull(updated.getValue().getChannel("wire-sensor-5-g2"));
        verify(callbackMock).stateUpdated(eq(new ChannelUID(thing.getUID(), "wire-sensor-1-g2")), any());
        assertNotNull(updated.getValue().getChannel("battery-2-charge"));

        when(diagnosticsApiMock.getBatteryInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"batteries":[]}
                """, BatteryInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "battery-1-charge"), RefreshType.REFRESH);
        assertTrue(subject.getThing().getChannels().stream()
                .noneMatch(channel -> channel.getUID().getId().startsWith("battery-")));
        assertNotNull(subject.getThing().getChannel("wire-sensor-1-label"));
        verify(callbackMock, never()).stateUpdated(argThat(channel -> channel.getId().matches("battery-[a-z].*")),
                any());
    }

    @Test
    public void shouldPreserveOtherPushTriggerSettingWhenSendingCommand() {
        PushInfo push = ModelParser.parse("""
                {"successful":true,"push":{"trigger":{"trigger12":
                {"name":"Way home","enter":false,"leave":true}}}}
                """, PushInfo.class);
        when(pushApiMock.getPushInfo()).thenReturn(push);
        when(pushApiMock.updatePush("trigger12", "3"))
                .thenReturn(ModelParser.parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "push-trigger-12-way_home-enter"), OnOffType.ON);
        verify(pushApiMock).updatePush("trigger12", "3");
    }

    @Test
    public void shouldPreserveEnterSettingWhenSendingLeaveCommand() {
        when(pushApiMock.getPushInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"push":{"trigger":{"trigger12":
                {"name":"Way home","enter":true,"leave":false}}}}
                """, PushInfo.class));
        when(pushApiMock.updatePush("trigger12", "3"))
                .thenReturn(ModelParser.parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "push-trigger-12-way_home-leave"), OnOffType.ON);
        verify(pushApiMock).updatePush("trigger12", "3");
    }

    @Test
    public void shouldUpdateAndTruncateRemoteStartName() {
        RemoteInfo response = new RemoteInfo();
        response.setSuccessful(true);
        when(remoteStartApiMock.updateRemoteStart(eq(2), eq("1234567890123456"), isNull(Integer.class),
                isNull(Integer.class))).thenReturn(response);
        when(remoteStartApiMock.getRemoteInfo()).thenReturn(response);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "remote-2-name"),
                new StringType("12345678901234567890"));

        verify(remoteStartApiMock).updateRemoteStart(eq(2), eq("1234567890123456"), isNull(Integer.class),
                isNull(Integer.class));
    }

    @Test
    public void shouldIgnoreOutOfRangeRemoteStartDistance() {
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "remote-2-distance"),
                new QuantityType<>(301, org.openhab.core.library.unit.SIUnits.METRE));

        verify(remoteStartApiMock, never()).updateRemoteStart(anyInt(), any(), any(), any());
    }

    @Test
    public void shouldUpdateExistingPushChannelLabelsOnlyOnce() {
        ThingUID uid = new ThingUID("1:2:3");
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid)
                .withChannels(
                        ChannelBuilder.create(new ChannelUID(uid, "push-trigger-0-parked-enter"), "Switch")
                                .withLabel("Push Trigger Setting").build(),
                        ChannelBuilder.create(new ChannelUID(uid, "push-trigger-0-parked-leave"), "Switch")
                                .withLabel("Push Trigger Setting").build())
                .build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        when(pushApiMock.getPushInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"push":{"trigger":{
                "trigger0":{"name":"Parked","enter":true,"leave":false}}}}
                """, PushInfo.class));

        ChannelUID refresh = new ChannelUID(uid, "push-interval");
        subject.handleCommand(refresh, RefreshType.REFRESH);

        assertEquals("Push Trigger 0 Parked Enter",
                Objects.requireNonNull(subject.getThing().getChannel("push-trigger-0-parked-enter")).getLabel());
        assertEquals("Push Trigger 0 Parked Leave",
                Objects.requireNonNull(subject.getThing().getChannel("push-trigger-0-parked-leave")).getLabel());
        clearInvocations(callbackMock);
        subject.handleCommand(refresh, RefreshType.REFRESH);
        verify(callbackMock, never()).thingUpdated(any());
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
        when(pushApiMock.getPushInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"push":{"trigger":{"interval":30,
                "trigger0":{"name":"Parked","enter":true,"leave":false},
                "trigger12":{"name":"WAY Home","enter":false,"leave":true}}}}
                """, PushInfo.class));
        subject.handleCommand(new ChannelUID(thing.getUID(), "push-interval"), RefreshType.REFRESH);
        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(callbackMock).thingUpdated(updated.capture());
        assertNotNull(updated.getValue().getChannel("push-trigger-0-parked-enter"));
        assertNotNull(updated.getValue().getChannel("push-trigger-12-way_home-leave"));
        assertEquals("Push Trigger 12 WAY Home Leave",
                Objects.requireNonNull(updated.getValue().getChannel("push-trigger-12-way_home-leave")).getLabel());
        assertEquals(4, updated.getValue().getChannels().size());
        assertNull(updated.getValue().getChannel("push-trigger-0-name"));
        assertNull(updated.getValue().getChannel("push-trigger-12-enter"));
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "push-trigger-12-way_home-leave"), OnOffType.ON);

        when(pushApiMock.getPushInfo()).thenReturn(ModelParser.parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-interval"), RefreshType.REFRESH);
        verify(callbackMock).thingUpdated(any());

        when(pushApiMock.getPushInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"push":{"trigger":{
                "trigger12":{"name":"Mowing","enter":true,"leave":false}}}}
                """, PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-interval"), RefreshType.REFRESH);
        verify(callbackMock, times(2)).thingUpdated(updated.capture());
        assertNull(updated.getValue().getChannel("push-trigger-12-way_home-leave"));
        assertNotNull(updated.getValue().getChannel("push-trigger-12-mowing-enter"));
        when(pushApiMock.updatePush("trigger12", "1"))
                .thenReturn(ModelParser.parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-trigger-12-way_home-enter"), OnOffType.ON);
        verify(pushApiMock).updatePush("trigger12", "1");
    }

    @Test
    public void shouldKeepDuplicatePushTriggerNamesDistinctAndIgnoreInvalidNames() {
        ThingUID uid = new ThingUID("1:2:3");
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build();
        subject = new RobonectHandler(thing, httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        when(pushApiMock.getPushInfo()).thenReturn(ModelParser.parse("""
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
        when(pushApiMock.updatePush("trigger2", "3"))
                .thenReturn(ModelParser.parse("{\"successful\":false}", PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-trigger-2-way_home-enter"), OnOffType.ON);
        verify(pushApiMock).updatePush("trigger2", "3");
    }

    @Test
    public void shouldPublishRecentDurationsAndStatistics() {
        HourInfo hours = ModelParser.parse("""
                {"successful":true,"seek":[{"duration":2},{"duration":4},{"duration":6}],
                "mowing":[{"duration":10},{"duration":20}]}
                """, HourInfo.class);
        when(diagnosticsApiMock.getHourInfo()).thenReturn(hours);
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
        MowerInfo info = ModelParser.parse("""
                {"successful":true,"name":"Mower","status":{"status":2,"mode":0,
                "stopped":false,"battery":50,"hours":1,"duration":2,"distance":0,
                "home":true,"prevent":3}}
                """, MowerInfo.class);
        when(mowerApiMock.getMowerInfo()).thenReturn(info);
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
        when(mowerApiMock.getMowerInfo()).thenReturn(mowerInfo);

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
        when(mowerApiMock.getMowerInfo()).thenReturn(incompleteInfo);

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
        when(mowerApiMock.getMowerInfo()).thenReturn(mowerInfo);
        when(diagnosticsApiMock.errorList()).thenReturn(errorList);

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
        when(mowerApiMock.getMowerInfo()).thenReturn(mowerInfo);

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
        when(mowerApiMock.getMowerInfo()).thenReturn(mowerInfo);

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
        when(mowerApiMock.getMowerInfo()).thenReturn(mowerInfo);
        when(mowerApiMock.getAjaxInfo()).thenReturn(ajaxInfo);

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
        when(mowerApiMock.getMowerInfo()).thenReturn(mowerInfo);
        when(mowerApiMock.getAjaxInfo()).thenReturn(ajaxInfo);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                RefreshType.REFRESH);

        verify(callbackMock).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_MODE)),
                eq(new StringType("EOD")));
    }

    @Test
    public void shouldResetErrorsOnOnCommand() {
        RobonectAnswer response = ModelParser.parse("{\"successful\":true}", RobonectAnswer.class);

        when(mowerApiMock.resetErrors()).thenReturn(response);
        when(mowerApiMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_RESET_ERROR),
                OnOffType.ON);

        verify(mowerApiMock).resetErrors();
        verify(callbackMock, never()).stateUpdated(any(), any());
        verify(mowerApiMock, never()).getMowerInfo();
        runScheduledRefresh();
        verify(mowerApiMock).getMowerInfo();
    }

    @Test
    public void shouldReadStatusImmediatelyAfterStartCommand() {
        RobonectAnswer response = new RobonectAnswer();
        response.setSuccessful(true);
        when(mowerApiMock.start()).thenReturn(response);
        when(mowerApiMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_MOWER_START),
                OnOffType.ON);

        verify(mowerApiMock).start();
        var order = inOrder(mowerApiMock);
        order.verify(mowerApiMock).start();
        order.verify(mowerApiMock).getMowerInfo();
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
        when(mowerApiMock.getMowerInfo()).thenReturn(mowerInfo);
        when(diagnosticsApiMock.errorList()).thenReturn(errorList);

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
        assertEquals(new QuantityType<>(-88, Units.DECIBEL_MILLIWATTS), stateCaptorWlanStrength.getValue());
    }

    @Test
    public void shouldUseApiWeatherIntervalWithoutWebRequest() {
        subject.configureUnofficialChannels(true);
        when(weatherApiMock.getWeatherInfo()).thenReturn(
                ModelParser.parse("{\"successful\":true,\"service\":{\"interval\":30}}", WeatherInfo.class));
        ChannelUID channel = new ChannelUID(new ThingUID("1:2:3"), "weather-service-interval");
        subject.handleCommand(channel, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(channel, new QuantityType<>(30, Units.MINUTE));
        verify(weatherApiMock, never()).getWeatherIntervalViaWebInterface();
    }

    @Test
    public void shouldReadMissingWeatherIntervalOnlyWhenUnofficialApiEnabled() {
        when(weatherApiMock.getWeatherInfo())
                .thenReturn(ModelParser.parse("{\"successful\":true,\"service\":{}}", WeatherInfo.class));
        when(weatherApiMock.getWeatherIntervalViaWebInterface()).thenReturn(10);
        ChannelUID channel = new ChannelUID(new ThingUID("1:2:3"), "weather-service-interval");
        subject.handleCommand(channel, RefreshType.REFRESH);
        verify(weatherApiMock, never()).getWeatherIntervalViaWebInterface();
        subject.configureUnofficialChannels(true);
        subject.handleCommand(channel, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(channel, new QuantityType<>(10, Units.MINUTE));
        subject.configureUnofficialChannels(false);
        subject.handleCommand(channel, RefreshType.REFRESH);
        verify(weatherApiMock, times(1)).getWeatherIntervalViaWebInterface();
    }

    @Test
    public void shouldPreserveWeatherDataAndRetryAfterIntervalFallbackFailure() {
        subject.configureUnofficialChannels(true);
        when(weatherApiMock.getWeatherInfo()).thenReturn(
                ModelParser.parse("{\"successful\":true,\"service\":{},\"weather\":{\"rain\":20}}", WeatherInfo.class));
        when(weatherApiMock.getWeatherIntervalViaWebInterface())
                .thenThrow(new RobonectCommunicationException("Unavailable")).thenReturn(null).thenReturn(10);
        ChannelUID channel = new ChannelUID(new ThingUID("1:2:3"), "weather-service-interval");
        subject.handleCommand(channel, RefreshType.REFRESH);
        subject.handleCommand(channel, RefreshType.REFRESH);
        verify(callbackMock, never()).stateUpdated(eq(channel), any());
        subject.handleCommand(channel, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(channel, new QuantityType<>(10, Units.MINUTE));
        verify(callbackMock, times(3)).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-rain"),
                new QuantityType<>(20, Units.PERCENT));
    }

    @Test
    public void shouldNotReadWeatherIntervalForFailedApiResponse() {
        subject.configureUnofficialChannels(true);
        when(weatherApiMock.getWeatherInfo())
                .thenReturn(ModelParser.parse("{\"successful\":false}", WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-service-interval"), RefreshType.REFRESH);
        verify(weatherApiMock, never()).getWeatherIntervalViaWebInterface();
    }

    @Test
    public void shouldRefreshWeatherLocationConditionsAndUnixTimes() {
        when(weatherApiMock.getWeatherInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"service":{"location":{"cityid":3208628},
                "config":{"maxrain":40,"minhumidity":20,"maxhumidity":90}},
                "weather":{"rain":20,"sunrise":1792482616,"sunset":1792519939,
                "icon":"https://openweathermap.org/img/w/%s.png",
                "condition":{"toorainy":false,"toocold":true,"toowarm":false,"toodry":false,
                "toowet":false,"day":false,"night":true},
                "timestamp":{"date":"ignored","time":"ignored","unix":1792536282}}}
                """, WeatherInfo.class));

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-timestamp"), RefreshType.REFRESH);

        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-location-cityid"),
                new DecimalType(3208628));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-rain"),
                new QuantityType<>(20, Units.PERCENT));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-config-max-rain"),
                new QuantityType<>(40, Units.PERCENT));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-config-min-humidity"),
                new QuantityType<>(20, Units.PERCENT));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-config-max-humidity"),
                new QuantityType<>(90, Units.PERCENT));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-icon"),
                new StringType("https://openweathermap.org/img/w/%s.png"));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-sunrise"),
                new DateTimeType(ZonedDateTime.of(2026, 10, 20, 7, 50, 16, 0, TIME_ZONE)));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-sunset"),
                new DateTimeType(ZonedDateTime.of(2026, 10, 20, 18, 12, 19, 0, TIME_ZONE)));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-timestamp"),
                new DateTimeType(ZonedDateTime.of(2026, 10, 20, 22, 44, 42, 0, TIME_ZONE)));
        for (String condition : new String[] { "too-rainy", "too-warm", "too-dry", "too-wet", "day" }) {
            verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-condition-" + condition),
                    OnOffType.OFF);
        }
        for (String condition : new String[] { "too-cold", "night" }) {
            verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-condition-" + condition),
                    OnOffType.ON);
        }
        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), "weather-location-zip")),
                any());
        verify(callbackMock, never())
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), "weather-location-country")), any());
    }

    @Test
    public void shouldWriteCityIdAndApplyReturnedLocationBeforeDelayedRead() {
        WeatherInfo result = ModelParser.parse("""
                {"successful":true,"service":{"location":{"cityid":3208628}}}
                """, WeatherInfo.class);
        when(weatherApiMock.updateWeather("cityid", "3208628")).thenReturn(result);
        when(weatherApiMock.getWeatherInfo()).thenReturn(result);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-location-cityid"),
                new DecimalType(3208628));

        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-location-cityid"),
                new DecimalType(3208628));
        verify(weatherApiMock, never()).getWeatherInfo();
        runScheduledRefresh();
        verify(weatherApiMock).getWeatherInfo();
    }

    @Test
    public void shouldWriteZipWithoutLosingLeadingZeroAndApplyReturnedCountry() {
        WeatherInfo result = ModelParser.parse("""
                {"successful":true,"service":{"location":{"zip":"01234","country":"de"}}}
                """, WeatherInfo.class);
        when(weatherApiMock.updateWeather("zip", "01234")).thenReturn(result);
        when(weatherApiMock.getWeatherInfo()).thenReturn(result);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-location-zip"), new StringType("01234"));

        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-location-zip"),
                new StringType("01234"));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-location-country"),
                new StringType("de"));
        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), "weather-location-cityid")),
                any());
        verify(weatherApiMock, never()).getWeatherInfo();
        runScheduledRefresh();
        verify(weatherApiMock).getWeatherInfo();
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1", "1.5", "2147483648" })
    public void shouldRejectInvalidWeatherCityIds(String value) {
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-location-cityid"), new DecimalType(value));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, commandScheduler);
    }

    @Test
    public void shouldRejectWeatherCommandWithIncompatibleUnit() {
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-config-min-temperature"),
                new QuantityType<>(1, org.openhab.core.library.unit.SIUnits.METRE));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, commandScheduler);
    }

    @Test
    public void shouldNotWriteReadOnlyWeatherChannels() {
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-location-country"), new StringType("de"));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-condition-too-cold"), OnOffType.ON);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-icon"), new StringType("icon"));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-sunrise"),
                new DateTimeType(ZonedDateTime.of(2026, 10, 20, 7, 50, 16, 0, TIME_ZONE)));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, commandScheduler);
    }

    @Test
    public void shouldIgnoreMissingWeatherFieldsAndDateTimeWithoutUnix() {
        when(weatherApiMock.getWeatherInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"service":{"location":{}},"weather":{"condition":{},
                "timestamp":{"date":"2026-10-20","time":"22:44:42"}}}
                """, WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-timestamp"), RefreshType.REFRESH);
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid", "9223372036854775807" })
    public void shouldMarkInvalidWeatherTimestampUndefinedAndStillApplyOtherFields(String unix) {
        when(weatherApiMock.getWeatherInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"weather":{"timestamp":{"unix":"%s"},"condition":{"toocold":true}}}
                """.formatted(unix), WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-timestamp"), RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-timestamp"), UnDefType.UNDEF);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-condition-too-cold"),
                OnOffType.ON);
    }

    @Test
    public void shouldApplyPartialWeatherResponseBeforeDelayedRead() {
        WeatherInfo result = ModelParser.parse("""
                {"successful":true,"service":{"enable":true}}
                """, WeatherInfo.class);
        when(weatherApiMock.updateWeather("service", "1")).thenReturn(result);
        when(weatherApiMock.getWeatherInfo()).thenReturn(result);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-service-enabled"), OnOffType.ON);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-service-enabled"),
                OnOffType.ON);
        verify(weatherApiMock, never()).getWeatherInfo();
        runScheduledRefresh();
        verify(weatherApiMock).getWeatherInfo();
    }

    @Test
    public void shouldApplyRemoteResponseBeforeDelayedRead() {
        RemoteInfo result = ModelParser.parse("""
                {"successful":true,"remotestart_2":{"name":"Garden"}}
                """, RemoteInfo.class);
        when(remoteStartApiMock.updateRemoteStart(eq(2), eq("Garden"), isNull(), isNull())).thenReturn(result);
        when(remoteStartApiMock.getRemoteInfo()).thenReturn(result);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "remote-2-name"), new StringType("Garden"));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "remote-2-name"),
                new StringType("Garden"));
        verify(remoteStartApiMock, never()).getRemoteInfo();
        runScheduledRefresh();
        verify(remoteStartApiMock).getRemoteInfo();
    }

    @Test
    public void shouldApplyTimerResponseAndStillScheduleRead() {
        TimerInfo result = ModelParser.parse("""
                {"successful":true,"timer":{"id":1,"enabled":true}}
                """, TimerInfo.class);
        when(timerApiMock.updateTimer(1, "enable", "1")).thenReturn(result);
        when(timerApiMock.getTimersInfo()).thenReturn(ModelParser.parse("""
                {"successful":false}
                """, TimersInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "timer-1-enabled"), OnOffType.ON);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "timer-1-enabled"), OnOffType.ON);
        verify(timerApiMock, never()).getTimersInfo();
        runScheduledRefresh();
        verify(timerApiMock).getTimersInfo();
    }

    @Test
    public void shouldScheduleReadWhenSuccessfulResponseHasNoState() {
        when(weatherApiMock.updateWeather("service", "1"))
                .thenReturn(ModelParser.parse("{\"successful\":true}", WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-service-enabled"), OnOffType.ON);
        verify(callbackMock, never()).stateUpdated(any(), any());
        verify(weatherApiMock, never()).getWeatherInfo();
        verify(commandScheduler).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void shouldApplyDoorResponseBeforeDelayedRead() {
        DoorInfo result = ModelParser.parse("""
                {"successful":true,"door":{"enabled":true,"state":{"open":true,"close":false}}}
                """, DoorInfo.class);
        when(mowerApiMock.setDoorDirection(Direction.OPEN)).thenReturn(result);
        when(mowerApiMock.getDoorInfo()).thenReturn(result);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "door-state"), UpDownType.UP);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "door-state"),
                org.openhab.core.library.types.PercentType.ZERO);
        verify(mowerApiMock, never()).getDoorInfo();
        runScheduledRefresh();
        verify(mowerApiMock).getDoorInfo();
    }

    @Test
    public void shouldApplyNameResponseBeforeDelayedStatusRead() {
        when(mowerApiMock.setName("Garden mower"))
                .thenReturn(ModelParser.parse("{\"successful\":true,\"name\":\"Garden mower\"}", Name.class));
        when(mowerApiMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "name"), new StringType("Garden mower"));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "name"),
                new StringType("Garden mower"));
        verify(mowerApiMock, never()).getMowerInfo();
        runScheduledRefresh();
        verify(mowerApiMock).getMowerInfo();
    }

    @Test
    public void shouldApplyPartialPushWriteWithoutRemovingOtherTriggers() {
        ThingUID uid = new ThingUID("1:2:3");
        subject = new RobonectHandler(ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build(),
                httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        PushInfo full = ModelParser.parse("""
                {"successful":true,"push":{"trigger":{
                "trigger0":{"name":"Parked","enter":false,"leave":false},
                "trigger1":{"name":"Mowing","enter":false,"leave":true}}}}
                """, PushInfo.class);
        when(pushApiMock.getPushInfo()).thenReturn(full);
        subject.handleCommand(new ChannelUID(uid, "push-interval"), RefreshType.REFRESH);
        clearInvocations(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, callbackMock);
        when(pushApiMock.updatePush("trigger1", "3")).thenReturn(ModelParser.parse("""
                {"successful":true,"push":{"trigger":{
                "trigger1":{"name":"Mowing","enter":true}}}}
                """, PushInfo.class));
        subject.handleCommand(new ChannelUID(uid, "push-trigger-1-mowing-enter"), OnOffType.ON);
        verify(pushApiMock).getPushInfo();
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "push-trigger-1-mowing-enter"), OnOffType.ON);
        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(uid, "push-trigger-1-mowing-leave")), any());
        assertNotNull(subject.getThing().getChannel("push-trigger-0-parked-enter"));
        runScheduledRefresh();
        verify(pushApiMock, times(2)).getPushInfo();
    }

    @Test
    public void shouldReadStatusImmediatelyAfterModeAndStopWrites() {
        when(mowerApiMock.setMode(ModeCommand.Mode.HOME)).thenReturn(new RobonectAnswer());
        when(mowerApiMock.stop()).thenReturn(new RobonectAnswer());
        when(mowerApiMock.getMowerInfo()).thenReturn(createSuccessfulMowerInfoResponse());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "mode"), new StringType("HOME"));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "start"), OnOffType.OFF);
        var order = inOrder(mowerApiMock);
        order.verify(mowerApiMock).setMode(ModeCommand.Mode.HOME);
        order.verify(mowerApiMock).getMowerInfo();
        order.verify(mowerApiMock).stop();
        order.verify(mowerApiMock).getMowerInfo();
        verifyNoInteractions(commandScheduler);
    }

    @Test
    public void shouldIgnoreSupersededReadsAndCancelReadsOnDispose() {
        when(weatherApiMock.updateWeather("service", "1"))
                .thenReturn(ModelParser.parse("{\"successful\":true}", WeatherInfo.class));
        ChannelUID channel = new ChannelUID(new ThingUID("1:2:3"), "weather-service-enabled");
        subject.handleCommand(channel, OnOffType.ON);
        subject.handleCommand(channel, OnOffType.ON);
        var refreshes = ArgumentCaptor.forClass(Runnable.class);
        verify(commandScheduler, times(2)).schedule(refreshes.capture(), eq(5L), eq(TimeUnit.SECONDS));
        verify(commandRefreshJob).cancel(true);
        refreshes.getAllValues().getFirst().run();
        verify(weatherApiMock, never()).getWeatherInfo();
        subject.dispose();
        refreshes.getAllValues().getLast().run();
        verify(weatherApiMock, never()).getWeatherInfo();
        verify(commandRefreshJob, times(2)).cancel(true);
    }

    @Test
    public void shouldNotPublishReadResponseIfDisposedDuringRequest() {
        WeatherInfo result = ModelParser.parse("""
                {"successful":true,"service":{"enable":true}}
                """, WeatherInfo.class);
        when(weatherApiMock.updateWeather("service", "1")).thenReturn(result);
        when(weatherApiMock.getWeatherInfo()).thenAnswer(invocation -> {
            subject.dispose();
            return result;
        });
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-service-enabled"), OnOffType.ON);
        clearInvocations(callbackMock);
        runScheduledRefresh();
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    @Test
    public void shouldNotScheduleReadAfterRejectedWrite() {
        when(weatherApiMock.updateWeather("service", "1"))
                .thenReturn(ModelParser.parse("{\"successful\":false}", WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-service-enabled"), OnOffType.ON);
        verifyNoInteractions(commandScheduler);
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    @Test
    public void shouldCreateAndRemoveJobTimeChannelsWithAjaxScraping() {
        useRealThingForPushMode();
        subject.configureAjaxChannels(false);
        assertTrue(subject.getThing().getChannels().isEmpty());
        subject.configureUnofficialChannels(true);
        subject.configureAjaxChannels(true);
        subject.configureAjaxChannels(true);
        assertEquals(3, subject.getThing().getChannels().size());
        assertEquals("DateTime", subject.getThing().getChannel("job-start").getAcceptedItemType());
        assertEquals("DateTime", subject.getThing().getChannel("job-end").getAcceptedItemType());
        subject.configureAjaxChannels(false);
        assertNull(subject.getThing().getChannel("job-start"));
        assertNull(subject.getThing().getChannel("job-end"));
        assertNotNull(subject.getThing().getChannel("push-mode"));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
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
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
    }

    @Test
    public void shouldRefreshAndCommandPushModeOnlyWhenUnofficialApiEnabled() {
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        subject.handleCommand(uid, RefreshType.REFRESH);
        subject.handleCommand(uid, new DecimalType(2));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        when(pushApiMock.getPushModeViaWebInterface()).thenReturn(1);
        subject.handleCommand(uid, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(uid, new DecimalType(1));
        when(pushApiMock.updatePushModeViaWebInterface(2)).thenReturn(2);
        subject.handleCommand(uid, new DecimalType(2));
        verify(callbackMock).stateUpdated(uid, new DecimalType(2));
        verify(pushApiMock).getPushModeViaWebInterface();
        verifyNoInteractions(commandScheduler);
        verify(pushApiMock, never()).getPushInfo();
    }

    @Test
    public void shouldRejectNonIntegerAndOutOfRangePushModeCommands() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        for (String value : new String[] { "-1", "3", "1.5", "2147483648" }) {
            subject.handleCommand(uid, new DecimalType(value));
        }
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, commandScheduler);
    }

    @Test
    public void shouldLeaveMissingPushModeUndefinedAndRecoverOnLaterRead() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        when(pushApiMock.getPushModeViaWebInterface()).thenReturn(null, 0);
        subject.handleCommand(uid, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(uid, UnDefType.UNDEF);
        subject.handleCommand(uid, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(uid, new DecimalType(0));
    }

    @Test
    public void shouldApplyMissingPushModeWriteResponseWithoutSchedulingRead() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        when(pushApiMock.updatePushModeViaWebInterface(2)).thenReturn(null);
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        subject.handleCommand(uid, new DecimalType(2));
        verify(callbackMock).stateUpdated(uid, UnDefType.UNDEF);
        verifyNoInteractions(commandScheduler);
        verify(pushApiMock, never()).getPushModeViaWebInterface();
    }

    private void useRealThingForPushMode() {
        subject = new RobonectHandler(
                ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3")).build(),
                httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldDiscardPushModeResponseWhenApiDisabledDuringRequest(boolean write) {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        ChannelUID channel = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        if (write) {
            when(pushApiMock.updatePushModeViaWebInterface(2)).thenAnswer(invocation -> {
                subject.configureUnofficialChannels(false);
                return 2;
            });
            subject.handleCommand(channel, new DecimalType(2));
        } else {
            when(pushApiMock.getPushModeViaWebInterface()).thenAnswer(invocation -> {
                subject.configureUnofficialChannels(false);
                return 2;
            });
            subject.handleCommand(channel, RefreshType.REFRESH);
        }
        verify(callbackMock, never()).stateUpdated(any(), any());
        assertNull(subject.getThing().getChannel(channel));
    }

    @ParameterizedTest
    @ValueSource(strings = { "00:00", "08:15", "23:59" })
    public void shouldPreserveTimerLocalTimeOnReadAndWrite(String time) {
        ThingUID uid = new ThingUID("1:2:3");
        subject = new RobonectHandler(ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build(),
                httpClientFactoryMock, timeZoneProvider, null, commandScheduler);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
        when(timerApiMock.getTimersInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"timer":[{"id":1,"start":"%s","end":"%s"}]}
                """.formatted(time, time), TimersInfo.class));
        ChannelUID start = new ChannelUID(uid, "timer-1-start");
        subject.handleCommand(start, RefreshType.REFRESH);
        assertEquals("Timer 1 Start Time",
                Objects.requireNonNull(subject.getThing().getChannel("timer-1-start")).getLabel());
        assertEquals("Timer 1 End Time",
                Objects.requireNonNull(subject.getThing().getChannel("timer-1-end")).getLabel());
        assertEquals("Timer 1 Monday",
                Objects.requireNonNull(subject.getThing().getChannel("timer-1-monday")).getLabel());
        var value = ArgumentCaptor.forClass(State.class);
        verify(callbackMock).stateUpdated(eq(start), value.capture());
        DateTimeType state = (DateTimeType) value.getValue();
        assertEquals(LocalTime.parse(time), state.getZonedDateTime(TIME_ZONE).toLocalTime());
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "timer-1-end"), state);
        when(timerApiMock.updateTimer(1, "start", time))
                .thenReturn(ModelParser.parse("{\"successful\":true}", TimerInfo.class));
        subject.handleCommand(start, state);
        verify(timerApiMock).updateTimer(1, "start", time);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 7 })
    public void shouldConvertTimerCommandsInBindingTimeZoneInWinterAndSummer(int month) {
        when(timerApiMock.updateTimer(1, "start", "08:15"))
                .thenReturn(ModelParser.parse("{\"successful\":true}", TimerInfo.class));
        DateTimeType command = new DateTimeType(ZonedDateTime.of(2026, month, 15, 8, 15, 0, 0, TIME_ZONE).toInstant());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "timer-1-start"), command);
        verify(timerApiMock).updateTimer(1, "start", "08:15");
    }

    @Test
    public void shouldWritePushPasswordWithoutPublishingItAndApplyReturnedSettings() {
        var response = ModelParser.parse("""
                {"successful":true,"push":{"server":{"name":"push-user"}}}
                """, PushInfo.class);
        when(pushApiMock.updatePush("pass", "new-password")).thenReturn(response);
        when(pushApiMock.getPushInfo()).thenReturn(response);
        ChannelUID password = new ChannelUID(new ThingUID("1:2:3"), "push-password");
        subject.handleCommand(password, new StringType("new-password"));
        verify(pushApiMock).updatePush("pass", "new-password");
        verify(callbackMock).stateUpdated(password, UnDefType.UNDEF);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "push-username"),
                new StringType("push-user"));
        verify(callbackMock, never()).stateUpdated(eq(password), any(StringType.class));
        verify(pushApiMock, never()).getPushInfo();
        runScheduledRefresh();
        verify(pushApiMock).getPushInfo();
    }

    @Test
    public void shouldKeepPushPasswordUndefinedOnLinkAndRefreshWithoutReadingDevice() {
        ChannelUID password = new ChannelUID(new ThingUID("1:2:3"), "push-password");
        subject.channelLinked(password);
        subject.handleCommand(password, RefreshType.REFRESH);
        verify(callbackMock, times(2)).stateUpdated(password, UnDefType.UNDEF);
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, commandScheduler);
    }

    @Test
    public void shouldKeepPushPasswordUndefinedAfterRejectedWrite() {
        when(pushApiMock.updatePush("pass", "new-password"))
                .thenReturn(ModelParser.parse("{\"successful\":false}", PushInfo.class));
        ChannelUID password = new ChannelUID(new ThingUID("1:2:3"), "push-password");
        subject.handleCommand(password, new StringType("new-password"));
        verify(callbackMock).stateUpdated(password, UnDefType.UNDEF);
        verify(callbackMock, never()).stateUpdated(eq(password), any(StringType.class));
        verifyNoInteractions(commandScheduler);
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
