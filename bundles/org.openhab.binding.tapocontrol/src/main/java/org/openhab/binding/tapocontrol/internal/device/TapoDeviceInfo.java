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
package org.openhab.binding.tapocontrol.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.JsonObject;

/**
 * Tapo-Device Information class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDeviceInfo {
    private String device_id = "";
    private String fw_ver = "";
    private String hw_ver = "";
    private String type = "";
    private String model = "";
    private String mac = "";
    private String hw_id = "";
    private String fw_id = "";
    private String oem_id = "";
    private String specs = "";
    private Boolean device_on = false;
    private Integer on_time = 0;
    private Boolean overheated = false;
    private String nickname = "";
    private String location = "";
    private String avatar = "";
    private Integer time_usage_today = 0;
    private Integer time_usage_past7 = 0;
    private Integer time_usage_past30 = 0;
    private Integer longitude = 0;
    private Integer latitude = 0;
    private Boolean has_set_location_info = false;
    private String ip = "";
    private String ssid = "";
    private Integer signal_level = 0;
    private Integer rssi = 0;
    private String region = "";
    private Integer time_diff = 0;
    private String lang = "";

    private JsonObject jsonObject = new JsonObject();

    /**
     * INIT
     */
    public TapoDeviceInfo() {
        setData();
    }

    public TapoDeviceInfo(JsonObject jso) {
        jsonObject = jso;
        setData();
    }

    private void setData() {
        this.mac = getString("mac");
        this.fw_ver = getString("fw_ver");
        this.hw_ver = getString("hw_ver");
        this.ip = getString("ip");
        this.model = getString("model");
        this.device_id = getString("device_id");
        this.overheated = getBool("overheated");
        this.device_on = getBool("device_on");
        this.signal_level = getInt("signal_level");
    }

    /***********************************
     *
     * HELPERS
     *
     ************************************/

    /**
     * 
     * @param name parameter name
     * @param defVal - default value;
     * @return string value
     */
    private String getString(String name, String defVal) {
        if (jsonObject.has(name)) {
            return jsonObject.get(name).getAsString();
        } else {
            return defVal;
        }
    }

    /**
     * 
     * @param name parameter name
     * @return string value
     */
    private String getString(String name) {
        return getString(name, "");
    }

    /**
     * 
     * @param name parameter name
     * @param defVal - default value;
     * @return boolean value
     */
    private Boolean getBool(String name, Boolean defVal) {
        if (jsonObject.has(name)) {
            return jsonObject.get(name).getAsBoolean();
        } else {
            return false;
        }
    }

    /**
     * 
     * @param name parameter name
     * @return boolean value
     */
    private Boolean getBool(String name) {
        return getBool(name, false);
    }

    /**
     * 
     * @param name parameter name
     * @param defVal - default value;
     * @return integer value
     */
    private Integer getInt(String name, Integer defVal) {
        if (jsonObject.has(name)) {
            return jsonObject.get(name).getAsInt();
        } else {
            return defVal;
        }
    }

    /**
     * 
     * @param name parameter name
     * @return integer value
     */
    private Integer getInt(String name) {
        return getInt(name, 0);
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public String getMAC() {
        return mac;
    }

    public String getFirmwareVersion() {
        return fw_ver;
    }

    public String getHardwareVersion() {
        return hw_ver;
    }

    public String getIP() {
        return ip;
    }

    public Integer getSignalLevel() {
        return signal_level;
    }

    public String getModel() {
        return model;
    }

    public String getSerial() {
        return device_id;
    }

    public Boolean isOverheated() {
        return overheated;
    }

    public Boolean isOn() {
        return device_on;
    }

    public Boolean isOff() {
        return !device_on;
    }

    public OnOffType getOnOffType() {
        if (isOn()) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }
}
