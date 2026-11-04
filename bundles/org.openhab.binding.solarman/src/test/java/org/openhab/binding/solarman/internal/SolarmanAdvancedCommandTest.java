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
package org.openhab.binding.solarman.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnection;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnector;
import org.openhab.binding.solarman.internal.modbus.SolarmanV5Protocol;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanProtocolException;
import org.openhab.binding.solarman.internal.util.SolarmanWorkQueue;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;

/**
 * Exercises the real advanced handler dispatch and worker with simulated device registers.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SolarmanAdvancedCommandTest {
    @ParameterizedTest
    @ValueSource(strings = { "quantity", "decimal", "percent", "six-periods", "unchanged", "partial-block",
            "partial-word", "refresh" })
    void advancedSocWritesOnlyOnTheWorkerAndPublishesDeviceReadback(String format) throws Exception {
        Map<Integer, byte[]> registers = new HashMap<>();
        registers.put(219, new byte[] { 0, 20 });
        for (int register = 268; register <= 273; register++) {
            registers.put(register, new byte[] { 0, 20 });
        }
        SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
        when(connection.isConnected()).thenReturn(true);
        try (var connectorScope = mockConstruction(SolarmanLoggerConnector.class,
                (connector, context) -> when(connector.createConnection()).thenReturn(connection));
                var protocolScope = mockConstruction(SolarmanV5Protocol.class, (protocol, context) -> {
                    when(protocol.readRegisters(any(), anyByte(), anyInt(), anyInt())).thenAnswer(invocation -> {
                        int first = invocation.getArgument(2);
                        int last = invocation.getArgument(3);
                        if ("partial-block".equals(format) && first == 603) {
                            throw new SolarmanProtocolException("Simulated unavailable non-control block");
                        }
                        Map<Integer, byte[]> snapshot = new HashMap<>();
                        for (int register = first; register <= last; register++) {
                            if ("partial-word".equals(format) && register == 603) {
                                continue;
                            }
                            snapshot.put(register, registers.getOrDefault(register, new byte[] { 0, 0 }).clone());
                        }
                        return snapshot;
                    });
                    when(protocol.writeRegisters(any(), anyInt(), any())).thenAnswer(invocation -> {
                        int first = invocation.getArgument(1);
                        byte[] data = invocation.getArgument(2);
                        for (int index = 0; index < data.length / 2; index++) {
                            registers.put(first + index, Arrays.copyOfRange(data, index * 2, index * 2 + 2));
                        }
                        return true;
                    });
                })) {
            var type = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;
            Thing thing = ThingBuilder.create(type, new ThingUID(type, "example"))
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "serialNumber", "1234567890",
                            "inverterType", "sunsynk_single_phase", "timeZone", "UTC")))
                    .build();
            WorkerHandler handler = new WorkerHandler(thing);
            try {
                handler.initialize();
                assertEquals(1, connectorScope.constructed().size());
                var protocol = protocolScope.constructed().getFirst();
                List<Integer> periods = "six-periods".equals(format) ? List.of(2, 3, 4, 5, 6, 1) : List.of(6);
                int target = "six-periods".equals(format) ? 100 : "unchanged".equals(format) ? 20 : 99;
                Command command = switch (format) {
                    case "quantity", "six-periods" -> new QuantityType<>(target, Units.PERCENT);
                    case "decimal" -> new DecimalType(target);
                    default -> new PercentType(target);
                };
                for (int period : periods) {
                    handler.handleCommand(new ChannelUID(thing.getUID(), "prog" + period + "-capacity"), command);
                }
                if ("refresh".equals(format)) {
                    handler.handleCommand(new ChannelUID(thing.getUID(), "prog6-capacity"), RefreshType.REFRESH);
                }
                verify(protocol, never()).readRegisters(any(), anyByte(), anyInt(), anyInt());
                verify(protocol, never()).writeRegisters(any(), anyInt(), any());
                for (int index = 0; index < periods.size(); index++) {
                    Objects.requireNonNull(handler.tick.get()).run();
                }
                assertTrue(handler.errors.isEmpty(), handler.errors::toString);
                var order = inOrder(protocol);
                for (int period : periods) {
                    int register = 267 + period;
                    order.verify(protocol).writeRegisters(same(connection), eq(register),
                            org.mockito.AdditionalMatchers.aryEq(new byte[] { 0, (byte) target }));
                    assertArrayEquals(new byte[] { 0, (byte) target }, registers.get(register));
                    verify(handler.callback, atLeastOnce()).stateUpdated(
                            new ChannelUID(thing.getUID(), "prog" + period + "-capacity"),
                            new QuantityType<>(target, Units.PERCENT));
                }
                verify(protocol, times(periods.size())).writeRegisters(any(), anyInt(), any());
                int readsPerCommand = List.of("partial-block", "partial-word", "refresh").contains(format) ? 12 : 8;
                verify(protocol, times(readsPerCommand * periods.size())).readRegisters(any(), anyByte(), anyInt(),
                        anyInt());
                Objects.requireNonNull(handler.tick.get()).run();
                verify(protocol, times(readsPerCommand * periods.size())).readRegisters(any(), anyByte(), anyInt(),
                        anyInt());
            } finally {
                handler.dispose();
            }
            verify(handler.future).cancel(true);
            verify(connectorScope.constructed().getFirst()).close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "number", "switch", "select", "time", "clock", "exhausted", "transport", "rejected",
            "missing", "malformed", "fresh-limit", "fresh-mask", "six-recovery", "unchanged" })
    void confirmationRecoveryIsBoundedAndRevalidatesDeviceState(String scenario) throws Exception {
        Map<Integer, byte[]> registers = new HashMap<>();
        registers.put(219, new byte[] { 0, 20 });
        registers.put(22, new byte[] { 26, 10 });
        registers.put(23, new byte[] { 31, 12 });
        registers.put(24, new byte[] { 0, 0 });
        for (int address = 268; address <= 273; address++) {
            registers.put(address, new byte[] { 0, 20 });
        }
        boolean masked = List.of("switch", "select", "fresh-mask").contains(scenario);
        boolean clock = "clock".equals(scenario);
        int address = masked ? 279 : "time".equals(scenario) ? 255 : clock ? 22 : 273;
        String channel = masked ? "switch".equals(scenario) ? "prog6-sell" : "prog6-charge"
                : "time".equals(scenario) ? "prog6-time" : clock ? "date-time" : "prog6-capacity";
        Command command = masked ? "switch".equals(scenario) ? OnOffType.ON : new StringType("Allow Grid")
                : "time".equals(scenario) ? new StringType("12:30")
                        : clock ? new DateTimeType("2026-10-31T13:00:00Z") : new QuantityType<>(99, Units.PERCENT);
        if ("unchanged".equals(scenario)) {
            registers.put(273, new byte[] { 0, 99 });
        }
        AtomicInteger writes = new AtomicInteger();
        AtomicInteger snapshots = new AtomicInteger();
        SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
        when(connection.isConnected()).thenReturn(true);
        try (var connectorScope = mockConstruction(SolarmanLoggerConnector.class,
                (connector, context) -> when(connector.createConnection()).thenReturn(connection));
                var protocolScope = mockConstruction(SolarmanV5Protocol.class, (protocol, context) -> {
                    when(protocol.readRegisters(any(), anyByte(), anyInt(), anyInt())).thenAnswer(invocation -> {
                        int first = invocation.getArgument(2);
                        int last = invocation.getArgument(3);
                        if (first == 0) {
                            snapshots.incrementAndGet();
                        }
                        if (writes.get() == 1 && "transport".equals(scenario)) {
                            throw new SolarmanConnectionException("Simulated transport failure");
                        }
                        if (writes.get() == 1 && snapshots.get() >= 3 && "fresh-limit".equals(scenario)) {
                            registers.put(219, new byte[] { 0, 100 });
                        }
                        if (writes.get() == 1 && snapshots.get() >= 3 && "fresh-mask".equals(scenario)) {
                            registers.put(279, new byte[] { 0, 64 });
                        }
                        Map<Integer, byte[]> snapshot = new HashMap<>();
                        for (int register = first; register <= last; register++) {
                            if (writes.get() == 1 && register == address && "missing".equals(scenario)) {
                                continue;
                            }
                            snapshot.put(register,
                                    writes.get() == 1 && register == address && "malformed".equals(scenario)
                                            ? new byte[] { 0 }
                                            : registers.getOrDefault(register, new byte[] { 0, 0 }).clone());
                        }
                        return snapshot;
                    });
                    when(protocol.writeRegisters(any(), anyInt(), any())).thenAnswer(invocation -> {
                        int count = writes.incrementAndGet();
                        if ("rejected".equals(scenario)) {
                            return false;
                        }
                        int first = invocation.getArgument(1);
                        byte[] data = invocation.getArgument(2);
                        boolean apply = "unchanged".equals(scenario) || "six-recovery".equals(scenario) && first != 273
                                || count > ("six-recovery".equals(scenario) ? 5 : 1);
                        if (apply && !"exhausted".equals(scenario)) {
                            for (int index = 0; index < data.length / 2; index++) {
                                registers.put(first + index, Arrays.copyOfRange(data, index * 2, index * 2 + 2));
                            }
                        }
                        return true;
                    });
                })) {
            var type = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;
            Thing thing = ThingBuilder.create(type, new ThingUID(type, "example"))
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "serialNumber", "1234567890",
                            "inverterType", "sunsynk_single_phase", "timeZone", "UTC")))
                    .build();
            WorkerHandler handler = new WorkerHandler(thing);
            try {
                handler.initialize();
                if ("six-recovery".equals(scenario)) {
                    for (int period : List.of(2, 3, 4, 5, 6, 1)) {
                        handler.handleCommand(new ChannelUID(thing.getUID(), "prog" + period + "-capacity"), command);
                    }
                    for (int index = 0; index < 6; index++) {
                        Objects.requireNonNull(handler.tick.get()).run();
                    }
                    assertEquals(7, writes.get());
                    for (int register = 268; register <= 273; register++) {
                        assertArrayEquals(new byte[] { 0, 99 }, registers.get(register));
                    }
                } else {
                    handler.handleCommand(new ChannelUID(thing.getUID(), channel), command);
                    assertEquals(0, writes.get());
                    Objects.requireNonNull(handler.tick.get()).run();
                    boolean single = clock
                            || List.of("transport", "rejected", "missing", "malformed", "fresh-limit", "unchanged")
                                    .contains(scenario);
                    assertEquals(single ? 1 : 2, writes.get());
                    if ("fresh-mask".equals(scenario)) {
                        assertArrayEquals(new byte[] { 0, 65 }, registers.get(address));
                    }
                    if (List.of("number", "switch", "select", "time", "fresh-mask").contains(scenario)) {
                        verify(handler.callback, atLeastOnce()).stateUpdated(new ChannelUID(thing.getUID(), channel),
                                assertInstanceOf(State.class, command));
                    }
                }
                boolean failure = List.of("exhausted", "transport", "rejected", "missing", "malformed", "fresh-limit")
                        .contains(scenario);
                assertEquals(failure ? 1 : 0, handler.errors.size(), handler.errors::toString);
                int expectedSnapshots = switch (scenario) {
                    case "six-recovery" -> 14;
                    case "unchanged", "rejected" -> 2;
                    case "clock", "transport", "missing", "malformed" -> 3;
                    case "exhausted" -> 5;
                    default -> 4;
                };
                assertEquals(expectedSnapshots, snapshots.get(), "Unexpected refresh/readback count for " + scenario);
                int confirmations = failure || clock || "unchanged".equals(scenario) ? 0
                        : "six-recovery".equals(scenario) ? 6 : 1;
                verify(handler.auditLogger, times(confirmations)).info(eq(
                        "Confirmed register change: channel={}, registers=[{}], previous={}, requested={}, confirmed={}"),
                        any(Object[].class));
                if ("unchanged".equals(scenario)) {
                    verify(handler.auditLogger, never()).warn(anyString(), any(Object[].class));
                }
                if (failure) {
                    verify(handler.auditLogger, atLeastOnce()).warn(anyString(), any(Object[].class));
                    if (!"fresh-limit".equals(scenario)) {
                        verify(handler.auditLogger).warn(eq(
                                "Write or confirmation failed: channel={}, registers=[{}], requested={}, stage={}, reason={}, readFailures={}"),
                                eq(new ChannelUID(thing.getUID(), channel)), eq("0x0111"), eq(command),
                                eq("rejected".equals(scenario) ? "write" : "readback"), any(), any());
                    }
                }
                verify(protocolScope.constructed().getFirst(), times(writes.get())).writeRegisters(any(), anyInt(),
                        any());
            } finally {
                handler.dispose();
            }
            verify(handler.future).cancel(true);
            verify(connectorScope.constructed().getFirst()).close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "confirmed", "refresh", "dispose" })
    void confirmationWaitsForTimerAndNeverInterleavesWithAnotherCommand(String scenario) throws Exception {
        Map<Integer, byte[]> registers = new HashMap<>();
        registers.put(219, new byte[] { 0, 20 });
        registers.put(273, new byte[] { 0, 20 });
        SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
        when(connection.isConnected()).thenReturn(true);
        try (var connectorScope = mockConstruction(SolarmanLoggerConnector.class,
                (connector, context) -> when(connector.createConnection()).thenReturn(connection));
                var protocolScope = mockConstruction(SolarmanV5Protocol.class, (protocol, context) -> {
                    when(protocol.readRegisters(any(), anyByte(), anyInt(), anyInt())).thenAnswer(invocation -> {
                        int first = invocation.getArgument(2);
                        int last = invocation.getArgument(3);
                        Map<Integer, byte[]> snapshot = new HashMap<>();
                        for (int address = first; address <= last; address++) {
                            snapshot.put(address, registers.getOrDefault(address, new byte[] { 0, 0 }).clone());
                        }
                        return snapshot;
                    });
                    when(protocol.writeRegisters(any(), anyInt(), any())).thenAnswer(invocation -> {
                        registers.put(invocation.getArgument(1), ((byte[]) invocation.getArgument(2)).clone());
                        return true;
                    });
                })) {
            var type = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;
            Thing thing = ThingBuilder.create(type, new ThingUID(type, "example"))
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "serialNumber", "1234567890",
                            "inverterType", "sunsynk_single_phase", "timeZone", "UTC")))
                    .build();
            WorkerHandler handler = new WorkerHandler(thing);
            handler.autoConfirm = false;
            try {
                handler.initialize();
                assertEquals(1, connectorScope.constructed().size());
                var protocol = protocolScope.constructed().getFirst();
                handler.handleCommand(new ChannelUID(thing.getUID(), "prog6-capacity"), new DecimalType(99));
                handler.handleCommand(new ChannelUID(thing.getUID(), "prog5-capacity"), new DecimalType(80));
                Objects.requireNonNull(handler.tick.get()).run();
                verify(protocol, times(4)).readRegisters(any(), anyByte(), anyInt(), anyInt());
                verify(protocol).writeRegisters(any(), eq(273), any());
                verify(protocol, never()).writeRegisters(any(), eq(272), any());
                verify(handler.callback, never()).stateUpdated(new ChannelUID(thing.getUID(), "prog6-capacity"),
                        new QuantityType<>(99, Units.PERCENT));
                verify(handler.auditLogger, never()).info(anyString(), any(Object[].class));
                verify(handler.testScheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
                Objects.requireNonNull(handler.tick.get()).run();
                verify(protocol, times(4)).readRegisters(any(), anyByte(), anyInt(), anyInt());
                Runnable continuation = Objects.requireNonNull(handler.deferred.getAndSet(null));
                if ("dispose".equals(scenario)) {
                    handler.dispose();
                    continuation.run();
                    verify(protocol, times(4)).readRegisters(any(), anyByte(), anyInt(), anyInt());
                    verify(handler.delayedFutures.getFirst()).cancel(true);
                    verify(handler.auditLogger, never()).info(anyString(), any(Object[].class));
                } else {
                    if ("refresh".equals(scenario)) {
                        handler.handleCommand(new ChannelUID(thing.getUID(), "prog6-capacity"), RefreshType.REFRESH);
                    }
                    continuation.run();
                    verify(protocol, times("refresh".equals(scenario) ? 12 : 8)).readRegisters(any(), anyByte(),
                            anyInt(), anyInt());
                    verify(handler.callback, atLeastOnce()).stateUpdated(
                            new ChannelUID(thing.getUID(), "prog6-capacity"), new QuantityType<>(99, Units.PERCENT));
                }
                verify(protocol, never()).writeRegisters(any(), eq(272), any());
                verify(connection, times("refresh".equals(scenario) ? 2 : 1)).close();
                assertTrue(handler.errors.isEmpty(), handler.errors::toString);
            } finally {
                handler.dispose();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "missing", "malformed", "block", "all-blocks", "transport" })
    void initialReadFailureIdentifiesRegisterBlockAndOriginalCauseWithoutRetry(String scenario) throws Exception {
        AtomicInteger snapshots = new AtomicInteger();
        SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
        when(connection.isConnected()).thenReturn(true);
        try (var connectorScope = mockConstruction(SolarmanLoggerConnector.class,
                (connector, context) -> when(connector.createConnection()).thenReturn(connection));
                var protocolScope = mockConstruction(SolarmanV5Protocol.class, (protocol, context) -> {
                    when(protocol.readRegisters(any(), anyByte(), anyInt(), anyInt())).thenAnswer(invocation -> {
                        int first = invocation.getArgument(2);
                        int last = invocation.getArgument(3);
                        if (first == 0) {
                            snapshots.incrementAndGet();
                        }
                        if (snapshots.get() == 1) {
                            if ("transport".equals(scenario)) {
                                throw new SolarmanConnectionException("Unable to exchange a complete logger frame",
                                        new SocketTimeoutException("Simulated socket timeout"));
                            }
                            if ("all-blocks".equals(scenario)
                                    || "block".equals(scenario) && first <= 269 && last >= 269) {
                                throw new SolarmanProtocolException("Simulated V5 sequence mismatch");
                            }
                        }
                        Map<Integer, byte[]> snapshot = new HashMap<>();
                        for (int address = first; address <= last; address++) {
                            if (snapshots.get() == 1 && address == 269 && "missing".equals(scenario)) {
                                continue;
                            }
                            snapshot.put(address,
                                    snapshots.get() == 1 && address == 269 && "malformed".equals(scenario)
                                            ? new byte[] { 0 }
                                            : new byte[] { 0, 20 });
                        }
                        return snapshot;
                    });
                })) {
            var type = SolarmanBindingConstants.THING_TYPE_SOLARMAN_LOGGER;
            Thing thing = ThingBuilder.create(type, new ThingUID(type, "example"))
                    .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "serialNumber", "1234567890",
                            "inverterType", "sunsynk_single_phase", "timeZone", "UTC")))
                    .build();
            WorkerHandler handler = new WorkerHandler(thing);
            try {
                handler.initialize();
                Command command = new PercentType(20);
                ChannelUID channel = new ChannelUID(thing.getUID(), "prog2-capacity");
                handler.handleCommand(channel, command);
                Objects.requireNonNull(handler.tick.get()).run();
                var protocol = protocolScope.constructed().getFirst();
                verify(protocol, never()).writeRegisters(any(), anyInt(), any());
                verify(protocol, times("transport".equals(scenario) ? 5 : 8)).readRegisters(any(), anyByte(), anyInt(),
                        anyInt());
                assertEquals(2, snapshots.get(), "Only the initial read and existing post-failure poll are allowed");
                assertEquals(1, handler.errors.size());
                String reason = Objects.requireNonNull(handler.errors.getFirst().getMessage());
                if (List.of("missing", "malformed", "block").contains(scenario)) {
                    assertTrue(reason.contains("269 (0x010D)"), reason);
                    assertTrue(reason.contains("malformed".equals(scenario) ? "1 bytes" : "missing"), reason);
                }
                if (List.of("transport", "all-blocks").contains(scenario)) {
                    assertNotNull(handler.errors.getFirst().getCause());
                    assertTrue(reason.contains("FC03 block 0.."), reason);
                }
                verify(handler.auditLogger).warn(eq(
                        "Write or confirmation failed: channel={}, registers=[{}], requested={}, stage={}, reason={}, readFailures={}"),
                        eq(channel), eq("0x010D"), eq(command), eq("pre-read"), eq(reason), argThat(failures -> {
                            String details = String.valueOf(failures);
                            return switch (scenario) {
                                case "block", "all-blocks" -> details.contains("FC03 block") && details
                                        .contains("SolarmanProtocolException: Simulated V5 sequence mismatch");
                                default -> "[]".equals(details);
                            };
                        }));
                if ("transport".equals(scenario)) {
                    assertTrue(reason.contains(
                            "SolarmanConnectionException: Unable to exchange a complete logger frame; caused by SocketTimeoutException: Simulated socket timeout"),
                            reason);
                }
            } finally {
                handler.dispose();
            }
            verify(connectorScope.constructed().getFirst()).close();
        }
    }

    private static class WorkerHandler extends SolarmanLoggerHandler {
        final AtomicReference<Runnable> tick = new AtomicReference<>();
        final AtomicReference<@Nullable Runnable> deferred = new AtomicReference<>();
        final List<ScheduledFuture<?>> delayedFutures = new ArrayList<>();
        boolean autoConfirm = true;
        final ScheduledExecutorService testScheduler = mock(ScheduledExecutorService.class);
        final ScheduledFuture<?> future = mock(ScheduledFuture.class);
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final List<Exception> errors = new ArrayList<>();

        final Logger auditLogger;

        WorkerHandler(Thing thing) {
            this(thing, mock(Logger.class));
        }

        WorkerHandler(Thing thing, Logger auditLogger) {
            super(thing, auditLogger);
            this.auditLogger = auditLogger;
            doAnswer(invocation -> {
                setThing(invocation.getArgument(0));
                return null;
            }).when(callback).thingUpdated(any());
            doAnswer(invocation -> {
                Runnable periodic = invocation.getArgument(0);
                tick.set(() -> {
                    periodic.run();
                    if (autoConfirm) {
                        runConfirmations();
                    }
                });
                return future;
            }).when(testScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                    any(TimeUnit.class));
            doAnswer(invocation -> {
                deferred.set(invocation.getArgument(0));
                ScheduledFuture<?> pending = mock(ScheduledFuture.class);
                delayedFutures.add(pending);
                return pending;
            }).when(testScheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
            setCallback(callback);
        }

        void runConfirmations() {
            Runnable continuation;
            while ((continuation = deferred.getAndSet(null)) != null) {
                continuation.run();
            }
        }

        @Override
        protected SolarmanWorkQueue createWorkQueue(Runnable poll, int interval, Consumer<Exception> errorHandler) {
            return new SolarmanWorkQueue(testScheduler, poll, interval, error -> {
                errors.add(error);
                errorHandler.accept(error);
            });
        }
    }
}
