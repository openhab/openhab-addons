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
package org.openhab.binding.wlanthermo.internal.api.esp32.dto.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Device {

    @SerializedName("device")
    @Expose
    private String device;
    @SerializedName("serial")
    @Expose
    private String serial;
    @SerializedName("cpu")
    @Expose
    private String cpu;
    @SerializedName("flash_size")
    @Expose
    private Integer flashSize;
    @SerializedName("hw_version")
    @Expose
    private String hwVersion;
    @SerializedName("sw_version")
    @Expose
    private String swVersion;
    @SerializedName("api_version")
    @Expose
    private String apiVersion;
    @SerializedName("language")
    @Expose
    private String language;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public Integer getFlashSize() {
        return flashSize;
    }

    public void setFlashSize(Integer flashSize) {
        this.flashSize = flashSize;
    }

    public String getHwVersion() {
        return hwVersion;
    }

    public void setHwVersion(String hwVersion) {
        this.hwVersion = hwVersion;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
