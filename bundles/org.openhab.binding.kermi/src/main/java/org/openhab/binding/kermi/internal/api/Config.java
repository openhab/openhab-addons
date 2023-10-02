/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.kermi.internal.api;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marco Descher - intial implementation
 */
public class Config {

    @SerializedName("DatapointConfigId")
    private String datapointConfigId;

    @SerializedName("DisplayName")
    private String displayName;

    @SerializedName("Description")
    private String description;

    @SerializedName("WellKnownName")
    private String wellKnownName;

    @SerializedName("Unit")
    private String unit;

    @SerializedName("DatapointType")
    private int datapointType;

    @SerializedName("PossibleValues")
    private Map<String, String> possibleValues;

    public String getDatapointConfigId() {
        return datapointConfigId;
    }

    public void setDatapointConfigId(String datapointConfigId) {
        this.datapointConfigId = datapointConfigId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWellKnownName() {
        return wellKnownName;
    }

    public void setWellKnownName(String wellKnownName) {
        this.wellKnownName = wellKnownName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getDatapointType() {
        return datapointType;
    }

    public void setDatapointType(int datapointType) {
        this.datapointType = datapointType;
    }

    public Map<String, String> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(Map<String, String> possibleValues) {
        this.possibleValues = possibleValues;
    }
}
