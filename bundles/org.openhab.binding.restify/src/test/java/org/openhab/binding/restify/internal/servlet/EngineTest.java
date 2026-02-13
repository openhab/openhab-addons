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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    void numberOrNullMapsNullAndNumberValues() {
        assertThat(sut.numberOrNull(null)).isSameAs(NullValue.NULL_VALUE);
        assertThat(sut.numberOrNull(42)).isEqualTo(new NumberValue(42));
    }

    @Test
    void stringOrNullMapsNullAndStringValues() {
        assertThat(sut.stringOrNull(null)).isSameAs(NullValue.NULL_VALUE);
        assertThat(sut.stringOrNull("hello")).isEqualTo(new StringValue("hello"));
    }

    @Test
    void booleanOrNullMapsNullAndBooleanValues() {
        assertThat(sut.booleanOrNull(null)).isSameAs(NullValue.NULL_VALUE);
        assertThat(sut.booleanOrNull(true)).isEqualTo(new BooleanValue(true));
    }

    @Test
    void evaluateMapsStringResponse() throws ParameterException {
        // Given
        var schema = new JsonResponse(Map.of("message", new StringResponse("ok")));

        // When
        var actual = sut.evaluate(schema);

        // Then
        assertThat(actual).isEqualTo(new Json.JsonObject(Map.of("message", new StringValue("ok"))));
        verifyNoInteractions(itemRegistry, thingRegistry);
    }

    @Test
    void evaluateMapsArrayResponse() throws ParameterException {
        // Given
        var schema = new JsonResponse(
                Map.of("values", new ArrayResponse(List.of(new StringResponse("a"), new StringResponse("b")))));

        // When
        var actual = sut.evaluate(schema);

        // Then
        assertThat(actual).isEqualTo(new Json.JsonObject(
                Map.of("values", new Json.JsonArray(List.of(new StringValue("a"), new StringValue("b"))))));
        verifyNoInteractions(itemRegistry, thingRegistry);
    }

    @Test
    void evaluateItemExpressionReturnsState() throws Exception {
        // Given
        var state = org.mockito.Mockito.mock(State.class);
        when(state.toFullString()).thenReturn("23.0 C");
        var item = org.mockito.Mockito.mock(Item.class);
        when(item.getState()).thenReturn(state);
        when(itemRegistry.getItem("temperature")).thenReturn(item);
        var schema = new JsonResponse(Map.of("item", new ItemResponse("temperature", "state")));

        // When
        var actual = sut.evaluate(schema);

        // Then
        assertThat(actual).isEqualTo(new Json.JsonObject(Map.of("item", new StringValue("23.0 C"))));
        verify(itemRegistry).getItem("temperature");
    }

    @Test
    void evaluateItemExpressionFailsForDuplicateSelectedField() throws Exception {
        // Given
        var state = org.mockito.Mockito.mock(State.class);
        when(state.toFullString()).thenReturn("23.0 C");
        var item = org.mockito.Mockito.mock(Item.class);
        when(item.getState()).thenReturn(state);
        when(itemRegistry.getItem("temperature")).thenReturn(item);
        var schema = new JsonResponse(Map.of("item", new ItemResponse("temperature", "state,state")));

        // When / Then
        assertThatThrownBy(() -> sut.evaluate(schema)).isInstanceOf(ParameterException.class)
                .hasMessage("servlet.error.duplicate-field");
        verify(itemRegistry).getItem("temperature");
    }

    @Test
    void evaluateItemExpressionFailsForUnknownField() throws Exception {
        // Given
        var item = org.mockito.Mockito.mock(Item.class);
        when(itemRegistry.getItem("temperature")).thenReturn(item);
        var schema = new JsonResponse(Map.of("item", new ItemResponse("temperature", "unknownField")));

        // When / Then
        assertThatThrownBy(() -> sut.evaluate(schema)).isInstanceOf(ParameterException.class)
                .hasMessage("servlet.error.parameter");
        verify(itemRegistry).getItem("temperature");
    }

    @Test
    void evaluateItemResponseReturnsNullWhenItemMissing() throws Exception {
        // Given
        when(itemRegistry.getItem("missing")).thenThrow(new ItemNotFoundException("missing"));

        // When
        var withoutExpression = sut.evaluate(new JsonResponse(Map.of("item", new ItemResponse("missing", ""))));
        var withExpression = sut.evaluate(new JsonResponse(Map.of("item", new ItemResponse("missing", "state"))));

        // Then
        var expected = new Json.JsonObject(Map.of("item", NullValue.NULL_VALUE));
        assertThat(withoutExpression).isEqualTo(expected);
        assertThat(withExpression).isEqualTo(expected);
    }

    @Test
    void evaluateThingResponseReturnsNullForInvalidUid() throws ParameterException {
        // When
        var invalidOne = sut.evaluate(new JsonResponse(Map.of("thing", new ThingResponse("not-a-uid", "status"))));
        var invalidTwo = sut.evaluate(new JsonResponse(Map.of("thing", new ThingResponse("restify::", "status"))));
        var invalidThree = sut.evaluate(new JsonResponse(Map.of("thing", new ThingResponse("abc", "status"))));

        // Then
        var expected = new Json.JsonObject(Map.of("thing", NullValue.NULL_VALUE));
        assertThat(invalidOne).isEqualTo(expected);
        assertThat(invalidTwo).isEqualTo(expected);
        assertThat(invalidThree).isEqualTo(expected);
        verifyNoInteractions(thingRegistry);
    }
}
