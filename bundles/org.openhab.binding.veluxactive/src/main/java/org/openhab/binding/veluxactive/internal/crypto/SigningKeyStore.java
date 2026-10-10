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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants;
import org.openhab.binding.veluxactive.internal.exception.ApiException;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Persists gateway credentials and the last allocated signature timestamp/nonce in private openHAB storage.
 * Allocation is serialized across gateway handlers and saved before a command can be sent.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = SigningKeyStore.class)
public class SigningKeyStore {

    private static final String STORAGE_NAME = "org.openhab.binding." + VeluxActiveBindingConstants.BINDING_ID
            + ".pairings";

    private final Storage<String> storage;
    private final Gson gson = new Gson();

    @Activate
    public SigningKeyStore(@Reference StorageService storageService) {
        storage = storageService.getStorage(STORAGE_NAME, getClass().getClassLoader());
    }

    public synchronized void put(String gatewayId, SigningKey key) {
        storage.put(gatewayId, gson.toJson(new StoredKey(Base64.getUrlEncoder().encodeToString(key.id()),
                Base64.getUrlEncoder().encodeToString(key.key()), 0, 0)));
    }

    public synchronized boolean contains(String gatewayId) {
        return storage.get(gatewayId) != null;
    }

    public synchronized void remove(String gatewayId) {
        storage.remove(gatewayId);
    }

    public synchronized Map<String, Object> sign(String gatewayId, String moduleId, String field, Object value,
            long serverTime, boolean modern) throws ApiException {
        StoredKey stored = read(gatewayId);
        if (stored == null) {
            throw new ApiException("Pair the gateway before sending signed commands.");
        }
        String id = stored.id;
        String encodedKey = stored.key;
        if (id == null || encodedKey == null) {
            throw new ApiException("Stored gateway credentials are invalid; pair again.");
        }
        try {
            SigningKey key = new SigningKey(Base64.getUrlDecoder().decode(id),
                    Base64.getUrlDecoder().decode(encodedKey));
            long timestamp = Math.max(serverTime, stored.timestamp);
            int nonce = timestamp == stored.timestamp ? stored.nonce + 1 : 0;
            if (modern && nonce >= 32767 || !modern && timestamp == stored.timestamp) {
                timestamp++;
                nonce = 0;
            }
            String hash;
            if (modern) {
                Mac mac = Mac.getInstance("HmacSHA512");
                mac.init(new SecretKeySpec(key.key(), "HmacSHA512"));
                hash = Base64.getUrlEncoder().encodeToString(
                        mac.doFinal((field + value + timestamp + nonce + moduleId).getBytes(StandardCharsets.UTF_8)));
            } else {
                MessageDigest digest = MessageDigest.getInstance("SHA-512");
                digest.update((field + value).getBytes(StandardCharsets.UTF_8));
                digest.update(key.key());
                hash = Base64.getUrlEncoder()
                        .encodeToString(digest.digest(Long.toString(timestamp).getBytes(StandardCharsets.UTF_8)));
            }
            storage.put(gatewayId, gson.toJson(new StoredKey(id, encodedKey, timestamp, nonce)));
            return modern ? Map.of("sign_key_id", id, "timestamp", timestamp, "nonce", nonce, "hash_" + field, hash)
                    : Map.of("sign_key_id", id, "timestamp", timestamp, "hash_" + field, hash);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new ApiException("Could not sign the gateway command.");
        }
    }

    private @Nullable StoredKey read(String gatewayId) throws ApiException {
        String json = storage.get(gatewayId);
        if (json == null) {
            return null;
        }
        try {
            StoredKey key = gson.fromJson(json, StoredKey.class);
            if (key == null || key.id == null || key.key == null || key.timestamp < 0 || key.nonce < 0
                    || key.nonce >= 32767) {
                throw new ApiException("Stored gateway credentials are invalid; pair again.");
            }
            return key;
        } catch (JsonParseException e) {
            throw new ApiException("Stored gateway credentials are invalid; pair again.");
        }
    }

    private record StoredKey(@Nullable String id, @Nullable String key, long timestamp, int nonce) {
        @Override
        public String toString() {
            return "StoredKey[redacted]";
        }
    }
}
