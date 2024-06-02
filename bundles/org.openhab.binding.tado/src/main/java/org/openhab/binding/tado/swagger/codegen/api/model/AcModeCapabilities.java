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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class AcModeCapabilities {
    @SerializedName("temperatures")
    private TemperatureRange temperatures = null;

    @SerializedName("fanSpeeds")
    private List<AcFanSpeed> fanSpeeds = null;

    @SerializedName("swings")
    private List<Power> swings = null;

    @SerializedName("light")
    private List<Power> light = null;

    @SerializedName("fanLevel")
    private List<ACFanLevel> fanLevel = null;

    @SerializedName("verticalSwing")
    private List<ACVerticalSwing> verticalSwing = null;

    @SerializedName("horizontalSwing")
    private List<ACHorizontalSwing> horizontalSwing = null;

    public AcModeCapabilities temperatures(TemperatureRange temperatures) {
        this.temperatures = temperatures;
        return this;
    }

    public TemperatureRange getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(TemperatureRange temperatures) {
        this.temperatures = temperatures;
    }

    public AcModeCapabilities fanSpeeds(List<AcFanSpeed> fanSpeeds) {
        this.fanSpeeds = fanSpeeds;
        return this;
    }

    public AcModeCapabilities addFanSpeedsItem(AcFanSpeed fanSpeedsItem) {
        if (this.fanSpeeds == null) {
            this.fanSpeeds = new ArrayList<>();
        }
        this.fanSpeeds.add(fanSpeedsItem);
        return this;
    }

    public List<AcFanSpeed> getFanSpeeds() {
        return fanSpeeds;
    }

    public void setFanSpeeds(List<AcFanSpeed> fanSpeeds) {
        this.fanSpeeds = fanSpeeds;
    }

    public AcModeCapabilities swings(List<Power> swings) {
        this.swings = swings;
        return this;
    }

    public AcModeCapabilities addSwingsItem(Power swingsItem) {
        if (this.swings == null) {
            this.swings = new ArrayList<>();
        }
        this.swings.add(swingsItem);
        return this;
    }

    public List<Power> getSwings() {
        return swings;
    }

    public void setSwings(List<Power> swings) {
        this.swings = swings;
    }

    public AcModeCapabilities light(List<Power> light) {
        this.light = light;
        return this;
    }

    public AcModeCapabilities addLightItem(Power lightItem) {
        if (this.light == null) {
            this.light = new ArrayList<>();
        }
        this.light.add(lightItem);
        return this;
    }

    public List<Power> getLight() {
        return light;
    }

    public void setLight(List<Power> light) {
        this.light = light;
    }

    public AcModeCapabilities fanLevel(List<ACFanLevel> fanLevel) {
        this.fanLevel = fanLevel;
        return this;
    }

    public AcModeCapabilities addFanLevelItem(ACFanLevel fanLevelItem) {
        if (this.fanLevel == null) {
            this.fanLevel = new ArrayList<>();
        }
        this.fanLevel.add(fanLevelItem);
        return this;
    }

    public List<ACFanLevel> getFanLevel() {
        return fanLevel;
    }

    public void setFanLevel(List<ACFanLevel> fanLevel) {
        this.fanLevel = fanLevel;
    }

    public AcModeCapabilities verticalSwing(List<ACVerticalSwing> verticalSwing) {
        this.verticalSwing = verticalSwing;
        return this;
    }

    public AcModeCapabilities addVerticalSwingItem(ACVerticalSwing verticalSwingItem) {
        if (this.verticalSwing == null) {
            this.verticalSwing = new ArrayList<>();
        }
        this.verticalSwing.add(verticalSwingItem);
        return this;
    }

    public List<ACVerticalSwing> getVerticalSwing() {
        return verticalSwing;
    }

    public void setVerticalSwing(List<ACVerticalSwing> verticalSwing) {
        this.verticalSwing = verticalSwing;
    }

    public AcModeCapabilities horizontalSwing(List<ACHorizontalSwing> horizontalSwing) {
        this.horizontalSwing = horizontalSwing;
        return this;
    }

    public AcModeCapabilities addHorizontalSwingItem(ACHorizontalSwing horizontalSwingItem) {
        if (this.horizontalSwing == null) {
            this.horizontalSwing = new ArrayList<>();
        }
        this.horizontalSwing.add(horizontalSwingItem);
        return this;
    }

    public List<ACHorizontalSwing> getHorizontalSwing() {
        return horizontalSwing;
    }

    public void setHorizontalSwing(List<ACHorizontalSwing> horizontalSwing) {
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
        AcModeCapabilities acModeCapabilities = (AcModeCapabilities) o;
        return Objects.equals(this.temperatures, acModeCapabilities.temperatures)
                && Objects.equals(this.fanSpeeds, acModeCapabilities.fanSpeeds)
                && Objects.equals(this.swings, acModeCapabilities.swings)
                && Objects.equals(this.light, acModeCapabilities.light)
                && Objects.equals(this.fanLevel, acModeCapabilities.fanLevel)
                && Objects.equals(this.verticalSwing, acModeCapabilities.verticalSwing)
                && Objects.equals(this.horizontalSwing, acModeCapabilities.horizontalSwing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperatures, fanSpeeds, swings, light, fanLevel, verticalSwing, horizontalSwing);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AcModeCapabilities {\n");

        sb.append("    temperatures: ").append(toIndentedString(temperatures)).append("\n");
        sb.append("    fanSpeeds: ").append(toIndentedString(fanSpeeds)).append("\n");
        sb.append("    swings: ").append(toIndentedString(swings)).append("\n");
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
