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
package org.openhab.binding.evohome.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.evohome.internal.EvohomeBindingConstants.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient.CachedSchedule;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * Tests retry, publication and disposal through scheduled work.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class EvohomeAccountBridgeHandlerTest {
    private static final String TOKEN = "{\"access_token\":\"test-token\",\"expires_in\":3600}";
    private static final String ACCOUNT = "{\"userId\":\"user\"}";
    private static final String LOCATIONS = "[{\"locationInfo\":{\"locationId\":\"location\"},\"gateways\":[]}]";
    private static final String STATUS = "{\"locationId\":\"location\",\"gateways\":[]}";
    private static final String CONTROLLER_INSTALLATION = """
            [{"locationInfo":{"locationId":"location","timeZone":{"timeZoneId":"Europe/Budapest"}},"gateways":[
              {"gatewayInfo":{"gatewayId":"gateway"},"temperatureControlSystems":[
                {"systemId":"controller","zones":[{"zoneId":"zone"}],"allowedSystemModes":[
                  {"systemMode":"Auto","canBePermanent":true},
                  {"systemMode":"AutoWithReset","canBePermanent":true},
                  {"systemMode":"Away","canBePermanent":true,"canBeTemporary":true}]},
                {"systemId":"other-controller","zones":[{"zoneId":"other-zone"}]}]}]}]
            """;
    private static final String SCHEDULE = "{\"dailySchedules\":[" + List
            .of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").stream().map(day -> """
                    {"dayOfWeek":"%s","switchpoints":[{"timeOfDay":"06:00:00","heatSetpoint":18},
                    {"timeOfDay":"18:00:00","heatSetpoint":20}]}
                    """.formatted(day)).reduce((left, right) -> left + "," + right).orElseThrow() + "]}";
    private final HttpClient http = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final List<Runnable> tasks = new ArrayList<>();
    private final List<Long> delays = new ArrayList<>();
    private final Bridge bridge = BridgeBuilder.create(THING_TYPE_EVOHOME_ACCOUNT, "test")
            .withConfiguration(
                    new Configuration(Map.of("username", "user", "password", "password", "refreshInterval", 15)))
            .build();
    private final EvohomeAccountBridgeHandler handler = new EvohomeAccountBridgeHandler(bridge, http, scheduler);

    @BeforeEach
    void setup() {
        when(http.newRequest(anyString())).thenReturn(request);
        when(scheduler.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS))).thenAnswer(invocation -> {
            tasks.add(Objects.requireNonNull(invocation.getArgument(0, Runnable.class)));
            delays.add(Objects.requireNonNull(invocation.getArgument(1, Long.class)));
            return mock(ScheduledFuture.class);
        });
        handler.setCallback(callback);
        doAnswer(invocation -> {
            bridge.setStatusInfo(Objects.requireNonNull(invocation.getArgument(1, ThingStatusInfo.class)));
            return null;
        }).when(callback).statusUpdated(eq(bridge), any(ThingStatusInfo.class));
    }

    private ContentResponse response(int code, String json) {
        return mock(ContentResponse.class, invocation -> switch (invocation.getMethod().getName()) {
            case "getStatus" -> code;
            case "getContentAsString" -> json;
            case "getHeaders" -> new HttpFields();
            default -> RETURNS_DEFAULTS.answer(invocation);
        });
    }

    @Test
    void startupTimeoutRetriesAndReturnsOnline() throws Exception {
        when(request.send()).thenThrow(new TimeoutException()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, LOCATIONS), response(200, STATUS));
        handler.initialize();
        tasks.get(0).run();
        assertEquals(ThingStatus.OFFLINE, bridge.getStatus());
        assertEquals(15L, delays.get(1));
        tasks.get(1).run();
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), ACCOUNT_LAST_SUCCESSFUL_UPDATE_CHANNEL)),
                any());
        handler.dispose();
    }

    @Test
    void failedPollDoesNotPublishFreshnessAndSubsequentPollRecovers() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(200, STATUS)).thenThrow(new TimeoutException()).thenReturn(response(200, STATUS));
        handler.initialize();
        tasks.get(0).run();
        clearInvocations(callback);
        tasks.get(1).run();
        assertEquals(ThingStatus.OFFLINE, bridge.getStatus());
        verify(callback, never()).stateUpdated(any(), any());
        tasks.get(2).run();
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), ACCOUNT_LAST_SUCCESSFUL_UPDATE_CHANNEL)),
                any());
        handler.dispose();
    }

    @Test
    void repeatedFailureBacksOffAndExplicitRefreshDoesNotBypassIt() throws Exception {
        when(request.send()).thenThrow(new TimeoutException());
        handler.initialize();
        for (int i = 0; i < 8; i++) {
            tasks.get(i).run();
        }
        assertEquals(List.of(0L, 15L, 30L, 60L, 120L, 240L, 480L, 900L, 900L), delays);
        handler.requestRefresh();
        assertEquals(9, tasks.size());
        handler.dispose();
    }

    @Test
    void successfulCommandSchedulesReadbackButDoesNotPublishOptimisticState() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(200, STATUS), response(204, ""), response(200, STATUS));
        handler.initialize();
        tasks.get(0).run();
        clearInvocations(callback);
        assertTrue(handler.setPermanentSetPoint("zone", 21));
        assertEquals(2L, delays.getLast());
        verify(callback, never()).stateUpdated(any(), any());
        tasks.getLast().run();
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), ACCOUNT_LAST_SUCCESSFUL_UPDATE_CHANNEL)),
                any());
        handler.dispose();
    }

    @Test
    void rejectedCommandMarksCommunicationError() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(200, STATUS), response(500, "{}"));
        handler.initialize();
        tasks.get(0).run();
        assertFalse(handler.setPermanentSetPoint("zone", 21));
        assertEquals(ThingStatus.OFFLINE, bridge.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, bridge.getStatusInfo().getStatusDetail());
        assertEquals(15L, delays.getLast());
        handler.dispose();
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 404, 409, 422 })
    void rejectedCommandDoesNotTurnAWorkingAccountOffline(int status) throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(200, STATUS), response(status, "{}"));
        handler.initialize();
        tasks.getFirst().run();
        clearInvocations(callback);
        assertFalse(handler.setPermanentSetPoint("zone", 21));
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        assertEquals(ThingStatusDetail.NONE, bridge.getStatusInfo().getStatusDetail());
        assertEquals(2L, delays.getLast());
        verify(callback, never()).stateUpdated(any(), any());
        handler.dispose();
    }

    @Test
    void invalidLocalTemperaturePreservesAccountAndNormalPolling() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(200, STATUS));
        handler.initialize();
        tasks.getFirst().run();
        clearInvocations(callback);
        assertFalse(handler.setPermanentSetPoint("zone", 4));
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        assertEquals(List.of(0L, 15L), delays);
        verify(request, times(4)).send();
        verify(callback, never()).stateUpdated(any(), any());
        handler.dispose();
    }

    @Test
    void acceptedTimedModeSchedulesReadbackWithoutPublishingOptimisticState() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(201, "{}"),
                response(200, STATUS));
        handler.initialize();
        tasks.getFirst().run();
        clearInvocations(callback);

        assertTrue(handler.setTemporaryTcsMode("controller", "Away", Instant.now().plusSeconds(3600)));

        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        assertEquals(2L, delays.getLast());
        verify(callback, never()).stateUpdated(any(), any());
        tasks.getLast().run();
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), ACCOUNT_LAST_SUCCESSFUL_UPDATE_CHANNEL)),
                any());
        handler.dispose();
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 422 })
    void rejectedTimedModePreservesAccountAndSchedulesActualReadback(int status) throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(status, "{}"));
        handler.initialize();
        tasks.getFirst().run();
        clearInvocations(callback);

        assertFalse(handler.setTemporaryTcsMode("controller", "Away", Instant.now().plusSeconds(3600)));

        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        assertEquals(ThingStatusDetail.NONE, bridge.getStatusInfo().getStatusDetail());
        assertEquals(2L, delays.getLast());
        verify(request, times(5)).send();
        verify(callback, never()).stateUpdated(any(), any());
        handler.dispose();
    }

    @Test
    void expiredTimedModeIsRejectedLocallyWithoutDisturbingNormalPolling() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS));
        handler.initialize();
        tasks.getFirst().run();
        clearInvocations(callback);

        assertFalse(handler.setTemporaryTcsMode("controller", "Away", Instant.now().minusSeconds(1)));

        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        assertEquals(List.of(0L, 15L), delays);
        verify(request, times(4)).send();
        verify(callback, never()).stateUpdated(any(), any());
        handler.dispose();
    }

    @Test
    void timedModeTransportFailureMarksAccountOfflineAndRetries() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(500, "{}"));
        handler.initialize();
        tasks.getFirst().run();
        clearInvocations(callback);

        assertFalse(handler.setTemporaryTcsMode("controller", "Away", Instant.now().plusSeconds(3600)));

        assertEquals(ThingStatus.OFFLINE, bridge.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, bridge.getStatusInfo().getStatusDetail());
        assertEquals(15L, delays.getLast());
        verify(callback, never()).stateUpdated(any(), any());
        handler.dispose();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void resetClearsOnlySelectedControllerCommandContextsAfterApiAcceptance(boolean accepted) throws Exception {
        EvohomeHeatingZoneHandler selectedZone = attachedZone("zone");
        EvohomeHeatingZoneHandler otherZone = attachedZone("other-zone");
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(accepted ? 201 : 422, "{}"));
        handler.initialize();
        tasks.getFirst().run();
        clearInvocations(selectedZone, otherZone, callback);

        assertEquals(accepted, handler.resetSystem("controller"));

        verify(selectedZone, times(accepted ? 1 : 0)).resetCommandContext();
        verify(otherZone, never()).resetCommandContext();
        verify(callback, never()).stateUpdated(any(), any());
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        handler.dispose();
    }

    @Test
    void ordinaryAutoModePreservesAllZoneCommandContexts() throws Exception {
        EvohomeHeatingZoneHandler selectedZone = attachedZone("zone");
        EvohomeHeatingZoneHandler otherZone = attachedZone("other-zone");
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(201, "{}"));
        handler.initialize();
        tasks.getFirst().run();
        clearInvocations(selectedZone, otherZone);

        assertTrue(handler.setTcsMode("controller", "Auto"));

        verify(selectedZone, never()).resetCommandContext();
        verify(otherZone, never()).resetCommandContext();
        handler.dispose();
    }

    private EvohomeHeatingZoneHandler attachedZone(String zoneId) {
        Thing zone = ThingBuilder.create(THING_TYPE_EVOHOME_HEATING_ZONE, zoneId).withBridge(bridge.getUID()).build();
        EvohomeHeatingZoneHandler zoneHandler = mock(EvohomeHeatingZoneHandler.class);
        when(zoneHandler.getId()).thenReturn(zoneId);
        when(zoneHandler.getThing()).thenReturn(zone);
        zone.setHandler(zoneHandler);
        ((BridgeImpl) bridge).addThing(zone);
        return zoneHandler;
    }

    @Test
    void unlinkedScheduleChannelsDoNotCauseScheduleRequests() throws Exception {
        attachedZone("zone");
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS));
        handler.initialize();
        tasks.getFirst().run();

        verify(request, times(4)).send();
        verify(http, never()).newRequest(contains("/schedule"));
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        handler.dispose();
    }

    @Test
    void oneSchedulePerPollWarmsLinkedZonesAndSubsequentPollUsesCache() throws Exception {
        EvohomeHeatingZoneHandler firstZone = attachedZone("zone");
        EvohomeHeatingZoneHandler secondZone = attachedZone("other-zone");
        when(firstZone.hasScheduleLinks()).thenReturn(true);
        when(secondZone.hasScheduleLinks()).thenReturn(true);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(200, SCHEDULE),
                response(200, STATUS), response(200, SCHEDULE), response(200, STATUS));
        handler.initialize();

        tasks.getFirst().run();
        verify(http, times(1)).newRequest(contains("/schedule"));
        verify(request, times(5)).send();
        tasks.getLast().run();
        tasks.getLast().run();

        verify(http, times(1)).newRequest(contains("temperatureZone/zone/schedule"));
        verify(http, times(1)).newRequest(contains("temperatureZone/other-zone/schedule"));
        verify(request, times(8)).send();
        ArgumentCaptor<CachedSchedule> cache = ArgumentCaptor.forClass(CachedSchedule.class);
        verify(firstZone, times(3)).updateSchedule(cache.capture(), any(Instant.class));
        assertSame(cache.getAllValues().get(1), cache.getAllValues().getLast());
        assertNotNull(cache.getValue());
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        handler.dispose();
    }

    @Test
    void forcedRefreshReadsAnUnlinkedZoneAndCoalescesRepeatedRequests() throws Exception {
        attachedZone("zone");
        attachedZone("other-zone");
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(200, STATUS),
                response(200, SCHEDULE), response(200, STATUS), response(200, SCHEDULE));
        handler.initialize();
        tasks.getFirst().run();

        assertTrue(handler.requestScheduleRefresh("other-zone"));
        assertTrue(handler.requestScheduleRefresh("other-zone"));
        tasks.getLast().run();

        verify(http, times(1)).newRequest(contains("temperatureZone/other-zone/schedule"));
        verify(http, never()).newRequest(contains("temperatureZone/zone/schedule"));
        assertTrue(handler.requestScheduleRefresh("other-zone"));
        tasks.getLast().run();
        verify(http, times(2)).newRequest(contains("temperatureZone/other-zone/schedule"));
        handler.dispose();
    }

    @ParameterizedTest
    @ValueSource(ints = { 404, 500 })
    void optionalScheduleFailureKeepsAccountOnlineAndLetsAnotherZoneProgress(int status) throws Exception {
        EvohomeHeatingZoneHandler firstZone = attachedZone("zone");
        EvohomeHeatingZoneHandler secondZone = attachedZone("other-zone");
        when(firstZone.hasScheduleLinks()).thenReturn(true);
        when(secondZone.hasScheduleLinks()).thenReturn(true);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(status, "{}"),
                response(200, STATUS), response(200, SCHEDULE), response(200, STATUS));
        handler.initialize();
        tasks.getFirst().run();

        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        verify(firstZone).updateSchedule(isNull(), any(Instant.class));
        tasks.getLast().run();
        tasks.getLast().run();

        verify(http, times(1)).newRequest(contains("temperatureZone/zone/schedule"));
        verify(http, times(1)).newRequest(contains("temperatureZone/other-zone/schedule"));
        assertEquals(List.of(0L, 15L, 15L, 15L), delays);
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        handler.dispose();
    }

    @ParameterizedTest
    @ValueSource(strings = { "{not-json", "invalid-time" })
    void malformedOptionalScheduleDoesNotInvalidateSuccessfulStatusReadback(String invalid) throws Exception {
        EvohomeHeatingZoneHandler zone = attachedZone("zone");
        when(zone.hasScheduleLinks()).thenReturn(true);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS),
                response(200, "invalid-time".equals(invalid) ? SCHEDULE.replace("06:00:00", "not-a-time") : invalid));
        handler.initialize();
        tasks.getFirst().run();

        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        verify(zone).updateSchedule(isNull(), any(Instant.class));
        verify(callback).stateUpdated(eq(new ChannelUID(bridge.getUID(), ACCOUNT_LAST_SUCCESSFUL_UPDATE_CHANNEL)),
                any());
        assertEquals(15L, delays.getLast());
        handler.dispose();
    }

    @Test
    void failedForcedRefreshRetainsThePreviousCacheAndItsTimestamp() throws Exception {
        EvohomeHeatingZoneHandler zone = attachedZone("zone");
        when(zone.hasScheduleLinks()).thenReturn(true);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(200, SCHEDULE),
                response(200, STATUS), response(500, "{}"));
        handler.initialize();
        tasks.getFirst().run();
        assertTrue(handler.requestScheduleRefresh("zone"));
        tasks.getLast().run();

        ArgumentCaptor<CachedSchedule> cache = ArgumentCaptor.forClass(CachedSchedule.class);
        verify(zone, times(2)).updateSchedule(cache.capture(), any(Instant.class));
        assertNotNull(cache.getAllValues().getFirst());
        assertSame(cache.getAllValues().getFirst(), cache.getAllValues().getLast());
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        handler.dispose();
    }

    @ParameterizedTest
    @ValueSource(ints = { 429, 503 })
    void scheduleProviderCooldownDefersAllRequestsWithoutTakingAccountOffline(int status) throws Exception {
        EvohomeHeatingZoneHandler zone = attachedZone("zone");
        when(zone.hasScheduleLinks()).thenReturn(true);
        ContentResponse throttled = response(status, "{}");
        HttpFields fields = new HttpFields();
        fields.put("Retry-After", "120");
        when(throttled.getHeaders()).thenReturn(fields);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), throttled);
        handler.initialize();
        tasks.getFirst().run();

        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        assertEquals(120L, delays.getLast());
        int taskCount = tasks.size();
        handler.requestRefresh();
        assertTrue(handler.requestScheduleRefresh("zone"));
        assertEquals(taskCount, tasks.size());
        verify(request, times(5)).send();
        handler.dispose();
    }

    @Test
    void scheduleProviderFailureWithoutRetryAfterUsesSixtySecondCooldown() throws Exception {
        EvohomeHeatingZoneHandler zone = attachedZone("zone");
        when(zone.hasScheduleLinks()).thenReturn(true);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(503, "{}"));
        handler.initialize();
        tasks.getFirst().run();

        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        assertEquals(60L, delays.getLast());
        handler.dispose();
    }

    @Test
    void refreshQueuedDuringOptionalRequestCannotBypassProviderCooldown() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS), response(200, STATUS))
                .thenAnswer(invocation -> {
                    assertTrue(handler.requestScheduleRefresh("zone"));
                    ContentResponse throttled = response(429, "{}");
                    HttpFields fields = new HttpFields();
                    fields.put("Retry-After", "120");
                    when(throttled.getHeaders()).thenReturn(fields);
                    return throttled;
                });
        handler.initialize();
        tasks.getFirst().run();
        EvohomeHeatingZoneHandler zone = attachedZone("zone");
        when(zone.hasScheduleLinks()).thenReturn(true);
        handler.requestRefresh();
        tasks.getLast().run();

        assertEquals(120L, delays.getLast());
        assertEquals(ThingStatus.ONLINE, bridge.getStatus());
        tasks.get(tasks.size() - 2).run();
        verify(request, times(6)).send();
        handler.dispose();
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1441 })
    void invalidScheduleIntervalDoesNotStartPolling(int minutes) {
        bridge.getConfiguration().put("scheduleRefreshInterval", minutes);
        handler.initialize();
        assertEquals(ThingStatus.OFFLINE, bridge.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, bridge.getStatusInfo().getStatusDetail());
        assertTrue(tasks.isEmpty());
    }

    @Test
    void scheduleRefreshRejectsMissingClientAndBlankId() {
        assertFalse(handler.requestScheduleRefresh("zone"));
        handler.initialize();
        assertFalse(handler.requestScheduleRefresh(""));
        assertFalse(handler.requestScheduleRefresh(" "));
        handler.dispose();
        assertFalse(handler.requestScheduleRefresh("zone"));
    }

    @Test
    void reinitializationClearsQueuedForcedScheduleRefreshes() throws Exception {
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS));
        handler.initialize();
        assertTrue(handler.requestScheduleRefresh("zone"));
        handler.initialize();
        tasks.getLast().run();

        verify(http, never()).newRequest(contains("/schedule"));
        verify(request, times(4)).send();
        handler.dispose();
    }

    @Test
    @Timeout(15)
    void disposalDuringScheduleReadPreventsPublicationAndRescheduling() throws Exception {
        EvohomeHeatingZoneHandler zone = attachedZone("zone");
        when(zone.hasScheduleLinks()).thenReturn(true);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT),
                response(200, CONTROLLER_INSTALLATION), response(200, STATUS)).thenAnswer(invocation -> {
                    entered.countDown();
                    if (!release.await(10, TimeUnit.SECONDS)) {
                        throw new TimeoutException();
                    }
                    return response(200, SCHEDULE);
                });
        handler.initialize();
        var executor = Executors.newSingleThreadExecutor();
        try {
            var work = executor.submit(tasks.getFirst());
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            handler.dispose();
            clearInvocations(callback);
            release.countDown();
            work.get(10, TimeUnit.SECONDS);

            verifyNoInteractions(callback);
            verify(zone, never()).updateSchedule(any(), any());
            assertEquals(1, tasks.size());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void cancelledInitializationCannotContactApiAfterDisposal() throws Exception {
        handler.initialize();
        handler.dispose();
        clearInvocations(callback);
        tasks.get(0).run();
        verify(request, never()).send();
        verifyNoInteractions(callback);
        assertEquals(1, tasks.size());
    }

    @Test
    @Timeout(15)
    void disposalDuringReadPreventsPublicationAndRescheduling() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS))
                .thenAnswer(invocation -> {
                    entered.countDown();
                    if (!release.await(10, TimeUnit.SECONDS)) {
                        throw new TimeoutException();
                    }
                    return response(200, STATUS);
                });
        handler.initialize();
        var executor = Executors.newSingleThreadExecutor();
        try {
            var work = executor.submit(tasks.getFirst());
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            handler.dispose();
            clearInvocations(callback);
            release.countDown();
            work.get(10, TimeUnit.SECONDS);
            verifyNoInteractions(callback);
            assertEquals(1, tasks.size());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    @Timeout(15)
    void disposalDuringCommandPreventsAcceptedContextAndReadback() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Runnable onAccepted = mock(Runnable.class);
        when(request.send()).thenReturn(response(200, TOKEN), response(200, ACCOUNT), response(200, LOCATIONS),
                response(200, STATUS)).thenAnswer(invocation -> {
                    entered.countDown();
                    if (!release.await(10, TimeUnit.SECONDS)) {
                        throw new TimeoutException();
                    }
                    return response(204, "");
                });
        handler.initialize();
        tasks.getFirst().run();
        var executor = Executors.newSingleThreadExecutor();
        try {
            var work = executor.submit(() -> handler.setPermanentSetPoint("zone", 21, onAccepted));
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            handler.dispose();
            clearInvocations(callback);
            release.countDown();
            assertFalse(work.get(10, TimeUnit.SECONDS));
            verifyNoInteractions(onAccepted, callback);
            assertEquals(2, tasks.size());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void obsoleteWorkCannotRunAfterReinitialization() throws Exception {
        handler.initialize();
        handler.initialize();
        tasks.getFirst().run();
        verify(request, never()).send();
        handler.dispose();
    }
}
