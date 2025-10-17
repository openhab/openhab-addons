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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The notification DTO.
 */
@JsonPropertyOrder({ NotificationDto.JSON_PROPERTY_ID, NotificationDto.JSON_PROPERTY_USER_ID,
        NotificationDto.JSON_PROPERTY_DATE, NotificationDto.JSON_PROPERTY_IS_READ, NotificationDto.JSON_PROPERTY_NAME,
        NotificationDto.JSON_PROPERTY_DESCRIPTION, NotificationDto.JSON_PROPERTY_URL,
        NotificationDto.JSON_PROPERTY_LEVEL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NotificationDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private String userId;

    public static final String JSON_PROPERTY_DATE = "Date";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime date;

    public static final String JSON_PROPERTY_IS_READ = "IsRead";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isRead;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_DESCRIPTION = "Description";
    @org.eclipse.jdt.annotation.NonNull
    private String description;

    public static final String JSON_PROPERTY_URL = "Url";
    @org.eclipse.jdt.annotation.NonNull
    private String url;

    public static final String JSON_PROPERTY_LEVEL = "Level";
    @org.eclipse.jdt.annotation.NonNull
    private NotificationLevel level;

    public NotificationDto() {
    }

    public NotificationDto id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the notification ID. Defaults to an empty string.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public NotificationDto userId(@org.eclipse.jdt.annotation.NonNull String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the notification&#39;s user ID. Defaults to an empty string.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserId() {
        return userId;
    }

    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull String userId) {
        this.userId = userId;
    }

    public NotificationDto date(@org.eclipse.jdt.annotation.NonNull OffsetDateTime date) {
        this.date = date;
        return this;
    }

    /**
     * Gets or sets the notification date.
     * 
     * @return date
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDate() {
        return date;
    }

    @JsonProperty(JSON_PROPERTY_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime date) {
        this.date = date;
    }

    public NotificationDto isRead(@org.eclipse.jdt.annotation.NonNull Boolean isRead) {
        this.isRead = isRead;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the notification has been read. Defaults to false.
     * 
     * @return isRead
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_READ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsRead() {
        return isRead;
    }

    @JsonProperty(JSON_PROPERTY_IS_READ)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsRead(@org.eclipse.jdt.annotation.NonNull Boolean isRead) {
        this.isRead = isRead;
    }

    public NotificationDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the notification&#39;s name. Defaults to an empty string.
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

    public NotificationDto description(@org.eclipse.jdt.annotation.NonNull String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets or sets the notification&#39;s description. Defaults to an empty string.
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

    public NotificationDto url(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
        return this;
    }

    /**
     * Gets or sets the notification&#39;s URL. Defaults to an empty string.
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

    public NotificationDto level(@org.eclipse.jdt.annotation.NonNull NotificationLevel level) {
        this.level = level;
        return this;
    }

    /**
     * Gets or sets the notification level.
     * 
     * @return level
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public NotificationLevel getLevel() {
        return level;
    }

    @JsonProperty(JSON_PROPERTY_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLevel(@org.eclipse.jdt.annotation.NonNull NotificationLevel level) {
        this.level = level;
    }

    /**
     * Return true if this NotificationDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationDto notificationDto = (NotificationDto) o;
        return Objects.equals(this.id, notificationDto.id) && Objects.equals(this.userId, notificationDto.userId)
                && Objects.equals(this.date, notificationDto.date)
                && Objects.equals(this.isRead, notificationDto.isRead)
                && Objects.equals(this.name, notificationDto.name)
                && Objects.equals(this.description, notificationDto.description)
                && Objects.equals(this.url, notificationDto.url) && Objects.equals(this.level, notificationDto.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, date, isRead, name, description, url, level);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NotificationDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    date: ").append(toIndentedString(date)).append("\n");
        sb.append("    isRead: ").append(toIndentedString(isRead)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    level: ").append(toIndentedString(level)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(
                    String.format("%sId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format("%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `Date` to the URL query string
        if (getDate() != null) {
            joiner.add(String.format("%sDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDate()))));
        }

        // add `IsRead` to the URL query string
        if (getIsRead() != null) {
            joiner.add(String.format("%sIsRead%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsRead()))));
        }

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

        // add `Url` to the URL query string
        if (getUrl() != null) {
            joiner.add(String.format("%sUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUrl()))));
        }

        // add `Level` to the URL query string
        if (getLevel() != null) {
            joiner.add(String.format("%sLevel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLevel()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private NotificationDto instance;

        public Builder() {
            this(new NotificationDto());
        }

        protected Builder(NotificationDto instance) {
            this.instance = instance;
        }

        public NotificationDto.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public NotificationDto.Builder userId(String userId) {
            this.instance.userId = userId;
            return this;
        }

        public NotificationDto.Builder date(OffsetDateTime date) {
            this.instance.date = date;
            return this;
        }

        public NotificationDto.Builder isRead(Boolean isRead) {
            this.instance.isRead = isRead;
            return this;
        }

        public NotificationDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public NotificationDto.Builder description(String description) {
            this.instance.description = description;
            return this;
        }

        public NotificationDto.Builder url(String url) {
            this.instance.url = url;
            return this;
        }

        public NotificationDto.Builder level(NotificationLevel level) {
            this.instance.level = level;
            return this;
        }

        /**
         * returns a built NotificationDto instance.
         *
         * The builder is not reusable.
         */
        public NotificationDto build() {
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
    public static NotificationDto.Builder builder() {
        return new NotificationDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public NotificationDto.Builder toBuilder() {
        return new NotificationDto.Builder().id(getId()).userId(getUserId()).date(getDate()).isRead(getIsRead())
                .name(getName()).description(getDescription()).url(getUrl()).level(getLevel());
    }
}
