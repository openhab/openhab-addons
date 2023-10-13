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
package org.openhab.binding.kermi.internal.model;

import java.util.List;

import org.openhab.binding.kermi.internal.api.Datapoint;

import com.google.gson.annotations.SerializedName;

/**
 * @author Marco Descher - intial implementation
 */
public class ListDatapointCacheFile {

    @SerializedName("DeviceId")
    private String deviceId;

    @SerializedName("Serial")
    private String serial;

    @SerializedName("Address")
    private String address;

    @SerializedName("Name")
    private String name;

    @SerializedName("Datapoints")
    private List<Datapoint> datapoints;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Datapoint> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<Datapoint> datapoints) {
        this.datapoints = datapoints;
    }
}
