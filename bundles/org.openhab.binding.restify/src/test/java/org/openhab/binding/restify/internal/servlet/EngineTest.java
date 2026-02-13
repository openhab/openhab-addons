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
package org.openhab.binding.restify.internal.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.restify.internal.servlet.Json.BooleanValue;
import org.openhab.binding.restify.internal.servlet.Json.NullValue;
import org.openhab.binding.restify.internal.servlet.Json.NumberValue;
import org.openhab.binding.restify.internal.servlet.Json.StringValue;
import org.openhab.binding.restify.internal.servlet.Response.ArrayResponse;
import org.openhab.binding.restify.internal.servlet.Response.ItemResponse;
import org.openhab.binding.restify.internal.servlet.Response.JsonResponse;
import org.openhab.binding.restify.internal.servlet.Response.StringResponse;
import org.openhab.binding.restify.internal.servlet.Response.ThingResponse;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.types.State;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class EngineTest {
    @Mock
    private ItemRegistry itemRegistry;

    @Mock
    private ThingRegistry thingRegistry;

    @InjectMocks
    private Engine sut;

    @ParameterizedTest
    @MethodSource("nullMappingCases")
    @DisplayName("null mapping helpers return Json null value and non-null values return typed JSON values")
    void nullMappingHelpersMapValuesCorrectly(String type, Object input, Json expected) {
        // When
        var actual = switch (type) {
            case "number" -> sut.numberOrNull((Number) input);
            case "string" -> sut.stringOrNull((String) input);
            case "boolean" -> sut.booleanOrNull((Boolean) input);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };

        // Then
        if (expected == NullValue.NULL_VALUE) {
            assertThat(actual).isSameAs(NullValue.NULL_VALUE);
        } else {
            assertThat(actual).isEqualTo(expected);
        }
    }

    private static Stream<Arguments> nullMappingCases() {
        return Stream.of(Arguments.of("number", null, NullValue.NULL_VALUE),
                Arguments.of("number", 42, new NumberValue(42)), Arguments.of("string", null, NullValue.NULL_VALUE),
                Arguments.of("string", "hello", new StringValue("hello")),
                Arguments.of("boolean", null, NullValue.NULL_VALUE),
                Arguments.of("boolean", true, new BooleanValue(true)));
    }

    @ParameterizedTest
    @MethodSource("simpleJsonResponses")
    @DisplayName("evaluate maps string and array responses into JSON structure")
    void evaluateMapsStringAndArrayResponses(JsonResponse schema, Json.JsonObject expected) throws ParameterException {
        // When
        var actual = sut.evaluate(schema);

        // Then
        assertThat(actual).isEqualTo(expected);
        verifyNoInteractions(itemRegistry, thingRegistry);
    }

    private static Stream<Arguments> simpleJsonResponses() {
        return Stream.of(
                Arguments.of(new JsonResponse(Map.of("message", new StringResponse("ok"))),
                        new Json.JsonObject(Map.of("message", new StringValue("ok")))),
                Arguments.of(
                        new JsonResponse(Map.of("values",
                                new ArrayResponse(List.of(new StringResponse("a"), new StringResponse("b"))))),
                        new Json.JsonObject(Map.of("values",
                                new Json.JsonArray(List.of(new StringValue("a"), new StringValue("b")))))));
    }

    @ParameterizedTest
    @MethodSource("itemExpressionCases")
    @DisplayName("evaluate item expressions returns selected fields and fails for invalid parameters")
    void evaluateItemExpressions(ItemResponse response, Item item, Json expected, String expectedErrorMessageKey)
            throws Exception {
        // Given
        when(itemRegistry.getItem("temperature")).thenReturn(item);

        // When / Then
        if (expectedErrorMessageKey == null) {
            var actual = sut.evaluate(new JsonResponse(Map.of("item", response)));
            assertThat(actual).isEqualTo(new Json.JsonObject(Map.of("item", expected)));
        } else {
            assertThatThrownBy(() -> sut.evaluate(new JsonResponse(Map.of("item", response))))
                    .isInstanceOf(ParameterException.class).hasMessage(expectedErrorMessageKey);
        }
        verify(itemRegistry).getItem("temperature");
    }

    private static Stream<Arguments> itemExpressionCases() {
        var state = org.mockito.Mockito.mock(State.class);
        when(state.toFullString()).thenReturn("23.0 C");
        var item = org.mockito.Mockito.mock(Item.class);
        when(item.getState()).thenReturn(state);

        return Stream.of(Arguments.of(new ItemResponse("temperature", "state"), item, new StringValue("23.0 C"), null),
                Arguments.of(new ItemResponse("temperature", "state,state"), item, null,
                        "servlet.error.duplicate-field"),
                Arguments.of(new ItemResponse("temperature", "unknownField"), item, null, "servlet.error.parameter"));
    }

    @ParameterizedTest
    @MethodSource("itemNotFoundCases")
    @DisplayName("evaluate item response returns JSON null when item does not exist")
    void evaluateItemResponseReturnsNullWhenItemMissing(ItemResponse response) throws Exception {
        // Given
        when(itemRegistry.getItem("missing")).thenThrow(new ItemNotFoundException("missing"));

        // When
        var actual = sut.evaluate(new JsonResponse(Map.of("item", response)));

        // Then
        assertThat(actual).isEqualTo(new Json.JsonObject(Map.of("item", NullValue.NULL_VALUE)));
        verify(itemRegistry).getItem("missing");
    }

    private static Stream<ItemResponse> itemNotFoundCases() {
        return Stream.of(new ItemResponse("missing", ""), new ItemResponse("missing", "state"));
    }

    @ParameterizedTest
    @MethodSource("invalidThingUidCases")
    @DisplayName("evaluate thing response returns JSON null for invalid thing UID")
    void evaluateThingResponseReturnsNullForInvalidUid(String invalidThingUid) throws ParameterException {
        // When
        var actual = sut.evaluate(new JsonResponse(Map.of("thing", new ThingResponse(invalidThingUid, "status"))));

        // Then
        assertThat(actual).isEqualTo(new Json.JsonObject(Map.of("thing", NullValue.NULL_VALUE)));
        verifyNoInteractions(thingRegistry);
    }

    private static Stream<String> invalidThingUidCases() {
        return Stream.of("not-a-uid", "restify::", "abc");
    }
}
