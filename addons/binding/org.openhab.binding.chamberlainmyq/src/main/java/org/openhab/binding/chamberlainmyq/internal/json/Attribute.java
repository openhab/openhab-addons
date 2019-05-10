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
package org.openhab.binding.chamberlainmyq.internal.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * The {@link MyqJson} is responsible for storing
 * the MyQ Attribute JSON response data.
 *
 * @author Scott Hanson - Initial contribution
 */
public class Attribute {

    @SerializedName("MyQDeviceTypeAttributeId")
    @Expose
    private Integer myQDeviceTypeAttributeId;
    @SerializedName("Value")
    @Expose
    private String value;
    @SerializedName("UpdatedTime")
    @Expose
    private String updatedTime;
    @SerializedName("IsDeviceProperty")
    @Expose
    private Boolean isDeviceProperty;
    @SerializedName("AttributeDisplayName")
    @Expose
    private String attributeDisplayName;
    @SerializedName("IsPersistent")
    @Expose
    private Boolean isPersistent;
    @SerializedName("IsTimeSeries")
    @Expose
    private Boolean isTimeSeries;
    @SerializedName("IsGlobal")
    @Expose
    private Boolean isGlobal;
    @SerializedName("UpdatedDate")
    @Expose
    private String updatedDate;

    public Integer getMyQDeviceTypeAttributeId() {
        return myQDeviceTypeAttributeId;
    }

    public String getValue() {
        return value;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public Boolean getIsDeviceProperty() {
        return isDeviceProperty;
    }

    public String getAttributeDisplayName() {
        return attributeDisplayName;
    }

    public Boolean getIsPersistent() {
        return isPersistent;
    }

    public Boolean getIsTimeSeries() {
        return isTimeSeries;
    }

    public Boolean getIsGlobal() {
        return isGlobal;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }
}