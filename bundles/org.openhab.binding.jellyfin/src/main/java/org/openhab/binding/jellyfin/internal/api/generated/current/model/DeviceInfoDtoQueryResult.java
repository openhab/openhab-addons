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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Query result container.
 */
@JsonPropertyOrder({ DeviceInfoDtoQueryResult.JSON_PROPERTY_ITEMS,
        DeviceInfoDtoQueryResult.JSON_PROPERTY_TOTAL_RECORD_COUNT, DeviceInfoDtoQueryResult.JSON_PROPERTY_START_INDEX })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DeviceInfoDtoQueryResult {
    public static final String JSON_PROPERTY_ITEMS = "Items";
    @org.eclipse.jdt.annotation.NonNull
    private List<DeviceInfoDto> items = new ArrayList<>();

    public static final String JSON_PROPERTY_TOTAL_RECORD_COUNT = "TotalRecordCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer totalRecordCount;

    public static final String JSON_PROPERTY_START_INDEX = "StartIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer startIndex;

    public DeviceInfoDtoQueryResult() {
    }

    public DeviceInfoDtoQueryResult items(@org.eclipse.jdt.annotation.NonNull List<DeviceInfoDto> items) {
        this.items = items;
        return this;
    }

    public DeviceInfoDtoQueryResult addItemsItem(DeviceInfoDto itemsItem) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(itemsItem);
        return this;
    }

    /**
     * Gets or sets the items.
     * 
     * @return items
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<DeviceInfoDto> getItems() {
        return items;
    }

    @JsonProperty(JSON_PROPERTY_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItems(@org.eclipse.jdt.annotation.NonNull List<DeviceInfoDto> items) {
        this.items = items;
    }

    public DeviceInfoDtoQueryResult totalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
        return this;
    }

    /**
     * Gets or sets the total number of records available.
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

    public DeviceInfoDtoQueryResult startIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Gets or sets the index of the first record in Items.
     * 
     * @return startIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getStartIndex() {
        return startIndex;
    }

    @JsonProperty(JSON_PROPERTY_START_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Return true if this DeviceInfoDtoQueryResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceInfoDtoQueryResult deviceInfoDtoQueryResult = (DeviceInfoDtoQueryResult) o;
        return Objects.equals(this.items, deviceInfoDtoQueryResult.items)
                && Objects.equals(this.totalRecordCount, deviceInfoDtoQueryResult.totalRecordCount)
                && Objects.equals(this.startIndex, deviceInfoDtoQueryResult.startIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, totalRecordCount, startIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeviceInfoDtoQueryResult {\n");
        sb.append("    items: ").append(toIndentedString(items)).append("\n");
        sb.append("    totalRecordCount: ").append(toIndentedString(totalRecordCount)).append("\n");
        sb.append("    startIndex: ").append(toIndentedString(startIndex)).append("\n");
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
