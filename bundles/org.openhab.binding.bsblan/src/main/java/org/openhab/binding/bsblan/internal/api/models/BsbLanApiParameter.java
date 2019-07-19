/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.bsblan.internal.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BsbLanApiParameter} is responsible for storing parameter info.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiParameter {
    @SerializedName("name")
    private String name;
    @SerializedName("value")
    private String value;
    @SerializedName("unit")
    private String unit;
    @SerializedName("desc")
    private String description;
    @SerializedName("dataType")
    private BsbLanApiParameterDataType dataType;

    public String getName() {
        return name;
    }

    public String setName(String value) {
        return name = value;
    }

    public String getValue() {
        return value;
    }

    public String setValue(String value) {
        return this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public String setUnit(String value) {
        // todo: unescape HTML
        return unit = value;
    }

    public String getDescription() {
        return description;
    }

    public String setDescription(String value) {
        return description = value;
    }

    public BsbLanApiParameterDataType getDataType() {
        return dataType;
    }

    public BsbLanApiParameterDataType setDataType(BsbLanApiParameterDataType value) {
        return dataType = value;
    }
}
