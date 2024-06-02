package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(ACFanLevel.Adapter.class)
public enum ACFanLevel {

    SILENT("SILENT"),

    LEVEL1("LEVEL1"),

    LEVEL2("LEVEL2"),

    LEVEL3("LEVEL3"),

    LEVEL4("LEVEL4"),

    LEVEL5("LEVEL5"),

    AUTO("AUTO");

    private String value;

    ACFanLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static ACFanLevel fromValue(String text) {
        for (ACFanLevel b : ACFanLevel.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<ACFanLevel> {
        @Override
        public void write(final JsonWriter jsonWriter, final ACFanLevel enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public ACFanLevel read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return ACFanLevel.fromValue(String.valueOf(value));
        }
    }
}
