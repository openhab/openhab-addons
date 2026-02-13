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
package org.openhab.binding.restify.internal.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.restify.internal.servlet.Authorization;

import tools.jackson.databind.ObjectMapper;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class AuthorizationParserTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AuthorizationParser sut = new AuthorizationParser();

    @Test
    void parseAuthorizationParsesBasicTypeCaseInsensitive() throws Exception {
        // Given
        var basicUpper = MAPPER.readTree("{\"type\":\"Basic\",\"username\":\"john\",\"password\":\"secret\"}");
        var basicLower = MAPPER.readTree("{\"type\":\"basic\",\"username\":\"john\",\"password\":\"secret\"}");

        // When
        var parsedUpper = sut.parseAuthorization(basicUpper);
        var parsedLower = sut.parseAuthorization(basicLower);

        // Then
        var expected = new Authorization.Basic("john", "secret");
        assertThat(parsedUpper).isEqualTo(expected);
        assertThat(parsedLower).isEqualTo(expected);
    }

    @Test
    void parseAuthorizationParsesBearerTypeCaseInsensitive() throws Exception {
        // Given
        var bearerUpper = MAPPER.readTree("{\"type\":\"Bearer\",\"token\":\"abc-token\"}");
        var bearerLower = MAPPER.readTree("{\"type\":\"bearer\",\"token\":\"abc-token\"}");

        // When
        var parsedUpper = sut.parseAuthorization(bearerUpper);
        var parsedLower = sut.parseAuthorization(bearerLower);

        // Then
        var expected = new Authorization.Bearer("abc-token");
        assertThat(parsedUpper).isEqualTo(expected);
        assertThat(parsedLower).isEqualTo(expected);
    }

    @Test
    void parseAuthorizationFailsWhenNotObject() throws Exception {
        assertParseFails("\"not-an-object\"", "Authorization should be a JSON object!");
        assertParseFails("[]", "Authorization should be a JSON object!");
        assertParseFails("null", "Authorization should be a JSON object!");
    }

    @Test
    void parseAuthorizationFailsWhenTypeMissing() throws Exception {
        assertParseFails("{\"username\":\"john\",\"password\":\"secret\"}", "Missing required field: type");
    }

    @Test
    void parseAuthorizationFailsWhenBasicUsernameMissing() throws Exception {
        assertParseFails("{\"type\":\"Basic\",\"password\":\"secret\"}", "Missing required field: username");
    }

    @Test
    void parseAuthorizationFailsWhenBasicPasswordMissing() throws Exception {
        assertParseFails("{\"type\":\"Basic\",\"username\":\"john\"}", "Missing required field: password");
    }

    @Test
    void parseAuthorizationFailsWhenBearerTokenMissing() throws Exception {
        assertParseFails("{\"type\":\"Bearer\"}", "Missing required field: token");
    }

    @Test
    void parseAuthorizationFailsForUnknownType() throws Exception {
        assertParseFails("{\"type\":\"Token\"}", "Unknown authorization type: Token");
        assertParseFails("{\"type\":\"ApiKey\"}", "Unknown authorization type: ApiKey");
        assertParseFails("{\"type\":\"Digest\"}", "Unknown authorization type: Digest");
    }

    private void assertParseFails(String json, String expectedMessage) throws Exception {
        // Given
        var authorizationNode = MAPPER.readTree(json);

        // When / Then
        assertThatThrownBy(() -> sut.parseAuthorization(authorizationNode)).isInstanceOf(EndpointParseException.class)
                .hasMessage(expectedMessage);
    }
}
