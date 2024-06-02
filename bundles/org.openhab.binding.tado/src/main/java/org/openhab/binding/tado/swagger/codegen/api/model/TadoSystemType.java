package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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
