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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.restify.internal.servlet.Response;

import tools.jackson.databind.ObjectMapper;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class ResponseParserTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ResponseParser sut = new ResponseParser();

    @Test
    void parseResponseParsesPlainString() throws Exception {
        assertParsed("\"hello\"", new Response.StringResponse("hello"));
    }

    @Test
    void parseResponseParsesNumber() throws Exception {
        assertParsed("42", new Response.NumberResponse(42));
    }

    @Test
    void parseResponseParsesBoolean() throws Exception {
        assertParsed("true", new Response.BooleanResponse(true));
    }

    @Test
    void parseResponseParsesNull() throws Exception {
        assertParsed("null", new Response.NullResponse());
    }

    @Test
    void parseResponseParsesItemExpression() throws Exception {
        assertParsed("\"$item.livingRoom.temperature\"", new Response.ItemResponse("livingRoom", "temperature"));
    }

    @Test
    void parseResponseParsesThingExpression() throws Exception {
        assertParsed("\"$thing.bridge.status\"", new Response.ThingResponse("bridge", "status"));
    }

    @Test
    void parseResponseParsesJsonObjectSchema() throws Exception {
        assertParsed("{\"message\":\"ok\"}",
                new Response.JsonResponse(Map.of("message", new Response.StringResponse("ok"))));
    }

    @Test
    void parseResponseParsesArraySchema() throws Exception {
        assertParsed("[\"one\", \"$item.light.state\"]", new Response.ArrayResponse(
                List.of(new Response.StringResponse("one"), new Response.ItemResponse("light", "state"))));
    }

    @Test
    void parseResponseParsesNestedSchema() throws Exception {
        assertParsed("{\"meta\":{\"answer\":\"42\"},\"list\":[\"a\",\"b\"]}",
                new Response.JsonResponse(
                        Map.of("meta", new Response.JsonResponse(Map.of("answer", new Response.StringResponse("42"))),
                                "list", new Response.ArrayResponse(
                                        List.of(new Response.StringResponse("a"), new Response.StringResponse("b"))))));
    }

    @Test
    void parseResponseFailsForUnsupportedStringExpressions() throws Exception {
        assertParseFails("\"$unknown.entity\"", "Unsupported schema type: $unknown.entity");
        assertParseFails("\"$\"", "Unsupported schema type: $");
    }

    private void assertParsed(String json, Response expected) throws Exception {
        // Given
        var responseNode = MAPPER.readTree(json);

        // When
        var actual = sut.parseResponse(responseNode);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    private void assertParseFails(String json, String expectedMessage) throws Exception {
        // Given
        var responseNode = MAPPER.readTree(json);

        // When / Then
        assertThatThrownBy(() -> sut.parseResponse(responseNode)).isInstanceOf(EndpointParseException.class)
                .hasMessage(expectedMessage);
    }
}
