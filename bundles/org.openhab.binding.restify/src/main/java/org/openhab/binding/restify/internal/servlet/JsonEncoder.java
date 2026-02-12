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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.restify.internal.servlet.Json.BooleanValue;
import org.openhab.binding.restify.internal.servlet.Json.JsonArray;
import org.openhab.binding.restify.internal.servlet.Json.JsonObject;
import org.openhab.binding.restify.internal.servlet.Json.NullValue;
import org.openhab.binding.restify.internal.servlet.Json.NumberValue;
import org.openhab.binding.restify.internal.servlet.Json.StringValue;
import org.osgi.service.component.annotations.Component;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component
public class JsonEncoder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public String encode(JsonObject json) {
        var content = new StringBuilder();
        encode(json, content);
        return content.toString();
    }

    private void encode(Json json, StringBuilder content) {
        switch (json) {
            case BooleanValue booleanValue -> encodeBooleanValue(booleanValue, content);
            case JsonArray jsonArray -> encodeJsonArray(jsonArray, content);
            case JsonObject jsonObject -> encodeJsonObject(jsonObject, content);
            case NullValue nullValue -> encodeNullValue(nullValue, content);
            case NumberValue numberValue -> encodeNumberValue(numberValue, content);
            case StringValue stringValue -> encodeStringValue(stringValue, content);
        }
    }

    private void encodeBooleanValue(BooleanValue json, StringBuilder content) {
        content.append(json.value());
    }

    private void encodeJsonArray(JsonArray json, StringBuilder content) {
        content.append('[');
        for (int i = 0; i < json.responses().size(); i++) {
            if (i > 0) {
                content.append(',');
            }
            encode(json.responses().get(i), content);
        }
        content.append(']');
    }

    private void encodeJsonObject(JsonObject json, StringBuilder content) {
        content.append('{');
        var iterator = json.response().entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry == null)
                continue;
            encodeStringValue(new StringValue(entry.getKey()), content);
            content.append(':');
            encode(entry.getValue(), content);
            if (iterator.hasNext()) {
                content.append(',');
            }
        }
        content.append('}');
    }

    private void encodeNullValue(NullValue json, StringBuilder content) {
        content.append("null");
    }

    private void encodeNumberValue(NumberValue json, StringBuilder content) {
        content.append(json.value());
    }

    private void encodeStringValue(StringValue json, StringBuilder content) {
        content.append('"');
        for (char c : json.value().toCharArray()) {
            switch (c) {
                case '"', '\\' -> content.append('\\').append(c);
                case '\b' -> content.append("\\b");
                case '\f' -> content.append("\\f");
                case '\n' -> content.append("\\n");
                case '\r' -> content.append("\\r");
                case '\t' -> content.append("\\t");
                default -> {
                    if (c < 0x20) {
                        content.append(String.format("\\u%04x", (int) c));
                    } else {
                        content.append(c);
                    }
                }
            }
        }
        content.append('"');
    }
}
