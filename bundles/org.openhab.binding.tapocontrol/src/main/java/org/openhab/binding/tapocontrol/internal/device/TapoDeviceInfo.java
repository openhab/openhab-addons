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

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;

import com.google.gson.JsonObject;

/**
 * Tapo-Device Information class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDeviceInfo {
    private String deviceId = "";
    private String fwVer = "";
    private String hwVer = "";
    private String type = "";
    private String model = "";
    private String mac = "";
    private String hwId = "";
    private String fwId = "";
    private String oemId = "";
    private String specs = "";
    private Boolean deviceOn = false;
    private Integer onTime = 0;
    private Boolean overheated = false;
    private Integer brightness = 0;
    private Integer hue = 0;
    private Integer saturation = 100;
    private Integer colorTemp = 0;
    private String nickname = "";
    private String location = "";
    private String avatar = "";
    private Integer timeUsageToday = 0;
    private Integer timeUsagePast7 = 0;
    private Integer timeUsagePast30 = 0;
    private Integer longitude = 0;
    private Integer latitude = 0;
    private Boolean hasSetLocationInfo = false;
    private String ip = "";
    private String ssid = "";
    private Integer signalLevel = 0;
    private Integer rssi = 0;
    private String region = "";
    private Integer timeDiff = 0;
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
        this.fwVer = getString("fw_ver");
        this.hwVer = getString("hw_ver");
        this.ip = getString("ip");
        this.model = getString("model");
        this.deviceId = getString("device_id");
        this.overheated = getBool("overheated");
        this.deviceOn = getBool("device_on");
        this.signalLevel = getInt("signal_level");
        this.onTime = getInt("on_time");
        this.brightness = getInt("brightness");
        this.hue = getInt("hue");
        this.saturation = getInt("saturation");
        this.colorTemp = getInt("color_temp", BULB_MIN_COLORTEMP);
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
        return fwVer;
    }

    public String getHardwareVersion() {
        return hwVer;
    }

    public String getIP() {
        return ip;
    }

    public Integer getSignalLevel() {
        return signalLevel;
    }

    public String getModel() {
        return model.replace(" ", "_");
    }

    public String getSerial() {
        return deviceId;
    }

    public Boolean isOverheated() {
        return overheated;
    }

    public Boolean isOn() {
        return deviceOn;
    }

    public Boolean isOff() {
        return !deviceOn;
    }

    public Integer getOnTime() {
        return onTime;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public Integer getHue() {
        return hue;
    }

    public Integer getSaturation() {
        return saturation;
    }

    public Integer getColorTemp() {
        return colorTemp;
    }

    public HSBType getHSB() {
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        PercentType b = new PercentType(brightness);
        return new HSBType(h, s, b);
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
