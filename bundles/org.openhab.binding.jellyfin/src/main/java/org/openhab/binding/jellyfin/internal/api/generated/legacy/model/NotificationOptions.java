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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * NotificationOptions
 */
@JsonPropertyOrder({ NotificationOptions.JSON_PROPERTY_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NotificationOptions {
    public static final String JSON_PROPERTY_OPTIONS = "Options";
    @org.eclipse.jdt.annotation.NonNull
    private List<NotificationOption> options;

    public NotificationOptions() {
    }

    public NotificationOptions options(@org.eclipse.jdt.annotation.NonNull List<NotificationOption> options) {
        this.options = options;
        return this;
    }

    public NotificationOptions addOptionsItem(NotificationOption optionsItem) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(optionsItem);
        return this;
    }

    /**
     * Get options
     * 
     * @return options
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NotificationOption> getOptions() {
        return options;
    }

    @JsonProperty(JSON_PROPERTY_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOptions(@org.eclipse.jdt.annotation.NonNull List<NotificationOption> options) {
        this.options = options;
    }

    /**
     * Return true if this NotificationOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationOptions notificationOptions = (NotificationOptions) o;
        return Objects.equals(this.options, notificationOptions.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NotificationOptions {\n");
        sb.append("    options: ").append(toIndentedString(options)).append("\n");
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

        // add `Options` to the URL query string
        if (getOptions() != null) {
            for (int i = 0; i < getOptions().size(); i++) {
                if (getOptions().get(i) != null) {
                    joiner.add(getOptions().get(i).toUrlQueryString(String.format("%sOptions%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private NotificationOptions instance;

        public Builder() {
            this(new NotificationOptions());
        }

        protected Builder(NotificationOptions instance) {
            this.instance = instance;
        }

        public NotificationOptions.Builder options(List<NotificationOption> options) {
            this.instance.options = options;
            return this;
        }

        /**
         * returns a built NotificationOptions instance.
         *
         * The builder is not reusable.
         */
        public NotificationOptions build() {
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
    public static NotificationOptions.Builder builder() {
        return new NotificationOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public NotificationOptions.Builder toBuilder() {
        return new NotificationOptions.Builder().options(getOptions());
    }
}
