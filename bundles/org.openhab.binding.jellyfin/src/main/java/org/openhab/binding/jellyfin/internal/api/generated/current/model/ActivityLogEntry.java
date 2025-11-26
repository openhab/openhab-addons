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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * An activity log entry.
 */
@JsonPropertyOrder({ ActivityLogEntry.JSON_PROPERTY_ID, ActivityLogEntry.JSON_PROPERTY_NAME,
        ActivityLogEntry.JSON_PROPERTY_OVERVIEW, ActivityLogEntry.JSON_PROPERTY_SHORT_OVERVIEW,
        ActivityLogEntry.JSON_PROPERTY_TYPE, ActivityLogEntry.JSON_PROPERTY_ITEM_ID,
        ActivityLogEntry.JSON_PROPERTY_DATE, ActivityLogEntry.JSON_PROPERTY_USER_ID,
        ActivityLogEntry.JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG, ActivityLogEntry.JSON_PROPERTY_SEVERITY })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ActivityLogEntry {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private Long id;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_OVERVIEW = "Overview";
    @org.eclipse.jdt.annotation.NonNull
    private String overview;

    public static final String JSON_PROPERTY_SHORT_OVERVIEW = "ShortOverview";
    @org.eclipse.jdt.annotation.NonNull
    private String shortOverview;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private String type;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String itemId;

    public static final String JSON_PROPERTY_DATE = "Date";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime date;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG = "UserPrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String userPrimaryImageTag;

    public static final String JSON_PROPERTY_SEVERITY = "Severity";
    @org.eclipse.jdt.annotation.NonNull
    private LogLevel severity;

    public ActivityLogEntry() {
    }

    public ActivityLogEntry id(@org.eclipse.jdt.annotation.NonNull Long id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the identifier.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull Long id) {
        this.id = id;
    }

    public ActivityLogEntry name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public ActivityLogEntry overview(@org.eclipse.jdt.annotation.NonNull String overview) {
        this.overview = overview;
        return this;
    }

    /**
     * Gets or sets the overview.
     * 
     * @return overview
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOverview() {
        return overview;
    }

    @JsonProperty(value = JSON_PROPERTY_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOverview(@org.eclipse.jdt.annotation.NonNull String overview) {
        this.overview = overview;
    }

    public ActivityLogEntry shortOverview(@org.eclipse.jdt.annotation.NonNull String shortOverview) {
        this.shortOverview = shortOverview;
        return this;
    }

    /**
     * Gets or sets the short overview.
     * 
     * @return shortOverview
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SHORT_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getShortOverview() {
        return shortOverview;
    }

    @JsonProperty(value = JSON_PROPERTY_SHORT_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setShortOverview(@org.eclipse.jdt.annotation.NonNull String shortOverview) {
        this.shortOverview = shortOverview;
    }

    public ActivityLogEntry type(@org.eclipse.jdt.annotation.NonNull String type) {
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
    public String getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
    }

    public ActivityLogEntry itemId(@org.eclipse.jdt.annotation.NonNull String itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item identifier.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getItemId() {
        return itemId;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull String itemId) {
        this.itemId = itemId;
    }

    public ActivityLogEntry date(@org.eclipse.jdt.annotation.NonNull OffsetDateTime date) {
        this.date = date;
        return this;
    }

    /**
     * Gets or sets the date.
     * 
     * @return date
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDate() {
        return date;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime date) {
        this.date = date;
    }

    public ActivityLogEntry userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the user identifier.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
    }

    public ActivityLogEntry userPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String userPrimaryImageTag) {
        this.userPrimaryImageTag = userPrimaryImageTag;
        return this;
    }

    /**
     * Gets or sets the user primary image tag.
     * 
     * @return userPrimaryImageTag
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserPrimaryImageTag() {
        return userPrimaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String userPrimaryImageTag) {
        this.userPrimaryImageTag = userPrimaryImageTag;
    }

    public ActivityLogEntry severity(@org.eclipse.jdt.annotation.NonNull LogLevel severity) {
        this.severity = severity;
        return this;
    }

    /**
     * Gets or sets the log severity.
     * 
     * @return severity
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SEVERITY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public LogLevel getSeverity() {
        return severity;
    }

    @JsonProperty(value = JSON_PROPERTY_SEVERITY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeverity(@org.eclipse.jdt.annotation.NonNull LogLevel severity) {
        this.severity = severity;
    }

    /**
     * Return true if this ActivityLogEntry object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActivityLogEntry activityLogEntry = (ActivityLogEntry) o;
        return Objects.equals(this.id, activityLogEntry.id) && Objects.equals(this.name, activityLogEntry.name)
                && Objects.equals(this.overview, activityLogEntry.overview)
                && Objects.equals(this.shortOverview, activityLogEntry.shortOverview)
                && Objects.equals(this.type, activityLogEntry.type)
                && Objects.equals(this.itemId, activityLogEntry.itemId)
                && Objects.equals(this.date, activityLogEntry.date)
                && Objects.equals(this.userId, activityLogEntry.userId)
                && Objects.equals(this.userPrimaryImageTag, activityLogEntry.userPrimaryImageTag)
                && Objects.equals(this.severity, activityLogEntry.severity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, overview, shortOverview, type, itemId, date, userId, userPrimaryImageTag,
                severity);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ActivityLogEntry {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    overview: ").append(toIndentedString(overview)).append("\n");
        sb.append("    shortOverview: ").append(toIndentedString(shortOverview)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    date: ").append(toIndentedString(date)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    userPrimaryImageTag: ").append(toIndentedString(userPrimaryImageTag)).append("\n");
        sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Overview` to the URL query string
        if (getOverview() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOverview%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOverview()))));
        }

        // add `ShortOverview` to the URL query string
        if (getShortOverview() != null) {
            joiner.add(String.format(Locale.ROOT, "%sShortOverview%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getShortOverview()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
        }

        // add `Date` to the URL query string
        if (getDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDate()))));
        }

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `UserPrimaryImageTag` to the URL query string
        if (getUserPrimaryImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserPrimaryImageTag()))));
        }

        // add `Severity` to the URL query string
        if (getSeverity() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeverity%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeverity()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ActivityLogEntry instance;

        public Builder() {
            this(new ActivityLogEntry());
        }

        protected Builder(ActivityLogEntry instance) {
            this.instance = instance;
        }

        public ActivityLogEntry.Builder id(Long id) {
            this.instance.id = id;
            return this;
        }

        public ActivityLogEntry.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ActivityLogEntry.Builder overview(String overview) {
            this.instance.overview = overview;
            return this;
        }

        public ActivityLogEntry.Builder shortOverview(String shortOverview) {
            this.instance.shortOverview = shortOverview;
            return this;
        }

        public ActivityLogEntry.Builder type(String type) {
            this.instance.type = type;
            return this;
        }

        public ActivityLogEntry.Builder itemId(String itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        public ActivityLogEntry.Builder date(OffsetDateTime date) {
            this.instance.date = date;
            return this;
        }

        public ActivityLogEntry.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public ActivityLogEntry.Builder userPrimaryImageTag(String userPrimaryImageTag) {
            this.instance.userPrimaryImageTag = userPrimaryImageTag;
            return this;
        }

        public ActivityLogEntry.Builder severity(LogLevel severity) {
            this.instance.severity = severity;
            return this;
        }

        /**
         * returns a built ActivityLogEntry instance.
         *
         * The builder is not reusable.
         */
        public ActivityLogEntry build() {
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
    public static ActivityLogEntry.Builder builder() {
        return new ActivityLogEntry.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ActivityLogEntry.Builder toBuilder() {
        return new ActivityLogEntry.Builder().id(getId()).name(getName()).overview(getOverview())
                .shortOverview(getShortOverview()).type(getType()).itemId(getItemId()).date(getDate())
                .userId(getUserId()).userPrimaryImageTag(getUserPrimaryImageTag()).severity(getSeverity());
    }
}
