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
package org.openhab.binding.veluxactive.internal.crypto;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * One cancellable local Netcom pairing session. The gateway must first be put into retrieve-key mode via the cloud
 * and authorized with its physical button. Framing uses little-endian 16-bit fields; secure frame counters use
 * a big-endian 64-bit counter in the final eight bytes of a 12-byte nonce, with no additional authenticated data.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class NetcomPairing implements AutoCloseable {
    private final InetAddress address;
    private final long deadline;
    private final int port;
    private final byte[] keyId;
    private volatile @Nullable Socket socket;
    private volatile boolean closed;

    public NetcomPairing(String host) throws IOException {
        this(host, 25050, Duration.ofSeconds(60));
    }

    NetcomPairing(String host, int port, Duration timeout) throws IOException {
        this.port = port;
        deadline = System.nanoTime() + timeout.toNanos();
        // Accept only literal addresses; pairing must not block on an unbounded DNS lookup.
        if (!(host.matches("[0-9.]+") || host.contains(":") && host.matches("[0-9a-fA-F:.]+"))) {
            throw new IOException("Supply the gateway's IP address without a port.");
        }
        address = InetAddress.getByName(host);
        UUID id = UUID.randomUUID();
        keyId = ByteBuffer.allocate(16).putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits())
                .array();
    }

    public boolean expired() {
        return closed || System.nanoTime() >= deadline;
    }

    /** Returns null while the gateway's listener is not yet available; handshake errors are terminal. */
    public @Nullable SigningKey attempt() throws IOException, GeneralSecurityException {
        checkDeadline();
        try (Socket connection = new Socket()) {
            socket = connection;
            if (closed) {
                throw new IOException("Pairing cancelled.");
            }
            try {
                connection.connect(new InetSocketAddress(address, port), 1000);
            } catch (ConnectException | SocketTimeoutException e) {
                return null;
            }
            connection.getOutputStream().write(frame(0, new byte[0]));
            if (receive(connection, 1).length != 0) {
                throw new IOException("Invalid Netcom pong.");
            }
            KeyPair pair = KeyPairGenerator.getInstance("X25519").generateKeyPair();
            byte[] publicKey = littleEndian(((XECPublicKey) pair.getPublic()).getU());
            connection.getOutputStream()
                    .write(frame(0x201, ByteBuffer.allocate(33).put((byte) 1).put(publicKey).array()));
            byte[] peer = receive(connection, 0x202);
            if (peer.length == 33 && (peer[0] == 0 || peer[0] == 1)) {
                peer = Arrays.copyOfRange(peer, 1, 33);
            }
            if (peer.length != 32) {
                throw new IOException("Invalid Netcom public key.");
            }
            byte[] bigEndian = peer.clone();
            reverse(bigEndian);
            PublicKey key = KeyFactory.getInstance("X25519")
                    .generatePublic(new XECPublicKeySpec(NamedParameterSpec.X25519, new BigInteger(1, bigEndian)));
            KeyAgreement agreement = KeyAgreement.getInstance("X25519");
            agreement.init(pair.getPrivate());
            agreement.doPhase(key, true);
            byte[] secret = agreement.generateSecret();
            byte[] sessionKey = Arrays.copyOf(MessageDigest.getInstance("SHA-512").digest(secret), 32);
            Arrays.fill(secret, (byte) 0);
            try {
                SecureFrames secure = new SecureFrames(sessionKey);
                sendSecure(connection, secure, 0x20e, ByteBuffer.allocate(17).put((byte) 0).put(keyId).array());
                byte[] response = receiveSecure(connection, secure, 0x20f);
                if (response.length != 33 || response[0] != 0) {
                    throw new IOException("Gateway rejected signing key retrieval.");
                }
                byte[] signingKey = Arrays.copyOfRange(response, 1, 33);
                sendSecure(connection, secure, 0x20a, new byte[] { 0 });
                byte[] challenge = receiveSecure(connection, secure, 0x20b);
                if (challenge.length == 0) {
                    throw new IOException("Empty gateway challenge.");
                }
                MessageDigest digest = MessageDigest.getInstance("SHA-512");
                digest.update(keyId);
                digest.update(signingKey);
                sendSecure(connection, secure, 0x20c,
                        ByteBuffer.allocate(80).put(keyId).put(digest.digest(challenge)).array());
                byte[] verified = receiveSecure(connection, secure, 0x20d);
                if (verified.length != 1 || verified[0] != 0) {
                    throw new IOException("Gateway rejected key verification.");
                }
                sendSecure(connection, secure, 8, new byte[0]);
                return new SigningKey(keyId, signingKey);
            } finally {
                Arrays.fill(sessionKey, (byte) 0);
            }
        } finally {
            socket = null;
        }
    }

    private void sendSecure(Socket connection, SecureFrames secure, int type, byte[] payload)
            throws IOException, GeneralSecurityException {
        checkDeadline();
        connection.getOutputStream().write(frame(0x200, secure.encrypt(frame(type, payload))));
    }

    private byte[] receiveSecure(Socket connection, SecureFrames secure, int expected)
            throws IOException, GeneralSecurityException {
        byte[] plaintext = secure.decrypt(receive(connection, 0x200));
        ByteArrayInputStream input = new ByteArrayInputStream(plaintext);
        Frame frame = readFrame(input);
        if (frame.type() != expected) {
            throw new IOException("Unexpected secure Netcom frame.");
        }
        byte[] payload = frame.payload();
        if (input.available() != 0) {
            throw new IOException("Trailing Netcom data.");
        }
        return payload;
    }

    private byte[] receive(Socket connection, int expected) throws IOException {
        InputStream input = new FilterInputStream(connection.getInputStream()) {
            @Override
            public int read(byte @Nullable [] bytes, int offset, int length) throws IOException {
                checkDeadline();
                connection.setSoTimeout((int) Math.max(1, Math.min(10000, (deadline - System.nanoTime()) / 1000000)));
                return super.read(Objects.requireNonNull(bytes), offset, length);
            }
        };
        while (true) {
            Frame response = readFrame(input);
            if (response.type() == expected) {
                return response.payload();
            }
            if (response.type() == 0 && response.payload().length == 0) {
                connection.getOutputStream().write(frame(1, new byte[0]));
            } else {
                throw new IOException("Unexpected Netcom frame.");
            }
        }
    }

    private record Frame(int type, byte[] payload) {
    }

    private static Frame readFrame(InputStream input) throws IOException {
        byte[] header = input.readNBytes(4);
        if (header.length != 4) {
            throw new EOFException("Incomplete Netcom header.");
        }
        ByteBuffer fields = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int type = Short.toUnsignedInt(fields.getShort());
        int length = Short.toUnsignedInt(fields.getShort());
        if (length > 1024) {
            throw new IOException("Oversized Netcom frame.");
        }
        byte[] payload = input.readNBytes(length);
        if (payload.length != length) {
            throw new EOFException("Incomplete Netcom payload.");
        }
        return new Frame(type, payload);
    }

    static byte[] frame(int type, byte[] payload) {
        return ByteBuffer.allocate(4 + payload.length).order(ByteOrder.LITTLE_ENDIAN).putShort((short) type)
                .putShort((short) payload.length).put(payload).array();
    }

    static byte[] littleEndian(BigInteger value) {
        byte[] bytes = value.toByteArray();
        reverse(bytes);
        return Arrays.copyOf(bytes, 32);
    }

    private static void reverse(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) { // NOPMD - Byte reversal cannot use arraycopy.
            byte b = bytes[i];
            bytes[i] = bytes[bytes.length - 1 - i];
            bytes[bytes.length - 1 - i] = b;
        }
    }

    private void checkDeadline() throws IOException {
        if (expired()) {
            throw new IOException("Pairing cancelled or timed out.");
        }
    }

    @Override
    public void close() {
        closed = true;
        Socket connection = socket;
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException ignored) {
                /* Closing also aborts an in-flight read. */ }
        }
    }

    static final class SecureFrames {
        private final byte[] key;
        private long outgoing;
        private long incoming;

        SecureFrames(byte[] key) {
            this.key = key;
        }

        byte[] encrypt(byte[] plaintext) throws GeneralSecurityException {
            return crypt(Cipher.ENCRYPT_MODE, outgoing++, plaintext);
        }

        byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException {
            return crypt(Cipher.DECRYPT_MODE, incoming++, ciphertext);
        }

        private byte[] crypt(int mode, long counter, byte[] bytes) throws GeneralSecurityException {
            Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
            cipher.init(mode, new SecretKeySpec(key, "ChaCha20"),
                    new IvParameterSpec(ByteBuffer.allocate(12).putInt(0).putLong(counter).array()));
            return cipher.doFinal(bytes);
        }
    }
}
