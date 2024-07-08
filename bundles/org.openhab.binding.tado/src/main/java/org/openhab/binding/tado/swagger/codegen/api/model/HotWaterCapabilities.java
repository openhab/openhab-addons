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
public class HotWaterCapabilities extends GenericZoneCapabilities {
    @SerializedName("canSetTemperature")
    private Boolean canSetTemperature = null;

    @SerializedName("temperatures")
    private TemperatureRange temperatures = null;

    public HotWaterCapabilities canSetTemperature(Boolean canSetTemperature) {
        this.canSetTemperature = canSetTemperature;
        return this;
    }

    public Boolean isCanSetTemperature() {
        return canSetTemperature;
    }

    public void setCanSetTemperature(Boolean canSetTemperature) {
        this.canSetTemperature = canSetTemperature;
    }

    public HotWaterCapabilities temperatures(TemperatureRange temperatures) {
        this.temperatures = temperatures;
        return this;
    }

    public TemperatureRange getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(TemperatureRange temperatures) {
        this.temperatures = temperatures;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HotWaterCapabilities hotWaterCapabilities = (HotWaterCapabilities) o;
        return Objects.equals(this.canSetTemperature, hotWaterCapabilities.canSetTemperature)
                && Objects.equals(this.temperatures, hotWaterCapabilities.temperatures) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canSetTemperature, temperatures, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HotWaterCapabilities {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    canSetTemperature: ").append(toIndentedString(canSetTemperature)).append("\n");
        sb.append("    temperatures: ").append(toIndentedString(temperatures)).append("\n");
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
