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
 * Class InstallationInfo.
 */
@JsonPropertyOrder({ InstallationInfo.JSON_PROPERTY_GUID, InstallationInfo.JSON_PROPERTY_NAME,
        InstallationInfo.JSON_PROPERTY_VERSION, InstallationInfo.JSON_PROPERTY_CHANGELOG,
        InstallationInfo.JSON_PROPERTY_SOURCE_URL, InstallationInfo.JSON_PROPERTY_CHECKSUM,
        InstallationInfo.JSON_PROPERTY_PACKAGE_INFO })

public class InstallationInfo {
    public static final String JSON_PROPERTY_GUID = "Guid";
    @org.eclipse.jdt.annotation.NonNull
    private UUID guid;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_VERSION = "Version";
    @org.eclipse.jdt.annotation.NonNull
    private String version;

    public static final String JSON_PROPERTY_CHANGELOG = "Changelog";
    @org.eclipse.jdt.annotation.NonNull
    private String changelog;

    public static final String JSON_PROPERTY_SOURCE_URL = "SourceUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String sourceUrl;

    public static final String JSON_PROPERTY_CHECKSUM = "Checksum";
    @org.eclipse.jdt.annotation.NonNull
    private String checksum;

    public static final String JSON_PROPERTY_PACKAGE_INFO = "PackageInfo";
    @org.eclipse.jdt.annotation.NonNull
    private PackageInfo packageInfo;

    public InstallationInfo() {
    }

    public InstallationInfo guid(@org.eclipse.jdt.annotation.NonNull UUID guid) {
        this.guid = guid;
        return this;
    }

    /**
     * Gets or sets the Id.
     * 
     * @return guid
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GUID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getGuid() {
        return guid;
    }

    @JsonProperty(JSON_PROPERTY_GUID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGuid(@org.eclipse.jdt.annotation.NonNull UUID guid) {
        this.guid = guid;
    }

    public InstallationInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public InstallationInfo version(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
        return this;
    }

    /**
     * Gets or sets the version.
     * 
     * @return version
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getVersion() {
        return version;
    }

    @JsonProperty(JSON_PROPERTY_VERSION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVersion(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
    }

    public InstallationInfo changelog(@org.eclipse.jdt.annotation.NonNull String changelog) {
        this.changelog = changelog;
        return this;
    }

    /**
     * Gets or sets the changelog for this version.
     * 
     * @return changelog
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CHANGELOG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getChangelog() {
        return changelog;
    }

    @JsonProperty(JSON_PROPERTY_CHANGELOG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChangelog(@org.eclipse.jdt.annotation.NonNull String changelog) {
        this.changelog = changelog;
    }

    public InstallationInfo sourceUrl(@org.eclipse.jdt.annotation.NonNull String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    /**
     * Gets or sets the source URL.
     * 
     * @return sourceUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SOURCE_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSourceUrl() {
        return sourceUrl;
    }

    @JsonProperty(JSON_PROPERTY_SOURCE_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSourceUrl(@org.eclipse.jdt.annotation.NonNull String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public InstallationInfo checksum(@org.eclipse.jdt.annotation.NonNull String checksum) {
        this.checksum = checksum;
        return this;
    }

    /**
     * Gets or sets a checksum for the binary.
     * 
     * @return checksum
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CHECKSUM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getChecksum() {
        return checksum;
    }

    @JsonProperty(JSON_PROPERTY_CHECKSUM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChecksum(@org.eclipse.jdt.annotation.NonNull String checksum) {
        this.checksum = checksum;
    }

    public InstallationInfo packageInfo(@org.eclipse.jdt.annotation.NonNull PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        return this;
    }

    /**
     * Gets or sets package information for the installation.
     * 
     * @return packageInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PACKAGE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    @JsonProperty(JSON_PROPERTY_PACKAGE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPackageInfo(@org.eclipse.jdt.annotation.NonNull PackageInfo packageInfo) {
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
}
