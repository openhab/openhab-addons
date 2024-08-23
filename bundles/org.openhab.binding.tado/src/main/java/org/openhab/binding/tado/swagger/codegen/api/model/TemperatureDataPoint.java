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

import java.time.OffsetDateTime;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class TemperatureDataPoint {
    @SerializedName("timestamp")
    private OffsetDateTime timestamp = null;

    @SerializedName("celsius")
    private Float celsius = null;

    @SerializedName("fahrenheit")
    private Float fahrenheit = null;

    public TemperatureDataPoint timestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TemperatureDataPoint celsius(Float celsius) {
        this.celsius = celsius;
        return this;
    }

    public Float getCelsius() {
        return celsius;
    }

    public void setCelsius(Float celsius) {
        this.celsius = celsius;
    }

    public TemperatureDataPoint fahrenheit(Float fahrenheit) {
        this.fahrenheit = fahrenheit;
        return this;
    }

    public Float getFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(Float fahrenheit) {
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
        TemperatureDataPoint temperatureDataPoint = (TemperatureDataPoint) o;
        return Objects.equals(this.timestamp, temperatureDataPoint.timestamp)
                && Objects.equals(this.celsius, temperatureDataPoint.celsius)
                && Objects.equals(this.fahrenheit, temperatureDataPoint.fahrenheit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, celsius, fahrenheit);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TemperatureDataPoint {\n");

        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
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
