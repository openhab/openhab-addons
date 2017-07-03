/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// TODO: Auto-generated Javadoc
/**
 * The Class PowerStatus.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class PowerStatus {

    /** The status. */
    private final boolean status;

    /**
     * Instantiates a new power status.
     *
     * @param status the status
     */
    public PowerStatus(boolean status) {
        this.status = status;
    }

    /**
     * Instantiates a new power status.
     *
     * @param results the results
     */
    public PowerStatus(ScalarWebResult results) {
        final JsonArray resultArray = results.getResults();

        if (resultArray.size() != 1) {
            throw new JsonParseException("Result should only have a single element: " + resultArray);
        }

        final JsonObject obj = resultArray.get(0).getAsJsonObject();
        final String strStatus = obj.get("status").getAsString();

        this.status = StringUtils.equalsIgnoreCase("active", strStatus);
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public boolean getStatus() {
        return status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PowerStatus [status=" + status + "]";
    }
}
