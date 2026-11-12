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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import org.openhab.binding.robonect.internal.RobonectPushServlet;
import org.openhab.binding.robonect.internal.RobonectRemoteStartApi;
import org.openhab.binding.robonect.internal.RobonectTimerApi;
import org.openhab.binding.robonect.internal.RobonectWeatherApi;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.ExtInfo;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.Job;
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
import org.openhab.binding.robonect.internal.model.cmd.DirectCommand;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.core.common.ThreadPoolManager;
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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonSyntaxException;

/**
 * The goal of this class is to test RobonectHandler in isolation.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Channel, command, polling, and push lifecycle coverage
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
    private @Mock RobonectPushServlet pushServletMock;
    private @Mock ScheduledExecutorService commandScheduler;
    private @Mock ScheduledFuture<?> commandRefreshJob;
    private @Mock ScheduledFuture<?> normalPollingJob;
    private @Mock ScheduledFuture<?> detailedPollingJob;
    private @Mock org.eclipse.jetty.client.HttpClient httpClient;

    /** Creates the handler with mocked endpoint APIs, a scheduler, and a Thing callback. */
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
        doReturn(commandRefreshJob).when(commandScheduler).schedule(any(Runnable.class), anyLong(),
                eq(TimeUnit.SECONDS));

        subject = createHandler(robonectThingMock, httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
    }

    private RobonectHandler createHandler(Thing thing, HttpClientFactory httpClientFactory,
            TimeZoneProvider timeZoneProvider, RobonectPushServlet pushServlet) {
        try (MockedStatic<ThreadPoolManager> threadPoolManager = Mockito.mockStatic(ThreadPoolManager.class)) {
            threadPoolManager.when(() -> ThreadPoolManager.getScheduledPool(anyString())).thenReturn(commandScheduler);
            return new RobonectHandler(thing, httpClientFactory, timeZoneProvider, pushServlet);
        }
    }

    /** Disposes the handler so its scheduled work cannot leak into another test. */
    @AfterEach
    public void tearDown() {
        subject.dispose();
    }

    /** Exercises pushes received both before a queued read and while its status request is in flight. */
    @Test
    public void shouldCoalesceQueuedPushesAndScheduleOneFollowUpForPushesDuringRead() {
        RobonectPushServlet servlet = mock(RobonectPushServlet.class);
        try (var clients = mockConstruction(RobonectClient.class,
                (client, context) -> when(client.mower()).thenReturn(mowerApiMock))) {
            initializePushHandler(servlet);
            var registrations = ArgumentCaptor.forClass(Runnable.class);
            verify(servlet).registerHandler(eq(subject.getThing().getUID()), registrations.capture(), eq("callback"),
                    eq("test-secret"));
            Runnable notify = registrations.getValue();
            var tasks = ArgumentCaptor.forClass(Runnable.class);
            when(mowerApiMock.getMowerInfo(anyBoolean())).thenAnswer(invocation -> {
                notify.run();
                notify.run();
                return createSuccessfulMowerInfoResponse();
            }).thenReturn(createSuccessfulMowerInfoResponse());

            notify.run();
            notify.run();
            verify(commandScheduler).schedule(tasks.capture(), eq(0L), eq(TimeUnit.SECONDS));
            tasks.getValue().run();

            verify(commandScheduler, times(2)).schedule(tasks.capture(), eq(0L), eq(TimeUnit.SECONDS));
            verify(mowerApiMock).getMowerInfo(anyBoolean());
            tasks.getValue().run();
            verify(mowerApiMock, times(2)).getMowerInfo(anyBoolean());
            verify(commandScheduler, times(2)).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
            assertEquals(1, clients.constructed().size());
        }
    }

    /** Retains the old push callback across reinitialization to verify that it can no longer schedule work. */
    @Test
    public void shouldCancelQueuedPushAndRejectOldRegistrationAfterReinitialization() {
        RobonectPushServlet servlet = mock(RobonectPushServlet.class);
        try (var clients = mockConstruction(RobonectClient.class,
                (client, context) -> when(client.mower()).thenReturn(mowerApiMock))) {
            initializePushHandler(servlet);
            var registrations = ArgumentCaptor.forClass(Runnable.class);
            verify(servlet).registerHandler(eq(subject.getThing().getUID()), registrations.capture(), eq("callback"),
                    eq("test-secret"));
            Runnable oldRegistration = registrations.getValue();
            oldRegistration.run();
            var tasks = ArgumentCaptor.forClass(Runnable.class);
            verify(commandScheduler).schedule(tasks.capture(), eq(0L), eq(TimeUnit.SECONDS));

            subject.dispose();
            verify(commandRefreshJob).cancel(true);
            subject.initialize();
            oldRegistration.run();
            tasks.getValue().run();

            verifyNoInteractions(mowerApiMock);
            verify(commandScheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
            assertEquals(2, clients.constructed().size());
        }
    }

    /** Reinitializes the handler from inside a push status read to check that its stale response is discarded. */
    @Test
    public void shouldDiscardPushResponseWhenTheSessionChangesDuringTheRead() {
        RobonectPushServlet servlet = mock(RobonectPushServlet.class);
        try (var clients = mockConstruction(RobonectClient.class,
                (client, context) -> when(client.mower()).thenReturn(mowerApiMock))) {
            initializePushHandler(servlet);
            var registrations = ArgumentCaptor.forClass(Runnable.class);
            verify(servlet).registerHandler(eq(subject.getThing().getUID()), registrations.capture(), eq("callback"),
                    eq("test-secret"));
            Runnable notify = registrations.getValue();
            when(mowerApiMock.getMowerInfo(anyBoolean())).thenAnswer(invocation -> {
                notify.run();
                subject.dispose();
                subject.initialize();
                clearInvocations(callbackMock);
                return createSuccessfulMowerInfoResponse();
            });

            notify.run();
            var tasks = ArgumentCaptor.forClass(Runnable.class);
            verify(commandScheduler).schedule(tasks.capture(), eq(0L), eq(TimeUnit.SECONDS));
            tasks.getValue().run();

            verifyNoInteractions(callbackMock);
            verify(commandScheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
            assertEquals(2, clients.constructed().size());
        }
    }

    /** Checks that push registration requires both a username and a password. */
    @Test
    public void shouldNotRegisterPushWithoutBothCredentials() {
        RobonectPushServlet servlet = mock(RobonectPushServlet.class);
        initializePushHandler(servlet, Map.of("pushUser", "callback"));
        verifyNoInteractions(servlet);
    }

    /**
     * Initializes push handling with valid test credentials.
     *
     * @param servlet the servlet that receives the registration
     */
    private void initializePushHandler(RobonectPushServlet servlet) {
        initializePushHandler(servlet, Map.of("pushUser", "callback", "pushPass", "test-secret"));
    }

    /**
     * Starts the handler with the supplied push configuration.
     *
     * @param servlet the servlet that receives the registration
     * @param credentials the push configuration entries to apply
     */
    private void initializePushHandler(RobonectPushServlet servlet, Map<String, Object> credentials) {
        var parameters = new HashMap<>(credentials);
        parameters.put("host", "localhost");
        parameters.put("timezone", TIME_ZONE.getId());
        var config = new Configuration(parameters);
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3"))
                .withConfiguration(config).build();
        subject = createHandler(thing, httpClientFactoryMock, timeZoneProvider, servlet);
        subject.setCallback(callbackMock);
        doReturn(commandRefreshJob).when(commandScheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
        subject.initialize();
    }

    /**
     * Runs both polling cycles to verify that their schedules and endpoint reads are independent.
     *
     * @param interval the detailed polling interval in seconds
     * @param unofficialApi whether to enable the unofficial API
     */
    @ParameterizedTest
    @CsvSource({ "1, false", "61, false", "61, true" })
    public void shouldPollNormalAndDetailedChannelsAtIndependentIntervals(int interval, boolean unofficialApi) {
        List<Runnable> pollers = initializePolling(
                Map.of("pollIntervalExtended", interval, "useWebApi", unofficialApi));
        verify(commandScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(7L), eq(TimeUnit.SECONDS));
        verify(commandScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq((long) interval),
                eq(TimeUnit.SECONDS));

        pollers.getFirst().run();
        verify(diagnosticsApiMock).getVersionInfo();
        verify(mowerApiMock).getMowerInfo(anyBoolean());
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

    /** Checks that the default configuration creates only the normal polling schedule. */
    @Test
    public void shouldNotScheduleDetailedPollingByDefault() {
        assertEquals(1, initializePolling(Map.of()).size());
        verify(commandScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), eq(0L), anyLong(),
                eq(TimeUnit.SECONDS));
    }

    /**
     * Exercises the polling schedule as the mower changes from active to inactive.
     *
     * @param activeStatus a mower status that requires more frequent drive diagnostics
     */
    @ParameterizedTest
    @EnumSource(value = MowerStatus.class, names = { "MOWING", "SEARCH_CHARGING_STATION", "SEARCHING" })
    public void shouldMoveDriveDiagnosticsToNormalPollingWhileMowerIsActive(MowerStatus activeStatus) {
        List<Runnable> pollers = initializePolling(Map.of("pollIntervalExtended", 61));
        MowerInfo active = createSuccessfulMowerInfoResponse();
        Objects.requireNonNull(active.getStatus()).setStatus(activeStatus);
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(active, createSuccessfulMowerInfoResponse());

        pollers.getFirst().run();
        verify(diagnosticsApiMock).getBatteryInfo();
        verify(diagnosticsApiMock).getMotorInfo();
        verify(diagnosticsApiMock).getWireInfo();

        clearInvocations(diagnosticsApiMock);
        pollers.getLast().run();
        verify(diagnosticsApiMock, never()).getBatteryInfo();
        verify(diagnosticsApiMock, never()).getMotorInfo();
        verify(diagnosticsApiMock, never()).getWireInfo();
        verify(diagnosticsApiMock).getHourInfo();

        pollers.getFirst().run();
        verify(diagnosticsApiMock, never()).getBatteryInfo();
        verify(diagnosticsApiMock, never()).getMotorInfo();
        verify(diagnosticsApiMock, never()).getWireInfo();
        pollers.getLast().run();
        verify(diagnosticsApiMock).getBatteryInfo();
        verify(diagnosticsApiMock).getMotorInfo();
        verify(diagnosticsApiMock).getWireInfo();
    }

    /** Verifies that version metadata is read again after a communication failure invalidates the earlier result. */
    @Test
    public void shouldReadVersionOnlyOnFirstNormalPollUntilCommunicationFailure() {
        Runnable poller = initializePolling(Map.of()).getFirst();
        poller.run();
        poller.run();
        verify(diagnosticsApiMock).getVersionInfo();
        verify(mowerApiMock, times(2)).getMowerInfo(anyBoolean());
        verify(diagnosticsApiMock, times(2)).getWlanInfo();

        when(mowerApiMock.getMowerInfo(anyBoolean())).thenThrow(new RobonectCommunicationException("Unavailable"))
                .thenReturn(createSuccessfulMowerInfoResponse());
        poller.run();
        poller.run();
        verify(diagnosticsApiMock, times(2)).getVersionInfo();
        verify(mowerApiMock, times(4)).getMowerInfo(anyBoolean());
        verify(diagnosticsApiMock, times(3)).getWlanInfo();
    }

    /** Checks that enabling the unofficial API leaves the zero detailed polling interval disabled. */
    @Test
    public void shouldNotScheduleDetailedPollingWhenIntervalIsZeroEvenWithUnofficialApiEnabled() {
        List<Runnable> pollers = initializePolling(Map.of("pollIntervalExtended", 0, "useWebApi", true));
        assertEquals(1, pollers.size());
        verify(commandScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), eq(0L), anyLong(),
                eq(TimeUnit.SECONDS));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
        MowerInfo active = createSuccessfulMowerInfoResponse();
        Objects.requireNonNull(active.getStatus()).setStatus(MowerStatus.MOWING);
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(active);
        pollers.getFirst().run();
        verify(diagnosticsApiMock, never()).getBatteryInfo();
        verify(diagnosticsApiMock, never()).getMotorInfo();
        verify(diagnosticsApiMock, never()).getWireInfo();
    }

    /** Invokes captured polling tasks after disposal to verify that they no longer access the mower. */
    @Test
    public void shouldCancelBothPollersOnDisposeAndIgnoreCancelledRuns() {
        List<Runnable> pollers = initializePolling(Map.of("pollIntervalExtended", 61));
        subject.dispose();
        verify(normalPollingJob).cancel(true);
        verify(detailedPollingJob).cancel(true);
        clearInvocations(callbackMock);
        pollers.forEach(Runnable::run);
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, callbackMock);
    }

    /** Checks that reinitializing the Thing cancels and replaces both schedules. */
    @Test
    public void shouldReplaceBothPollingSchedulesOnReinitialization() {
        List<Runnable> pollers = initializePolling(Map.of("pollIntervalExtended", 61));
        Configuration config = new Configuration(subject.getThing().getConfiguration().getProperties());
        config.put("pollIntervalExtended", 91);
        subject.thingUpdated(ThingBuilder.create(subject.getThing()).withConfiguration(config).build());
        subject.createEndpointHandlers(robonectClientMock);
        verify(normalPollingJob).cancel(true);
        verify(detailedPollingJob).cancel(true);
        verify(commandScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(91L), eq(TimeUnit.SECONDS));
        clearInvocations(callbackMock);
        pollers.forEach(Runnable::run);
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, callbackMock);
    }

    /**
     * Exercises both directions of a configuration change that toggles detailed polling.
     *
     * @param initiallyEnabled whether detailed polling is enabled before the configuration change
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldEnableOrDisableDetailedPollingWhenIntervalChanges(boolean initiallyEnabled) {
        List<Runnable> pollers = initializePolling(Map.of("pollIntervalExtended", initiallyEnabled ? 61 : 0));
        Configuration config = new Configuration(subject.getThing().getConfiguration().getProperties());
        config.put("pollIntervalExtended", initiallyEnabled ? 0 : 61);
        clearInvocations(commandScheduler);

        subject.thingUpdated(ThingBuilder.create(subject.getThing()).withConfiguration(config).build());
        subject.createEndpointHandlers(robonectClientMock);

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

    /**
     * Checks that normal polling resumes after either a request failure or malformed data.
     *
     * @param malformedJson whether to simulate malformed JSON rather than a communication failure
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldRecoverNormalPollingAfterCommunicationOrJsonFailure(boolean malformedJson) {
        List<Runnable> pollers = initializePolling(Map.of("pollIntervalExtended", 61));
        RuntimeException failure = malformedJson ? new JsonSyntaxException("Malformed response")
                : new RobonectCommunicationException("Unavailable");
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenThrow(failure)
                .thenReturn(createSuccessfulMowerInfoResponse());
        pollers.getFirst().run();
        pollers.getLast().run();
        pollers.getFirst().run();
        verify(mowerApiMock, times(2)).getMowerInfo(anyBoolean());
        verify(diagnosticsApiMock).getBatteryInfo();
        verify(callbackMock, atLeastOnce()).statusUpdated(eq(subject.getThing()),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        if (!malformedJson) {
            verify(callbackMock).statusUpdated(eq(subject.getThing()),
                    argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
            verify(diagnosticsApiMock, times(2)).getVersionInfo();
        }
    }

    /** Reinitializes during the battery read to check that stale detailed work neither publishes nor continues. */
    @Test
    public void shouldStopDetailedPollingAndDiscardResponseAfterReinitialization() {
        List<Runnable> pollers = initializePolling(Map.of("pollIntervalExtended", 61));
        BatteryInfo result = ModelParser.parse("""
                {"successful":true,"batteries":[{"charge":65}]}
                """, BatteryInfo.class);
        when(diagnosticsApiMock.getBatteryInfo()).thenAnswer(invocation -> {
            subject.initialize();
            subject.createEndpointHandlers(robonectClientMock);
            clearInvocations(callbackMock);
            return result;
        });
        pollers.getLast().run();
        verify(diagnosticsApiMock).getBatteryInfo();
        verifyNoMoreInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    /**
     * Holds the first poll in a status read while another poll attempts to acquire the same lock.
     *
     * @param action whether the waiting poll should proceed, be disposed, or be interrupted
     */
    @ParameterizedTest
    @ValueSource(strings = { "serialize", "dispose", "interrupt" })
    @Timeout(30)
    public void shouldSerializePollersAndCancelWaitingPoller(String action) throws Exception {
        boolean dispose = "dispose".equals(action);
        boolean interrupt = "interrupt".equals(action);
        List<Runnable> pollers = initializePolling(Map.of("pollIntervalExtended", 61));
        CountDownLatch statusEntered = new CountDownLatch(1);
        CountDownLatch releaseStatus = new CountDownLatch(1);
        AtomicReference<Thread> detailedThread = new AtomicReference<>();
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenAnswer(invocation -> {
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
                order.verify(mowerApiMock).getMowerInfo(anyBoolean());
                order.verify(diagnosticsApiMock).getWlanInfo();
                order.verify(diagnosticsApiMock).getBatteryInfo();
            }
        } finally {
            releaseStatus.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /**
     * Initializes a Thing with mocked clients and captures the polling tasks for manual execution.
     *
     * @param parameters the Thing configuration overrides
     * @return the scheduled polling tasks in registration order
     */
    private List<Runnable> initializePolling(Map<String, Object> parameters) {
        Map<String, Object> config = new HashMap<>(parameters);
        config.put("host", "localhost");
        config.put("pollInterval", 7);
        config.put("offlineTimeout", 30);
        config.put("timezone", TIME_ZONE.getId());
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3"))
                .withConfiguration(new Configuration(config)).build();
        subject = createHandler(thing, httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        doReturn(normalPollingJob, detailedPollingJob).when(commandScheduler)
                .scheduleWithFixedDelay(any(Runnable.class), eq(0L), anyLong(), eq(TimeUnit.SECONDS));
        subject.initialize();
        subject.createEndpointHandlers(robonectClientMock);
        when(diagnosticsApiMock.getVersionInfo()).thenReturn(new VersionInfo());
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(createSuccessfulMowerInfoResponse());

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

    /** Uses multiple reported sensors and batteries to exercise dynamic channel creation and state publication. */
    @Test
    public void shouldCreateChannelsForEveryWireSensorAndBattery() {
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3")).build();
        subject = createHandler(thing, httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);

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
        verify(callbackMock).stateUpdated(new ChannelUID(thing.getUID(), "wire-sensor-3-g2"), UnDefType.UNDEF);

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
        verify(callbackMock).stateUpdated(new ChannelUID(thing.getUID(), "wire-sensor-1-g2"), UnDefType.UNDEF);
        assertNotNull(updated.getValue().getChannel("battery-2-charge"));

        when(diagnosticsApiMock.getBatteryInfo()).thenReturn(
                ModelParser.parse("{\"successful\":true}", BatteryInfo.class),
                ModelParser.parse("{\"successful\":false,\"batteries\":[]}", BatteryInfo.class));
        ChannelUID batteryCharge = new ChannelUID(thing.getUID(), "battery-1-charge");
        subject.handleCommand(batteryCharge, RefreshType.REFRESH);
        subject.handleCommand(batteryCharge, RefreshType.REFRESH);
        assertNotNull(subject.getThing().getChannel("battery-2-charge"));
        verify(callbackMock, never()).stateUpdated(batteryCharge, UnDefType.UNDEF);

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

    /** Checks that changing the enter setting retains the reported leave setting. */
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

    /** Checks that changing the leave setting retains the reported enter setting. */
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

    /** Exercises the firmware's name length limit before sending the remote start update. */
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

    /** Checks that a distance above the supported range does not reach the mower API. */
    @Test
    public void shouldIgnoreOutOfRangeRemoteStartDistance() {
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "remote-2-distance"),
                new QuantityType<>(301, org.openhab.core.library.unit.SIUnits.METRE));

        verify(remoteStartApiMock, never()).updateRemoteStart(anyInt(), any(), any(), any());
    }

    /** Repeats the same refresh to verify that the updated labels do not trigger another Thing update. */
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
        subject = createHandler(thing, httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
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

    /** Checks that higher numbered trigger entries create channels and publish their settings. */
    @Test
    public void shouldCreatePushTriggerChannelsBeyondTen() {
        ThingUID uid = new ThingUID("1:2:3");
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid)
                .withChannels(ChannelBuilder.create(new ChannelUID(uid, "push-trigger-0-name"), "String").build(),
                        ChannelBuilder.create(new ChannelUID(uid, "push-trigger-12-enter"), "Switch").build())
                .build();
        subject = createHandler(thing, httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
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

    /** Checks trigger identity when firmware reports duplicate display names and invalid names. */
    @Test
    public void shouldKeepDuplicatePushTriggerNamesDistinctAndIgnoreInvalidNames() {
        ThingUID uid = new ThingUID("1:2:3");
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build();
        subject = createHandler(thing, httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
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

    /** Checks conversion of the reported mowing history and its aggregate durations into channel states. */
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

    /** Checks that the status response publishes both the home flag and the prevention code. */
    @Test
    public void shouldPublishHomeAndPreventionCode() {
        MowerInfo info = ModelParser.parse("""
                {"successful":true,"name":"Mower","status":{"status":2,"mode":0,
                "stopped":false,"battery":50,"hours":1,"duration":2,"distance":0,
                "home":true,"prevent":3}}
                """, MowerInfo.class);
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(info);
        ThingUID uid = new ThingUID("1:2:3");
        subject.handleCommand(new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS_HOME), RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS_HOME),
                OnOffType.ON);
        verify(callbackMock).stateUpdated(new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS_PREVENT),
                new DecimalType(3));
    }

    /** Checks the timer timestamp and its representation in the configured time zone. */
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
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(mowerInfo);

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

    /** Distinguishes omitted status fields from valid zero and false values. */
    @Test
    public void shouldClearMissingMowerFieldsAndPreserveReportedZeroAndFalse() {
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(ModelParser.parse("""
                {"successful":true,"status":{"battery":0,"home":false},"health":{},"wlan":{}}
                """, MowerInfo.class), ModelParser.parse("{\"successful\":true}", MowerInfo.class));
        ThingUID uid = new ThingUID("1:2:3");
        ChannelUID status = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS);
        ChannelUID battery = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS_BATTERY);
        ChannelUID home = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_STATUS_HOME);
        subject.handleCommand(status, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(battery, DecimalType.ZERO);
        verify(callbackMock).stateUpdated(home, OnOffType.OFF);
        verify(callbackMock).stateUpdated(new ChannelUID(uid, RobonectBindingConstants.CHANNEL_HEALTH_TEMP),
                UnDefType.UNDEF);
        verify(callbackMock).stateUpdated(new ChannelUID(uid, RobonectBindingConstants.CHANNEL_WLAN_SIGNAL_STRENGTH),
                UnDefType.UNDEF);

        subject.handleCommand(status, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(battery, UnDefType.UNDEF);
        verify(callbackMock).stateUpdated(home, UnDefType.UNDEF);
    }

    /** Checks current error state and the follow-up lookup for error details. */
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
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(mowerInfo);
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

    /** Checks that status without a current error clears the earlier error information. */
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
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(mowerInfo);

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

    /** Checks the numeric and quantity states produced from a status response. */
    @Test
    public void shouldUpdateNumericStateOnMowerStatusRefresh() throws InterruptedException {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        mowerInfo.getStatus().setStatus(MowerStatus.MOWING);

        // when
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(mowerInfo);

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

    /** Checks that Ajax job times are published as time-of-day values in the mower's configured zone. */
    @Test
    public void shouldUseAjaxUiInfoForJobTimes() {
        ArgumentCaptor<State> jobStartCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> jobEndCaptor = ArgumentCaptor.forClass(State.class);
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        Job job = new Job();
        job.setJobActiv(true);
        job.setJobStart("10:00");
        job.setJobEnd("12:00");
        mowerInfo.setJob(job);

        subject.setUseAjaxApi(true);
        when(mowerApiMock.getMowerInfo(true)).thenReturn(mowerInfo);

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

    /**
     * Checks dated start and end estimates against the mower's observed scheduling cases.
     *
     * @param start the optional local start in HH:mm format
     * @param end the optional local end in HH:mm format
     * @param duration the optional duration in minutes
     * @param expectedStart the expected local start date and time
     * @param expectedEnd the expected local end date and time
     */
    @ParameterizedTest(name = "[{index}] start={0}, end={1}, duration={2}")
    @CsvSource(textBlock = """
            ,      ,      , 2026-11-08T10:00, 2026-11-08T11:00
            ,      , 25   , 2026-11-08T10:00, 2026-11-08T10:25
            , 09:00,      , 2026-11-08T10:00, 2026-11-08T09:00
            , 09:00, 25   , 2026-11-08T08:35, 2026-11-08T09:00
            , 11:00,      , 2026-11-08T10:00, 2026-11-08T11:00
            , 11:00, 25   , 2026-11-08T10:35, 2026-11-08T11:00
            11:00,      ,      , 2026-11-08T11:00, 2026-11-08T12:00
            11:00,      , 25   , 2026-11-08T11:00, 2026-11-08T11:25
            09:00,      ,      , 2026-11-09T09:00, 2026-11-09T10:00
            09:00,      , 25   , 2026-11-09T09:00, 2026-11-09T09:25
            11:00, 12:00, 25   , 2026-11-08T11:00, 2026-11-08T12:00
            12:00, 11:00, 25   , 2026-11-08T12:00, 2026-11-09T11:00
            11:00, 11:00, 25   , 2026-11-08T11:00, 2026-11-09T11:00
            11:00, 09:00, 25   , 2026-11-08T11:00, 2026-11-09T09:00
            09:00, 11:00, 25   , 2026-11-08T09:00, 2026-11-08T11:00
            08:00, 09:00, 25   , 2026-11-09T08:00, 2026-11-09T09:00
            09:00, 08:00, 25   , 2026-11-08T09:00, 2026-11-09T08:00
            09:00, 09:00, 25   , 2026-11-09T09:00, 2026-11-10T09:00
            """)
    public void shouldEstimateJobWindowLikeTheMower(String start, String end, Integer duration, String expectedStart,
            String expectedEnd) {
        ZonedDateTime now = ZonedDateTime.of(2026, 11, 8, 10, 0, 0, 0, TIME_ZONE);
        var settings = new RobonectClient.JobSettings().withStart(start).withEnd(end).withDuration(duration);

        var job = RobonectMowerHandler.estimateJob(null, settings, now);

        assertEquals(LocalDateTime.parse(expectedStart).atZone(TIME_ZONE), job.start());
        assertEquals(LocalDateTime.parse(expectedEnd).atZone(TIME_ZONE), job.end());
    }

    /** Checks that an expired local job clears its channels after the status read recovers. */
    @Test
    public void shouldExpireLocalJobOnNextSuccessfulStatusRead() {
        ThingUID uid = new ThingUID("1:2:3");
        ChannelUID customJob = new ChannelUID(uid, "timer-custom-job");
        ChannelUID job = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB);
        ChannelUID start = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB_START);
        ChannelUID end = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB_END);
        LocalTime now = LocalTime.now(TIME_ZONE);
        LocalTime expiredEnd = now.minusMinutes(5);
        if (expiredEnd.isAfter(now)) {
            expiredEnd = LocalTime.MIDNIGHT;
        }
        String endTime = expiredEnd.format(DateTimeFormatter.ofPattern("HH:mm"));
        Channel channel = ChannelBuilder.create(customJob, "Switch").withType(new ChannelTypeUID("robonect", "jobType"))
                .withConfiguration(new Configuration(Map.of("durationMinutes", 1, "endTime", endTime))).build();
        when(robonectThingMock.getChannel(customJob.getId())).thenReturn(channel);
        when(robonectThingMock.getChannels()).thenReturn(List.of(channel));
        when(mowerApiMock.startJob(any())).thenReturn(ModelParser.parse("{\"successful\":true}", RobonectAnswer.class));
        when(mowerApiMock.getMowerInfo(false)).thenThrow(new RobonectCommunicationException("Unavailable"))
                .thenReturn(createSuccessfulMowerInfoResponse());

        subject.handleCommand(customJob, OnOffType.ON);
        verify(mowerApiMock, never()).getMowerInfo(false);
        runScheduledRefresh(5);
        verify(mowerApiMock, never()).clearTrackedJob();
        verify(callbackMock, never()).stateUpdated(any(), any());

        subject.handleCommand(job, RefreshType.REFRESH);

        var order = inOrder(callbackMock, mowerApiMock);
        order.verify(mowerApiMock, times(2)).getMowerInfo(false);
        order.verify(mowerApiMock).clearTrackedJob();
        order.verify(callbackMock).stateUpdated(job, OnOffType.OFF);
        order.verify(callbackMock).stateUpdated(start, UnDefType.UNDEF);
        order.verify(callbackMock).stateUpdated(end, UnDefType.UNDEF);
        order.verify(callbackMock).stateUpdated(customJob, OnOffType.OFF);
        verify(callbackMock, never()).stateUpdated(job, OnOffType.ON);
        verify(callbackMock, never()).stateUpdated(customJob, OnOffType.ON);
    }

    /** Checks that confirmed Ajax polls repeat the job states and apply custom switch changes in order. */
    @Test
    public void shouldSwitchCustomAjaxJobsOnlyFromConfirmedPolls() {
        ThingUID uid = new ThingUID("1:2:3");
        ChannelUID first = new ChannelUID(uid, "first-job");
        ChannelUID second = new ChannelUID(uid, "second-job");
        ChannelUID job = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB);
        ChannelUID start = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB_START);
        ChannelUID end = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB_END);
        ChannelTypeUID jobType = new ChannelTypeUID("robonect", "jobType");
        Channel firstChannel = ChannelBuilder.create(first, "Switch").withType(jobType).build();
        Channel secondChannel = ChannelBuilder.create(second, "Switch").withType(jobType).build();
        when(robonectThingMock.getChannels()).thenReturn(List.of(firstChannel, secondChannel));
        when(robonectThingMock.getChannel(first.getId())).thenReturn(firstChannel);
        when(robonectThingMock.getChannel(second.getId())).thenReturn(secondChannel);
        when(mowerApiMock.startJob(any())).thenReturn(ModelParser.parse("{\"successful\":true}", RobonectAnswer.class));
        when(mowerApiMock.stopJob(null)).thenReturn(ModelParser.parse("{\"successful\":true}", RobonectAnswer.class));
        MowerInfo active = createSuccessfulMowerInfoResponse();
        Job activeJob = new Job();
        activeJob.setJobActiv(true);
        activeJob.setJobStart("10:00");
        activeJob.setJobEnd("10:30");
        active.setJob(activeJob);
        MowerInfo inactive = createSuccessfulMowerInfoResponse();
        Job inactiveJob = new Job();
        inactiveJob.setJobActiv(false);
        inactive.setJob(inactiveJob);
        when(mowerApiMock.getMowerInfo(true)).thenReturn(active, active, active, createSuccessfulMowerInfoResponse(),
                active, active, inactive);
        subject.setUseAjaxApi(true);

        subject.handleCommand(first, OnOffType.ON);
        verify(mowerApiMock, never()).getMowerInfo(true);
        runScheduledRefresh(5);
        subject.handleCommand(job, RefreshType.REFRESH);
        verify(callbackMock, times(2)).stateUpdated(job, OnOffType.ON);
        verify(callbackMock, times(2)).stateUpdated(eq(start), any(DateTimeType.class));
        verify(callbackMock, times(2)).stateUpdated(eq(end), any(DateTimeType.class));
        verify(callbackMock, times(2)).stateUpdated(first, OnOffType.ON);
        verify(callbackMock, times(2)).stateUpdated(second, OnOffType.OFF);
        verify(callbackMock, never()).stateUpdated(first, OnOffType.OFF);
        verify(callbackMock, never()).stateUpdated(second, OnOffType.ON);

        clearInvocations(commandScheduler);
        subject.handleCommand(second, OnOffType.ON);
        runScheduledRefresh(5);
        var order = inOrder(callbackMock);
        order.verify(callbackMock).stateUpdated(first, OnOffType.OFF);
        order.verify(callbackMock).stateUpdated(second, OnOffType.ON);

        clearInvocations(callbackMock);
        subject.handleCommand(job, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(job, OnOffType.OFF);
        verify(callbackMock).stateUpdated(start, UnDefType.UNDEF);
        verify(callbackMock).stateUpdated(end, UnDefType.UNDEF);
        verify(callbackMock).stateUpdated(first, OnOffType.OFF);
        verify(callbackMock).stateUpdated(second, OnOffType.OFF);

        clearInvocations(callbackMock);
        clearInvocations(commandScheduler);
        subject.handleCommand(second, OnOffType.ON);
        runScheduledRefresh(5);
        clearInvocations(commandScheduler);
        subject.handleCommand(second, OnOffType.OFF);
        verify(mowerApiMock, times(5)).getMowerInfo(true);
        runScheduledRefresh(5);
        verify(callbackMock, never()).stateUpdated(job, OnOffType.OFF);
        verify(callbackMock, times(2)).stateUpdated(second, OnOffType.ON);
        subject.handleCommand(job, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(job, OnOffType.OFF);
        verify(callbackMock).stateUpdated(second, OnOffType.OFF);
    }

    /** Holds an inactive Ajax response in flight while a custom start command arrives. */
    @Test
    @Timeout(30)
    public void shouldApplyInFlightAjaxStatusBeforeStartingNewJob() throws Exception {
        ThingUID uid = new ThingUID("1:2:3");
        ChannelUID job = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB);
        ChannelUID customJob = new ChannelUID(uid, "custom-job");
        Channel channel = ChannelBuilder.create(customJob, "Switch").withType(new ChannelTypeUID("robonect", "jobType"))
                .build();
        when(robonectThingMock.getChannels()).thenReturn(List.of(channel));
        when(robonectThingMock.getChannel(customJob.getId())).thenReturn(channel);
        when(mowerApiMock.startJob(any())).thenReturn(ModelParser.parse("{\"successful\":true}", RobonectAnswer.class));
        MowerInfo active = createSuccessfulMowerInfoResponse();
        Job activeJob = new Job();
        activeJob.setJobActiv(true);
        active.setJob(activeJob);
        CountDownLatch statusEntered = new CountDownLatch(1);
        CountDownLatch releaseStatus = new CountDownLatch(1);
        when(mowerApiMock.getMowerInfo(true)).thenAnswer(invocation -> {
            statusEntered.countDown();
            assertTrue(releaseStatus.await(10, TimeUnit.SECONDS));
            return createSuccessfulMowerInfoResponse();
        }).thenReturn(active);
        subject.setUseAjaxApi(true);

        AtomicReference<Thread> commandThread = new AtomicReference<>();
        var executor = Executors.newFixedThreadPool(2, Thread.ofPlatform().daemon().factory());
        try {
            Future<?> refresh = executor.submit(() -> subject.handleCommand(job, RefreshType.REFRESH));
            assertTrue(statusEntered.await(10, TimeUnit.SECONDS));
            Future<?> start = executor.submit(() -> {
                commandThread.set(Thread.currentThread());
                subject.handleCommand(customJob, OnOffType.ON);
            });
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                while (!start.isDone()) {
                    Thread thread = commandThread.get();
                    if (thread != null && thread.getState() == Thread.State.BLOCKED) {
                        StackTraceElement[] stack = thread.getStackTrace();
                        if (stack.length > 0 && stack[0].getClassName().equals(RobonectMowerHandler.class.getName())
                                && stack[0].getMethodName().equals("handleJobCommand")) {
                            break;
                        }
                    }
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
                }
            });
            verify(mowerApiMock, never()).startJob(any());
            releaseStatus.countDown();
            refresh.get(10, TimeUnit.SECONDS);
            start.get(10, TimeUnit.SECONDS);
            verify(mowerApiMock).getMowerInfo(true);
            runScheduledRefresh(5);

            var order = inOrder(callbackMock, mowerApiMock);
            order.verify(callbackMock).stateUpdated(customJob, OnOffType.OFF);
            order.verify(mowerApiMock).startJob(any());
            order.verify(callbackMock).stateUpdated(job, OnOffType.ON);
            order.verify(callbackMock).stateUpdated(customJob, OnOffType.ON);
            verify(callbackMock).stateUpdated(customJob, OnOffType.OFF);
        } finally {
            releaseStatus.countDown();
            executor.shutdownNow();
        }
    }

    /** Checks that Ajax status distinguishes end-of-day mode from the API's HOME mode. */
    @Test
    public void shouldUseAjaxUiInfoForEndOfDay() {
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        Objects.requireNonNull(mowerInfo.getStatus()).setMode(MowerMode.EOD);

        subject.setUseAjaxApi(true);
        when(mowerApiMock.getMowerInfo(true)).thenReturn(mowerInfo);

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                RefreshType.REFRESH);

        verify(callbackMock).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_MODE)),
                eq(new StringType("EOD")));
    }

    /**
     * Checks which channel settings are forwarded to the stop operation.
     *
     * @param afterMode the configured after-mode text, or {@code null} if omitted
     * @param expectedMode the supported mode forwarded to the API, or {@code null} for its fallback
     */
    @ParameterizedTest
    @CsvSource({ ",", "invalid,", "JOB,", "AUTO, AUTO", "EOD, EOD", "MANUAL, MANUAL" })
    public void shouldPassOnlySupportedAfterModesWhenStoppingJob(String afterMode, ModeCommand.Mode expectedMode) {
        ThingUID uid = new ThingUID("robonect:mower:test");
        ChannelUID jobChannel = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB);
        Configuration jobConfig = new Configuration(afterMode == null ? Map.of() : Map.of("afterMode", afterMode));
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid)
                .withChannel(ChannelBuilder.create(jobChannel, "Switch")
                        .withType(new ChannelTypeUID("robonect", "jobType")).withConfiguration(jobConfig).build())
                .build();
        subject = createHandler(thing, httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
        when(mowerApiMock.getMowerInfo(false)).thenReturn(createSuccessfulMowerInfoResponse());

        when(mowerApiMock.stopJob(expectedMode)).thenReturn(new RobonectAnswer());
        subject.handleCommand(jobChannel, OnOffType.OFF);

        verify(mowerApiMock).stopJob(expectedMode);
    }

    /**
     * Checks that optional job time text is validated before sending a start command.
     *
     * @param start the configured local start text, or {@code null} if omitted
     * @param valid whether the input should reach the mower API
     */
    @ParameterizedTest
    @CsvSource({ ", true", "09:00, true", "540, false" })
    public void shouldStartJobWithOptionalTextTime(String start, boolean valid) {
        ThingUID uid = new ThingUID("robonect:mower:test");
        ChannelUID jobChannel = new ChannelUID(uid, RobonectBindingConstants.CHANNEL_JOB);
        var config = new Configuration(start == null ? Map.of("durationMinutes", 60)
                : Map.of("durationMinutes", 60, "startTime", start, "endTime", "10:00"));
        Thing thing = ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid)
                .withChannel(ChannelBuilder.create(jobChannel, "Switch")
                        .withType(new ChannelTypeUID("robonect", "jobType")).withConfiguration(config).build())
                .build();
        subject = createHandler(thing, httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
        when(mowerApiMock.getMowerInfo(false)).thenReturn(createSuccessfulMowerInfoResponse());

        if (valid) {
            when(mowerApiMock.startJob(any())).thenReturn(new RobonectAnswer());
        }
        subject.handleCommand(jobChannel, OnOffType.ON);

        if (!valid) {
            verifyNoInteractions(mowerApiMock);
            return;
        }
        var settings = ArgumentCaptor.forClass(RobonectClient.JobSettings.class);
        verify(mowerApiMock).startJob(settings.capture());
        assertEquals(60, settings.getValue().getDuration());
        assertEquals(ModeCommand.RemoteStart.STANDARD, settings.getValue().getRemoteStart());
        assertNull(settings.getValue().getAfterMode());
    }

    /** Checks the Thing Action result and API settings with both omitted and explicit inputs. */
    @Test
    public void shouldStartJobsFromTheThingActionWithOptionalInputs() {
        initializePolling(Map.of());
        assertTrue(subject.getServices().contains(RobonectActions.class));
        when(mowerApiMock.startJob(any())).thenReturn(ModelParser.parse("{\"successful\":true}", RobonectAnswer.class));
        RobonectActions actions = new RobonectActions();
        actions.setThingHandler(subject);

        assertTrue(actions.startJob(null, null, null, null, null));
        assertTrue(actions.startJob(60, "09:00", "10:00", "REMOTE_1", "EOD"));

        var settings = ArgumentCaptor.forClass(RobonectClient.JobSettings.class);
        verify(mowerApiMock, times(2)).startJob(settings.capture());
        RobonectClient.JobSettings defaults = settings.getAllValues().getFirst();
        assertNull(defaults.getDuration());
        assertEquals(ModeCommand.RemoteStart.STANDARD, defaults.getRemoteStart());
        assertNull(defaults.getAfterMode());
        RobonectClient.JobSettings supplied = settings.getValue();
        assertEquals(60, supplied.getDuration());
        assertEquals(ModeCommand.RemoteStart.REMOTE_1, supplied.getRemoteStart());
        assertEquals(ModeCommand.Mode.EOD, supplied.getAfterMode());
    }

    /** Checks that invalid duration, times, remote start, and after-mode are rejected before the API call. */
    @Test
    public void shouldRejectInvalidThingActionInputsBeforeStartingJob() {
        initializePolling(Map.of());
        RobonectActions actions = new RobonectActions();
        actions.setThingHandler(subject);

        assertThrows(IllegalArgumentException.class, () -> actions.startJob(1440, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> actions.startJob(null, "24:00", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> actions.startJob(null, null, "12:60", null, null));
        assertThrows(IllegalArgumentException.class, () -> actions.startJob(null, null, null, "REMOTE_6", null));
        assertThrows(IllegalArgumentException.class, () -> actions.startJob(null, null, null, null, "JOB"));
        verifyNoInteractions(mowerApiMock);
    }

    /** Checks that only ON sends a custom channel's configured wheel command. */
    @Test
    public void shouldDriveWheelsFromCustomChannel() {
        ChannelUID uid = new ChannelUID(subject.getThing().getUID(), "direct#test");
        Configuration config = new Configuration(Map.of("left", new BigDecimal("-20"), "right", 20, "timeout", 500));
        Channel channel = ChannelBuilder.create(uid, "Switch").withType(new ChannelTypeUID("robonect", "directType"))
                .withConfiguration(config).build();
        when(robonectThingMock.getChannel(uid.getId())).thenReturn(channel);
        DirectCommand direct = new DirectCommand(-20, 20, 500);
        when(mowerApiMock.drive(direct)).thenReturn(ModelParser.parse("{\"successful\":true}", RobonectAnswer.class));

        subject.handleCommand(uid, OnOffType.OFF);
        subject.handleCommand(uid, RefreshType.REFRESH);
        verifyNoInteractions(mowerApiMock);
        subject.handleCommand(uid, OnOffType.ON);
        verify(mowerApiMock).drive(direct);
    }

    /**
     * Checks that a missing, fractional, or out-of-range channel setting prevents a drive command.
     *
     * @param key the setting to replace or remove
     * @param value the invalid setting, or {@code null} to omit it
     */
    @ParameterizedTest
    @CsvSource({ "left,", "right,", "timeout,", "left,1.5", "right,1001", "timeout,5001" })
    public void shouldRejectInvalidDirectChannelSettings(String key, BigDecimal value) {
        ChannelUID uid = new ChannelUID(subject.getThing().getUID(), "direct#test");
        Map<String, Object> settings = new HashMap<>(Map.of("left", -20, "right", 20, "timeout", 500));
        if (value == null) {
            settings.remove(key);
        } else {
            settings.put(key, value);
        }
        Channel channel = ChannelBuilder.create(uid, "Switch").withType(new ChannelTypeUID("robonect", "directType"))
                .withConfiguration(new Configuration(settings)).build();
        when(robonectThingMock.getChannel(uid.getId())).thenReturn(channel);

        subject.handleCommand(uid, OnOffType.ON);
        verifyNoInteractions(mowerApiMock);
    }

    /** Checks acceptance and rejection of wheel commands issued through the Thing Action. */
    @Test
    public void shouldReportDirectThingActionResult() {
        initializePolling(Map.of());
        RobonectActions actions = new RobonectActions();
        actions.setThingHandler(subject);
        DirectCommand direct = new DirectCommand(-100, 100, 1);
        when(mowerApiMock.drive(direct)).thenReturn(ModelParser.parse("{\"successful\":true}", RobonectAnswer.class),
                ModelParser.parse("{\"successful\":false}", RobonectAnswer.class));

        assertTrue(actions.drive(-100, 100, 1));
        assertThrows(IllegalStateException.class, () -> actions.drive(-100, 100, 1));
        verify(mowerApiMock, times(2)).drive(direct);
    }

    /**
     * Checks that invalid Thing Action inputs are rejected before a request is sent.
     *
     * @param left the left wheel value
     * @param right the right wheel value
     * @param timeout the duration in milliseconds
     */
    @ParameterizedTest
    @CsvSource({ ",20,500", "20,,500", "20,20,", "-101,20,500", "20,101,500", "20,20,0", "20,20,5001" })
    public void shouldRejectInvalidDirectThingActionInputs(Integer left, Integer right, Integer timeout) {
        RobonectActions actions = new RobonectActions();
        actions.setThingHandler(subject);

        assertThrows(IllegalArgumentException.class, () -> actions.drive(left, right, timeout));
        verifyNoInteractions(mowerApiMock);
    }

    /** Verifies that the reset switch sends the error-reset operation on an ON command. */
    @Test
    public void shouldResetErrorsOnOnCommand() {
        RobonectAnswer response = ModelParser.parse("{\"successful\":true}", RobonectAnswer.class);

        when(mowerApiMock.resetErrors()).thenReturn(response);
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(createSuccessfulMowerInfoResponse());

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_RESET_ERROR),
                OnOffType.ON);

        verify(mowerApiMock).resetErrors();
        verify(callbackMock, never()).stateUpdated(any(), any());
        verify(mowerApiMock, never()).getMowerInfo(anyBoolean());
        runScheduledRefresh(5);
        verify(mowerApiMock).getMowerInfo(anyBoolean());
    }

    /** Checks that a start command reads status immediately rather than waiting for the delayed refresh. */
    @Test
    public void shouldReadStatusImmediatelyAfterStartCommand() {
        RobonectAnswer response = new RobonectAnswer();
        response.setSuccessful(true);
        when(mowerApiMock.start()).thenReturn(response);
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(createSuccessfulMowerInfoResponse());

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_MOWER_START),
                OnOffType.ON);

        verify(mowerApiMock).start();
        var order = inOrder(mowerApiMock);
        order.verify(mowerApiMock).start();
        order.verify(mowerApiMock).getMowerInfo(anyBoolean());
        verifyNoInteractions(commandScheduler);
    }

    /** Checks the channel states published from a successful mower status response. */
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
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(mowerInfo);
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

    /** Checks that a polling interval in the normal weather response avoids an unofficial web request. */
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

    /** Checks the web-interface fallback for an omitted weather interval under both API settings. */
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

    /** Exercises an unavailable interval fallback followed by a successful retry without dropping weather data. */
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
        verify(callbackMock, times(2)).stateUpdated(channel, UnDefType.UNDEF);
        subject.handleCommand(channel, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(channel, new QuantityType<>(10, Units.MINUTE));
        verify(callbackMock, times(3)).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-rain"),
                new QuantityType<>(20, Units.PERCENT));
    }

    /** Checks that a rejected weather response does not trigger the interval fallback. */
    @Test
    public void shouldNotReadWeatherIntervalForFailedApiResponse() {
        subject.configureUnofficialChannels(true);
        when(weatherApiMock.getWeatherInfo())
                .thenReturn(ModelParser.parse("{\"successful\":false}", WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-service-interval"), RefreshType.REFRESH);
        verify(weatherApiMock, never()).getWeatherIntervalViaWebInterface();
    }

    /** Checks publication of weather data and conversion of its timestamps into date-time states. */
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
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-location-zip"),
                UnDefType.UNDEF);
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "weather-location-country"),
                UnDefType.UNDEF);
    }

    /** Checks immediate publication of the location returned by a city ID write. */
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
        runScheduledRefresh(5);
        verify(weatherApiMock).getWeatherInfo();
    }

    /**
     * Uses a ZIP code with a leading zero to check that the write preserves its text and publishes the returned
     * country.
     */
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
        runScheduledRefresh(5);
        verify(weatherApiMock).getWeatherInfo();
    }

    /**
     * Checks that invalid city IDs do not reach the weather API.
     *
     * @param value the invalid city ID supplied to the channel
     */
    @ParameterizedTest
    @ValueSource(strings = { "-1", "1.5", "2147483648" })
    public void shouldRejectInvalidWeatherCityIds(String value) {
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-location-cityid"), new DecimalType(value));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, commandScheduler);
    }

    /** Checks that the weather channel rejects a quantity of the wrong dimension. */
    @Test
    public void shouldRejectWeatherCommandWithIncompatibleUnit() {
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-config-min-temperature"),
                new QuantityType<>(1, org.openhab.core.library.unit.SIUnits.METRE));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, commandScheduler);
    }

    /** Checks that commands for read-only weather data do not call a write endpoint. */
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

    /** Checks that a full weather response clears omitted fields and timestamps without a Unix value. */
    @Test
    public void shouldClearMissingWeatherFieldsAndDateTimeWithoutUnix() {
        when(weatherApiMock.getWeatherInfo()).thenReturn(ModelParser.parse("""
                {"successful":true,"service":{"location":{}},"weather":{"condition":{},
                "timestamp":{"date":"2026-10-20","time":"22:44:42"}}}
                """, WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-timestamp"), RefreshType.REFRESH);
        for (String channel : List.of("weather-timestamp", "weather-location-zip", "weather-condition-day",
                "weather-config-max-rain")) {
            verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), channel), UnDefType.UNDEF);
        }
    }

    /**
     * Checks that timestamp parsing failure is isolated to the affected channel.
     *
     * @param unix the invalid Unix timestamp supplied in the weather response
     */
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

    /** Checks that a weather write publishes supplied fields immediately and leaves absent fields alone. */
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
        runScheduledRefresh(5);
        verify(weatherApiMock).getWeatherInfo();
    }

    /** Checks that remote start states from a write response are published before the scheduled read. */
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
        runScheduledRefresh(5);
        verify(remoteStartApiMock).getRemoteInfo();
    }

    /** Checks that a timer write publishes the response immediately while retaining its follow-up read. */
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
        runScheduledRefresh(5);
        verify(timerApiMock).getTimersInfo();
    }

    /** Checks that a successful write without state still schedules a follow-up read. */
    @Test
    public void shouldScheduleReadWhenSuccessfulResponseHasNoState() {
        when(weatherApiMock.updateWeather("service", "1"))
                .thenReturn(ModelParser.parse("{\"successful\":true}", WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-service-enabled"), OnOffType.ON);
        verify(callbackMock, never()).stateUpdated(any(), any());
        verify(weatherApiMock, never()).getWeatherInfo();
        verify(commandScheduler).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    }

    /** Checks that a door write response updates the door channels before the scheduled read. */
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
        runScheduledRefresh(5);
        verify(mowerApiMock).getDoorInfo();
    }

    /** Checks that a name change publishes the returned name before the scheduled status read. */
    @Test
    public void shouldApplyNameResponseBeforeDelayedStatusRead() {
        when(mowerApiMock.setName("Garden mower"))
                .thenReturn(ModelParser.parse("{\"successful\":true,\"name\":\"Garden mower\"}", Name.class));
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(createSuccessfulMowerInfoResponse());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "name"), new StringType("Garden mower"));
        verify(callbackMock).stateUpdated(new ChannelUID(new ThingUID("1:2:3"), "name"),
                new StringType("Garden mower"));
        verify(mowerApiMock, never()).getMowerInfo(anyBoolean());
        runScheduledRefresh(5);
        verify(mowerApiMock).getMowerInfo(anyBoolean());
    }

    /** Checks that a partial push write response preserves trigger channels outside the response. */
    @Test
    public void shouldApplyPartialPushWriteWithoutRemovingOtherTriggers() {
        ThingUID uid = new ThingUID("1:2:3");
        subject = createHandler(ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build(),
                httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
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
        runScheduledRefresh(5);
        verify(pushApiMock, times(2)).getPushInfo();
    }

    /** Checks that mode changes delay their status read and the mower stop switch reads immediately. */
    @Test
    public void shouldDelayStatusAfterModeWriteAndReadImmediatelyAfterStopWrite() {
        when(mowerApiMock.setMode(ModeCommand.Mode.HOME)).thenReturn(new RobonectAnswer());
        when(mowerApiMock.stop()).thenReturn(new RobonectAnswer());
        when(mowerApiMock.getMowerInfo(anyBoolean())).thenReturn(createSuccessfulMowerInfoResponse());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "mode"), new StringType("HOME"));
        verify(mowerApiMock, never()).getMowerInfo(anyBoolean());
        runScheduledRefresh(5);
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "start"), OnOffType.OFF);
        var order = inOrder(mowerApiMock);
        order.verify(mowerApiMock).setMode(ModeCommand.Mode.HOME);
        order.verify(mowerApiMock).getMowerInfo(anyBoolean());
        order.verify(mowerApiMock).stop();
        order.verify(mowerApiMock).getMowerInfo(anyBoolean());
        verifyNoMoreInteractions(commandScheduler);
    }

    /** Exercises successive commands and disposal to check cancellation of stale follow-up reads. */
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

    /** Disposes the handler while a refresh is reading and verifies that the response cannot publish state. */
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
        runScheduledRefresh(5);
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    /** Checks that a failed write produces neither a state update nor a delayed read. */
    @Test
    public void shouldNotScheduleReadAfterRejectedWrite() {
        when(weatherApiMock.updateWeather("service", "1"))
                .thenReturn(ModelParser.parse("{\"successful\":false}", WeatherInfo.class));
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "weather-service-enabled"), OnOffType.ON);
        verifyNoInteractions(commandScheduler);
        verify(callbackMock, never()).stateUpdated(any(), any());
    }

    /** Toggles Ajax scraping to verify that both job time channels remain available without duplicates. */
    @Test
    public void shouldKeepJobTimeChannelsWithOrWithoutAjaxScraping() {
        useRealThingForPushMode();
        subject.configureAjaxChannels(false);
        assertEquals(2, subject.getThing().getChannels().size());
        subject.configureUnofficialChannels(true);
        subject.configureAjaxChannels(true);
        subject.configureAjaxChannels(true);
        assertEquals(3, subject.getThing().getChannels().size());
        assertEquals("DateTime", subject.getThing().getChannel("job-start").getAcceptedItemType());
        assertEquals("DateTime", subject.getThing().getChannel("job-end").getAcceptedItemType());
        subject.configureAjaxChannels(false);
        assertNotNull(subject.getThing().getChannel("job-start"));
        assertNotNull(subject.getThing().getChannel("job-end"));
        assertNotNull(subject.getThing().getChannel("push-mode"));
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock);
    }

    /** Toggles the unofficial API to verify that it controls the lifecycle of the push mode channel. */
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

    /** Exercises both refresh and write with the unofficial API enabled and disabled. */
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

    /** Checks that invalid values are rejected before the push mode endpoint is called. */
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

    /** Exercises an absent push mode followed by a valid response. */
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

    /** Checks that a push mode write without a valid selection leaves state alone and queues no read. */
    @Test
    public void shouldIgnoreMissingPushModeWriteResponseWithoutSchedulingRead() {
        useRealThingForPushMode();
        subject.configureUnofficialChannels(true);
        when(pushApiMock.updatePushModeViaWebInterface(2)).thenReturn(null);
        ChannelUID uid = new ChannelUID(new ThingUID("1:2:3"), "push-mode");
        subject.handleCommand(uid, new DecimalType(2));
        verify(callbackMock, never()).stateUpdated(eq(uid), any());
        verifyNoInteractions(commandScheduler);
        verify(pushApiMock, never()).getPushModeViaWebInterface();
    }

    /** Uses a mutable Thing so dynamic channel changes can be inspected in the tests. */
    private void useRealThingForPushMode() {
        subject = createHandler(
                ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, new ThingUID("1:2:3")).build(),
                httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
    }

    /**
     * Disables the unofficial API during a push mode request to check that its response is discarded.
     *
     * @param write whether to exercise a mode write rather than a read
     */
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

    /**
     * Checks that timer updates preserve the time of day in both directions.
     *
     * @param time the local time in HH:mm format
     */
    @ParameterizedTest
    @ValueSource(strings = { "00:00", "08:15", "23:59" })
    public void shouldPreserveTimerLocalTimeOnReadAndWrite(String time) {
        ThingUID uid = new ThingUID("1:2:3");
        subject = createHandler(ThingBuilder.create(RobonectBindingConstants.THING_TYPE_AUTOMOWER, uid).build(),
                httpClientFactoryMock, timeZoneProvider, pushServletMock);
        subject.setCallback(callbackMock);
        subject.createEndpointHandlers(robonectClientMock);
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
        verify(callbackMock).stateUpdated(new ChannelUID(uid, "timer-1-monday"), UnDefType.UNDEF);
        when(timerApiMock.updateTimer(1, "start", time))
                .thenReturn(ModelParser.parse("{\"successful\":true}", TimerInfo.class));
        subject.handleCommand(start, state);
        verify(timerApiMock).updateTimer(1, "start", time);
    }

    /**
     * Checks conversion under both standard time and daylight saving time.
     *
     * @param month the month of the command date
     */
    @ParameterizedTest
    @ValueSource(ints = { 1, 7 })
    public void shouldConvertTimerCommandsInBindingTimeZoneInWinterAndSummer(int month) {
        when(timerApiMock.updateTimer(1, "start", "08:15"))
                .thenReturn(ModelParser.parse("{\"successful\":true}", TimerInfo.class));
        DateTimeType command = new DateTimeType(ZonedDateTime.of(2026, month, 15, 8, 15, 0, 0, TIME_ZONE).toInstant());
        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), "timer-1-start"), command);
        verify(timerApiMock).updateTimer(1, "start", "08:15");
    }

    /** Checks that a password write still publishes the non-secret settings returned by the mower. */
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
        runScheduledRefresh(5);
        verify(pushApiMock).getPushInfo();
    }

    /** Checks that linking or refreshing the password channel requires no device request and publishes no secret. */
    @Test
    public void shouldKeepPushPasswordUndefinedOnLinkAndRefreshWithoutReadingDevice() {
        ChannelUID password = new ChannelUID(new ThingUID("1:2:3"), "push-password");
        subject.channelLinked(password);
        subject.handleCommand(password, RefreshType.REFRESH);
        verify(callbackMock, times(2)).stateUpdated(password, UnDefType.UNDEF);
        verifyNoInteractions(mowerApiMock, diagnosticsApiMock, pushApiMock, remoteStartApiMock, timerApiMock,
                weatherApiMock, commandScheduler);
    }

    /** Checks that a rejected password write cannot publish the requested password. */
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

    /**
     * Runs the captured delayed refresh immediately so tests need no real timer.
     *
     * @param delaySeconds the expected scheduling delay in seconds
     */
    private void runScheduledRefresh(long delaySeconds) {
        var refresh = ArgumentCaptor.forClass(Runnable.class);
        verify(commandScheduler).schedule(refresh.capture(), eq(delaySeconds), eq(TimeUnit.SECONDS));
        refresh.getValue().run();
    }

    /**
     * Builds the shared status fixture with timer, mower, and WLAN data.
     *
     * @return a successful mower status response
     */
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
