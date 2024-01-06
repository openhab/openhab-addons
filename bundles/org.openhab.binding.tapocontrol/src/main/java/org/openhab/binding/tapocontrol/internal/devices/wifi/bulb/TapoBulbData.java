/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.devices.wifi.bulb;

import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoBaseDeviceData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Tapo-Device Information class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoBulbData extends TapoBaseDeviceData {
    @SerializedName("device_on")
    @Expose(serialize = true, deserialize = true)
    private boolean deviceOn = false;

    @Expose(serialize = true, deserialize = true)
    private int brightness = 100;

    @SerializedName("color_temp")
    @Expose(serialize = true, deserialize = true)
    private int colorTemp = 0;

    @Expose(serialize = true, deserialize = true)
    private int hue = 0;

    @Expose(serialize = true, deserialize = true)
    private int saturation = 100;

    @Expose(serialize = false, deserialize = true)
    private long onTime = 0;

    @SerializedName("time_usage_past7")
    @Expose(serialize = false, deserialize = true)
    private long timeUsagePast7 = 0;

    @SerializedName("time_usage_past30")
    @Expose(serialize = false, deserialize = true)
    private long timeUsagePast30 = 0;

    @SerializedName("time_usage_today")
    @Expose(serialize = false, deserialize = true)
    private long timeUsageToday = 0;

    @SerializedName("dynamic_light_effect_enable")
    @Expose(serialize = false, deserialize = true)
    private boolean dynamicLightEffectEnable = false;

    @SerializedName("dynamic_light_effect_id")
    @Expose(serialize = false, deserialize = true)
    private String dynamicLightEffectId = "";

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    public void switchOn() {
        deviceOn = true;
    }

    public void switchOff() {
        deviceOn = false;
    }

    public void switchOnOff(boolean on) {
        deviceOn = on;
    }

    public void setBrightness(int value) {
        brightness = value;
    }

    public void setColorTemp(int value) {
        colorTemp = value;
    }

    public void setHue(int value) {
        hue = value;
    }

    public void setSaturation(int value) {
        saturation = value;
    }

    public void setDynamicLightEffectId(String fxId) {
        dynamicLightEffectId = fxId;
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/
    public boolean dynamicLightEffectEnabled() {
        return dynamicLightEffectEnable;
    }

    public String getDynamicLightEffectId() {
        return dynamicLightEffectId;
    }

    public int getBrightness() {
        return brightness;
    }

    public int getColorTemp() {
        return colorTemp;
    }

    public HSBType getHSB() {
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        PercentType b = new PercentType(brightness);
        return new HSBType(h, s, b);
    }

    public int getHue() {
        return hue;
    }

    public boolean isOff() {
        return !deviceOn;
    }

    public boolean isOn() {
        return deviceOn;
    }

    public long getOnTime() {
        return onTime;
    }

    public int getSaturation() {
        return saturation;
    }

    public long getTimeUsagePast7() {
        return timeUsagePast7;
    }

    public long getTimeUsagePast30() {
        return timeUsagePast30;
    }

    public long getTimeUsagePastToday() {
        return timeUsageToday;
    }

    public TapoBulbModeEnum getWorkingMode() {
        if (dynamicLightEffectEnable) {
            return TapoBulbModeEnum.LIGHT_FX;
        } else if (colorTemp == 0) {
            return TapoBulbModeEnum.COLOR_LIGHT;
        } else {
            return TapoBulbModeEnum.WHITE_LIGHT;
        }
    }

    public boolean supportsMultiRequest() {
        return !getHardwareVersion().startsWith("1");
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
