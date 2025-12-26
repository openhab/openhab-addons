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
 * Contains information about a specific folder.
 */
@JsonPropertyOrder({ FolderStorageDto.JSON_PROPERTY_PATH, FolderStorageDto.JSON_PROPERTY_FREE_SPACE,
        FolderStorageDto.JSON_PROPERTY_USED_SPACE, FolderStorageDto.JSON_PROPERTY_STORAGE_TYPE,
        FolderStorageDto.JSON_PROPERTY_DEVICE_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class FolderStorageDto {
    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_FREE_SPACE = "FreeSpace";
    @org.eclipse.jdt.annotation.NonNull
    private Long freeSpace;

    public static final String JSON_PROPERTY_USED_SPACE = "UsedSpace";
    @org.eclipse.jdt.annotation.NonNull
    private Long usedSpace;

    public static final String JSON_PROPERTY_STORAGE_TYPE = "StorageType";
    @org.eclipse.jdt.annotation.NonNull
    private String storageType;

    public static final String JSON_PROPERTY_DEVICE_ID = "DeviceId";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceId;

    public FolderStorageDto() {
    }

    public FolderStorageDto path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets the path of the folder in question.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public FolderStorageDto freeSpace(@org.eclipse.jdt.annotation.NonNull Long freeSpace) {
        this.freeSpace = freeSpace;
        return this;
    }

    /**
     * Gets the free space of the underlying storage device of the
     * Jellyfin.Api.Models.SystemInfoDtos.FolderStorageDto.Path.
     * 
     * @return freeSpace
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_FREE_SPACE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getFreeSpace() {
        return freeSpace;
    }

    @JsonProperty(value = JSON_PROPERTY_FREE_SPACE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFreeSpace(@org.eclipse.jdt.annotation.NonNull Long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public FolderStorageDto usedSpace(@org.eclipse.jdt.annotation.NonNull Long usedSpace) {
        this.usedSpace = usedSpace;
        return this;
    }

    /**
     * Gets the used space of the underlying storage device of the
     * Jellyfin.Api.Models.SystemInfoDtos.FolderStorageDto.Path.
     * 
     * @return usedSpace
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USED_SPACE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getUsedSpace() {
        return usedSpace;
    }

    @JsonProperty(value = JSON_PROPERTY_USED_SPACE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUsedSpace(@org.eclipse.jdt.annotation.NonNull Long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public FolderStorageDto storageType(@org.eclipse.jdt.annotation.NonNull String storageType) {
        this.storageType = storageType;
        return this;
    }

    /**
     * Gets the kind of storage device of the Jellyfin.Api.Models.SystemInfoDtos.FolderStorageDto.Path.
     * 
     * @return storageType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STORAGE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getStorageType() {
        return storageType;
    }

    @JsonProperty(value = JSON_PROPERTY_STORAGE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStorageType(@org.eclipse.jdt.annotation.NonNull String storageType) {
        this.storageType = storageType;
    }

    public FolderStorageDto deviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Gets the Device Identifier.
     * 
     * @return deviceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Return true if this FolderStorageDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FolderStorageDto folderStorageDto = (FolderStorageDto) o;
        return Objects.equals(this.path, folderStorageDto.path)
                && Objects.equals(this.freeSpace, folderStorageDto.freeSpace)
                && Objects.equals(this.usedSpace, folderStorageDto.usedSpace)
                && Objects.equals(this.storageType, folderStorageDto.storageType)
                && Objects.equals(this.deviceId, folderStorageDto.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, freeSpace, usedSpace, storageType, deviceId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FolderStorageDto {\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    freeSpace: ").append(toIndentedString(freeSpace)).append("\n");
        sb.append("    usedSpace: ").append(toIndentedString(usedSpace)).append("\n");
        sb.append("    storageType: ").append(toIndentedString(storageType)).append("\n");
        sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
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

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `FreeSpace` to the URL query string
        if (getFreeSpace() != null) {
            joiner.add(String.format(Locale.ROOT, "%sFreeSpace%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFreeSpace()))));
        }

        // add `UsedSpace` to the URL query string
        if (getUsedSpace() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUsedSpace%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUsedSpace()))));
        }

        // add `StorageType` to the URL query string
        if (getStorageType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStorageType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStorageType()))));
        }

        // add `DeviceId` to the URL query string
        if (getDeviceId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private FolderStorageDto instance;

        public Builder() {
            this(new FolderStorageDto());
        }

        protected Builder(FolderStorageDto instance) {
            this.instance = instance;
        }

        public FolderStorageDto.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public FolderStorageDto.Builder freeSpace(Long freeSpace) {
            this.instance.freeSpace = freeSpace;
            return this;
        }

        public FolderStorageDto.Builder usedSpace(Long usedSpace) {
            this.instance.usedSpace = usedSpace;
            return this;
        }

        public FolderStorageDto.Builder storageType(String storageType) {
            this.instance.storageType = storageType;
            return this;
        }

        public FolderStorageDto.Builder deviceId(String deviceId) {
            this.instance.deviceId = deviceId;
            return this;
        }

        /**
         * returns a built FolderStorageDto instance.
         *
         * The builder is not reusable.
         */
        public FolderStorageDto build() {
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
    public static FolderStorageDto.Builder builder() {
        return new FolderStorageDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public FolderStorageDto.Builder toBuilder() {
        return new FolderStorageDto.Builder().path(getPath()).freeSpace(getFreeSpace()).usedSpace(getUsedSpace())
                .storageType(getStorageType()).deviceId(getDeviceId());
    }
}
