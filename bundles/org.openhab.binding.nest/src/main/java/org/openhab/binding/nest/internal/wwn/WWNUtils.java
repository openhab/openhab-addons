/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.wwn;

import java.io.Reader;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for sharing WWN utility methods between objects.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public final class WWNUtils {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private WWNUtils() {
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
