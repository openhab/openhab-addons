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
package org.openhab.binding.tapocontrol.internal.helpers.utils;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * {@link JsonUtils} JsonUtils -
 * Utility Helper Functions handling json helper functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class JsonUtils {

    /**
     * Check if string is valid json
     * 
     * @param json string to check
     * @return true if is valid json
     */
    public static boolean isValidJson(String json) {
        try {
            JsonObject jsnObject = GSON.fromJson(json, JsonObject.class);
            return jsnObject != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get String from jsonObject with a json key
     * 
     * @param jsonObject jsonObject
     * @param name parameter name (json-key)
     * @param defVal default value;
     * @return string value
     */
    public static String jsonObjectToString(@Nullable JsonObject jsonObject, String name, String defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            return jsonObject.get(name).getAsString();
        } else {
            return defVal;
        }
    }

    /**
     * Get String from jsonObject
     * 
     * @param jsonObject jsonObject
     * @param name parameter name
     * @return string value
     */
    public static String jsonObjectToString(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToString(jsonObject, name, "");
    }

    /**
     * Get Boolean from jsonObject with a json key
     * 
     * @param jsonObject jsonObject
     * @param name parameter name (json-key)
     * @param defVal - default value;
     * @return boolean value
     */
    public static Boolean jsonObjectToBool(@Nullable JsonObject jsonObject, String name, Boolean defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            return jsonObject.get(name).getAsBoolean();
        } else {
            return false;
        }
    }

    /**
     * Get Boolean from jsonObject with a json key
     * 
     * @param jsonObject jsonObject
     * @param name parameter name (json-key)
     * @return boolean value
     */
    public static Boolean jsonObjectToBool(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToBool(jsonObject, name, false);
    }

    /**
     * Get Integer from jsonObject with a json key
     * 
     * @param jsonObject jsonObject
     * @param name parameter name (json-key)
     * @param defVal - default value;
     * @return integer value
     */
    public static Integer jsonObjectToInt(@Nullable JsonObject jsonObject, String name, Integer defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            return jsonObject.get(name).getAsInt();
        } else {
            return defVal;
        }
    }

    /**
     * Get Integer from jsonObject with a json key
     * 
     * @param jsonObject jsonObject
     * @param name parameter name (json-key)
     * @return integer value
     */
    public static Integer jsonObjectToInt(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToInt(jsonObject, name, 0);
    }

    /**
     * Get Number from jsonObject with a json key
     * 
     * @param jsonObject jsonObject
     * @param name parameter name
     * @param defVal - default value;
     * @return number value
     */
    public static Number jsonObjectToNumber(@Nullable JsonObject jsonObject, String name, Number defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            return jsonObject.get(name).getAsNumber();
        } else {
            return defVal;
        }
    }

    /**
     * Get Number from jsonObject with a json key
     * 
     * @param jsonObject jsonObject
     * @param name parameter name
     * @return number value
     */
    public static Number jsonObjectToNumber(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToNumber(jsonObject, name, 0);
    }

    /**
     * Return class object from json formated string
     * 
     * @param json json formatted string
     * @param clazz class string should parsed to
     */
    public static <T> T getObjectFromJson(String json, Class<T> clazz) throws JsonParseException {
        @Nullable
        T result = GSON.fromJson(json, clazz);
        if (result == null) {
            throw new JsonParseException("result is null");
        }
        return result;
    }

    /**
     * Return class object from JsonObject
     * 
     * @param jso JsonOject
     * @param clazz class string should parsed to
     */
    public static <T> T getObjectFromJson(JsonObject jso, Class<T> clazz) throws JsonParseException {
        return getObjectFromJson(jso.toString(), clazz);
    }
}
