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
package org.openhab.binding.bmwcardata.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.bmwcardata.internal.MapStorage;
import org.openhab.binding.bmwcardata.internal.api.Tokens;
import org.openhab.binding.bmwcardata.internal.stream.StreamSource;
import org.openhab.binding.bmwcardata.internal.stream.StreamState;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

import com.google.gson.Gson;

/**
 * Tests the refresh and retry handling of the {@link BMWCarDataAccountHandler}. The jobs of the handler run on a
 * scheduler controlled by the test, the HTTP client and the OAuth service are mocked.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BMWCarDataAccountHandlerTest {

    private static final ThingUID ACCOUNT_UID = new ThingUID(THING_TYPE_ACCOUNT, "account");
    private static final String VIN = "WBA12345678901234";
    private static final long PERIODIC_DELAY = 120;
    private static final long RETRY_DELAY = 900;
    private static final String TELEMATIC_DATA = """
            {"telematicData":{"vehicle.vehicle.travelledDistance":
             {"value":12345,"unit":"km","timestamp":"2026-10-24T10:00:00Z"}}}""";

    /** A job submitted to the scheduler, run by the test. */
    private static class Job {
        final Runnable task;
        final long delaySeconds;
        final boolean periodic;
        boolean cancelled;
        boolean ran;

        Job(Runnable task, long delaySeconds, boolean periodic) {
            this.task = task;
            this.delaySeconds = delaySeconds;
            this.periodic = periodic;
        }

        boolean isPending() {
            return !cancelled && (periodic || !ran);
        }
    }

    private final List<Job> jobs = new ArrayList<>();
    private final List<Answer<ContentResponse>> responses = new ArrayList<>();
    private final MapStorage storage = new MapStorage();
    private @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) ThingHandlerCallback accountCallback;
    private @NonNullByDefault({}) ThingHandlerCallback vehicleCallback;
    private @NonNullByDefault({}) Thing vehicleThing;
    private @NonNullByDefault({}) BMWCarDataAccountHandler handler;
    private @NonNullByDefault({}) BMWCarDataVehicleHandler vehicle;

    @BeforeEach
    void setUp() throws Exception {
        storage.put("container", "container-1|" + BMWCarDataAccountHandler.CONTAINER_PURPOSE);
        setUpHttpClient();
        vehicleThing = ThingBuilder.create(THING_TYPE_VEHICLE, new ThingUID(THING_TYPE_VEHICLE, ACCOUNT_UID, "car"))
                .withBridge(ACCOUNT_UID).withConfiguration(new Configuration(Map.of(CONFIG_VIN, VIN))).build();
        vehicleCallback = statusTrackingCallback();
        vehicle = new BMWCarDataVehicleHandler(vehicleThing);
        vehicle.setCallback(vehicleCallback);
        vehicle.initialize();
        vehicleThing.setHandler(vehicle);
    }

    @AfterEach
    void tearDown() {
        // the API restores the interrupt flag of an interrupted request
        Thread.interrupted();
    }

    @Test
    void transientFailureKeepsVehicleOnlineWhileStreamIsConnectedAndIsRetriedOnce() throws Exception {
        authorize(true);
        respond(status(502, "<html>502 Bad Gateway</html>"), status(200, TELEMATIC_DATA));

        runJobs(PERIODIC_DELAY);
        verify(vehicleCallback, never()).statusUpdated(any(), argThat(offline(ThingStatusDetail.COMMUNICATION_ERROR)));
        assertEquals(1, pendingJobs(RETRY_DELAY));

        runJobs(RETRY_DELAY);
        verify(vehicleCallback).stateUpdated(new ChannelUID(vehicleThing.getUID(), "range#mileage"),
                QuantityType.valueOf("12345 km"));
        assertEquals(ThingStatus.ONLINE, vehicleThing.getStatus());
        assertEquals(2, telematicRequests());
        assertEquals(0, pendingJobs(RETRY_DELAY));
    }

    @Test
    void transientFailureWithoutStreamSetsVehicleOfflineUntilRetrySucceeds() throws Exception {
        authorize(false);
        respond(status(502, "<html>502 Bad Gateway</html>"), status(200, TELEMATIC_DATA));

        runJobs(PERIODIC_DELAY);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, vehicleThing.getStatusInfo().getStatusDetail());

        runJobs(RETRY_DELAY);
        assertEquals(ThingStatus.ONLINE, vehicleThing.getStatus());
    }

    @Test
    void failedRetryIsNotRetriedAgain() throws Exception {
        authorize(false);
        respond(status(503, ""), failure(new ExecutionException(new java.net.ConnectException("refused"))));

        runJobs(PERIODIC_DELAY);
        runJobs(RETRY_DELAY);

        assertEquals(2, telematicRequests());
        assertEquals(0, pendingJobs(RETRY_DELAY));
    }

    @Test
    void permanentFailureIsNotRetried() throws Exception {
        authorize(true);
        respond(status(403, "{\"exveErrorId\":\"CU-403\"}"));

        runJobs(PERIODIC_DELAY);

        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, vehicleThing.getStatusInfo().getStatusDetail());
        assertEquals(0, pendingJobs(RETRY_DELAY));
    }

    @Test
    void retryIsSkippedWhenAnotherRefreshSucceededMeanwhile() throws Exception {
        authorize(true);
        respond(status(502, ""), status(200, TELEMATIC_DATA));

        runJobs(PERIODIC_DELAY);
        // e.g. the catch-up refresh after a stream outage
        runJobs(PERIODIC_DELAY);
        runJobs(RETRY_DELAY);

        assertEquals(2, telematicRequests());
    }

    @Test
    void disposeCancelsPendingRetry() throws Exception {
        authorize(true);
        respond(status(502, ""));
        runJobs(PERIODIC_DELAY);
        Job retry = jobs.stream().filter(job -> job.delaySeconds == RETRY_DELAY).findFirst().orElseThrow();

        handler.dispose();

        assertTrue(retry.cancelled);
    }

    @Test
    void requestInterruptedByDisposeDoesNotSetVehicleOffline() throws Exception {
        authorize(false);
        respond(invocation -> {
            handler.dispose();
            throw new InterruptedException();
        });

        runJobs(PERIODIC_DELAY);

        verify(vehicleCallback, never()).statusUpdated(any(), argThat(offline(ThingStatusDetail.COMMUNICATION_ERROR)));
        assertEquals(0, pendingJobs(RETRY_DELAY));
    }

    @Test
    void refreshOfPreviousLifecycleDoesNotActAfterReinitialization() throws Exception {
        authorize(false);
        respond(invocation -> {
            // the configuration is changed while the request is running, the new lifecycle is authorized again
            handler.dispose();
            handler.initialize();
            runJobs(0);
            return status(502, "").answer(invocation);
        });

        runJobs(PERIODIC_DELAY);

        verify(vehicleCallback, never()).statusUpdated(any(), argThat(offline(ThingStatusDetail.COMMUNICATION_ERROR)));
        assertEquals(0, pendingJobs(RETRY_DELAY));
    }

    private void authorize(boolean streaming) throws Exception {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(ACCOUNT_UID);
        when(bridge.getThingTypeUID()).thenReturn(THING_TYPE_ACCOUNT);
        when(bridge.getConfiguration()).thenReturn(new Configuration(
                Map.of("clientId", "client-1", "streaming", streaming, "refreshInterval", 6, "requestBudget", 40)));
        when(bridge.getThings()).thenReturn(List.of(vehicleThing));

        AccessTokenResponse token = new AccessTokenResponse();
        token.setAccessToken("access");
        token.setRefreshToken("refresh");
        token.setExpiresIn(3600);
        token.setCreatedOn(Instant.now());
        token.setExtraFields(Map.of("id_token", "id", "gcid", "gcid-1"));
        OAuthClientService oAuthService = mock(OAuthClientService.class);
        when(oAuthService.getAccessTokenResponse()).thenReturn(token);
        when(oAuthService.refreshToken()).thenReturn(token);
        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        when(oAuthFactory.createOAuthClientService(anyString(), anyString(), any(), anyString(), any(), any(), any()))
                .thenReturn(oAuthService);

        StreamSource streamSource = mock(StreamSource.class);
        when(streamSource.stop()).thenReturn(CompletableFuture.completedFuture(true));
        handler = new BMWCarDataAccountHandler(bridge, httpClient, new Gson(), storage, oAuthFactory, scheduler()) {
            @Override
            StreamSource createStreamSource(Tokens tokens) {
                return streamSource;
            }
        };
        accountCallback = mock(ThingHandlerCallback.class);
        handler.setCallback(accountCallback);
        handler.initialize();
        runJobs(0);
        if (streaming) {
            handler.onStreamStateChanged(StreamState.CONNECTED, null);
        }
        assertEquals(1, pendingJobs(PERIODIC_DELAY));
    }

    private ScheduledExecutorService scheduler() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> addJob(invocation, false));
        when(scheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> addJob(invocation, true));
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(scheduler).execute(any(Runnable.class));
        return scheduler;
    }

    private ScheduledFuture<?> addJob(InvocationOnMock invocation, boolean periodic) {
        long delay = invocation.getArgument(1);
        TimeUnit unit = invocation.getArgument(periodic ? 3 : 2);
        Job job = new Job(invocation.getArgument(0), unit.toSeconds(delay), periodic);
        jobs.add(job);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        when(future.cancel(anyBoolean())).thenAnswer(cancel -> {
            job.cancelled = true;
            return true;
        });
        when(future.isDone()).thenAnswer(done -> job.cancelled || (job.ran && !job.periodic));
        return future;
    }

    /**
     * Runs the pending jobs with the given delay, as if the time had passed.
     */
    private void runJobs(long delaySeconds) {
        for (Job job : List.copyOf(jobs)) {
            if (job.delaySeconds == delaySeconds && job.isPending()) {
                job.ran = true;
                job.task.run();
            }
        }
    }

    private long pendingJobs(long delaySeconds) {
        return jobs.stream().filter(job -> job.delaySeconds == delaySeconds && job.isPending()).count();
    }

    private void setUpHttpClient() throws Exception {
        httpClient = mock(HttpClient.class);
        Request request = mock(Request.class, RETURNS_SELF);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenAnswer(invocation -> {
            if (responses.isEmpty()) {
                fail("Unexpected request");
            }
            return responses.remove(0).answer(invocation);
        });
    }

    @SafeVarargs
    private void respond(Answer<ContentResponse>... answers) {
        responses.addAll(List.of(answers));
    }

    private static Answer<ContentResponse> status(int status, String content) {
        return invocation -> {
            ContentResponse response = mock(ContentResponse.class);
            when(response.getStatus()).thenReturn(status);
            when(response.getContentAsString()).thenReturn(content);
            return response;
        };
    }

    private static Answer<ContentResponse> failure(Exception exception) {
        return invocation -> {
            throw exception;
        };
    }

    private int telematicRequests() {
        return (int) mockingDetails(httpClient).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("newRequest")
                        && invocation.getArgument(0).toString().endsWith("/telematicData"))
                .count();
    }

    private static ThingHandlerCallback statusTrackingCallback() {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        doAnswer(invocation -> {
            Thing thing = invocation.getArgument(0);
            thing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
        return callback;
    }

    private static ArgumentMatcher<ThingStatusInfo> offline(ThingStatusDetail detail) {
        return info -> info.getStatus() == ThingStatus.OFFLINE && info.getStatusDetail() == detail;
    }
}
