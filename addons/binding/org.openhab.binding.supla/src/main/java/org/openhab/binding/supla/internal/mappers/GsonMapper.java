package org.openhab.binding.supla.internal.mappers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public final class GsonMapper implements JsonMapper {
    private final Gson gson;

    private static Gson buildGson() {
        return new GsonBuilder().create();
    }

    GsonMapper(Gson gson) {
        this.gson = gson;
    }

    public GsonMapper() {
        this(buildGson());
    }

    @Override
    public String map(Object o) {
        return gson.toJson(o);
    }

    @Override
    public <T> T to(Class<T> clazz, String string) {
        return gson.fromJson(string, clazz);
    }

    @Override
    public <T> T to(Type type, String string) {
        return gson.fromJson(string, type);
    }
}
