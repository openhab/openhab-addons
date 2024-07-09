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
public class SensorDataPoints {
    @SerializedName("insideTemperature")
    private TemperatureDataPoint insideTemperature = null;

    @SerializedName("humidity")
    private PercentageDataPoint humidity = null;

    public SensorDataPoints insideTemperature(TemperatureDataPoint insideTemperature) {
        this.insideTemperature = insideTemperature;
        return this;
    }

    public TemperatureDataPoint getInsideTemperature() {
        return insideTemperature;
    }

    public void setInsideTemperature(TemperatureDataPoint insideTemperature) {
        this.insideTemperature = insideTemperature;
    }

    public SensorDataPoints humidity(PercentageDataPoint humidity) {
        this.humidity = humidity;
        return this;
    }

    public PercentageDataPoint getHumidity() {
        return humidity;
    }

    public void setHumidity(PercentageDataPoint humidity) {
        this.humidity = humidity;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SensorDataPoints sensorDataPoints = (SensorDataPoints) o;
        return Objects.equals(this.insideTemperature, sensorDataPoints.insideTemperature)
                && Objects.equals(this.humidity, sensorDataPoints.humidity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(insideTemperature, humidity);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SensorDataPoints {\n");

        sb.append("    insideTemperature: ").append(toIndentedString(insideTemperature)).append("\n");
        sb.append("    humidity: ").append(toIndentedString(humidity)).append("\n");
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
