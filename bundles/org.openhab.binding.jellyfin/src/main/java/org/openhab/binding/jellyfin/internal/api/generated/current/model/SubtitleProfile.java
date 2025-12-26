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
 * A class for subtitle profile information.
 */
@JsonPropertyOrder({ SubtitleProfile.JSON_PROPERTY_FORMAT, SubtitleProfile.JSON_PROPERTY_METHOD,
        SubtitleProfile.JSON_PROPERTY_DIDL_MODE, SubtitleProfile.JSON_PROPERTY_LANGUAGE,
        SubtitleProfile.JSON_PROPERTY_CONTAINER })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SubtitleProfile {
    public static final String JSON_PROPERTY_FORMAT = "Format";
    @org.eclipse.jdt.annotation.NonNull
    private String format;

    public static final String JSON_PROPERTY_METHOD = "Method";
    @org.eclipse.jdt.annotation.NonNull
    private SubtitleDeliveryMethod method;

    public static final String JSON_PROPERTY_DIDL_MODE = "DidlMode";
    @org.eclipse.jdt.annotation.NonNull
    private String didlMode;

    public static final String JSON_PROPERTY_LANGUAGE = "Language";
    @org.eclipse.jdt.annotation.NonNull
    private String language;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public SubtitleProfile() {
    }

    public SubtitleProfile format(@org.eclipse.jdt.annotation.NonNull String format) {
        this.format = format;
        return this;
    }

    /**
     * Gets or sets the format.
     * 
     * @return format
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFormat() {
        return format;
    }

    @JsonProperty(value = JSON_PROPERTY_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFormat(@org.eclipse.jdt.annotation.NonNull String format) {
        this.format = format;
    }

    public SubtitleProfile method(@org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod method) {
        this.method = method;
        return this;
    }

    /**
     * Gets or sets the delivery method.
     * 
     * @return method
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_METHOD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SubtitleDeliveryMethod getMethod() {
        return method;
    }

    @JsonProperty(value = JSON_PROPERTY_METHOD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMethod(@org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod method) {
        this.method = method;
    }

    public SubtitleProfile didlMode(@org.eclipse.jdt.annotation.NonNull String didlMode) {
        this.didlMode = didlMode;
        return this;
    }

    /**
     * Gets or sets the DIDL mode.
     * 
     * @return didlMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DIDL_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDidlMode() {
        return didlMode;
    }

    @JsonProperty(value = JSON_PROPERTY_DIDL_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDidlMode(@org.eclipse.jdt.annotation.NonNull String didlMode) {
        this.didlMode = didlMode;
    }

    public SubtitleProfile language(@org.eclipse.jdt.annotation.NonNull String language) {
        this.language = language;
        return this;
    }

    /**
     * Gets or sets the language.
     * 
     * @return language
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLanguage() {
        return language;
    }

    @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLanguage(@org.eclipse.jdt.annotation.NonNull String language) {
        this.language = language;
    }

    public SubtitleProfile container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Gets or sets the container.
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContainer() {
        return container;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
    }

    /**
     * Return true if this SubtitleProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubtitleProfile subtitleProfile = (SubtitleProfile) o;
        return Objects.equals(this.format, subtitleProfile.format)
                && Objects.equals(this.method, subtitleProfile.method)
                && Objects.equals(this.didlMode, subtitleProfile.didlMode)
                && Objects.equals(this.language, subtitleProfile.language)
                && Objects.equals(this.container, subtitleProfile.container);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, method, didlMode, language, container);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SubtitleProfile {\n");
        sb.append("    format: ").append(toIndentedString(format)).append("\n");
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
        sb.append("    didlMode: ").append(toIndentedString(didlMode)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
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

        // add `Format` to the URL query string
        if (getFormat() != null) {
            joiner.add(String.format(Locale.ROOT, "%sFormat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFormat()))));
        }

        // add `Method` to the URL query string
        if (getMethod() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMethod%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMethod()))));
        }

        // add `DidlMode` to the URL query string
        if (getDidlMode() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDidlMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDidlMode()))));
        }

        // add `Language` to the URL query string
        if (getLanguage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLanguage()))));
        }

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format(Locale.ROOT, "%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SubtitleProfile instance;

        public Builder() {
            this(new SubtitleProfile());
        }

        protected Builder(SubtitleProfile instance) {
            this.instance = instance;
        }

        public SubtitleProfile.Builder format(String format) {
            this.instance.format = format;
            return this;
        }

        public SubtitleProfile.Builder method(SubtitleDeliveryMethod method) {
            this.instance.method = method;
            return this;
        }

        public SubtitleProfile.Builder didlMode(String didlMode) {
            this.instance.didlMode = didlMode;
            return this;
        }

        public SubtitleProfile.Builder language(String language) {
            this.instance.language = language;
            return this;
        }

        public SubtitleProfile.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        /**
         * returns a built SubtitleProfile instance.
         *
         * The builder is not reusable.
         */
        public SubtitleProfile build() {
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
    public static SubtitleProfile.Builder builder() {
        return new SubtitleProfile.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SubtitleProfile.Builder toBuilder() {
        return new SubtitleProfile.Builder().format(getFormat()).method(getMethod()).didlMode(getDidlMode())
                .language(getLanguage()).container(getContainer());
    }
}
