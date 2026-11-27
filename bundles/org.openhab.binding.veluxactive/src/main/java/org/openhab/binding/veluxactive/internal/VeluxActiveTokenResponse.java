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
package org.openhab.binding.veluxactive.internal;

import java.math.BigDecimal;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * VELUX token response with array scopes and a legacy expiry field.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public final class VeluxActiveTokenResponse {
    @SerializedName("access_token")
    private @Nullable String accessToken;
    @SerializedName("refresh_token")
    private @Nullable String refreshToken;
    @SerializedName("token_type")
    private @Nullable String tokenType;
    @SerializedName("expires_in")
    private @Nullable BigDecimal expiresIn;
    @SerializedName("expire_in")
    private @Nullable BigDecimal legacyExpiresIn;
    private @Nullable JsonElement scope;
    private @Nullable String state;

    public AccessTokenResponse toStandard() {
        String accessToken = this.accessToken;
        JsonElement scope = this.scope;
        BigDecimal expiry = expiresIn != null ? expiresIn : legacyExpiresIn;
        long lifetime = expiry != null ? expiry.longValueExact() : 0;
        if (accessToken == null || accessToken.isBlank() || lifetime <= 0) {
            throw new JsonParseException("Missing token or invalid expiry");
        }
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType(tokenType);
        response.setExpiresIn(lifetime);
        response.setState(state);
        if (scope != null && !scope.isJsonNull()) {
            if (scope.isJsonArray()) {
                StringJoiner scopes = new StringJoiner(" ");
                for (JsonElement entry : scope.getAsJsonArray()) {
                    scopes.add(scopeString(entry));
                }
                response.setScope(scopes.toString());
            } else {
                response.setScope(scopeString(scope));
            }
        }
        return response;
    }

    private static String scopeString(JsonElement value) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("Expected a scope string");
        }
        return value.getAsString();
    }
}
