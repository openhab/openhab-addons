package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(OverlayTerminationConditionType.Adapter.class)
public enum OverlayTerminationConditionType {

    MANUAL("MANUAL"),

    TADO_MODE("TADO_MODE"),

    TIMER("TIMER");

    private String value;

    OverlayTerminationConditionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static OverlayTerminationConditionType fromValue(String text) {
        for (OverlayTerminationConditionType b : OverlayTerminationConditionType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<OverlayTerminationConditionType> {
        @Override
        public void write(final JsonWriter jsonWriter, final OverlayTerminationConditionType enumeration)
                throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public OverlayTerminationConditionType read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return OverlayTerminationConditionType.fromValue(String.valueOf(value));
        }
    }
}
