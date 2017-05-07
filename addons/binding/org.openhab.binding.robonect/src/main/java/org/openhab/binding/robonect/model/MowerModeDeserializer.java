package org.openhab.binding.robonect.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class MowerModeDeserializer implements JsonDeserializer<MowerMode> {
    @Override
    public MowerMode deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        int mode = jsonElement.getAsInt();
        return MowerMode.fromMode(mode);
    }
}
