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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class GroupUpdate.
 */
@JsonPropertyOrder({ ObjectGroupUpdate.JSON_PROPERTY_GROUP_ID, ObjectGroupUpdate.JSON_PROPERTY_TYPE,
        ObjectGroupUpdate.JSON_PROPERTY_DATA })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ObjectGroupUpdate {
    public static final String JSON_PROPERTY_GROUP_ID = "GroupId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID groupId;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private GroupUpdateType type;

    public static final String JSON_PROPERTY_DATA = "Data";
    @org.eclipse.jdt.annotation.NonNull
    private Object data = null;

    public ObjectGroupUpdate() {
    }

    public ObjectGroupUpdate groupId(@org.eclipse.jdt.annotation.NonNull UUID groupId) {
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

    public ObjectGroupUpdate type(@org.eclipse.jdt.annotation.NonNull GroupUpdateType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets the update type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupUpdateType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull GroupUpdateType type) {
        this.type = type;
    }

    public ObjectGroupUpdate data(@org.eclipse.jdt.annotation.NonNull Object data) {
        this.data = data;
        return this;
    }

    /**
     * Gets the update data.
     * 
     * @return data
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Object getData() {
        return data;
    }

    @JsonProperty(JSON_PROPERTY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setData(@org.eclipse.jdt.annotation.NonNull Object data) {
        this.data = data;
    }

    /**
     * Return true if this ObjectGroupUpdate object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ObjectGroupUpdate objectGroupUpdate = (ObjectGroupUpdate) o;
        return Objects.equals(this.groupId, objectGroupUpdate.groupId)
                && Objects.equals(this.type, objectGroupUpdate.type)
                && Objects.equals(this.data, objectGroupUpdate.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, type, data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ObjectGroupUpdate {\n");
        sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
            joiner.add(String.format("%sGroupId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGroupId()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format("%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `Data` to the URL query string
        if (getData() != null) {
            joiner.add(String.format("%sData%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getData()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ObjectGroupUpdate instance;

        public Builder() {
            this(new ObjectGroupUpdate());
        }

        protected Builder(ObjectGroupUpdate instance) {
            this.instance = instance;
        }

        public ObjectGroupUpdate.Builder groupId(UUID groupId) {
            this.instance.groupId = groupId;
            return this;
        }

        public ObjectGroupUpdate.Builder type(GroupUpdateType type) {
            this.instance.type = type;
            return this;
        }

        public ObjectGroupUpdate.Builder data(Object data) {
            this.instance.data = data;
            return this;
        }

        /**
         * returns a built ObjectGroupUpdate instance.
         *
         * The builder is not reusable.
         */
        public ObjectGroupUpdate build() {
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
    public static ObjectGroupUpdate.Builder builder() {
        return new ObjectGroupUpdate.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ObjectGroupUpdate.Builder toBuilder() {
        return new ObjectGroupUpdate.Builder().groupId(getGroupId()).type(getType()).data(getData());
    }
}
