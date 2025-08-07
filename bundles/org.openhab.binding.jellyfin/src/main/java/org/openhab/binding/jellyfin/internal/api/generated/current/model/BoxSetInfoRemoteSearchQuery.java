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

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * BoxSetInfoRemoteSearchQuery
 */
@JsonPropertyOrder({ BoxSetInfoRemoteSearchQuery.JSON_PROPERTY_SEARCH_INFO,
        BoxSetInfoRemoteSearchQuery.JSON_PROPERTY_ITEM_ID,
        BoxSetInfoRemoteSearchQuery.JSON_PROPERTY_SEARCH_PROVIDER_NAME,
        BoxSetInfoRemoteSearchQuery.JSON_PROPERTY_INCLUDE_DISABLED_PROVIDERS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BoxSetInfoRemoteSearchQuery {
    public static final String JSON_PROPERTY_SEARCH_INFO = "SearchInfo";
    @org.eclipse.jdt.annotation.NonNull
    private BoxSetInfo searchInfo;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID itemId;

    public static final String JSON_PROPERTY_SEARCH_PROVIDER_NAME = "SearchProviderName";
    @org.eclipse.jdt.annotation.NonNull
    private String searchProviderName;

    public static final String JSON_PROPERTY_INCLUDE_DISABLED_PROVIDERS = "IncludeDisabledProviders";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean includeDisabledProviders;

    public BoxSetInfoRemoteSearchQuery() {
    }

    public BoxSetInfoRemoteSearchQuery searchInfo(@org.eclipse.jdt.annotation.NonNull BoxSetInfo searchInfo) {
        this.searchInfo = searchInfo;
        return this;
    }

    /**
     * Get searchInfo
     * 
     * @return searchInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SEARCH_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BoxSetInfo getSearchInfo() {
        return searchInfo;
    }

    @JsonProperty(JSON_PROPERTY_SEARCH_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSearchInfo(@org.eclipse.jdt.annotation.NonNull BoxSetInfo searchInfo) {
        this.searchInfo = searchInfo;
    }

    public BoxSetInfoRemoteSearchQuery itemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Get itemId
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
    }

    public BoxSetInfoRemoteSearchQuery searchProviderName(
            @org.eclipse.jdt.annotation.NonNull String searchProviderName) {
        this.searchProviderName = searchProviderName;
        return this;
    }

    /**
     * Gets or sets the provider name to search within if set.
     * 
     * @return searchProviderName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SEARCH_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSearchProviderName() {
        return searchProviderName;
    }

    @JsonProperty(JSON_PROPERTY_SEARCH_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSearchProviderName(@org.eclipse.jdt.annotation.NonNull String searchProviderName) {
        this.searchProviderName = searchProviderName;
    }

    public BoxSetInfoRemoteSearchQuery includeDisabledProviders(
            @org.eclipse.jdt.annotation.NonNull Boolean includeDisabledProviders) {
        this.includeDisabledProviders = includeDisabledProviders;
        return this;
    }

    /**
     * Gets or sets a value indicating whether disabled providers should be included.
     * 
     * @return includeDisabledProviders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_INCLUDE_DISABLED_PROVIDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIncludeDisabledProviders() {
        return includeDisabledProviders;
    }

    @JsonProperty(JSON_PROPERTY_INCLUDE_DISABLED_PROVIDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIncludeDisabledProviders(@org.eclipse.jdt.annotation.NonNull Boolean includeDisabledProviders) {
        this.includeDisabledProviders = includeDisabledProviders;
    }

    /**
     * Return true if this BoxSetInfoRemoteSearchQuery object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery = (BoxSetInfoRemoteSearchQuery) o;
        return Objects.equals(this.searchInfo, boxSetInfoRemoteSearchQuery.searchInfo)
                && Objects.equals(this.itemId, boxSetInfoRemoteSearchQuery.itemId)
                && Objects.equals(this.searchProviderName, boxSetInfoRemoteSearchQuery.searchProviderName)
                && Objects.equals(this.includeDisabledProviders, boxSetInfoRemoteSearchQuery.includeDisabledProviders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchInfo, itemId, searchProviderName, includeDisabledProviders);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BoxSetInfoRemoteSearchQuery {\n");
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
}
