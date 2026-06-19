package org.openhab.binding.tidal.internal.api.model;

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class FlatteningTypeAdapterFactory implements TypeAdapterFactory {

    private final String[] keysToFlatten = { "attributes", "links" };

    public FlatteningTypeAdapterFactory() {
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement element = JsonParser.parseReader(in);

                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();

                    for (String key : keysToFlatten) {
                        if (obj.has(key) && obj.get(key).isJsonObject()) {
                            JsonObject nested = obj.getAsJsonObject(key);
                            for (Map.Entry<String, JsonElement> entry : nested.entrySet()) {
                                if (!obj.has(entry.getKey())) {
                                    obj.add(entry.getKey(), entry.getValue());
                                }
                            }
                            obj.remove(key);
                        }
                    }
                }

                // désérialisation standard avec le delegate
                T res = delegate.fromJsonTree(element);
                return res;
            }
        };
    }
}