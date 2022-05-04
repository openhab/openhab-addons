/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.network.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.network.internal.WakeOnLanPacketSender.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.util.HexUtils;

/**
 * Tests cases for {@link WakeOnLanPacketSender}.
 *
 * @author Wouter Born - Initial contribution
 */
@Timeout(value = 10)
@NonNullByDefault
public class WakeOnLanPacketSenderTest {

    private void assertValidMagicPacket(byte[] macBytes, byte[] packet) {
        byte[] prefix = new byte[PREFIX_BYTE_SIZE];
        Arrays.fill(prefix, (byte) 0xff);

        assertThat(Arrays.copyOfRange(packet, 0, PREFIX_BYTE_SIZE), is(prefix));

        for (int i = PREFIX_BYTE_SIZE; i < MAGIC_PACKET_BYTE_SIZE; i += MAC_BYTE_SIZE) {
            assertThat(Arrays.copyOfRange(packet, i, i + MAC_BYTE_SIZE), is(macBytes));
        }
    }

    @Test
    public void sendWithColonSeparatedMacAddress() {
        byte[] actualPacket = new byte[MAGIC_PACKET_BYTE_SIZE];

        WakeOnLanPacketSender sender = new WakeOnLanPacketSender("6f:70:65:6e:48:41",
                bytes -> System.arraycopy(bytes, 0, actualPacket, 0, bytes.length));

        sender.sendWakeOnLanPacketViaMac();

        assertValidMagicPacket(HexUtils.hexToBytes("6f:70:65:6e:48:41", ":"), actualPacket);
    }

    @Test
    public void sendWithHyphenSeparatedMacAddress() {
        byte[] actualPacket = new byte[MAGIC_PACKET_BYTE_SIZE];

        WakeOnLanPacketSender sender = new WakeOnLanPacketSender("6F-70-65-6E-48-41",
                bytes -> System.arraycopy(bytes, 0, actualPacket, 0, bytes.length));

        sender.sendWakeOnLanPacketViaMac();

        assertValidMagicPacket(HexUtils.hexToBytes("6F-70-65-6E-48-41", "-"), actualPacket);
    }

    @Test
    public void sendWithNoSeparatedMacAddress() {
        byte[] actualPacket = new byte[MAGIC_PACKET_BYTE_SIZE];

        WakeOnLanPacketSender sender = new WakeOnLanPacketSender("6f70656e4841",
                bytes -> System.arraycopy(bytes, 0, actualPacket, 0, bytes.length));

        sender.sendWakeOnLanPacketViaMac();

        assertValidMagicPacket(HexUtils.hexToBytes("6f70656e4841"), actualPacket);
    }

    @Test
    public void sendWithHostnameAndPort() throws IOException, InterruptedException {
        sendWOLTest("127.0.0.1", 4444);
    }

    @Test
    public void sendWithHostnameAndPortNull() throws IOException, InterruptedException {
        sendWOLTest("127.0.0.1", null);
    }

    @Test
    public void sendWithHostnameNullAndPortNull() {
        assertThrows(IllegalStateException.class, () -> sendWOLTest(null, null));
    }

    @Test
    public void sendWithHostnameNull() {
        assertThrows(IllegalStateException.class, () -> sendWOLTest(null, 4444));
    }

    private void sendWOLTest(@Nullable String hostname, @Nullable Integer port)
            throws InterruptedException, IOException {
        DatagramSocket socket = new DatagramSocket(4444);

        byte[] buf = new byte[256];
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);

        while (socket.isClosed()) {
            Thread.sleep(100);
        }

        try {
            WakeOnLanPacketSender sender = new WakeOnLanPacketSender("6f70656e4841", hostname, port);
            sender.sendWakeOnLanPacketViaIp();

            // This Test is only applicable for IP Requests
            if (hostname != null && port != null) {
                socket.receive(datagramPacket);
            }

            Assertions.assertTrue(datagramPacket.getData().length > 0);
        } finally {
            socket.close();
        }
    }

    @Test
    public void sendWithEmptyMacAddressThrowsException() {
        assertThrows(IllegalStateException.class, () -> new WakeOnLanPacketSender("").sendWakeOnLanPacketViaMac());
    }

    @Test
    public void sendWithTooShortMacAddressThrowsException() {
        assertThrows(IllegalStateException.class,
                () -> new WakeOnLanPacketSender("6f:70:65:6e:48").sendWakeOnLanPacketViaMac());
    }

    @Test
    public void sendWithTooLongMacAddressThrowsException() {
        assertThrows(IllegalStateException.class,
                () -> new WakeOnLanPacketSender("6f:70:65:6e:48:41:42").sendWakeOnLanPacketViaMac());
    }

    @Test
    public void sendWithUnsupportedSeparatorInMacAddressThrowsException() {
        assertThrows(IllegalStateException.class,
                () -> new WakeOnLanPacketSender("6f=70=65=6e=48=41").sendWakeOnLanPacketViaMac());
    }
}
