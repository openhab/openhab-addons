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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.bmwcardata.internal.MapStorage;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient.DeviceAuthorization;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataException;
import org.openhab.binding.bmwcardata.internal.api.Tokens;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.stream.StreamSource;
import org.openhab.binding.bmwcardata.internal.stream.StreamState;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
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
 * Tests the refresh and retry handling, the device authorization and the basic data cache of the
 * {@link BMWCarDataAccountHandler}. The jobs of the handler run on a scheduler controlled by the test, the HTTP client
 * and the OAuth service are mocked.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BMWCarDataAccountHandlerTest {

    private static final ThingUID ACCOUNT_UID = new ThingUID(THING_TYPE_ACCOUNT, "account");
    private static final String VIN = "WBA12345678901234";
    // the refresh interval of 6 hours, as the first refresh after the start is done per vehicle
    private static final long PERIODIC_DELAY = 6 * 3600;
    private static final long RETRY_DELAY = 900;
    private static final int POLL_INTERVAL = 5;
    private static final long AUTH_RETRY_DELAY = 60;
    private static final String TELEMATIC_DATA = """
            {"telematicData":{"vehicle.vehicle.travelledDistance":
             {"value":12345,"unit":"km","timestamp":"2026-10-24T10:00:00Z"}}}""";
    // the PNG signature, enough to tell the bytes apart
    private static final byte[] IMAGE = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n' };
    private static final String BASIC_DATA = """
            {"brand":"BMW","modelName":"iX xDrive40","isTelematicsCapable":false}""";

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
    private final MapStorage budgetStorage = new MapStorage();
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
                .withBridge(ACCOUNT_UID).withConfiguration(new Configuration(Map.of(CONFIG_VIN, VIN)))
                // the basic data is known, so the vehicle goes online without retrieving it
                .withProperties(Map.of(PROPERTY_MODEL, "iX xDrive40", PROPERTY_VIN, VIN)).build();
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
        // translated, with only a short detail, as the raw response is not meant for the user
        assertEquals("@text/offline.comm-error.refresh-failed [\"HTTP 502\"]",
                vehicleThing.getStatusInfo().getDescription());

        runJobs(RETRY_DELAY);
        assertEquals(ThingStatus.ONLINE, vehicleThing.getStatus());
    }

    @Test
    void unusableContainerIsReplacedAtTheNextRefresh() throws Exception {
        authorize(false);
        // BMW reports a container that is not allocated to the customer or does not exist with CU-105, not only 404
        respond(status(403,
                "{\"exveErrorId\":\"CU-105\",\"exveErrorMsg\":\"No permission for specified containerId\"}"));

        runJobs(PERIODIC_DELAY);
        assertNull(storage.get("container"));
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, vehicleThing.getStatusInfo().getStatusDetail());

        respond(status(200, "{\"containers\":[]}"), status(200, "{\"containerId\":\"container-2\"}"),
                status(200, TELEMATIC_DATA));
        runJobs(PERIODIC_DELAY);
        assertEquals("container-2|" + BMWCarDataAccountHandler.CONTAINER_PURPOSE, storage.get("container"));
        assertEquals(ThingStatus.ONLINE, vehicleThing.getStatus());
        assertTrue(responses.isEmpty());
    }

    @Test
    void containerIsIdentifiedByItsDescriptors() {
        String purpose = BMWCarDataAccountHandler.containerPurpose(List.of("vehicle.a", "vehicle.b"));
        assertEquals(purpose, BMWCarDataAccountHandler.containerPurpose(List.of("vehicle.b", "vehicle.a")));
        assertNotEquals(purpose, BMWCarDataAccountHandler.containerPurpose(List.of("vehicle.a", "vehicle.c")));
        assertEquals(BMWCarDataAccountHandler.containerPurpose(VehicleChannel.descriptors()),
                BMWCarDataAccountHandler.CONTAINER_PURPOSE);
    }

    @Test
    void containerOfAnEarlierVersionIsReplaced() throws Exception {
        // stored by a version that identified the container by a revision number
        storage.put("container", "container-1|openHAB BMW CarData binding rev 6");
        authorize(false);
        respond(status(200, """
                {"containers":[{"containerId":"container-1","name":"openHAB",\
                "purpose":"openHAB BMW CarData binding rev 6","state":"ACTIVE"}]}"""), status(204, ""),
                status(200, "{\"containerId\":\"container-2\"}"), status(200, TELEMATIC_DATA));

        runJobs(PERIODIC_DELAY);

        assertEquals("container-2|" + BMWCarDataAccountHandler.CONTAINER_PURPOSE, storage.get("container"));
        assertEquals(1, requests("/containers/container-1"));
        assertTrue(responses.isEmpty());
    }

    @Test
    void containerLimitIsReportedClearly() throws Exception {
        storage.remove("container");
        authorize(false);
        respond(status(200, "{\"containers\":[]}"),
                status(403, "{\"exveErrorId\":\"CU-124\",\"exveErrorMsg\":\"Maximum number of containers reached\"}"));

        runJobs(PERIODIC_DELAY);

        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, vehicleThing.getStatusInfo().getStatusDetail());
        assertEquals("@text/offline.comm-error.container-limit", vehicleThing.getStatusInfo().getDescription());
        assertNull(storage.get("container"));
    }

    @Test
    void vinWithoutPermissionIsAConfigurationErrorAndNotRefreshedAgain() throws Exception {
        authorize(false);
        // e.g. the vehicle is not mapped to the customer as primary user
        respond(status(403, "{\"exveErrorId\":\"CU-104\",\"exveErrorMsg\":\"No permission for specified VIN\"}"));

        runJobs(PERIODIC_DELAY);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, vehicleThing.getStatusInfo().getStatusDetail());
        assertEquals("@text/offline.conf-error.vin-not-permitted", vehicleThing.getStatusInfo().getDescription());

        runJobs(PERIODIC_DELAY);
        assertEquals(1, telematicRequests());
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

    @Test
    void vehicleIsReadOnceWhenItGoesOnline() throws Exception {
        authorize(false);
        respond(status(200, TELEMATIC_DATA));

        handler.vehicleOnline(vehicle);
        runJobs(0);
        verify(vehicleCallback).stateUpdated(new ChannelUID(vehicleThing.getUID(), "range#mileage"),
                QuantityType.valueOf("12345 km"));

        // e.g. after the account was offline for a moment
        handler.vehicleOnline(vehicle);
        runJobs(0);
        assertEquals(1, telematicRequests());
    }

    @Test
    void vehicleIsReadAgainAfterItsRestart() throws Exception {
        authorize(false);
        respond(status(200, TELEMATIC_DATA), status(200, TELEMATIC_DATA));
        handler.vehicleOnline(vehicle);
        runJobs(0);

        handler.childHandlerDisposed(vehicle, vehicleThing);
        handler.vehicleOnline(vehicle);
        runJobs(0);

        assertEquals(2, telematicRequests());
    }

    @Test
    void vehicleIsReadAgainAfterTheRestartOfTheAccount() throws Exception {
        authorize(false);
        respond(status(200, TELEMATIC_DATA), status(200, TELEMATIC_DATA));
        handler.vehicleOnline(vehicle);
        runJobs(0);

        handler.dispose();
        handler.initialize();
        runJobs(0);
        handler.vehicleOnline(vehicle);
        runJobs(0);

        assertEquals(2, telematicRequests());
    }

    @Test
    void readOfAVehicleScheduledBeforeTheRestartOfTheAccountIsDropped() throws Exception {
        authorize(false);
        handler.vehicleOnline(vehicle);
        handler.dispose();

        // fails with an unexpected request if the read of the previous lifecycle is sent
        runJobs(0);

        assertEquals(0, telematicRequests());
    }

    @Test
    void lateCallOfADisposedVehicleDoesNotKeepItsSuccessorFromBeingRead() throws Exception {
        authorize(false);
        respond(status(200, TELEMATIC_DATA));
        // the vehicle is disposed after its job checked it, but before it went online
        vehicle.dispose();
        handler.childHandlerDisposed(vehicle, vehicleThing);
        handler.vehicleOnline(vehicle);
        runJobs(0);
        assertEquals(0, telematicRequests());

        BMWCarDataVehicleHandler successor = new BMWCarDataVehicleHandler(vehicleThing);
        successor.setCallback(vehicleCallback);
        successor.initialize();
        vehicleThing.setHandler(successor);
        handler.vehicleOnline(successor);
        runJobs(0);

        assertEquals(1, telematicRequests());
    }

    @Test
    void vehicleDisposedBeforeItsReadIsNotRead() throws Exception {
        authorize(false);
        handler.vehicleOnline(vehicle);
        vehicle.dispose();

        // fails with an unexpected request if the disposed vehicle is read
        runJobs(0);

        assertEquals(0, telematicRequests());
    }

    @Test
    void basicDataIsFetchedOnceAndThenServedFromTheCache() throws Exception {
        authorize(false);
        respond(status(200, BASIC_DATA));

        assertEquals("iX xDrive40", handler.fetchBasicData(VIN).modelName);
        BasicData cached = handler.fetchBasicData(VIN);

        assertEquals("iX xDrive40", cached.modelName);
        assertEquals(Boolean.FALSE, cached.isTelematicsCapable);
        assertEquals(1, requests("/basicData"));
    }

    @Test
    void corruptBasicDataInTheCacheFallsBackToTheApi() throws Exception {
        authorize(false);
        storage.put("basicData.v2." + VIN, "{not json");
        respond(status(200, BASIC_DATA));

        assertEquals("iX xDrive40", handler.fetchBasicData(VIN).modelName);
        // the entry is replaced, so the next fetch needs no request
        assertEquals("iX xDrive40", handler.fetchBasicData(VIN).modelName);
        assertEquals(1, requests("/basicData"));
    }

    @Test
    void vehicleImageIsFetchedOnceAndThenServedFromTheCache() throws Exception {
        authorize(false);
        respond(image(IMAGE));

        RawType image = handler.fetchVehicleImage(VIN);
        assertEquals("image/png", image.getMimeType());
        assertArrayEquals(IMAGE, image.getBytes());
        assertArrayEquals(IMAGE, handler.fetchVehicleImage(VIN).getBytes());
        assertEquals(1, requests("/image"));
    }

    @Test
    void corruptVehicleImageInTheCacheFallsBackToTheApi() throws Exception {
        authorize(false);
        storage.put("image." + VIN, "not a data URI");
        respond(image(IMAGE));

        assertArrayEquals(IMAGE, handler.fetchVehicleImage(VIN).getBytes());
        // the entry is replaced, so the next fetch needs no request
        assertArrayEquals(IMAGE, handler.fetchVehicleImage(VIN).getBytes());
        assertEquals(1, requests("/image"));
    }

    @Test
    void responseWithoutImageIsAnErrorAndNotCached() throws Exception {
        authorize(false);
        respond(status(200, "{}"));

        assertThrows(BMWCarDataException.class, () -> handler.fetchVehicleImage(VIN));
        assertNull(storage.get("image." + VIN));
    }

    @Test
    void basicDataCachedByAnEarlierVersionIsReadAgain() throws Exception {
        authorize(false);
        // an earlier version cached only the fields it mapped, so the new ones would be missing
        storage.put("basicData." + VIN, "{\"brand\":\"BMW\",\"modelName\":\"iX xDrive40\"}");
        respond(status(200, "{\"brand\":\"BMW\",\"modelName\":\"iX xDrive40\",\"bodyType\":\"SAV\"}"));

        assertEquals("SAV", handler.fetchBasicData(VIN).bodyType);
        assertEquals(1, requests("/basicData"));
    }

    @Test
    void rejectedClientIdIsAConfigurationErrorWithoutRetry() throws Exception {
        // BMW answers an unknown client ID or inactive subscriptions with 400
        respond(status(400, "{\"error\":\"invalid_request\",\"error_description\":\"invalid client\"}"));
        startHandler(false, null);

        verify(accountCallback).statusUpdated(any(), argThat(offline(ThingStatusDetail.CONFIGURATION_ERROR)));
        verify(accountCallback, never()).statusUpdated(any(), argThat(offline(ThingStatusDetail.COMMUNICATION_ERROR)));
        assertEquals(0, pendingJobs(AUTH_RETRY_DELAY));
    }

    @Test
    void unauthorizedDeviceAuthorizationRequestIsAConfigurationErrorWithoutRetry() throws Exception {
        // like BMW's token endpoint for an invalid client: 401 without WWW-Authenticate header, which the HTTP client
        // turns into a failed request that keeps the status
        respond(failure(new ExecutionException(unauthorizedResponse())));
        startHandler(false, null);

        verify(accountCallback).statusUpdated(any(), argThat(offline(ThingStatusDetail.CONFIGURATION_ERROR)));
        verify(accountCallback, never()).statusUpdated(any(), argThat(offline(ThingStatusDetail.COMMUNICATION_ERROR)));
        assertEquals(0, pendingJobs(AUTH_RETRY_DELAY));
    }

    @Test
    void failingDeviceAuthorizationRequestIsRetried() throws Exception {
        respond(status(503, ""));
        startHandler(false, null);

        verify(accountCallback).statusUpdated(any(),
                argThat(info -> offline(ThingStatusDetail.COMMUNICATION_ERROR).matches(info)
                        && "@text/offline.comm-error.authorization-request-failed [\"HTTP 503\"]"
                                .equals(info.getDescription())));
        assertEquals(1, pendingJobs(AUTH_RETRY_DELAY));
    }

    @Test
    void unauthorizedResponseWhilePollingKeepsPolling() throws Exception {
        // BMW answers an expired code, an invalid client and an account pending activation with 401 without
        // WWW-Authenticate header, so the HTTP client fails the request and the error code is lost
        respond(status(200, deviceCode("USER-1", 600)), failure(new ExecutionException(unauthorizedResponse())));
        startHandler(false, null);
        verify(accountCallback).statusUpdated(any(), argThat(offline(ThingStatusDetail.AUTHORIZATION_PENDING)));
        assertEquals("USER-1", pendingUserCode());

        runJobs(POLL_INTERVAL);

        assertTrue(responses.isEmpty());
        assertEquals(1, pendingJobs(POLL_INTERVAL));
        assertEquals("USER-1", pendingUserCode());
        verify(accountCallback, never()).statusUpdated(any(), argThat(offline(ThingStatusDetail.CONFIGURATION_ERROR)));
        verify(accountCallback, never()).statusUpdated(any(), argThat(offline(ThingStatusDetail.COMMUNICATION_ERROR)));
    }

    @Test
    void expiredCodeStartsANewAuthorization() throws Exception {
        respond(status(200, deviceCode("USER-1", 1)));
        startHandler(false, null);
        Instant requested = Instant.now();
        // the code expires by the handler's own clock, as a 401 of BMW does not tell why it was rejected
        waitUntil(() -> Instant.now().isAfter(requested.plusSeconds(1)));
        respond(status(200, deviceCode("USER-2", 600)));

        runJobs(POLL_INTERVAL);

        // a new code is requested instead of polling with the expired one
        assertTrue(responses.isEmpty());
        assertEquals("USER-2", pendingUserCode());
    }

    private void authorize(boolean streaming) throws Exception {
        AccessTokenResponse token = new AccessTokenResponse();
        token.setAccessToken("access");
        token.setRefreshToken("refresh");
        token.setExpiresIn(3600);
        token.setCreatedOn(Instant.now());
        token.setExtraFields(Map.of("id_token", "id", "gcid", "gcid-1"));
        startHandler(streaming, token);
        if (streaming) {
            handler.onStreamStateChanged(StreamState.CONNECTED, null);
        }
        assertEquals(1, pendingJobs(PERIODIC_DELAY));
    }

    /**
     * Initializes the account handler and runs its first authentication.
     *
     * @param storedToken the token stored by the OAuth client service, {@code null} for a new device authorization
     */
    private void startHandler(boolean streaming, @Nullable AccessTokenResponse storedToken) throws Exception {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(ACCOUNT_UID);
        when(bridge.getThingTypeUID()).thenReturn(THING_TYPE_ACCOUNT);
        when(bridge.getConfiguration()).thenReturn(new Configuration(
                Map.of("clientId", "client-1", "streaming", streaming, "refreshInterval", 6, "requestBudget", 40)));
        when(bridge.getThings()).thenReturn(List.of(vehicleThing));

        OAuthClientService oAuthService = mock(OAuthClientService.class);
        when(oAuthService.getAccessTokenResponse()).thenReturn(storedToken);
        if (storedToken != null) {
            when(oAuthService.refreshToken()).thenReturn(storedToken);
        }
        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        when(oAuthFactory.createOAuthClientService(anyString(), anyString(), any(), anyString(), any(), any(), any()))
                .thenReturn(oAuthService);

        StreamSource streamSource = mock(StreamSource.class);
        when(streamSource.stop()).thenReturn(CompletableFuture.completedFuture(true));
        handler = new BMWCarDataAccountHandler(bridge, httpClient, new Gson(), storage, budgetStorage, oAuthFactory,
                scheduler()) {
            @Override
            StreamSource createStreamSource(Tokens tokens) {
                return streamSource;
            }
        };
        accountCallback = mock(ThingHandlerCallback.class);
        handler.setCallback(accountCallback);
        handler.initialize();
        runJobs(0);
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

    private static Answer<ContentResponse> image(byte[] content) {
        return invocation -> {
            ContentResponse response = mock(ContentResponse.class);
            when(response.getStatus()).thenReturn(200);
            when(response.getContent()).thenReturn(content);
            when(response.getMediaType()).thenReturn("image/png");
            return response;
        };
    }

    private static Answer<ContentResponse> failure(Exception exception) {
        return invocation -> {
            throw exception;
        };
    }

    private int telematicRequests() {
        return requests("/telematicData");
    }

    private int requests(String pathSuffix) {
        return (int) mockingDetails(httpClient).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("newRequest")
                        && invocation.getArgument(0).toString().endsWith(pathSuffix))
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

    private String pendingUserCode() {
        DeviceAuthorization authorization = handler.getPendingAuthorization();
        assertNotNull(authorization);
        return Objects.requireNonNull(authorization).userCode();
    }

    private static String deviceCode(String userCode, int expiresInSeconds) {
        return """
                {"device_code":"device-%s","user_code":"%s","verification_uri":"https://customer.bmwgroup.com/oneid/link",\
                "expires_in":%d,"interval":%d}"""
                .formatted(userCode, userCode, expiresInSeconds, POLL_INTERVAL);
    }

    private static HttpResponseException unauthorizedResponse() {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(401);
        return new HttpResponseException("HTTP protocol violation: Authentication challenge without WWW-Authenticate",
                response);
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < deadline, "condition not reached within 10 seconds");
            Thread.sleep(20);
        }
    }
}
