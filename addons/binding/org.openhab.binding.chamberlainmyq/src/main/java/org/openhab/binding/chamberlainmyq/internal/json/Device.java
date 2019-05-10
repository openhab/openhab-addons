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

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * The {@link MyqJson} is responsible for storing
 * the MyQ Device JSON response data.
 *
 * @author Scott Hanson - Initial contribution
 */
public class Device {

    @SerializedName("MyQDeviceId")
    @Expose
    private Integer myQDeviceId;
    @SerializedName("ParentMyQDeviceId")
    @Expose
    private Integer parentMyQDeviceId;
    @SerializedName("MyQDeviceTypeId")
    @Expose
    private Integer myQDeviceTypeId;
    @SerializedName("MyQDeviceTypeName")
    @Expose
    private String myQDeviceTypeName;
    @SerializedName("RegistrationDateTime")
    @Expose
    private String registrationDateTime;
    @SerializedName("SerialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("UserName")
    @Expose
    private String userName;
    @SerializedName("UserCountryId")
    @Expose
    private Integer userCountryId;
    @SerializedName("Attributes")
    @Expose
    private List<Attribute> attributes = null;
    @SerializedName("ChildrenMyQDeviceIds")
    @Expose
    private String childrenMyQDeviceIds;
    @SerializedName("UpdatedBy")
    @Expose
    private String updatedBy;
    @SerializedName("UpdatedDate")
    @Expose
    private String updatedDate;
    @SerializedName("ConnectServerDeviceId")
    @Expose
    private String connectServerDeviceId;

    public Integer getMyQDeviceId() {
        return myQDeviceId;
    }

    public Integer getParentMyQDeviceId() {
        return parentMyQDeviceId;
    }

    public Integer getMyQDeviceTypeId() {
        return myQDeviceTypeId;
    }

    public String getMyQDeviceTypeName() {
        return myQDeviceTypeName;
    }

    public String getRegistrationDateTime() {
        return registrationDateTime;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getUserCountryId() {
        return userCountryId;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public String getChildrenMyQDeviceIds() {
        return childrenMyQDeviceIds;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public String getConnectServerDeviceId() {
        return connectServerDeviceId;
    }
}