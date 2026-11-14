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
package org.openhab.binding.veluxactive.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.veluxactive.internal.ModuleRequestContext;
import org.openhab.binding.veluxactive.internal.VeluxActiveApi;
import org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants;
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.crypto.SigningKeyStore;
import org.openhab.binding.veluxactive.internal.exception.ApiException;
import org.openhab.binding.veluxactive.internal.exception.ApiUnauthorizedException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests authentication, recovery and asynchronous lifecycle transitions without live credentials.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveAccountHandlerTest {
    private final OAuthFactory factory = mock(OAuthFactory.class);
    private final OAuthClientService originalClient = mock(OAuthClientService.class);
    private final OAuthClientService client = mock(OAuthClientService.class);
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final ScheduledFuture<?> future = mock(ScheduledFuture.class);
    private final ManualExecutor executor = new ManualExecutor();
    private final List<Runnable> scheduled = new ArrayList<>();
    private final List<ThingStatusInfo> statuses = new ArrayList<>();
    private final Bridge bridge = spy(bridge("user", "password"));
    private final VeluxActiveApi api = mock(VeluxActiveApi.class);
    private final VeluxActiveAccountHandler handler = new VeluxActiveAccountHandler(bridge, factory, executor,
            scheduler, api, () -> java.time.ZoneId.of("Europe/Copenhagen"));

    @BeforeEach
    void setUp() {
        when(factory.createOAuthClientService(anyString(), eq(TOKEN_URL), isNull(), eq(CLIENT_ID), eq(CLIENT_SECRET),
                eq(SCOPE), eq(false))).thenReturn(originalClient);
        when(originalClient.withGsonBuilder(any())).thenReturn(client);
        when(scheduler.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS))).thenAnswer(invocation -> {
            scheduled.add(invocation.getArgument(0));
            return future;
        });
        attachCallback(handler);
    }

    @Test
    void logsInAsynchronouslyAndSchedulesRefresh() throws Exception {
        when(client.getAccessTokenByResourceOwnerPasswordCredentials("user", "password", SCOPE)).thenReturn(token());
        handler.initialize();
        assertThat(status().getStatus(), is(ThingStatus.UNKNOWN));
        verifyNoInteractions(factory);
        executor.runAll();
        assertThat(status().getStatus(), is(ThingStatus.ONLINE));
        verify(client).addExtraAuthField("user_prefix", "velux");
        verify(client).addExtraAuthField("app_version", APP_VERSION);
        verify(originalClient).withGsonBuilder(any());
        verify(originalClient, never()).getAccessTokenResponse();
        verify(originalClient, never()).addExtraAuthField(anyString(), anyString());
        verify(scheduler).schedule(any(Runnable.class), longThat(delay -> delay > 3500 && delay <= 3540),
                eq(TimeUnit.SECONDS));
    }

    @Test
    void reusesPersistedToken() throws Exception {
        when(client.getAccessTokenResponse()).thenReturn(token());
        handler.initialize();
        executor.runAll();
        assertThat(status().getStatus(), is(ThingStatus.ONLINE));
        verify(client, never()).getAccessTokenByResourceOwnerPasswordCredentials(anyString(), anyString(), anyString());
    }

    @Test
    void refreshesBeforeExpiry() throws Exception {
        AccessTokenResponse expiring = token();
        expiring.setCreatedOn(Instant.now().minusSeconds(3590));
        when(client.getAccessTokenResponse()).thenReturn(token(), expiring);
        when(client.refreshToken()).thenReturn(token());
        handler.initialize();
        executor.runAll();
        scheduled.removeFirst().run();
        executor.runAll();
        verify(client).refreshToken();
        assertThat(status().getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    void rejectedRefreshTokenFallsBackToPasswordGrant() throws Exception {
        when(client.getAccessTokenResponse()).thenThrow(oAuthError("invalid_grant"));
        when(client.getAccessTokenByResourceOwnerPasswordCredentials("user", "password", SCOPE)).thenReturn(token());
        handler.initialize();
        executor.runAll();
        assertThat(status().getStatus(), is(ThingStatus.ONLINE));
        verify(client).getAccessTokenByResourceOwnerPasswordCredentials("user", "password", SCOPE);
    }

    @Test
    void rejectedCredentialsStopRetriesAndHideProviderDescription() throws Exception {
        OAuthResponseException error = oAuthError("invalid_grant");
        error.setErrorDescription("password=secret");
        when(client.getAccessTokenByResourceOwnerPasswordCredentials("user", "password", SCOPE)).thenThrow(error);
        handler.initialize();
        executor.runAll();
        assertThat(status().getStatusDetail(), is(ThingStatusDetail.CONFIGURATION_ERROR));
        assertThat(String.valueOf(status().getDescription()).contains(CLIENT_SECRET), is(false));
        assertThat(scheduled.isEmpty(), is(true));
    }

    @Test
    void communicationFailureRetriesWithoutDiscardingTokens() throws Exception {
        when(client.getAccessTokenResponse()).thenThrow(new IOException("unavailable")).thenReturn(token());
        handler.initialize();
        executor.runAll();
        assertThat(status().getStatusDetail(), is(ThingStatusDetail.COMMUNICATION_ERROR));
        verify(scheduler).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
        scheduled.removeFirst().run();
        executor.runAll();
        assertThat(status().getStatus(), is(ThingStatus.ONLINE));
        verify(factory, never()).deleteServiceAndAccessToken(anyString());
    }

    @Test
    void missingConfigurationDoesNotContactOAuth() {
        handler.thingUpdated(bridge("user", ""));
        executor.runAll();
        assertThat(status().getStatusDetail(), is(ThingStatusDetail.CONFIGURATION_ERROR));
        verifyNoInteractions(factory);
    }

    @Test
    void disposalCancelsRefreshAndRetainsTokens() throws Exception {
        when(client.getAccessTokenResponse()).thenReturn(token());
        handler.initialize();
        executor.runAll();
        handler.dispose();
        executor.runAll();
        verify(future).cancel(false);
        verify(factory).ungetOAuthService(anyString());
        verify(factory, never()).deleteServiceAndAccessToken(anyString());
        int updates = statuses.size();
        scheduled.removeFirst().run();
        executor.runAll();
        assertThat(statuses.size(), is(updates));
        verify(client, times(1)).getAccessTokenResponse();
    }

    @Test
    void changedCredentialsInvalidateStoredTokenBeforeReinitialization() throws Exception {
        when(client.getAccessTokenResponse()).thenReturn(token());
        handler.initialize();
        executor.runAll();
        clearInvocations(factory);
        handler.thingUpdated(bridge("user", "new-password"));
        executor.runAll();
        var order = inOrder(factory);
        order.verify(factory).ungetOAuthService(anyString());
        order.verify(factory).deleteServiceAndAccessToken(anyString());
        order.verify(factory).createOAuthClientService(anyString(), eq(TOKEN_URL), isNull(), eq(CLIENT_ID),
                eq(CLIENT_SECRET), eq(SCOPE), eq(false));
        assertThat(status().getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    void accountIdentityChangesTheTokenStoreHandle() throws Exception {
        when(client.getAccessTokenResponse()).thenReturn(token());
        handler.initialize();
        executor.runAll();
        handler.thingUpdated(bridge("another-user", "password"));
        executor.runAll();
        var handles = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(factory, times(2)).createOAuthClientService(handles.capture(), eq(TOKEN_URL), isNull(), eq(CLIENT_ID),
                eq(CLIENT_SECRET), eq(SCOPE), eq(false));
        assertThat(handles.getAllValues().get(1), not(handles.getAllValues().get(0)));
    }

    @Test
    void removalDeletesStoredCredentials() throws Exception {
        when(client.getAccessTokenResponse()).thenReturn(token());
        handler.initialize();
        executor.runAll();
        handler.handleRemoval();
        executor.runAll();
        verify(factory).deleteServiceAndAccessToken(anyString());
        assertThat(status().getStatus(), is(ThingStatus.REMOVED));
    }

    @Test
    void disposalDuringLoginReturnsPromptlyAndPreventsLatePublication() throws Exception {
        ExecutorService worker = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            return thread;
        });
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);
        var concurrentHandler = new VeluxActiveAccountHandler(bridge, factory, worker, scheduler,
                mock(VeluxActiveApi.class), () -> java.time.ZoneId.of("Europe/Copenhagen"));
        attachCallback(concurrentHandler);
        when(client.getAccessTokenByResourceOwnerPasswordCredentials("user", "password", SCOPE))
                .thenAnswer(invocation -> {
                    entered.countDown();
                    assertThat(release.await(10, TimeUnit.SECONDS), is(true));
                    return token();
                });
        doAnswer(invocation -> {
            closed.countDown();
            return null;
        }).when(factory).ungetOAuthService(anyString());
        try {
            concurrentHandler.initialize();
            assertThat(entered.await(10, TimeUnit.SECONDS), is(true));
            assertTimeoutPreemptively(Duration.ofSeconds(2), concurrentHandler::dispose);
            assertThat(closed.getCount(), is(1L));
            release.countDown();
            assertThat(closed.await(10, TimeUnit.SECONDS), is(true));
            assertThat(status().getStatus(), is(ThingStatus.UNKNOWN));
            verifyNoInteractions(scheduler);
        } finally {
            release.countDown();
            concurrentHandler.dispose();
            worker.shutdown();
            if (!worker.awaitTermination(10, TimeUnit.SECONDS)) {
                worker.shutdownNow();
            }
        }
    }

    @Test
    void pollsEachHomeOnceAndDistributesStatus() throws Exception {
        var first = gateway("home", "first");
        var second = gateway("home", "second");
        when(client.getAccessTokenResponse()).thenReturn(token());
        var response = new HomeStatus();
        when(api.homeStatus("access", "home")).thenReturn(response);
        handler.initialize();
        executor.runAll();
        verify(api, times(1)).homeStatus("access", "home");
        verify(first).update(any(), same(response));
        verify(second).update(any(), same(response));
        verify(scheduler).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void pollRequestedDuringStatusPublicationIsScheduledImmediately() throws Exception {
        var gateway = gateway("home", "gateway");
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        doAnswer(invocation -> {
            handler.requestPoll();
            return null;
        }).when(gateway).update(any(), any());
        handler.initialize();
        executor.runAll();
        verify(scheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
    }

    @Test
    void pollFailureRecoversOnTheNextRequest() throws Exception {
        var gateway = gateway("home", "gateway");
        when(client.getAccessTokenResponse()).thenReturn(token());
        var response = new HomeStatus();
        when(api.homeStatus("access", "home")).thenThrow(new ApiException("Unavailable")).thenReturn(response);
        handler.initialize();
        executor.runAll();
        verify(gateway).failed(any(), eq("@text/offline.comm-error.module.status-error"));
        handler.requestPoll();
        handler.requestPoll();
        executor.runAll();
        verify(api, times(2)).homeStatus("access", "home");
        verify(gateway).update(any(), same(response));
    }

    @Test
    void rejectedApiTokenIsRefreshedAndPollingRecovers() throws Exception {
        gateway("home", "gateway");
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(client.refreshToken()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenThrow(new ApiUnauthorizedException("Rejected"))
                .thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        verify(client).refreshToken();
        handler.requestPoll();
        executor.runAll();
        verify(api, times(2)).homeStatus("access", "home");
    }

    @Test
    void disposalDuringPollPreventsPublicationAndRescheduling() throws Exception {
        var gateway = gateway("home", "gateway");
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenAnswer(invocation -> {
            handler.dispose();
            return new HomeStatus();
        });
        handler.initialize();
        executor.runAll();
        verify(gateway, never()).update(any(), any());
        verify(scheduler, never()).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
        handler.requestPoll();
        executor.runAll();
        verify(api, times(1)).homeStatus("access", "home");
    }

    @Test
    void signedUnlockUsesServerTimeAndAcknowledgesAfterSuccess() throws Exception {
        var gateway = gateway("home", "gateway");
        var requestContext = Objects.requireNonNull(gateway.captureRequestContext());
        when(gateway.isRequestContextCurrent(requestContext)).thenReturn(true);
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        when(api.signingTime()).thenReturn(1700000000L);
        var signature = java.util.Map.<String, Object> of("hash_scenario", "signature");
        when(gateway.sign(requestContext, "gateway", "scenario", "home", 1700000000L)).thenReturn(signature);
        handler.initialize();
        executor.runAll();
        clearInvocations(api, gateway);
        handler.unlock(requestContext);
        executor.runAll();
        var order = inOrder(api, gateway);
        order.verify(gateway).sign(requestContext, "gateway", "scenario", "home", 1700000000L);
        order.verify(api).unlock("access", "home", "gateway", "Europe/Copenhagen", signature);
        order.verify(gateway).unlockAccepted(requestContext);
        doThrow(new ApiException("Rejected")).when(api).unlock(anyString(), anyString(), anyString(), anyString(),
                anyMap());
        clearInvocations(gateway);
        handler.unlock(requestContext);
        executor.runAll();
        verify(gateway, never()).unlockAccepted(any());
    }

    @Test
    void lockCommandUsesConfiguredTimezoneAndPollsForConfirmation() throws Exception {
        var gateway = gateway("home", "gateway");
        var requestContext = Objects.requireNonNull(gateway.captureRequestContext());
        when(gateway.isRequestContextCurrent(requestContext)).thenReturn(true);
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        clearInvocations(api, gateway, future);
        handler.lock(requestContext);
        executor.runAll();
        var order = inOrder(api, gateway);
        order.verify(api).lock("access", "home", "gateway", "Europe/Copenhagen");
        order.verify(gateway).lockAccepted(requestContext);
        order.verify(api).homeStatus("access", "home");
        verify(future).cancel(false);
        verify(scheduler).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
    }

    @Test
    void rejectedLockDoesNotPublishAcknowledgement() throws Exception {
        var gateway = gateway("home", "gateway");
        var requestContext = Objects.requireNonNull(gateway.captureRequestContext());
        when(gateway.isRequestContextCurrent(requestContext)).thenReturn(true);
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        doThrow(new ApiException("Rejected")).when(api).lock(anyString(), anyString(), anyString(), anyString());
        handler.lock(requestContext);
        executor.runAll();
        verify(gateway, never()).lockAccepted(any());
        verify(gateway).failed(requestContext, "@text/offline.comm-error.module.command-error");
    }

    @Test
    void disposalDuringLockSuppressesAcknowledgementAndRefresh() throws Exception {
        var gateway = gateway("home", "gateway");
        var requestContext = Objects.requireNonNull(gateway.captureRequestContext());
        when(gateway.isRequestContextCurrent(requestContext)).thenReturn(true);
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        clearInvocations(api, gateway);
        doAnswer(invocation -> {
            handler.dispose();
            return null;
        }).when(api).lock(anyString(), anyString(), anyString(), anyString());
        handler.lock(requestContext);
        executor.runAll();
        verify(gateway, never()).lockAccepted(any());
        verify(api, never()).homeStatus(anyString(), anyString());
    }

    @Test
    void queuedLockIsDiscardedWhenGatewayWasDisposed() throws Exception {
        var gateway = gateway("home", "gateway");
        var requestContext = Objects.requireNonNull(gateway.captureRequestContext());
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        handler.lock(requestContext);
        when(gateway.isRequestContextCurrent(requestContext)).thenReturn(false);
        executor.runAll();
        verify(api, never()).lock(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sensorAndGatewayShareHomeStatusAndTopologyFailureDoesNotBlockDiagnostics() throws Exception {
        var gateway = gateway("home", "gateway");
        var sensor = mock(VeluxActiveIndoorSensorHandler.class);
        var requestContext = new ModuleRequestContext(sensor, "home", "sensor", 1, null);
        when(sensor.captureRequestContext()).thenReturn(requestContext);
        var child = mock(org.openhab.core.thing.Thing.class);
        when(child.getHandler()).thenReturn(sensor);
        Bridge parent = gateway.getThing();
        var children = new ArrayList<>(parent.getThings());
        children.add(child);
        doReturn(children).when(parent).getThings();
        when(client.getAccessTokenResponse()).thenReturn(token());
        var response = new HomeStatus();
        when(api.homeStatus("access", "home")).thenReturn(response);
        var topology = List
                .of(new ModuleInfo("home", "sensor", "Sensor", ModuleType.INDOOR_SENSOR, "room", "Room", null));
        when(api.modules("access")).thenThrow(new ApiException("Unavailable")).thenReturn(topology);
        handler.initialize();
        executor.runAll();
        verify(api, times(1)).homeStatus("access", "home");
        verify(gateway).update(any(), same(response));
        verify(sensor).update(requestContext, response);
        verify(sensor, never()).updateTopology(any(), any());
        handler.requestPoll();
        executor.runAll();
        var order = inOrder(sensor);
        order.verify(sensor).updateTopology(requestContext, topology);
        order.verify(sensor).update(requestContext, response);
    }

    @Test
    void openingAndGatewaySharePollingAndCommandsTriggerFastPoll() throws Exception {
        var gateway = gateway("home", "gateway");
        var opening = opening();
        var requestContext = Objects.requireNonNull(opening.captureRequestContext());
        when(client.getAccessTokenResponse()).thenReturn(token());
        var response = new HomeStatus();
        when(api.homeStatus("access", "home")).thenReturn(response);
        handler.initialize();
        executor.runAll();
        verify(api, times(1)).homeStatus("access", "home");
        verify(opening).update(requestContext, response);
        verify(gateway).update(any(), same(response));
        clearInvocations(api, scheduler);
        handler.setPosition(requestContext, "gateway", 30);
        executor.runAll();
        var order = inOrder(api, opening);
        order.verify(api).setPosition("access", "home", "opening", "gateway", 30, "Europe/Copenhagen");
        order.verify(opening).positionAccepted(requestContext, 30);
        order.verify(api).homeStatus("access", "home");
        verify(scheduler).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS));
        clearInvocations(opening);
        handler.stopMovements(requestContext, "gateway");
        executor.runAll();
        verify(opening, never()).positionAccepted(any(), anyInt());
        verify(api).stopMovements("access", "home", "gateway", "Europe/Copenhagen");
    }

    @Test
    void openingCommandFailureRecoversAndRefreshesRejectedAuthentication() throws Exception {
        var opening = opening();
        var requestContext = Objects.requireNonNull(opening.captureRequestContext());
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(client.refreshToken()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        doThrow(new ApiUnauthorizedException("Rejected")).doNothing().when(api).setPosition("access", "home", "opening",
                "gateway", 30, "Europe/Copenhagen");
        handler.setPosition(requestContext, "gateway", 30);
        executor.runAll();
        verify(opening).failed(requestContext, "@text/offline.comm-error.module.command-error");
        verify(opening, never()).positionAccepted(any(), anyInt());
        verify(client).refreshToken();
        clearInvocations(opening);
        handler.setPosition(requestContext, "gateway", 30);
        executor.runAll();
        verify(opening).update(eq(requestContext), any());
        verify(opening, never()).failed(any(), any());
    }

    @Test
    void queuedOpeningCommandsAreDiscardedAfterChildDisposal() throws Exception {
        var opening = opening();
        var requestContext = Objects.requireNonNull(opening.captureRequestContext());
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        handler.setPosition(requestContext, "gateway", 30);
        handler.stopMovements(requestContext, "gateway");
        when(opening.isRequestContextCurrent(requestContext)).thenReturn(false);
        executor.runAll();
        verify(api, never()).setPosition(anyString(), anyString(), anyString(), anyString(), anyInt(), anyString());
        verify(api, never()).stopMovements(anyString(), anyString(), anyString(), anyString());
        verify(opening, never()).positionAccepted(any(), anyInt());
    }

    @Test
    void accountDisposalDuringMovementSuppressesFollowUpPoll() throws Exception {
        var opening = opening();
        var requestContext = Objects.requireNonNull(opening.captureRequestContext());
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.homeStatus("access", "home")).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        clearInvocations(api);
        doAnswer(invocation -> {
            handler.dispose();
            return null;
        }).when(api).setPosition(anyString(), anyString(), anyString(), anyString(), anyInt(), anyString());
        handler.setPosition(requestContext, "gateway", 30);
        executor.runAll();
        verify(api, never()).homeStatus(anyString(), anyString());
        verify(opening, never()).positionAccepted(any(), anyInt());
    }

    @Test
    void resolvesHomeBeforePollingAndRetainsMappingThroughTopologyFailure() throws Exception {
        var gateway = realGateway();
        when(client.getAccessTokenResponse()).thenReturn(token());
        var topology = List
                .of(new ModuleInfo("resolved-home", "gateway", "Gateway", ModuleType.GATEWAY, null, null, null));
        when(api.modules("access")).thenThrow(new ApiException("Unavailable")).thenReturn(topology)
                .thenThrow(new ApiException("Unavailable"));
        when(api.homeStatus("access", "resolved-home")).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        assertThat(handler.homeId("gateway"), is(""));
        verify(api, never()).homeStatus(anyString(), anyString());
        handler.lock(Objects.requireNonNull(gateway.captureRequestContext()));
        executor.runAll();
        verify(api, never()).lock(anyString(), anyString(), anyString(), anyString());

        handler.requestPoll();
        executor.runAll();
        assertThat(handler.homeId("gateway"), is("resolved-home"));
        verify(api).homeStatus("access", "resolved-home");
        handler.requestPoll();
        executor.runAll();
        verify(api, times(2)).homeStatus("access", "resolved-home");
        assertThat(handler.homeId("gateway"), is("resolved-home"));
    }

    @Test
    void movedOrMissingGatewayInvalidatesOldCommandTargetsAndRecovers() throws Exception {
        var gateway = realGateway();
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.modules("access")).thenReturn(
                List.of(new ModuleInfo("first", "gateway", "Gateway", ModuleType.GATEWAY, null, null, null)),
                List.of(new ModuleInfo("second", "gateway", "Gateway", ModuleType.GATEWAY, null, null, null)),
                List.of(),
                List.of(new ModuleInfo("second", "gateway", "Gateway", ModuleType.GATEWAY, null, null, null)));
        when(api.homeStatus(anyString(), anyString())).thenReturn(new HomeStatus());
        handler.initialize();
        executor.runAll();
        var old = Objects.requireNonNull(gateway.captureRequestContext());
        handler.requestPoll();
        handler.lock(old);
        executor.runAll();
        verify(api, never()).lock(anyString(), anyString(), anyString(), anyString());
        verify(api).homeStatus("access", "second");
        assertThat(gateway.isRequestContextCurrent(old), is(false));
        handler.requestPoll();
        executor.runAll();
        assertThat(handler.homeId("gateway"), is(""));
        assertThat(gateway.getThing().getStatus(), is(ThingStatus.OFFLINE));
        handler.requestPoll();
        executor.runAll();
        assertThat(handler.homeId("gateway"), is("second"));
        verify(api, times(2)).homeStatus("access", "second");
        handler.dispose();
        assertThat(handler.homeId("gateway"), is(""));
    }

    @Test
    void ambiguousGatewayHomeIsNotUsed() throws Exception {
        realGateway();
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.modules("access"))
                .thenReturn(List.of(new ModuleInfo("first", "gateway", "Gateway", ModuleType.GATEWAY, null, null, null),
                        new ModuleInfo("second", "gateway", "Gateway", ModuleType.GATEWAY, null, null, null)));
        handler.initialize();
        executor.runAll();
        verify(api, never()).homeStatus(anyString(), anyString());
        assertThat(handler.homeId("gateway"), is(""));
        verify(scheduler).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void disposalDuringTopologyFetchDiscardsAssociation() throws Exception {
        realGateway();
        when(client.getAccessTokenResponse()).thenReturn(token());
        when(api.modules("access")).thenAnswer(invocation -> {
            handler.dispose();
            return List.of(new ModuleInfo("home", "gateway", "Gateway", ModuleType.GATEWAY, null, null, null));
        });
        handler.initialize();
        executor.runAll();
        assertThat(handler.homeId("gateway"), is(""));
        verify(api, never()).homeStatus(anyString(), anyString());
    }

    private VeluxActiveGatewayHandler realGateway() {
        Bridge gatewayThing = spy(
                BridgeBuilder.create(THING_TYPE_GATEWAY, "gateway").withBridge(bridge.getUID())
                        .withConfiguration(
                                new Configuration(Map.of(VeluxActiveBindingConstants.PARAMETER_MODULE_ID, "gateway")))
                        .build());
        var gateway = new VeluxActiveGatewayHandler(gatewayThing, mock(SigningKeyStore.class));
        when(gatewayThing.getHandler()).thenReturn(gateway);
        when(bridge.getHandler()).thenReturn(handler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        doReturn(List.of(gatewayThing)).when(bridge).getThings();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(bridge.getUID())).thenReturn(bridge);
        doAnswer(invocation -> {
            gatewayThing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
        gateway.setCallback(callback);
        gateway.initialize();
        return gateway;
    }

    private VeluxActiveOpeningHandler opening() {
        Bridge parent = bridge.getThings().isEmpty() ? gateway("home", "gateway").getThing()
                : (Bridge) bridge.getThings().getFirst();
        var opening = mock(VeluxActiveOpeningHandler.class);
        var requestContext = new ModuleRequestContext(opening, "home", "opening", 1, null);
        when(opening.captureRequestContext()).thenReturn(requestContext);
        when(opening.isRequestContextCurrent(requestContext)).thenReturn(true);
        var child = mock(org.openhab.core.thing.Thing.class);
        when(child.getHandler()).thenReturn(opening);
        var children = new ArrayList<>(parent.getThings());
        children.add(child);
        doReturn(children).when(parent).getThings();
        return opening;
    }

    private VeluxActiveGatewayHandler gateway(String homeId, String moduleId) {
        var gateway = mock(VeluxActiveGatewayHandler.class);
        when(gateway.captureRequestContext()).thenReturn(new ModuleRequestContext(gateway, homeId, moduleId, 1, null));
        var child = mock(Bridge.class);
        when(child.getHandler()).thenReturn(gateway);
        when(gateway.getThing()).thenReturn(child);
        var children = new ArrayList<>(bridge.getThings());
        children.add(child);
        doReturn(children).when(bridge).getThings();
        return gateway;
    }

    private void attachCallback(VeluxActiveAccountHandler accountHandler) {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        doAnswer(invocation -> {
            statuses.add(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
        accountHandler.setCallback(callback);
    }

    private ThingStatusInfo status() {
        return statuses.getLast();
    }

    private static Bridge bridge(String username, String password) {
        return BridgeBuilder.create(THING_TYPE_ACCOUNT, "test")
                .withConfiguration(new Configuration(Map.of("username", username, "password", password))).build();
    }

    private static AccessTokenResponse token() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken("access");
        response.setRefreshToken("refresh");
        response.setExpiresIn(3600);
        response.setCreatedOn(Instant.now());
        return response;
    }

    private static OAuthResponseException oAuthError(String code) {
        OAuthResponseException error = new OAuthResponseException();
        error.setError(code);
        return error;
    }

    private static class ManualExecutor implements Executor {
        private final Deque<Runnable> tasks = new ArrayDeque<>();

        @Override
        public void execute(Runnable task) {
            tasks.addLast(task);
        }

        void runAll() {
            int count = 0;
            while (!tasks.isEmpty()) {
                assertThat("Unexpected unbounded work queue", ++count < 100, is(true));
                tasks.removeFirst().run();
            }
        }
    }
}
