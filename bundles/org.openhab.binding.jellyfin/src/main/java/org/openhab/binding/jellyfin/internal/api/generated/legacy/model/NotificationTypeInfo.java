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
 * NotificationTypeInfo
 */
@JsonPropertyOrder({ NotificationTypeInfo.JSON_PROPERTY_TYPE, NotificationTypeInfo.JSON_PROPERTY_NAME,
        NotificationTypeInfo.JSON_PROPERTY_ENABLED, NotificationTypeInfo.JSON_PROPERTY_CATEGORY,
        NotificationTypeInfo.JSON_PROPERTY_IS_BASED_ON_USER_EVENT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NotificationTypeInfo {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private String type;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ENABLED = "Enabled";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enabled;

    public static final String JSON_PROPERTY_CATEGORY = "Category";
    @org.eclipse.jdt.annotation.NonNull
    private String category;

    public static final String JSON_PROPERTY_IS_BASED_ON_USER_EVENT = "IsBasedOnUserEvent";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isBasedOnUserEvent;

    public NotificationTypeInfo() {
    }

    public NotificationTypeInfo type(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
    }

    public NotificationTypeInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
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

    public NotificationTypeInfo enabled(@org.eclipse.jdt.annotation.NonNull Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get enabled
     * 
     * @return enabled
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty(JSON_PROPERTY_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabled(@org.eclipse.jdt.annotation.NonNull Boolean enabled) {
        this.enabled = enabled;
    }

    public NotificationTypeInfo category(@org.eclipse.jdt.annotation.NonNull String category) {
        this.category = category;
        return this;
    }

    /**
     * Get category
     * 
     * @return category
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CATEGORY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCategory() {
        return category;
    }

    @JsonProperty(JSON_PROPERTY_CATEGORY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCategory(@org.eclipse.jdt.annotation.NonNull String category) {
        this.category = category;
    }

    public NotificationTypeInfo isBasedOnUserEvent(@org.eclipse.jdt.annotation.NonNull Boolean isBasedOnUserEvent) {
        this.isBasedOnUserEvent = isBasedOnUserEvent;
        return this;
    }

    /**
     * Get isBasedOnUserEvent
     * 
     * @return isBasedOnUserEvent
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_BASED_ON_USER_EVENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsBasedOnUserEvent() {
        return isBasedOnUserEvent;
    }

    @JsonProperty(JSON_PROPERTY_IS_BASED_ON_USER_EVENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsBasedOnUserEvent(@org.eclipse.jdt.annotation.NonNull Boolean isBasedOnUserEvent) {
        this.isBasedOnUserEvent = isBasedOnUserEvent;
    }

    /**
     * Return true if this NotificationTypeInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationTypeInfo notificationTypeInfo = (NotificationTypeInfo) o;
        return Objects.equals(this.type, notificationTypeInfo.type)
                && Objects.equals(this.name, notificationTypeInfo.name)
                && Objects.equals(this.enabled, notificationTypeInfo.enabled)
                && Objects.equals(this.category, notificationTypeInfo.category)
                && Objects.equals(this.isBasedOnUserEvent, notificationTypeInfo.isBasedOnUserEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, enabled, category, isBasedOnUserEvent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NotificationTypeInfo {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    isBasedOnUserEvent: ").append(toIndentedString(isBasedOnUserEvent)).append("\n");
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
            joiner.add(String.format("%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format("%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Enabled` to the URL query string
        if (getEnabled() != null) {
            joiner.add(String.format("%sEnabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnabled()))));
        }

        // add `Category` to the URL query string
        if (getCategory() != null) {
            joiner.add(String.format("%sCategory%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCategory()))));
        }

        // add `IsBasedOnUserEvent` to the URL query string
        if (getIsBasedOnUserEvent() != null) {
            joiner.add(String.format("%sIsBasedOnUserEvent%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsBasedOnUserEvent()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private NotificationTypeInfo instance;

        public Builder() {
            this(new NotificationTypeInfo());
        }

        protected Builder(NotificationTypeInfo instance) {
            this.instance = instance;
        }

        public NotificationTypeInfo.Builder type(String type) {
            this.instance.type = type;
            return this;
        }

        public NotificationTypeInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public NotificationTypeInfo.Builder enabled(Boolean enabled) {
            this.instance.enabled = enabled;
            return this;
        }

        public NotificationTypeInfo.Builder category(String category) {
            this.instance.category = category;
            return this;
        }

        public NotificationTypeInfo.Builder isBasedOnUserEvent(Boolean isBasedOnUserEvent) {
            this.instance.isBasedOnUserEvent = isBasedOnUserEvent;
            return this;
        }

        /**
         * returns a built NotificationTypeInfo instance.
         *
         * The builder is not reusable.
         */
        public NotificationTypeInfo build() {
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
    public static NotificationTypeInfo.Builder builder() {
        return new NotificationTypeInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public NotificationTypeInfo.Builder toBuilder() {
        return new NotificationTypeInfo.Builder().type(getType()).name(getName()).enabled(getEnabled())
                .category(getCategory()).isBasedOnUserEvent(getIsBasedOnUserEvent());
    }
}
