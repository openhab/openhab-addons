/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        return dataCollection;
    }

    public void setDataCollection(String dataCollection) {
        this.dataCollection = dataCollection;
    }

    public String getDeviceClass() {
        return deviceClass;
    }

    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
