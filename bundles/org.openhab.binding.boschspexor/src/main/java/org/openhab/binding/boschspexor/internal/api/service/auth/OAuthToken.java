/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal.api.service.auth;

import static java.util.Optional.empty;
import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.storage.Storage;

import com.nimbusds.oauth2.sdk.token.Tokens;

/**
 * Representation of an OAuthToken retrieved /stored within the storage system
 *
 * @author Marc Fischer - Initial contribution
 *
 */
@NonNullByDefault
public class OAuthToken {

    private static final long BUFFER_OF_EXPIRATION_SEC = 1;

    private Optional<String> accessToken = Optional.empty();
    private Optional<String> refreshToken = Optional.empty();
    private long expiresAt = 0;
    private LocalDateTime createdOn = LocalDateTime.MIN;

    public void save(Storage<String> storage) {
        storage.put(OAUTH2_ACCESSTOKEN, accessToken.get());
        storage.put(OAUTH2_REFRESHTOKEN, refreshToken.get());
        storage.put(OAUTH2_CREATEDON, createdOn.format(DateTimeFormatter.ISO_DATE_TIME));
        storage.put(OAUTH2_EXPIRED_AT, String.valueOf(expiresAt));
    }

    public void reset(Storage<String> storage) {
        storage.remove(OAUTH2_ACCESSTOKEN);
        storage.remove(OAUTH2_REFRESHTOKEN);
        storage.remove(OAUTH2_CREATEDON);
        storage.remove(OAUTH2_EXPIRED_AT);
    }

    public Optional<String> getAccessToken() {
        return accessToken;
    }

    public Optional<String> getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public boolean isAccessTokenExpired() {
        return createdOn.plusSeconds(expiresAt).minusSeconds(BUFFER_OF_EXPIRATION_SEC).isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "OAuthToken [accessToken=" + accessToken + ", refreshToken=" + refreshToken + ", expiresAt=" + expiresAt
                + ", createdOn=" + createdOn + "]";
    }

    public static OAuthToken of(Tokens tokens) {
        OAuthToken token = new OAuthToken();
        token.accessToken = Optional.of(tokens.getAccessToken().getValue());
        token.refreshToken = Optional.of(tokens.getRefreshToken().getValue());
        token.createdOn = LocalDateTime.now();
        token.expiresAt = tokens.getAccessToken().getLifetime();
        return token;
    }

    public static Optional<OAuthToken> load(Storage<String> storage) {
        Optional<OAuthToken> result = empty();
        if (storage.containsKey(OAUTH2_REFRESHTOKEN)) {
            result = Optional.of(new OAuthToken());
            result.get().accessToken = getStorageKey(storage, OAUTH2_ACCESSTOKEN);
            result.get().refreshToken = getStorageKey(storage, OAUTH2_REFRESHTOKEN);
            Optional<String> createdOn = getStorageKey(storage, OAUTH2_CREATEDON);
            result.get().createdOn = LocalDateTime.parse(createdOn.get(), DateTimeFormatter.ISO_DATE_TIME);
            result.get().expiresAt = Integer.valueOf(getStorageKey(storage, OAUTH2_EXPIRED_AT).get());
        }
        return result;
    }

    private static Optional<String> getStorageKey(Storage<String> storage, String key) {
        String value = storage.get(key);
        Optional<String> result = empty();
        if (value != null) {
            result = Optional.of(value);
        }
        return result;
    }
}
