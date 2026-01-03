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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class SendCommand.
 */
@JsonPropertyOrder({ SendCommand.JSON_PROPERTY_GROUP_ID, SendCommand.JSON_PROPERTY_PLAYLIST_ITEM_ID,
        SendCommand.JSON_PROPERTY_WHEN, SendCommand.JSON_PROPERTY_POSITION_TICKS, SendCommand.JSON_PROPERTY_COMMAND,
        SendCommand.JSON_PROPERTY_EMITTED_AT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SendCommand {
    public static final String JSON_PROPERTY_GROUP_ID = "GroupId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID groupId;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID playlistItemId;

    public static final String JSON_PROPERTY_WHEN = "When";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime when;

    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long positionTicks;

    public static final String JSON_PROPERTY_COMMAND = "Command";
    @org.eclipse.jdt.annotation.Nullable
    private SendCommandType command;

    public static final String JSON_PROPERTY_EMITTED_AT = "EmittedAt";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime emittedAt;

    public SendCommand() {
    }

    public SendCommand groupId(@org.eclipse.jdt.annotation.Nullable UUID groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * Gets the group identifier.
     * 
     * @return groupId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_GROUP_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getGroupId() {
        return groupId;
    }

    @JsonProperty(value = JSON_PROPERTY_GROUP_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupId(@org.eclipse.jdt.annotation.Nullable UUID groupId) {
        this.groupId = groupId;
    }

    public SendCommand playlistItemId(@org.eclipse.jdt.annotation.Nullable UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Gets the playlist identifier of the playing item.
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.Nullable UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    public SendCommand when(@org.eclipse.jdt.annotation.Nullable OffsetDateTime when) {
        this.when = when;
        return this;
    }

    /**
     * Gets or sets the UTC time when to execute the command.
     * 
     * @return when
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_WHEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getWhen() {
        return when;
    }

    @JsonProperty(value = JSON_PROPERTY_WHEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWhen(@org.eclipse.jdt.annotation.Nullable OffsetDateTime when) {
        this.when = when;
    }

    public SendCommand positionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets the position ticks.
     * 
     * @return positionTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
    }

    public SendCommand command(@org.eclipse.jdt.annotation.Nullable SendCommandType command) {
        this.command = command;
        return this;
    }

    /**
     * Gets the command.
     * 
     * @return command
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COMMAND, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SendCommandType getCommand() {
        return command;
    }

    @JsonProperty(value = JSON_PROPERTY_COMMAND, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommand(@org.eclipse.jdt.annotation.Nullable SendCommandType command) {
        this.command = command;
    }

    public SendCommand emittedAt(@org.eclipse.jdt.annotation.Nullable OffsetDateTime emittedAt) {
        this.emittedAt = emittedAt;
        return this;
    }

    /**
     * Gets the UTC time when this command has been emitted.
     * 
     * @return emittedAt
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EMITTED_AT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getEmittedAt() {
        return emittedAt;
    }

    @JsonProperty(value = JSON_PROPERTY_EMITTED_AT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEmittedAt(@org.eclipse.jdt.annotation.Nullable OffsetDateTime emittedAt) {
        this.emittedAt = emittedAt;
    }

    /**
     * Return true if this SendCommand object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SendCommand sendCommand = (SendCommand) o;
        return Objects.equals(this.groupId, sendCommand.groupId)
                && Objects.equals(this.playlistItemId, sendCommand.playlistItemId)
                && Objects.equals(this.when, sendCommand.when)
                && Objects.equals(this.positionTicks, sendCommand.positionTicks)
                && Objects.equals(this.command, sendCommand.command)
                && Objects.equals(this.emittedAt, sendCommand.emittedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, playlistItemId, when, positionTicks, command, emittedAt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SendCommand {\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
        sb.append("    playlistItemId: ").append(toIndentedString(playlistItemId)).append("\n");
        sb.append("    when: ").append(toIndentedString(when)).append("\n");
        sb.append("    positionTicks: ").append(toIndentedString(positionTicks)).append("\n");
        sb.append("    command: ").append(toIndentedString(command)).append("\n");
        sb.append("    emittedAt: ").append(toIndentedString(emittedAt)).append("\n");
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

        // add `GroupId` to the URL query string
        if (getGroupId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sGroupId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGroupId()))));
        }

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        // add `When` to the URL query string
        if (getWhen() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sWhen%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWhen()))));
        }

        // add `PositionTicks` to the URL query string
        if (getPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPositionTicks()))));
        }

        // add `Command` to the URL query string
        if (getCommand() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCommand%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCommand()))));
        }

        // add `EmittedAt` to the URL query string
        if (getEmittedAt() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEmittedAt%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEmittedAt()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SendCommand instance;

        public Builder() {
            this(new SendCommand());
        }

        protected Builder(SendCommand instance) {
            this.instance = instance;
        }

        public SendCommand.Builder groupId(UUID groupId) {
            this.instance.groupId = groupId;
            return this;
        }

        public SendCommand.Builder playlistItemId(UUID playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        public SendCommand.Builder when(OffsetDateTime when) {
            this.instance.when = when;
            return this;
        }

        public SendCommand.Builder positionTicks(Long positionTicks) {
            this.instance.positionTicks = positionTicks;
            return this;
        }

        public SendCommand.Builder command(SendCommandType command) {
            this.instance.command = command;
            return this;
        }

        public SendCommand.Builder emittedAt(OffsetDateTime emittedAt) {
            this.instance.emittedAt = emittedAt;
            return this;
        }

        /**
         * returns a built SendCommand instance.
         *
         * The builder is not reusable.
         */
        public SendCommand build() {
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
    public static SendCommand.Builder builder() {
        return new SendCommand.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SendCommand.Builder toBuilder() {
        return new SendCommand.Builder().groupId(getGroupId()).playlistItemId(getPlaylistItemId()).when(getWhen())
                .positionTicks(getPositionTicks()).command(getCommand()).emittedAt(getEmittedAt());
    }
}
