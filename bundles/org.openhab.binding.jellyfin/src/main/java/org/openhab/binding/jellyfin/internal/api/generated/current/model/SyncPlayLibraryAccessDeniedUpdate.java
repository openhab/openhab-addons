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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * SyncPlayLibraryAccessDeniedUpdate
 */
@JsonPropertyOrder({ SyncPlayLibraryAccessDeniedUpdate.JSON_PROPERTY_GROUP_ID,
        SyncPlayLibraryAccessDeniedUpdate.JSON_PROPERTY_DATA, SyncPlayLibraryAccessDeniedUpdate.JSON_PROPERTY_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SyncPlayLibraryAccessDeniedUpdate {
    public static final String JSON_PROPERTY_GROUP_ID = "GroupId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID groupId;

    public static final String JSON_PROPERTY_DATA = "Data";
    @org.eclipse.jdt.annotation.NonNull
    private String data;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private GroupUpdateType type = GroupUpdateType.LIBRARY_ACCESS_DENIED;

    public SyncPlayLibraryAccessDeniedUpdate() {
    }

    @JsonCreator
    public SyncPlayLibraryAccessDeniedUpdate(@JsonProperty(JSON_PROPERTY_GROUP_ID) UUID groupId,
            @JsonProperty(JSON_PROPERTY_DATA) String data, @JsonProperty(JSON_PROPERTY_TYPE) GroupUpdateType type) {
        this();
        this.groupId = groupId;
        this.data = data;
        this.type = type;
    }

    /**
     * Gets the group identifier.
     * 
     * @return groupId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_GROUP_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getGroupId() {
        return groupId;
    }

    /**
     * Gets the update data.
     * 
     * @return data
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getData() {
        return data;
    }

    /**
     * Enum GroupUpdateType.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupUpdateType getType() {
        return type;
    }

    /**
     * Return true if this SyncPlayLibraryAccessDeniedUpdate object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SyncPlayLibraryAccessDeniedUpdate syncPlayLibraryAccessDeniedUpdate = (SyncPlayLibraryAccessDeniedUpdate) o;
        return Objects.equals(this.groupId, syncPlayLibraryAccessDeniedUpdate.groupId)
                && Objects.equals(this.data, syncPlayLibraryAccessDeniedUpdate.data)
                && Objects.equals(this.type, syncPlayLibraryAccessDeniedUpdate.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, data, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SyncPlayLibraryAccessDeniedUpdate {\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sGroupId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGroupId()))));
        }

        // add `Data` to the URL query string
        if (getData() != null) {
            joiner.add(String.format(Locale.ROOT, "%sData%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getData()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SyncPlayLibraryAccessDeniedUpdate instance;

        public Builder() {
            this(new SyncPlayLibraryAccessDeniedUpdate());
        }

        protected Builder(SyncPlayLibraryAccessDeniedUpdate instance) {
            this.instance = instance;
        }

        public SyncPlayLibraryAccessDeniedUpdate.Builder groupId(UUID groupId) {
            this.instance.groupId = groupId;
            return this;
        }

        public SyncPlayLibraryAccessDeniedUpdate.Builder data(String data) {
            this.instance.data = data;
            return this;
        }

        public SyncPlayLibraryAccessDeniedUpdate.Builder type(GroupUpdateType type) {
            this.instance.type = type;
            return this;
        }

        /**
         * returns a built SyncPlayLibraryAccessDeniedUpdate instance.
         *
         * The builder is not reusable.
         */
        public SyncPlayLibraryAccessDeniedUpdate build() {
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
    public static SyncPlayLibraryAccessDeniedUpdate.Builder builder() {
        return new SyncPlayLibraryAccessDeniedUpdate.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SyncPlayLibraryAccessDeniedUpdate.Builder toBuilder() {
        return new SyncPlayLibraryAccessDeniedUpdate.Builder().groupId(getGroupId()).data(getData()).type(getType());
    }
}
