/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@code ModelUtil} utility class to get the {@code Gson} instance to parse the Smarther API data with.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public final class ModelUtil {

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private ModelUtil() {
        // Util class
    }

    /**
     * Returns the {@code Gson} instance to parse the Smarther API data with.
     *
     * @return the {@code Gson} instance
     */
    public static Gson gsonInstance() {
        return GSON;
    }
}
