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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class QueueRequestDto.
 */
@JsonPropertyOrder({ QueueRequestDto.JSON_PROPERTY_ITEM_IDS, QueueRequestDto.JSON_PROPERTY_MODE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QueueRequestDto {
    public static final String JSON_PROPERTY_ITEM_IDS = "ItemIds";
    @org.eclipse.jdt.annotation.Nullable
    private List<UUID> itemIds = new ArrayList<>();

    public static final String JSON_PROPERTY_MODE = "Mode";
    @org.eclipse.jdt.annotation.Nullable
    private GroupQueueMode mode;

    public QueueRequestDto() {
    }

    public QueueRequestDto itemIds(@org.eclipse.jdt.annotation.Nullable List<UUID> itemIds) {
        this.itemIds = itemIds;
        return this;
    }

    public QueueRequestDto addItemIdsItem(UUID itemIdsItem) {
        if (this.itemIds == null) {
            this.itemIds = new ArrayList<>();
        }
        this.itemIds.add(itemIdsItem);
        return this;
    }

    /**
     * Gets or sets the items to enqueue.
     * 
     * @return itemIds
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEM_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getItemIds() {
        return itemIds;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemIds(@org.eclipse.jdt.annotation.Nullable List<UUID> itemIds) {
        this.itemIds = itemIds;
    }

    public QueueRequestDto mode(@org.eclipse.jdt.annotation.Nullable GroupQueueMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Enum GroupQueueMode.
     * 
     * @return mode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupQueueMode getMode() {
        return mode;
    }

    @JsonProperty(value = JSON_PROPERTY_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMode(@org.eclipse.jdt.annotation.Nullable GroupQueueMode mode) {
        this.mode = mode;
    }

    /**
     * Return true if this QueueRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueueRequestDto queueRequestDto = (QueueRequestDto) o;
        return Objects.equals(this.itemIds, queueRequestDto.itemIds) && Objects.equals(this.mode, queueRequestDto.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemIds, mode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QueueRequestDto {\n");
        sb.append("    itemIds: ").append(toIndentedString(itemIds)).append("\n");
        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
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

        // add `ItemIds` to the URL query string
        if (getItemIds() != null) {
            for (int i = 0; i < getItemIds().size(); i++) {
                if (getItemIds().get(i) != null) {
                    joiner.add(String.format(java.util.Locale.ROOT, "%sItemIds%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                            containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getItemIds().get(i)))));
                }
            }
        }

        // add `Mode` to the URL query string
        if (getMode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMode()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private QueueRequestDto instance;

        public Builder() {
            this(new QueueRequestDto());
        }

        protected Builder(QueueRequestDto instance) {
            this.instance = instance;
        }

        public QueueRequestDto.Builder itemIds(List<UUID> itemIds) {
            this.instance.itemIds = itemIds;
            return this;
        }

        public QueueRequestDto.Builder mode(GroupQueueMode mode) {
            this.instance.mode = mode;
            return this;
        }

        /**
         * returns a built QueueRequestDto instance.
         *
         * The builder is not reusable.
         */
        public QueueRequestDto build() {
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
    public static QueueRequestDto.Builder builder() {
        return new QueueRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public QueueRequestDto.Builder toBuilder() {
        return new QueueRequestDto.Builder().itemIds(getItemIds()).mode(getMode());
    }
}
