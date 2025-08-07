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
import java.util.Objects;
import java.util.UUID;

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
    @Deprecated
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
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
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
    @JsonProperty(JSON_PROPERTY_OVERVIEW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getOverview() {
        return overview;
    }

    @JsonProperty(JSON_PROPERTY_OVERVIEW)
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
    @JsonProperty(JSON_PROPERTY_SHORT_OVERVIEW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getShortOverview() {
        return shortOverview;
    }

    @JsonProperty(JSON_PROPERTY_SHORT_OVERVIEW)
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
    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getItemId() {
        return itemId;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_ID)
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
    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
    }

    @Deprecated
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
    @JsonProperty(JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUserPrimaryImageTag() {
        return userPrimaryImageTag;
    }

    @Deprecated
    @JsonProperty(JSON_PROPERTY_USER_PRIMARY_IMAGE_TAG)
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
    @JsonProperty(JSON_PROPERTY_SEVERITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LogLevel getSeverity() {
        return severity;
    }

    @JsonProperty(JSON_PROPERTY_SEVERITY)
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
}
