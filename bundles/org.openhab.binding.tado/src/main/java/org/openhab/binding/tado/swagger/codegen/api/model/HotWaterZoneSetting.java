package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class HotWaterZoneSetting extends GenericZoneSetting {
    @SerializedName("power")
    private Power power = null;

    @SerializedName("temperature")
    private TemperatureObject temperature = null;

    public HotWaterZoneSetting power(Power power) {
        this.power = power;
        return this;
    }

    public Power getPower() {
        return power;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public HotWaterZoneSetting temperature(TemperatureObject temperature) {
        this.temperature = temperature;
        return this;
    }

    public TemperatureObject getTemperature() {
        return temperature;
    }

    public void setTemperature(TemperatureObject temperature) {
        this.temperature = temperature;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HotWaterZoneSetting hotWaterZoneSetting = (HotWaterZoneSetting) o;
        return Objects.equals(this.power, hotWaterZoneSetting.power)
                && Objects.equals(this.temperature, hotWaterZoneSetting.temperature) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(power, temperature, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HotWaterZoneSetting {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    power: ").append(toIndentedString(power)).append("\n");
        sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
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
