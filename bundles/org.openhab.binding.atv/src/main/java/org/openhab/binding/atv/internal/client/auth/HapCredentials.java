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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.InvalidCredentialsError;

/**
 * Identifiers and encryption keys used by HAP.
 *
 * <p>
 * The string form is the four fields hex-encoded (lowercase) and joined with {@code ":"}.
 * Parsing accepts the 4-part HAP form, the 2-part "legacy credentials" form used by AirPlay
 * (where the seed is stored as LTSK and the identifier as client id) and {@code null} for
 * {@link #NO_CREDENTIALS}.
 *
 * <p>
 * The combination of empty/non-empty fields determines the {@link AuthenticationType};
 * invalid combinations are rejected at construction time.
 *
 * @param ltpk the device long-term public key (Ed25519), or the sentinel bytes
 *            {@code "transient"} for transient credentials
 * @param ltsk our long-term secret key (Ed25519 seed)
 * @param atvId the device identifier
 * @param clientId our pairing identifier
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record HapCredentials(byte[] ltpk, byte[] ltsk, byte[] atvId, byte[] clientId) {

    private static final byte[] EMPTY = new byte[0];
    private static final byte[] TRANSIENT_MARKER = "transient".getBytes(StandardCharsets.UTF_8);

    /**
     * No credentials at all.
     */
    public static final HapCredentials NO_CREDENTIALS = new HapCredentials(EMPTY, EMPTY, EMPTY, EMPTY);

    /**
     * Sentinel credentials selecting transient pairing.
     */
    public static final HapCredentials TRANSIENT_CREDENTIALS = new HapCredentials(TRANSIENT_MARKER, EMPTY, EMPTY,
            EMPTY);

    /**
     * Canonical constructor; validates that the field combination maps to a known
     * {@link AuthenticationType}.
     *
     * @throws InvalidCredentialsError if the combination of fields is invalid
     */
    public HapCredentials {
        ltpk = ltpk.clone();
        ltsk = ltsk.clone();
        atvId = atvId.clone();
        clientId = clientId.clone();
        detectType(ltpk, ltsk, atvId, clientId); // validate
    }

    /**
     * The authentication type detected from the field combination.
     *
     * @return the authentication type
     */
    public AuthenticationType type() {
        return detectType(ltpk, ltsk, atvId, clientId);
    }

    private static AuthenticationType detectType(byte[] ltpk, byte[] ltsk, byte[] atvId, byte[] clientId) {
        if (ltpk.length == 0 && ltsk.length == 0 && atvId.length == 0 && clientId.length == 0) {
            return AuthenticationType.Null;
        }
        if (Arrays.equals(ltpk, TRANSIENT_MARKER)) {
            return AuthenticationType.Transient;
        }
        if (ltpk.length == 0 && ltsk.length != 0 && atvId.length == 0 && clientId.length != 0) {
            return AuthenticationType.Legacy;
        }
        if (ltpk.length != 0 && ltsk.length != 0 && atvId.length != 0 && clientId.length != 0) {
            return AuthenticationType.HAP;
        }
        throw new InvalidCredentialsError("invalid credentials type");
    }

    /**
     * Parses a string representation of credentials.
     *
     * @param detailString colon-separated hex string (2-part legacy or 4-part HAP form),
     *            or {@code null} for {@link #NO_CREDENTIALS}
     * @return the parsed credentials
     * @throws InvalidCredentialsError if the string has an unsupported number of parts or
     *             maps to an invalid field combination
     */
    public static HapCredentials parse(@Nullable String detailString) {
        if (detailString == null) {
            return NO_CREDENTIALS;
        }

        String[] split = detailString.split(":", -1);

        // Compatibility with "legacy credentials" used by AirPlay where seed is stored
        // as LTSK and identifier as client_id (others are empty).
        if (split.length == 2) {
            byte[] clientId = unhex(detailString, split[0]);
            byte[] ltsk = unhex(detailString, split[1]);
            return new HapCredentials(EMPTY, ltsk, EMPTY, clientId);
        }
        if (split.length == 4) {
            byte[] ltpk = unhex(detailString, split[0]);
            byte[] ltsk = unhex(detailString, split[1]);
            byte[] atvId = unhex(detailString, split[2]);
            byte[] clientId = unhex(detailString, split[3]);
            return new HapCredentials(ltpk, ltsk, atvId, clientId);
        }

        throw new InvalidCredentialsError("invalid credentials: " + detailString);
    }

    private static byte[] unhex(String detailString, String hex) {
        try {
            return HexFormat.of().parseHex(hex);
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsError("invalid credentials: " + detailString, e);
        }
    }

    @Override
    public byte[] ltpk() {
        return ltpk.clone();
    }

    @Override
    public byte[] ltsk() {
        return ltsk.clone();
    }

    @Override
    public byte[] atvId() {
        return atvId.clone();
    }

    @Override
    public byte[] clientId() {
        return clientId.clone();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (!(other instanceof HapCredentials that)) {
            return false;
        }
        return Arrays.equals(ltpk, that.ltpk) && Arrays.equals(ltsk, that.ltsk) && Arrays.equals(atvId, that.atvId)
                && Arrays.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(ltpk), Arrays.hashCode(ltsk), Arrays.hashCode(atvId),
                Arrays.hashCode(clientId));
    }

    /**
     * String representation: the four fields hex-encoded and colon-joined.
     */
    @Override
    public String toString() {
        HexFormat hex = HexFormat.of();
        return String.join(":", hex.formatHex(ltpk), hex.formatHex(ltsk), hex.formatHex(atvId),
                hex.formatHex(clientId));
    }
}
