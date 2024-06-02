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
public class AirConditioningCapabilities extends GenericZoneCapabilities {
    @SerializedName("COOL")
    private AcModeCapabilities COOL = null;

    @SerializedName("HEAT")
    private AcModeCapabilities HEAT = null;

    @SerializedName("DRY")
    private AcModeCapabilities DRY = null;

    @SerializedName("FAN")
    private AcModeCapabilities FAN = null;

    @SerializedName("AUTO")
    private AcModeCapabilities AUTO = null;

    public AirConditioningCapabilities COOL(AcModeCapabilities COOL) {
        this.COOL = COOL;
        return this;
    }

    public AcModeCapabilities getCOOL() {
        return COOL;
    }

    public void setCOOL(AcModeCapabilities COOL) {
        this.COOL = COOL;
    }

    public AirConditioningCapabilities HEAT(AcModeCapabilities HEAT) {
        this.HEAT = HEAT;
        return this;
    }

    public AcModeCapabilities getHEAT() {
        return HEAT;
    }

    public void setHEAT(AcModeCapabilities HEAT) {
        this.HEAT = HEAT;
    }

    public AirConditioningCapabilities DRY(AcModeCapabilities DRY) {
        this.DRY = DRY;
        return this;
    }

    public AcModeCapabilities getDRY() {
        return DRY;
    }

    public void setDRY(AcModeCapabilities DRY) {
        this.DRY = DRY;
    }

    public AirConditioningCapabilities FAN(AcModeCapabilities FAN) {
        this.FAN = FAN;
        return this;
    }

    public AcModeCapabilities getFAN() {
        return FAN;
    }

    public void setFAN(AcModeCapabilities FAN) {
        this.FAN = FAN;
    }

    public AirConditioningCapabilities AUTO(AcModeCapabilities AUTO) {
        this.AUTO = AUTO;
        return this;
    }

    public AcModeCapabilities getAUTO() {
        return AUTO;
    }

    public void setAUTO(AcModeCapabilities AUTO) {
        this.AUTO = AUTO;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AirConditioningCapabilities airConditioningCapabilities = (AirConditioningCapabilities) o;
        return Objects.equals(this.COOL, airConditioningCapabilities.COOL)
                && Objects.equals(this.HEAT, airConditioningCapabilities.HEAT)
                && Objects.equals(this.DRY, airConditioningCapabilities.DRY)
                && Objects.equals(this.FAN, airConditioningCapabilities.FAN)
                && Objects.equals(this.AUTO, airConditioningCapabilities.AUTO) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(COOL, HEAT, DRY, FAN, AUTO, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AirConditioningCapabilities {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    COOL: ").append(toIndentedString(COOL)).append("\n");
        sb.append("    HEAT: ").append(toIndentedString(HEAT)).append("\n");
        sb.append("    DRY: ").append(toIndentedString(DRY)).append("\n");
        sb.append("    FAN: ").append(toIndentedString(FAN)).append("\n");
        sb.append("    AUTO: ").append(toIndentedString(AUTO)).append("\n");
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
