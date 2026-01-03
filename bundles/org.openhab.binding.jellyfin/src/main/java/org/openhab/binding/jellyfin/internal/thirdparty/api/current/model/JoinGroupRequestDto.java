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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class JoinGroupRequestDto.
 */
@JsonPropertyOrder({ JoinGroupRequestDto.JSON_PROPERTY_GROUP_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class JoinGroupRequestDto {
    public static final String JSON_PROPERTY_GROUP_ID = "GroupId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID groupId;

    public JoinGroupRequestDto() {
    }

    public JoinGroupRequestDto groupId(@org.eclipse.jdt.annotation.Nullable UUID groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * Gets or sets the group identifier.
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

    /**
     * Return true if this JoinGroupRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JoinGroupRequestDto joinGroupRequestDto = (JoinGroupRequestDto) o;
        return Objects.equals(this.groupId, joinGroupRequestDto.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JoinGroupRequestDto {\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
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

        return joiner.toString();
    }

    public static class Builder {

        private JoinGroupRequestDto instance;

        public Builder() {
            this(new JoinGroupRequestDto());
        }

        protected Builder(JoinGroupRequestDto instance) {
            this.instance = instance;
        }

        public JoinGroupRequestDto.Builder groupId(UUID groupId) {
            this.instance.groupId = groupId;
            return this;
        }

        /**
         * returns a built JoinGroupRequestDto instance.
         *
         * The builder is not reusable.
         */
        public JoinGroupRequestDto build() {
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
    public static JoinGroupRequestDto.Builder builder() {
        return new JoinGroupRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public JoinGroupRequestDto.Builder toBuilder() {
        return new JoinGroupRequestDto.Builder().groupId(getGroupId());
    }
}
