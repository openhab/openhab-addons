package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(AcMode.Adapter.class)
public enum AcMode {

    COOL("COOL"),

    HEAT("HEAT"),

    DRY("DRY"),

    FAN("FAN"),

    AUTO("AUTO");

    private String value;

    AcMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static AcMode fromValue(String text) {
        for (AcMode b : AcMode.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<AcMode> {
        @Override
        public void write(final JsonWriter jsonWriter, final AcMode enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public AcMode read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return AcMode.fromValue(String.valueOf(value));
        }
    }
}
