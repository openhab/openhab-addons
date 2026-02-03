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
package org.openhab.binding.viessmann.internal.dto.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

/**
 * The {@link FeatureProperties} provides properties of features
 *
 * @author Ronny Grun - Initial contribution
 */
@JsonAdapter(FeatureProperties.FeaturePropertiesAdapter.class)
public class FeatureProperties {
    public FeatureString value;
    public FeatureString status;
    public FeatureBoolean active;
    public FeaturePropertiesEnabled enabled;
    public FeatureString name;
    public FeatureInteger shift;
    public FeatureDouble slope;
    public FeatureEntriesWeekDays entries;
    public FeatureErrorEntries errorEntries;
    public FeatureBoolean overlapAllowed;
    public FeatureInteger temperature;
    public FeatureString start;
    public FeatureString begin;
    public FeatureString end;
    public FeatureInteger top;
    public FeatureInteger middle;
    public FeatureInteger bottom;
    public FeatureListDouble day;
    public FeatureListDouble week;
    public FeatureListDouble month;
    public FeatureListDouble year;
    public FeatureString unit;
    public FeatureDouble hours;
    public FeatureInteger starts;
    public FeatureInteger hoursLoadClassOne;
    public FeatureInteger hoursLoadClassTwo;
    public FeatureInteger hoursLoadClassThree;
    public FeatureInteger hoursLoadClassFour;
    public FeatureInteger hoursLoadClassFive;
    public FeatureInteger min;
    public FeatureInteger max;
    public FeatureString phase;
    public FeatureString switchOnValue;
    public FeatureString switchOffValue;
    public FeatureDouble currentDay;
    public FeatureDouble lastSevenDays;
    public FeatureDouble currentMonth;
    public FeatureDouble lastMonth;
    public FeatureDouble currentYear;
    public FeatureDouble lastYear;
    public FeatureInteger target;
    public FeatureInteger current;
    public FeatureListString weekdays;
    public FeatureInteger startHour;
    public FeatureInteger startMinute;

    /**
     * Holds unknown/variable properties from the API.
     *
     * IMPORTANT:
     * - @JsonIgnore prevents it from being serialized as "additionalProperties"
     * - @JsonAnyGetter below flattens it into the "properties" object
     */
    @JsonIgnore
    public Map<String, JsonElement> additionalProperties = new HashMap<>();

    /**
     * Flatten additionalProperties into the JSON object of "properties".
     *
     * Example output:
     * "properties": { "horizontal": {...}, "vertical": {...} }
     */
    @JsonAnyGetter
    public Map<String, JsonElement> any() {
        return additionalProperties;
    }

