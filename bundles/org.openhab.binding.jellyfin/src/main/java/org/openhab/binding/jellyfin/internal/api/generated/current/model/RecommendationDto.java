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
 * RecommendationDto
 */
@JsonPropertyOrder({ RecommendationDto.JSON_PROPERTY_ITEMS, RecommendationDto.JSON_PROPERTY_RECOMMENDATION_TYPE,
        RecommendationDto.JSON_PROPERTY_BASELINE_ITEM_NAME, RecommendationDto.JSON_PROPERTY_CATEGORY_ID })

public class RecommendationDto {
    public static final String JSON_PROPERTY_ITEMS = "Items";
    @org.eclipse.jdt.annotation.NonNull
    private List<BaseItemDto> items;

    public static final String JSON_PROPERTY_RECOMMENDATION_TYPE = "RecommendationType";
    @org.eclipse.jdt.annotation.NonNull
    private RecommendationType recommendationType;

    public static final String JSON_PROPERTY_BASELINE_ITEM_NAME = "BaselineItemName";
    @org.eclipse.jdt.annotation.NonNull
    private String baselineItemName;

    public static final String JSON_PROPERTY_CATEGORY_ID = "CategoryId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID categoryId;

    public RecommendationDto() {
    }

    public RecommendationDto items(@org.eclipse.jdt.annotation.NonNull List<BaseItemDto> items) {
        this.items = items;
        return this;
    }

    public RecommendationDto addItemsItem(BaseItemDto itemsItem) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(itemsItem);
        return this;
    }

    /**
     * Get items
     * 
     * @return items
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<BaseItemDto> getItems() {
        return items;
    }

    @JsonProperty(JSON_PROPERTY_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItems(@org.eclipse.jdt.annotation.NonNull List<BaseItemDto> items) {
        this.items = items;
    }

    public RecommendationDto recommendationType(
            @org.eclipse.jdt.annotation.NonNull RecommendationType recommendationType) {
        this.recommendationType = recommendationType;
        return this;
    }

    /**
     * Get recommendationType
     * 
     * @return recommendationType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RECOMMENDATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public RecommendationType getRecommendationType() {
        return recommendationType;
    }

    @JsonProperty(JSON_PROPERTY_RECOMMENDATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecommendationType(@org.eclipse.jdt.annotation.NonNull RecommendationType recommendationType) {
        this.recommendationType = recommendationType;
    }

    public RecommendationDto baselineItemName(@org.eclipse.jdt.annotation.NonNull String baselineItemName) {
        this.baselineItemName = baselineItemName;
        return this;
    }

    /**
     * Get baselineItemName
     * 
     * @return baselineItemName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BASELINE_ITEM_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBaselineItemName() {
        return baselineItemName;
    }

    @JsonProperty(JSON_PROPERTY_BASELINE_ITEM_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBaselineItemName(@org.eclipse.jdt.annotation.NonNull String baselineItemName) {
        this.baselineItemName = baselineItemName;
    }

    public RecommendationDto categoryId(@org.eclipse.jdt.annotation.NonNull UUID categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    /**
     * Get categoryId
     * 
     * @return categoryId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CATEGORY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getCategoryId() {
        return categoryId;
    }

    @JsonProperty(JSON_PROPERTY_CATEGORY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCategoryId(@org.eclipse.jdt.annotation.NonNull UUID categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Return true if this RecommendationDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecommendationDto recommendationDto = (RecommendationDto) o;
        return Objects.equals(this.items, recommendationDto.items)
                && Objects.equals(this.recommendationType, recommendationDto.recommendationType)
                && Objects.equals(this.baselineItemName, recommendationDto.baselineItemName)
                && Objects.equals(this.categoryId, recommendationDto.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, recommendationType, baselineItemName, categoryId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RecommendationDto {\n");
        sb.append("    items: ").append(toIndentedString(items)).append("\n");
        sb.append("    recommendationType: ").append(toIndentedString(recommendationType)).append("\n");
        sb.append("    baselineItemName: ").append(toIndentedString(baselineItemName)).append("\n");
        sb.append("    categoryId: ").append(toIndentedString(categoryId)).append("\n");
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
