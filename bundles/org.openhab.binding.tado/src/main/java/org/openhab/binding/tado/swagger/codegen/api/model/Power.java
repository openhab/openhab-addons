package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(Power.Adapter.class)
public enum Power {

    ON("ON"),

    OFF("OFF");

    private String value;

    Power(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static Power fromValue(String text) {
        for (Power b : Power.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<Power> {
        @Override
        public void write(final JsonWriter jsonWriter, final Power enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public Power read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return Power.fromValue(String.valueOf(value));
        }
    }
}
