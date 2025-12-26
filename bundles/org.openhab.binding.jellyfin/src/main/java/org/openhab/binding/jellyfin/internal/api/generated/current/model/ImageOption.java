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
 * ImageOption
 */
@JsonPropertyOrder({ ImageOption.JSON_PROPERTY_TYPE, ImageOption.JSON_PROPERTY_LIMIT,
        ImageOption.JSON_PROPERTY_MIN_WIDTH })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ImageOption {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private ImageType type;

    public static final String JSON_PROPERTY_LIMIT = "Limit";
    @org.eclipse.jdt.annotation.NonNull
    private Integer limit;

    public static final String JSON_PROPERTY_MIN_WIDTH = "MinWidth";
    @org.eclipse.jdt.annotation.NonNull
    private Integer minWidth;

    public ImageOption() {
    }

    public ImageOption type(@org.eclipse.jdt.annotation.NonNull ImageType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ImageType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull ImageType type) {
        this.type = type;
    }

    public ImageOption limit(@org.eclipse.jdt.annotation.NonNull Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Gets or sets the limit.
     * 
     * @return limit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LIMIT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty(value = JSON_PROPERTY_LIMIT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLimit(@org.eclipse.jdt.annotation.NonNull Integer limit) {
        this.limit = limit;
    }

    public ImageOption minWidth(@org.eclipse.jdt.annotation.NonNull Integer minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    /**
     * Gets or sets the minimum width.
     * 
     * @return minWidth
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MIN_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMinWidth() {
        return minWidth;
    }

    @JsonProperty(value = JSON_PROPERTY_MIN_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinWidth(@org.eclipse.jdt.annotation.NonNull Integer minWidth) {
        this.minWidth = minWidth;
    }

    /**
     * Return true if this ImageOption object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageOption imageOption = (ImageOption) o;
        return Objects.equals(this.type, imageOption.type) && Objects.equals(this.limit, imageOption.limit)
                && Objects.equals(this.minWidth, imageOption.minWidth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, limit, minWidth);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ImageOption {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    minWidth: ").append(toIndentedString(minWidth)).append("\n");
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

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `Limit` to the URL query string
        if (getLimit() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLimit%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLimit()))));
        }

        // add `MinWidth` to the URL query string
        if (getMinWidth() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMinWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMinWidth()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ImageOption instance;

        public Builder() {
            this(new ImageOption());
        }

        protected Builder(ImageOption instance) {
            this.instance = instance;
        }

        public ImageOption.Builder type(ImageType type) {
            this.instance.type = type;
            return this;
        }

        public ImageOption.Builder limit(Integer limit) {
            this.instance.limit = limit;
            return this;
        }

        public ImageOption.Builder minWidth(Integer minWidth) {
            this.instance.minWidth = minWidth;
            return this;
        }

        /**
         * returns a built ImageOption instance.
         *
         * The builder is not reusable.
         */
        public ImageOption build() {
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
    public static ImageOption.Builder builder() {
        return new ImageOption.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ImageOption.Builder toBuilder() {
        return new ImageOption.Builder().type(getType()).limit(getLimit()).minWidth(getMinWidth());
    }
}
