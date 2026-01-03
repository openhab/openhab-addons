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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

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
    @org.eclipse.jdt.annotation.Nullable
    private PlaystateCommand command;

    public static final String JSON_PROPERTY_SEEK_POSITION_TICKS = "SeekPositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long seekPositionTicks;

    public static final String JSON_PROPERTY_CONTROLLING_USER_ID = "ControllingUserId";
    @org.eclipse.jdt.annotation.Nullable
    private String controllingUserId;

    public PlaystateRequest() {
    }

    public PlaystateRequest command(@org.eclipse.jdt.annotation.Nullable PlaystateCommand command) {
        this.command = command;
        return this;
    }

    /**
     * Enum PlaystateCommand.
     * 
     * @return command
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COMMAND, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlaystateCommand getCommand() {
        return command;
    }

    @JsonProperty(value = JSON_PROPERTY_COMMAND, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommand(@org.eclipse.jdt.annotation.Nullable PlaystateCommand command) {
        this.command = command;
    }

    public PlaystateRequest seekPositionTicks(@org.eclipse.jdt.annotation.Nullable Long seekPositionTicks) {
        this.seekPositionTicks = seekPositionTicks;
        return this;
    }

    /**
     * Get seekPositionTicks
     * 
     * @return seekPositionTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SEEK_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getSeekPositionTicks() {
        return seekPositionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_SEEK_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeekPositionTicks(@org.eclipse.jdt.annotation.Nullable Long seekPositionTicks) {
        this.seekPositionTicks = seekPositionTicks;
    }

    public PlaystateRequest controllingUserId(@org.eclipse.jdt.annotation.Nullable String controllingUserId) {
        this.controllingUserId = controllingUserId;
        return this;
    }

    /**
     * Gets or sets the controlling user identifier.
     * 
     * @return controllingUserId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONTROLLING_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getControllingUserId() {
        return controllingUserId;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTROLLING_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setControllingUserId(@org.eclipse.jdt.annotation.Nullable String controllingUserId) {
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

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `Command` to the URL query string
        if (getCommand() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCommand%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCommand()))));
        }

        // add `SeekPositionTicks` to the URL query string
        if (getSeekPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSeekPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeekPositionTicks()))));
        }

        // add `ControllingUserId` to the URL query string
        if (getControllingUserId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sControllingUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getControllingUserId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlaystateRequest instance;

        public Builder() {
            this(new PlaystateRequest());
        }

        protected Builder(PlaystateRequest instance) {
            this.instance = instance;
        }

        public PlaystateRequest.Builder command(PlaystateCommand command) {
            this.instance.command = command;
            return this;
        }

        public PlaystateRequest.Builder seekPositionTicks(Long seekPositionTicks) {
            this.instance.seekPositionTicks = seekPositionTicks;
            return this;
        }

        public PlaystateRequest.Builder controllingUserId(String controllingUserId) {
            this.instance.controllingUserId = controllingUserId;
            return this;
        }

        /**
         * returns a built PlaystateRequest instance.
         *
         * The builder is not reusable.
         */
        public PlaystateRequest build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static PlaystateRequest.Builder builder() {
        return new PlaystateRequest.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlaystateRequest.Builder toBuilder() {
        return new PlaystateRequest.Builder().command(getCommand()).seekPositionTicks(getSeekPositionTicks())
                .controllingUserId(getControllingUserId());
    }
}
