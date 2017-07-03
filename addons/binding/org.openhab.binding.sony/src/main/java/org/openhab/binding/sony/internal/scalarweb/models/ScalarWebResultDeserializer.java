/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebResultDeserializer.
 *
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebResultDeserializer implements JsonDeserializer<ScalarWebResult> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebResultDeserializer.class);

    /*
     * (non-Javadoc)
     *
     * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type,
     * com.google.gson.JsonDeserializationContext)
     */
    @Override
    public ScalarWebResult deserialize(JsonElement je, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        if (je instanceof JsonObject) {
            final JsonObject jo = je.getAsJsonObject();

            int id = -1;

            final JsonElement idElm = jo.get("id");
            if (idElm != null && idElm.isJsonPrimitive()) {
                id = idElm.getAsInt();
            }

            return new ScalarWebResult(id, getArray(jo, "result"), getArray(jo, "error"));
        }
        logger.debug(">>. not an object?");
        return null;
    }

    /**
     * Gets the array.
     *
     * @param jo the jo
     * @param singularName the singular name
     * @return the array
     */
    private JsonArray getArray(JsonObject jo, String singularName) {
        final JsonArray ja = new JsonArray();

        logger.debug(">>> deserializing: {}", singularName);
        final JsonElement sing = jo.get(singularName);
        if (sing != null && sing.isJsonArray()) {
            // logger.debug(">>> singularfound: {}", singularName);
            ja.addAll(sing.getAsJsonArray());
        }

        final JsonElement plur = jo.get(singularName + "s");
        if (plur != null && plur.isJsonArray()) {
            // logger.debug(">>> pluralfound: {}s", singularName);
            ja.addAll(plur.getAsJsonArray());
        }

        return ja;
    }
}