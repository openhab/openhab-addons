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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.util.HexUtils;

/**
 * Tests correlated requests and mode transactions without real clocks or Bluetooth hardware.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class GeniSessionTest {
    // Objects 86:6 and 86:10, wrapped in GENI replies.
    private static final String OPERATING_REPLY = "2412F8E70A0E00012F0100000700000046E5C600C536";
    private static final String CONTROL_REPLY = "2412F8E70A0E00012F010000070006007FFFFFFFB216";
    private final Queue<Runnable> sends = new ArrayDeque<>();
    private final Queue<Runnable> timeouts = new ArrayDeque<>();
    private final List<String> packets = new ArrayList<>();
    private final List<Map<GeniMeasurand, BigDecimal>> updates = new ArrayList<>();
    private final Queue<CompletableFuture<@Nullable Void>> writes = new ArrayDeque<>();
    private final ScheduledFuture<?> timeoutFuture = mock(ScheduledFuture.class);
    private final Runnable connectionFailed = mock(Runnable.class);
    private Runnable afterUpdate = () -> {
    };
    private @NonNullByDefault({}) GeniSession session;

    @BeforeEach
    void setUp() {
        var scheduler = mock(ScheduledExecutorService.class);
        when(scheduler.schedule(any(Runnable.class), eq(700L), eq(TimeUnit.MILLISECONDS))).thenAnswer(call -> {
            sends.add(call.getArgument(0));
            return mock(ScheduledFuture.class);
        });
        when(scheduler.schedule(any(Runnable.class), eq(5L), eq(TimeUnit.SECONDS))).thenAnswer(call -> {
            timeouts.add(call.getArgument(0));
            return timeoutFuture;
        });
        session = new GeniSession(scheduler, packet -> {
            packets.add(HexUtils.bytesToHex(packet));
            CompletableFuture<@Nullable Void> write = writes.poll();
            return write == null ? CompletableFuture.completedFuture(null) : write;
        }, values -> {
            updates.add(values);
            afterUpdate.run();
        }, connectionFailed);
        session.start();
    }

    @Test
    void closeCancelsTimeoutAndIgnoresLateWriteCompletion() {
        var write = new CompletableFuture<@Nullable Void>();
        writes.add(write);
        session.read(GeniReadRequest.OperatingMode);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(OPERATING_REPLY));

        session.close();

        verify(timeoutFuture).cancel(false);
        write.complete(null);
        timeouts.remove().run();
        session.accept(HexUtils.hexToBytes(OPERATING_REPLY));
        assertThat(updates, empty());
        assertThat(sends, empty());
        verifyNoInteractions(connectionFailed);
    }

    @Test
    void readsWithIdenticalLayoutsAreCorrelated() {
        session.read(GeniReadRequest.OperatingMode);
        session.read(GeniReadRequest.ControlMode);
        sends.remove().run();
        assertThat(packets, contains("2707E7F80A03560006C55A"));
        assertThat(sends, empty());
        session.accept(HexUtils.hexToBytes(OPERATING_REPLY));
        sends.remove().run();
        assertThat(packets.getLast(), is("2707E7F80A0356000A04D6"));
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        assertThat(updates, contains(Map.of(GeniMeasurand.OperatingMode, BigDecimal.ZERO),
                Map.of(GeniMeasurand.ControlMode, BigDecimal.ZERO)));
    }

    @Test
    void modeWritePreservesOtherBytesAndWaitsForEachBluetoothWrite() {
        session.write(ModeSetting.OperatingMode, 1);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(OPERATING_REPLY));
        var firstFragment = new CompletableFuture<@Nullable Void>();
        writes.add(firstFragment);
        sends.remove().run();
        assertThat(packets, contains("2707E7F80A03560006C55A", "2714E7F80A90560006012F0100000700010046E5"));
        firstFragment.complete(null);
        assertThat(packets.getLast(), is("C6006C21"));
        assertThat(sends, empty());
        session.accept(HexUtils.hexToBytes("2405F8E70A0100AEA2"));
        sends.remove().run();
        assertThat(packets.getLast(), is("2707E7F80A03560006C55A"));
        // The command and acknowledgement do not publish the requested value.
        assertThat(updates, contains(Map.of(GeniMeasurand.OperatingMode, BigDecimal.ZERO)));
        byte[] changed = HexUtils.hexToBytes(OPERATING_REPLY);
        changed[14] = 1;
        CRC16Calculator.put(changed, 18);
        session.accept(changed);
        assertThat(updates.getLast(), is(Map.of(GeniMeasurand.OperatingMode, BigDecimal.ONE)));
    }

    @Test
    void controlModeWriteUsesItsOwnOffsetAndObject() {
        session.write(ModeSetting.ControlMode, 13);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        sends.remove().run();
        assertThat(packets, contains("2707E7F80A0356000A04D6", "2714E7F80A9056000A012F0100000700060D7FFF", "FFFF6915"));
        assertThat(updates, contains(Map.of(GeniMeasurand.ControlMode, BigDecimal.ZERO)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "2405F8E70A01010000", "2404F8E70AC00000" })
    void rejectedWritesStillReadBackWithoutPublishingTheCommand(String reply) {
        session.write(ModeSetting.ControlMode, 14);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        sends.remove().run();
        byte[] rejected = HexUtils.hexToBytes(reply);
        CRC16Calculator.put(rejected, rejected.length - 4);
        session.accept(rejected);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        assertThat(updates, contains(Map.of(GeniMeasurand.ControlMode, BigDecimal.ZERO),
                Map.of(GeniMeasurand.ControlMode, BigDecimal.ZERO)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "2405F8E70A01010000", "2412F8E70A0E00012F020000070006007FFFFFFF0000",
            "2412F8E70A0E00012E010000070006007FFFFFFF0000", "2412F8E70A0E00012F010000060006007FFFFFFF0000" })
    void failedOrUnsupportedPrereadNeverWrites(String reply) {
        session.write(ModeSetting.ControlMode, 15);
        sends.remove().run();
        byte[] response = HexUtils.hexToBytes(reply);
        CRC16Calculator.put(response, response.length - 4);
        session.accept(response);
        assertThat(packets, contains("2707E7F80A0356000A04D6"));
        assertThat(sends, empty());
    }

    @Test
    void timeoutDiscardsCommandsAndLateRepliesUntilAnewConnection() {
        session.read(GeniReadRequest.OperatingMode);
        session.write(ModeSetting.ControlMode, 13);
        sends.remove().run();
        timeouts.remove().run();
        verify(connectionFailed).run();
        session.accept(HexUtils.hexToBytes(OPERATING_REPLY));
        session.read(GeniReadRequest.ControlMode);
        assertThat(sends, empty());
        assertThat(updates, empty());
        session.start();
        session.read(GeniReadRequest.ControlMode);
        assertThat(sends, empty());
        session.stop();
        session.start();
        session.read(GeniReadRequest.ControlMode);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        assertThat(updates, contains(Map.of(GeniMeasurand.ControlMode, BigDecimal.ZERO)));
        assertThat(sends, empty());
    }

    @Test
    void disconnectDuringFragmentWritePreventsRemainingFragmentsAndCommandReplay() {
        session.write(ModeSetting.ControlMode, 13);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        var fragment = new CompletableFuture<@Nullable Void>();
        writes.add(fragment);
        sends.remove().run();
        session.stop();
        session.start();
        fragment.complete(null);
        assertThat(packets, hasSize(2));
        assertThat(sends, empty());
        verifyNoInteractions(connectionFailed);
    }

    @Test
    void bluetoothFailureInvalidatesSession() {
        writes.add(CompletableFuture.failedFuture(new IllegalStateException("Disconnected")));
        session.read(GeniReadRequest.ControlMode);
        sends.remove().run();
        verify(connectionFailed).run();
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        assertThat(updates, empty());
    }

    @Test
    void replyBeforeBluetoothCompletionDoesNotAdvanceTheQueue() {
        var write = new CompletableFuture<@Nullable Void>();
        writes.add(write);
        session.read(GeniReadRequest.OperatingMode);
        session.read(GeniReadRequest.ControlMode);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(OPERATING_REPLY));
        assertThat(updates, empty());
        assertThat(sends, empty());
        write.complete(null);
        assertThat(updates, contains(Map.of(GeniMeasurand.OperatingMode, BigDecimal.ZERO)));
        sends.remove().run();
        assertThat(packets.getLast(), is("2707E7F80A0356000A04D6"));
    }

    @Test
    void disconnectBetweenPrereadAndWriteCancelsTheWrite() {
        session.write(ModeSetting.ControlMode, 15);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        session.stop();
        session.start();
        sends.remove().run();
        assertThat(packets, contains("2707E7F80A0356000A04D6"));
        assertThat(sends, empty());
    }

    @Test
    void connectionResetDuringStatePublicationPreventsThePendingModeWrite() {
        afterUpdate = () -> {
            session.stop();
            session.start();
        };
        session.write(ModeSetting.ControlMode, 13);
        sends.remove().run();
        session.accept(HexUtils.hexToBytes(CONTROL_REPLY));
        assertThat(sends, empty());
        assertThat(packets, contains("2707E7F80A0356000A04D6"));
    }

    @Test
    void closedSessionCannotRestartAndCancelledSendCannotRunAfterReconnect() {
        session.read(GeniReadRequest.OperatingMode);
        Runnable obsolete = sends.remove();
        session.stop();
        session.start();
        session.read(GeniReadRequest.ControlMode);
        obsolete.run();
        assertThat(packets, empty());
        session.close();
        session.start();
        sends.remove().run();
        session.read(GeniReadRequest.OperatingMode);
        assertThat(packets, empty());
        assertThat(sends, empty());
    }
}
