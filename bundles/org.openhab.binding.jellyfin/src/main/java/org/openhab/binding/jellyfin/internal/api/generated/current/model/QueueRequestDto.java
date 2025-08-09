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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class QueueRequestDto.
 */
@JsonPropertyOrder({ QueueRequestDto.JSON_PROPERTY_ITEM_IDS, QueueRequestDto.JSON_PROPERTY_MODE })

public class QueueRequestDto {
    public static final String JSON_PROPERTY_ITEM_IDS = "ItemIds";
    @org.eclipse.jdt.annotation.NonNull
    private List<UUID> itemIds = new ArrayList<>();

    public static final String JSON_PROPERTY_MODE = "Mode";
    @org.eclipse.jdt.annotation.NonNull
    private GroupQueueMode mode;

    public QueueRequestDto() {
    }

    public QueueRequestDto itemIds(@org.eclipse.jdt.annotation.NonNull List<UUID> itemIds) {
        this.itemIds = itemIds;
        return this;
    }

    public QueueRequestDto addItemIdsItem(UUID itemIdsItem) {
        if (this.itemIds == null) {
            this.itemIds = new ArrayList<>();
        }
        this.itemIds.add(itemIdsItem);
        return this;
    }

    /**
     * Gets or sets the items to enqueue.
     * 
     * @return itemIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEM_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UUID> getItemIds() {
        return itemIds;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemIds(@org.eclipse.jdt.annotation.NonNull List<UUID> itemIds) {
        this.itemIds = itemIds;
    }

    public QueueRequestDto mode(@org.eclipse.jdt.annotation.NonNull GroupQueueMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Enum GroupQueueMode.
     * 
     * @return mode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GroupQueueMode getMode() {
        return mode;
    }

    @JsonProperty(JSON_PROPERTY_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMode(@org.eclipse.jdt.annotation.NonNull GroupQueueMode mode) {
        this.mode = mode;
    }

    /**
     * Return true if this QueueRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueueRequestDto queueRequestDto = (QueueRequestDto) o;
        return Objects.equals(this.itemIds, queueRequestDto.itemIds) && Objects.equals(this.mode, queueRequestDto.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemIds, mode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QueueRequestDto {\n");
        sb.append("    itemIds: ").append(toIndentedString(itemIds)).append("\n");
        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
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
