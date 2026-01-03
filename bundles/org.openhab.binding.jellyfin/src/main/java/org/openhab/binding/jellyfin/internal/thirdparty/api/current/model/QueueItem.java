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
 * QueueItem
 */
@JsonPropertyOrder({ QueueItem.JSON_PROPERTY_ID, QueueItem.JSON_PROPERTY_PLAYLIST_ITEM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QueueItem {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private UUID id;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.Nullable
    private String playlistItemId;

    public QueueItem() {
    }

    public QueueItem id(@org.eclipse.jdt.annotation.Nullable UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
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

    public QueueItem playlistItemId(@org.eclipse.jdt.annotation.Nullable String playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Get playlistItemId
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.Nullable String playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    /**
     * Return true if this QueueItem object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueueItem queueItem = (QueueItem) o;
        return Objects.equals(this.id, queueItem.id) && Objects.equals(this.playlistItemId, queueItem.playlistItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, playlistItemId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QueueItem {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    playlistItemId: ").append(toIndentedString(playlistItemId)).append("\n");
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

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private QueueItem instance;

        public Builder() {
            this(new QueueItem());
        }

        protected Builder(QueueItem instance) {
            this.instance = instance;
        }

        public QueueItem.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public QueueItem.Builder playlistItemId(String playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        /**
         * returns a built QueueItem instance.
         *
         * The builder is not reusable.
         */
        public QueueItem build() {
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
    public static QueueItem.Builder builder() {
        return new QueueItem.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public QueueItem.Builder toBuilder() {
        return new QueueItem.Builder().id(getId()).playlistItemId(getPlaylistItemId());
    }
}
