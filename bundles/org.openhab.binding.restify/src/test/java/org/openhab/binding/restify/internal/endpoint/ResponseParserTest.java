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

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.restify.internal.servlet.Response;

import tools.jackson.databind.ObjectMapper;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class ResponseParserTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ResponseParser sut = new ResponseParser();

    private static Stream<Arguments> validResponses() {
        return Stream.of(Arguments.of("\"hello\"", new Response.StringResponse("hello")),
                Arguments.of("\"$item.livingRoom.temperature\"",
                        new Response.ItemResponse("livingRoom", "temperature")),
                Arguments.of("\"$thing.bridge.status\"", new Response.ThingResponse("bridge", "status")),
                Arguments.of("{\"message\":\"ok\"}",
                        new Response.JsonResponse(Map.of("message", new Response.StringResponse("ok")))),
                Arguments.of("[\"one\", \"$item.light.state\"]",
                        new Response.ArrayResponse(List.of(new Response.StringResponse("one"),
                                new Response.ItemResponse("light", "state")))),
                Arguments.of("{\"meta\":{\"answer\":\"42\"},\"list\":[\"a\",\"b\"]}", new Response.JsonResponse(Map.of(
                        "meta", new Response.JsonResponse(Map.of("answer", new Response.StringResponse("42"))), "list",
                        new Response.ArrayResponse(
                                List.of(new Response.StringResponse("a"), new Response.StringResponse("b")))))));
    }

    @ParameterizedTest(name = "parses supported response schema [{index}]")
    @MethodSource("validResponses")
    void parseResponseParsesSupportedSchemaNodes(String json, Response expected) throws Exception {
        // Given
        var responseNode = MAPPER.readTree(json);

        // When
        var actual = sut.parseResponse(responseNode);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "fails for unsupported node type [{index}]")
    @MethodSource("unsupportedNodeTypes")
    void parseResponseFailsForUnsupportedNodeTypes(String json, String expectedMessage) throws Exception {
        // Given
        var responseNode = MAPPER.readTree(json);

        // When / Then
        assertThatThrownBy(() -> sut.parseResponse(responseNode)).isInstanceOf(EndpointParseException.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> unsupportedNodeTypes() {
        return Stream.of(Arguments.of("null", "Response schema cannot be null!"),
                Arguments.of("123", "Unsupported schema type: NUMBER"),
                Arguments.of("true", "Unsupported schema type: BOOLEAN"));
    }

    @ParameterizedTest(name = "fails for unsupported string schema [{index}]")
    @MethodSource("unsupportedStringSchemas")
    void parseResponseFailsForUnsupportedStringExpressions(String schema) throws Exception {
        // Given
        var responseNode = MAPPER.readTree("\"%s\"".formatted(schema));

        // When / Then
        assertThatThrownBy(() -> sut.parseResponse(responseNode)).isInstanceOf(EndpointParseException.class)
                .hasMessage("Unsupported schema type: %s".formatted(schema));
    }

    private static Stream<String> unsupportedStringSchemas() {
        return Stream.of("$unknown.entity", "$");
    }
}
