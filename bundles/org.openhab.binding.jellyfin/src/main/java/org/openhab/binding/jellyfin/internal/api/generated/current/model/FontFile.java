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

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class FontFile.
 */
@JsonPropertyOrder({ FontFile.JSON_PROPERTY_NAME, FontFile.JSON_PROPERTY_SIZE, FontFile.JSON_PROPERTY_DATE_CREATED,
        FontFile.JSON_PROPERTY_DATE_MODIFIED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class FontFile {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_SIZE = "Size";
    @org.eclipse.jdt.annotation.NonNull
    private Long size;

    public static final String JSON_PROPERTY_DATE_CREATED = "DateCreated";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateCreated;

    public static final String JSON_PROPERTY_DATE_MODIFIED = "DateModified";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateModified;

    public FontFile() {
    }

    public FontFile name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public FontFile size(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
        return this;
    }

    /**
     * Gets or sets the size.
     * 
     * @return size
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getSize() {
        return size;
    }

    @JsonProperty(value = JSON_PROPERTY_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSize(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
    }

    public FontFile dateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    /**
     * Gets or sets the date created.
     * 
     * @return dateCreated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateCreated() {
        return dateCreated;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public FontFile dateModified(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateModified) {
        this.dateModified = dateModified;
        return this;
    }

    /**
     * Gets or sets the date modified.
     * 
     * @return dateModified
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_MODIFIED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateModified() {
        return dateModified;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_MODIFIED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateModified(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateModified) {
        this.dateModified = dateModified;
    }

    /**
     * Return true if this FontFile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FontFile fontFile = (FontFile) o;
        return Objects.equals(this.name, fontFile.name) && Objects.equals(this.size, fontFile.size)
                && Objects.equals(this.dateCreated, fontFile.dateCreated)
                && Objects.equals(this.dateModified, fontFile.dateModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size, dateCreated, dateModified);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FontFile {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    dateCreated: ").append(toIndentedString(dateCreated)).append("\n");
        sb.append("    dateModified: ").append(toIndentedString(dateModified)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Size` to the URL query string
        if (getSize() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSize%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSize()))));
        }

        // add `DateCreated` to the URL query string
        if (getDateCreated() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateCreated%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateCreated()))));
        }

        // add `DateModified` to the URL query string
        if (getDateModified() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateModified%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateModified()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private FontFile instance;

        public Builder() {
            this(new FontFile());
        }

        protected Builder(FontFile instance) {
            this.instance = instance;
        }

        public FontFile.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public FontFile.Builder size(Long size) {
            this.instance.size = size;
            return this;
        }

        public FontFile.Builder dateCreated(OffsetDateTime dateCreated) {
            this.instance.dateCreated = dateCreated;
            return this;
        }

        public FontFile.Builder dateModified(OffsetDateTime dateModified) {
            this.instance.dateModified = dateModified;
            return this;
        }

        /**
         * returns a built FontFile instance.
         *
         * The builder is not reusable.
         */
        public FontFile build() {
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
    public static FontFile.Builder builder() {
        return new FontFile.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public FontFile.Builder toBuilder() {
        return new FontFile.Builder().name(getName()).size(getSize()).dateCreated(getDateCreated())
                .dateModified(getDateModified());
    }
}
