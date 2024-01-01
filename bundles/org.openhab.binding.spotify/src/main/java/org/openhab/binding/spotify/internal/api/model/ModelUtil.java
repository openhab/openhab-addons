/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.spotify.internal.api.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Util class to get the Gson instance used to parse the Spotify data.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public final class ModelUtil {
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private ModelUtil() {
        // Util class
    }

    /**
     * @return Returns the Gson instance to parse the Spotify data.
     */
    public static Gson gsonInstance() {
        return GSON;
    }
}
