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

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class NewGroupRequestDto.
 */
@JsonPropertyOrder({ NewGroupRequestDto.JSON_PROPERTY_GROUP_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NewGroupRequestDto {
    public static final String JSON_PROPERTY_GROUP_NAME = "GroupName";
    @org.eclipse.jdt.annotation.NonNull
    private String groupName;

    public NewGroupRequestDto() {
    }

    public NewGroupRequestDto groupName(@org.eclipse.jdt.annotation.NonNull String groupName) {
        this.groupName = groupName;
        return this;
    }

    /**
     * Gets or sets the group name.
     * 
     * @return groupName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_GROUP_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty(value = JSON_PROPERTY_GROUP_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupName(@org.eclipse.jdt.annotation.NonNull String groupName) {
        this.groupName = groupName;
    }

    /**
     * Return true if this NewGroupRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NewGroupRequestDto newGroupRequestDto = (NewGroupRequestDto) o;
        return Objects.equals(this.groupName, newGroupRequestDto.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NewGroupRequestDto {\n");
        sb.append("    groupName: ").append(toIndentedString(groupName)).append("\n");
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

        // add `GroupName` to the URL query string
        if (getGroupName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sGroupName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGroupName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private NewGroupRequestDto instance;

        public Builder() {
            this(new NewGroupRequestDto());
        }

        protected Builder(NewGroupRequestDto instance) {
            this.instance = instance;
        }

        public NewGroupRequestDto.Builder groupName(String groupName) {
            this.instance.groupName = groupName;
            return this;
        }

        /**
         * returns a built NewGroupRequestDto instance.
         *
         * The builder is not reusable.
         */
        public NewGroupRequestDto build() {
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
    public static NewGroupRequestDto.Builder builder() {
        return new NewGroupRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public NewGroupRequestDto.Builder toBuilder() {
        return new NewGroupRequestDto.Builder().groupName(getGroupName());
    }
}
