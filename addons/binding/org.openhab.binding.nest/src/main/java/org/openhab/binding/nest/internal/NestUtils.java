/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal;

import java.io.Reader;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for sharing utility methods between objects.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public final class NestUtils {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private NestUtils() {
        // hidden utility class constructor
    }

    public static <T> T fromJson(String json, Class<T> dataClass) {
        return GSON.fromJson(json, dataClass);
    }

    public static <T> T fromJson(Reader reader, Class<T> dataClass) {
        return GSON.fromJson(reader, dataClass);
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

}
