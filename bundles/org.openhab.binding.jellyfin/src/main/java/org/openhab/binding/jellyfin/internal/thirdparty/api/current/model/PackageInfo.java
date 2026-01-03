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
 * Class PackageInfo.
 */
@JsonPropertyOrder({ PackageInfo.JSON_PROPERTY_NAME, PackageInfo.JSON_PROPERTY_DESCRIPTION,
        PackageInfo.JSON_PROPERTY_OVERVIEW, PackageInfo.JSON_PROPERTY_OWNER, PackageInfo.JSON_PROPERTY_CATEGORY,
        PackageInfo.JSON_PROPERTY_GUID, PackageInfo.JSON_PROPERTY_VERSIONS, PackageInfo.JSON_PROPERTY_IMAGE_URL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PackageInfo {
    public static final String JSON_PROPERTY_NAME = "name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_DESCRIPTION = "description";
    @org.eclipse.jdt.annotation.Nullable
    private String description;

    public static final String JSON_PROPERTY_OVERVIEW = "overview";
    @org.eclipse.jdt.annotation.Nullable
    private String overview;

    public static final String JSON_PROPERTY_OWNER = "owner";
    @org.eclipse.jdt.annotation.Nullable
    private String owner;

    public static final String JSON_PROPERTY_CATEGORY = "category";
    @org.eclipse.jdt.annotation.Nullable
    private String category;

    public static final String JSON_PROPERTY_GUID = "guid";
    @org.eclipse.jdt.annotation.Nullable
    private UUID guid;

    public static final String JSON_PROPERTY_VERSIONS = "versions";
    @org.eclipse.jdt.annotation.Nullable
    private List<VersionInfo> versions = new ArrayList<>();

    public static final String JSON_PROPERTY_IMAGE_URL = "imageUrl";
    @org.eclipse.jdt.annotation.Nullable
    private String imageUrl;

    public PackageInfo() {
    }

    public PackageInfo name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
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

    public PackageInfo description(@org.eclipse.jdt.annotation.Nullable String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets or sets a long description of the plugin containing features or helpful explanations.
     * 
     * @return description
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDescription() {
        return description;
    }

    @JsonProperty(value = JSON_PROPERTY_DESCRIPTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDescription(@org.eclipse.jdt.annotation.Nullable String description) {
        this.description = description;
    }

    public PackageInfo overview(@org.eclipse.jdt.annotation.Nullable String overview) {
        this.overview = overview;
        return this;
    }

    /**
     * Gets or sets a short overview of what the plugin does.
     * 
     * @return overview
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOverview() {
        return overview;
    }

    @JsonProperty(value = JSON_PROPERTY_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOverview(@org.eclipse.jdt.annotation.Nullable String overview) {
        this.overview = overview;
    }

    public PackageInfo owner(@org.eclipse.jdt.annotation.Nullable String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets or sets the owner.
     * 
     * @return owner
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_OWNER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOwner() {
        return owner;
    }

    @JsonProperty(value = JSON_PROPERTY_OWNER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOwner(@org.eclipse.jdt.annotation.Nullable String owner) {
        this.owner = owner;
    }

    public PackageInfo category(@org.eclipse.jdt.annotation.Nullable String category) {
        this.category = category;
        return this;
    }

    /**
     * Gets or sets the category.
     * 
     * @return category
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CATEGORY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCategory() {
        return category;
    }

    @JsonProperty(value = JSON_PROPERTY_CATEGORY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCategory(@org.eclipse.jdt.annotation.Nullable String category) {
        this.category = category;
    }

    public PackageInfo guid(@org.eclipse.jdt.annotation.Nullable UUID guid) {
        this.guid = guid;
        return this;
    }

    /**
     * Gets or sets the guid of the assembly associated with this plugin. This is used to identify the proper item for
     * automatic updates.
     * 
     * @return guid
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_GUID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getGuid() {
        return guid;
    }

    @JsonProperty(value = JSON_PROPERTY_GUID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGuid(@org.eclipse.jdt.annotation.Nullable UUID guid) {
        this.guid = guid;
    }

    public PackageInfo versions(@org.eclipse.jdt.annotation.Nullable List<VersionInfo> versions) {
        this.versions = versions;
        return this;
    }

    public PackageInfo addVersionsItem(VersionInfo versionsItem) {
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.add(versionsItem);
        return this;
    }

    /**
     * Gets or sets the versions.
     * 
     * @return versions
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VERSIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<VersionInfo> getVersions() {
        return versions;
    }

    @JsonProperty(value = JSON_PROPERTY_VERSIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVersions(@org.eclipse.jdt.annotation.Nullable List<VersionInfo> versions) {
        this.versions = versions;
    }

    public PackageInfo imageUrl(@org.eclipse.jdt.annotation.Nullable String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    /**
     * Gets or sets the image url for the package.
     * 
     * @return imageUrl
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getImageUrl() {
        return imageUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageUrl(@org.eclipse.jdt.annotation.Nullable String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Return true if this PackageInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageInfo packageInfo = (PackageInfo) o;
        return Objects.equals(this.name, packageInfo.name) && Objects.equals(this.description, packageInfo.description)
                && Objects.equals(this.overview, packageInfo.overview) && Objects.equals(this.owner, packageInfo.owner)
                && Objects.equals(this.category, packageInfo.category) && Objects.equals(this.guid, packageInfo.guid)
                && Objects.equals(this.versions, packageInfo.versions)
                && Objects.equals(this.imageUrl, packageInfo.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, overview, owner, category, guid, versions, imageUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PackageInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    overview: ").append(toIndentedString(overview)).append("\n");
        sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    guid: ").append(toIndentedString(guid)).append("\n");
        sb.append("    versions: ").append(toIndentedString(versions)).append("\n");
        sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
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

        // add `name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sname%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `description` to the URL query string
        if (getDescription() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sdescription%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDescription()))));
        }

        // add `overview` to the URL query string
        if (getOverview() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%soverview%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOverview()))));
        }

        // add `owner` to the URL query string
        if (getOwner() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sowner%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOwner()))));
        }

        // add `category` to the URL query string
        if (getCategory() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%scategory%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCategory()))));
        }

        // add `guid` to the URL query string
        if (getGuid() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sguid%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGuid()))));
        }

        // add `versions` to the URL query string
        if (getVersions() != null) {
            for (int i = 0; i < getVersions().size(); i++) {
                if (getVersions().get(i) != null) {
                    joiner.add(getVersions().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sversions%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `imageUrl` to the URL query string
        if (getImageUrl() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%simageUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageUrl()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PackageInfo instance;

        public Builder() {
            this(new PackageInfo());
        }

        protected Builder(PackageInfo instance) {
            this.instance = instance;
        }

        public PackageInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public PackageInfo.Builder description(String description) {
            this.instance.description = description;
            return this;
        }

        public PackageInfo.Builder overview(String overview) {
            this.instance.overview = overview;
            return this;
        }

        public PackageInfo.Builder owner(String owner) {
            this.instance.owner = owner;
            return this;
        }

        public PackageInfo.Builder category(String category) {
            this.instance.category = category;
            return this;
        }

        public PackageInfo.Builder guid(UUID guid) {
            this.instance.guid = guid;
            return this;
        }

        public PackageInfo.Builder versions(List<VersionInfo> versions) {
            this.instance.versions = versions;
            return this;
        }

        public PackageInfo.Builder imageUrl(String imageUrl) {
            this.instance.imageUrl = imageUrl;
            return this;
        }

        /**
         * returns a built PackageInfo instance.
         *
         * The builder is not reusable.
         */
        public PackageInfo build() {
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
    public static PackageInfo.Builder builder() {
        return new PackageInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PackageInfo.Builder toBuilder() {
        return new PackageInfo.Builder().name(getName()).description(getDescription()).overview(getOverview())
                .owner(getOwner()).category(getCategory()).guid(getGuid()).versions(getVersions())
                .imageUrl(getImageUrl());
    }
}
