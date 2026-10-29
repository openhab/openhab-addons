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
package org.openhab.binding.dreame.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.dreame.internal.api.DreameCloudException;
import org.openhab.binding.dreame.internal.api.DreameVacuumApi;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameVacuumProperties;
import org.openhab.binding.dreame.internal.util.DreameVacuumMapStateTest;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * Restricts vacuum MQTT subscriptions to models with a known protocol mapping.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumHandlerTest {
    @Test
    void homeAssistantMappedModelsEnableMqttDiagnostics() {
        assertTrue(DreameVacuumHandler.supportsMqttDiagnostics(device("dreame.vacuum.r9445d")));
        assertTrue(DreameVacuumHandler.supportsMqttDiagnostics(device("dreame.vacuum.r2205")));
        assertTrue(DreameVacuumHandler.supportsMqttDiagnostics(device("dreame.vacuum.r9533a")));
        assertFalse(DreameVacuumHandler.supportsMqttDiagnostics(device("dreame.vacuum.other")));
        assertFalse(DreameVacuumHandler.supportsMqttDiagnostics(device("dreame.mower.g2540d")));
        assertFalse(DreameVacuumHandler.supportsMqttDiagnostics(device("mova.mower.g2584d")));
    }

    @Test
    void cachesPartialUpdatesAndRejectsObsoleteCallbacks() {
        Thing thing = Objects.requireNonNull(mock(Thing.class));
        ThingUID uid = new ThingUID("dreame:vacuum:test");
        when(thing.getUID()).thenReturn(uid);
        when(thing.getChannels()).thenReturn(java.util.List.of());
        doReturn(mock(Channel.class)).when(thing).getChannel(anyString());
        when(thing.getConfiguration()).thenReturn(new Configuration());
        ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        DreameVacuumHandler handler = new DreameVacuumHandler(thing);
        handler.setCallback(callback);
        handler.initialize();
        ChannelUID battery = new ChannelUID(uid, "battery-level");
        verify(callback).stateUpdated(battery, UnDefType.UNDEF);
        clearInvocations(callback);
        // Deliver updates for the current lifecycle, followed by unrelated partial updates.
        ChannelUID map = new ChannelUID(uid, "map-svg");
        handler.receiveMapData(1, 1, DreameVacuumMapStateTest.frame(1, 1, 'I', 0, 0, 1, 1, new byte[] { 1 }, "{}"));
        handler.handleCommand(map, RefreshType.REFRESH);
        ArgumentCaptor<org.openhab.core.types.State> mapState = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(callback, times(2)).stateUpdated(eq(map), mapState.capture());
        assertEquals(mapState.getAllValues().get(0), mapState.getAllValues().get(1));
        handler.handleCommand(new ChannelUID(uid, "map-png"), RefreshType.REFRESH);
        verify(callback, times(2)).stateUpdated(eq(new ChannelUID(uid, "map-png")), any());
        handler.handleCommand(new ChannelUID(uid, "rooms"), RefreshType.REFRESH);
        verify(callback, times(2)).stateUpdated(new ChannelUID(uid, "rooms"), new StringType("1"));
        handler.receiveProperties(1, 1, Map.of("3/1", 99));
        handler.receiveProperties(1, 1, Map.of("2/1", 3));
        handler.handleCommand(battery, RefreshType.REFRESH);
        verify(callback, times(2)).stateUpdated(battery, new DecimalType(99));
        clearInvocations(callback);
        handler.receiveMapData(0, 1, "invalid");
        handler.receiveMapData(1, 0, "invalid");
        handler.receiveProperties(0, 1, Map.of("3/1", 1));
        handler.receiveProperties(1, 0, Map.of("3/1", 1));
        handler.handleCommand(battery, OnOffType.ON);
        verifyNoInteractions(callback);
        handler.dispose();
        verify(callback).stateUpdated(battery, UnDefType.UNDEF);
        clearInvocations(callback);
        handler.receiveMapData(1, 1, "invalid");
        handler.receiveProperties(1, 1, Map.of("3/1", 1));
        handler.handleCommand(battery, RefreshType.REFRESH);
        verifyNoInteractions(callback);
        handler.initialize();
        clearInvocations(callback);
        handler.handleCommand(battery, RefreshType.REFRESH);
        verify(callback).stateUpdated(battery, UnDefType.UNDEF);
        handler.dispose();
    }

    @Test
    void snapshotFillsMissingPropertiesWithoutOverwritingNewerPushes() throws Exception {
        SnapshotFixture f = new SnapshotFixture();
        doAnswer(invocation -> {
            f.handler.receiveProperties(1, 1, Map.of("3/1", 98));
            return new DreameVacuumProperties(Map.of("3/1", 99, "2/2", 0), Map.of());
        }).when(f.api).getVacuumProperties(any(), any());
        f.handler.refreshProperties(f.api, device("dreame.vacuum.r9445d"), 1);
        verify(f.callback).stateUpdated(new ChannelUID(f.uid, "battery-level"), new DecimalType(98));
        verify(f.callback, never()).stateUpdated(new ChannelUID(f.uid, "battery-level"), new DecimalType(99));
        verify(f.callback).stateUpdated(new ChannelUID(f.uid, "error-code"), DecimalType.ZERO);
        verify(f.callback, atLeastOnce()).statusUpdated(eq(f.handler.getThing()),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        f.handler.dispose();
    }

    @Test
    void repeatedFailedQueriesGoOfflineAndSuccessfulQueriesRecover() throws Exception {
        SnapshotFixture f = new SnapshotFixture();
        when(f.api.getVacuumProperties(any(), any())).thenThrow(new DreameCloudException("test"))
                .thenThrow(new DreameCloudException("test"))
                .thenReturn(new DreameVacuumProperties(Map.of("2/1", 13), Map.of()));
        f.handler.refreshProperties(f.api, device("dreame.vacuum.r9445d"), 1);
        verify(f.callback, never()).statusUpdated(any(), argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        f.handler.refreshProperties(f.api, device("dreame.vacuum.r9445d"), 1);
        verify(f.callback).statusUpdated(eq(f.handler.getThing()),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        f.handler.refreshProperties(f.api, device("dreame.vacuum.r9445d"), 1);
        verify(f.callback).statusUpdated(eq(f.handler.getThing()),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(f.callback).stateUpdated(new ChannelUID(f.uid, "state"), new StringType("CHARGING_COMPLETED"));
        f.handler.dispose();
    }

    @Test
    void failedQueryDoesNotOverrideNewerPushAndDisposalRejectsSnapshot() throws Exception {
        SnapshotFixture f = new SnapshotFixture();
        doAnswer(invocation -> {
            f.handler.receiveProperties(1, 1, Map.of("2/1", 22));
            throw new DreameCloudException("test");
        }).when(f.api).getVacuumProperties(any(), any());
        f.handler.refreshProperties(f.api, device("dreame.vacuum.r9445d"), 1);
        verify(f.callback, never()).statusUpdated(any(), argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        doAnswer(invocation -> {
            BooleanSupplier guard = invocation.getArgument(1);
            assertTrue(guard.getAsBoolean());
            f.handler.dispose();
            assertFalse(guard.getAsBoolean());
            clearInvocations(f.callback);
            return new DreameVacuumProperties(Map.of("3/1", 99), Map.of());
        }).when(f.api).getVacuumProperties(any(), any());
        f.handler.refreshProperties(f.api, device("dreame.vacuum.r9445d"), 1);
        verifyNoInteractions(f.callback);
    }

    private static class SnapshotFixture {
        final ThingUID uid = new ThingUID("dreame:vacuum:test");
        final ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        final DreameVacuumApi api = Objects.requireNonNull(mock(DreameVacuumApi.class));
        final DreameVacuumHandler handler;

        SnapshotFixture() {
            Thing thing = Objects.requireNonNull(mock(Thing.class));
            when(thing.getUID()).thenReturn(uid);
            when(thing.getChannels()).thenReturn(java.util.List.of());
            when(thing.getConfiguration()).thenReturn(new Configuration());
            doReturn(mock(Channel.class)).when(thing).getChannel(anyString());
            handler = new DreameVacuumHandler(thing);
            handler.setCallback(callback);
            handler.initialize();
            clearInvocations(callback);
        }
    }

    private static DreameDevice device(String model) {
        return new DreameDevice("test", "test", model, "", "", "", "");
    }
}
