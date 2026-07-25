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
package org.openhab.binding.homeconnectdirect.internal.servlet.handler.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.homeconnectdirect.internal.common.utils.ConfigurationUtils;
import org.openhab.binding.homeconnectdirect.internal.handler.model.ApplianceMessage;
import org.openhab.binding.homeconnectdirect.internal.handler.model.MessageType;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

/**
 * Tests for the messages json validation of {@link LogRequestHandler}.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
class LogRequestHandlerTest {

    private final Gson gson = ConfigurationUtils.createGson();

    @Test
    @DisplayName("A serialized appliance message passes validation (guards against field name drift)")
    void testValidSerializedMessage() {
        assertTrue(LogRequestHandler.isValidMessagesJson(validMessagesJson(), gson));
    }

    @Test
    @DisplayName("An empty array is valid")
    void testEmptyArray() {
        assertTrue(LogRequestHandler.isValidMessagesJson("[]", gson));
    }

    @ParameterizedTest(name = "missing ''{0}'' is rejected")
    @ValueSource(strings = { "dateTime", "type", "resource", "action" })
    @DisplayName("A message missing a mandatory field is rejected")
    void testMissingMandatoryField(String field) {
        var array = parseArray(validMessagesJson());
        array.get(0).getAsJsonObject().remove(field);
        assertFalse(LogRequestHandler.isValidMessagesJson(gson.toJson(array), gson));
    }

    @ParameterizedTest(name = "null ''{0}'' is rejected")
    @ValueSource(strings = { "dateTime", "type", "resource", "action" })
    @DisplayName("A message with a null mandatory field is rejected")
    void testNullMandatoryField(String field) {
        var array = parseArray(validMessagesJson());
        var object = array.get(0).getAsJsonObject();
        object.add(field, JsonNull.INSTANCE);
        assertFalse(LogRequestHandler.isValidMessagesJson(gson.toJson(array), gson));
    }

    @Test
    @DisplayName("A json object instead of an array is rejected")
    void testNonArray() {
        assertFalse(LogRequestHandler.isValidMessagesJson("{}", gson));
    }

    @Test
    @DisplayName("A non-object array element is rejected")
    void testNonObjectElement() {
        assertFalse(LogRequestHandler.isValidMessagesJson("[\"foo\"]", gson));
    }

    @Test
    @DisplayName("A malformed date is rejected by the typed parser")
    void testMalformedDate() {
        var array = parseArray(validMessagesJson());
        array.get(0).getAsJsonObject().addProperty("dateTime", "not-a-date");
        assertFalse(LogRequestHandler.isValidMessagesJson(gson.toJson(array), gson));
    }

    @Test
    @DisplayName("Invalid json is rejected")
    void testInvalidJson() {
        assertFalse(LogRequestHandler.isValidMessagesJson("not json", gson));
    }

    private String validMessagesJson() {
        var message = new ApplianceMessage(OffsetDateTime.now(), 1L, MessageType.INCOMING, Resource.RO_VALUES, 1, 1L,
                1L, Action.GET, null, null, null, null);
        return gson.toJson(new ApplianceMessage[] { message });
    }

    private JsonArray parseArray(String json) {
        return Objects.requireNonNull(gson.fromJson(json, JsonArray.class));
    }
}
