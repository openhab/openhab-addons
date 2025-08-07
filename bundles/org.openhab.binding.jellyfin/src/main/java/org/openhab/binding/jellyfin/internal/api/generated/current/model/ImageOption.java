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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ImageOption
 */
@JsonPropertyOrder({ ImageOption.JSON_PROPERTY_TYPE, ImageOption.JSON_PROPERTY_LIMIT,
        ImageOption.JSON_PROPERTY_MIN_WIDTH })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ImageOption {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private ImageType type;

    public static final String JSON_PROPERTY_LIMIT = "Limit";
    @org.eclipse.jdt.annotation.NonNull
    private Integer limit;

    public static final String JSON_PROPERTY_MIN_WIDTH = "MinWidth";
    @org.eclipse.jdt.annotation.NonNull
    private Integer minWidth;

    public ImageOption() {
    }

    public ImageOption type(@org.eclipse.jdt.annotation.NonNull ImageType type) {
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

    public ImageType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull ImageType type) {
        this.type = type;
    }

    public ImageOption limit(@org.eclipse.jdt.annotation.NonNull Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Gets or sets the limit.
     * 
     * @return limit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getLimit() {
        return limit;
    }

    @JsonProperty(JSON_PROPERTY_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLimit(@org.eclipse.jdt.annotation.NonNull Integer limit) {
        this.limit = limit;
    }

    public ImageOption minWidth(@org.eclipse.jdt.annotation.NonNull Integer minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    /**
     * Gets or sets the minimum width.
     * 
     * @return minWidth
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MIN_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMinWidth() {
        return minWidth;
    }

    @JsonProperty(JSON_PROPERTY_MIN_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinWidth(@org.eclipse.jdt.annotation.NonNull Integer minWidth) {
        this.minWidth = minWidth;
    }

    /**
     * Return true if this ImageOption object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageOption imageOption = (ImageOption) o;
        return Objects.equals(this.type, imageOption.type) && Objects.equals(this.limit, imageOption.limit)
                && Objects.equals(this.minWidth, imageOption.minWidth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, limit, minWidth);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ImageOption {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    minWidth: ").append(toIndentedString(minWidth)).append("\n");
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
