package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(TemperatureUnit.Adapter.class)
public enum TemperatureUnit {

    CELSIUS("CELSIUS"),

    FAHRENHEIT("FAHRENHEIT");

    private String value;

    TemperatureUnit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TemperatureUnit fromValue(String text) {
        for (TemperatureUnit b : TemperatureUnit.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<TemperatureUnit> {
        @Override
        public void write(final JsonWriter jsonWriter, final TemperatureUnit enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public TemperatureUnit read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return TemperatureUnit.fromValue(String.valueOf(value));
        }
    }
}
