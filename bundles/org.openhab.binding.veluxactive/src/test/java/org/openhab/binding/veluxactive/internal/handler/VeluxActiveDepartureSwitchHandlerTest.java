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
import org.junit.jupiter.params.provider.EnumSource;
import org.openhab.binding.veluxactive.internal.ModuleRequestContext;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Verifies departure switch diagnostics and lifecycle handling.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveDepartureSwitchHandlerTest {
    private final ThingUID bridgeUID = new ThingUID(THING_TYPE_GATEWAY, "test");
    private final Thing thing = ThingBuilder.create(THING_TYPE_DEPARTURE_SWITCH, "gateway").withBridge(bridgeUID)
            .withConfiguration(new Configuration(Map.of("moduleId", "gateway"))).build();
    private final Bridge bridge = mock(Bridge.class);
    private final VeluxActiveGatewayHandler gateway = mock(VeluxActiveGatewayHandler.class);
    private final VeluxActiveAccountHandler account = mock(VeluxActiveAccountHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final VeluxActiveDepartureSwitchHandler handler = new VeluxActiveDepartureSwitchHandler(thing);
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

    @Test
    void publishesObservedSwitchDiagnosticsWithoutRoomData() {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","type":"NXD","battery_percent":35,
                  "battery_state":"medium","rf_state":"medium","firmware_revision":16,
                  "reachable":true,"last_seen":1791635702}]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(states.get(CHANNEL_BATTERY), is(new DecimalType(35)));
        assertThat(states.get(CHANNEL_LOW_BATTERY), is(OnOffType.OFF));
        assertThat(states.get(CHANNEL_SIGNAL), is(new DecimalType(2)));
        assertThat(states.get(CHANNEL_LAST_SEEN), is(new DateTimeType(Instant.ofEpochSecond(1791635702L))));
        assertThat(thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("16"));
        assertThat(states.size(), is(4));
    }

    @Test
    void missingOrInvalidDiagnosticsBecomeUndefined() {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","battery_percent":101,
                  "battery_state":"unknown","rf_state":"unknown","last_seen":9223372036854775807}]}}
                """);
        assertThat(states.get(CHANNEL_BATTERY), is(UnDefType.UNDEF));
        assertThat(states.get(CHANNEL_LOW_BATTERY), is(UnDefType.UNDEF));
        assertThat(states.get(CHANNEL_SIGNAL), is(UnDefType.UNDEF));
        assertThat(states.get(CHANNEL_LAST_SEEN), is(UnDefType.UNDEF));
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","battery_percent":0,
                  "battery_state":"very_low","rf_state":"very_low"}]}}
                """);
        assertThat(states.get(CHANNEL_BATTERY), is(new DecimalType(0)));
        assertThat(states.get(CHANNEL_LOW_BATTERY), is(OnOffType.ON));
        assertThat(states.get(CHANNEL_SIGNAL), is(new DecimalType(0)));
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway"}]}}
                """);
        assertThat(states.values().stream().allMatch(UnDefType.UNDEF::equals), is(true));
    }

    @Test
    void unreachableAndMissingSwitchClearsDiagnosticsAndCanRecover() {
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","battery_percent":35,"reachable":true}]}}
                """);
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","battery_percent":35,"reachable":false}]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(states.values().stream().allMatch(UnDefType.UNDEF::equals), is(true));
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","reachable":true}]},
                 "errors":[{"id":"gateway","code":6}]}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        update("""
                {"home":{"id":"home","modules":[]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.OFFLINE));
        update("""
                {"home":{"id":"home","modules":[{"id":"gateway","battery_percent":35,"reachable":true}]}}
                """);
        assertThat(thing.getStatus(), is(ThingStatus.ONLINE));
        assertThat(states.get(CHANNEL_BATTERY), is(new DecimalType(35)));
    }

    @Test
    void refreshUsesSharedPollAndDoesNotSendLockCommands() {
        clearInvocations(account);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY), RefreshType.REFRESH);
        verify(account).requestPoll();
        clearInvocations(account);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY), OnOffType.ON);
        verifyNoInteractions(account);
    }

    @Test
    void disposalSuppressesLateUpdatesAndCommands() {
        var requestContext = requireNonNull(handler.captureRequestContext());
        handler.dispose();
        clearInvocations(callback, account);
        handler.update(requestContext, new HomeStatus());
        handler.failed(requestContext, "late failure");
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY), RefreshType.REFRESH);
        verifyNoInteractions(callback, account);
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

    private void update(String json) {
        handler.update(requireNonNull(handler.captureRequestContext()),
                requireNonNull(new Gson().fromJson(json, HomeStatus.class)));
    }
}
