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
package org.openhab.binding.veluxactive.internal.api.serialization;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.veluxactive.internal.api.dto.serialization.VeluxActiveTokenResponseDeserializer;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.AccessTokenResponseExtraFieldsAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Tests the provider token representations and validation.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveTokenResponseDeserializerTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(AccessTokenResponse.class, new VeluxActiveTokenResponseDeserializer())
            .registerTypeAdapterFactory(new AccessTokenResponseExtraFieldsAdapterFactory()).create();

    @Test
    void acceptsScopeArray() {
        AccessTokenResponse token = requireNonNull(gson.fromJson("""
                {"access_token":"access","refresh_token":"refresh","expires_in":10800,
                 "token_type":"Bearer","scope":["velux_scopes","another_scope"]}
                """, AccessTokenResponse.class));
        assertThat(token.getAccessToken(), is("access"));
        assertThat(token.getRefreshToken(), is("refresh"));
        assertThat(token.getExpiresIn(), is(10800L));
        assertThat(token.getTokenType(), is("Bearer"));
        assertThat(token.getScope(), is("velux_scopes another_scope"));
    }

    @Test
    void acceptsLegacyExpiryAndStringScope() {
        AccessTokenResponse token = requireNonNull(gson.fromJson("""
                {"access_token":"access","expire_in":3600,"scope":"all_scopes"}
                """, AccessTokenResponse.class));
        assertThat(token.getExpiresIn(), is(3600L));
        assertThat(token.getScope(), is("all_scopes"));
        assertThat(token.getRefreshToken(), nullValue());
    }

    @Test
    void acceptsRefreshResponseWithoutScopeOrRefreshToken() {
        AccessTokenResponse token = requireNonNull(gson.fromJson("""
                {"access_token":"new-access","expires_in":3600}
                """, AccessTokenResponse.class));
        assertThat(token.getScope(), nullValue());
        assertThat(token.getRefreshToken(), nullValue());
    }

    @Test
    void acceptsObservedResponseThroughCoreAdapter() {
        AccessTokenResponse token = requireNonNull(gson.fromJson("""
                {"access_token":"access","refresh_token":"refresh","expires_in":10800,
                 "expire_in":10800,"scope":["velux_scopes"]}
                """, AccessTokenResponse.class));
        assertThat(token.getAccessToken(), is("access"));
        assertThat(token.getRefreshToken(), is("refresh"));
        assertThat(token.getExpiresIn(), is(10800L));
        assertThat(token.getScope(), is("velux_scopes"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"access_token\":\"\",\"expires_in\":3600}",
            "{\"access_token\":\"secret\",\"expires_in\":0}", "{\"access_token\":\"secret\",\"expires_in\":-1}",
            "{\"access_token\":\"secret\",\"expires_in\":1.5}",
            "{\"access_token\":\"secret\",\"expires_in\":\"secret\"}",
            "{\"access_token\":\"secret\",\"expires_in\":null}",
            "{\"access_token\":\"secret\",\"expires_in\":3600,\"scope\":[{}]}",
            "{\"access_token\":\"secret\",\"expires_in\":3600,\"scope\":42}",
            "{\"access_token\":{},\"expires_in\":3600}" })
    void rejectsInvalidResponsesWithoutExposingContents(String json) {
        JsonParseException error = assertThrows(JsonParseException.class,
                () -> gson.fromJson(json, AccessTokenResponse.class));
        assertThat(error.getMessage(), is("Invalid VELUX ACTIVE token response"));
        assertThat(error.getCause(), nullValue());
    }
}
