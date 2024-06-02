package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(TadoMode.Adapter.class)
public enum TadoMode {

    HOME("HOME"),

    SLEEP("SLEEP"),

    AWAY("AWAY");

    private String value;

    TadoMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TadoMode fromValue(String text) {
        for (TadoMode b : TadoMode.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<TadoMode> {
        @Override
        public void write(final JsonWriter jsonWriter, final TadoMode enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public TadoMode read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return TadoMode.fromValue(String.valueOf(value));
        }
    }
}
