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
package org.openhab.io.hueemulation.internal.dto.response;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This object describes the right hand side of "success".
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueSuccessGeneric extends HueSuccessResponse {
    public String message;
    public transient String key;

    public HueSuccessGeneric(@Nullable Object message, String key) {
        this.message = message != null ? String.valueOf(message) : "";
        this.key = key;
    }

    public static class Serializer implements JsonSerializer<HueSuccessGeneric> {
        @NonNullByDefault({})
        @Override
        public JsonElement serialize(HueSuccessGeneric product, Type type, JsonSerializationContext jsc) {
            JsonObject jObj = new JsonObject();
            jObj.addProperty(product.key, product.message);
            return jObj;
        }
    }

    public boolean isValid() {
        return !message.isEmpty();
    }
}
