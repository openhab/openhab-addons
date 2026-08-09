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
package org.openhab.binding.atv.internal.client.auth;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;

/**
 * Shared SRP-6a client logic for both HAP and AirPlay-legacy flavors.
 *
 * <p>
 * The private ephemeral {@code a} is injectable via a {@link Supplier} of raw big-endian bytes so
 * tests can reproduce fixed handshakes; the default supplier draws 32 secure-random bytes (the same
 * size as an Ed25519 auth private key, which HAP pairing feeds in as the SRP private value).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSrpClient {

    private final SrpContext context;
    private final Supplier<byte[]> privateSupplier;

    private String username = "";
    private String password = "";
    private @Nullable BigInteger clientPrivate;
    private @Nullable BigInteger clientPublic;
    private byte @Nullable [] sessionKey;
    private byte @Nullable [] clientProof;
    private byte @Nullable [] expectedServerProof;

    /**
     * @param context the SRP arithmetic context (group + hash + session-key derivation)
     * @param privateSupplier supplies the raw bytes of the client private ephemeral {@code a}
     */
    protected AbstractSrpClient(SrpContext context, Supplier<byte[]> privateSupplier) {
        this.context = context;
        this.privateSupplier = privateSupplier;
    }

    /** Default supplier: 32 secure-random bytes. */
    protected static Supplier<byte[]> secureRandomSupplier() {
        SecureRandom random = new SecureRandom();
        return () -> {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            return bytes;
        };
    }

    /** Begins a session: fixes the username/password and computes the client public {@code A}. */
    protected void begin(String username, String password) {
        this.username = username;
        this.password = password;
        BigInteger localClientPrivate = new BigInteger(1, privateSupplier.get());
        this.clientPrivate = localClientPrivate;
        this.clientPublic = context.clientPublic(localClientPrivate);
    }

    /**
     * Processes the server public key and salt: derives the session key and both proofs.
     *
     * @param serverPublicKey the server public value B, big-endian
     * @param salt the salt bytes
     * @throws AuthenticationError if B is invalid (B mod N == 0)
     */
    public void process(byte[] serverPublicKey, byte[] salt) {
        BigInteger serverPublic = new BigInteger(1, serverPublicKey);
        if (serverPublic.mod(context.group().prime()).signum() == 0) {
            throw new AuthenticationError("invalid server public key");
        }
        BigInteger localClientPublic = Objects.requireNonNull(clientPublic, "begin() must be called first");
        BigInteger localClientPrivate = Objects.requireNonNull(clientPrivate, "begin() must be called first");
        BigInteger passwordHash = context.passwordHash(salt, username, password);
        BigInteger u = context.commonSecret(localClientPublic, serverPublic);
        BigInteger premaster = context.clientPremaster(passwordHash, serverPublic, localClientPrivate, u);
        byte[] localSessionKey = context.sessionKey(premaster);
        byte[] localClientProof = context.clientProof(localSessionKey, salt, serverPublic, localClientPublic, username);
        this.sessionKey = localSessionKey;
        this.clientProof = localClientProof;
        this.expectedServerProof = context.serverProof(localSessionKey, localClientProof, localClientPublic);
    }

    /** The client public value {@code A} as minimal big-endian bytes. */
    public byte[] publicKey() {
        return SrpContext.intToBytes(Objects.requireNonNull(clientPublic, "begin() must be called first"));
    }

    /** The client proof {@code M1}. */
    public byte[] proof() {
        return Objects.requireNonNull(clientProof, "process() must be called first").clone();
    }

    /** The negotiated session key {@code K}. */
    public byte[] sessionKey() {
        return Objects.requireNonNull(sessionKey, "process() must be called first").clone();
    }

    /**
     * Verifies the server proof {@code M2 = H(A || M1 || K)}.
     *
     * @param serverProof the proof received from the server
     * @return true if it matches the locally computed value
     */
    public boolean verifyServerProof(byte[] serverProof) {
        return Arrays.equals(serverProof, expectedServerProof);
    }
}
