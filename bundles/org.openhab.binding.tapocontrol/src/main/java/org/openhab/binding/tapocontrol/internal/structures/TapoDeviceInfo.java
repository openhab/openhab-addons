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
package org.openhab.binding.tapocontrol.internal.structures;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

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
    private Number onTime = 0;
    private Boolean overheated = false;
    private Integer brightness = 0;
    private Integer hue = 0;
    private Integer saturation = 100;
    private Integer colorTemp = 0;
    private String nickname = "";
    private String location = "";
    private String avatar = "";
    private Number timeUsageToday = 0;
    private Number timeUsagePast7 = 0;
    private Number timeUsagePast30 = 0;
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
    private TapoLightEffect lightEffect = new TapoLightEffect();

    private JsonObject jsonObject = new JsonObject();

    /**
     * INIT
     */
    public TapoDeviceInfo() {
        setData();
    }

    /**
     * Init DeviceInfo with new Data;
     * 
     * @param jso JsonObject new Data
     */
    public TapoDeviceInfo(JsonObject jso) {
        jsonObject = jso;
        setData();
    }

    /**
     * Set Data (new JsonObject)
     * 
     * @param jso JsonObject new Data
     */
    public TapoDeviceInfo setData(JsonObject jso) {
        this.jsonObject = jso;
        setData();
        return this;
    }

    private void setData() {
        this.mac = jsonObjectToString(jsonObject, DEVICE_PROPERTY_MAC);
        this.fwVer = jsonObjectToString(jsonObject, DEVICE_PROPERTY_FW);
        this.hwVer = jsonObjectToString(jsonObject, DEVICE_PROPERTY_HW);
        this.ip = jsonObjectToString(jsonObject, DEVICE_PROPERTY_IP);
        this.model = jsonObjectToString(jsonObject, DEVICE_PROPERTY_MODEL);
        this.type = jsonObjectToString(jsonObject, DEVICE_PROPERTY_TYPE);
        this.deviceId = jsonObjectToString(jsonObject, DEVICE_PROPERTY_ID);
        this.overheated = jsonObjectToBool(jsonObject, DEVICE_PROPERTY_OVERHEAT);
        this.deviceOn = jsonObjectToBool(jsonObject, DEVICE_PROPERTY_ON);
        this.signalLevel = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_SIGNAL);
        this.onTime = jsonObjectToNumber(jsonObject, DEVICE_PROPERTY_ONTIME);
        this.brightness = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_BRIGHTNES);
        this.hue = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_HUE);
        this.saturation = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_SATURATION);
        this.colorTemp = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_COLORTEMP, BULB_MIN_COLORTEMP);
        this.region = jsonObjectToString(jsonObject, DEVICE_PROPERTY_REGION);
        this.lightEffect = lightEffect.setData(jsonObject);
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public String getMAC() {
        return formatMac(mac, MAC_DIVISION_CHAR);
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
        return getMAC();
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

    public Number getOnTime() {
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

    public TapoLightEffect getLightEffect() {
        return lightEffect;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
