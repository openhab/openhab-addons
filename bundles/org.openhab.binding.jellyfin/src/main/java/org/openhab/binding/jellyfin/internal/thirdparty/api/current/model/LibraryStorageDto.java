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
 * Contains informations about a libraries storage informations.
 */
@JsonPropertyOrder({ LibraryStorageDto.JSON_PROPERTY_ID, LibraryStorageDto.JSON_PROPERTY_NAME,
        LibraryStorageDto.JSON_PROPERTY_FOLDERS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryStorageDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private UUID id;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_FOLDERS = "Folders";
    @org.eclipse.jdt.annotation.Nullable
    private List<FolderStorageDto> folders = new ArrayList<>();

    public LibraryStorageDto() {
    }

    public LibraryStorageDto id(@org.eclipse.jdt.annotation.Nullable UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the Library Id.
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

    public LibraryStorageDto name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name of the library.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
    }

    public LibraryStorageDto folders(@org.eclipse.jdt.annotation.Nullable List<FolderStorageDto> folders) {
        this.folders = folders;
        return this;
    }

    public LibraryStorageDto addFoldersItem(FolderStorageDto foldersItem) {
        if (this.folders == null) {
            this.folders = new ArrayList<>();
        }
        this.folders.add(foldersItem);
        return this;
    }

    /**
     * Gets or sets the storage informations about the folders used in a library.
     * 
     * @return folders
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<FolderStorageDto> getFolders() {
        return folders;
    }

    @JsonProperty(value = JSON_PROPERTY_FOLDERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFolders(@org.eclipse.jdt.annotation.Nullable List<FolderStorageDto> folders) {
        this.folders = folders;
    }

    /**
     * Return true if this LibraryStorageDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryStorageDto libraryStorageDto = (LibraryStorageDto) o;
        return Objects.equals(this.id, libraryStorageDto.id) && Objects.equals(this.name, libraryStorageDto.name)
                && Objects.equals(this.folders, libraryStorageDto.folders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, folders);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LibraryStorageDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    folders: ").append(toIndentedString(folders)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Folders` to the URL query string
        if (getFolders() != null) {
            for (int i = 0; i < getFolders().size(); i++) {
                if (getFolders().get(i) != null) {
                    joiner.add(getFolders().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sFolders%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private LibraryStorageDto instance;

        public Builder() {
            this(new LibraryStorageDto());
        }

        protected Builder(LibraryStorageDto instance) {
            this.instance = instance;
        }

        public LibraryStorageDto.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public LibraryStorageDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public LibraryStorageDto.Builder folders(List<FolderStorageDto> folders) {
            this.instance.folders = folders;
            return this;
        }

        /**
         * returns a built LibraryStorageDto instance.
         *
         * The builder is not reusable.
         */
        public LibraryStorageDto build() {
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
    public static LibraryStorageDto.Builder builder() {
        return new LibraryStorageDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LibraryStorageDto.Builder toBuilder() {
        return new LibraryStorageDto.Builder().id(getId()).name(getName()).folders(getFolders());
    }
}
