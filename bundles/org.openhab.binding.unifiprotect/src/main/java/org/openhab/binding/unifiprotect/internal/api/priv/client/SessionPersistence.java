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
package org.openhab.binding.unifiprotect.internal.api.priv.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.gson.JsonUtil;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles persistence of authentication session to disk
 * Saves cookies and CSRF tokens to avoid re-authenticating on every restart
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SessionPersistence {

    private final Logger logger = LoggerFactory.getLogger(SessionPersistence.class);

    private final Path sessionFile;
    private static final String SESSION_DIR = Paths
            .get(OpenHAB.getUserDataFolder(), "cache", UnifiProtectBindingConstants.BINDING_ID).toString();

    /**
     * Create a session persistence handler for the given host and username
     * 
     * @param host the UniFi Protect host
     * @param username the username
     * @throws IOException if the session directory cannot be created
     */
    public SessionPersistence(String host, String username) throws IOException {
        Path sessionDir = Paths.get(SESSION_DIR);
        Files.createDirectories(sessionDir);
        logger.debug("Session directory ensured: {}", sessionDir);

        String filename = sanitizeFilename(host) + "_" + sanitizeFilename(username) + "_session.json";
        this.sessionFile = sessionDir.resolve(filename);
    }

    /**
     * Save session data to disk
     * 
     * @param sessionData the session data to save
     * @throws IOException if the session cannot be written to disk
     */
    public void save(SessionData sessionData) throws IOException {
        String json = JsonUtil.toJson(sessionData);
        Files.writeString(sessionFile, json);
        logger.debug("Session saved to: {}", sessionFile);
    }

    /**
     * Load session data from disk
     * Returns null if no session exists or if it's expired
     * 
     * @return the loaded session data, or null if no valid session exists
     * @throws IOException if the session file exists but cannot be read
     */
    public @Nullable SessionData load() throws IOException {
        if (!Files.exists(sessionFile)) {
            logger.debug("No session file found: {}", sessionFile);
            return null;
        }

        String json = Files.readString(sessionFile);
        SessionData data = JsonUtil.fromJson(json, SessionData.class);

        if (data != null && !data.isExpired()) {
            logger.debug("Loaded session from: {}", sessionFile);
            return data;
        } else {
            logger.debug("Session expired, deleting: {}", sessionFile);
            delete();
            return null;
        }
    }

    /**
     * Delete session file
     * 
     * @throws IOException if the session file exists but cannot be deleted
     */
    public void delete() throws IOException {
        Files.deleteIfExists(sessionFile);
        logger.debug("Session deleted: {}", sessionFile);
    }

    /**
     * Sanitize filename to be safe for all OS
     */
    private String sanitizeFilename(String input) {
        return input.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /**
     * Session data stored to disk
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
