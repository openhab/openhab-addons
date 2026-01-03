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
 * Api model for MediaSegment&#39;s.
 */
@JsonPropertyOrder({ MediaSegmentDto.JSON_PROPERTY_ID, MediaSegmentDto.JSON_PROPERTY_ITEM_ID,
        MediaSegmentDto.JSON_PROPERTY_TYPE, MediaSegmentDto.JSON_PROPERTY_START_TICKS,
        MediaSegmentDto.JSON_PROPERTY_END_TICKS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaSegmentDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private UUID id;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID itemId;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.Nullable
    private MediaSegmentType type = MediaSegmentType.UNKNOWN;

    public static final String JSON_PROPERTY_START_TICKS = "StartTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long startTicks;

    public static final String JSON_PROPERTY_END_TICKS = "EndTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long endTicks;

    public MediaSegmentDto() {
    }

    public MediaSegmentDto id(@org.eclipse.jdt.annotation.Nullable UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id of the media segment.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.Nullable UUID id) {
        this.id = id;
    }

    public MediaSegmentDto itemId(@org.eclipse.jdt.annotation.Nullable UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the id of the associated item.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.Nullable UUID itemId) {
        this.itemId = itemId;
    }

    public MediaSegmentDto type(@org.eclipse.jdt.annotation.Nullable MediaSegmentType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type of content this segment defines.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaSegmentType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.Nullable MediaSegmentType type) {
        this.type = type;
    }

    public MediaSegmentDto startTicks(@org.eclipse.jdt.annotation.Nullable Long startTicks) {
        this.startTicks = startTicks;
        return this;
    }

    /**
     * Gets or sets the start of the segment.
     * 
     * @return startTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_START_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getStartTicks() {
        return startTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_START_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartTicks(@org.eclipse.jdt.annotation.Nullable Long startTicks) {
        this.startTicks = startTicks;
    }

    public MediaSegmentDto endTicks(@org.eclipse.jdt.annotation.Nullable Long endTicks) {
        this.endTicks = endTicks;
        return this;
    }

    /**
     * Gets or sets the end of the segment.
     * 
     * @return endTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_END_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getEndTicks() {
        return endTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_END_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndTicks(@org.eclipse.jdt.annotation.Nullable Long endTicks) {
        this.endTicks = endTicks;
    }

    /**
     * Return true if this MediaSegmentDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaSegmentDto mediaSegmentDto = (MediaSegmentDto) o;
        return Objects.equals(this.id, mediaSegmentDto.id) && Objects.equals(this.itemId, mediaSegmentDto.itemId)
                && Objects.equals(this.type, mediaSegmentDto.type)
                && Objects.equals(this.startTicks, mediaSegmentDto.startTicks)
                && Objects.equals(this.endTicks, mediaSegmentDto.endTicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemId, type, startTicks, endTicks);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaSegmentDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    startTicks: ").append(toIndentedString(startTicks)).append("\n");
        sb.append("    endTicks: ").append(toIndentedString(endTicks)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `StartTicks` to the URL query string
        if (getStartTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sStartTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartTicks()))));
        }

        // add `EndTicks` to the URL query string
        if (getEndTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEndTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndTicks()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MediaSegmentDto instance;

        public Builder() {
            this(new MediaSegmentDto());
        }

        protected Builder(MediaSegmentDto instance) {
            this.instance = instance;
        }

        public MediaSegmentDto.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public MediaSegmentDto.Builder itemId(UUID itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        public MediaSegmentDto.Builder type(MediaSegmentType type) {
            this.instance.type = type;
            return this;
        }

        public MediaSegmentDto.Builder startTicks(Long startTicks) {
            this.instance.startTicks = startTicks;
            return this;
        }

        public MediaSegmentDto.Builder endTicks(Long endTicks) {
            this.instance.endTicks = endTicks;
            return this;
        }

        /**
         * returns a built MediaSegmentDto instance.
         *
         * The builder is not reusable.
         */
        public MediaSegmentDto build() {
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
    public static MediaSegmentDto.Builder builder() {
        return new MediaSegmentDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MediaSegmentDto.Builder toBuilder() {
        return new MediaSegmentDto.Builder().id(getId()).itemId(getItemId()).type(getType()).startTicks(getStartTicks())
                .endTicks(getEndTicks());
    }
}
