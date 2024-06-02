package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class TemperatureObject {
    @SerializedName("celsius")
    private Float celsius = null;

    @SerializedName("fahrenheit")
    private Float fahrenheit = null;

    public TemperatureObject celsius(Float celsius) {
        this.celsius = celsius;
        return this;
    }

    public Float getCelsius() {
        return celsius;
    }

    public void setCelsius(Float celsius) {
        this.celsius = celsius;
    }

    public TemperatureObject fahrenheit(Float fahrenheit) {
        this.fahrenheit = fahrenheit;
        return this;
    }

    public Float getFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(Float fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TemperatureObject temperatureObject = (TemperatureObject) o;
        return Objects.equals(this.celsius, temperatureObject.celsius)
                && Objects.equals(this.fahrenheit, temperatureObject.fahrenheit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(celsius, fahrenheit);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TemperatureObject {\n");

        sb.append("    celsius: ").append(toIndentedString(celsius)).append("\n");
        sb.append("    fahrenheit: ").append(toIndentedString(fahrenheit)).append("\n");
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
