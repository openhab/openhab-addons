/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dbquery.internal.domain;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Encodes domain objects to JSON
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class DBQueryJSONEncoder {
    private final Gson gson;

    public DBQueryJSONEncoder() {
        gson = new GsonBuilder().registerTypeAdapter(QueryResult.class, new QueryResultGSONSerializer())
                .registerTypeAdapter(ResultRow.class, new ResultRowGSONSerializer())
                .registerTypeAdapter(QueryParameters.class, new QueryParametersGSONSerializer()).create();
    }

    public String encode(QueryResult queryResult) {
        return gson.toJson(queryResult);
    }

    public String encode(QueryParameters parameters) {
        return gson.toJson(parameters);
    }

    @NonNullByDefault({})
    private static class QueryResultGSONSerializer implements JsonSerializer<QueryResult> {
        @Override
        public JsonElement serialize(QueryResult src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("correct", src.isCorrect());
            if (src.getErrorMessage() != null) {
                jsonObject.addProperty("errorMessage", src.getErrorMessage());
            }
            jsonObject.add("data", context.serialize(src.getData()));
            return jsonObject;
        }
    }

    private static class ResultRowGSONSerializer implements JsonSerializer<ResultRow> {
        @Override
        public JsonElement serialize(ResultRow src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (String columnName : src.getColumnNames()) {
                jsonObject.add(columnName, convertValueToJsonPrimitive(src.getValue(columnName)));
            }
            return jsonObject;
        }
    }

    private static class QueryParametersGSONSerializer implements JsonSerializer<QueryParameters> {
        @Override
        public JsonElement serialize(QueryParameters src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, @Nullable Object> param : src.getAll().entrySet()) {
                jsonObject.add(param.getKey(), convertValueToJsonPrimitive(param.getValue()));
            }
            return jsonObject;
        }
    }

    private static JsonElement convertValueToJsonPrimitive(@Nullable Object value) {
        if (value instanceof Number number) {
            return new JsonPrimitive(number);
        } else if (value instanceof Boolean boolean1) {
            return new JsonPrimitive(boolean1);
        } else if (value instanceof Character) {
            return new JsonPrimitive((Character) value);
        } else if (value instanceof Date date) {
            return new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(date.toInstant()));
        } else if (value instanceof Instant instant) {
            return new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(instant));
        } else if (value != null) {
            return new JsonPrimitive(value.toString());
        } else {
            return JsonNull.INSTANCE;
        }
    }
}
