/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

// TODO: Auto-generated Javadoc
/**
 * The Class GetServiceProtocols.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class GetServiceProtocols {

    /** The service names. */
    private final Set<String> serviceNames = new HashSet<String>();

    /**
     * Instantiates a new gets the service protocols.
     *
     * @param results the results
     */
    public GetServiceProtocols(ScalarWebResult results) {
        final JsonArray resultArray = results.getResults();

        for (JsonElement result : resultArray) {
            if (result.isJsonArray()) {
                final JsonArray ja = result.getAsJsonArray();
                if (ja.size() > 0) {
                    serviceNames.add(ja.get(0).getAsString());
                }
            }
        }
    }

    /**
     * Gets the service names.
     *
     * @return the service names
     */
    public Set<String> getServiceNames() {
        return new HashSet<String>(serviceNames);
    }
}
