package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class GenericZoneCapabilities {
    @SerializedName("type")
    private TadoSystemType type = null;

    public GenericZoneCapabilities type(TadoSystemType type) {
        this.type = type;
        return this;
    }

    public TadoSystemType getType() {
        return type;
    }

    public void setType(TadoSystemType type) {
        this.type = type;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenericZoneCapabilities genericZoneCapabilities = (GenericZoneCapabilities) o;
        return Objects.equals(this.type, genericZoneCapabilities.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GenericZoneCapabilities {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
