/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
    /**
     * AVAILABLE BUT UNUSED FIELDS
     * remove before push to real version
     * 
     * private Boolean hasSetLocationInfo = false;
     * private Integer latitude = 0;
     * private Integer longitude = 0;
     * private Integer timeDiff = 0;
     * private String avatar = "";
     * private String fwId = "";
     * private String hwId = "";
     * private String specs = "";
     * private String ssid = "";
     * private String oemId = "";
     * private String lang = "";
     * private String location = "";
     */

    private Boolean deviceOn = false;
    private Boolean overheated = false;
    private Integer brightness = 0;
    private Integer colorTemp = 0;
    private Integer hue = 0;
    private Integer rssi = 0;
    private Integer saturation = 100;
    private Integer signalLevel = 0;
    private Number onTime = 0;
    private Number timeUsagePast30 = 0;
    private Number timeUsagePast7 = 0;
    private Number timeUsageToday = 0;
    private String deviceId = "";
    private String fwVer = "";
    private String hwVer = "";
    private String ip = "";
    private String mac = "";
    private String model = "";
    private String nickname = "";
    private String region = "";
    private String type = "";
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
        this.brightness = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_BRIGHTNES);
        this.colorTemp = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_COLORTEMP, BULB_MIN_COLORTEMP);
        this.deviceId = jsonObjectToString(jsonObject, DEVICE_PROPERTY_ID);
        this.deviceOn = jsonObjectToBool(jsonObject, DEVICE_PROPERTY_ON);
        this.fwVer = jsonObjectToString(jsonObject, DEVICE_PROPERTY_FW);
        this.hue = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_HUE);
        this.hwVer = jsonObjectToString(jsonObject, DEVICE_PROPERTY_HW);
        this.ip = jsonObjectToString(jsonObject, DEVICE_PROPERTY_IP);
        this.lightEffect = lightEffect.setData(jsonObject);
        this.mac = jsonObjectToString(jsonObject, DEVICE_PROPERTY_MAC);
        this.model = jsonObjectToString(jsonObject, DEVICE_PROPERTY_MODEL);
        this.nickname = jsonObjectToString(jsonObject, DEVICE_PROPERTY_NICKNAME);
        this.onTime = jsonObjectToNumber(jsonObject, DEVICE_PROPERTY_ONTIME);
        this.overheated = jsonObjectToBool(jsonObject, DEVICE_PROPERTY_OVERHEAT);
        this.region = jsonObjectToString(jsonObject, DEVICE_PROPERTY_REGION);
        this.saturation = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_SATURATION);
        this.signalLevel = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_SIGNAL);
        this.rssi = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_SIGNAL_RSSI);
        this.timeUsagePast7 = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_USAGE_7);
        this.timeUsagePast30 = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_USAGE_30);
        this.timeUsageToday = jsonObjectToInt(jsonObject, DEVICE_PROPERTY_USAGE_TODAY);
        this.type = jsonObjectToString(jsonObject, DEVICE_PROPERTY_TYPE);
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public Integer getBrightness() {
        return brightness;
    }

    public Integer getColorTemp() {
        return colorTemp;
    }

    public String getFirmwareVersion() {
        return fwVer;
    }

    public String getHardwareVersion() {
        return hwVer;
    }

    public HSBType getHSB() {
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        PercentType b = new PercentType(brightness);
        return new HSBType(h, s, b);
    }

    public Integer getHue() {
        return hue;
    }

    public TapoLightEffect getLightEffect() {
        return lightEffect;
    }

    public String getIP() {
        return ip;
    }

    public Boolean isOff() {
        return !deviceOn;
    }

    public Boolean isOn() {
        return deviceOn;
    }

    public Boolean isOverheated() {
        return overheated;
    }

    public String getMAC() {
        return formatMac(mac, MAC_DIVISION_CHAR);
    }

    public String getModel() {
        return model.replace(" ", "_");
    }

    public String getNickname() {
        return nickname;
    }

    public Number getOnTime() {
        return onTime;
    }

    public String getRegion() {
        return region;
    }

    public String getRepresentationProperty() {
        return getMAC();
    }

    public Integer getSaturation() {
        return saturation;
    }

    public String getSerial() {
        return deviceId;
    }

    public Integer getSignalLevel() {
        return signalLevel;
    }

    public Integer getRSSI() {
        return rssi;
    }

    public Number getTimeUsagePast7() {
        return timeUsagePast7;
    }

    public Number getTimeUsagePast30() {
        return timeUsagePast30;
    }

    public Number getTimeUsagePastToday() {
        return timeUsageToday;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
