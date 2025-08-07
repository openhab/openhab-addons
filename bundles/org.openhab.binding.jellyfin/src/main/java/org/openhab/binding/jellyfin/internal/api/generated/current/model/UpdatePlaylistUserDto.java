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
 * Update existing playlist user dto. Fields set to &#x60;null&#x60; will not be updated and keep their current values.
 */
@JsonPropertyOrder({ UpdatePlaylistUserDto.JSON_PROPERTY_CAN_EDIT })

public class UpdatePlaylistUserDto {
    public static final String JSON_PROPERTY_CAN_EDIT = "CanEdit";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canEdit;

    public UpdatePlaylistUserDto() {
    }

    public UpdatePlaylistUserDto canEdit(@org.eclipse.jdt.annotation.NonNull Boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the user can edit the playlist.
     * 
     * @return canEdit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CAN_EDIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getCanEdit() {
        return canEdit;
    }

    @JsonProperty(JSON_PROPERTY_CAN_EDIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanEdit(@org.eclipse.jdt.annotation.NonNull Boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * Return true if this UpdatePlaylistUserDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdatePlaylistUserDto updatePlaylistUserDto = (UpdatePlaylistUserDto) o;
        return Objects.equals(this.canEdit, updatePlaylistUserDto.canEdit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canEdit);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdatePlaylistUserDto {\n");
        sb.append("    canEdit: ").append(toIndentedString(canEdit)).append("\n");
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
