package org.openhab.binding.tado.swagger.codegen.api;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

// Adapted version of gson-extras RuntimeTypeAdapterFactory, but without using
// internal packages and fallback to basetype instead of error.
public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
    private final Class<?> baseType;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<String, Class<?>>();

    private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
        if (typeFieldName == null || baseType == null) {
            throw new NullPointerException();
        }
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
    }

    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
        return new RuntimeTypeAdapterFactory<T>(baseType, typeFieldName);
    }

    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
        if (type == null || label == null) {
            throw new NullPointerException();
        }

        if (labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }

        labelToSubtype.put(label, type);
        return this;
    }

    @Override
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type.getRawType() != baseType) {
            return null;
        }

        final TypeAdapter<R> baseTypeDelegate = gson.getDelegateAdapter(this, type);

        final Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();

        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new TypeAdapter<R>() {
            @Override
            public R read(JsonReader in) throws IOException {
                JsonElement jsonElement = new JsonParser().parse(in);
                JsonElement labelJsonElement = jsonElement.getAsJsonObject().get(typeFieldName);

                TypeAdapter<R> delegate = null;

                if (labelJsonElement != null) {
                    String label = labelJsonElement.getAsString();
                    delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                }

                if (delegate == null) {
                    // if label unknown. use base type delegate
                    delegate = baseTypeDelegate;
                }

                return delegate.fromJsonTree(jsonElement);
            }

            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();

                TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    // if label unknown. use base type delegate
                    delegate = baseTypeDelegate;
                }

                delegate.write(out, value);
            }
        }.nullSafe();
    }
}
