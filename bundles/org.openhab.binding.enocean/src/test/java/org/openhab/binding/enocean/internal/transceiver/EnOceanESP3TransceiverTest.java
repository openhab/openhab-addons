/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
import org.openhab.binding.enocean.internal.messages.ESP3PacketFactory;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;

/**
 * Tests for {@link EnOceanESP3Transceiver}.
 *
 * @author Ravi Nadahar - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EnOceanESP3TransceiverTest {

    private @Mock @NonNullByDefault({}) TransceiverErrorListener errorListener;
    private @Mock @NonNullByDefault({}) SerialPortManager portManager;
    private @Mock @NonNullByDefault({}) SerialPort port;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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
                Objects.requireNonNull(getClass().getResourceAsStream("/ESP3Stream1.txt")).readAllBytes(),
                StandardCharsets.UTF_8);
        ByteArrayInputStream bis = new ByteArrayInputStream(HexFormat.of().parseHex(hexBytes));
        when(port.getInputStream()).thenReturn(bis);

        CapturingPacketListener sender1Listener = new CapturingPacketListener(0x058DF435L);
        CapturingPacketListener sender2Listener = new CapturingPacketListener(0x0582D29DL);
        EnOceanESP3Transceiver trans = new EnOceanESP3Transceiver("", errorListener, scheduler, portManager);
        trans.addPacketListener(sender1Listener, 0x058DF435L);
        trans.addPacketListener(sender2Listener, 0x0582D29DL);
        trans.initialize();
        trans.startReceiving(scheduler);
        try {
            assertThat(sender1Listener.packets, waitUntil(hasSize(4), 10000L));
            assertThat(sender2Listener.packets, waitUntil(hasSize(4), 10000L));
        } finally {
            trans.shutDownRx();
        }
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(7,
                7, (byte) 1, HexFormat.of().parseHex("F6E0058DF4352001FFFFFFFF5B00"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(7,
                7, (byte) 1, HexFormat.of().parseHex("F6D0058DF4352001FFFFFFFF5900"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(7,
                7, (byte) 1, HexFormat.of().parseHex("F6C0058DF4352001FFFFFFFF5B00"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(7,
                7, (byte) 1, HexFormat.of().parseHex("F6F0058DF4352101FFFFFFFF5C00"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A59E16000F0582D29D0001FFFFFFFF5500"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A59E14000F0582D29D0001FFFFFFFF5500"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A59E13000F0582D29D0001FFFFFFFF5500"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A59E11000F0582D29D0001FFFFFFFF5500"))))));
    }

    @Test
    public void testReceiver2() throws Exception {
        String hexBytes = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/ESP3Stream2.txt")).readAllBytes(),
                StandardCharsets.UTF_8);
        ByteArrayInputStream bis = new ByteArrayInputStream(HexFormat.of().parseHex(hexBytes));
        when(port.getInputStream()).thenReturn(bis);

        CapturingPacketListener sender1Listener = new CapturingPacketListener(0x0582D29DL);
        CapturingPacketListener sender2Listener = new CapturingPacketListener(0x05194A1DL);
        EnOceanESP3Transceiver trans = new EnOceanESP3Transceiver("", errorListener, scheduler, portManager);
        trans.addPacketListener(sender1Listener, 0x0582D29DL);
        trans.addPacketListener(sender2Listener, 0x05194A1DL);
        trans.initialize();
        trans.startReceiving(scheduler);
        try {
            assertThat(sender1Listener.packets, waitUntil(hasSize(35), 10000L));
            assertThat(sender2Listener.packets, waitUntil(hasSize(11), 10000L));
        } finally {
            trans.shutDownRx();
        }
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A59F27000F0582D29D0001FFFFFFFF5C00"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A59F27000F0582D29D0001FFFFFFFF5C00"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A59F27000F0582D29D0001FFFFFFFF5C00"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A5A029000F0582D29D0001FFFFFFFF5800"))))));
        assertThat(sender1Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A5A02B000F0582D29D0001FFFFFFFF5800"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A56D01C30905194A1D0001FFFFFFFF5300"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A56901E20905194A1D0001FFFFFFFF5800"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A56901EB0905194A1D0001FFFFFFFF5600"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A56901F50905194A1D0001FFFFFFFF5600"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A56901FE0905194A1D0001FFFFFFFF5600"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A56902060905194A1D0001FFFFFFFF5900"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(10,
                7, (byte) 1, HexFormat.of().parseHex("A569020D0905194A1D0001FFFFFFFF5C00"))))));
        assertThat(sender2Listener.packets, hasItem(equalPacket(Objects.requireNonNull(ESP3PacketFactory.buildPacket(8,
                7, (byte) 1, HexFormat.of().parseHex("D0066405194A1D0001FFFFFFFF5C00"))))));
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
