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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Manages API keys on a UniFi OS console via the {@code /proxy/users/api/v2/...} endpoints.
 * These endpoints are console-wide (not application-specific), so one manager can serve all
 * child bindings (Protect, Access, Network).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface UniFiApiKeyManager {

    String getUserId() throws UniFiException;

    List<UniFiApiKey> listApiKeys(String userId) throws UniFiException;

    UniFiApiKey createApiKey(String userId, String name) throws UniFiException;

    void deleteApiKey(String keyId) throws UniFiException;

    default @Nullable UniFiApiKey findApiKeyByName(String userId, String name) throws UniFiException {
        return listApiKeys(userId).stream().filter(k -> name.equals(k.name)).findFirst().orElse(null);
    }

    /**
     * Ensures an API key with the given name exists, deleting and recreating it if necessary.
     * Always recreates because the console only returns the full plaintext key at creation time.
     */
    default UniFiApiKey ensureApiKey(String userId, String name) throws UniFiException {
        UniFiApiKey existing = findApiKeyByName(userId, name);
        if (existing != null) {
            String existingId = existing.id;
            if (existingId != null) {
                deleteApiKey(existingId);
            }
        }
        return createApiKey(userId, name);
    }

    /**
     * Auto-provisions an API key for a child binding, returning the full plaintext token.
     * Handles the complete flow: resolve user ID, ensure key exists, return token.
     *
     * @param keyName the name for the API key (e.g. "openHAB-protect-myNvr")
     * @return the full plaintext API token
     * @throws UniFiException if provisioning fails
     */
    default String provisionApiToken(String keyName) throws UniFiException {
        String userId = getUserId();
        UniFiApiKey key = ensureApiKey(userId, keyName);
        String token = key.fullApiKey;
        if (token == null || token.isBlank()) {
            throw new UniFiException("API key '" + keyName + "' was created but fullApiKey is empty");
        }
        return token;
    }
}
