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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Media Update Info Dto.
 */
@JsonPropertyOrder({ MediaUpdateInfoDto.JSON_PROPERTY_UPDATES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaUpdateInfoDto {
    public static final String JSON_PROPERTY_UPDATES = "Updates";
    @org.eclipse.jdt.annotation.Nullable
    private List<MediaUpdateInfoPathDto> updates = new ArrayList<>();

    public MediaUpdateInfoDto() {
    }

    public MediaUpdateInfoDto updates(@org.eclipse.jdt.annotation.Nullable List<MediaUpdateInfoPathDto> updates) {
        this.updates = updates;
        return this;
    }

    public MediaUpdateInfoDto addUpdatesItem(MediaUpdateInfoPathDto updatesItem) {
        if (this.updates == null) {
            this.updates = new ArrayList<>();
        }
        this.updates.add(updatesItem);
        return this;
    }

    /**
     * Gets or sets the list of updates.
     * 
     * @return updates
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_UPDATES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaUpdateInfoPathDto> getUpdates() {
        return updates;
    }

    @JsonProperty(value = JSON_PROPERTY_UPDATES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUpdates(@org.eclipse.jdt.annotation.Nullable List<MediaUpdateInfoPathDto> updates) {
        this.updates = updates;
    }

    /**
     * Return true if this MediaUpdateInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaUpdateInfoDto mediaUpdateInfoDto = (MediaUpdateInfoDto) o;
        return Objects.equals(this.updates, mediaUpdateInfoDto.updates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updates);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaUpdateInfoDto {\n");
        sb.append("    updates: ").append(toIndentedString(updates)).append("\n");
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

        // add `Updates` to the URL query string
        if (getUpdates() != null) {
            for (int i = 0; i < getUpdates().size(); i++) {
                if (getUpdates().get(i) != null) {
                    joiner.add(getUpdates().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sUpdates%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private MediaUpdateInfoDto instance;

        public Builder() {
            this(new MediaUpdateInfoDto());
        }

        protected Builder(MediaUpdateInfoDto instance) {
            this.instance = instance;
        }

        public MediaUpdateInfoDto.Builder updates(List<MediaUpdateInfoPathDto> updates) {
            this.instance.updates = updates;
            return this;
        }

        /**
         * returns a built MediaUpdateInfoDto instance.
         *
         * The builder is not reusable.
         */
        public MediaUpdateInfoDto build() {
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
    public static MediaUpdateInfoDto.Builder builder() {
        return new MediaUpdateInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MediaUpdateInfoDto.Builder toBuilder() {
        return new MediaUpdateInfoDto.Builder().updates(getUpdates());
    }
}
