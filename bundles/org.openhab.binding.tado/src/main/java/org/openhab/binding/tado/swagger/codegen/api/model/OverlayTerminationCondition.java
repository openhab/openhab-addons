package org.openhab.binding.tado.swagger.codegen.api.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class OverlayTerminationCondition {
    @SerializedName("type")
    private OverlayTerminationConditionType type = null;

    @SerializedName("projectedExpiry")
    private OffsetDateTime projectedExpiry = null;

    public OverlayTerminationCondition type(OverlayTerminationConditionType type) {
        this.type = type;
        return this;
    }

    public OverlayTerminationConditionType getType() {
        return type;
    }

    public void setType(OverlayTerminationConditionType type) {
        this.type = type;
    }

    public OffsetDateTime getProjectedExpiry() {
        return projectedExpiry;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OverlayTerminationCondition overlayTerminationCondition = (OverlayTerminationCondition) o;
        return Objects.equals(this.type, overlayTerminationCondition.type)
                && Objects.equals(this.projectedExpiry, overlayTerminationCondition.projectedExpiry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, projectedExpiry);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OverlayTerminationCondition {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    projectedExpiry: ").append(toIndentedString(projectedExpiry)).append("\n");
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
