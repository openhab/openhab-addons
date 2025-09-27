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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class BaseItem.
 */
@JsonPropertyOrder({ BaseItem.JSON_PROPERTY_SIZE, BaseItem.JSON_PROPERTY_CONTAINER, BaseItem.JSON_PROPERTY_IS_H_D,
        BaseItem.JSON_PROPERTY_IS_SHORTCUT, BaseItem.JSON_PROPERTY_SHORTCUT_PATH, BaseItem.JSON_PROPERTY_WIDTH,
        BaseItem.JSON_PROPERTY_HEIGHT, BaseItem.JSON_PROPERTY_EXTRA_IDS, BaseItem.JSON_PROPERTY_DATE_LAST_SAVED,
        BaseItem.JSON_PROPERTY_REMOTE_TRAILERS, BaseItem.JSON_PROPERTY_SUPPORTS_EXTERNAL_TRANSFER })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BaseItem {
    public static final String JSON_PROPERTY_SIZE = "Size";
    @org.eclipse.jdt.annotation.NonNull
    private Long size;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_IS_H_D = "IsHD";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isHD;

    public static final String JSON_PROPERTY_IS_SHORTCUT = "IsShortcut";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isShortcut;

    public static final String JSON_PROPERTY_SHORTCUT_PATH = "ShortcutPath";
    @org.eclipse.jdt.annotation.NonNull
    private String shortcutPath;

    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.NonNull
    private Integer width;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.NonNull
    private Integer height;

    public static final String JSON_PROPERTY_EXTRA_IDS = "ExtraIds";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> extraIds;

    public static final String JSON_PROPERTY_DATE_LAST_SAVED = "DateLastSaved";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateLastSaved;

    public static final String JSON_PROPERTY_REMOTE_TRAILERS = "RemoteTrailers";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaUrl> remoteTrailers;

    public static final String JSON_PROPERTY_SUPPORTS_EXTERNAL_TRANSFER = "SupportsExternalTransfer";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsExternalTransfer;

    public BaseItem() {
    }

    @JsonCreator
    public BaseItem(@JsonProperty(JSON_PROPERTY_IS_H_D) Boolean isHD,
            @JsonProperty(JSON_PROPERTY_SUPPORTS_EXTERNAL_TRANSFER) Boolean supportsExternalTransfer) {
        this();
        this.isHD = isHD;
        this.supportsExternalTransfer = supportsExternalTransfer;
    }

    public BaseItem size(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
        return this;
    }

    /**
     * Get size
     * 
     * @return size
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getSize() {
        return size;
    }

    @JsonProperty(JSON_PROPERTY_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSize(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
    }

    public BaseItem container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Get container
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContainer() {
        return container;
    }

    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
    }

    /**
     * Get isHD
     * 
     * @return isHD
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_H_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsHD() {
        return isHD;
    }

    public BaseItem isShortcut(@org.eclipse.jdt.annotation.NonNull Boolean isShortcut) {
        this.isShortcut = isShortcut;
        return this;
    }

    /**
     * Get isShortcut
     * 
     * @return isShortcut
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_SHORTCUT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsShortcut() {
        return isShortcut;
    }

    @JsonProperty(JSON_PROPERTY_IS_SHORTCUT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsShortcut(@org.eclipse.jdt.annotation.NonNull Boolean isShortcut) {
        this.isShortcut = isShortcut;
    }

    public BaseItem shortcutPath(@org.eclipse.jdt.annotation.NonNull String shortcutPath) {
        this.shortcutPath = shortcutPath;
        return this;
    }

    /**
     * Get shortcutPath
     * 
     * @return shortcutPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SHORTCUT_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getShortcutPath() {
        return shortcutPath;
    }

    @JsonProperty(JSON_PROPERTY_SHORTCUT_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setShortcutPath(@org.eclipse.jdt.annotation.NonNull String shortcutPath) {
        this.shortcutPath = shortcutPath;
    }

    public BaseItem width(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Get width
     * 
     * @return width
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getWidth() {
        return width;
    }

    @JsonProperty(JSON_PROPERTY_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWidth(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
    }

    public BaseItem height(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Get height
     * 
     * @return height
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getHeight() {
        return height;
    }

    @JsonProperty(JSON_PROPERTY_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeight(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
    }

    public BaseItem extraIds(@org.eclipse.jdt.annotation.NonNull List<UUID> extraIds) {
        this.extraIds = extraIds;
        return this;
    }

    public BaseItem addExtraIdsItem(UUID extraIdsItem) {
        if (this.extraIds == null) {
            this.extraIds = new ArrayList<>();
        }
        this.extraIds.add(extraIdsItem);
        return this;
    }

    /**
     * Get extraIds
     * 
     * @return extraIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_EXTRA_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getExtraIds() {
        return extraIds;
    }

    @JsonProperty(JSON_PROPERTY_EXTRA_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExtraIds(@org.eclipse.jdt.annotation.NonNull List<UUID> extraIds) {
        this.extraIds = extraIds;
    }

    public BaseItem dateLastSaved(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateLastSaved) {
        this.dateLastSaved = dateLastSaved;
        return this;
    }

    /**
     * Get dateLastSaved
     * 
     * @return dateLastSaved
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DATE_LAST_SAVED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateLastSaved() {
        return dateLastSaved;
    }

    @JsonProperty(JSON_PROPERTY_DATE_LAST_SAVED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateLastSaved(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateLastSaved) {
        this.dateLastSaved = dateLastSaved;
    }

    public BaseItem remoteTrailers(@org.eclipse.jdt.annotation.NonNull List<MediaUrl> remoteTrailers) {
        this.remoteTrailers = remoteTrailers;
        return this;
    }

    public BaseItem addRemoteTrailersItem(MediaUrl remoteTrailersItem) {
        if (this.remoteTrailers == null) {
            this.remoteTrailers = new ArrayList<>();
        }
        this.remoteTrailers.add(remoteTrailersItem);
        return this;
    }

    /**
     * Gets or sets the remote trailers.
     * 
     * @return remoteTrailers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REMOTE_TRAILERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaUrl> getRemoteTrailers() {
        return remoteTrailers;
    }

    @JsonProperty(JSON_PROPERTY_REMOTE_TRAILERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoteTrailers(@org.eclipse.jdt.annotation.NonNull List<MediaUrl> remoteTrailers) {
        this.remoteTrailers = remoteTrailers;
    }

    /**
     * Get supportsExternalTransfer
     * 
     * @return supportsExternalTransfer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_EXTERNAL_TRANSFER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsExternalTransfer() {
        return supportsExternalTransfer;
    }

    /**
     * Return true if this BaseItem object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseItem baseItem = (BaseItem) o;
        return Objects.equals(this.size, baseItem.size) && Objects.equals(this.container, baseItem.container)
                && Objects.equals(this.isHD, baseItem.isHD) && Objects.equals(this.isShortcut, baseItem.isShortcut)
                && Objects.equals(this.shortcutPath, baseItem.shortcutPath)
                && Objects.equals(this.width, baseItem.width) && Objects.equals(this.height, baseItem.height)
                && Objects.equals(this.extraIds, baseItem.extraIds)
                && Objects.equals(this.dateLastSaved, baseItem.dateLastSaved)
                && Objects.equals(this.remoteTrailers, baseItem.remoteTrailers)
                && Objects.equals(this.supportsExternalTransfer, baseItem.supportsExternalTransfer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, container, isHD, isShortcut, shortcutPath, width, height, extraIds, dateLastSaved,
                remoteTrailers, supportsExternalTransfer);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BaseItem {\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
        sb.append("    isHD: ").append(toIndentedString(isHD)).append("\n");
        sb.append("    isShortcut: ").append(toIndentedString(isShortcut)).append("\n");
        sb.append("    shortcutPath: ").append(toIndentedString(shortcutPath)).append("\n");
        sb.append("    width: ").append(toIndentedString(width)).append("\n");
        sb.append("    height: ").append(toIndentedString(height)).append("\n");
        sb.append("    extraIds: ").append(toIndentedString(extraIds)).append("\n");
        sb.append("    dateLastSaved: ").append(toIndentedString(dateLastSaved)).append("\n");
        sb.append("    remoteTrailers: ").append(toIndentedString(remoteTrailers)).append("\n");
        sb.append("    supportsExternalTransfer: ").append(toIndentedString(supportsExternalTransfer)).append("\n");
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

        // add `Size` to the URL query string
        if (getSize() != null) {
            joiner.add(String.format("%sSize%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSize()))));
        }

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format("%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        // add `IsHD` to the URL query string
        if (getIsHD() != null) {
            joiner.add(String.format("%sIsHD%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsHD()))));
        }

        // add `IsShortcut` to the URL query string
        if (getIsShortcut() != null) {
            joiner.add(String.format("%sIsShortcut%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsShortcut()))));
        }

        // add `ShortcutPath` to the URL query string
        if (getShortcutPath() != null) {
            joiner.add(String.format("%sShortcutPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getShortcutPath()))));
        }

        // add `Width` to the URL query string
        if (getWidth() != null) {
            joiner.add(String.format("%sWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWidth()))));
        }

        // add `Height` to the URL query string
        if (getHeight() != null) {
            joiner.add(String.format("%sHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHeight()))));
        }

        // add `ExtraIds` to the URL query string
        if (getExtraIds() != null) {
            for (int i = 0; i < getExtraIds().size(); i++) {
                if (getExtraIds().get(i) != null) {
                    joiner.add(String.format("%sExtraIds%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getExtraIds().get(i)))));
                }
            }
        }

        // add `DateLastSaved` to the URL query string
        if (getDateLastSaved() != null) {
            joiner.add(String.format("%sDateLastSaved%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateLastSaved()))));
        }

        // add `RemoteTrailers` to the URL query string
        if (getRemoteTrailers() != null) {
            for (int i = 0; i < getRemoteTrailers().size(); i++) {
                if (getRemoteTrailers().get(i) != null) {
                    joiner.add(getRemoteTrailers().get(i).toUrlQueryString(String.format("%sRemoteTrailers%s%s", prefix,
                            suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `SupportsExternalTransfer` to the URL query string
        if (getSupportsExternalTransfer() != null) {
            joiner.add(String.format("%sSupportsExternalTransfer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsExternalTransfer()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BaseItem instance;

        public Builder() {
            this(new BaseItem());
        }

        protected Builder(BaseItem instance) {
            this.instance = instance;
        }

        public BaseItem.Builder size(Long size) {
            this.instance.size = size;
            return this;
        }

        public BaseItem.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        public BaseItem.Builder isHD(Boolean isHD) {
            this.instance.isHD = isHD;
            return this;
        }

        public BaseItem.Builder isShortcut(Boolean isShortcut) {
            this.instance.isShortcut = isShortcut;
            return this;
        }

        public BaseItem.Builder shortcutPath(String shortcutPath) {
            this.instance.shortcutPath = shortcutPath;
            return this;
        }

        public BaseItem.Builder width(Integer width) {
            this.instance.width = width;
            return this;
        }

        public BaseItem.Builder height(Integer height) {
            this.instance.height = height;
            return this;
        }

        public BaseItem.Builder extraIds(List<UUID> extraIds) {
            this.instance.extraIds = extraIds;
            return this;
        }

        public BaseItem.Builder dateLastSaved(OffsetDateTime dateLastSaved) {
            this.instance.dateLastSaved = dateLastSaved;
            return this;
        }

        public BaseItem.Builder remoteTrailers(List<MediaUrl> remoteTrailers) {
            this.instance.remoteTrailers = remoteTrailers;
            return this;
        }

        public BaseItem.Builder supportsExternalTransfer(Boolean supportsExternalTransfer) {
            this.instance.supportsExternalTransfer = supportsExternalTransfer;
            return this;
        }

        /**
         * returns a built BaseItem instance.
         *
         * The builder is not reusable.
         */
        public BaseItem build() {
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
    public static BaseItem.Builder builder() {
        return new BaseItem.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BaseItem.Builder toBuilder() {
        return new BaseItem.Builder().size(getSize()).container(getContainer()).isHD(getIsHD())
                .isShortcut(getIsShortcut()).shortcutPath(getShortcutPath()).width(getWidth()).height(getHeight())
                .extraIds(getExtraIds()).dateLastSaved(getDateLastSaved()).remoteTrailers(getRemoteTrailers())
                .supportsExternalTransfer(getSupportsExternalTransfer());
    }
}
