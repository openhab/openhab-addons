/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

// TODO: Auto-generated Javadoc
/**
 * The Class MethodTypes.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class MethodTypes {

    /** The methods. */
    private final Map<String, ScalarWebMethod> methods;

    /**
     * Instantiates a new method types.
     *
     * @param results the results
     */
    public MethodTypes(ScalarWebResult results) {
        Map<String, ScalarWebMethod> myMethods = new HashMap<String, ScalarWebMethod>();
        for (JsonElement elm : results.getResults()) {
            if (elm.isJsonArray()) {
                final JsonArray elmArray = elm.getAsJsonArray();

                if (elmArray.size() == 4) {
                    final String methodName = elmArray.get(0).getAsString();

                    // ignore versions and methodtypes - common to all and are used to get here!
                    if (methodName.equalsIgnoreCase(ScalarWebMethod.GetVersions)
                            || methodName.equalsIgnoreCase(ScalarWebMethod.GetMethodTypes)) {
                        continue;
                    }

                    final List<String> parms = new ArrayList<String>();
                    final JsonElement parmElm = elmArray.get(1);
                    if (parmElm.isJsonArray()) {
                        for (JsonElement parm : parmElm.getAsJsonArray()) {
                            parms.add(parm.getAsString());
                        }
                    } else {
                        throw new JsonParseException("Method Parameters wasn't an array: " + elmArray);
                    }

                    final List<String> retVals = new ArrayList<String>();
                    final JsonElement valsElm = elmArray.get(2);
                    if (valsElm.isJsonArray()) {
                        for (JsonElement retVal : valsElm.getAsJsonArray()) {
                            retVals.add(retVal.getAsString());
                        }
                    } else {
                        throw new JsonParseException("Return Values wasn't an array: " + elmArray);
                    }

                    final String methodVersion = elmArray.get(3).getAsString();

                    if (myMethods.containsKey(methodName)) {
                        throw new JsonParseException("Duplicate method name found in the results: " + methodName);
                    } else {
                        myMethods.put(methodName, new ScalarWebMethod(methodName, parms, retVals, methodVersion));
                    }
                } else {
                    throw new JsonParseException("MethodTypes array didn't have 4 elements: " + elmArray);
                }

            } else {
                throw new JsonParseException("MethodTypes had a non-array element: " + elm);
            }

        }

        methods = Collections.unmodifiableMap(myMethods);
    }

    /**
     * Gets the methods.
     *
     * @return the methods
     */
    public Map<String, ScalarWebMethod> getMethods() {
        return methods;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MethodTypes [methods=" + methods + "]";
    }
}
