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

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the optional contents of the backup archive.
 */
@JsonPropertyOrder({ BackupOptionsDto.JSON_PROPERTY_METADATA, BackupOptionsDto.JSON_PROPERTY_TRICKPLAY,
        BackupOptionsDto.JSON_PROPERTY_SUBTITLES, BackupOptionsDto.JSON_PROPERTY_DATABASE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BackupOptionsDto {
    public static final String JSON_PROPERTY_METADATA = "Metadata";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean metadata;

    public static final String JSON_PROPERTY_TRICKPLAY = "Trickplay";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean trickplay;

    public static final String JSON_PROPERTY_SUBTITLES = "Subtitles";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean subtitles;

    public static final String JSON_PROPERTY_DATABASE = "Database";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean database;

    public BackupOptionsDto() {
    }

    public BackupOptionsDto metadata(@org.eclipse.jdt.annotation.Nullable Boolean metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the archive contains the Metadata contents.
     * 
     * @return metadata
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_METADATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getMetadata() {
        return metadata;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadata(@org.eclipse.jdt.annotation.Nullable Boolean metadata) {
        this.metadata = metadata;
    }

    public BackupOptionsDto trickplay(@org.eclipse.jdt.annotation.Nullable Boolean trickplay) {
        this.trickplay = trickplay;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the archive contains the Trickplay contents.
     * 
     * @return trickplay
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TRICKPLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getTrickplay() {
        return trickplay;
    }

    @JsonProperty(value = JSON_PROPERTY_TRICKPLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrickplay(@org.eclipse.jdt.annotation.Nullable Boolean trickplay) {
        this.trickplay = trickplay;
    }

    public BackupOptionsDto subtitles(@org.eclipse.jdt.annotation.Nullable Boolean subtitles) {
        this.subtitles = subtitles;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the archive contains the Subtitle contents.
     * 
     * @return subtitles
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSubtitles() {
        return subtitles;
    }

    @JsonProperty(value = JSON_PROPERTY_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitles(@org.eclipse.jdt.annotation.Nullable Boolean subtitles) {
        this.subtitles = subtitles;
    }

    public BackupOptionsDto database(@org.eclipse.jdt.annotation.Nullable Boolean database) {
        this.database = database;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the archive contains the Database contents.
     * 
     * @return database
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DATABASE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getDatabase() {
        return database;
    }

    @JsonProperty(value = JSON_PROPERTY_DATABASE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDatabase(@org.eclipse.jdt.annotation.Nullable Boolean database) {
        this.database = database;
    }

    /**
     * Return true if this BackupOptionsDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BackupOptionsDto backupOptionsDto = (BackupOptionsDto) o;
        return Objects.equals(this.metadata, backupOptionsDto.metadata)
                && Objects.equals(this.trickplay, backupOptionsDto.trickplay)
                && Objects.equals(this.subtitles, backupOptionsDto.subtitles)
                && Objects.equals(this.database, backupOptionsDto.database);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, trickplay, subtitles, database);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BackupOptionsDto {\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    trickplay: ").append(toIndentedString(trickplay)).append("\n");
        sb.append("    subtitles: ").append(toIndentedString(subtitles)).append("\n");
        sb.append("    database: ").append(toIndentedString(database)).append("\n");
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

        // add `Metadata` to the URL query string
        if (getMetadata() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMetadata%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadata()))));
        }

        // add `Trickplay` to the URL query string
        if (getTrickplay() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTrickplay%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTrickplay()))));
        }

        // add `Subtitles` to the URL query string
        if (getSubtitles() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSubtitles%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSubtitles()))));
        }

        // add `Database` to the URL query string
        if (getDatabase() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDatabase%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDatabase()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BackupOptionsDto instance;

        public Builder() {
            this(new BackupOptionsDto());
        }

        protected Builder(BackupOptionsDto instance) {
            this.instance = instance;
        }

        public BackupOptionsDto.Builder metadata(Boolean metadata) {
            this.instance.metadata = metadata;
            return this;
        }

        public BackupOptionsDto.Builder trickplay(Boolean trickplay) {
            this.instance.trickplay = trickplay;
            return this;
        }

        public BackupOptionsDto.Builder subtitles(Boolean subtitles) {
            this.instance.subtitles = subtitles;
            return this;
        }

        public BackupOptionsDto.Builder database(Boolean database) {
            this.instance.database = database;
            return this;
        }

        /**
         * returns a built BackupOptionsDto instance.
         *
         * The builder is not reusable.
         */
        public BackupOptionsDto build() {
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
    public static BackupOptionsDto.Builder builder() {
        return new BackupOptionsDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BackupOptionsDto.Builder toBuilder() {
        return new BackupOptionsDto.Builder().metadata(getMetadata()).trickplay(getTrickplay())
                .subtitles(getSubtitles()).database(getDatabase());
    }
}
