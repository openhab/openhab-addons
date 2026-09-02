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
package org.openhab.binding.enocean.internal.transceiver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ESP2Packet.ESP2PacketType;
import org.openhab.binding.enocean.internal.messages.ESP2PacketConverter;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.util.SameThreadExecutorService;

/**
 * Tests for {@link EnOceanESP2Transceiver}.
 *
 * @author Ravi Nadahar - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EnOceanESP2TransceiverTest {

    private @Mock @NonNullByDefault({}) TransceiverErrorListener errorListener;
    private @Mock @NonNullByDefault({}) SerialPortManager portManager;
    private @Mock @NonNullByDefault({}) SerialPort port;
    private SameThreadExecutorService scheduler = new SameThreadExecutorService();

    @BeforeEach
    void setUp() {
        when(portManager.getIdentifier("")).thenReturn(new SerialPortIdentifier() {

            @Override
            public SerialPort open(String owner, int timeout) throws PortInUseException {
                return port;
            }

            @Override
            public boolean isCurrentlyOwned() {
                return true;
            }

            @Override
            public String getName() {
                return "comx";
            }

            @Override
            public @Nullable String getCurrentOwner() {
                return "OH";
            }
        });
    }

    @Test
    public void testReceiver1() throws Exception {
        String hexBytes = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/ESP2Stream1.txt")).readAllBytes(),
                StandardCharsets.UTF_8);
        ByteArrayInputStream bis = new ByteArrayInputStream(HexFormat.of().parseHex(hexBytes));
        when(port.getInputStream()).thenReturn(bis);

        CapturingPacketListener sender2Listener = new CapturingPacketListener(2L);
        CapturingPacketListener sender5Listener = new CapturingPacketListener(5L);
        EnOceanESP2Transceiver trans = new EnOceanESP2Transceiver("", errorListener, scheduler, portManager);
        trans.addPacketListener(sender2Listener, 2L);
        trans.addPacketListener(sender5Listener, 5L);
        trans.initialize();
        trans.startReceiving(scheduler);
        try {
            assertThat(sender2Listener.packets, waitUntil(hasSize(31), 10000L));
            assertThat(sender5Listener.packets, waitUntil(hasSize(31), 10000L));
        } finally {
            trans.shutDownRx();
        }
        assertThat(sender2Listener.packets,
                hasItem(equalPacket(
                        Objects.requireNonNull(ESP2PacketConverter.buildPacket(ESP2PacketType.RECEIVE_MESSAGE_TELEGRAM,
                                HexFormat.of().parseHex("8B0550000000000000023012"))))));
        assertThat(sender5Listener.packets,
                hasItem(equalPacket(
                        Objects.requireNonNull(ESP2PacketConverter.buildPacket(ESP2PacketType.RECEIVE_MESSAGE_TELEGRAM,
                                HexFormat.of().parseHex("8B0550000000000000053015"))))));
    }

    @Test
    public void testReceiver2() throws Exception {
        String hexBytes = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/ESP2Stream2.txt")).readAllBytes(),
                StandardCharsets.UTF_8);
        ByteArrayInputStream bis = new ByteArrayInputStream(HexFormat.of().parseHex(hexBytes));
        when(port.getInputStream()).thenReturn(bis);

        CapturingPacketListener sender1Listener = new CapturingPacketListener(0x33221118L);
        CapturingPacketListener sender2Listener = new CapturingPacketListener(0x33221320L);
        EnOceanESP2Transceiver trans = new EnOceanESP2Transceiver("", errorListener, scheduler, portManager);
        trans.addPacketListener(sender1Listener, 0x33221118L);
        trans.addPacketListener(sender2Listener, 0x33221320L);
        trans.initialize();
        trans.startReceiving(scheduler);
        try {
            assertThat(sender1Listener.packets, waitUntil(hasSize(6), 10000L));
            assertThat(sender2Listener.packets, waitUntil(hasSize(3), 10000L));
        } finally {
            trans.shutDownRx();
        }
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP2PacketConverter.buildPacket(
                ESP2PacketType.RECEIVE_RADIO_TELEGRAM, HexFormat.of().parseHex("0B05000000003322111820AE"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP2PacketConverter.buildPacket(
                ESP2PacketType.RECEIVE_RADIO_TELEGRAM, HexFormat.of().parseHex("0B05100000003322111830CE"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP2PacketConverter.buildPacket(
                ESP2PacketType.RECEIVE_RADIO_TELEGRAM, HexFormat.of().parseHex("0B05300000003322111830EE"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP2PacketConverter.buildPacket(
                ESP2PacketType.RECEIVE_RADIO_TELEGRAM, HexFormat.of().parseHex("0B0700A0800F3322132000C9"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP2PacketConverter.buildPacket(
                ESP2PacketType.RECEIVE_RADIO_TELEGRAM, HexFormat.of().parseHex("0B07009D800F3322132000C6"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP2PacketConverter.buildPacket(
                ESP2PacketType.RECEIVE_RADIO_TELEGRAM, HexFormat.of().parseHex("0B07009B800F3322132000C4"))))));
    }

    public static <T> Matcher<T> waitUntil(Matcher<T> matcher, long timeoutMs) {
        return new WaitUntil<>(matcher, timeoutMs);
    }

    public static <T extends BasePacket> Matcher<T> equalPacket(BasePacket expected) {
        return new EqualPacket<>(expected);
    }

    public static class EqualPacket<T extends BasePacket> extends BaseMatcher<T> {

        private final BasePacket expected;

        public EqualPacket(BasePacket expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(@Nullable Object actual) {
            if (actual instanceof ERP1Message message && expected instanceof ERP1Message other) {
                return message.getIsTeachIn() == other.getIsTeachIn()
                        && message.getPacketType() == other.getPacketType() && message.getRORG() == other.getRORG()
                        && Arrays.equals(message.getSenderId(), other.getSenderId())
                        && Arrays.equals(message.getPayload(), other.getPayload())
                        && Arrays.equals(message.getOptionalPayload(), other.getOptionalPayload());
            }
            return false;
        }

        @Override
        public void describeTo(@Nullable Description description) {
            if (description != null) {
                description.appendText("equalPacket(").appendValue(expected).appendText(")");
            }
        }
    }

    public static class WaitUntil<T> extends BaseMatcher<T> {

        private final Matcher<T> matcher;
        private final long timeoutMs;

        public WaitUntil(Matcher<T> matcher, long timeoutMs) {
            this.matcher = matcher;
            this.timeoutMs = timeoutMs;
        }

        @Override
        public boolean matches(@Nullable Object actual) {
            long before = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < before) {
                if (matcher.matches(actual)) {
                    return true;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
            return false;
        }

        @Override
        public void describeTo(@Nullable Description description) {
            if (description != null) {
                description.appendText("waitUntil ").appendDescriptionOf(matcher);
            }
        }

        @Override
        public void describeMismatch(@Nullable Object item, @Nullable Description mismatchDescription) {
            matcher.describeMismatch(item, mismatchDescription);
        }
    }

    private static class CapturingPacketListener implements PacketListener {

        private final List<BasePacket> packets = Collections.synchronizedList(new ArrayList<>());
        private final long senderId;

        public CapturingPacketListener(long senderId) {
            this.senderId = senderId;
        }

        @Override
        public void packetReceived(BasePacket packet) {
            packets.add(packet);
        }

        @Override
        public long getEnOceanIdToListenTo() {
            return senderId;
        }
    }
}
