/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * GeneralCommand
 */
@JsonPropertyOrder({ GeneralCommand.JSON_PROPERTY_NAME, GeneralCommand.JSON_PROPERTY_CONTROLLING_USER_ID,
        GeneralCommand.JSON_PROPERTY_ARGUMENTS })

public class GeneralCommand {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private GeneralCommandType name;

    public static final String JSON_PROPERTY_CONTROLLING_USER_ID = "ControllingUserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID controllingUserId;

    public static final String JSON_PROPERTY_ARGUMENTS = "Arguments";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> arguments = new HashMap<>();

    public GeneralCommand() {
    }

    public GeneralCommand name(@org.eclipse.jdt.annotation.NonNull GeneralCommandType name) {
        this.name = name;
        return this;
    }

    /**
     * This exists simply to identify a set of known commands.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GeneralCommandType getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull GeneralCommandType name) {
        this.name = name;
    }

    public GeneralCommand controllingUserId(@org.eclipse.jdt.annotation.NonNull UUID controllingUserId) {
        this.controllingUserId = controllingUserId;
        return this;
    }

    /**
     * Get controllingUserId
     * 
     * @return controllingUserId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTROLLING_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getControllingUserId() {
        return controllingUserId;
    }

    @JsonProperty(JSON_PROPERTY_CONTROLLING_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setControllingUserId(@org.eclipse.jdt.annotation.NonNull UUID controllingUserId) {
        this.controllingUserId = controllingUserId;
    }

    public GeneralCommand arguments(@org.eclipse.jdt.annotation.NonNull Map<String, String> arguments) {
        this.arguments = arguments;
        return this;
    }

    public GeneralCommand putArgumentsItem(String key, String argumentsItem) {
        if (this.arguments == null) {
            this.arguments = new HashMap<>();
        }
        this.arguments.put(key, argumentsItem);
        return this;
    }

    /**
     * Get arguments
     * 
     * @return arguments
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ARGUMENTS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getArguments() {
        return arguments;
    }

    @JsonProperty(JSON_PROPERTY_ARGUMENTS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setArguments(@org.eclipse.jdt.annotation.NonNull Map<String, String> arguments) {
        this.arguments = arguments;
    }

    /**
     * Return true if this GeneralCommand object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeneralCommand generalCommand = (GeneralCommand) o;
        return Objects.equals(this.name, generalCommand.name)
                && Objects.equals(this.controllingUserId, generalCommand.controllingUserId)
                && Objects.equals(this.arguments, generalCommand.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, controllingUserId, arguments);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GeneralCommand {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    controllingUserId: ").append(toIndentedString(controllingUserId)).append("\n");
        sb.append("    arguments: ").append(toIndentedString(arguments)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
