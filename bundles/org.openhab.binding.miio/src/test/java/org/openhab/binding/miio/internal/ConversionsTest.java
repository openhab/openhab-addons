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

    private static final String RECIPES = "{\"recipes\":[{\"recipeID\":1,\"recipeName\":\"Fries\",\"tips\":\"long text\","
            + "\"cookCommand\":{\"time\":11,\"temperature\":180}},{\"recipeID\":2,\"recipeName\":\"Pizza\",\"tips\":\"more\","
            + "\"cookCommand\":{\"time\":15,\"temperature\":200}},{\"recipeName\":\"No id\"}],\"hasMore\":false}";

    private JsonElement getJsonElement(String path, JsonElement value) {
        return Conversions.execute("getJsonElement-" + path, value, Collections.emptyMap());
    }

    @Test
    public void getJsonElementPathTest() {
        // the response of a custom refresh command arrives as a string containing json
        JsonElement asString = new JsonPrimitive(RECIPES);
        JsonElement asObject = JsonParser.parseString(RECIPES);
        for (JsonElement value : new JsonElement[] { asString, asObject }) {
            assertEquals(JsonParser.parseString("false"), getJsonElement("hasMore", value));
            assertEquals(JsonParser.parseString("[1,2]"), getJsonElement("recipes[*].recipeID", value));
            assertEquals(JsonParser.parseString("[11,15]"), getJsonElement("recipes[*].cookCommand.time", value));
            assertEquals(JsonParser.parseString("[\"Fries\",\"Pizza\",\"No id\"]"),
                    getJsonElement("recipes[*].recipeName", value));
            assertEquals(JsonParser.parseString("\"Pizza\""), getJsonElement("recipes[1].recipeName", value));
            assertEquals(JsonParser.parseString("{\"time\":11,\"temperature\":180}"),
                    getJsonElement("recipes[0].cookCommand", value));
            assertEquals(JsonParser.parseString(
                    "[{\"recipeID\":1,\"recipeName\":\"Fries\"},{\"recipeID\":2,\"recipeName\":\"Pizza\"},{\"recipeName\":\"No id\"}]"),
                    getJsonElement("recipes[*].{recipeID,recipeName}", value));
            // projection directly on the array
            assertEquals(JsonParser.parseString("[{\"recipeID\":1},{\"recipeID\":2}]"),
                    getJsonElement("recipes.{recipeID}", value));
            assertEquals(JsonParser.parseString("{\"time\":11}"),
                    getJsonElement("recipes[0].cookCommand.{time,notThere}", value));
        }
    }

    @Test
    public void getJsonElementPathOnArrayTest() {
        JsonElement value = JsonParser.parseString("[{\"id\":1,\"name\":\"a\"},{\"id\":2,\"name\":\"b\"}]");
        assertEquals(JsonParser.parseString("[\"a\",\"b\"]"), getJsonElement("[*].name", value));
        assertEquals(JsonParser.parseString("2"), getJsonElement("[1].id", value));
    }

    @Test
    public void getJsonElementBlankMembersIgnoredTest() {
        JsonElement value = JsonParser.parseString(RECIPES);
        assertEquals(JsonParser.parseString("{\"time\":11}"), getJsonElement("recipes[0].cookCommand.{time,}", value));
        assertEquals(JsonParser.parseString("{\"time\":11}"),
                getJsonElement("recipes[0].cookCommand.{time,,notThere}", value));
        assertEquals(JsonParser.parseString("{\"time\":11,\"temperature\":180}"),
                getJsonElement("recipes[0].cookCommand.{time, ,temperature}", value));
    }

    @Test
    public void getJsonElementExactMemberHasPrecedenceTest() {
        // existing behavior: a member name is used as is, even if it looks like a path
        JsonElement value = JsonParser.parseString("{\"blt.3.17q3si5345k00\":\"-54\",\"blt\":{\"3\":1}}");
        assertEquals(new JsonPrimitive("-54"), getJsonElement("blt.3.17q3si5345k00", value));
    }

    @Test
    public void getJsonElementDatabaseUsagesUnchangedTest() {
        // getJsonElement-current_program etc. (lumi.gateway): custom refresh response passed as string or object
        String fm = "{\"current_program\":527782008,\"current_progress\":1,\"current_volume\":20,\"current_status\":\"pause\"}";
        for (JsonElement value : new JsonElement[] { new JsonPrimitive(fm), JsonParser.parseString(fm) }) {
            assertEquals(new JsonPrimitive(527782008), getJsonElement("current_program", value));
            assertEquals(new JsonPrimitive("pause"), getJsonElement("current_status", value));
            assertEquals(value, getJsonElement("gateway_status", value));
        }
        // a member name on an array response returns the input, as before
        JsonElement array = JsonParser.parseString("[{\"gateway_status\":\"enable\"}]");
        assertEquals(array, getJsonElement("gateway_status", array));
        JsonElement text = new JsonPrimitive("ok");
        assertEquals(text, getJsonElement("gateway_status", text));

        // getDidElement (chuangmi.plug.212a01): result is keyed by the device id
        Map<String, Object> deviceVariables = new HashMap<>();
        deviceVariables.put("deviceId", "123456789");
        JsonElement bleDevices = new JsonPrimitive("{\"123456789\":[{\"did\":\"blt.3.abc\",\"name\":\"Sensor\"}]}");
        assertEquals(JsonParser.parseString("[{\"did\":\"blt.3.abc\",\"name\":\"Sensor\"}]"),
                Conversions.execute("getDidElement", bleDevices, deviceVariables));
        deviceVariables.put("deviceId", "987654321");
        assertEquals(bleDevices, Conversions.execute("getDidElement", bleDevices, deviceVariables));
    }

    @Test
    public void getJsonElementPathNotFoundTest() {
        JsonElement asString = new JsonPrimitive(RECIPES);
        JsonElement asObject = JsonParser.parseString(RECIPES);
        for (JsonElement value : new JsonElement[] { asString, asObject }) {
            // the array exists, but no element has the member
            assertEquals(JsonParser.parseString("[]"), getJsonElement("recipes[*].nothing", value));
            assertEquals(value, getJsonElement("nothing[*].recipeID", value));
            assertEquals(value, getJsonElement("recipes[5].recipeID", value));
            assertEquals(value, getJsonElement("hasMore[*]", value));
            assertEquals(value, getJsonElement("recipes.{recipeID}.x", value));
            assertEquals(value, getJsonElement("recipes[x]", value));
            assertEquals(value, getJsonElement("recipes[99999999999]", value));
            assertEquals(value, getJsonElement("recipes[*", value));
            // empty segments are invalid
            assertEquals(value, getJsonElement("recipes..recipeID", value));
            assertEquals(value, getJsonElement("hasMore.", value));
            assertEquals(value, getJsonElement(".hasMore", value));
            assertEquals(value, getJsonElement(".", value));
            // no members listed
            assertEquals(value, getJsonElement("recipes[0].{}", value));
            assertEquals(value, getJsonElement("recipes[0].{ , }", value));
        }
        JsonElement notJson = new JsonPrimitive("some non json value");
        assertEquals(notJson, getJsonElement("recipes[*].recipeID", notJson));
    }

    // as returned by the Mi Home cloud: the cooking data is a Json string within the recipe
    private static final String CLOUD_RECIPES = "{\"recipes\":[{\"recipeID\":1,\"recipeName\":\"Fries\",\"recipeCommand\":"
            + "\"{\\\"cookCommand\\\":{\\\"time\\\":11,\\\"temperature\\\":180,\\\"preHeat\\\":1},\\\"tips\\\":\\\"long text\\\"}\"},"
            + "{\"recipeID\":2,\"recipeName\":\"Pizza\",\"recipeCommand\":"
            + "\"{\\\"cookCommand\\\":{\\\"time\\\":15,\\\"temperature\\\":200,\\\"preHeat\\\":1},\\\"tips\\\":\\\"more\\\"}\"},"
            + "{\"recipeID\":3,\"recipeName\":\"Broken\",\"recipeCommand\":\"{not json\"}],\"hasMore\":false}";

    @Test
    public void getJsonElementPathThroughJsonStringTest() {
        JsonElement asString = new JsonPrimitive(CLOUD_RECIPES);
        JsonElement asObject = JsonParser.parseString(CLOUD_RECIPES);
        for (JsonElement value : new JsonElement[] { asString, asObject }) {
            assertEquals(JsonParser.parseString("[11,15]"),
                    getJsonElement("recipes[*].recipeCommand.cookCommand.time", value));
            assertEquals(JsonParser.parseString("{\"time\":15,\"temperature\":200,\"preHeat\":1}"),
                    getJsonElement("recipes[1].recipeCommand.cookCommand", value));
            assertEquals(JsonParser.parseString("180"),
                    getJsonElement("recipes[0].recipeCommand.cookCommand.temperature", value));
            // a string at the end of the path is not parsed
            assertEquals(JsonParser.parseString("\"{not json\""), getJsonElement("recipes[2].recipeCommand", value));
            // a string that is not Json can not be followed
            assertEquals(value, getJsonElement("recipes[2].recipeCommand.cookCommand", value));
            assertEquals(value, getJsonElement("recipes[0].recipeName.x", value));
        }
    }

    @Test
    public void getJsonElementIndexedPathThroughJsonStringTest() {
        // the array itself is a Json string within the response
        String response = "{\"recipes\":\"[{\\\"id\\\":1,\\\"name\\\":\\\"Fries\\\"},{\\\"id\\\":2,\\\"name\\\":\\\"Pizza\\\"}]\","
                + "\"text\":\"[not json\"}";
        for (JsonElement value : new JsonElement[] { new JsonPrimitive(response), JsonParser.parseString(response) }) {
            assertEquals(JsonParser.parseString("[1,2]"), getJsonElement("recipes[*].id", value));
            assertEquals(JsonParser.parseString("\"Pizza\""), getJsonElement("recipes[1].name", value));
            assertEquals(JsonParser.parseString("[{\"id\":1},{\"id\":2}]"), getJsonElement("recipes[*].{id}", value));
            assertEquals(value, getJsonElement("recipes[2].id", value));
            // a string that is not a Json array can not be indexed
            assertEquals(value, getJsonElement("text[0]", value));
        }
    }

    @Test
    public void getJsonElementMembersWithPathTest() {
        JsonElement asString = new JsonPrimitive(CLOUD_RECIPES);
        JsonElement asObject = JsonParser.parseString(CLOUD_RECIPES);
        for (JsonElement value : new JsonElement[] { asString, asObject }) {
            // members of a path are named after the last segment; unresolved members are left out
            assertEquals(JsonParser.parseString(
                    "[{\"recipeID\":1,\"recipeName\":\"Fries\",\"time\":11,\"temperature\":180,\"preHeat\":1},"
                            + "{\"recipeID\":2,\"recipeName\":\"Pizza\",\"time\":15,\"temperature\":200,\"preHeat\":1},"
                            + "{\"recipeID\":3,\"recipeName\":\"Broken\"}]"),
                    getJsonElement(
                            "recipes[*].{recipeID,recipeName,recipeCommand.cookCommand.time,recipeCommand.cookCommand.temperature,recipeCommand.cookCommand.preHeat}",
                            value));
            // projection on the Json string itself
            assertEquals(
                    JsonParser.parseString("[{\"cookCommand\":{\"time\":11,\"temperature\":180,\"preHeat\":1}},"
                            + "{\"cookCommand\":{\"time\":15,\"temperature\":200,\"preHeat\":1}}]"),
                    getJsonElement("recipes[*].recipeCommand.{cookCommand}", value));
        }
        // nested projections in a member are not supported
        JsonElement value = JsonParser.parseString(CLOUD_RECIPES);
        assertEquals(JsonParser.parseString("[{\"recipeID\":1},{\"recipeID\":2},{\"recipeID\":3}]"),
                getJsonElement("recipes[*].{recipeID,recipeCommand.{cookCommand}}", value));
    }
}
