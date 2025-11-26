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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the MediaBrowser.Model.Updates.VersionInfo class.
 */
@JsonPropertyOrder({ VersionInfo.JSON_PROPERTY_VERSION, VersionInfo.JSON_PROPERTY_VERSION_NUMBER,
        VersionInfo.JSON_PROPERTY_CHANGELOG, VersionInfo.JSON_PROPERTY_TARGET_ABI, VersionInfo.JSON_PROPERTY_SOURCE_URL,
        VersionInfo.JSON_PROPERTY_CHECKSUM, VersionInfo.JSON_PROPERTY_TIMESTAMP,
        VersionInfo.JSON_PROPERTY_REPOSITORY_NAME, VersionInfo.JSON_PROPERTY_REPOSITORY_URL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class VersionInfo {
    public static final String JSON_PROPERTY_VERSION = "version";
    @org.eclipse.jdt.annotation.NonNull
    private String version;

    public static final String JSON_PROPERTY_VERSION_NUMBER = "VersionNumber";
    @org.eclipse.jdt.annotation.NonNull
    private String versionNumber;

    public static final String JSON_PROPERTY_CHANGELOG = "changelog";
    @org.eclipse.jdt.annotation.NonNull
    private String changelog;

    public static final String JSON_PROPERTY_TARGET_ABI = "targetAbi";
    @org.eclipse.jdt.annotation.NonNull
    private String targetAbi;

    public static final String JSON_PROPERTY_SOURCE_URL = "sourceUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String sourceUrl;

    public static final String JSON_PROPERTY_CHECKSUM = "checksum";
    @org.eclipse.jdt.annotation.NonNull
    private String checksum;

    public static final String JSON_PROPERTY_TIMESTAMP = "timestamp";
    @org.eclipse.jdt.annotation.NonNull
    private String timestamp;

    public static final String JSON_PROPERTY_REPOSITORY_NAME = "repositoryName";
    @org.eclipse.jdt.annotation.NonNull
    private String repositoryName;

    public static final String JSON_PROPERTY_REPOSITORY_URL = "repositoryUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String repositoryUrl;

    public VersionInfo() {
    }

    @JsonCreator
    public VersionInfo(@JsonProperty(JSON_PROPERTY_VERSION_NUMBER) String versionNumber) {
        this();
        this.versionNumber = versionNumber;
    }

    public VersionInfo version(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
        return this;
    }

    /**
     * Gets or sets the version.
     * 
     * @return version
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVersion() {
        return version;
    }

    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVersion(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
    }

    /**
     * Gets the version as a System.Version.
     * 
     * @return versionNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VERSION_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVersionNumber() {
        return versionNumber;
    }

    public VersionInfo changelog(@org.eclipse.jdt.annotation.NonNull String changelog) {
        this.changelog = changelog;
        return this;
    }

    /**
     * Gets or sets the changelog for this version.
     * 
     * @return changelog
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHANGELOG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChangelog() {
        return changelog;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANGELOG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChangelog(@org.eclipse.jdt.annotation.NonNull String changelog) {
        this.changelog = changelog;
    }

    public VersionInfo targetAbi(@org.eclipse.jdt.annotation.NonNull String targetAbi) {
        this.targetAbi = targetAbi;
        return this;
    }

    /**
     * Gets or sets the ABI that this version was built against.
     * 
     * @return targetAbi
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TARGET_ABI, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTargetAbi() {
        return targetAbi;
    }

    @JsonProperty(value = JSON_PROPERTY_TARGET_ABI, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTargetAbi(@org.eclipse.jdt.annotation.NonNull String targetAbi) {
        this.targetAbi = targetAbi;
    }

    public VersionInfo sourceUrl(@org.eclipse.jdt.annotation.NonNull String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    /**
     * Gets or sets the source URL.
     * 
     * @return sourceUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SOURCE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSourceUrl() {
        return sourceUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_SOURCE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSourceUrl(@org.eclipse.jdt.annotation.NonNull String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public VersionInfo checksum(@org.eclipse.jdt.annotation.NonNull String checksum) {
        this.checksum = checksum;
        return this;
    }

    /**
     * Gets or sets a checksum for the binary.
     * 
     * @return checksum
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHECKSUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChecksum() {
        return checksum;
    }

    @JsonProperty(value = JSON_PROPERTY_CHECKSUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChecksum(@org.eclipse.jdt.annotation.NonNull String checksum) {
        this.checksum = checksum;
    }

    public VersionInfo timestamp(@org.eclipse.jdt.annotation.NonNull String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Gets or sets a timestamp of when the binary was built.
     * 
     * @return timestamp
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TIMESTAMP, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty(value = JSON_PROPERTY_TIMESTAMP, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimestamp(@org.eclipse.jdt.annotation.NonNull String timestamp) {
        this.timestamp = timestamp;
    }

    public VersionInfo repositoryName(@org.eclipse.jdt.annotation.NonNull String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    /**
     * Gets or sets the repository name.
     * 
     * @return repositoryName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_REPOSITORY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getRepositoryName() {
        return repositoryName;
    }

    @JsonProperty(value = JSON_PROPERTY_REPOSITORY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRepositoryName(@org.eclipse.jdt.annotation.NonNull String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public VersionInfo repositoryUrl(@org.eclipse.jdt.annotation.NonNull String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
        return this;
    }

    /**
     * Gets or sets the repository url.
     * 
     * @return repositoryUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_REPOSITORY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_REPOSITORY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRepositoryUrl(@org.eclipse.jdt.annotation.NonNull String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    /**
     * Return true if this VersionInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VersionInfo versionInfo = (VersionInfo) o;
        return Objects.equals(this.version, versionInfo.version)
                && Objects.equals(this.versionNumber, versionInfo.versionNumber)
                && Objects.equals(this.changelog, versionInfo.changelog)
                && Objects.equals(this.targetAbi, versionInfo.targetAbi)
                && Objects.equals(this.sourceUrl, versionInfo.sourceUrl)
                && Objects.equals(this.checksum, versionInfo.checksum)
                && Objects.equals(this.timestamp, versionInfo.timestamp)
                && Objects.equals(this.repositoryName, versionInfo.repositoryName)
                && Objects.equals(this.repositoryUrl, versionInfo.repositoryUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, versionNumber, changelog, targetAbi, sourceUrl, checksum, timestamp,
                repositoryName, repositoryUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class VersionInfo {\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    versionNumber: ").append(toIndentedString(versionNumber)).append("\n");
        sb.append("    changelog: ").append(toIndentedString(changelog)).append("\n");
        sb.append("    targetAbi: ").append(toIndentedString(targetAbi)).append("\n");
        sb.append("    sourceUrl: ").append(toIndentedString(sourceUrl)).append("\n");
        sb.append("    checksum: ").append(toIndentedString(checksum)).append("\n");
        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
        sb.append("    repositoryName: ").append(toIndentedString(repositoryName)).append("\n");
        sb.append("    repositoryUrl: ").append(toIndentedString(repositoryUrl)).append("\n");
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

        // add `version` to the URL query string
        if (getVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sversion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVersion()))));
        }

        // add `VersionNumber` to the URL query string
        if (getVersionNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVersionNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVersionNumber()))));
        }

        // add `changelog` to the URL query string
        if (getChangelog() != null) {
            joiner.add(String.format(Locale.ROOT, "%schangelog%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChangelog()))));
        }

        // add `targetAbi` to the URL query string
        if (getTargetAbi() != null) {
            joiner.add(String.format(Locale.ROOT, "%stargetAbi%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTargetAbi()))));
        }

        // add `sourceUrl` to the URL query string
        if (getSourceUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%ssourceUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSourceUrl()))));
        }

        // add `checksum` to the URL query string
        if (getChecksum() != null) {
            joiner.add(String.format(Locale.ROOT, "%schecksum%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChecksum()))));
        }

        // add `timestamp` to the URL query string
        if (getTimestamp() != null) {
            joiner.add(String.format(Locale.ROOT, "%stimestamp%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTimestamp()))));
        }

        // add `repositoryName` to the URL query string
        if (getRepositoryName() != null) {
            joiner.add(String.format(Locale.ROOT, "%srepositoryName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRepositoryName()))));
        }

        // add `repositoryUrl` to the URL query string
        if (getRepositoryUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%srepositoryUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRepositoryUrl()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private VersionInfo instance;

        public Builder() {
            this(new VersionInfo());
        }

        protected Builder(VersionInfo instance) {
            this.instance = instance;
        }

        public VersionInfo.Builder version(String version) {
            this.instance.version = version;
            return this;
        }

        public VersionInfo.Builder versionNumber(String versionNumber) {
            this.instance.versionNumber = versionNumber;
            return this;
        }

        public VersionInfo.Builder changelog(String changelog) {
            this.instance.changelog = changelog;
            return this;
        }

        public VersionInfo.Builder targetAbi(String targetAbi) {
            this.instance.targetAbi = targetAbi;
            return this;
        }

        public VersionInfo.Builder sourceUrl(String sourceUrl) {
            this.instance.sourceUrl = sourceUrl;
            return this;
        }

        public VersionInfo.Builder checksum(String checksum) {
            this.instance.checksum = checksum;
            return this;
        }

        public VersionInfo.Builder timestamp(String timestamp) {
            this.instance.timestamp = timestamp;
            return this;
        }

        public VersionInfo.Builder repositoryName(String repositoryName) {
            this.instance.repositoryName = repositoryName;
            return this;
        }

        public VersionInfo.Builder repositoryUrl(String repositoryUrl) {
            this.instance.repositoryUrl = repositoryUrl;
            return this;
        }

        /**
         * returns a built VersionInfo instance.
         *
         * The builder is not reusable.
         */
        public VersionInfo build() {
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
    public static VersionInfo.Builder builder() {
        return new VersionInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public VersionInfo.Builder toBuilder() {
        return new VersionInfo.Builder().version(getVersion()).versionNumber(getVersionNumber())
                .changelog(getChangelog()).targetAbi(getTargetAbi()).sourceUrl(getSourceUrl()).checksum(getChecksum())
                .timestamp(getTimestamp()).repositoryName(getRepositoryName()).repositoryUrl(getRepositoryUrl());
    }
}
