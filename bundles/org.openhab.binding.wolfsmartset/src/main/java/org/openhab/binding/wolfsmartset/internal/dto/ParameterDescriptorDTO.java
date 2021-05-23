/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * generated with https://www.jsonschema2pojo.org/
 * 
 * @author Bo Biene - Initial contribution
 */
public class ParameterDescriptorDTO {
    @SerializedName("ValueId")
    @Expose
    public Long ValueId;

    @SerializedName("SortId")
    @Expose
    public Long SortId;

    @SerializedName("SubBundleId")
    @Expose
    public Long SubBundleId;

    @SerializedName("ParameterId")
    @Expose
    public Long ParameterId;

    @SerializedName("IsReadOnly")
    @Expose
    public Boolean IsReadOnly;

    @SerializedName("NoDataPoint")
    @Expose
    public Boolean NoDataPoint;

    @SerializedName("IsExpertProtectable")
    @Expose
    public Boolean IsExpertProtectable;

    @SerializedName("Name")
    @Expose
    public String Name;

    @SerializedName("Group")
    @Expose
    public String Group;

    @SerializedName("ControlType")
    @Expose
    public Integer ControlType;

    @SerializedName("Value")
    @Expose
    public String Value;

    @SerializedName("ValueState")
    @Expose
    public Long ValueState;

    @SerializedName("HasDependentParameter")
    @Expose
    public Boolean HasDependentParameter;

    @SerializedName("ProtGrp")
    @Expose
    public String ProtGrp;

    @SerializedName("Unit")
    @Expose
    public String Unit;

    @SerializedName("Decimals")
    @Expose
    public Long Decimals;

    @SerializedName("MinValueCondition")
    @Expose
    public String MinValueCondition;

    @SerializedName("MaxValueCondition")
    @Expose
    public String MaxValueCondition;

    @SerializedName("MinValue")
    @Expose
    public Long MinValue;

    @SerializedName("MaxValue")
    @Expose
    public Long MaxValue;

    @SerializedName("StepWidth")
    @Expose
    public double StepWidth;

    @SerializedName("NamePrefix")
    @Expose
    public String NamePrefix;

    @SerializedName("TurnOffValue")
    @Expose
    public Long TurnOffValue;
}
