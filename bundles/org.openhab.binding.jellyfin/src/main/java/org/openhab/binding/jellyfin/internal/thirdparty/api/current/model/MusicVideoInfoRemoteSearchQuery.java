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
 * MusicVideoInfoRemoteSearchQuery
 */
@JsonPropertyOrder({ MusicVideoInfoRemoteSearchQuery.JSON_PROPERTY_SEARCH_INFO,
        MusicVideoInfoRemoteSearchQuery.JSON_PROPERTY_ITEM_ID,
        MusicVideoInfoRemoteSearchQuery.JSON_PROPERTY_SEARCH_PROVIDER_NAME,
        MusicVideoInfoRemoteSearchQuery.JSON_PROPERTY_INCLUDE_DISABLED_PROVIDERS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MusicVideoInfoRemoteSearchQuery {
    public static final String JSON_PROPERTY_SEARCH_INFO = "SearchInfo";
    @org.eclipse.jdt.annotation.Nullable
    private MusicVideoInfo searchInfo;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID itemId;

    public static final String JSON_PROPERTY_SEARCH_PROVIDER_NAME = "SearchProviderName";
    @org.eclipse.jdt.annotation.Nullable
    private String searchProviderName;

    public static final String JSON_PROPERTY_INCLUDE_DISABLED_PROVIDERS = "IncludeDisabledProviders";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean includeDisabledProviders;

    public MusicVideoInfoRemoteSearchQuery() {
    }

    public MusicVideoInfoRemoteSearchQuery searchInfo(@org.eclipse.jdt.annotation.Nullable MusicVideoInfo searchInfo) {
        this.searchInfo = searchInfo;
        return this;
    }

    /**
     * Get searchInfo
     * 
     * @return searchInfo
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SEARCH_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MusicVideoInfo getSearchInfo() {
        return searchInfo;
    }

    @JsonProperty(value = JSON_PROPERTY_SEARCH_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSearchInfo(@org.eclipse.jdt.annotation.Nullable MusicVideoInfo searchInfo) {
        this.searchInfo = searchInfo;
    }

    public MusicVideoInfoRemoteSearchQuery itemId(@org.eclipse.jdt.annotation.Nullable UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Get itemId
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

    public MusicVideoInfoRemoteSearchQuery searchProviderName(
            @org.eclipse.jdt.annotation.Nullable String searchProviderName) {
        this.searchProviderName = searchProviderName;
        return this;
    }

    /**
     * Gets or sets the provider name to search within if set.
     * 
     * @return searchProviderName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SEARCH_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSearchProviderName() {
        return searchProviderName;
    }

    @JsonProperty(value = JSON_PROPERTY_SEARCH_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSearchProviderName(@org.eclipse.jdt.annotation.Nullable String searchProviderName) {
        this.searchProviderName = searchProviderName;
    }

    public MusicVideoInfoRemoteSearchQuery includeDisabledProviders(
            @org.eclipse.jdt.annotation.Nullable Boolean includeDisabledProviders) {
        this.includeDisabledProviders = includeDisabledProviders;
        return this;
    }

    /**
     * Gets or sets a value indicating whether disabled providers should be included.
     * 
     * @return includeDisabledProviders
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_INCLUDE_DISABLED_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIncludeDisabledProviders() {
        return includeDisabledProviders;
    }

    @JsonProperty(value = JSON_PROPERTY_INCLUDE_DISABLED_PROVIDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIncludeDisabledProviders(@org.eclipse.jdt.annotation.Nullable Boolean includeDisabledProviders) {
        this.includeDisabledProviders = includeDisabledProviders;
    }

    /**
     * Return true if this MusicVideoInfoRemoteSearchQuery object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery = (MusicVideoInfoRemoteSearchQuery) o;
        return Objects.equals(this.searchInfo, musicVideoInfoRemoteSearchQuery.searchInfo)
                && Objects.equals(this.itemId, musicVideoInfoRemoteSearchQuery.itemId)
                && Objects.equals(this.searchProviderName, musicVideoInfoRemoteSearchQuery.searchProviderName)
                && Objects.equals(this.includeDisabledProviders,
                        musicVideoInfoRemoteSearchQuery.includeDisabledProviders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchInfo, itemId, searchProviderName, includeDisabledProviders);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MusicVideoInfoRemoteSearchQuery {\n");
        sb.append("    searchInfo: ").append(toIndentedString(searchInfo)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    searchProviderName: ").append(toIndentedString(searchProviderName)).append("\n");
        sb.append("    includeDisabledProviders: ").append(toIndentedString(includeDisabledProviders)).append("\n");
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

        // add `SearchInfo` to the URL query string
        if (getSearchInfo() != null) {
            joiner.add(getSearchInfo().toUrlQueryString(prefix + "SearchInfo" + suffix));
        }

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
        }

        // add `SearchProviderName` to the URL query string
        if (getSearchProviderName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSearchProviderName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSearchProviderName()))));
        }

        // add `IncludeDisabledProviders` to the URL query string
        if (getIncludeDisabledProviders() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIncludeDisabledProviders%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIncludeDisabledProviders()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MusicVideoInfoRemoteSearchQuery instance;

        public Builder() {
            this(new MusicVideoInfoRemoteSearchQuery());
        }

        protected Builder(MusicVideoInfoRemoteSearchQuery instance) {
            this.instance = instance;
        }

        public MusicVideoInfoRemoteSearchQuery.Builder searchInfo(MusicVideoInfo searchInfo) {
            this.instance.searchInfo = searchInfo;
            return this;
        }

        public MusicVideoInfoRemoteSearchQuery.Builder itemId(UUID itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        public MusicVideoInfoRemoteSearchQuery.Builder searchProviderName(String searchProviderName) {
            this.instance.searchProviderName = searchProviderName;
            return this;
        }

        public MusicVideoInfoRemoteSearchQuery.Builder includeDisabledProviders(Boolean includeDisabledProviders) {
            this.instance.includeDisabledProviders = includeDisabledProviders;
            return this;
        }

        /**
         * returns a built MusicVideoInfoRemoteSearchQuery instance.
         *
         * The builder is not reusable.
         */
        public MusicVideoInfoRemoteSearchQuery build() {
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
    public static MusicVideoInfoRemoteSearchQuery.Builder builder() {
        return new MusicVideoInfoRemoteSearchQuery.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MusicVideoInfoRemoteSearchQuery.Builder toBuilder() {
        return new MusicVideoInfoRemoteSearchQuery.Builder().searchInfo(getSearchInfo()).itemId(getItemId())
                .searchProviderName(getSearchProviderName()).includeDisabledProviders(getIncludeDisabledProviders());
    }
}
