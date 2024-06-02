package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class OverlayTemplate {
    @SerializedName("terminationCondition")
    private OverlayTerminationConditionTemplate terminationCondition = null;

    public OverlayTemplate terminationCondition(OverlayTerminationConditionTemplate terminationCondition) {
        this.terminationCondition = terminationCondition;
        return this;
    }

    public OverlayTerminationConditionTemplate getTerminationCondition() {
        return terminationCondition;
    }

    public void setTerminationCondition(OverlayTerminationConditionTemplate terminationCondition) {
        this.terminationCondition = terminationCondition;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OverlayTemplate overlayTemplate = (OverlayTemplate) o;
        return Objects.equals(this.terminationCondition, overlayTemplate.terminationCondition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terminationCondition);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OverlayTemplate {\n");

        sb.append("    terminationCondition: ").append(toIndentedString(terminationCondition)).append("\n");
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
