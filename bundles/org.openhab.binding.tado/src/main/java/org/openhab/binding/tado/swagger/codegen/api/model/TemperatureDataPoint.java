package org.openhab.binding.tado.swagger.codegen.api.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

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
