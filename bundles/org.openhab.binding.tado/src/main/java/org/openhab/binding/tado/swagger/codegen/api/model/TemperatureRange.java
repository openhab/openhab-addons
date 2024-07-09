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
public class TemperatureRange {
    @SerializedName("celsius")
    private IntRange celsius = null;

    @SerializedName("fahrenheit")
    private IntRange fahrenheit = null;

    public TemperatureRange celsius(IntRange celsius) {
        this.celsius = celsius;
        return this;
    }

    public IntRange getCelsius() {
        return celsius;
    }

    public void setCelsius(IntRange celsius) {
        this.celsius = celsius;
    }

    public TemperatureRange fahrenheit(IntRange fahrenheit) {
        this.fahrenheit = fahrenheit;
        return this;
    }

    public IntRange getFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(IntRange fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TemperatureRange temperatureRange = (TemperatureRange) o;
        return Objects.equals(this.celsius, temperatureRange.celsius)
                && Objects.equals(this.fahrenheit, temperatureRange.fahrenheit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(celsius, fahrenheit);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TemperatureRange {\n");

        sb.append("    celsius: ").append(toIndentedString(celsius)).append("\n");
        sb.append("    fahrenheit: ").append(toIndentedString(fahrenheit)).append("\n");
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
