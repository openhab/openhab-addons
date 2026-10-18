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
package org.openhab.binding.unifi.internal.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Persists an authenticated session (cookie + CSRF token) to disk so the binding does not re-login on every
 * restart. Keyed by {@code (host, username)} so multiple consoles / users get independent cache files.
 * <p>
 * Ported from the UniFi Protect binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SessionPersistence {

    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(SessionPersistence.class);

    private final Path sessionFile;
    private static final String SESSION_DIR = Paths
            .get(OpenHAB.getUserDataFolder(), "cache", UniFiBindingConstants.BINDING_ID).toString();

    public SessionPersistence(String host, String username) throws IOException {
        Path sessionDir = Paths.get(SESSION_DIR);
        Files.createDirectories(sessionDir);
        logger.debug("Session directory ensured: {}", sessionDir);

        String filename = sanitizeFilename(host) + "_" + sanitizeFilename(username) + "_session.json";
        this.sessionFile = sessionDir.resolve(filename);
    }

    public void save(SessionData sessionData) throws IOException {
        String json = GSON.toJson(sessionData);
        Files.writeString(sessionFile, json);
        logger.debug("Session saved to: {}", sessionFile);
    }

    public @Nullable SessionData load() throws IOException {
        if (!Files.exists(sessionFile)) {
            logger.debug("No session file found: {}", sessionFile);
            return null;
        }

        String json = Files.readString(sessionFile);
        SessionData data = GSON.fromJson(json, SessionData.class);

        if (data != null && !data.isExpired()) {
            logger.debug("Loaded session from: {}", sessionFile);
            return data;
        } else {
            logger.debug("Session expired, deleting: {}", sessionFile);
            delete();
            return null;
        }
    }

    public void delete() throws IOException {
        Files.deleteIfExists(sessionFile);
        logger.debug("Session deleted: {}", sessionFile);
    }

    private String sanitizeFilename(String input) {
        return input.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /**
     * Session data stored to disk.
     */
    public static class SessionData {
        public @Nullable String cookie;
        public @Nullable String csrfToken;
        public @Nullable Instant expiresAt;

        public SessionData() {
        }

        public SessionData(@Nullable String cookie, @Nullable String csrfToken, @Nullable Instant expiresAt) {
            this.cookie = cookie;
            this.csrfToken = csrfToken;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired() {
            Instant expires = expiresAt;
            if (expires == null) {
                return true;
            }
            return Instant.now().isAfter(expires);
        }
    }
}
