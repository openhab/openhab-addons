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
        this.mac = getString(DEVICE_PROPERTY_MAC);
        this.fwVer = getString(DEVICE_PROPERTY_FW);
        this.hwVer = getString(DEVICE_PROPERTY_HW);
        this.ip = getString(DEVICE_PROPERTY_IP);
        this.model = getString(DEVICE_PROPERTY_MODEL);
        this.deviceId = getString(DEVICE_PROPERTY_ID);
        this.overheated = getBool(DEVICE_PROPERTY_OVERHEAT);
        this.deviceOn = getBool(DEVICE_PROPERTY_ON);
        this.signalLevel = getInt(DEVICE_PROPERTY_SIGNAL);
        this.onTime = getInt(DEVICE_PROPERTY_ONTIME);
        this.brightness = getInt(DEVICE_PROPERTY_BRIGHTNES);
        this.hue = getInt(DEVICE_PROPERTY_HUE);
        this.saturation = getInt(DEVICE_PROPERTY_SATURATION);
        this.colorTemp = getInt(DEVICE_PROPERTY_COLORTEMP, BULB_MIN_COLORTEMP);
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

    public String getRepresentationProperty() {
        return mac.replace("-", "");
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
