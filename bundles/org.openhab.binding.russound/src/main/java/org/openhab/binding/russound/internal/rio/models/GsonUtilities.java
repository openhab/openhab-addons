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
package org.openhab.binding.russound.internal.rio.models;

import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Utility class for common GSON related items
 *
 * @author Tim Roberts - Initial contribution
 */
public class GsonUtilities {
    /**
     * Utility method to create a standard {@link Gson} for the system. The standard GSon will register the
     * {@link AtomicStringTypeAdapter} and the various serializers (Presets, Banks, Favorites)
     *
     * @return a non-null {@link Gson}
     */
    public static Gson createGson() {
        final GsonBuilder gs = new GsonBuilder();
        gs.registerTypeAdapter(new TypeToken<AtomicReference<String>>() {
        }.getType(), new AtomicStringTypeAdapter());
        gs.registerTypeAdapter(RioPreset.class, new RioPresetSerializer());
        gs.registerTypeAdapter(RioBank.class, new RioBankSerializer());
        gs.registerTypeAdapter(RioFavorite.class, new RioFavoriteSerializer());
        return gs.create();
    }
}
