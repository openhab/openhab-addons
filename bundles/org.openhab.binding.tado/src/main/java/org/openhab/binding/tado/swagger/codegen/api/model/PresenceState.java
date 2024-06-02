package org.openhab.binding.tado.swagger.codegen.api.model;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(PresenceState.Adapter.class)
public enum PresenceState {

    HOME("HOME"),

    AWAY("AWAY");

    private String value;

    PresenceState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static PresenceState fromValue(String text) {
        for (PresenceState b : PresenceState.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static class Adapter extends TypeAdapter<PresenceState> {
        @Override
        public void write(final JsonWriter jsonWriter, final PresenceState enumeration) throws IOException {
            jsonWriter.value(enumeration.getValue());
        }

        @Override
        public PresenceState read(final JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            return PresenceState.fromValue(String.valueOf(value));
        }
    }
}
