/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.miio.internal.basic.Conversions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Test case for {@link ConversionsTest}
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class ConversionsTest {

    @Test
    public void getDidElementTest() {
        Map<String, Object> deviceVariables = new HashMap<>();
        String transformation = "getDidElement";
        JsonElement validInput = new JsonPrimitive(
                "{\"361185596\":\"{\\\"C812105B04000400\\\":\\\"-92\\\",\\\"blt.3.17q3si5345k00\\\":\\\"-54\\\",\\\"blt.4.10heul64og400\\\":\\\"-73\\\"}\"}");

        // test no did in deviceVariables
        JsonElement value = validInput;
        JsonElement transformedResponse = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(transformedResponse);
        assertEquals(value, transformedResponse);

        // test valid input & response
        deviceVariables.put("deviceId", "361185596");
        value = validInput;
        transformedResponse = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(transformedResponse);
        assertEquals(new JsonPrimitive(
                "{\"C812105B04000400\":\"-92\",\"blt.3.17q3si5345k00\":\"-54\",\"blt.4.10heul64og400\":\"-73\"}"),
                transformedResponse);

        // test non json
        value = new JsonPrimitive("some non json value");
        transformedResponse = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(transformedResponse);
        assertEquals(value, transformedResponse);

        // test different did in deviceVariables
        deviceVariables.put("deviceId", "ABC185596");
        value = validInput;
        transformedResponse = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(transformedResponse);
        assertEquals(value, transformedResponse);

        // test empty input
        value = new JsonPrimitive("");
        transformedResponse = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(transformedResponse);
        assertEquals(value, transformedResponse);
    }

    @Test
    public void getJsonElementTest() {
        Map<String, Object> deviceVariables = Collections.emptyMap();

        // test invalid missing element
        String transformation = "getJsonElement";
        JsonElement value = new JsonPrimitive("");
        JsonElement resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(value, resp);

        // test invalid missing element
        value = new JsonPrimitive("{\"test\": \"testresponse\"}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(value, resp);

        transformation = "getJsonElement-test";

        // test non json
        value = new JsonPrimitive("some non json value");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(value, resp);

        // test non json empty string
        value = new JsonPrimitive("");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(value, resp);

        // test input as jsonString
        value = new JsonPrimitive("{\"test\": \"testresponse\"}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(new JsonPrimitive("testresponse"), resp);

        // test input as jsonObject
        value = JsonParser.parseString("{\"test\": \"testresponse\"}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(new JsonPrimitive("testresponse"), resp);

        // test input as jsonString for a number
        value = new JsonPrimitive("{\"test\": 3}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(new JsonPrimitive(3), resp);

        // test input as jsonString for an array
        value = new JsonPrimitive("{\"test\": []}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(new JsonArray(), resp);

        // test input as jsonString for a boolean
        value = new JsonPrimitive("{\"test\": false}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(new JsonPrimitive(false), resp);

        // test input as jsonObject for a number
        value = JsonParser.parseString("{\"test\": 3}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(new JsonPrimitive(3), resp);

        // test input as jsonString for non-existing element
        value = new JsonPrimitive("{\"nottest\": \"testresponse\"}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(value, resp);

        // test input as jsonString for non-existing element
        value = JsonParser.parseString("{\"nottest\": \"testresponse\"}");
        resp = Conversions.execute(transformation, value, deviceVariables);
        assertNotNull(resp);
        assertEquals(value, resp);
    }
}
