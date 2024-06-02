package org.openhab.binding.tado.swagger.codegen.api.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class PercentageDataPoint {
    @SerializedName("timestamp")
    private OffsetDateTime timestamp = null;

    @SerializedName("percentage")
    private Float percentage = null;

    public PercentageDataPoint timestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public PercentageDataPoint percentage(Float percentage) {
        this.percentage = percentage;
        return this;
    }

    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PercentageDataPoint percentageDataPoint = (PercentageDataPoint) o;
        return Objects.equals(this.timestamp, percentageDataPoint.timestamp)
                && Objects.equals(this.percentage, percentageDataPoint.percentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, percentage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PercentageDataPoint {\n");

        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
        sb.append("    percentage: ").append(toIndentedString(percentage)).append("\n");
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
