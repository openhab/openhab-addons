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
package org.openhab.binding.e3dc.internal.rscp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.zip.CRC32;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A fake E3/DC device on the loopback interface for tests.
 *
 * <p>
 * It accepts one connection, grants the authentication and answers every further request with the frame its
 * supplier delivers. The frames are built byte by byte here, independent of the codec under test.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class FakeRscpDevice implements AutoCloseable {

    /** The encryption key the fake device and its clients share. */
    public static final String RSCP_PASSWORD = "meinRscpPasswort";

    /** The access level a device grants to a user account. */
    static final int AUTH_LEVEL_USER = 10;

    private final ServerSocket server;
    private final Thread thread;

    private FakeRscpDevice(Supplier<byte[]> answers) throws IOException {
        server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        thread = new Thread(() -> serve(answers), "fake-e3dc-device");
        thread.setDaemon(true);
        thread.start();
    }

    /** Starts a device whose answer to every request after the authentication is the payload of the supplier. */
    public static FakeRscpDevice start(Supplier<byte[]> answerPayloads) throws IOException {
        return new FakeRscpDevice(answerPayloads);
    }

    /** The port the device listens on. */
    public int port() {
        return server.getLocalPort();
    }

    private void serve(Supplier<byte[]> answers) {
        try (Socket socket = server.accept()) {
            socket.setSoTimeout(10_000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            RscpCipher cipher = new RscpCipher(RSCP_PASSWORD.getBytes(StandardCharsets.UTF_8));
            readEncryptedFrame(in, cipher);
            out.write(cipher.encrypt(frame(item(RscpTag.RSCP_AUTHENTICATION, RscpType.UCHAR8.getCode(),
                    new byte[] { (byte) AUTH_LEVEL_USER }))));
            out.flush();
            while (!Thread.currentThread().isInterrupted()) {
                readEncryptedFrame(in, cipher);
                out.write(cipher.encrypt(frame(answers.get())));
                out.flush();
            }
        } catch (IOException e) {
            // the client closed the connection, which ends the script
        }
    }

    @Override
    public void close() throws IOException {
        thread.interrupt();
        server.close();
    }

    static byte[] item(int tag, int type, byte[] data) {
        ByteBuffer header = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN).putInt(tag).put((byte) type)
                .putShort((short) data.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(header.array(), 0, 7);
        out.write(data, 0, data.length);
        return out.toByteArray();
    }

    /** An item that carries no value, the answer to a request for a tag the device does not know. */
    public static byte[] emptyContainer(int tag) {
        return item(tag, RscpType.CONTAINER.getCode(), new byte[0]);
    }

    static byte[] frame(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.allocate(RscpCodec.HEADER_LENGTH + payload.length + 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(RscpCodec.MAGIC_BYTES[0]);
        buffer.put(RscpCodec.MAGIC_BYTES[1]);
        // the control bytes as they travel: zero, then the version with the CRC flag
        buffer.put((byte) 0);
        buffer.put((byte) RscpCodec.VERSION);
        buffer.putLong(1_600_000_000L);
        buffer.putInt(0);
        buffer.putShort((short) payload.length);
        buffer.put(payload);
        byte[] bytes = buffer.array();
        CRC32 crc = new CRC32();
        crc.update(bytes, 0, bytes.length - 4);
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putInt(bytes.length - 4, (int) crc.getValue());
        return bytes;
    }

    /** Reads one complete encrypted frame, mirroring what a device has to do. */
    static byte[] readEncryptedFrame(InputStream in, RscpCipher cipher) throws IOException {
        int needed = Rijndael256.BLOCK;
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        int total = -1;
        while (true) {
            if (raw.size() < needed) {
                byte[] chunk = RscpCodec.readFully(in, needed - raw.size());
                raw.write(chunk, 0, chunk.length);
            }
            byte[] encrypted = raw.toByteArray();
            if (total < 0) {
                byte[] probe = cipher.peek(encrypted);
                // the CRC flag sits in the second control byte, index 3
                int ctrl = probe[3] & 0xFF;
                int dataLength = ByteBuffer.wrap(probe, 16, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
                total = RscpCodec.HEADER_LENGTH + dataLength + ((ctrl & RscpCodec.CTRL_CRC) != 0 ? 4 : 0);
                needed = ((total + Rijndael256.BLOCK - 1) / Rijndael256.BLOCK) * Rijndael256.BLOCK;
            }
            if (encrypted.length >= needed) {
                break;
            }
        }
        return Arrays.copyOf(cipher.decrypt(raw.toByteArray()), total);
    }
}
