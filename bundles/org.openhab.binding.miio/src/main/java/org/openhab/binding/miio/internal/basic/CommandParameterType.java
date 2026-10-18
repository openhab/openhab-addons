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
package org.openhab.binding.miio.internal.basic;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Various types of parameters to be send
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
@JsonAdapter(value = CommandParameterType.Adapter.class, nullSafe = false)
public enum CommandParameterType {
    NONE("none"),
    EMPTY("empty"),
    ONOFF("onoff"),
    ONOFFPARA("onoffpara"),
    ONOFFBOOL("onoffbool"),
    ONOFFBOOLSTRING("onoffboolstring"),
    ONOFFNUMBER("onoffnumber"),
    STRING("string"),
    CUSTOMSTRING("customstring"),
    NUMBER("number"),
    COLOR("color"),
    UNKNOWN("unknown");

    private String text;

    CommandParameterType(String text) {
        this.text = text;
    }

    public static CommandParameterType fromString(String text) {
        for (CommandParameterType param : CommandParameterType.values()) {
            if (param.text.equalsIgnoreCase(text)) {
                return param;
            }
        }
        return UNKNOWN;
    }

    /**
     * Reads the type from a database file without regard to case; an unrecognized value becomes {@link #UNKNOWN}
     * instead of the {@code null} of the default Gson enum handling.
     */
    static class Adapter extends TypeAdapter<CommandParameterType> {
        @Override
        public @Nullable CommandParameterType read(@Nullable JsonReader in) throws IOException {
            if (in == null) {
                return UNKNOWN;
            }
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return UNKNOWN;
            }
            return fromString(in.nextString());
        }

        @Override
        public void write(@Nullable JsonWriter out, @Nullable CommandParameterType value) throws IOException {
            if (out != null) {
                out.value(value != null ? value.name() : null);
            }
        }
    }
}
