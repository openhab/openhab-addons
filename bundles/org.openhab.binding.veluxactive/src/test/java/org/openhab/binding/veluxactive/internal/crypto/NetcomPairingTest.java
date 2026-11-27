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

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import java.time.Duration;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.crypto.AEADBadTagException;
import javax.crypto.KeyAgreement;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Exercises local pairing against a loopback gateway and verifies cipher vectors independently generated with OpenSSL.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Timeout(20)
class NetcomPairingTest {
    @Test
    void matchesCipherVectorsAndRejectsTampering() throws Exception {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) i;
        }
        var sender = new NetcomPairing.SecureFrames(key);
        var receiver = new NetcomPairing.SecureFrames(key);
        byte[] plain = HexFormat.of().parseHex("0e021100000102030405060708090a0b0c0d0e0f00");
        byte[] first = sender.encrypt(plain);
        assertThat(HexFormat.of().formatHex(first),
                is("16ba5331ade7a4d217645a66a74a442cf4bcfdfae1e32258142e5cf173e9ef89e7456329a7"));
        assertThat(receiver.decrypt(first), is(plain));
        byte[] second = sender.encrypt(plain);
        assertThat(HexFormat.of().formatHex(second),
                is("675e6dd9310bba7929747828bb1d1d191cfc314fce6738e6902097f05198efdba99c45c80b"));
        second[0] ^= 1;
        assertThrows(AEADBadTagException.class, () -> receiver.decrypt(second));
    }

    @Test
    void completesExchangeAndVerifiesKeyChallenge() throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                NetcomPairing pairing = new NetcomPairing("127.0.0.1", server.getLocalPort(), Duration.ofSeconds(15))) {
            server.setSoTimeout(10000);
            var peer = CompletableFuture.supplyAsync(() -> {
                try (Socket socket = server.accept()) {
                    socket.setSoTimeout(10000);
                    assertThat(receive(socket, 0).length, is(0));
                    send(socket, 0, new byte[0]);
                    assertThat(receive(socket, 1).length, is(0));
                    send(socket, 1, new byte[0]);
                    byte[] request = receive(socket, 0x201);
                    assertThat(request[0], is((byte) 1));
                    var keys = KeyPairGenerator.getInstance("X25519").generateKeyPair();
                    send(socket, 0x202, NetcomPairing.littleEndian(((XECPublicKey) keys.getPublic()).getU()));
                    byte[] bigEndian = Arrays.copyOfRange(request, 1, 33);
                    for (int i = 0; i < 16; i++) { // NOPMD - Reverse the wire byte order.
                        byte b = bigEndian[i];
                        bigEndian[i] = bigEndian[31 - i];
                        bigEndian[31 - i] = b;
                    }
                    var remote = KeyFactory.getInstance("X25519").generatePublic(
                            new XECPublicKeySpec(NamedParameterSpec.X25519, new BigInteger(1, bigEndian)));
                    var agreement = KeyAgreement.getInstance("X25519");
                    agreement.init(keys.getPrivate());
                    agreement.doPhase(remote, true);
                    var secure = new NetcomPairing.SecureFrames(
                            Arrays.copyOf(MessageDigest.getInstance("SHA-512").digest(agreement.generateSecret()), 32));
                    byte[] keyRequest = secure.decrypt(receive(socket, 0x200));
                    assertThat(Arrays.copyOf(keyRequest, 5), is(HexFormat.of().parseHex("0e02110000")));
                    byte[] id = Arrays.copyOfRange(keyRequest, 5, 21);
                    byte[] key = new byte[32];
                    Arrays.fill(key, (byte) 0x42);
                    send(socket, 0x200, secure.encrypt(
                            NetcomPairing.frame(0x20f, ByteBuffer.allocate(33).put((byte) 0).put(key).array())));
                    assertThat(secure.decrypt(receive(socket, 0x200)), is(HexFormat.of().parseHex("0a02010000")));
                    byte[] nonce = new byte[16];
                    Arrays.fill(nonce, (byte) 0x19);
                    send(socket, 0x200, secure.encrypt(NetcomPairing.frame(0x20b, nonce)));
                    byte[] challenge = secure.decrypt(receive(socket, 0x200));
                    var digest = MessageDigest.getInstance("SHA-512");
                    digest.update(id);
                    digest.update(key);
                    assertThat(challenge, is(NetcomPairing.frame(0x20c,
                            ByteBuffer.allocate(80).put(id).put(digest.digest(nonce)).array())));
                    send(socket, 0x200, secure.encrypt(NetcomPairing.frame(0x20d, new byte[] { 0 })));
                    assertThat(secure.decrypt(receive(socket, 0x200)), is(HexFormat.of().parseHex("08000000")));
                    return id;
                } catch (Exception e) {
                    throw new java.util.concurrent.CompletionException(e);
                }
            });
            SigningKey result = requireNonNull(pairing.attempt());
            assertThat(result.id(), is(peer.get(15, TimeUnit.SECONDS)));
            assertThat(result.key()[0], is((byte) 0x42));
        }
    }

    @Test
    void cancellationClosesBlockedRead() throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                NetcomPairing pairing = new NetcomPairing("127.0.0.1", server.getLocalPort(), Duration.ofSeconds(15))) {
            server.setSoTimeout(10000);
            var result = CompletableFuture.runAsync(() -> assertThrows(IOException.class, pairing::attempt));
            try (Socket socket = server.accept()) {
                socket.setSoTimeout(10000);
                receive(socket, 0);
                pairing.close();
                result.get(5, TimeUnit.SECONDS);
            }
        }
    }

    @Test
    void rejectsMalformedFramesAndNonAddressInputs() throws Exception {
        assertThrows(IOException.class, () -> new NetcomPairing("https://gateway/"));
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                NetcomPairing pairing = new NetcomPairing("127.0.0.1", server.getLocalPort(), Duration.ofSeconds(15))) {
            server.setSoTimeout(10000);
            var result = CompletableFuture.runAsync(() -> assertThrows(IOException.class, pairing::attempt));
            try (Socket socket = server.accept()) {
                socket.setSoTimeout(10000);
                receive(socket, 0);
                socket.getOutputStream().write(HexFormat.of().parseHex("0100ffff"));
                result.get(5, TimeUnit.SECONDS);
            }
        }
    }

    private static byte[] receive(Socket socket, int type) throws IOException {
        byte[] header = socket.getInputStream().readNBytes(4);
        assertThat(header.length, is(4));
        ByteBuffer fields = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(Short.toUnsignedInt(fields.getShort()), is(type));
        return socket.getInputStream().readNBytes(Short.toUnsignedInt(fields.getShort()));
    }

    private static void send(Socket socket, int type, byte[] payload) throws IOException {
        // Deliberately fragment every frame to exercise TCP reassembly.
        for (byte b : NetcomPairing.frame(type, payload)) {
            socket.getOutputStream().write(b);
        }
    }
}
