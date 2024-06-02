package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class ActivityDataPoints {
    @SerializedName("heatingPower")
    private PercentageDataPoint heatingPower = null;

    @SerializedName("acPower")
    private AcPowerDataPoint acPower = null;

    public ActivityDataPoints heatingPower(PercentageDataPoint heatingPower) {
        this.heatingPower = heatingPower;
        return this;
    }

    public PercentageDataPoint getHeatingPower() {
        return heatingPower;
    }

    public void setHeatingPower(PercentageDataPoint heatingPower) {
        this.heatingPower = heatingPower;
    }

    public ActivityDataPoints acPower(AcPowerDataPoint acPower) {
        this.acPower = acPower;
        return this;
    }

    public AcPowerDataPoint getAcPower() {
        return acPower;
    }

    public void setAcPower(AcPowerDataPoint acPower) {
        this.acPower = acPower;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActivityDataPoints activityDataPoints = (ActivityDataPoints) o;
        return Objects.equals(this.heatingPower, activityDataPoints.heatingPower)
                && Objects.equals(this.acPower, activityDataPoints.acPower);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heatingPower, acPower);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ActivityDataPoints {\n");

        sb.append("    heatingPower: ").append(toIndentedString(heatingPower)).append("\n");
        sb.append("    acPower: ").append(toIndentedString(acPower)).append("\n");
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
