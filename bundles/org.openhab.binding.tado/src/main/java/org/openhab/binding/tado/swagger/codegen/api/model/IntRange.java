package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class IntRange {
    @SerializedName("min")
    private Integer min = null;

    @SerializedName("max")
    private Integer max = null;

    @SerializedName("step")
    private Float step = null;

    public IntRange min(Integer min) {
        this.min = min;
        return this;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public IntRange max(Integer max) {
        this.max = max;
        return this;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public IntRange step(Float step) {
        this.step = step;
        return this;
    }

    public Float getStep() {
        return step;
    }

    public void setStep(Float step) {
        this.step = step;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntRange intRange = (IntRange) o;
        return Objects.equals(this.min, intRange.min) && Objects.equals(this.max, intRange.max)
                && Objects.equals(this.step, intRange.step);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, step);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IntRange {\n");

        sb.append("    min: ").append(toIndentedString(min)).append("\n");
        sb.append("    max: ").append(toIndentedString(max)).append("\n");
        sb.append("    step: ").append(toIndentedString(step)).append("\n");
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
