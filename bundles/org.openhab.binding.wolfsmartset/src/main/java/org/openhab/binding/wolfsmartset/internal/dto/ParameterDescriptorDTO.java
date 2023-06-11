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
    public Long valueId;

    @SerializedName("SortId")
    @Expose
    public Long sortId;

    @SerializedName("SubBundleId")
    @Expose
    public Long subBundleId;

    @SerializedName("ParameterId")
    @Expose
    public Long parameterId;

    @SerializedName("IsReadOnly")
    @Expose
    public Boolean isReadOnly;

    @SerializedName("NoDataPoint")
    @Expose
    public Boolean noDataPoint;

    @SerializedName("IsExpertProtectable")
    @Expose
    public Boolean isExpertProtectable;

    @SerializedName("Name")
    @Expose
    public String name;

    @SerializedName("Group")
    @Expose
    public String group;

    @SerializedName("ControlType")
    @Expose
    public Integer controlType;

    @SerializedName("Value")
    @Expose
    public String value;

    @SerializedName("ValueState")
    @Expose
    public Long valueState;

    @SerializedName("HasDependentParameter")
    @Expose
    public Boolean hasDependentParameter;

    @SerializedName("ProtGrp")
    @Expose
    public String protGrp;

    @SerializedName("Unit")
    @Expose
    public String unit;

    @SerializedName("Decimals")
    @Expose
    public Long decimals;

    @SerializedName("MinValueCondition")
    @Expose
    public String minValueCondition;

    @SerializedName("MaxValueCondition")
    @Expose
    public String maxValueCondition;

    @SerializedName("MinValue")
    @Expose
    public Long minValue;

    @SerializedName("MaxValue")
    @Expose
    public Long maxValue;

    @SerializedName("StepWidth")
    @Expose
    public double stepWidth;

    @SerializedName("NamePrefix")
    @Expose
    public String namePrefix;

    @SerializedName("TurnOffValue")
    @Expose
    public Long turnOffValue;
}
