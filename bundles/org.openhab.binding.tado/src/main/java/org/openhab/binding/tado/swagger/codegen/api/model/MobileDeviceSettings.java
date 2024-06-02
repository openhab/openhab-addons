package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class MobileDeviceSettings {
    @SerializedName("geoTrackingEnabled")
    private Boolean geoTrackingEnabled = null;

    public MobileDeviceSettings geoTrackingEnabled(Boolean geoTrackingEnabled) {
        this.geoTrackingEnabled = geoTrackingEnabled;
        return this;
    }

    public Boolean isGeoTrackingEnabled() {
        return geoTrackingEnabled;
    }

    public void setGeoTrackingEnabled(Boolean geoTrackingEnabled) {
        this.geoTrackingEnabled = geoTrackingEnabled;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MobileDeviceSettings mobileDeviceSettings = (MobileDeviceSettings) o;
        return Objects.equals(this.geoTrackingEnabled, mobileDeviceSettings.geoTrackingEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(geoTrackingEnabled);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MobileDeviceSettings {\n");

        sb.append("    geoTrackingEnabled: ").append(toIndentedString(geoTrackingEnabled)).append("\n");
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
