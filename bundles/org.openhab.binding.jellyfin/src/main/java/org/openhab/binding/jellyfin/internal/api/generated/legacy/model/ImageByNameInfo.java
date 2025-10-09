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

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ImageByNameInfo
 */
@JsonPropertyOrder({ ImageByNameInfo.JSON_PROPERTY_NAME, ImageByNameInfo.JSON_PROPERTY_THEME,
        ImageByNameInfo.JSON_PROPERTY_CONTEXT, ImageByNameInfo.JSON_PROPERTY_FILE_LENGTH,
        ImageByNameInfo.JSON_PROPERTY_FORMAT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ImageByNameInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_THEME = "Theme";
    @org.eclipse.jdt.annotation.NonNull
    private String theme;

    public static final String JSON_PROPERTY_CONTEXT = "Context";
    @org.eclipse.jdt.annotation.NonNull
    private String context;

    public static final String JSON_PROPERTY_FILE_LENGTH = "FileLength";
    @org.eclipse.jdt.annotation.NonNull
    private Long fileLength;

    public static final String JSON_PROPERTY_FORMAT = "Format";
    @org.eclipse.jdt.annotation.NonNull
    private String format;

    public ImageByNameInfo() {
    }

    public ImageByNameInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public ImageByNameInfo theme(@org.eclipse.jdt.annotation.NonNull String theme) {
        this.theme = theme;
        return this;
    }

    /**
     * Gets or sets the theme.
     * 
     * @return theme
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THEME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTheme() {
        return theme;
    }

    @JsonProperty(JSON_PROPERTY_THEME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTheme(@org.eclipse.jdt.annotation.NonNull String theme) {
        this.theme = theme;
    }

    public ImageByNameInfo context(@org.eclipse.jdt.annotation.NonNull String context) {
        this.context = context;
        return this;
    }

    /**
     * Gets or sets the context.
     * 
     * @return context
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContext() {
        return context;
    }

    @JsonProperty(JSON_PROPERTY_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContext(@org.eclipse.jdt.annotation.NonNull String context) {
        this.context = context;
    }

    public ImageByNameInfo fileLength(@org.eclipse.jdt.annotation.NonNull Long fileLength) {
        this.fileLength = fileLength;
        return this;
    }

    /**
     * Gets or sets the length of the file.
     * 
     * @return fileLength
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FILE_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getFileLength() {
        return fileLength;
    }

    @JsonProperty(JSON_PROPERTY_FILE_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFileLength(@org.eclipse.jdt.annotation.NonNull Long fileLength) {
        this.fileLength = fileLength;
    }

    public ImageByNameInfo format(@org.eclipse.jdt.annotation.NonNull String format) {
        this.format = format;
        return this;
    }

    /**
     * Gets or sets the format.
     * 
     * @return format
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FORMAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFormat() {
        return format;
    }

    @JsonProperty(JSON_PROPERTY_FORMAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFormat(@org.eclipse.jdt.annotation.NonNull String format) {
        this.format = format;
    }

    /**
     * Return true if this ImageByNameInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageByNameInfo imageByNameInfo = (ImageByNameInfo) o;
        return Objects.equals(this.name, imageByNameInfo.name) && Objects.equals(this.theme, imageByNameInfo.theme)
                && Objects.equals(this.context, imageByNameInfo.context)
                && Objects.equals(this.fileLength, imageByNameInfo.fileLength)
                && Objects.equals(this.format, imageByNameInfo.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, theme, context, fileLength, format);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ImageByNameInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    theme: ").append(toIndentedString(theme)).append("\n");
        sb.append("    context: ").append(toIndentedString(context)).append("\n");
        sb.append("    fileLength: ").append(toIndentedString(fileLength)).append("\n");
        sb.append("    format: ").append(toIndentedString(format)).append("\n");
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
            joiner.add(String.format("%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Theme` to the URL query string
        if (getTheme() != null) {
            joiner.add(String.format("%sTheme%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTheme()))));
        }

        // add `Context` to the URL query string
        if (getContext() != null) {
            joiner.add(String.format("%sContext%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContext()))));
        }

        // add `FileLength` to the URL query string
        if (getFileLength() != null) {
            joiner.add(String.format("%sFileLength%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFileLength()))));
        }

        // add `Format` to the URL query string
        if (getFormat() != null) {
            joiner.add(String.format("%sFormat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFormat()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ImageByNameInfo instance;

        public Builder() {
            this(new ImageByNameInfo());
        }

        protected Builder(ImageByNameInfo instance) {
            this.instance = instance;
        }

        public ImageByNameInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ImageByNameInfo.Builder theme(String theme) {
            this.instance.theme = theme;
            return this;
        }

        public ImageByNameInfo.Builder context(String context) {
            this.instance.context = context;
            return this;
        }

        public ImageByNameInfo.Builder fileLength(Long fileLength) {
            this.instance.fileLength = fileLength;
            return this;
        }

        public ImageByNameInfo.Builder format(String format) {
            this.instance.format = format;
            return this;
        }

        /**
         * returns a built ImageByNameInfo instance.
         *
         * The builder is not reusable.
         */
        public ImageByNameInfo build() {
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
    public static ImageByNameInfo.Builder builder() {
        return new ImageByNameInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ImageByNameInfo.Builder toBuilder() {
        return new ImageByNameInfo.Builder().name(getName()).theme(getTheme()).context(getContext())
                .fileLength(getFileLength()).format(getFormat());
    }
}
