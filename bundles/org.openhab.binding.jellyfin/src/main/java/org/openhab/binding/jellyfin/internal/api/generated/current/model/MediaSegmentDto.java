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

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Api model for MediaSegment&#39;s.
 */
@JsonPropertyOrder({ MediaSegmentDto.JSON_PROPERTY_ID, MediaSegmentDto.JSON_PROPERTY_ITEM_ID,
        MediaSegmentDto.JSON_PROPERTY_TYPE, MediaSegmentDto.JSON_PROPERTY_START_TICKS,
        MediaSegmentDto.JSON_PROPERTY_END_TICKS })

public class MediaSegmentDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID itemId;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private MediaSegmentType type = MediaSegmentType.UNKNOWN;

    public static final String JSON_PROPERTY_START_TICKS = "StartTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long startTicks;

    public static final String JSON_PROPERTY_END_TICKS = "EndTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long endTicks;

    public MediaSegmentDto() {
    }

    public MediaSegmentDto id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id of the media segment.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public MediaSegmentDto itemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the id of the associated item.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
    }

    public MediaSegmentDto type(@org.eclipse.jdt.annotation.NonNull MediaSegmentType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type of content this segment defines.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MediaSegmentType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull MediaSegmentType type) {
        this.type = type;
    }

    public MediaSegmentDto startTicks(@org.eclipse.jdt.annotation.NonNull Long startTicks) {
        this.startTicks = startTicks;
        return this;
    }

    /**
     * Gets or sets the start of the segment.
     * 
     * @return startTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getStartTicks() {
        return startTicks;
    }

    @JsonProperty(JSON_PROPERTY_START_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartTicks(@org.eclipse.jdt.annotation.NonNull Long startTicks) {
        this.startTicks = startTicks;
    }

    public MediaSegmentDto endTicks(@org.eclipse.jdt.annotation.NonNull Long endTicks) {
        this.endTicks = endTicks;
        return this;
    }

    /**
     * Gets or sets the end of the segment.
     * 
     * @return endTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_END_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getEndTicks() {
        return endTicks;
    }

    @JsonProperty(JSON_PROPERTY_END_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndTicks(@org.eclipse.jdt.annotation.NonNull Long endTicks) {
        this.endTicks = endTicks;
    }

    /**
     * Return true if this MediaSegmentDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaSegmentDto mediaSegmentDto = (MediaSegmentDto) o;
        return Objects.equals(this.id, mediaSegmentDto.id) && Objects.equals(this.itemId, mediaSegmentDto.itemId)
                && Objects.equals(this.type, mediaSegmentDto.type)
                && Objects.equals(this.startTicks, mediaSegmentDto.startTicks)
                && Objects.equals(this.endTicks, mediaSegmentDto.endTicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemId, type, startTicks, endTicks);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaSegmentDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    startTicks: ").append(toIndentedString(startTicks)).append("\n");
        sb.append("    endTicks: ").append(toIndentedString(endTicks)).append("\n");
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
