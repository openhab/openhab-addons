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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.api.DreameCloudException;
import org.openhab.binding.dreame.internal.api.DreameVacuumApi;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameVacuumAction;
import org.openhab.binding.dreame.internal.model.DreameVacuumSetting;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;

/**
 * Exercises command ordering, lifecycle invalidation and failure recovery with a controlled executor.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumCommandHandlerTest {
    @Test
    void sendsInOrderAndPublishesAcknowledgedCommands() throws Exception {
        Fixture f = new Fixture();
        f.command("start");
        f.command("PAUSE");
        f.command("DOCK");
        verifyNoInteractions(f.api);
        assertEquals(1, f.work.size());
        f.work.removeFirst().run();
        var order = inOrder(f.api);
        order.verify(f.api).callVacuumAction(eq(f.device), eq(DreameVacuumAction.START), any());
        order.verify(f.api).callVacuumAction(eq(f.device), eq(DreameVacuumAction.PAUSE), any());
        order.verify(f.api).callVacuumAction(eq(f.device), eq(DreameVacuumAction.DOCK), any());
        var stateOrder = inOrder(f.callback);
        stateOrder.verify(f.callback).stateUpdated(f.channel, new StringType("START"));
        stateOrder.verify(f.callback).stateUpdated(f.channel, new StringType("PAUSE"));
        stateOrder.verify(f.callback).stateUpdated(f.channel, new StringType("DOCK"));
        f.handler.dispose();
    }

    @Test
    void failureDoesNotRetryOrBlockFollowingCommand() throws Exception {
        Fixture f = new Fixture();
        doThrow(new DreameCloudException("failed")).when(f.api).callVacuumAction(any(), eq(DreameVacuumAction.START),
                any());
        f.command("START");
        f.command("PAUSE");
        f.work.removeFirst().run();
        verify(f.api).callVacuumAction(any(), eq(DreameVacuumAction.START), any());
        verify(f.api).callVacuumAction(any(), eq(DreameVacuumAction.PAUSE), any());
        verifyNoMoreInteractions(f.api);
        verify(f.callback).stateUpdated(f.channel, new StringType("PAUSE"));
        verify(f.callback, never()).stateUpdated(f.channel, new StringType("START"));
        f.handler.dispose();
    }

    @Test
    void ignoresUnsupportedInputsAndDropsCommandsOnDispose() {
        Fixture f = new Fixture();
        f.command("UNSUPPORTED");
        f.handler.handleCommand(f.channel, RefreshType.REFRESH);
        f.handler.handleCommand(new ChannelUID(f.uid, "state"), new StringType("START"));
        assertTrue(f.work.isEmpty());
        f.command("START");
        f.handler.dispose();
        f.work.removeFirst().run();
        f.command("DOCK");
        verifyNoInteractions(f.api);
        assertTrue(f.work.isEmpty());
    }

    @Test
    void rechecksBridgeAndModelBeforeDispatch() {
        Fixture f = new Fixture();
        f.command("START");
        when(f.bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);
        f.work.removeFirst().run();
        when(f.bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(f.account.getVacuumDevices())
                .thenReturn(List.of(new DreameDevice("test", "", "dreame.vacuum.other", "", "", "", "")));
        f.command("START");
        f.work.removeFirst().run();
        verifyNoInteractions(f.api);
        f.handler.dispose();
    }

    @Test
    void lifecycleGuardChangesWhileApiIsWaiting() throws Exception {
        Fixture f = new Fixture();
        doAnswer(invocation -> {
            BooleanSupplier guard = invocation.getArgument(2);
            assertTrue(guard.getAsBoolean());
            f.handler.dispose();
            assertFalse(guard.getAsBoolean());
            return null;
        }).when(f.api).callVacuumAction(any(), any(), any());
        f.command("START");
        f.command("DOCK");
        f.work.removeFirst().run();
        verify(f.api).callVacuumAction(any(), eq(DreameVacuumAction.START), any());
        verifyNoMoreInteractions(f.api);
    }

    @Test
    void publishesAcknowledgedRoomSelection() throws Exception {
        Fixture f = new Fixture();
        ChannelUID rooms = new ChannelUID(f.uid, "room-cleaning");
        f.handler.handleCommand(rooms, new StringType("3, 7,3"));
        f.work.removeFirst().run();

        verify(f.api).cleanVacuumRooms(eq(f.device), eq(List.of(3, 7)), eq(1), eq(2), any());
        verify(f.callback).stateUpdated(rooms, new StringType("3,7"));
        f.handler.dispose();
    }

    @Test
    void routesAndPublishesNewCleaningSettings() throws Exception {
        Fixture f = new Fixture();
        ChannelUID genius = new ChannelUID(f.uid, "clean-genius");
        ChannelUID route = new ChannelUID(f.uid, "cleaning-route");
        ChannelUID drying = new ChannelUID(f.uid, "drying-time");
        f.handler.handleCommand(genius, new StringType("deep"));
        f.handler.handleCommand(route, new StringType("quick"));
        f.handler.handleCommand(drying, new StringType("3h"));
        while (!f.work.isEmpty()) {
            f.work.removeFirst().run();
        }

        verify(f.api).setVacuumSetting(eq(f.device), eq(DreameVacuumSetting.CLEAN_GENIUS), eq(2), any());
        verify(f.api).setVacuumSetting(eq(f.device), eq(DreameVacuumSetting.CLEANING_ROUTE), eq(4), any());
        verify(f.api).setVacuumSetting(eq(f.device), eq(DreameVacuumSetting.DRYING_TIME), eq(3), any());
        verify(f.callback).stateUpdated(genius, new StringType("DEEP"));
        verify(f.callback).stateUpdated(route, new StringType("QUICK"));
        verify(f.callback).stateUpdated(drying, new StringType("3H"));
        f.handler.dispose();
    }

    private static class Fixture {
        final ThingUID uid = new ThingUID("dreame:vacuum:test");
        final ChannelUID channel = new ChannelUID(uid, "command");
        final Bridge bridge = Objects.requireNonNull(mock(Bridge.class));
        final DreameAccountHandler account = Objects.requireNonNull(mock(DreameAccountHandler.class));
        final DreameVacuumApi api = Objects.requireNonNull(mock(DreameVacuumApi.class));
        final ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        final DreameDevice device = new DreameDevice("test", "", "dreame.vacuum.r9445d", "", "", "", "");
        final Deque<Runnable> work = new ArrayDeque<>();
        final DreameVacuumHandler handler;

        Fixture() {
            Thing thing = Objects.requireNonNull(mock(Thing.class));
            ThingUID bridgeUid = new ThingUID("dreame:account:test");
            Configuration config = new Configuration();
            config.put("deviceId", "test");
            when(thing.getUID()).thenReturn(uid);
            doReturn(mock(Channel.class)).when(thing).getChannel(anyString());
            when(thing.getConfiguration()).thenReturn(config);
            when(thing.getBridgeUID()).thenReturn(bridgeUid);
            when(callback.getBridge(bridgeUid)).thenReturn(bridge);
            when(bridge.getHandler()).thenReturn(account);
            when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);
            when(account.getVacuumApi()).thenReturn(api);
            when(account.getVacuumDevices()).thenReturn(List.of(device));
            handler = new DreameVacuumHandler(thing, work::addLast);
            handler.setCallback(callback);
            // Initialize offline to keep this command test independent of MQTT scheduling.
            handler.initialize();
            when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
            clearInvocations(callback);
        }

        void command(String command) {
            handler.handleCommand(channel, new StringType(command));
        }
    }
}
