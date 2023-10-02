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

import com.google.gson.annotations.SerializedName;

/**
 * @author Marco Descher - intial implementation
 */
public class DatapointValue {

    @SerializedName("Value")
    private Object value;

    @SerializedName("DatapointConfigId")
    private String datapointConfigId;

    @SerializedName("DeviceId")
    private String deviceId;

    @SerializedName("Flags")
    private int flags;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDatapointConfigId() {
        return datapointConfigId;
    }

    public void setDatapointConfigId(String datapointConfigId) {
        this.datapointConfigId = datapointConfigId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
}
