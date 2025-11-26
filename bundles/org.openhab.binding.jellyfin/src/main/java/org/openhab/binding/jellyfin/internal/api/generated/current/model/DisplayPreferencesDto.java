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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the display preferences for any item that supports them (usually Folders).
 */
@JsonPropertyOrder({ DisplayPreferencesDto.JSON_PROPERTY_ID, DisplayPreferencesDto.JSON_PROPERTY_VIEW_TYPE,
        DisplayPreferencesDto.JSON_PROPERTY_SORT_BY, DisplayPreferencesDto.JSON_PROPERTY_INDEX_BY,
        DisplayPreferencesDto.JSON_PROPERTY_REMEMBER_INDEXING, DisplayPreferencesDto.JSON_PROPERTY_PRIMARY_IMAGE_HEIGHT,
        DisplayPreferencesDto.JSON_PROPERTY_PRIMARY_IMAGE_WIDTH, DisplayPreferencesDto.JSON_PROPERTY_CUSTOM_PREFS,
        DisplayPreferencesDto.JSON_PROPERTY_SCROLL_DIRECTION, DisplayPreferencesDto.JSON_PROPERTY_SHOW_BACKDROP,
        DisplayPreferencesDto.JSON_PROPERTY_REMEMBER_SORTING, DisplayPreferencesDto.JSON_PROPERTY_SORT_ORDER,
        DisplayPreferencesDto.JSON_PROPERTY_SHOW_SIDEBAR, DisplayPreferencesDto.JSON_PROPERTY_CLIENT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DisplayPreferencesDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_VIEW_TYPE = "ViewType";
    @org.eclipse.jdt.annotation.NonNull
    private String viewType;

    public static final String JSON_PROPERTY_SORT_BY = "SortBy";
    @org.eclipse.jdt.annotation.NonNull
    private String sortBy;

    public static final String JSON_PROPERTY_INDEX_BY = "IndexBy";
    @org.eclipse.jdt.annotation.NonNull
    private String indexBy;

    public static final String JSON_PROPERTY_REMEMBER_INDEXING = "RememberIndexing";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean rememberIndexing;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_HEIGHT = "PrimaryImageHeight";
    @org.eclipse.jdt.annotation.NonNull
    private Integer primaryImageHeight;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_WIDTH = "PrimaryImageWidth";
    @org.eclipse.jdt.annotation.NonNull
    private Integer primaryImageWidth;

    public static final String JSON_PROPERTY_CUSTOM_PREFS = "CustomPrefs";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> customPrefs = new HashMap<>();

    public static final String JSON_PROPERTY_SCROLL_DIRECTION = "ScrollDirection";
    @org.eclipse.jdt.annotation.NonNull
    private ScrollDirection scrollDirection;

    public static final String JSON_PROPERTY_SHOW_BACKDROP = "ShowBackdrop";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean showBackdrop;

    public static final String JSON_PROPERTY_REMEMBER_SORTING = "RememberSorting";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean rememberSorting;

    public static final String JSON_PROPERTY_SORT_ORDER = "SortOrder";
    @org.eclipse.jdt.annotation.NonNull
    private SortOrder sortOrder;

    public static final String JSON_PROPERTY_SHOW_SIDEBAR = "ShowSidebar";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean showSidebar;

    public static final String JSON_PROPERTY_CLIENT = "Client";
    @org.eclipse.jdt.annotation.NonNull
    private String client;

    public DisplayPreferencesDto() {
    }

    public DisplayPreferencesDto id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the user id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public DisplayPreferencesDto viewType(@org.eclipse.jdt.annotation.NonNull String viewType) {
        this.viewType = viewType;
        return this;
    }

    /**
     * Gets or sets the type of the view.
     * 
     * @return viewType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VIEW_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getViewType() {
        return viewType;
    }

    @JsonProperty(value = JSON_PROPERTY_VIEW_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setViewType(@org.eclipse.jdt.annotation.NonNull String viewType) {
        this.viewType = viewType;
    }

    public DisplayPreferencesDto sortBy(@org.eclipse.jdt.annotation.NonNull String sortBy) {
        this.sortBy = sortBy;
        return this;
    }

    /**
     * Gets or sets the sort by.
     * 
     * @return sortBy
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SORT_BY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSortBy() {
        return sortBy;
    }

    @JsonProperty(value = JSON_PROPERTY_SORT_BY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSortBy(@org.eclipse.jdt.annotation.NonNull String sortBy) {
        this.sortBy = sortBy;
    }

    public DisplayPreferencesDto indexBy(@org.eclipse.jdt.annotation.NonNull String indexBy) {
        this.indexBy = indexBy;
        return this;
    }

    /**
     * Gets or sets the index by.
     * 
     * @return indexBy
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_INDEX_BY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getIndexBy() {
        return indexBy;
    }

    @JsonProperty(value = JSON_PROPERTY_INDEX_BY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexBy(@org.eclipse.jdt.annotation.NonNull String indexBy) {
        this.indexBy = indexBy;
    }

    public DisplayPreferencesDto rememberIndexing(@org.eclipse.jdt.annotation.NonNull Boolean rememberIndexing) {
        this.rememberIndexing = rememberIndexing;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [remember indexing].
     * 
     * @return rememberIndexing
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_REMEMBER_INDEXING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRememberIndexing() {
        return rememberIndexing;
    }

    @JsonProperty(value = JSON_PROPERTY_REMEMBER_INDEXING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRememberIndexing(@org.eclipse.jdt.annotation.NonNull Boolean rememberIndexing) {
        this.rememberIndexing = rememberIndexing;
    }

    public DisplayPreferencesDto primaryImageHeight(@org.eclipse.jdt.annotation.NonNull Integer primaryImageHeight) {
        this.primaryImageHeight = primaryImageHeight;
        return this;
    }

    /**
     * Gets or sets the height of the primary image.
     * 
     * @return primaryImageHeight
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPrimaryImageHeight() {
        return primaryImageHeight;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageHeight(@org.eclipse.jdt.annotation.NonNull Integer primaryImageHeight) {
        this.primaryImageHeight = primaryImageHeight;
    }

    public DisplayPreferencesDto primaryImageWidth(@org.eclipse.jdt.annotation.NonNull Integer primaryImageWidth) {
        this.primaryImageWidth = primaryImageWidth;
        return this;
    }

    /**
     * Gets or sets the width of the primary image.
     * 
     * @return primaryImageWidth
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPrimaryImageWidth() {
        return primaryImageWidth;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageWidth(@org.eclipse.jdt.annotation.NonNull Integer primaryImageWidth) {
        this.primaryImageWidth = primaryImageWidth;
    }

    public DisplayPreferencesDto customPrefs(@org.eclipse.jdt.annotation.NonNull Map<String, String> customPrefs) {
        this.customPrefs = customPrefs;
        return this;
    }

    public DisplayPreferencesDto putCustomPrefsItem(String key, String customPrefsItem) {
        if (this.customPrefs == null) {
            this.customPrefs = new HashMap<>();
        }
        this.customPrefs.put(key, customPrefsItem);
        return this;
    }

    /**
     * Gets or sets the custom prefs.
     * 
     * @return customPrefs
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CUSTOM_PREFS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getCustomPrefs() {
        return customPrefs;
    }

    @JsonProperty(value = JSON_PROPERTY_CUSTOM_PREFS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomPrefs(@org.eclipse.jdt.annotation.NonNull Map<String, String> customPrefs) {
        this.customPrefs = customPrefs;
    }

    public DisplayPreferencesDto scrollDirection(@org.eclipse.jdt.annotation.NonNull ScrollDirection scrollDirection) {
        this.scrollDirection = scrollDirection;
        return this;
    }

    /**
     * Gets or sets the scroll direction.
     * 
     * @return scrollDirection
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SCROLL_DIRECTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ScrollDirection getScrollDirection() {
        return scrollDirection;
    }

    @JsonProperty(value = JSON_PROPERTY_SCROLL_DIRECTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScrollDirection(@org.eclipse.jdt.annotation.NonNull ScrollDirection scrollDirection) {
        this.scrollDirection = scrollDirection;
    }

    public DisplayPreferencesDto showBackdrop(@org.eclipse.jdt.annotation.NonNull Boolean showBackdrop) {
        this.showBackdrop = showBackdrop;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to show backdrops on this item.
     * 
     * @return showBackdrop
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SHOW_BACKDROP, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getShowBackdrop() {
        return showBackdrop;
    }

    @JsonProperty(value = JSON_PROPERTY_SHOW_BACKDROP, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setShowBackdrop(@org.eclipse.jdt.annotation.NonNull Boolean showBackdrop) {
        this.showBackdrop = showBackdrop;
    }

    public DisplayPreferencesDto rememberSorting(@org.eclipse.jdt.annotation.NonNull Boolean rememberSorting) {
        this.rememberSorting = rememberSorting;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [remember sorting].
     * 
     * @return rememberSorting
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_REMEMBER_SORTING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRememberSorting() {
        return rememberSorting;
    }

    @JsonProperty(value = JSON_PROPERTY_REMEMBER_SORTING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRememberSorting(@org.eclipse.jdt.annotation.NonNull Boolean rememberSorting) {
        this.rememberSorting = rememberSorting;
    }

    public DisplayPreferencesDto sortOrder(@org.eclipse.jdt.annotation.NonNull SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    /**
     * Gets or sets the sort order.
     * 
     * @return sortOrder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SORT_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @JsonProperty(value = JSON_PROPERTY_SORT_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSortOrder(@org.eclipse.jdt.annotation.NonNull SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public DisplayPreferencesDto showSidebar(@org.eclipse.jdt.annotation.NonNull Boolean showSidebar) {
        this.showSidebar = showSidebar;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [show sidebar].
     * 
     * @return showSidebar
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SHOW_SIDEBAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getShowSidebar() {
        return showSidebar;
    }

    @JsonProperty(value = JSON_PROPERTY_SHOW_SIDEBAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setShowSidebar(@org.eclipse.jdt.annotation.NonNull Boolean showSidebar) {
        this.showSidebar = showSidebar;
    }

    public DisplayPreferencesDto client(@org.eclipse.jdt.annotation.NonNull String client) {
        this.client = client;
        return this;
    }

    /**
     * Gets or sets the client.
     * 
     * @return client
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CLIENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getClient() {
        return client;
    }

    @JsonProperty(value = JSON_PROPERTY_CLIENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setClient(@org.eclipse.jdt.annotation.NonNull String client) {
        this.client = client;
    }

    /**
     * Return true if this DisplayPreferencesDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DisplayPreferencesDto displayPreferencesDto = (DisplayPreferencesDto) o;
        return Objects.equals(this.id, displayPreferencesDto.id)
                && Objects.equals(this.viewType, displayPreferencesDto.viewType)
                && Objects.equals(this.sortBy, displayPreferencesDto.sortBy)
                && Objects.equals(this.indexBy, displayPreferencesDto.indexBy)
                && Objects.equals(this.rememberIndexing, displayPreferencesDto.rememberIndexing)
                && Objects.equals(this.primaryImageHeight, displayPreferencesDto.primaryImageHeight)
                && Objects.equals(this.primaryImageWidth, displayPreferencesDto.primaryImageWidth)
                && Objects.equals(this.customPrefs, displayPreferencesDto.customPrefs)
                && Objects.equals(this.scrollDirection, displayPreferencesDto.scrollDirection)
                && Objects.equals(this.showBackdrop, displayPreferencesDto.showBackdrop)
                && Objects.equals(this.rememberSorting, displayPreferencesDto.rememberSorting)
                && Objects.equals(this.sortOrder, displayPreferencesDto.sortOrder)
                && Objects.equals(this.showSidebar, displayPreferencesDto.showSidebar)
                && Objects.equals(this.client, displayPreferencesDto.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, viewType, sortBy, indexBy, rememberIndexing, primaryImageHeight, primaryImageWidth,
                customPrefs, scrollDirection, showBackdrop, rememberSorting, sortOrder, showSidebar, client);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DisplayPreferencesDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    viewType: ").append(toIndentedString(viewType)).append("\n");
        sb.append("    sortBy: ").append(toIndentedString(sortBy)).append("\n");
        sb.append("    indexBy: ").append(toIndentedString(indexBy)).append("\n");
        sb.append("    rememberIndexing: ").append(toIndentedString(rememberIndexing)).append("\n");
        sb.append("    primaryImageHeight: ").append(toIndentedString(primaryImageHeight)).append("\n");
        sb.append("    primaryImageWidth: ").append(toIndentedString(primaryImageWidth)).append("\n");
        sb.append("    customPrefs: ").append(toIndentedString(customPrefs)).append("\n");
        sb.append("    scrollDirection: ").append(toIndentedString(scrollDirection)).append("\n");
        sb.append("    showBackdrop: ").append(toIndentedString(showBackdrop)).append("\n");
        sb.append("    rememberSorting: ").append(toIndentedString(rememberSorting)).append("\n");
        sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
        sb.append("    showSidebar: ").append(toIndentedString(showSidebar)).append("\n");
        sb.append("    client: ").append(toIndentedString(client)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `ViewType` to the URL query string
        if (getViewType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sViewType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getViewType()))));
        }

        // add `SortBy` to the URL query string
        if (getSortBy() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSortBy%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSortBy()))));
        }

        // add `IndexBy` to the URL query string
        if (getIndexBy() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIndexBy%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndexBy()))));
        }

        // add `RememberIndexing` to the URL query string
        if (getRememberIndexing() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRememberIndexing%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRememberIndexing()))));
        }

        // add `PrimaryImageHeight` to the URL query string
        if (getPrimaryImageHeight() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPrimaryImageHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageHeight()))));
        }

        // add `PrimaryImageWidth` to the URL query string
        if (getPrimaryImageWidth() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPrimaryImageWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageWidth()))));
        }

        // add `CustomPrefs` to the URL query string
        if (getCustomPrefs() != null) {
            for (String _key : getCustomPrefs().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sCustomPrefs%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getCustomPrefs().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getCustomPrefs().get(_key)))));
            }
        }

        // add `ScrollDirection` to the URL query string
        if (getScrollDirection() != null) {
            joiner.add(String.format(Locale.ROOT, "%sScrollDirection%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getScrollDirection()))));
        }

        // add `ShowBackdrop` to the URL query string
        if (getShowBackdrop() != null) {
            joiner.add(String.format(Locale.ROOT, "%sShowBackdrop%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getShowBackdrop()))));
        }

        // add `RememberSorting` to the URL query string
        if (getRememberSorting() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRememberSorting%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRememberSorting()))));
        }

        // add `SortOrder` to the URL query string
        if (getSortOrder() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSortOrder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSortOrder()))));
        }

        // add `ShowSidebar` to the URL query string
        if (getShowSidebar() != null) {
            joiner.add(String.format(Locale.ROOT, "%sShowSidebar%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getShowSidebar()))));
        }

        // add `Client` to the URL query string
        if (getClient() != null) {
            joiner.add(String.format(Locale.ROOT, "%sClient%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getClient()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private DisplayPreferencesDto instance;

        public Builder() {
            this(new DisplayPreferencesDto());
        }

        protected Builder(DisplayPreferencesDto instance) {
            this.instance = instance;
        }

        public DisplayPreferencesDto.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public DisplayPreferencesDto.Builder viewType(String viewType) {
            this.instance.viewType = viewType;
            return this;
        }

        public DisplayPreferencesDto.Builder sortBy(String sortBy) {
            this.instance.sortBy = sortBy;
            return this;
        }

        public DisplayPreferencesDto.Builder indexBy(String indexBy) {
            this.instance.indexBy = indexBy;
            return this;
        }

        public DisplayPreferencesDto.Builder rememberIndexing(Boolean rememberIndexing) {
            this.instance.rememberIndexing = rememberIndexing;
            return this;
        }

        public DisplayPreferencesDto.Builder primaryImageHeight(Integer primaryImageHeight) {
            this.instance.primaryImageHeight = primaryImageHeight;
            return this;
        }

        public DisplayPreferencesDto.Builder primaryImageWidth(Integer primaryImageWidth) {
            this.instance.primaryImageWidth = primaryImageWidth;
            return this;
        }

        public DisplayPreferencesDto.Builder customPrefs(Map<String, String> customPrefs) {
            this.instance.customPrefs = customPrefs;
            return this;
        }

        public DisplayPreferencesDto.Builder scrollDirection(ScrollDirection scrollDirection) {
            this.instance.scrollDirection = scrollDirection;
            return this;
        }

        public DisplayPreferencesDto.Builder showBackdrop(Boolean showBackdrop) {
            this.instance.showBackdrop = showBackdrop;
            return this;
        }

        public DisplayPreferencesDto.Builder rememberSorting(Boolean rememberSorting) {
            this.instance.rememberSorting = rememberSorting;
            return this;
        }

        public DisplayPreferencesDto.Builder sortOrder(SortOrder sortOrder) {
            this.instance.sortOrder = sortOrder;
            return this;
        }

        public DisplayPreferencesDto.Builder showSidebar(Boolean showSidebar) {
            this.instance.showSidebar = showSidebar;
            return this;
        }

        public DisplayPreferencesDto.Builder client(String client) {
            this.instance.client = client;
            return this;
        }

        /**
         * returns a built DisplayPreferencesDto instance.
         *
         * The builder is not reusable.
         */
        public DisplayPreferencesDto build() {
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
    public static DisplayPreferencesDto.Builder builder() {
        return new DisplayPreferencesDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DisplayPreferencesDto.Builder toBuilder() {
        return new DisplayPreferencesDto.Builder().id(getId()).viewType(getViewType()).sortBy(getSortBy())
                .indexBy(getIndexBy()).rememberIndexing(getRememberIndexing())
                .primaryImageHeight(getPrimaryImageHeight()).primaryImageWidth(getPrimaryImageWidth())
                .customPrefs(getCustomPrefs()).scrollDirection(getScrollDirection()).showBackdrop(getShowBackdrop())
                .rememberSorting(getRememberSorting()).sortOrder(getSortOrder()).showSidebar(getShowSidebar())
                .client(getClient());
    }
}
