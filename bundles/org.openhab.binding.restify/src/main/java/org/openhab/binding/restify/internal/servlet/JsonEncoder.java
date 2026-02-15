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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.servlet.Json.BooleanValue;
import org.openhab.binding.restify.internal.servlet.Json.JsonArray;
import org.openhab.binding.restify.internal.servlet.Json.JsonObject;
import org.openhab.binding.restify.internal.servlet.Json.NullValue;
import org.openhab.binding.restify.internal.servlet.Json.NumberValue;
import org.openhab.binding.restify.internal.servlet.Json.StringValue;
import org.osgi.service.component.annotations.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = JsonEncoder.class)
public class JsonEncoder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final JsonMapper mapper = new JsonMapper();

    public String encode(JsonObject json) {
        try {
            return mapper.writeValueAsString(toJsonCompatibleObject(json));
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot encode response to JSON", e);
        }
    }

    private @Nullable Object toJsonCompatibleObject(Json json) {
        switch (json) {
            case BooleanValue booleanValue -> {
                return booleanValue.value();
            }
            case JsonArray jsonArray -> {
                return toJsonCompatibleArray(jsonArray);
            }
            case JsonObject jsonObject -> {
                return toJsonCompatibleMap(jsonObject);
            }
            case NullValue ignored -> {
                return null;
            }
            case NumberValue numberValue -> {
                return numberValue.value();
            }
            case StringValue stringValue -> {
                return stringValue.value();
            }
        }
    }

    private List<@Nullable Object> toJsonCompatibleArray(JsonArray jsonArray) {
        var values = new ArrayList<@Nullable Object>(jsonArray.responses().size());
        for (var entry : jsonArray.responses()) {
            values.add(toJsonCompatibleObject(entry));
        }
        return values;
    }

    private Map<String, @Nullable Object> toJsonCompatibleMap(JsonObject jsonObject) {
        var values = new TreeMap<String, @Nullable Object>();
        for (var entry : jsonObject.response().entrySet()) {
            values.put(entry.getKey(), toJsonCompatibleObject(entry.getValue()));
        }
        return values;
    }
}
