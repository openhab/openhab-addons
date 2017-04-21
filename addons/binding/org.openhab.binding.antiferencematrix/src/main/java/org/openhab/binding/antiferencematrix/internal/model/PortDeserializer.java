package org.openhab.binding.antiferencematrix.internal.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Gson port deserializer used to ensure that ports are deseralised to the correct type (InputPort or OutputPort)
 * 
 * @author Neil
 *
 */
public class PortDeserializer implements JsonDeserializer<Port> {

    @Override
    public Port deserialize(final JsonElement json, final Type thpeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement jsonType = jsonObject.get("Mode");
        String type = jsonType.getAsString();

        Port port = null;

        if ("Input".equals(type)) {
            port = context.deserialize(json, InputPort.class);
        } else if ("Output".equals(type)) {
            port = context.deserialize(json, OutputPort.class);
        }
        return port;
    }

}
