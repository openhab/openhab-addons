/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// TODO: Auto-generated Javadoc
/**
 * The Class FieldExistence.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class FieldExistence {

    /** The all. */
    private final boolean all;

    /** The fields. */
    private final Set<String> fields = new HashSet<String>();

    /**
     * Instantiates a new field existence.
     *
     * @param all the all
     */
    public FieldExistence(boolean all) {
        this.all = all;
    }

    /**
     * Instantiates a new field existence.
     *
     * @param results the results
     */
    public FieldExistence(ScalarWebResult results) {
        this(false);

        final JsonArray resultArray = results.getResults();

        if (resultArray.size() != 1) {
            throw new JsonParseException("Result should only have a single element: " + resultArray);
        }

        final JsonElement elm = resultArray.get(0);

        if (elm.isJsonObject()) {
            JsonObject object = elm.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                fields.add(entry.getKey());
            }
        }
    }

    /**
     * Checks for.
     *
     * @param fieldName the field name
     * @return true, if successful
     */
    public boolean has(String fieldName) {
        return all || fields.contains(fieldName);
    }
}