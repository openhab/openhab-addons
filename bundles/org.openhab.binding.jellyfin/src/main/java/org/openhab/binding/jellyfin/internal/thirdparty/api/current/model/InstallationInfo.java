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
 * Class InstallationInfo.
 */
@JsonPropertyOrder({ InstallationInfo.JSON_PROPERTY_GUID, InstallationInfo.JSON_PROPERTY_NAME,
        InstallationInfo.JSON_PROPERTY_VERSION, InstallationInfo.JSON_PROPERTY_CHANGELOG,
        InstallationInfo.JSON_PROPERTY_SOURCE_URL, InstallationInfo.JSON_PROPERTY_CHECKSUM,
        InstallationInfo.JSON_PROPERTY_PACKAGE_INFO })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class InstallationInfo {
    public static final String JSON_PROPERTY_GUID = "Guid";
    @org.eclipse.jdt.annotation.Nullable
    private UUID guid;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_VERSION = "Version";
    @org.eclipse.jdt.annotation.Nullable
    private String version;

    public static final String JSON_PROPERTY_CHANGELOG = "Changelog";
    @org.eclipse.jdt.annotation.Nullable
    private String changelog;

    public static final String JSON_PROPERTY_SOURCE_URL = "SourceUrl";
    @org.eclipse.jdt.annotation.Nullable
    private String sourceUrl;

    public static final String JSON_PROPERTY_CHECKSUM = "Checksum";
    @org.eclipse.jdt.annotation.Nullable
    private String checksum;

    public static final String JSON_PROPERTY_PACKAGE_INFO = "PackageInfo";
    @org.eclipse.jdt.annotation.Nullable
    private PackageInfo packageInfo;

    public InstallationInfo() {
    }

    public InstallationInfo guid(@org.eclipse.jdt.annotation.Nullable UUID guid) {
        this.guid = guid;
        return this;
    }

    /**
     * Gets or sets the Id.
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

    public InstallationInfo name(@org.eclipse.jdt.annotation.Nullable String name) {
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

    public InstallationInfo version(@org.eclipse.jdt.annotation.Nullable String version) {
        this.version = version;
        return this;
    }

    /**
     * Gets or sets the version.
     * 
     * @return version
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVersion() {
        return version;
    }

    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVersion(@org.eclipse.jdt.annotation.Nullable String version) {
        this.version = version;
    }

    public InstallationInfo changelog(@org.eclipse.jdt.annotation.Nullable String changelog) {
        this.changelog = changelog;
        return this;
    }

    /**
     * Gets or sets the changelog for this version.
     * 
     * @return changelog
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CHANGELOG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChangelog() {
        return changelog;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANGELOG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChangelog(@org.eclipse.jdt.annotation.Nullable String changelog) {
        this.changelog = changelog;
    }

    public InstallationInfo sourceUrl(@org.eclipse.jdt.annotation.Nullable String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    /**
     * Gets or sets the source URL.
     * 
     * @return sourceUrl
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SOURCE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSourceUrl() {
        return sourceUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_SOURCE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSourceUrl(@org.eclipse.jdt.annotation.Nullable String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public InstallationInfo checksum(@org.eclipse.jdt.annotation.Nullable String checksum) {
        this.checksum = checksum;
        return this;
    }

    /**
     * Gets or sets a checksum for the binary.
     * 
     * @return checksum
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CHECKSUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChecksum() {
        return checksum;
    }

    @JsonProperty(value = JSON_PROPERTY_CHECKSUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChecksum(@org.eclipse.jdt.annotation.Nullable String checksum) {
        this.checksum = checksum;
    }

    public InstallationInfo packageInfo(@org.eclipse.jdt.annotation.Nullable PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        return this;
    }

    /**
     * Gets or sets package information for the installation.
     * 
     * @return packageInfo
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PACKAGE_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    @JsonProperty(value = JSON_PROPERTY_PACKAGE_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPackageInfo(@org.eclipse.jdt.annotation.Nullable PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    /**
     * Return true if this InstallationInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstallationInfo installationInfo = (InstallationInfo) o;
        return Objects.equals(this.guid, installationInfo.guid) && Objects.equals(this.name, installationInfo.name)
                && Objects.equals(this.version, installationInfo.version)
                && Objects.equals(this.changelog, installationInfo.changelog)
                && Objects.equals(this.sourceUrl, installationInfo.sourceUrl)
                && Objects.equals(this.checksum, installationInfo.checksum)
                && Objects.equals(this.packageInfo, installationInfo.packageInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guid, name, version, changelog, sourceUrl, checksum, packageInfo);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InstallationInfo {\n");
        sb.append("    guid: ").append(toIndentedString(guid)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    changelog: ").append(toIndentedString(changelog)).append("\n");
        sb.append("    sourceUrl: ").append(toIndentedString(sourceUrl)).append("\n");
        sb.append("    checksum: ").append(toIndentedString(checksum)).append("\n");
        sb.append("    packageInfo: ").append(toIndentedString(packageInfo)).append("\n");
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

        // add `Guid` to the URL query string
        if (getGuid() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sGuid%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGuid()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Version` to the URL query string
        if (getVersion() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVersion()))));
        }

        // add `Changelog` to the URL query string
        if (getChangelog() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sChangelog%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChangelog()))));
        }

        // add `SourceUrl` to the URL query string
        if (getSourceUrl() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSourceUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSourceUrl()))));
        }

        // add `Checksum` to the URL query string
        if (getChecksum() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sChecksum%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChecksum()))));
        }

        // add `PackageInfo` to the URL query string
        if (getPackageInfo() != null) {
            joiner.add(getPackageInfo().toUrlQueryString(prefix + "PackageInfo" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private InstallationInfo instance;

        public Builder() {
            this(new InstallationInfo());
        }

        protected Builder(InstallationInfo instance) {
            this.instance = instance;
        }

        public InstallationInfo.Builder guid(UUID guid) {
            this.instance.guid = guid;
            return this;
        }

        public InstallationInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public InstallationInfo.Builder version(String version) {
            this.instance.version = version;
            return this;
        }

        public InstallationInfo.Builder changelog(String changelog) {
            this.instance.changelog = changelog;
            return this;
        }

        public InstallationInfo.Builder sourceUrl(String sourceUrl) {
            this.instance.sourceUrl = sourceUrl;
            return this;
        }

        public InstallationInfo.Builder checksum(String checksum) {
            this.instance.checksum = checksum;
            return this;
        }

        public InstallationInfo.Builder packageInfo(PackageInfo packageInfo) {
            this.instance.packageInfo = packageInfo;
            return this;
        }

        /**
         * returns a built InstallationInfo instance.
         *
         * The builder is not reusable.
         */
        public InstallationInfo build() {
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
    public static InstallationInfo.Builder builder() {
        return new InstallationInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public InstallationInfo.Builder toBuilder() {
        return new InstallationInfo.Builder().guid(getGuid()).name(getName()).version(getVersion())
                .changelog(getChangelog()).sourceUrl(getSourceUrl()).checksum(getChecksum())
                .packageInfo(getPackageInfo());
    }
}
