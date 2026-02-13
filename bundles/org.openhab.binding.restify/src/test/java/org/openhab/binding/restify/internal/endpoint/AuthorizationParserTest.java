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

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.restify.internal.servlet.Authorization;

import tools.jackson.databind.ObjectMapper;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class AuthorizationParserTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AuthorizationParser sut = new AuthorizationParser();

    private static Stream<Arguments> validAuthorizations() {
        return Stream.of(
                Arguments.of("{\"type\":\"Basic\",\"username\":\"john\",\"password\":\"secret\"}",
                        new Authorization.Basic("john", "secret")),
                Arguments.of("{\"type\":\"basic\",\"username\":\"john\",\"password\":\"secret\"}",
                        new Authorization.Basic("john", "secret")),
                Arguments.of("{\"type\":\"Bearer\",\"token\":\"abc-token\"}", new Authorization.Bearer("abc-token")),
                Arguments.of("{\"type\":\"bearer\",\"token\":\"abc-token\"}", new Authorization.Bearer("abc-token")));
    }

    @ParameterizedTest(name = "parses supported authorization [{index}]")
    @MethodSource("validAuthorizations")
    void parseAuthorizationParsesSupportedTypes(String json, Authorization expected) throws Exception {
        // Given
        var authorizationNode = MAPPER.readTree(json);

        // When
        var actual = sut.parseAuthorization(authorizationNode);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> invalidAuthorizationShape() {
        return Stream.of(Arguments.of("\"not-an-object\""), Arguments.of("[]"), Arguments.of("null"));
    }

    @ParameterizedTest(name = "fails when authorization shape is invalid [{index}]")
    @MethodSource("invalidAuthorizationShape")
    void parseAuthorizationFailsWhenNotObject(String json) throws Exception {
        // Given
        var authorizationNode = MAPPER.readTree(json);

        // When / Then
        assertThatThrownBy(() -> sut.parseAuthorization(authorizationNode)).isInstanceOf(EndpointParseException.class)
                .hasMessage("Authorization should be a JSON object!");
    }

    private static Stream<Arguments> missingRequiredFields() {
        return Stream.of(
                Arguments.of("{\"username\":\"john\",\"password\":\"secret\"}", "Missing required field: type"),
                Arguments.of("{\"type\":\"Basic\",\"password\":\"secret\"}", "Missing required field: username"),
                Arguments.of("{\"type\":\"Basic\",\"username\":\"john\"}", "Missing required field: password"),
                Arguments.of("{\"type\":\"Bearer\"}", "Missing required field: token"));
    }

    @ParameterizedTest(name = "fails when required authorization field missing [{index}]")
    @MethodSource("missingRequiredFields")
    void parseAuthorizationFailsWhenFieldMissing(String json, String expectedMessage) throws Exception {
        // Given
        var authorizationNode = MAPPER.readTree(json);

        // When / Then
        assertThatThrownBy(() -> sut.parseAuthorization(authorizationNode)).isInstanceOf(EndpointParseException.class)
                .hasMessage(expectedMessage);
    }

    @ParameterizedTest(name = "fails for unknown authorization type [{index}]")
    @MethodSource("unknownTypes")
    void parseAuthorizationFailsForUnknownType(String type) throws Exception {
        // Given
        var authorizationNode = MAPPER.readTree("{\"type\":\"%s\"}".formatted(type));

        // When / Then
        assertThatThrownBy(() -> sut.parseAuthorization(authorizationNode)).isInstanceOf(EndpointParseException.class)
                .hasMessage("Unknown authorization type: %s".formatted(type));
    }

    private static Stream<String> unknownTypes() {
        return Stream.of("Token", "ApiKey", "Digest");
    }
}
