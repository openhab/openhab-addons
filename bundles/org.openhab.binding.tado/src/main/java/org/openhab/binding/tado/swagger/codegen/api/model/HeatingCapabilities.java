package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class HeatingCapabilities extends GenericZoneCapabilities {
    @SerializedName("temperatures")
    private TemperatureRange temperatures = null;

    public HeatingCapabilities temperatures(TemperatureRange temperatures) {
        this.temperatures = temperatures;
        return this;
    }

    public TemperatureRange getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(TemperatureRange temperatures) {
        this.temperatures = temperatures;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HeatingCapabilities heatingCapabilities = (HeatingCapabilities) o;
        return Objects.equals(this.temperatures, heatingCapabilities.temperatures) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperatures, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HeatingCapabilities {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    temperatures: ").append(toIndentedString(temperatures)).append("\n");
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
