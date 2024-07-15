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
public class HeatingZoneSetting extends GenericZoneSetting {
    @SerializedName("power")
    private Power power = null;

    @SerializedName("temperature")
    private TemperatureObject temperature = null;

    public HeatingZoneSetting power(Power power) {
        this.power = power;
        return this;
    }

    public Power getPower() {
        return power;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public HeatingZoneSetting temperature(TemperatureObject temperature) {
        this.temperature = temperature;
        return this;
    }

    public TemperatureObject getTemperature() {
        return temperature;
    }

    public void setTemperature(TemperatureObject temperature) {
        this.temperature = temperature;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HeatingZoneSetting heatingZoneSetting = (HeatingZoneSetting) o;
        return Objects.equals(this.power, heatingZoneSetting.power)
                && Objects.equals(this.temperature, heatingZoneSetting.temperature) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(power, temperature, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HeatingZoneSetting {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    power: ").append(toIndentedString(power)).append("\n");
        sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
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
