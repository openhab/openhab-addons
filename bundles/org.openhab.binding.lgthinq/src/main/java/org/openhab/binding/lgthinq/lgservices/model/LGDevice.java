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
package org.openhab.binding.lgthinq.lgservices.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link LGDevice}
 *
 * @author Nemer Daud - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NonNullByDefault
public class LGDevice {
    private String modelName = "";
    @JsonProperty("deviceType")
    private int deviceTypeId;
    private String deviceCode = "";
    private String alias = "";
    private String deviceId = "";
    private String platformType = "";
    private String modelJsonUri = "";
    private boolean online;

    public String getModelName() {
        return modelName;
    }

    @JsonIgnore
    public DeviceTypes getDeviceType() {
        return DeviceTypes.fromDeviceTypeId(deviceTypeId, deviceCode);
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getModelJsonUri() {
        return modelJsonUri;
    }

    public void setModelJsonUri(String modelJsonUri) {
        this.modelJsonUri = modelJsonUri;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
