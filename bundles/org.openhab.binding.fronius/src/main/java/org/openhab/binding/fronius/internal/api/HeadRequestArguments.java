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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HeadRequestArguments} is responsible for storing
 * the "RequestArguments" node from the {@link Head}
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class HeadRequestArguments {
    @SerializedName("DataCollection")
    private String dataCollection;
    @SerializedName("DeviceClass")
    private String deviceClass;
    @SerializedName("DeviceId")
    private String deviceId;
    @SerializedName("Scope")
    private String scope;

    public String getDataCollection() {
        if (null == dataCollection) {
            dataCollection = "";
        }
        return dataCollection;
    }

    public void setDataCollection(String dataCollection) {
        this.dataCollection = dataCollection;
    }

    public String getDeviceClass() {
        if (null == deviceClass) {
            deviceClass = "";
        }
        return deviceClass;
    }

    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    public String getDeviceId() {
        if (null == deviceId) {
            deviceId = "";
        }
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getScope() {
        if (null == scope) {
            scope = "";
        }
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
