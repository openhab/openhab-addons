/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarUtilities.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarUtilities {

    /**
     * Creates the not implemented result.
     *
     * @param methodName the method name
     * @return the scalar web result
     */
    public static ScalarWebResult createNotImplementedResult(String methodName) {
        return createErrorResult(HttpStatus.SC_NOT_IMPLEMENTED, methodName + " was not implemented");
    }

    /**
     * Creates the error result.
     *
     * @param httpCode the http code
     * @param reason the reason
     * @return the scalar web result
     */
    public static ScalarWebResult createErrorResult(int httpCode, String reason) {
        final JsonArray ja = new JsonArray();
        ja.add(new JsonPrimitive(httpCode));
        ja.add(new JsonPrimitive(reason));
        return new ScalarWebResult(-1, null, ja);
    }

    /**
     * Gets the fields.
     *
     * @param obj the obj
     * @return the fields
     */
    public static JsonArray getFields(JsonObject obj) {
        final JsonArray arry = new JsonArray();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            arry.add(new JsonPrimitive(entry.getKey()));
        }

        return arry;
    }
}
