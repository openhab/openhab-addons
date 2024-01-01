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
package org.openhab.binding.meteoblue.internal.json;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JsonData} is the Java class used to model the JSON
 * response to a weather request.
 *
 * @author Chris Carman - Initial contribution
 */
public class JsonData {

    private JsonMetadata metadata;
    private JsonUnits units;

    @SerializedName("data_day")
    private JsonDataDay dataDay;

    @SerializedName("error_message")
    private String errorMessage;

    public JsonData() {
    }

    /**
     * Get the {@link JsonMetadata} object
     *
     * @return the JsonMetadata object
     */
    public JsonMetadata getMetadata() {
        return metadata;
    }

    /**
     * Get the {@link JsonUnits} object
     *
     * @return the JsonUnits object
     */
    public JsonUnits getUnits() {
        return units;
    }

    /**
     * Get the {@link JsonDataDay} object
     *
     * @return the JsonDataDay object
     */
    public JsonDataDay getDataDay() {
        return dataDay;
    }

    // get the error message
    public String getErrorMessage() {
        return errorMessage;
    }
}
