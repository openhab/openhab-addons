package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class MobileDeviceLocation {
    @SerializedName("stale")
    private Boolean stale = null;

    @SerializedName("atHome")
    private Boolean atHome = null;

    public MobileDeviceLocation stale(Boolean stale) {
        this.stale = stale;
        return this;
    }

    public Boolean isStale() {
        return stale;
    }

    public void setStale(Boolean stale) {
        this.stale = stale;
    }

    public MobileDeviceLocation atHome(Boolean atHome) {
        this.atHome = atHome;
        return this;
    }

    public Boolean isAtHome() {
        return atHome;
    }

    public void setAtHome(Boolean atHome) {
        this.atHome = atHome;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MobileDeviceLocation mobileDeviceLocation = (MobileDeviceLocation) o;
        return Objects.equals(this.stale, mobileDeviceLocation.stale)
                && Objects.equals(this.atHome, mobileDeviceLocation.atHome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stale, atHome);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MobileDeviceLocation {\n");

        sb.append("    stale: ").append(toIndentedString(stale)).append("\n");
        sb.append("    atHome: ").append(toIndentedString(atHome)).append("\n");
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