    @JsonIgnore
    public ArrayList<String> getUsedEntries() {
        ArrayList<String> list = new ArrayList<>();

        if (value != null) {
            list.add("value");
        }
        if (status != null) {
            list.add("status");
        }
        if (active != null) {
            list.add("active");
        }
        if (enabled != null) {
            list.add("enabled");
        }
        if (name != null) {
            list.add("name");
        }
        if (shift != null) {
            list.add("shift");
        }
        if (slope != null) {
            list.add("slope");
        }
        if (entries != null) {
            list.add("entries");
        }
        if (overlapAllowed != null) {
            list.add("overlapAllowed");
        }
        if (temperature != null) {
            list.add("temperature");
        }
        if (start != null) {
            list.add("start");
        }
        if (begin != null) {
            list.add("begin");
        }
        if (end != null) {
            list.add("end");
        }
        if (top != null) {
            list.add("top");
        }
        if (middle != null) {
            list.add("middle");
        }
        if (bottom != null) {
            list.add("bottom");
        }
        if (day != null) {
            list.add("day");
        }
        if (week != null) {
            list.add("week");
        }
        if (month != null) {
            list.add("month");
        }
        if (year != null) {
            list.add("year");
        }
        if (unit != null) {
            list.add("unit");
        }
        if (hours != null) {
            list.add("hours");
        }
        if (starts != null) {
            list.add("starts");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassOne");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassTwo");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassThree");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassFour");
        }
        if (hoursLoadClassOne != null) {
            list.add("hoursLoadClassFive");
        }
        if (min != null) {
            list.add("min");
        }
        if (max != null) {
            list.add("max");
        }
        if (phase != null) {
            list.add("phase");
        }
        if (switchOnValue != null) {
            list.add("switchOnValue");
        }
        if (switchOffValue != null) {
            list.add("switchOffValue");
        }
        if (currentDay != null) {
            list.add("currentDay");
        }
        if (lastSevenDays != null) {
            list.add("lastSevenDays");
        }
        if (currentMonth != null) {
            list.add("currentMonth");
        }
        if (lastMonth != null) {
            list.add("lastMonth");
        }
        if (currentYear != null) {
            list.add("currentYear");
        }
        if (lastYear != null) {
            list.add("lastYear");
        }
        if (target != null) {
            list.add("target");
        }
        if (current != null) {
            list.add("current");
        }
        if (weekdays != null) {
            list.add("weekdays");
        }
        if (startHour != null) {
            list.add("startHour");
        }
        if (startMinute != null) {
            list.add("startMinute");
        }

        list.addAll(additionalProperties.keySet());

        return list;
    }

    public static class FeaturePropertiesAdapter implements JsonDeserializer<FeatureProperties> {
        @Override
        public FeatureProperties deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            FeatureProperties props = new FeatureProperties();
            if (json == null || !json.isJsonObject()) {
                return props;
            }
            JsonObject obj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                switch (key) {
                    case "value":
                        props.value = context.deserialize(value, FeatureString.class);
                        break;
                    case "status":
                        props.status = context.deserialize(value, FeatureString.class);
                        break;
                    case "active":
                        props.active = context.deserialize(value, FeatureBoolean.class);
                        break;
                    case "enabled":
                        props.enabled = context.deserialize(value, FeaturePropertiesEnabled.class);
                        break;
                    case "name":
                        props.name = context.deserialize(value, FeatureString.class);
                        break;
                    case "shift":
                        props.shift = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "slope":
                        props.slope = context.deserialize(value, FeatureDouble.class);
                        break;
                    case "entries":
                        props.entries = context.deserialize(value, FeatureEntriesWeekDays.class);
                        break;
                    case "errorEntries":
                        props.errorEntries = context.deserialize(value, FeatureErrorEntries.class);
                        break;
                    case "overlapAllowed":
                        props.overlapAllowed = context.deserialize(value, FeatureBoolean.class);
                        break;
                    case "temperature":
                        props.temperature = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "start":
                        props.start = context.deserialize(value, FeatureString.class);
                        break;
                    case "begin":
                        props.begin = context.deserialize(value, FeatureString.class);
                        break;
                    case "end":
                        props.end = context.deserialize(value, FeatureString.class);
                        break;
                    case "top":
                        props.top = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "middle":
                        props.middle = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "bottom":
                        props.bottom = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "day":
                        props.day = context.deserialize(value, FeatureListDouble.class);
                        break;
                    case "week":
                        props.week = context.deserialize(value, FeatureListDouble.class);
                        break;
                    case "month":
                        props.month = context.deserialize(value, FeatureListDouble.class);
                        break;
                    case "year":
                        props.year = context.deserialize(value, FeatureListDouble.class);
                        break;
                    case "unit":
                        props.unit = context.deserialize(value, FeatureString.class);
                        break;
                    case "hours":
                        props.hours = context.deserialize(value, FeatureDouble.class);
                        break;
                    case "starts":
                        props.starts = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "hoursLoadClassOne":
                        props.hoursLoadClassOne = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "hoursLoadClassTwo":
                        props.hoursLoadClassTwo = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "hoursLoadClassThree":
                        props.hoursLoadClassThree = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "hoursLoadClassFour":
                        props.hoursLoadClassFour = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "hoursLoadClassFive":
                        props.hoursLoadClassFive = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "min":
                        props.min = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "max":
                        props.max = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "phase":
                        props.phase = context.deserialize(value, FeatureString.class);
                        break;
                    case "switchOnValue":
                        props.switchOnValue = context.deserialize(value, FeatureString.class);
                        break;
                    case "switchOffValue":
                        props.switchOffValue = context.deserialize(value, FeatureString.class);
                        break;
                    case "currentDay":
                        props.currentDay = context.deserialize(value, FeatureDouble.class);
                        break;
                    case "lastSevenDays":
                        props.lastSevenDays = context.deserialize(value, FeatureDouble.class);
                        break;
                    case "currentMonth":
                        props.currentMonth = context.deserialize(value, FeatureDouble.class);
                        break;
                    case "lastMonth":
                        props.lastMonth = context.deserialize(value, FeatureDouble.class);
                        break;
                    case "currentYear":
                        props.currentYear = context.deserialize(value, FeatureDouble.class);
                        break;
                    case "lastYear":
                        props.lastYear = context.deserialize(value, FeatureDouble.class);
                        break;
                    case "target":
                        props.target = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "current":
                        props.current = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "weekdays":
                        props.weekdays = context.deserialize(value, FeatureListString.class);
                        break;
                    case "startHour":
                        props.startHour = context.deserialize(value, FeatureInteger.class);
                        break;
                    case "startMinute":
                        props.startMinute = context.deserialize(value, FeatureInteger.class);
                        break;
                    default:
                        props.additionalProperties.put(key, value);
                        break;
                }
            }
            return props;
        }
    }
}
