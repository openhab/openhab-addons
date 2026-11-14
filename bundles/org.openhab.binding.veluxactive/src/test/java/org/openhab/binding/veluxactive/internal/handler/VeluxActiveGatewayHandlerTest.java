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

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants;
import org.openhab.binding.veluxactive.internal.action.VeluxActivePairingActions;
import org.openhab.binding.veluxactive.internal.api.ModuleInfo;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.crypto.NetcomPairing;
import org.openhab.binding.veluxactive.internal.crypto.SigningKey;
import org.openhab.binding.veluxactive.internal.crypto.SigningKeyStore;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Verifies gateway state, command semantics and stale callback suppression.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveGatewayHandlerTest {
    private final ThingUID bridgeUID = new ThingUID(THING_TYPE_ACCOUNT, "test");
    private final Bridge thing = BridgeBuilder.create(THING_TYPE_GATEWAY, "gateway").withBridge(bridgeUID)
            .withConfiguration(new Configuration(Map.of(VeluxActiveBindingConstants.PARAMETER_MODULE_ID, "gateway")))
            .build();
    private final Bridge bridge = mock(Bridge.class);
    private final VeluxActiveAccountHandler account = mock(VeluxActiveAccountHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final SigningKeyStore signingKeys = mock(SigningKeyStore.class);
    private final VeluxActiveGatewayHandler handler = new VeluxActiveGatewayHandler(thing, signingKeys);
    private final Map<String, State> states = new HashMap<>();

    @Test
    @Timeout(15)
    void pairingActionPersistsVerifiedKeyAndEnablesUnlock() throws Exception {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","firmware_revision_netatmo":203}]}}
                """);
        var stored = new CompletableFuture<SigningKey>();
        doAnswer(i -> {
            stored.complete(i.getArgument(1));
            return null;
        }).when(signingKeys).put(eq("gateway"), any());
        var actions = new VeluxActivePairingActions();
        actions.setThingHandler(handler);
        var key = new SigningKey(new byte[16], new byte[32]);
        try (var mocked = mockConstruction(NetcomPairing.class,
                (session, context) -> when(session.attempt()).thenReturn(key))) {
            assertThat(actions.pair("192.0.2.1").startsWith("STARTED"), is(true));
            assertThat(actions.pair("192.0.2.1"), is("IN_PROGRESS"));
            var accepted = ArgumentCaptor.forClass(Runnable.class);
            verify(account).retrieveKey(any(), accepted.capture(), any());
            accepted.getValue().run();
            assertThat(stored.get(10, TimeUnit.SECONDS), is(key));
            assertThat(actions.pairingStatus(), is("PAIRED"));
            verify(mocked.constructed().getFirst()).close();
        } finally {
            handler.dispose();
        }
    }

    @Test
    void disposalCancelsPairingAndSuppressesLateCloudAcceptance() throws Exception {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","firmware_revision_netatmo":203}]}}
                """);
        try (var mocked = mockConstruction(NetcomPairing.class)) {
            handler.pair("192.0.2.1");
            var accepted = ArgumentCaptor.forClass(Runnable.class);
            verify(account).retrieveKey(any(), accepted.capture(), any());
            handler.dispose();
            accepted.getValue().run();
            verify(mocked.constructed().getFirst()).close();
            verify(mocked.constructed().getFirst(), never()).attempt();
            verify(signingKeys, never()).put(anyString(), any());
            assertThat(handler.pairingStatus(), is("CANCELLED"));
        }
    }

    @Test
    void pairedGatewayRoutesOffAndOnlyPublishesAfterAcceptance() {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","locked":true,"firmware_revision_netatmo":203}]}}
                """);
        when(signingKeys.contains("gateway")).thenReturn(true);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LOCKED), OnOffType.OFF);
        verify(account).unlock(any());
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.ON));
        handler.unlockAccepted(requireNonNull(handler.captureRequestContext()));
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.OFF));
    }

    @Test
    void removalDeletesCredentialsAfterDisposal() {
        handler.dispose();
        handler.handleRemoval();
        verify(signingKeys).removeIfUnowned("gateway", thing.getUID());
    }

    @BeforeEach
    void setup() {
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(account);
        when(account.homeId("gateway")).thenReturn("home");
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        doAnswer(invocation -> {
            ChannelUID channel = invocation.getArgument(0);
            states.put(channel.getId(), invocation.getArgument(1));
            return null;
        }).when(callback).stateUpdated(any(), any());
        doAnswer(invocation -> {
            ThingStatusInfo status = invocation.getArgument(1);
            thing.setStatusInfo(status);
            return null;
        }).when(callback).statusUpdated(any(), any());
        handler.setCallback(callback);
        handler.initialize();
    }

    @Test
    void publishesOnlyReportedState() {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","locked":true,"locking":false,
                "last_seen":1791635715}]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.ON));
        assertThat(states.get(CHANNEL_LAST_SEEN), not(UnDefType.UNDEF));
        update("{\"home\":{\"id\":\"home\",\"modules\":[{\"id\":\"gateway\"}]}}");
        assertThat(states.get(CHANNEL_LOCKED), is(UnDefType.UNDEF));
        assertThat(states.get(CHANNEL_LAST_SEEN), is(UnDefType.UNDEF));
    }

    @Test
    void unreachableErrorOverridesPresentModuleAndRecovers() {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","locked":true}]},
                 "errors":[{"id":"gateway","code":6}]}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(states.get(CHANNEL_LOCKED), is(UnDefType.UNDEF));
        update("{\"home\":{\"id\":\"home\",\"modules\":[{\"id\":\"gateway\",\"locked\":false}]}}");
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.OFF));
    }

    @Test
    void missingGatewayBeforeFirstStatusIsOfflineNotUnlocked() {
        update("{\"home\":{\"id\":\"home\",\"modules\":[]}}");
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(states.get(CHANNEL_LOCKED), is(UnDefType.UNDEF));
    }

    @Test
    void onlyOnOnLockedChannelSendsCommandWithoutOptimisticState() {
        clearInvocations(account);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LAST_SEEN), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LOCKED), OnOffType.OFF);
        verify(account, never()).lock(any());
        verify(account, never()).requestPoll();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LOCKED), OnOffType.ON);
        verify(account).lock(any());
        assertThat(states.containsKey(CHANNEL_LOCKED), is(false));
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LOCKED), RefreshType.REFRESH);
        verify(account).requestPoll();
    }

    @Test
    void disposalSuppressesLateStatusAndCommands() {
        var requestContext = requireNonNull(handler.captureRequestContext());
        handler.dispose();
        clearInvocations(callback, account);
        handler.update(requestContext, status("{\"home\":{\"id\":\"home\",\"modules\":[]}}"));
        handler.failed(requestContext, "late failure");
        handler.lockAccepted(requestContext);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LOCKED), OnOffType.ON);
        verifyNoInteractions(callback, account);
    }

    @Test
    void acknowledgedLockUpdatesChannelAndPollingReconcilesIt() {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","locked":false,"locking":false}]}}
                """);
        handler.lockAccepted(requireNonNull(handler.captureRequestContext()));
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.ON));
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","locked":false}]}}
                """);
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.OFF));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void acknowledgedLockDoesNotBounceWhileLockingAndReconcilesCompletion(boolean completed) {
        handler.lockAccepted(requireNonNull(handler.captureRequestContext()));
        for (int poll = 0; poll < 2; poll++) {
            update("""
                    {"home":{"modules":[{"id":"gateway","locked":false,"locking":true}]}}
                    """);
            assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.ON));
        }
        update("""
                {"home":{"modules":[{"id":"gateway","locked":%s,"locking":false}]}}
                """.formatted(completed));
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.from(completed)));
        update("""
                {"home":{"modules":[{"id":"gateway","locked":false,"locking":true}]}}
                """);
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.OFF));
    }

    @Test
    void unlockingClearsPendingLock() {
        var context = requireNonNull(handler.captureRequestContext());
        handler.lockAccepted(context);
        handler.unlockAccepted(context);
        update("""
                {"home":{"modules":[{"id":"gateway","locked":false,"locking":true}]}}
                """);
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.OFF));
    }

    @Test
    void bridgeStatusChangeClearsPendingLock() {
        handler.lockAccepted(requireNonNull(handler.captureRequestContext()));
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        update("""
                {"home":{"modules":[{"id":"gateway","locked":false,"locking":true}]}}
                """);
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.OFF));
    }

    @Test
    void unreachableGatewayClearsPendingLock() {
        handler.lockAccepted(requireNonNull(handler.captureRequestContext()));
        update("""
                {"home":{"modules":[{"id":"gateway","reachable":false}]}}
                """);
        assertThat(states.get(CHANNEL_LOCKED), is(UnDefType.UNDEF));
        update("""
                {"home":{"modules":[{"id":"gateway","locked":false,"locking":true}]}}
                """);
        assertThat(states.get(CHANNEL_LOCKED), is(OnOffType.OFF));
    }

    @Test
    void publishesFirmwarePropertiesAndRetainsThemAcrossPartialResponses() {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","firmware_revision_netatmo":203,
                "firmware_revision_thirdparty":"0.1.2.0.43.0","hardware_version":236}]}}
                """);
        assertThat(thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("203"));
        assertThat(thing.getProperties().get(Thing.PROPERTY_HARDWARE_VERSION), is("236"));
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","locked":false}]}}
                """);
        assertThat(thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("203"));
    }

    @Test
    void discoveryFiltersByBothHomeAndGateway() {
        doAnswer(invocation -> {
            Consumer<List<ModuleInfo>> consumer = invocation.getArgument(0);
            consumer.accept(
                    List.of(new ModuleInfo("home", "sensor", "Sensor", ModuleType.INDOOR_SENSOR, null, null, "gateway"),
                            new ModuleInfo("home", "other", "Other", ModuleType.INDOOR_SENSOR, null, null,
                                    "other-gateway"),
                            new ModuleInfo("other-home", "foreign", "Foreign", ModuleType.INDOOR_SENSOR, null, null,
                                    "gateway")));
            return null;
        }).when(account).discover(any());
        var results = new ArrayList<ModuleInfo>();
        handler.discover(results::addAll);
        assertThat(results.size(), is(1));
        assertThat(results.getFirst().moduleId(), is("sensor"));
    }

    @ParameterizedTest
    @EnumSource(value = ThingStatus.class, names = { "UNKNOWN", "ONLINE", "OFFLINE" })
    void missingModuleDoesNotResetConnectivityAndRecovers(ThingStatus initialStatus) {
        if (initialStatus != ThingStatus.UNKNOWN) {
            update("{\"home\":{\"modules\":[{\"id\":\"gateway\",\"reachable\":true}]}}");
            if (initialStatus == ThingStatus.OFFLINE) {
                handler.failed(requireNonNull(handler.captureRequestContext()), "failure");
            }
        }
        assertThat(thing.getStatus(), is(initialStatus));
        update("{\"home\":{\"modules\":[]}}");
        assertThat(thing.getStatus(),
                is(initialStatus == ThingStatus.ONLINE ? ThingStatus.ONLINE : ThingStatus.OFFLINE));
        assertThat(states.values().stream().allMatch(UnDefType.UNDEF::equals), is(true));
        update("{\"home\":{\"modules\":[{\"id\":\"gateway\",\"reachable\":true}]}}");
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(thing.getStatusInfo().getDescription(), is((String) null));
    }

    @Test
    void bridgeRecoveryWaitsForStatusWithoutReturningToUnknown() {
        update("{\"home\":{\"modules\":[{\"id\":\"gateway\",\"reachable\":true}]}}");
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);
        handler.bridgeStatusChanged(
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, null));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(thing.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.BRIDGE_OFFLINE));
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        update("{\"home\":{\"modules\":[{\"id\":\"gateway\",\"reachable\":true}]}}");
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
    }

    @Test
    void homeChangesPreserveOnlineButUnresolvedHomeRequiresStatusRecovery() {
        update("{\"home\":{\"modules\":[{\"id\":\"gateway\",\"locked\":true}]}}");
        when(account.homeId("gateway")).thenReturn("other-home");
        handler.homeResolved(true);
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(states.get(CHANNEL_LOCKED), is(UnDefType.UNDEF));
        when(account.homeId("gateway")).thenReturn("");
        handler.homeResolved(true);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(thing.getStatusInfo().getDescription(), is("@text/offline.comm-error.gateway.home-unresolved"));
        when(account.homeId("gateway")).thenReturn("other-home");
        handler.homeResolved(true);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        update("{\"home\":{\"modules\":[{\"id\":\"gateway\",\"locked\":false}]}}");
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
    }

    private void update(String json) {
        handler.update(requireNonNull(handler.captureRequestContext()), status(json));
    }

    private static HomeStatus status(String json) {
        return requireNonNull(new Gson().fromJson(json, HomeStatus.class));
    }
}
