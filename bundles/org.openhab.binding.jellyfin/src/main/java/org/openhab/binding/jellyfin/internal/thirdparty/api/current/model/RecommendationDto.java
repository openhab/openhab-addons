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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * RecommendationDto
 */
@JsonPropertyOrder({ RecommendationDto.JSON_PROPERTY_ITEMS, RecommendationDto.JSON_PROPERTY_RECOMMENDATION_TYPE,
        RecommendationDto.JSON_PROPERTY_BASELINE_ITEM_NAME, RecommendationDto.JSON_PROPERTY_CATEGORY_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RecommendationDto {
    public static final String JSON_PROPERTY_ITEMS = "Items";
    @org.eclipse.jdt.annotation.Nullable
    private List<BaseItemDto> items;

    public static final String JSON_PROPERTY_RECOMMENDATION_TYPE = "RecommendationType";
    @org.eclipse.jdt.annotation.Nullable
    private RecommendationType recommendationType;

    public static final String JSON_PROPERTY_BASELINE_ITEM_NAME = "BaselineItemName";
    @org.eclipse.jdt.annotation.Nullable
    private String baselineItemName;

    public static final String JSON_PROPERTY_CATEGORY_ID = "CategoryId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID categoryId;

    public RecommendationDto() {
    }

    public RecommendationDto items(@org.eclipse.jdt.annotation.Nullable List<BaseItemDto> items) {
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<BaseItemDto> getItems() {
        return items;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItems(@org.eclipse.jdt.annotation.Nullable List<BaseItemDto> items) {
        this.items = items;
    }

    public RecommendationDto recommendationType(
            @org.eclipse.jdt.annotation.Nullable RecommendationType recommendationType) {
        this.recommendationType = recommendationType;
        return this;
    }

    /**
     * Get recommendationType
     * 
     * @return recommendationType
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_RECOMMENDATION_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public RecommendationType getRecommendationType() {
        return recommendationType;
    }

    @JsonProperty(value = JSON_PROPERTY_RECOMMENDATION_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecommendationType(@org.eclipse.jdt.annotation.Nullable RecommendationType recommendationType) {
        this.recommendationType = recommendationType;
    }

    public RecommendationDto baselineItemName(@org.eclipse.jdt.annotation.Nullable String baselineItemName) {
        this.baselineItemName = baselineItemName;
        return this;
    }

    /**
     * Get baselineItemName
     * 
     * @return baselineItemName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BASELINE_ITEM_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getBaselineItemName() {
        return baselineItemName;
    }

    @JsonProperty(value = JSON_PROPERTY_BASELINE_ITEM_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBaselineItemName(@org.eclipse.jdt.annotation.Nullable String baselineItemName) {
        this.baselineItemName = baselineItemName;
    }

    public RecommendationDto categoryId(@org.eclipse.jdt.annotation.Nullable UUID categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    /**
     * Get categoryId
     * 
     * @return categoryId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CATEGORY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getCategoryId() {
        return categoryId;
    }

    @JsonProperty(value = JSON_PROPERTY_CATEGORY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCategoryId(@org.eclipse.jdt.annotation.Nullable UUID categoryId) {
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

        // add `Items` to the URL query string
        if (getItems() != null) {
            for (int i = 0; i < getItems().size(); i++) {
                if (getItems().get(i) != null) {
                    joiner.add(getItems().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sItems%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `RecommendationType` to the URL query string
        if (getRecommendationType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRecommendationType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRecommendationType()))));
        }

        // add `BaselineItemName` to the URL query string
        if (getBaselineItemName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBaselineItemName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBaselineItemName()))));
        }

        // add `CategoryId` to the URL query string
        if (getCategoryId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCategoryId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCategoryId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private RecommendationDto instance;

        public Builder() {
            this(new RecommendationDto());
        }

        protected Builder(RecommendationDto instance) {
            this.instance = instance;
        }

        public RecommendationDto.Builder items(List<BaseItemDto> items) {
            this.instance.items = items;
            return this;
        }

        public RecommendationDto.Builder recommendationType(RecommendationType recommendationType) {
            this.instance.recommendationType = recommendationType;
            return this;
        }

        public RecommendationDto.Builder baselineItemName(String baselineItemName) {
            this.instance.baselineItemName = baselineItemName;
            return this;
        }

        public RecommendationDto.Builder categoryId(UUID categoryId) {
            this.instance.categoryId = categoryId;
            return this;
        }

        /**
         * returns a built RecommendationDto instance.
         *
         * The builder is not reusable.
         */
        public RecommendationDto build() {
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
    public static RecommendationDto.Builder builder() {
        return new RecommendationDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public RecommendationDto.Builder toBuilder() {
        return new RecommendationDto.Builder().items(getItems()).recommendationType(getRecommendationType())
                .baselineItemName(getBaselineItemName()).categoryId(getCategoryId());
    }
}
