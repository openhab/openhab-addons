package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class Geolocation {
    @SerializedName("latitude")
    private Double latitude = null;

    @SerializedName("longitude")
    private Double longitude = null;

    public Geolocation latitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Geolocation longitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Geolocation geolocation = (Geolocation) o;
        return Objects.equals(this.latitude, geolocation.latitude)
                && Objects.equals(this.longitude, geolocation.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Geolocation {\n");

        sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
        sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
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
