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
 * Update existing playlist user dto. Fields set to &#x60;null&#x60; will not be updated and keep their current values.
 */
@JsonPropertyOrder({ UpdatePlaylistUserDto.JSON_PROPERTY_CAN_EDIT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UpdatePlaylistUserDto {
    public static final String JSON_PROPERTY_CAN_EDIT = "CanEdit";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canEdit;

    public UpdatePlaylistUserDto() {
    }

    public UpdatePlaylistUserDto canEdit(@org.eclipse.jdt.annotation.NonNull Boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the user can edit the playlist.
     * 
     * @return canEdit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAN_EDIT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanEdit() {
        return canEdit;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_EDIT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanEdit(@org.eclipse.jdt.annotation.NonNull Boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * Return true if this UpdatePlaylistUserDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdatePlaylistUserDto updatePlaylistUserDto = (UpdatePlaylistUserDto) o;
        return Objects.equals(this.canEdit, updatePlaylistUserDto.canEdit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canEdit);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdatePlaylistUserDto {\n");
        sb.append("    canEdit: ").append(toIndentedString(canEdit)).append("\n");
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

        // add `CanEdit` to the URL query string
        if (getCanEdit() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCanEdit%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanEdit()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UpdatePlaylistUserDto instance;

        public Builder() {
            this(new UpdatePlaylistUserDto());
        }

        protected Builder(UpdatePlaylistUserDto instance) {
            this.instance = instance;
        }

        public UpdatePlaylistUserDto.Builder canEdit(Boolean canEdit) {
            this.instance.canEdit = canEdit;
            return this;
        }

        /**
         * returns a built UpdatePlaylistUserDto instance.
         *
         * The builder is not reusable.
         */
        public UpdatePlaylistUserDto build() {
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
    public static UpdatePlaylistUserDto.Builder builder() {
        return new UpdatePlaylistUserDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UpdatePlaylistUserDto.Builder toBuilder() {
        return new UpdatePlaylistUserDto.Builder().canEdit(getCanEdit());
    }
}
