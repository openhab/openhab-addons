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
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * SRP-6a server, the counterpart used by fake devices in tests.
 *
 * <p>
 * The server derives the verifier from the username/password/salt, computes the public
 * value {@code B}, and — once it receives the client public {@code A} — derives the session
 * key, validates the client proof {@code M1}, and produces the server proof {@code M2}.
 *
 * <p>
 * Both the HAP flavor (3072-bit, SHA-512, {@code K = H(S)}) and the AirPlay-legacy flavor
 * (2048-bit, SHA-1, {@code K = H(S||00) || H(S||01)}) are supported via the static factories. The
 * private ephemeral {@code b} is injectable so tests can reproduce fixed handshakes.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class SrpServer {

    private final SrpContext context;
    private final String username;
    private final byte[] salt;
    private final BigInteger verifier;
    private final BigInteger serverPrivate;
    private final BigInteger serverPublic;

    private byte @Nullable [] sessionKey;
    private byte @Nullable [] expectedClientProof;
    private byte @Nullable [] serverProof;

    private SrpServer(SrpContext context, String username, String password, byte[] salt,
            Supplier<byte[]> privateSupplier) {
        this.context = context;
        this.username = username;
        this.salt = salt.clone();
        this.verifier = context.verifier(context.passwordHash(salt, username, password));
        this.serverPrivate = new BigInteger(1, privateSupplier.get());
        this.serverPublic = context.serverPublic(verifier, serverPrivate);
    }

    /**
     * Creates a HAP server session (3072-bit group, SHA-512, K = H(S)).
     *
     * @param username the SRP username (typically {@code "Pair-Setup"})
     * @param password the PIN
     * @param salt the salt bytes
     * @param privateSupplier supplies the raw bytes of the server private ephemeral {@code b}
     */
    public static SrpServer hap(String username, String password, byte[] salt, Supplier<byte[]> privateSupplier) {
        return new SrpServer(SrpContext.hap(), username, password, salt, privateSupplier);
    }

    /**
     * Creates an AirPlay-legacy server session (2048-bit group, SHA-1, K = H(S||00) || H(S||01)).
     *
     * @param username the SRP username (uppercase-hex client identifier)
     * @param password the PIN
     * @param salt the salt bytes
     * @param privateSupplier supplies the raw bytes of the server private ephemeral {@code b}
     */
    public static SrpServer legacyAirPlay(String username, String password, byte[] salt,
            Supplier<byte[]> privateSupplier) {
        return new SrpServer(SrpContext.legacyAirPlay(), username, password, salt, privateSupplier);
    }

    /**
     * The server public value {@code B} as minimal big-endian bytes.
     */
    public byte[] publicKey() {
        return SrpContext.intToBytes(serverPublic);
    }

    /**
     * The salt bytes.
     */
    public byte[] salt() {
        return salt.clone();
    }

    /**
     * The password verifier {@code v} as minimal big-endian bytes.
     */
    public byte[] verifier() {
        return SrpContext.intToBytes(verifier);
    }

    /**
     * Processes the client public key: derives the session key and both proofs.
     *
     * @param clientPublicKey the client public value A, big-endian
     */
    public void process(byte[] clientPublicKey) {
        BigInteger clientPublic = new BigInteger(1, clientPublicKey);
        BigInteger u = context.commonSecret(clientPublic, serverPublic);
        BigInteger premaster = context.serverPremaster(verifier, serverPrivate, clientPublic, u);
        byte[] localSessionKey = context.sessionKey(premaster);
        byte[] localClientProof = context.clientProof(localSessionKey, salt, serverPublic, clientPublic, username);
        this.sessionKey = localSessionKey;
        this.expectedClientProof = localClientProof;
        this.serverProof = context.serverProof(localSessionKey, localClientProof, clientPublic);
    }

    /**
     * Validates the client proof {@code M1}.
     *
     * @param clientProof the proof received from the client
     * @return true if it matches the locally computed value
     */
    public boolean verifyClientProof(byte[] clientProof) {
        return Arrays.equals(clientProof, expectedClientProof);
    }

    /**
     * The server proof {@code M2 = H(A || M1 || K)}.
     */
    public byte[] proof() {
        return Objects.requireNonNull(serverProof, "process() must be called first").clone();
    }

    /**
     * The negotiated session key {@code K}.
     */
    public byte[] sessionKey() {
        return Objects.requireNonNull(sessionKey, "process() must be called first").clone();
    }
}
