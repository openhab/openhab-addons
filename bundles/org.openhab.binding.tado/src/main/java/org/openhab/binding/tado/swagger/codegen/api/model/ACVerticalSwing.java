package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(ACVerticalSwing.Adapter.class)
public enum ACVerticalSwing {

    OFF("OFF"),

    ON("ON"),

    UP("UP"),

    MID_UP("MID_UP"),

    MID("MID"),

    MID_DOWN("MID_DOWN"),

    DOWN("DOWN"),

    AUTO("AUTO");

    private String value;

    ACVerticalSwing(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static ACVerticalSwing fromValue(String text) {
        for (ACVerticalSwing b : ACVerticalSwing.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<ACVerticalSwing> {
        @Override
        public void write(final JsonWriter jsonWriter, final ACVerticalSwing enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public ACVerticalSwing read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return ACVerticalSwing.fromValue(String.valueOf(value));
        }
    }
}
