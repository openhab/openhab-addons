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
package org.openhab.io.hueemulation.internal.dto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueCommand;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Hue API rule object
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueRuleEntry {
    // A unique, editable name given to the group.
    public String name = "";
    public String description = "";

    public String owner = "";

    public enum Operator {
        unknown,
        eq, // equals, Used for bool and int.
        gt, // greater than, Allowed on int values.
        lt, // less than, Allowed on int values.
        dx, // value has changed, Time (timestamps) int and bool values. Only dx or ddx is allowed, but not both.
        ddx, // delayed value has changed
        stable, // Time (timestamps) int and bool values. An attribute has or has not changed for a given time.
        not_stable,
        in, // Current time is in or not in given time interval (only for /config/localtime, not UTC). “in” rule will be
            // triggered on starttime and “not in” rule will be triggered on endtime. Only one “in” operator is allowed
            // in a rule. Multiple “not in” operators are allowed in a rule.
        not_in
    }

    /**
     * A complete condition could look like this:
     * <p>
     *
     * <pre>
     * {
     *       "address": "/sensors/2/state/buttonevent",
     *       "operator": "eq",
     *       "value": "16"
     * },
     * </pre>
     */
    public static class Condition {
        /**
         * A hue resource address like "/config/localtime" or "/sensors/2/state/buttonevent"
         */
        public String address = "";
        public Operator operator = Operator.unknown;
        /**
         * A value like "16"
         */
        public @Nullable String value;

        public Condition() {
        }

        public Condition(String address, Operator operator, @Nullable String value) {
            this.address = address;
            this.value = value;
            this.operator = operator;
        }
    }

    // The IDs of the lights that are in the group.
    public List<Condition> conditions = new ArrayList<>();
    public List<HueCommand> actions = new ArrayList<>();

    HueRuleEntry() {
        name = "";
    }

    public HueRuleEntry(@Nullable String name) {
        this.name = name != null ? name : "";
    }

    /**
     * This custom serializer (de)serialize the Condition class and translates between the enum values that have
     * an underbar and the json strings that have a whitespace instead.
     */
    @NonNullByDefault({})
    public static class SerializerCondition
            implements JsonSerializer<HueRuleEntry.Condition>, JsonDeserializer<HueRuleEntry.Condition> {
        @Override
        public JsonElement serialize(HueRuleEntry.Condition product, Type type, JsonSerializationContext context) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("address", product.address);
            String value = product.value;
            if (value != null) {
                jObj.addProperty("value", value);
            }
            jObj.addProperty("operator", product.operator.name().replace("_", " "));

            return jObj;
        }

        @Override
        public Condition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jObj = json.getAsJsonObject();
            Condition c = new Condition();

            c.address = jObj.get("address").getAsString();
            if (jObj.has("value")) {
                c.value = jObj.get("value").getAsString();
            }
            String operator = jObj.get("operator").getAsString().replace(" ", "_");
            c.operator = Operator.valueOf(operator);
            return c;
        }
    }
}
