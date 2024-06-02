package org.openhab.binding.tado.swagger.codegen.api.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class OpenWindow {
    @SerializedName("durationInSeconds")
    private Integer durationInSeconds = null;

    @SerializedName("expiry")
    private OffsetDateTime expiry = null;

    @SerializedName("remainingTimeInSeconds")
    private Integer remainingTimeInSeconds = null;

    public OpenWindow durationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
        return this;
    }

    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public OpenWindow expiry(OffsetDateTime expiry) {
        this.expiry = expiry;
        return this;
    }

    public OffsetDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(OffsetDateTime expiry) {
        this.expiry = expiry;
    }

    public OpenWindow remainingTimeInSeconds(Integer remainingTimeInSeconds) {
        this.remainingTimeInSeconds = remainingTimeInSeconds;
        return this;
    }

    public Integer getRemainingTimeInSeconds() {
        return remainingTimeInSeconds;
    }

    public void setRemainingTimeInSeconds(Integer remainingTimeInSeconds) {
        this.remainingTimeInSeconds = remainingTimeInSeconds;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenWindow openWindow = (OpenWindow) o;
        return Objects.equals(this.durationInSeconds, openWindow.durationInSeconds)
                && Objects.equals(this.expiry, openWindow.expiry)
                && Objects.equals(this.remainingTimeInSeconds, openWindow.remainingTimeInSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationInSeconds, expiry, remainingTimeInSeconds);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenWindow {\n");

        sb.append("    durationInSeconds: ").append(toIndentedString(durationInSeconds)).append("\n");
        sb.append("    expiry: ").append(toIndentedString(expiry)).append("\n");
        sb.append("    remainingTimeInSeconds: ").append(toIndentedString(remainingTimeInSeconds)).append("\n");
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
