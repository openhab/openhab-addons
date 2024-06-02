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
package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class CoolingZoneSetting extends GenericZoneSetting {
    @SerializedName("power")
    private Power power = null;

    @SerializedName("mode")
    private AcMode mode = null;

    @SerializedName("temperature")
    private TemperatureObject temperature = null;

    @SerializedName("fanSpeed")
    private AcFanSpeed fanSpeed = null;

    @SerializedName("swing")
    private Power swing = null;

    @SerializedName("light")
    private Power light = null;

    @SerializedName("fanLevel")
    private ACFanLevel fanLevel = null;

    @SerializedName("verticalSwing")
    private ACVerticalSwing verticalSwing = null;

    @SerializedName("horizontalSwing")
    private ACHorizontalSwing horizontalSwing = null;

    public CoolingZoneSetting power(Power power) {
        this.power = power;
        return this;
    }

    public Power getPower() {
        return power;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public CoolingZoneSetting mode(AcMode mode) {
        this.mode = mode;
        return this;
    }

    public AcMode getMode() {
        return mode;
    }

    public void setMode(AcMode mode) {
        this.mode = mode;
    }

    public CoolingZoneSetting temperature(TemperatureObject temperature) {
        this.temperature = temperature;
        return this;
    }

    public TemperatureObject getTemperature() {
        return temperature;
    }

    public void setTemperature(TemperatureObject temperature) {
        this.temperature = temperature;
    }

    public CoolingZoneSetting fanSpeed(AcFanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
        return this;
    }

    public AcFanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(AcFanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public CoolingZoneSetting swing(Power swing) {
        this.swing = swing;
        return this;
    }

    public Power getSwing() {
        return swing;
    }

    public void setSwing(Power swing) {
        this.swing = swing;
    }

    public CoolingZoneSetting light(Power light) {
        this.light = light;
        return this;
    }

    public Power getLight() {
        return light;
    }

    public void setLight(Power light) {
        this.light = light;
    }

    public CoolingZoneSetting fanLevel(ACFanLevel fanLevel) {
        this.fanLevel = fanLevel;
        return this;
    }

    public ACFanLevel getFanLevel() {
        return fanLevel;
    }

    public void setFanLevel(ACFanLevel fanLevel) {
        this.fanLevel = fanLevel;
    }

    public CoolingZoneSetting verticalSwing(ACVerticalSwing verticalSwing) {
        this.verticalSwing = verticalSwing;
        return this;
    }

    public ACVerticalSwing getVerticalSwing() {
        return verticalSwing;
    }

    public void setVerticalSwing(ACVerticalSwing verticalSwing) {
        this.verticalSwing = verticalSwing;
    }

    public CoolingZoneSetting horizontalSwing(ACHorizontalSwing horizontalSwing) {
        this.horizontalSwing = horizontalSwing;
        return this;
    }

    public ACHorizontalSwing getHorizontalSwing() {
        return horizontalSwing;
    }

    public void setHorizontalSwing(ACHorizontalSwing horizontalSwing) {
        this.horizontalSwing = horizontalSwing;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CoolingZoneSetting coolingZoneSetting = (CoolingZoneSetting) o;
        return Objects.equals(this.power, coolingZoneSetting.power)
                && Objects.equals(this.mode, coolingZoneSetting.mode)
                && Objects.equals(this.temperature, coolingZoneSetting.temperature)
                && Objects.equals(this.fanSpeed, coolingZoneSetting.fanSpeed)
                && Objects.equals(this.swing, coolingZoneSetting.swing)
                && Objects.equals(this.light, coolingZoneSetting.light)
                && Objects.equals(this.fanLevel, coolingZoneSetting.fanLevel)
                && Objects.equals(this.verticalSwing, coolingZoneSetting.verticalSwing)
                && Objects.equals(this.horizontalSwing, coolingZoneSetting.horizontalSwing) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(power, mode, temperature, fanSpeed, swing, light, fanLevel, verticalSwing, horizontalSwing,
                super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CoolingZoneSetting {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    power: ").append(toIndentedString(power)).append("\n");
        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
        sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
        sb.append("    fanSpeed: ").append(toIndentedString(fanSpeed)).append("\n");
        sb.append("    swing: ").append(toIndentedString(swing)).append("\n");
        sb.append("    light: ").append(toIndentedString(light)).append("\n");
        sb.append("    fanLevel: ").append(toIndentedString(fanLevel)).append("\n");
        sb.append("    verticalSwing: ").append(toIndentedString(verticalSwing)).append("\n");
        sb.append("    horizontalSwing: ").append(toIndentedString(horizontalSwing)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
