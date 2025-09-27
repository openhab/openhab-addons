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
 * The notification summary DTO.
 */
@JsonPropertyOrder({ NotificationsSummaryDto.JSON_PROPERTY_UNREAD_COUNT,
        NotificationsSummaryDto.JSON_PROPERTY_MAX_UNREAD_NOTIFICATION_LEVEL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NotificationsSummaryDto {
    public static final String JSON_PROPERTY_UNREAD_COUNT = "UnreadCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer unreadCount;

    public static final String JSON_PROPERTY_MAX_UNREAD_NOTIFICATION_LEVEL = "MaxUnreadNotificationLevel";
    @org.eclipse.jdt.annotation.NonNull
    private NotificationLevel maxUnreadNotificationLevel;

    public NotificationsSummaryDto() {
    }

    public NotificationsSummaryDto unreadCount(@org.eclipse.jdt.annotation.NonNull Integer unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    /**
     * Gets or sets the number of unread notifications.
     * 
     * @return unreadCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_UNREAD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getUnreadCount() {
        return unreadCount;
    }

    @JsonProperty(JSON_PROPERTY_UNREAD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUnreadCount(@org.eclipse.jdt.annotation.NonNull Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public NotificationsSummaryDto maxUnreadNotificationLevel(
            @org.eclipse.jdt.annotation.NonNull NotificationLevel maxUnreadNotificationLevel) {
        this.maxUnreadNotificationLevel = maxUnreadNotificationLevel;
        return this;
    }

    /**
     * Gets or sets the maximum unread notification level.
     * 
     * @return maxUnreadNotificationLevel
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_UNREAD_NOTIFICATION_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public NotificationLevel getMaxUnreadNotificationLevel() {
        return maxUnreadNotificationLevel;
    }

    @JsonProperty(JSON_PROPERTY_MAX_UNREAD_NOTIFICATION_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxUnreadNotificationLevel(
            @org.eclipse.jdt.annotation.NonNull NotificationLevel maxUnreadNotificationLevel) {
        this.maxUnreadNotificationLevel = maxUnreadNotificationLevel;
    }

    /**
     * Return true if this NotificationsSummaryDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationsSummaryDto notificationsSummaryDto = (NotificationsSummaryDto) o;
        return Objects.equals(this.unreadCount, notificationsSummaryDto.unreadCount)
                && Objects.equals(this.maxUnreadNotificationLevel, notificationsSummaryDto.maxUnreadNotificationLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unreadCount, maxUnreadNotificationLevel);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NotificationsSummaryDto {\n");
        sb.append("    unreadCount: ").append(toIndentedString(unreadCount)).append("\n");
        sb.append("    maxUnreadNotificationLevel: ").append(toIndentedString(maxUnreadNotificationLevel)).append("\n");
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

        // add `UnreadCount` to the URL query string
        if (getUnreadCount() != null) {
            joiner.add(String.format("%sUnreadCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUnreadCount()))));
        }

        // add `MaxUnreadNotificationLevel` to the URL query string
        if (getMaxUnreadNotificationLevel() != null) {
            joiner.add(String.format("%sMaxUnreadNotificationLevel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxUnreadNotificationLevel()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private NotificationsSummaryDto instance;

        public Builder() {
            this(new NotificationsSummaryDto());
        }

        protected Builder(NotificationsSummaryDto instance) {
            this.instance = instance;
        }

        public NotificationsSummaryDto.Builder unreadCount(Integer unreadCount) {
            this.instance.unreadCount = unreadCount;
            return this;
        }

        public NotificationsSummaryDto.Builder maxUnreadNotificationLevel(
                NotificationLevel maxUnreadNotificationLevel) {
            this.instance.maxUnreadNotificationLevel = maxUnreadNotificationLevel;
            return this;
        }

        /**
         * returns a built NotificationsSummaryDto instance.
         *
         * The builder is not reusable.
         */
        public NotificationsSummaryDto build() {
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
    public static NotificationsSummaryDto.Builder builder() {
        return new NotificationsSummaryDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public NotificationsSummaryDto.Builder toBuilder() {
        return new NotificationsSummaryDto.Builder().unreadCount(getUnreadCount())
                .maxUnreadNotificationLevel(getMaxUnreadNotificationLevel());
    }
}
