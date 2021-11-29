/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.miio.internal.basic.Conversions;

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

        // test without deviceVariables
        resp = Conversions.execute(transformation, value, null);
        assertNotNull(resp);
        assertEquals(new JsonPrimitive("testresponse"), resp);

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
