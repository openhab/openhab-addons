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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.openhab.binding.veluxactive.internal.ModuleRequestContext;
import org.openhab.binding.veluxactive.internal.api.VeluxType;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Verifies NXO position conventions, unsigned commands and lifecycle handling.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveOpeningHandlerTest {
    private final ThingUID bridgeUID = new ThingUID(THING_TYPE_GATEWAY, "test");
    private final Thing thing = ThingBuilder.create(THING_TYPE_OPENING, "opening").withBridge(bridgeUID)
            .withConfiguration(new Configuration(Map.of("moduleId", "opening"))).build();
    private final Bridge bridge = mock(Bridge.class);
    private final VeluxActiveGatewayHandler gateway = mock(VeluxActiveGatewayHandler.class);
    private final VeluxActiveAccountHandler account = mock(VeluxActiveAccountHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final VeluxActiveOpeningHandler handler = new VeluxActiveOpeningHandler(thing);
    private final Map<String, State> states = new HashMap<>();

    @BeforeEach
    void setup() {
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(gateway);
        when(gateway.getThing()).thenReturn(bridge);
        when(gateway.account()).thenReturn(account);
        when(gateway.captureRequestContext()).thenReturn(new ModuleRequestContext(gateway, "home", "gateway", 1, null));
        when(gateway.isRequestContextCurrent(any())).thenReturn(true);
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

    @ParameterizedTest
    @EnumSource(value = VeluxType.class, names = { "WINDOW", "SHUTTER" })
    void reportsWindowPositionsAndProperties(VeluxType type) {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","type":"NXO","velux_type":"%s",
                 "bridge":"gateway","current_position":20,"target_position":80,"mode":"algo_available",
                 "firmware_revision":53,"manufacturer":"Netatmo","last_seen":1791635702,"reachable":true}]}}
                """.formatted(type.serializedName()));
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(states.get(CHANNEL_POSITION), is(new PercentType(80)));
        assertThat(states.get(CHANNEL_MODE), is(new StringType("algo-available")));
        assertThat(states.get(CHANNEL_LAST_SEEN), is(new DateTimeType(Instant.ofEpochSecond(1791635702L))));
        assertThat(thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("53"));
        assertThat(thing.getProperties().get(Thing.PROPERTY_VENDOR), is("Netatmo"));
        assertThat(thing.getProperties().get(PROPERTY_VELUX_TYPE), is(type.toCamelCase()));
    }

    @ParameterizedTest
    @EnumSource(value = VeluxType.class, names = { "BLIND", "VENETIAN_BLIND", "AWNING_BLIND" })
    void preservesCoverPositionsAndSupportsUnsignedMovement(VeluxType type) {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"%s",
                 "bridge":"gateway","current_position":20,"target_position":80}]}}
                """.formatted(type.serializedName()));
        assertThat(states.get(CHANNEL_POSITION), is(new PercentType(20)));
        clearInvocations(account, callback);
        command(UpDownType.UP);
        command(UpDownType.DOWN);
        command(new PercentType(45));
        var requestContext = requireNonNull(handler.captureRequestContext());
        verify(account).setPosition(requestContext, "gateway", 0);
        verify(account).setPosition(requestContext, "gateway", 100);
        verify(account).setPosition(requestContext, "gateway", 45);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void allowsWindowClosingButDoesNotAttemptUnsignedOpening() {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"window","bridge":"gateway",
                 "current_position":100,"target_position":0}]}}
                """);
        assertThat(states.get(CHANNEL_POSITION), is(new PercentType(0)));
        clearInvocations(account);
        command(UpDownType.UP);
        command(new PercentType(50));
        verifyNoInteractions(account);
        command(UpDownType.DOWN);
        command(new PercentType(100));
        verify(account, times(2)).setPosition(requireNonNull(handler.captureRequestContext()), "gateway", 0);
    }

    @Test
    void stopTargetsTheGatewayAndRefreshUsesSharedPolling() {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"window","bridge":"gateway"}]}}
                """);
        clearInvocations(account);
        command(StopMoveType.STOP);
        verify(account).stopMovements(requireNonNull(handler.captureRequestContext()), "gateway");
        command(RefreshType.REFRESH);
        verify(account).requestPoll();
        clearInvocations(account);
        command(StopMoveType.MOVE);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_MODE), UpDownType.UP);
        verifyNoInteractions(account);
    }

    @Test
    void missingOrUnsupportedTypeDoesNotGuessPositionDirection() {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","bridge":"gateway","current_position":25}]}}
                """);
        assertThat(states.get(CHANNEL_POSITION), is(UnDefType.UNDEF));
        clearInvocations(account);
        command(UpDownType.DOWN);
        verifyNoInteractions(account);
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"future_type",
                 "current_position":25,"target_position":75}]}}
                """);
        assertThat(states.get(CHANNEL_POSITION), is(UnDefType.UNDEF));
        command(new PercentType(30));
        verifyNoInteractions(account);
    }

    @Test
    void inheritsGatewayRoutingEvenWhenStatusOmitsBridge() {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"blind","current_position":0}]}}
                """);
        clearInvocations(account);
        command(UpDownType.DOWN);
        command(StopMoveType.STOP);
        verify(account).setPosition(requireNonNull(handler.captureRequestContext()), "gateway", 100);
        verify(account).stopMovements(requireNonNull(handler.captureRequestContext()), "gateway");
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","bridge":"gateway","current_position":100}]}}
                """);
        assertThat(states.get(CHANNEL_POSITION), is(new PercentType(100)));
        command(UpDownType.UP);
        verify(account).setPosition(requireNonNull(handler.captureRequestContext()), "gateway", 0);
    }

    @Test
    void invalidOrMissingValuesAreUndefined() {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"window","current_position":-1,
                 "target_position":101,"last_seen":9223372036854775807}]}}
                """);
        assertThat(states.size(), is(4));
        assertThat(states.values().stream().allMatch(UnDefType.UNDEF::equals), is(true));
        update("""
                {"home":{"id":"home","modules":[{"id":"opening"}]}}
                """);
        assertThat(states.values().stream().allMatch(UnDefType.UNDEF::equals), is(true));
    }

    @Test
    void reportsBatteryStateAndClearsItWhenMissing() {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"shutter",
                 "battery_state":"very_low","reachable":true}]}}
                """);
        assertThat(states.get(CHANNEL_BATTERY_STATE), is(new StringType("very-low")));

        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"shutter",
                 "reachable":true}]}}
                """);
        assertThat(states.get(CHANNEL_BATTERY_STATE), is(UnDefType.UNDEF));
    }

    @Test
    void unreachableMissingAndRecoveredOpening() {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"shutter",
                 "bridge":"gateway","current_position":35,"reachable":true}]}}
                """);
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","reachable":false}]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(states.values().stream().allMatch(UnDefType.UNDEF::equals), is(true));
        clearInvocations(account);
        command(UpDownType.UP);
        verifyNoInteractions(account);
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","reachable":true}]},
                 "errors":[{"id":"opening","code":6}]}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        update("""
                {"home":{"id":"home","modules":[]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","current_position":35,"reachable":true}]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(states.get(CHANNEL_POSITION), is(new PercentType(65)));
    }

    @ParameterizedTest
    @EnumSource(value = VeluxType.class, names = { "WINDOW", "SHUTTER" })
    void acceptedTargetIsPublishedAndNextPollReconcilesActualPosition(VeluxType type) {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"%s",
                 "bridge":"gateway","current_position":20,"target_position":0}]}}
                """.formatted(type));
        var requestContext = requireNonNull(handler.captureRequestContext());
        handler.positionAccepted(requestContext, 0);
        assertThat(states.get(CHANNEL_POSITION), is(new PercentType(type.isInverted() ? 100 : 0)));
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","current_position":10,"target_position":0}]}}
                """);
        assertThat(states.get(CHANNEL_POSITION), is(new PercentType(type.isInverted() ? 90 : 10)));
    }

    @Test
    void disposalSuppressesLateUpdatesAndCommands() {
        var requestContext = requireNonNull(handler.captureRequestContext());
        handler.dispose();
        clearInvocations(callback, account);
        handler.update(requestContext, new HomeStatus());
        handler.failed(requestContext, "late failure");
        handler.positionAccepted(requestContext, 0);
        command(RefreshType.REFRESH);
        command(UpDownType.UP);
        command(StopMoveType.STOP);
        verifyNoInteractions(callback, account);
    }

    @Test
    void parentInvalidationSuppressesOldResponsesAndRecoveryUsesNewParent() {
        var oldRequestContext = requireNonNull(handler.captureRequestContext());
        when(gateway.isRequestContextCurrent(requireNonNull(oldRequestContext.parent()))).thenReturn(false);
        clearInvocations(callback);
        handler.update(oldRequestContext, new HomeStatus());
        handler.positionAccepted(oldRequestContext, 0);
        handler.failed(oldRequestContext, "late");
        verifyNoInteractions(callback);

        var parentRequestContext = new ModuleRequestContext(gateway, "other-home", "other-gateway", 2, null);
        when(gateway.captureRequestContext()).thenReturn(parentRequestContext);
        when(gateway.isRequestContextCurrent(parentRequestContext)).thenReturn(true);
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        assertThat(requireNonNull(handler.captureRequestContext()).homeId(), is("other-home"));
        update("""
                {"home":{"id":"other-home","modules":[{"id":"opening","velux_type":"blind",
                "bridge":"other-gateway","current_position":25}]}}
                """);
        command(UpDownType.DOWN);
        verify(account).setPosition(requireNonNull(handler.captureRequestContext()), "other-gateway", 100);
    }

    @Test
    void wrongGatewayAssignmentBlocksCommands() {
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"shutter",
                "bridge":"another-gateway","current_position":25}]}}
                """);
        assertThat(thing.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.CONFIGURATION_ERROR));
        clearInvocations(account);
        command(UpDownType.DOWN);
        verifyNoInteractions(account);
    }

    @Test
    void offlineGatewaySuppressesUpdatesUntilRecovery() {
        var requestContext = requireNonNull(handler.captureRequestContext());
        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);
        handler.bridgeStatusChanged(
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, null));
        assertThat(thing.getStatusInfo().getStatusDetail(), is(ThingStatusDetail.BRIDGE_OFFLINE));
        clearInvocations(callback);
        handler.positionAccepted(requestContext, 0);
        handler.update(requestContext, new HomeStatus());
        verifyNoInteractions(callback);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        update("""
                {"home":{"id":"home","modules":[{"id":"opening","velux_type":"blind","current_position":25}]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(states.get(CHANNEL_POSITION), is(new PercentType(25)));
    }

    @ParameterizedTest
    @CsvSource({ "algo_active,algo-active", "algo_available,algo-available", "algo_disabled,algo-disabled",
            "manual,manual", "unavailable,unavailable" })
    void publishesHyphenatedOperatingModes(String apiMode, String publishedMode) {
        update("""
                {"home":{"modules":[{"id":"opening","mode":"%s"}]}}
                """.formatted(apiMode));
        assertThat(states.get(CHANNEL_MODE), is(new StringType(publishedMode)));
    }

    @Test
    void pairedWindowOpeningUsesSignedCommand() {
        when(gateway.canSign()).thenReturn(true);
        update("""
                {"home":{"modules":[{"id":"opening","velux_type":"window","current_position":0}]}}
                """);
        command(new PercentType(25));
        verify(account).setSignedPosition(requireNonNull(handler.captureRequestContext()), 75);
        verify(account, never()).setPosition(any(), anyString(), anyInt());
    }

    private void command(Command command) {
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POSITION), command);
    }

    @ParameterizedTest
    @EnumSource(value = ThingStatus.class, names = { "UNKNOWN", "ONLINE", "OFFLINE" })
    void missingModuleDoesNotResetConnectivityAndRecovers(ThingStatus initialStatus) {
        if (initialStatus != ThingStatus.UNKNOWN) {
            update("{\"home\":{\"modules\":[{\"id\":\"opening\",\"reachable\":true}]}}");
            if (initialStatus == ThingStatus.OFFLINE) {
                handler.failed(requireNonNull(handler.captureRequestContext()), "failure");
            }
        }
        assertThat(thing.getStatus(), is(initialStatus));
        update("{\"home\":{\"modules\":[]}}");
        assertThat(thing.getStatus(),
                is(initialStatus == ThingStatus.ONLINE ? ThingStatus.ONLINE : ThingStatus.OFFLINE));
        assertThat(states.values().stream().allMatch(UnDefType.UNDEF::equals), is(true));
        update("{\"home\":{\"modules\":[{\"id\":\"opening\",\"reachable\":true}]}}");
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(thing.getStatusInfo().getDescription(), is((String) null));
    }

    @Test
    void bridgeRecoveryWaitsForStatusWithoutReturningToUnknown() {
        update("{\"home\":{\"modules\":[{\"id\":\"opening\",\"reachable\":true}]}}");
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
        update("{\"home\":{\"modules\":[{\"id\":\"opening\",\"reachable\":true}]}}");
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
    }

    private void update(String json) {
        handler.update(requireNonNull(handler.captureRequestContext()),
                requireNonNull(new Gson().fromJson(json, HomeStatus.class)));
    }
}
