package org.openhab.binding.amplipi.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Partial reconfiguration of an audio Source
 **/
@Schema(description = "Partial reconfiguration of an audio Source ")
public class SourceUpdate {

    @Schema
    /**
     * Friendly name
     **/
    private String name;

    @Schema
    private String input;

    /**
     * Friendly name
     *
     * @return name
     **/
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SourceUpdate name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get input
     *
     * @return input
     **/
    @JsonProperty("input")
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public SourceUpdate input(String input) {
        this.input = input;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SourceUpdate {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    input: ").append(toIndentedString(input)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
