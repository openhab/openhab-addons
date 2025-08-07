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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * PlaystateRequest
 */
@JsonPropertyOrder({ PlaystateRequest.JSON_PROPERTY_COMMAND, PlaystateRequest.JSON_PROPERTY_SEEK_POSITION_TICKS,
        PlaystateRequest.JSON_PROPERTY_CONTROLLING_USER_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaystateRequest {
    public static final String JSON_PROPERTY_COMMAND = "Command";
    @org.eclipse.jdt.annotation.NonNull
    private PlaystateCommand command;

    public static final String JSON_PROPERTY_SEEK_POSITION_TICKS = "SeekPositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long seekPositionTicks;

    public static final String JSON_PROPERTY_CONTROLLING_USER_ID = "ControllingUserId";
    @org.eclipse.jdt.annotation.NonNull
    private String controllingUserId;

    public PlaystateRequest() {
    }

    public PlaystateRequest command(@org.eclipse.jdt.annotation.NonNull PlaystateCommand command) {
        this.command = command;
        return this;
    }

    /**
     * Enum PlaystateCommand.
     * 
     * @return command
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COMMAND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlaystateCommand getCommand() {
        return command;
    }

    @JsonProperty(JSON_PROPERTY_COMMAND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommand(@org.eclipse.jdt.annotation.NonNull PlaystateCommand command) {
        this.command = command;
    }

    public PlaystateRequest seekPositionTicks(@org.eclipse.jdt.annotation.NonNull Long seekPositionTicks) {
        this.seekPositionTicks = seekPositionTicks;
        return this;
    }

    /**
     * Get seekPositionTicks
     * 
     * @return seekPositionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SEEK_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getSeekPositionTicks() {
        return seekPositionTicks;
    }

    @JsonProperty(JSON_PROPERTY_SEEK_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeekPositionTicks(@org.eclipse.jdt.annotation.NonNull Long seekPositionTicks) {
        this.seekPositionTicks = seekPositionTicks;
    }

    public PlaystateRequest controllingUserId(@org.eclipse.jdt.annotation.NonNull String controllingUserId) {
        this.controllingUserId = controllingUserId;
        return this;
    }

    /**
     * Gets or sets the controlling user identifier.
     * 
     * @return controllingUserId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTROLLING_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getControllingUserId() {
        return controllingUserId;
    }

    @JsonProperty(JSON_PROPERTY_CONTROLLING_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setControllingUserId(@org.eclipse.jdt.annotation.NonNull String controllingUserId) {
        this.controllingUserId = controllingUserId;
    }

    /**
     * Return true if this PlaystateRequest object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaystateRequest playstateRequest = (PlaystateRequest) o;
        return Objects.equals(this.command, playstateRequest.command)
                && Objects.equals(this.seekPositionTicks, playstateRequest.seekPositionTicks)
                && Objects.equals(this.controllingUserId, playstateRequest.controllingUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, seekPositionTicks, controllingUserId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlaystateRequest {\n");
        sb.append("    command: ").append(toIndentedString(command)).append("\n");
        sb.append("    seekPositionTicks: ").append(toIndentedString(seekPositionTicks)).append("\n");
        sb.append("    controllingUserId: ").append(toIndentedString(controllingUserId)).append("\n");
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
