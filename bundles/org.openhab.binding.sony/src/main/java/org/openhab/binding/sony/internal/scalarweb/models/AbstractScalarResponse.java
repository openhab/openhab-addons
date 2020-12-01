/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This abstract class provides common functionality for all scalar responses.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractScalarResponse {
    /**
     * An abstract method to allow the caller to get the payload of the response
     * 
     * @return a non-null json array
     */
    protected abstract @Nullable JsonArray getPayload();

    /**
     * Converts this generic response into the specified type
     *
     * @param <T> the generic type that will be returned
     * @param clazz the class to cast to
     * @return the object cast to class
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public <T> T as(final Class<T> clazz) throws IOException {
        Objects.requireNonNull(clazz, "clazz cannot be null");

        // First see if there is a constructor that takes a ScalarWebResult (us)
        // If so - call it with us
        // Otherwise try to use GSON to construct the class and set the fields
        try {
            final Constructor<T> constr = clazz.getConstructor(this.getClass());
            return constr.newInstance(this);
        } catch (final NoSuchMethodException e) {
            final JsonArray localResults = getPayload();
            if (localResults == null || isBlank(localResults)) {
                throw new IllegalArgumentException(
                        "Cannot convert ScalarWebResult for " + clazz + " with results: " + localResults);
            } else if (localResults.size() == 1) {
                JsonElement elm = localResults.get(0);
                if (elm.isJsonArray()) {
                    final JsonArray arry = elm.getAsJsonArray();
                    if (arry.size() == 1) {
                        elm = arry.get(0);
                    } else {
                        elm = arry;
                    }
                }
                final Gson gson = GsonUtilities.getApiGson();

                if (elm.isJsonObject()) {
                    final JsonObject jobj = elm.getAsJsonObject();
                    return gson.fromJson(jobj, clazz);
                } else {
                    if (SonyUtil.isPrimitive(clazz)) {
                        return gson.fromJson(elm, clazz);
                    } else {
                        throw new IllegalArgumentException(
                                "Cannot convert ScalarWebResult to " + clazz + " with results: " + localResults, e);
                    }
                }
            }
            throw new IllegalArgumentException(
                    "Cannot convert ScalarWebResult to " + clazz + " with results: " + localResults, e);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(
                    "Cannot convert ScalarWebResult to " + clazz + " for reason: " + e.getMessage(), e);
        }
    }

    /**
     * Converts this generic response into an array of the specified type
     *
     * @param <T> the generic type that will be returned
     * @param clazz the class to cast to
     * @return a non-null, possibly empty list of objects converted to the class
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public <T> List<T> asArray(final Class<T> clazz) throws IOException {
        Objects.requireNonNull(clazz, "clazz cannot be null");

        final JsonArray localResults = getPayload();
        if (localResults == null) { // empty array is okay here - just not null
            throw new IllegalArgumentException(
                    "Cannot convert ScalarWebResult for " + clazz + " with results: " + localResults);
        }

        final Gson gson = GsonUtilities.getDefaultGson();
        final List<T> rc = new ArrayList<T>();

        for (final JsonElement resElm : localResults) {
            if (resElm.isJsonArray()) {
                for (final JsonElement elm : resElm.getAsJsonArray()) {
                    rc.add(getObject(gson, elm, clazz));
                }
            } else {
                rc.add(getObject(gson, resElm, clazz));
            }
        }
        return rc;
    }

    /**
     * Helper method to convert an json element to an object
     * 
     * @param gson a non-null GSON instance
     * @param elm a non-null element to convert
     * @param clazz a non-null class to convert to
     * @return a non-null object
     * @throws IllegalArgumentException if class cannot be converted
     */
    private static <T> T getObject(Gson gson, JsonElement elm, Class<T> clazz) {
        Objects.requireNonNull(gson, "gson cannot be null");
        Objects.requireNonNull(elm, "elm cannot be null");
        Objects.requireNonNull(clazz, "clazz cannot be null");

        if (elm.isJsonObject()) {
            final JsonObject jobj = elm.getAsJsonObject();
            return gson.fromJson(jobj, clazz);
        } else {
            if (SonyUtil.isPrimitive(clazz)) {
                return gson.fromJson(elm, clazz);
            } else {
                throw new IllegalArgumentException(
                        "Cannot convert ScalarWebResult to " + clazz + " with results: " + elm);
            }
        }
    }

    /**
     * Utility method to check if the associated array is empty. This will include if the array consists of ONLY other
     * arrays and those arrays are empty
     *
     * @param arry the json array to check
     * @return true if empty or null, false otherwise
     */
    protected static boolean isBlank(final @Nullable JsonArray arry) {
        if (arry == null || arry.size() == 0) {
            return true;
        }
        for (final JsonElement elm : arry) {
            if (elm.isJsonArray()) {
                if (!isBlank(elm.getAsJsonArray())) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
