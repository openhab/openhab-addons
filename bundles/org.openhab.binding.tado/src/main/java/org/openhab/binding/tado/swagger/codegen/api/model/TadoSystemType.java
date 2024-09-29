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
package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@JsonAdapter(TadoSystemType.Adapter.class)
public enum TadoSystemType {

    HEATING("HEATING"),

    AIR_CONDITIONING("AIR_CONDITIONING"),

    HOT_WATER("HOT_WATER");

    private String value;

    TadoSystemType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TadoSystemType fromValue(String text) {
        for (TadoSystemType b : TadoSystemType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<TadoSystemType> {
        @Override
        public void write(final JsonWriter jsonWriter, final TadoSystemType enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public TadoSystemType read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return TadoSystemType.fromValue(String.valueOf(value));
        }
    }
}
