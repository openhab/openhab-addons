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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

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
    @org.eclipse.jdt.annotation.NonNull
    private UUID groupId;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID playlistItemId;

    public static final String JSON_PROPERTY_WHEN = "When";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime when;

    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long positionTicks;

    public static final String JSON_PROPERTY_COMMAND = "Command";
    @org.eclipse.jdt.annotation.NonNull
    private SendCommandType command;

    public static final String JSON_PROPERTY_EMITTED_AT = "EmittedAt";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime emittedAt;

    public SendCommand() {
    }

    public SendCommand groupId(@org.eclipse.jdt.annotation.NonNull UUID groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * Gets the group identifier.
     * 
     * @return groupId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getGroupId() {
        return groupId;
    }

    @JsonProperty(JSON_PROPERTY_GROUP_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupId(@org.eclipse.jdt.annotation.NonNull UUID groupId) {
        this.groupId = groupId;
    }

    public SendCommand playlistItemId(@org.eclipse.jdt.annotation.NonNull UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Gets the playlist identifier of the playing item.
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.NonNull UUID playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    public SendCommand when(@org.eclipse.jdt.annotation.NonNull OffsetDateTime when) {
        this.when = when;
        return this;
    }

    /**
     * Gets or sets the UTC time when to execute the command.
     * 
     * @return when
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_WHEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getWhen() {
        return when;
    }

    @JsonProperty(JSON_PROPERTY_WHEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWhen(@org.eclipse.jdt.annotation.NonNull OffsetDateTime when) {
        this.when = when;
    }

    public SendCommand positionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets the position ticks.
     * 
     * @return positionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(JSON_PROPERTY_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
    }

    public SendCommand command(@org.eclipse.jdt.annotation.NonNull SendCommandType command) {
        this.command = command;
        return this;
    }

    /**
     * Gets the command.
     * 
     * @return command
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COMMAND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SendCommandType getCommand() {
        return command;
    }

    @JsonProperty(JSON_PROPERTY_COMMAND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommand(@org.eclipse.jdt.annotation.NonNull SendCommandType command) {
        this.command = command;
    }

    public SendCommand emittedAt(@org.eclipse.jdt.annotation.NonNull OffsetDateTime emittedAt) {
        this.emittedAt = emittedAt;
        return this;
    }

    /**
     * Gets the UTC time when this command has been emitted.
     * 
     * @return emittedAt
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_EMITTED_AT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getEmittedAt() {
        return emittedAt;
    }

    @JsonProperty(JSON_PROPERTY_EMITTED_AT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEmittedAt(@org.eclipse.jdt.annotation.NonNull OffsetDateTime emittedAt) {
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
}
