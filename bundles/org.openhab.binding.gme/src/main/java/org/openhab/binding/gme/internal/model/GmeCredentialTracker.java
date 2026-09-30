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
package org.openhab.binding.gme.internal.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;

/**
 * Tracks the age of GME API credentials without persisting the password itself.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public final class GmeCredentialTracker {

    private static final String STORAGE_CREDENTIAL_FINGERPRINT = "credentialFingerprint";
    private static final String STORAGE_PASSWORD_CHANGED_AT = "passwordChangedAt";

    private final Storage<String> storage;
    private final ZoneId zoneId;
    private final Clock clock;

    public GmeCredentialTracker(Storage<String> storage, ZoneId zoneId, Clock clock) {
        this.storage = storage;
        this.zoneId = zoneId;
        this.clock = clock;
    }

    /**
     * Updates credential tracking after a successful GME authentication.
     *
     * <p>
     * The stored password change time is preserved when the credentials have not changed. A new timestamp is stored
     * only after a successful authentication with different credentials.
     *
     * @param username current GME username
     * @param password current GME password
     * @param initialPasswordChangedAt optional YYYY-MM-DD bootstrap date
     * @return the effective password change instant
     */
    public Instant updateAfterSuccessfulAuthentication(String username, String password,
            String initialPasswordChangedAt) {
        String fingerprint = credentialFingerprint(username, password);
        String storedFingerprint = storage.get(STORAGE_CREDENTIAL_FINGERPRINT);
        Instant storedChangedAt = getChangedAt();

        if (fingerprint.equals(storedFingerprint) && storedChangedAt != null) {
            return storedChangedAt;
        }

        Instant changedAt;
        if (storedFingerprint == null && storedChangedAt == null && !initialPasswordChangedAt.isBlank()) {
            changedAt = parseInitialDate(initialPasswordChangedAt);
        } else {
            changedAt = clock.instant();
        }

        storage.put(STORAGE_CREDENTIAL_FINGERPRINT, fingerprint);
        storage.put(STORAGE_PASSWORD_CHANGED_AT, changedAt.toString());

        return changedAt;
    }

    public @Nullable Instant getChangedAt() {
        String value = storage.get(STORAGE_PASSWORD_CHANGED_AT);
        if (value == null) {
            return null;
        }

        try {
            return Instant.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Instant parseInitialDate(String value) {
        try {
            return LocalDate.parse(value.trim()).atStartOfDay(zoneId).toInstant();
        } catch (RuntimeException e) {
            return clock.instant();
        }
    }

    private String credentialFingerprint(String username, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((username + "\0" + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
