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

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A list of notifications with the total record count for pagination.
 */
@JsonPropertyOrder({ NotificationResultDto.JSON_PROPERTY_NOTIFICATIONS,
        NotificationResultDto.JSON_PROPERTY_TOTAL_RECORD_COUNT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NotificationResultDto {
    public static final String JSON_PROPERTY_NOTIFICATIONS = "Notifications";
    @org.eclipse.jdt.annotation.NonNull
    private List<NotificationDto> notifications = new ArrayList<>();

    public static final String JSON_PROPERTY_TOTAL_RECORD_COUNT = "TotalRecordCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer totalRecordCount;

    public NotificationResultDto() {
    }

    public NotificationResultDto notifications(
            @org.eclipse.jdt.annotation.NonNull List<NotificationDto> notifications) {
        this.notifications = notifications;
        return this;
    }

    public NotificationResultDto addNotificationsItem(NotificationDto notificationsItem) {
        if (this.notifications == null) {
            this.notifications = new ArrayList<>();
        }
        this.notifications.add(notificationsItem);
        return this;
    }

    /**
     * Gets or sets the current page of notifications.
     * 
     * @return notifications
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NOTIFICATIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NotificationDto> getNotifications() {
        return notifications;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFICATIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNotifications(@org.eclipse.jdt.annotation.NonNull List<NotificationDto> notifications) {
        this.notifications = notifications;
    }

    public NotificationResultDto totalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
        return this;
    }

    /**
     * Gets or sets the total number of notifications.
     * 
     * @return totalRecordCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TOTAL_RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTotalRecordCount() {
        return totalRecordCount;
    }

    @JsonProperty(JSON_PROPERTY_TOTAL_RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    /**
     * Return true if this NotificationResultDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationResultDto notificationResultDto = (NotificationResultDto) o;
        return Objects.equals(this.notifications, notificationResultDto.notifications)
                && Objects.equals(this.totalRecordCount, notificationResultDto.totalRecordCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notifications, totalRecordCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NotificationResultDto {\n");
        sb.append("    notifications: ").append(toIndentedString(notifications)).append("\n");
        sb.append("    totalRecordCount: ").append(toIndentedString(totalRecordCount)).append("\n");
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

        // add `Notifications` to the URL query string
        if (getNotifications() != null) {
            for (int i = 0; i < getNotifications().size(); i++) {
                if (getNotifications().get(i) != null) {
                    joiner.add(getNotifications().get(i).toUrlQueryString(String.format("%sNotifications%s%s", prefix,
                            suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `TotalRecordCount` to the URL query string
        if (getTotalRecordCount() != null) {
            joiner.add(String.format("%sTotalRecordCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTotalRecordCount()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private NotificationResultDto instance;

        public Builder() {
            this(new NotificationResultDto());
        }

        protected Builder(NotificationResultDto instance) {
            this.instance = instance;
        }

        public NotificationResultDto.Builder notifications(List<NotificationDto> notifications) {
            this.instance.notifications = notifications;
            return this;
        }

        public NotificationResultDto.Builder totalRecordCount(Integer totalRecordCount) {
            this.instance.totalRecordCount = totalRecordCount;
            return this;
        }

        /**
         * returns a built NotificationResultDto instance.
         *
         * The builder is not reusable.
         */
        public NotificationResultDto build() {
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
    public static NotificationResultDto.Builder builder() {
        return new NotificationResultDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public NotificationResultDto.Builder toBuilder() {
        return new NotificationResultDto.Builder().notifications(getNotifications())
                .totalRecordCount(getTotalRecordCount());
    }
}
