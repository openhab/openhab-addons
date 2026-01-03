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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class GroupInfoDto.
 */
@JsonPropertyOrder({ GroupInfoDto.JSON_PROPERTY_GROUP_ID, GroupInfoDto.JSON_PROPERTY_GROUP_NAME,
        GroupInfoDto.JSON_PROPERTY_STATE, GroupInfoDto.JSON_PROPERTY_PARTICIPANTS,
        GroupInfoDto.JSON_PROPERTY_LAST_UPDATED_AT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class GroupInfoDto {
    public static final String JSON_PROPERTY_GROUP_ID = "GroupId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID groupId;

    public static final String JSON_PROPERTY_GROUP_NAME = "GroupName";
    @org.eclipse.jdt.annotation.Nullable
    private String groupName;

    public static final String JSON_PROPERTY_STATE = "State";
    @org.eclipse.jdt.annotation.Nullable
    private GroupStateType state;

    public static final String JSON_PROPERTY_PARTICIPANTS = "Participants";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> participants = new ArrayList<>();

    public static final String JSON_PROPERTY_LAST_UPDATED_AT = "LastUpdatedAt";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime lastUpdatedAt;

    public GroupInfoDto() {
    }

    public GroupInfoDto groupId(@org.eclipse.jdt.annotation.Nullable UUID groupId) {
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

    public GroupInfoDto groupName(@org.eclipse.jdt.annotation.Nullable String groupName) {
        this.groupName = groupName;
        return this;
    }

    /**
     * Gets the group name.
     * 
     * @return groupName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_GROUP_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty(value = JSON_PROPERTY_GROUP_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupName(@org.eclipse.jdt.annotation.Nullable String groupName) {
        this.groupName = groupName;
    }

    public GroupInfoDto state(@org.eclipse.jdt.annotation.Nullable GroupStateType state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the group state.
     * 
     * @return state
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_STATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupStateType getState() {
        return state;
    }

    @JsonProperty(value = JSON_PROPERTY_STATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setState(@org.eclipse.jdt.annotation.Nullable GroupStateType state) {
        this.state = state;
    }

    public GroupInfoDto participants(@org.eclipse.jdt.annotation.Nullable List<String> participants) {
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARTICIPANTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getParticipants() {
        return participants;
    }

    @JsonProperty(value = JSON_PROPERTY_PARTICIPANTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParticipants(@org.eclipse.jdt.annotation.Nullable List<String> participants) {
        this.participants = participants;
    }

    public GroupInfoDto lastUpdatedAt(@org.eclipse.jdt.annotation.Nullable OffsetDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
        return this;
    }

    /**
     * Gets the date when this DTO has been created.
     * 
     * @return lastUpdatedAt
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LAST_UPDATED_AT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_UPDATED_AT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastUpdatedAt(@org.eclipse.jdt.annotation.Nullable OffsetDateTime lastUpdatedAt) {
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

        // add `GroupName` to the URL query string
        if (getGroupName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sGroupName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGroupName()))));
        }

        // add `State` to the URL query string
        if (getState() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sState%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getState()))));
        }

        // add `Participants` to the URL query string
        if (getParticipants() != null) {
            for (int i = 0; i < getParticipants().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sParticipants%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getParticipants().get(i)))));
            }
        }

        // add `LastUpdatedAt` to the URL query string
        if (getLastUpdatedAt() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLastUpdatedAt%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastUpdatedAt()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private GroupInfoDto instance;

        public Builder() {
            this(new GroupInfoDto());
        }

        protected Builder(GroupInfoDto instance) {
            this.instance = instance;
        }

        public GroupInfoDto.Builder groupId(UUID groupId) {
            this.instance.groupId = groupId;
            return this;
        }

        public GroupInfoDto.Builder groupName(String groupName) {
            this.instance.groupName = groupName;
            return this;
        }

        public GroupInfoDto.Builder state(GroupStateType state) {
            this.instance.state = state;
            return this;
        }

        public GroupInfoDto.Builder participants(List<String> participants) {
            this.instance.participants = participants;
            return this;
        }

        public GroupInfoDto.Builder lastUpdatedAt(OffsetDateTime lastUpdatedAt) {
            this.instance.lastUpdatedAt = lastUpdatedAt;
            return this;
        }

        /**
         * returns a built GroupInfoDto instance.
         *
         * The builder is not reusable.
         */
        public GroupInfoDto build() {
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
    public static GroupInfoDto.Builder builder() {
        return new GroupInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public GroupInfoDto.Builder toBuilder() {
        return new GroupInfoDto.Builder().groupId(getGroupId()).groupName(getGroupName()).state(getState())
                .participants(getParticipants()).lastUpdatedAt(getLastUpdatedAt());
    }
}
