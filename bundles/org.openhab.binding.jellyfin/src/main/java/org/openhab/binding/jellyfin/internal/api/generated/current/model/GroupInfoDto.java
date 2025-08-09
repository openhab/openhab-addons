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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class GroupInfoDto.
 */
@JsonPropertyOrder({ GroupInfoDto.JSON_PROPERTY_GROUP_ID, GroupInfoDto.JSON_PROPERTY_GROUP_NAME,
        GroupInfoDto.JSON_PROPERTY_STATE, GroupInfoDto.JSON_PROPERTY_PARTICIPANTS,
        GroupInfoDto.JSON_PROPERTY_LAST_UPDATED_AT })

public class GroupInfoDto {
    public static final String JSON_PROPERTY_GROUP_ID = "GroupId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID groupId;

    public static final String JSON_PROPERTY_GROUP_NAME = "GroupName";
    @org.eclipse.jdt.annotation.NonNull
    private String groupName;

    public static final String JSON_PROPERTY_STATE = "State";
    @org.eclipse.jdt.annotation.NonNull
    private GroupStateType state;

    public static final String JSON_PROPERTY_PARTICIPANTS = "Participants";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> participants = new ArrayList<>();

    public static final String JSON_PROPERTY_LAST_UPDATED_AT = "LastUpdatedAt";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime lastUpdatedAt;

    public GroupInfoDto() {
    }

    public GroupInfoDto groupId(@org.eclipse.jdt.annotation.NonNull UUID groupId) {
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

    public GroupInfoDto groupName(@org.eclipse.jdt.annotation.NonNull String groupName) {
        this.groupName = groupName;
        return this;
    }

    /**
     * Gets the group name.
     * 
     * @return groupName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GROUP_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGroupName() {
        return groupName;
    }

    @JsonProperty(JSON_PROPERTY_GROUP_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupName(@org.eclipse.jdt.annotation.NonNull String groupName) {
        this.groupName = groupName;
    }

    public GroupInfoDto state(@org.eclipse.jdt.annotation.NonNull GroupStateType state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the group state.
     * 
     * @return state
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GroupStateType getState() {
        return state;
    }

    @JsonProperty(JSON_PROPERTY_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setState(@org.eclipse.jdt.annotation.NonNull GroupStateType state) {
        this.state = state;
    }

    public GroupInfoDto participants(@org.eclipse.jdt.annotation.NonNull List<String> participants) {
        this.participants = participants;
        return this;
    }

    public GroupInfoDto addParticipantsItem(String participantsItem) {
        if (this.participants == null) {
            this.participants = new ArrayList<>();
        }
        this.participants.add(participantsItem);
        return this;
    }

    /**
     * Gets the participants.
     * 
     * @return participants
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARTICIPANTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getParticipants() {
        return participants;
    }

    @JsonProperty(JSON_PROPERTY_PARTICIPANTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParticipants(@org.eclipse.jdt.annotation.NonNull List<String> participants) {
        this.participants = participants;
    }

    public GroupInfoDto lastUpdatedAt(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
        return this;
    }

    /**
     * Gets the date when this DTO has been created.
     * 
     * @return lastUpdatedAt
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LAST_UPDATED_AT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    @JsonProperty(JSON_PROPERTY_LAST_UPDATED_AT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastUpdatedAt(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /**
     * Return true if this GroupInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupInfoDto groupInfoDto = (GroupInfoDto) o;
        return Objects.equals(this.groupId, groupInfoDto.groupId)
                && Objects.equals(this.groupName, groupInfoDto.groupName)
                && Objects.equals(this.state, groupInfoDto.state)
                && Objects.equals(this.participants, groupInfoDto.participants)
                && Objects.equals(this.lastUpdatedAt, groupInfoDto.lastUpdatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, groupName, state, participants, lastUpdatedAt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GroupInfoDto {\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
        sb.append("    groupName: ").append(toIndentedString(groupName)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    participants: ").append(toIndentedString(participants)).append("\n");
        sb.append("    lastUpdatedAt: ").append(toIndentedString(lastUpdatedAt)).append("\n");
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
