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
package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * generated with https://www.jsonschema2pojo.org/
 * 
 * @author Bo Biene - Initial contribution
 */
@Generated("jsonschema2pojo")
public class GetParameterValuesDTO {

    @SerializedName("LastAccess")
    @Expose
    private String lastAccess;
    @SerializedName("Values")
    @Expose
    private List<ValueDTO> values = null;
    @SerializedName("IsNewJobCreated")
    @Expose
    private Boolean isNewJobCreated;

    public String getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(String lastAccess) {
        this.lastAccess = lastAccess;
    }

    public List<ValueDTO> getValues() {
        return values;
    }

    public void setValues(List<ValueDTO> values) {
        this.values = values;
    }

    public boolean getIsNewJobCreated() {
        return isNewJobCreated != null ? isNewJobCreated : false;
    }

    public void setIsNewJobCreated(Boolean isNewJobCreated) {
        this.isNewJobCreated = isNewJobCreated;
    }
}
