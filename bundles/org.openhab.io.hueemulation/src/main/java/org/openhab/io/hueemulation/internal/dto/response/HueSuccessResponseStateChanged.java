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
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This response object is a bit complicated. The response is required to look like this:
 *
 * <pre>
 * {
 *   "success":{
 *   "/1d49eeed-1fa7-434a-8e6c-70bb2cdc3e8f/lights/1/state/on": true
 *   }
 * }
 * </pre>
 *
 * This object describes the right hand side of "success". The json key itself is the uri path
 * and the value is either a boolean, number or string.
 *
 * This is done with a custom serializer that creates the proper {@link JsonObject}.
 *
 * @author David Graeff - Initial contribution
 */
public class HueSuccessResponseStateChanged extends HueSuccessResponse {
    private transient Object value;
    private transient String relURI;

    public HueSuccessResponseStateChanged(String relURI, Object value) {
        this.relURI = relURI;
        this.value = value;
    }

    public static class Serializer implements JsonSerializer<HueSuccessResponseStateChanged> {
        @Override
        public JsonElement serialize(HueSuccessResponseStateChanged product, Type type, JsonSerializationContext jsc) {
            JsonObject jObj = new JsonObject();
            if (product.value instanceof Float) {
                jObj.addProperty(product.relURI, (Float) product.value);
            }
            if (product.value instanceof Double) {
                jObj.addProperty(product.relURI, (Double) product.value);
            }
            if (product.value instanceof Integer) {
                jObj.addProperty(product.relURI, (Integer) product.value);
            }
            if (product.value instanceof Boolean) {
                jObj.addProperty(product.relURI, (Boolean) product.value);
            }
            if (product.value instanceof String) {
                jObj.addProperty(product.relURI, (String) product.value);
            }
            if (product.value instanceof List) {
                jObj.add(product.relURI, new Gson().toJsonTree(product.value));
            }
            return jObj;
        }
    }
}
