/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.mappers;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public final class GsonMapper implements JsonMapper {
    private final Gson gson;

    private static Gson buildGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
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
