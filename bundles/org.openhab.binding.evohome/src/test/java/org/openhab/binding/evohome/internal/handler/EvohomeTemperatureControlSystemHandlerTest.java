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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.evohome.internal.action.EvohomeSystemActions;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.GatewayStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.TemperatureControlSystemStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Tests the distinction between system modes and resetting all zone overrides.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class EvohomeTemperatureControlSystemHandlerTest {
    private final EvohomeAccountBridgeHandler bridge = mock(EvohomeAccountBridgeHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final Thing thing = ThingBuilder.create(THING_TYPE_EVOHOME_DISPLAY, "controller")
            .withConfiguration(new Configuration(Map.of("id", "controller"))).build();
    private final EvohomeTemperatureControlSystemHandler handler = new EvohomeTemperatureControlSystemHandler(thing) {
        @Override
        protected @Nullable EvohomeAccountBridgeHandler getEvohomeBridge() {
            return bridge;
        }
    };

    @BeforeEach
    void setup() {
        handler.setCallback(callback);
        handler.initialize();
        when(bridge.setTcsMode(eq("controller"), anyString())).thenReturn(true);
        when(bridge.resetSystem("controller")).thenReturn(true);
        clearInvocations(bridge, callback);
    }

    @Test
    void autoChangesSystemModeWithoutResettingZoneOverrides() {
        command("SystemMode", new StringType("Auto"));
        verify(bridge).setTcsMode("controller", "Auto");
        verifyNoMoreInteractions(bridge);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void resetOnExplicitlyResetsSystemAndZoneOverrides() {
        command("Reset", OnOffType.ON);
        verify(bridge).resetSystem("controller");
        verifyNoMoreInteractions(bridge);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void resetOffDoesNotSendARequest() {
        command("Reset", OnOffType.OFF);
        verifyNoInteractions(bridge);
    }

    @Test
    void autoWithResetUsesResetOperationInsteadOfAnOrdinaryModeWrite() {
        assertTrue(handler.setSystemMode("AutoWithReset"));
        verify(bridge).resetSystem("controller");
        verify(bridge, never()).setTcsMode(anyString(), anyString());
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void autoWithResetChannelCommandAlsoUsesResetOperation() {
        command("SystemMode", new StringType("AutoWithReset"));
        verify(bridge).resetSystem("controller");
        verify(bridge, never()).setTcsMode(anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void timedCommandReturnsBridgeAcceptanceWithoutPublishingOptimisticStates(boolean accepted) {
        Instant until = Instant.now().plusSeconds(3600);
        when(bridge.setTemporaryTcsMode("controller", "Away", until)).thenReturn(accepted);

        assertEquals(accepted, handler.setTemporarySystemMode("Away", until));

        verify(bridge).setTemporaryTcsMode("controller", "Away", until);
        verify(bridge, never()).resetSystem(anyString());
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void ordinaryModeChannelReplacesTemporaryModeWithAPermanentRequest() {
        handler.update(gateway("[]"), status("""
                {"mode":"Away","isPermanent":false,"timeUntil":"2030-01-01T12:00:00Z"}
                """));
        clearInvocations(callback);

        command("SystemMode", new StringType("Auto"));

        verify(bridge).setTcsMode("controller", "Auto");
        verify(bridge, never()).setTemporaryTcsMode(anyString(), anyString(), any());
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void actualPermanentStatusIgnoresAStaleExpiry() {
        handler.update(gateway("[]"), status("""
                {"mode":"Auto","isPermanent":true,"timeUntil":"2030-01-01T12:00:00Z"}
                """));

        verify(callback).stateUpdated(channel("SystemMode"), new StringType("Auto"));
        verify(callback).stateUpdated(channel("SystemModePermanent"), OnOffType.ON);
        verify(callback).stateUpdated(channel("SystemModeUntil"), UnDefType.UNDEF);
    }

    @ParameterizedTest
    @ValueSource(strings = { "2030-01-17T20:15:42+05:30", "2000-01-01T12:00:00+02:00" })
    void actualTimedStatusPublishesUtcExpiryIncludingAValidPastReadback(String until) {
        handler.update(gateway("[]"), status("""
                {"mode":"Away","isPermanent":false,"timeUntil":"%s"}
                """.formatted(until)));

        verify(callback).stateUpdated(channel("SystemMode"), new StringType("Away"));
        verify(callback).stateUpdated(channel("SystemModePermanent"), OnOffType.OFF);
        verify(callback).stateUpdated(channel("SystemModeUntil"),
                new DateTimeType(OffsetDateTime.parse(until).toInstant().atZone(ZoneOffset.UTC)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "not-a-date", "2030-01-01T12:00:00" })
    void invalidTimedStatusExpiryPublishesUndefinedInsteadOfRetainingAnOldDeadline(String until) {
        handler.update(gateway("[]"), status("""
                {"mode":"Away","isPermanent":false,"timeUntil":"2030-01-01T12:00:00Z"}
                """));
        clearInvocations(callback);

        handler.update(gateway("[]"), status("""
                {"mode":"Away","isPermanent":false,"timeUntil":"%s"}
                """.formatted(until)));

        verify(callback).stateUpdated(channel("SystemModePermanent"), OnOffType.OFF);
        verify(callback).stateUpdated(channel("SystemModeUntil"), UnDefType.UNDEF);
    }

    @Test
    void missingPermanenceIsUnknownEvenWhenAnExpiryIsPresent() {
        handler.update(gateway("[]"), status("""
                {"mode":"Away","timeUntil":"2030-01-01T12:00:00Z"}
                """));

        verify(callback).stateUpdated(channel("SystemMode"), new StringType("Away"));
        verify(callback).stateUpdated(channel("SystemModePermanent"), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel("SystemModeUntil"), UnDefType.UNDEF);
    }

    @Test
    void missingTimedExpiryPublishesUndefined() {
        handler.update(gateway("[]"), status("{\"mode\":\"Away\",\"isPermanent\":false}"));
        verify(callback).stateUpdated(channel("SystemModePermanent"), OnOffType.OFF);
        verify(callback).stateUpdated(channel("SystemModeUntil"), UnDefType.UNDEF);
    }

    @Test
    void missingModeStatusClearsAllThreeChannels() {
        handler.update(gateway("[]"), status("null"));

        verify(callback).stateUpdated(channel("SystemMode"), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel("SystemModePermanent"), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel("SystemModeUntil"), UnDefType.UNDEF);
    }

    @Test
    void missingControllerAndGatewayFaultsRetainPreviousChannelValues() {
        handler.update(gateway("[]"), status("{\"mode\":\"Auto\",\"isPermanent\":true}"));
        clearInvocations(callback);

        handler.update(gateway("[]"), null);
        handler.update(null, status("{\"mode\":\"Away\",\"isPermanent\":false}"));
        handler.update(gateway("[{\"faultType\":\"CommunicationError\"}]"),
                status("{\"mode\":\"Away\",\"isPermanent\":false}"));

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void readOnlyModeStatusChannelsIgnoreCommands() {
        command("SystemModePermanent", OnOffType.ON);
        command("SystemModePermanent", OnOffType.OFF);
        command("SystemModeUntil", new DateTimeType(ZonedDateTime.now()));
        verifyNoInteractions(bridge);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void displayRegistersSystemActions() {
        assertTrue(handler.getServices().contains(EvohomeSystemActions.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "SystemMode", "Reset", "SystemModePermanent", "SystemModeUntil" })
    void refreshReadsCloudStateWithoutChangingModeOrResettingOverrides(String channel) {
        command(channel, RefreshType.REFRESH);
        verify(bridge).requestRefresh();
        verifyNoMoreInteractions(bridge);
    }

    private void command(String channel, Command command) {
        handler.handleCommand(channel(channel), command);
    }

    private ChannelUID channel(String channel) {
        return new ChannelUID(thing.getUID(), channel);
    }

    private GatewayStatus gateway(String faults) {
        return Objects
                .requireNonNull(new Gson().fromJson("{\"activeFaults\":%s}".formatted(faults), GatewayStatus.class));
    }

    private TemperatureControlSystemStatus status(String modeStatus) {
        return Objects.requireNonNull(
                new Gson().fromJson("{\"systemId\":\"controller\",\"systemModeStatus\":%s}".formatted(modeStatus),
                        TemperatureControlSystemStatus.class));
    }
}
