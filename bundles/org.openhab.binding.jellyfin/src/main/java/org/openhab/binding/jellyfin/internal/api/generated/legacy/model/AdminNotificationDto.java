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
 * The admin notification dto.
 */
@JsonPropertyOrder({ AdminNotificationDto.JSON_PROPERTY_NAME, AdminNotificationDto.JSON_PROPERTY_DESCRIPTION,
        AdminNotificationDto.JSON_PROPERTY_NOTIFICATION_LEVEL, AdminNotificationDto.JSON_PROPERTY_URL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class AdminNotificationDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_DESCRIPTION = "Description";
    @org.eclipse.jdt.annotation.NonNull
    private String description;

    public static final String JSON_PROPERTY_NOTIFICATION_LEVEL = "NotificationLevel";
    @org.eclipse.jdt.annotation.NonNull
    private NotificationLevel notificationLevel;

    public static final String JSON_PROPERTY_URL = "Url";
    @org.eclipse.jdt.annotation.NonNull
    private String url;

    public AdminNotificationDto() {
    }

    public AdminNotificationDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the notification name.
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

    public AdminNotificationDto description(@org.eclipse.jdt.annotation.NonNull String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets or sets the notification description.
     * 
     * @return description
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDescription() {
        return description;
    }

    @JsonProperty(JSON_PROPERTY_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDescription(@org.eclipse.jdt.annotation.NonNull String description) {
        this.description = description;
    }

    public AdminNotificationDto notificationLevel(
            @org.eclipse.jdt.annotation.NonNull NotificationLevel notificationLevel) {
        this.notificationLevel = notificationLevel;
        return this;
    }

    /**
     * Gets or sets the notification level.
     * 
     * @return notificationLevel
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NOTIFICATION_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public NotificationLevel getNotificationLevel() {
        return notificationLevel;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFICATION_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNotificationLevel(@org.eclipse.jdt.annotation.NonNull NotificationLevel notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    public AdminNotificationDto url(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
        return this;
    }

    /**
     * Gets or sets the notification url.
     * 
     * @return url
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUrl() {
        return url;
    }

    @JsonProperty(JSON_PROPERTY_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUrl(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
    }

    /**
     * Return true if this AdminNotificationDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdminNotificationDto adminNotificationDto = (AdminNotificationDto) o;
        return Objects.equals(this.name, adminNotificationDto.name)
                && Objects.equals(this.description, adminNotificationDto.description)
                && Objects.equals(this.notificationLevel, adminNotificationDto.notificationLevel)
                && Objects.equals(this.url, adminNotificationDto.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, notificationLevel, url);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdminNotificationDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    notificationLevel: ").append(toIndentedString(notificationLevel)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
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

        // add `Description` to the URL query string
        if (getDescription() != null) {
            joiner.add(String.format("%sDescription%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDescription()))));
        }

        // add `NotificationLevel` to the URL query string
        if (getNotificationLevel() != null) {
            joiner.add(String.format("%sNotificationLevel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNotificationLevel()))));
        }

        // add `Url` to the URL query string
        if (getUrl() != null) {
            joiner.add(String.format("%sUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUrl()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private AdminNotificationDto instance;

        public Builder() {
            this(new AdminNotificationDto());
        }

        protected Builder(AdminNotificationDto instance) {
            this.instance = instance;
        }

        public AdminNotificationDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public AdminNotificationDto.Builder description(String description) {
            this.instance.description = description;
            return this;
        }

        public AdminNotificationDto.Builder notificationLevel(NotificationLevel notificationLevel) {
            this.instance.notificationLevel = notificationLevel;
            return this;
        }

        public AdminNotificationDto.Builder url(String url) {
            this.instance.url = url;
            return this;
        }

        /**
         * returns a built AdminNotificationDto instance.
         *
         * The builder is not reusable.
         */
        public AdminNotificationDto build() {
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
    public static AdminNotificationDto.Builder builder() {
        return new AdminNotificationDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public AdminNotificationDto.Builder toBuilder() {
        return new AdminNotificationDto.Builder().name(getName()).description(getDescription())
                .notificationLevel(getNotificationLevel()).url(getUrl());
    }
}
