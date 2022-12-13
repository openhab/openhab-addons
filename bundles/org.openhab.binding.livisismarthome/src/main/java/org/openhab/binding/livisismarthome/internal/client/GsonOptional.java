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
package org.openhab.binding.livisismarthome.internal.client;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link GsonOptional} supports non-null-compatible methods to use gson.
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class GsonOptional {

    /**
     * date format as used in json in API. Example: 2016-07-11T10:55:52.3863424Z
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

    public GsonOptional() {
    }

    public <T> Optional<T> fromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        return Optional.ofNullable(fromJsonNullable(json, clazz));
    }

    public <T> @Nullable T fromJsonNullable(String json, Class<T> clazz) throws JsonSyntaxException {
        return gson.fromJson(json, clazz);
    }

    public String toJson(Object src) {
        return gson.toJson(src);
    }
}
