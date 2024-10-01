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
@JsonAdapter(ACHorizontalSwing.Adapter.class)
public enum ACHorizontalSwing {

    OFF("OFF"),

    ON("ON"),

    LEFT("LEFT"),

    MID_LEFT("MID_LEFT"),

    MID("MID"),

    MID_RIGHT("MID_RIGHT"),

    RIGHT("RIGHT"),

    AUTO("AUTO");

    private String value;

    ACHorizontalSwing(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static ACHorizontalSwing fromValue(String text) {
        for (ACHorizontalSwing b : ACHorizontalSwing.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<ACHorizontalSwing> {
        @Override
        public void write(final JsonWriter jsonWriter, final ACHorizontalSwing enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public ACHorizontalSwing read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return ACHorizontalSwing.fromValue(String.valueOf(value));
        }
    }
}
