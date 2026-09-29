/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.homeconnectdirect.internal.common.json.adapter;

import java.lang.reflect.Type;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.DoubleKeyMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson adapter for {@link DoubleKeyMap} serialization and deserialization.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class DoubleKeyMapAdapter implements JsonSerializer<DoubleKeyMap<?, ?, ?>> {

    @Override
    @SuppressWarnings("unchecked")
    public JsonElement serialize(@Nullable DoubleKeyMap<?, ?, ?> src, @Nullable Type typeOfSrc,
            @Nullable JsonSerializationContext context) {
        if (src == null || context == null) {
            return new JsonArray();
        }

        JsonArray jsonArray = new JsonArray();
        src.entrySet().stream().sorted((e1, e2) -> {
            Object k1 = e1.getKey();
            Object k2 = e2.getKey();
            if (k1 instanceof Comparable && k2 instanceof Comparable) {
                return ((Comparable<Object>) k1).compareTo(k2);
            }
            return 0;
        }).map(Map.Entry::getValue).forEach(value -> jsonArray.add(context.serialize(value)));

        return jsonArray;
    }
}
