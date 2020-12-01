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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * This class represents the request to get the method types and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class MethodTypes {

    /** The methods by method name (will be unmodifiable) */
    private final List<ScalarWebMethod> methods;

    /**
     * Instantiates a new method types
     *
     * @param results the non-null results
     */
    public MethodTypes(final ScalarWebResult results) {
        Objects.requireNonNull(results, "results cannot be null");

        final List<ScalarWebMethod> myMethods = new ArrayList<ScalarWebMethod>();
        final JsonArray rsts = results.getResults();
        if (rsts == null) {
            throw new JsonParseException("No results to deserialize");
        }

        for (final JsonElement elm : rsts) {
            if (elm.isJsonArray()) {
                final JsonArray elmArray = elm.getAsJsonArray();

                if (elmArray.size() == 4) {
                    final String methodName = elmArray.get(0).getAsString();

                    // NOTE: some devices include whitespace in the response like:
                    // "{\"stuff\": \"blah\"}" vs "{\"stuff\":\"blah\"}"
                    // we remove the whitespace to make the parms/retvals consistent
                    // over all devices
                    final List<String> parms = new ArrayList<String>();
                    final JsonElement parmElm = elmArray.get(1);
                    if (parmElm.isJsonArray()) {
                        for (final JsonElement parm : parmElm.getAsJsonArray()) {
                            parms.add(parm.getAsString().replaceAll("\\s", ""));
                        }
                    } else {
                        throw new JsonParseException("Method Parameters wasn't an array: " + elmArray);
                    }

                    final List<String> retVals = new ArrayList<String>();
                    final JsonElement valsElm = elmArray.get(2);
                    if (valsElm.isJsonArray()) {
                        for (final JsonElement retVal : valsElm.getAsJsonArray()) {
                            retVals.add(retVal.getAsString().replaceAll("\\s", ""));
                        }
                    } else {
                        throw new JsonParseException("Return Values wasn't an array: " + elmArray);
                    }

                    final String methodVersion = elmArray.get(3).getAsString();

                    myMethods.add(new ScalarWebMethod(methodName, parms, retVals, methodVersion));
                } else {
                    throw new JsonParseException("MethodTypes array didn't have 4 elements: " + elmArray);
                }

            } else {
                throw new JsonParseException("MethodTypes had a non-array element: " + elm);
            }
        }

        methods = Collections.unmodifiableList(myMethods);
    }

    /**
     * Gets the methods supported
     *
     * @return the non-null, possibly empty unmodifiable map of methods by method name
     */
    public List<ScalarWebMethod> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        return "MethodTypes [methods=" + methods + "]";
    }
}
