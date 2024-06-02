package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

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
