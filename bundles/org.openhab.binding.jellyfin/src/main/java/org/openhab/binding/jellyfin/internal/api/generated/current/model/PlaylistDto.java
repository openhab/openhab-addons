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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * DTO for playlists.
 */
@JsonPropertyOrder({ PlaylistDto.JSON_PROPERTY_OPEN_ACCESS, PlaylistDto.JSON_PROPERTY_SHARES,
        PlaylistDto.JSON_PROPERTY_ITEM_IDS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaylistDto {
    public static final String JSON_PROPERTY_OPEN_ACCESS = "OpenAccess";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean openAccess;

    public static final String JSON_PROPERTY_SHARES = "Shares";
    @org.eclipse.jdt.annotation.NonNull
    private List<PlaylistUserPermissions> shares = new ArrayList<>();

    public static final String JSON_PROPERTY_ITEM_IDS = "ItemIds";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> itemIds = new ArrayList<>();

    public PlaylistDto() {
    }

    public PlaylistDto openAccess(@org.eclipse.jdt.annotation.NonNull Boolean openAccess) {
        this.openAccess = openAccess;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the playlist is publicly readable.
     * 
     * @return openAccess
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OPEN_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getOpenAccess() {
        return openAccess;
    }

    @JsonProperty(value = JSON_PROPERTY_OPEN_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOpenAccess(@org.eclipse.jdt.annotation.NonNull Boolean openAccess) {
        this.openAccess = openAccess;
    }

    public PlaylistDto shares(@org.eclipse.jdt.annotation.NonNull List<PlaylistUserPermissions> shares) {
        this.shares = shares;
        return this;
    }

    public PlaylistDto addSharesItem(PlaylistUserPermissions sharesItem) {
        if (this.shares == null) {
            this.shares = new ArrayList<>();
        }
        this.shares.add(sharesItem);
        return this;
    }

    /**
     * Gets or sets the share permissions.
     * 
     * @return shares
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SHARES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<PlaylistUserPermissions> getShares() {
        return shares;
    }

    @JsonProperty(value = JSON_PROPERTY_SHARES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setShares(@org.eclipse.jdt.annotation.NonNull List<PlaylistUserPermissions> shares) {
        this.shares = shares;
    }

    public PlaylistDto itemIds(@org.eclipse.jdt.annotation.NonNull List<UUID> itemIds) {
        this.itemIds = itemIds;
        return this;
    }

    public PlaylistDto addItemIdsItem(UUID itemIdsItem) {
        if (this.itemIds == null) {
            this.itemIds = new ArrayList<>();
        }
        this.itemIds.add(itemIdsItem);
        return this;
    }

    /**
     * Gets or sets the item ids.
     * 
     * @return itemIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEM_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getItemIds() {
        return itemIds;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemIds(@org.eclipse.jdt.annotation.NonNull List<UUID> itemIds) {
        this.itemIds = itemIds;
    }

    /**
     * Return true if this PlaylistDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaylistDto playlistDto = (PlaylistDto) o;
        return Objects.equals(this.openAccess, playlistDto.openAccess)
                && Objects.equals(this.shares, playlistDto.shares) && Objects.equals(this.itemIds, playlistDto.itemIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(openAccess, shares, itemIds);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlaylistDto {\n");
        sb.append("    openAccess: ").append(toIndentedString(openAccess)).append("\n");
        sb.append("    shares: ").append(toIndentedString(shares)).append("\n");
        sb.append("    itemIds: ").append(toIndentedString(itemIds)).append("\n");
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

        // add `OpenAccess` to the URL query string
        if (getOpenAccess() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOpenAccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOpenAccess()))));
        }

        // add `Shares` to the URL query string
        if (getShares() != null) {
            for (int i = 0; i < getShares().size(); i++) {
                if (getShares().get(i) != null) {
                    joiner.add(getShares().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sShares%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `ItemIds` to the URL query string
        if (getItemIds() != null) {
            for (int i = 0; i < getItemIds().size(); i++) {
                if (getItemIds().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sItemIds%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getItemIds().get(i)))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlaylistDto instance;

        public Builder() {
            this(new PlaylistDto());
        }

        protected Builder(PlaylistDto instance) {
            this.instance = instance;
        }

        public PlaylistDto.Builder openAccess(Boolean openAccess) {
            this.instance.openAccess = openAccess;
            return this;
        }

        public PlaylistDto.Builder shares(List<PlaylistUserPermissions> shares) {
            this.instance.shares = shares;
            return this;
        }

        public PlaylistDto.Builder itemIds(List<UUID> itemIds) {
            this.instance.itemIds = itemIds;
            return this;
        }

        /**
         * returns a built PlaylistDto instance.
         *
         * The builder is not reusable.
         */
        public PlaylistDto build() {
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
    public static PlaylistDto.Builder builder() {
        return new PlaylistDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlaylistDto.Builder toBuilder() {
        return new PlaylistDto.Builder().openAccess(getOpenAccess()).shares(getShares()).itemIds(getItemIds());
    }
}
