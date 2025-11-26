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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Contains informations about the systems storage.
 */
@JsonPropertyOrder({ SystemStorageDto.JSON_PROPERTY_PROGRAM_DATA_FOLDER, SystemStorageDto.JSON_PROPERTY_WEB_FOLDER,
        SystemStorageDto.JSON_PROPERTY_IMAGE_CACHE_FOLDER, SystemStorageDto.JSON_PROPERTY_CACHE_FOLDER,
        SystemStorageDto.JSON_PROPERTY_LOG_FOLDER, SystemStorageDto.JSON_PROPERTY_INTERNAL_METADATA_FOLDER,
        SystemStorageDto.JSON_PROPERTY_TRANSCODING_TEMP_FOLDER, SystemStorageDto.JSON_PROPERTY_LIBRARIES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SystemStorageDto {
    public static final String JSON_PROPERTY_PROGRAM_DATA_FOLDER = "ProgramDataFolder";
    @org.eclipse.jdt.annotation.NonNull
    private FolderStorageDto programDataFolder;

    public static final String JSON_PROPERTY_WEB_FOLDER = "WebFolder";
    @org.eclipse.jdt.annotation.NonNull
    private FolderStorageDto webFolder;

    public static final String JSON_PROPERTY_IMAGE_CACHE_FOLDER = "ImageCacheFolder";
    @org.eclipse.jdt.annotation.NonNull
    private FolderStorageDto imageCacheFolder;

    public static final String JSON_PROPERTY_CACHE_FOLDER = "CacheFolder";
    @org.eclipse.jdt.annotation.NonNull
    private FolderStorageDto cacheFolder;

    public static final String JSON_PROPERTY_LOG_FOLDER = "LogFolder";
    @org.eclipse.jdt.annotation.NonNull
    private FolderStorageDto logFolder;

    public static final String JSON_PROPERTY_INTERNAL_METADATA_FOLDER = "InternalMetadataFolder";
    @org.eclipse.jdt.annotation.NonNull
    private FolderStorageDto internalMetadataFolder;

    public static final String JSON_PROPERTY_TRANSCODING_TEMP_FOLDER = "TranscodingTempFolder";
    @org.eclipse.jdt.annotation.NonNull
    private FolderStorageDto transcodingTempFolder;

    public static final String JSON_PROPERTY_LIBRARIES = "Libraries";
    @org.eclipse.jdt.annotation.NonNull
    private List<LibraryStorageDto> libraries = new ArrayList<>();

    public SystemStorageDto() {
    }

    public SystemStorageDto programDataFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto programDataFolder) {
        this.programDataFolder = programDataFolder;
        return this;
    }

    /**
     * Gets or sets the Storage information of the program data folder.
     * 
     * @return programDataFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROGRAM_DATA_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public FolderStorageDto getProgramDataFolder() {
        return programDataFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_PROGRAM_DATA_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramDataFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto programDataFolder) {
        this.programDataFolder = programDataFolder;
    }

    public SystemStorageDto webFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto webFolder) {
        this.webFolder = webFolder;
        return this;
    }

    /**
     * Gets or sets the Storage information of the web UI resources folder.
     * 
     * @return webFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_WEB_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public FolderStorageDto getWebFolder() {
        return webFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_WEB_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWebFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto webFolder) {
        this.webFolder = webFolder;
    }

    public SystemStorageDto imageCacheFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto imageCacheFolder) {
        this.imageCacheFolder = imageCacheFolder;
        return this;
    }

    /**
     * Gets or sets the Storage information of the folder where images are cached.
     * 
     * @return imageCacheFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IMAGE_CACHE_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public FolderStorageDto getImageCacheFolder() {
        return imageCacheFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_CACHE_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageCacheFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto imageCacheFolder) {
        this.imageCacheFolder = imageCacheFolder;
    }

    public SystemStorageDto cacheFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto cacheFolder) {
        this.cacheFolder = cacheFolder;
        return this;
    }

    /**
     * Gets or sets the Storage information of the cache folder.
     * 
     * @return cacheFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CACHE_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public FolderStorageDto getCacheFolder() {
        return cacheFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_CACHE_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCacheFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto cacheFolder) {
        this.cacheFolder = cacheFolder;
    }

    public SystemStorageDto logFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto logFolder) {
        this.logFolder = logFolder;
        return this;
    }

    /**
     * Gets or sets the Storage information of the folder where logfiles are saved to.
     * 
     * @return logFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOG_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public FolderStorageDto getLogFolder() {
        return logFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_LOG_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLogFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto logFolder) {
        this.logFolder = logFolder;
    }

    public SystemStorageDto internalMetadataFolder(
            @org.eclipse.jdt.annotation.NonNull FolderStorageDto internalMetadataFolder) {
        this.internalMetadataFolder = internalMetadataFolder;
        return this;
    }

    /**
     * Gets or sets the Storage information of the folder where metadata is stored.
     * 
     * @return internalMetadataFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_INTERNAL_METADATA_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public FolderStorageDto getInternalMetadataFolder() {
        return internalMetadataFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_INTERNAL_METADATA_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInternalMetadataFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto internalMetadataFolder) {
        this.internalMetadataFolder = internalMetadataFolder;
    }

    public SystemStorageDto transcodingTempFolder(
            @org.eclipse.jdt.annotation.NonNull FolderStorageDto transcodingTempFolder) {
        this.transcodingTempFolder = transcodingTempFolder;
        return this;
    }

    /**
     * Gets or sets the Storage information of the transcoding cache.
     * 
     * @return transcodingTempFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TRANSCODING_TEMP_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public FolderStorageDto getTranscodingTempFolder() {
        return transcodingTempFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_TRANSCODING_TEMP_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingTempFolder(@org.eclipse.jdt.annotation.NonNull FolderStorageDto transcodingTempFolder) {
        this.transcodingTempFolder = transcodingTempFolder;
    }

    public SystemStorageDto libraries(@org.eclipse.jdt.annotation.NonNull List<LibraryStorageDto> libraries) {
        this.libraries = libraries;
        return this;
    }

    public SystemStorageDto addLibrariesItem(LibraryStorageDto librariesItem) {
        if (this.libraries == null) {
            this.libraries = new ArrayList<>();
        }
        this.libraries.add(librariesItem);
        return this;
    }

    /**
     * Gets or sets the storage informations of all libraries.
     * 
     * @return libraries
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LIBRARIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryStorageDto> getLibraries() {
        return libraries;
    }

    @JsonProperty(value = JSON_PROPERTY_LIBRARIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLibraries(@org.eclipse.jdt.annotation.NonNull List<LibraryStorageDto> libraries) {
        this.libraries = libraries;
    }

    /**
     * Return true if this SystemStorageDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SystemStorageDto systemStorageDto = (SystemStorageDto) o;
        return Objects.equals(this.programDataFolder, systemStorageDto.programDataFolder)
                && Objects.equals(this.webFolder, systemStorageDto.webFolder)
                && Objects.equals(this.imageCacheFolder, systemStorageDto.imageCacheFolder)
                && Objects.equals(this.cacheFolder, systemStorageDto.cacheFolder)
                && Objects.equals(this.logFolder, systemStorageDto.logFolder)
                && Objects.equals(this.internalMetadataFolder, systemStorageDto.internalMetadataFolder)
                && Objects.equals(this.transcodingTempFolder, systemStorageDto.transcodingTempFolder)
                && Objects.equals(this.libraries, systemStorageDto.libraries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programDataFolder, webFolder, imageCacheFolder, cacheFolder, logFolder,
                internalMetadataFolder, transcodingTempFolder, libraries);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SystemStorageDto {\n");
        sb.append("    programDataFolder: ").append(toIndentedString(programDataFolder)).append("\n");
        sb.append("    webFolder: ").append(toIndentedString(webFolder)).append("\n");
        sb.append("    imageCacheFolder: ").append(toIndentedString(imageCacheFolder)).append("\n");
        sb.append("    cacheFolder: ").append(toIndentedString(cacheFolder)).append("\n");
        sb.append("    logFolder: ").append(toIndentedString(logFolder)).append("\n");
        sb.append("    internalMetadataFolder: ").append(toIndentedString(internalMetadataFolder)).append("\n");
        sb.append("    transcodingTempFolder: ").append(toIndentedString(transcodingTempFolder)).append("\n");
        sb.append("    libraries: ").append(toIndentedString(libraries)).append("\n");
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

        // add `ProgramDataFolder` to the URL query string
        if (getProgramDataFolder() != null) {
            joiner.add(getProgramDataFolder().toUrlQueryString(prefix + "ProgramDataFolder" + suffix));
        }

        // add `WebFolder` to the URL query string
        if (getWebFolder() != null) {
            joiner.add(getWebFolder().toUrlQueryString(prefix + "WebFolder" + suffix));
        }

        // add `ImageCacheFolder` to the URL query string
        if (getImageCacheFolder() != null) {
            joiner.add(getImageCacheFolder().toUrlQueryString(prefix + "ImageCacheFolder" + suffix));
        }

        // add `CacheFolder` to the URL query string
        if (getCacheFolder() != null) {
            joiner.add(getCacheFolder().toUrlQueryString(prefix + "CacheFolder" + suffix));
        }

        // add `LogFolder` to the URL query string
        if (getLogFolder() != null) {
            joiner.add(getLogFolder().toUrlQueryString(prefix + "LogFolder" + suffix));
        }

        // add `InternalMetadataFolder` to the URL query string
        if (getInternalMetadataFolder() != null) {
            joiner.add(getInternalMetadataFolder().toUrlQueryString(prefix + "InternalMetadataFolder" + suffix));
        }

        // add `TranscodingTempFolder` to the URL query string
        if (getTranscodingTempFolder() != null) {
            joiner.add(getTranscodingTempFolder().toUrlQueryString(prefix + "TranscodingTempFolder" + suffix));
        }

        // add `Libraries` to the URL query string
        if (getLibraries() != null) {
            for (int i = 0; i < getLibraries().size(); i++) {
                if (getLibraries().get(i) != null) {
                    joiner.add(getLibraries().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sLibraries%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private SystemStorageDto instance;

        public Builder() {
            this(new SystemStorageDto());
        }

        protected Builder(SystemStorageDto instance) {
            this.instance = instance;
        }

        public SystemStorageDto.Builder programDataFolder(FolderStorageDto programDataFolder) {
            this.instance.programDataFolder = programDataFolder;
            return this;
        }

        public SystemStorageDto.Builder webFolder(FolderStorageDto webFolder) {
            this.instance.webFolder = webFolder;
            return this;
        }

        public SystemStorageDto.Builder imageCacheFolder(FolderStorageDto imageCacheFolder) {
            this.instance.imageCacheFolder = imageCacheFolder;
            return this;
        }

        public SystemStorageDto.Builder cacheFolder(FolderStorageDto cacheFolder) {
            this.instance.cacheFolder = cacheFolder;
            return this;
        }

        public SystemStorageDto.Builder logFolder(FolderStorageDto logFolder) {
            this.instance.logFolder = logFolder;
            return this;
        }

        public SystemStorageDto.Builder internalMetadataFolder(FolderStorageDto internalMetadataFolder) {
            this.instance.internalMetadataFolder = internalMetadataFolder;
            return this;
        }

        public SystemStorageDto.Builder transcodingTempFolder(FolderStorageDto transcodingTempFolder) {
            this.instance.transcodingTempFolder = transcodingTempFolder;
            return this;
        }

        public SystemStorageDto.Builder libraries(List<LibraryStorageDto> libraries) {
            this.instance.libraries = libraries;
            return this;
        }

        /**
         * returns a built SystemStorageDto instance.
         *
         * The builder is not reusable.
         */
        public SystemStorageDto build() {
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
    public static SystemStorageDto.Builder builder() {
        return new SystemStorageDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SystemStorageDto.Builder toBuilder() {
        return new SystemStorageDto.Builder().programDataFolder(getProgramDataFolder()).webFolder(getWebFolder())
                .imageCacheFolder(getImageCacheFolder()).cacheFolder(getCacheFolder()).logFolder(getLogFolder())
                .internalMetadataFolder(getInternalMetadataFolder()).transcodingTempFolder(getTranscodingTempFolder())
                .libraries(getLibraries());
    }
}
