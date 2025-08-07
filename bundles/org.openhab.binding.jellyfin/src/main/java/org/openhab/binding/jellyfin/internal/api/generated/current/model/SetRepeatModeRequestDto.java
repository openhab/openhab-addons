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
 * Class SetRepeatModeRequestDto.
 */
@JsonPropertyOrder({ SetRepeatModeRequestDto.JSON_PROPERTY_MODE })

public class SetRepeatModeRequestDto {
    public static final String JSON_PROPERTY_MODE = "Mode";
    @org.eclipse.jdt.annotation.NonNull
    private GroupRepeatMode mode;

    public SetRepeatModeRequestDto() {
    }

    public SetRepeatModeRequestDto mode(@org.eclipse.jdt.annotation.NonNull GroupRepeatMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Enum GroupRepeatMode.
     * 
     * @return mode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GroupRepeatMode getMode() {
        return mode;
    }

    @JsonProperty(JSON_PROPERTY_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMode(@org.eclipse.jdt.annotation.NonNull GroupRepeatMode mode) {
        this.mode = mode;
    }

    /**
     * Return true if this SetRepeatModeRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetRepeatModeRequestDto setRepeatModeRequestDto = (SetRepeatModeRequestDto) o;
        return Objects.equals(this.mode, setRepeatModeRequestDto.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SetRepeatModeRequestDto {\n");
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
