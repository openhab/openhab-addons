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
 * The media update info path.
 */
@JsonPropertyOrder({ MediaUpdateInfoPathDto.JSON_PROPERTY_PATH, MediaUpdateInfoPathDto.JSON_PROPERTY_UPDATE_TYPE })

public class MediaUpdateInfoPathDto {
    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_UPDATE_TYPE = "UpdateType";
    @org.eclipse.jdt.annotation.NonNull
    private String updateType;

    public MediaUpdateInfoPathDto() {
    }

    public MediaUpdateInfoPathDto path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets media path.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPath() {
        return path;
    }

    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public MediaUpdateInfoPathDto updateType(@org.eclipse.jdt.annotation.NonNull String updateType) {
        this.updateType = updateType;
        return this;
    }

    /**
     * Gets or sets media update type. Created, Modified, Deleted.
     * 
     * @return updateType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_UPDATE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUpdateType() {
        return updateType;
    }

    @JsonProperty(JSON_PROPERTY_UPDATE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUpdateType(@org.eclipse.jdt.annotation.NonNull String updateType) {
        this.updateType = updateType;
    }

    /**
     * Return true if this MediaUpdateInfoPathDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaUpdateInfoPathDto mediaUpdateInfoPathDto = (MediaUpdateInfoPathDto) o;
        return Objects.equals(this.path, mediaUpdateInfoPathDto.path)
                && Objects.equals(this.updateType, mediaUpdateInfoPathDto.updateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, updateType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaUpdateInfoPathDto {\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    updateType: ").append(toIndentedString(updateType)).append("\n");
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
