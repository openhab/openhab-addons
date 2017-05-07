package org.openhab.binding.robonect.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class TimerModeDeserializer implements JsonDeserializer<Timer.TimerMode> {
    @Override
    public Timer.TimerMode deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        int code = jsonElement.getAsInt();
        return Timer.TimerMode.fromCode(code);
    }
} 

