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
 * Class NewGroupRequestDto.
 */
@JsonPropertyOrder({ NewGroupRequestDto.JSON_PROPERTY_GROUP_NAME })

public class NewGroupRequestDto {
    public static final String JSON_PROPERTY_GROUP_NAME = "GroupName";
    @org.eclipse.jdt.annotation.NonNull
    private String groupName;

    public NewGroupRequestDto() {
    }

    public NewGroupRequestDto groupName(@org.eclipse.jdt.annotation.NonNull String groupName) {
        this.groupName = groupName;
        return this;
    }

    /**
     * Gets or sets the group name.
     * 
     * @return groupName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GROUP_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getGroupName() {
        return groupName;
    }

    @JsonProperty(JSON_PROPERTY_GROUP_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGroupName(@org.eclipse.jdt.annotation.NonNull String groupName) {
        this.groupName = groupName;
    }

    /**
     * Return true if this NewGroupRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NewGroupRequestDto newGroupRequestDto = (NewGroupRequestDto) o;
        return Objects.equals(this.groupName, newGroupRequestDto.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NewGroupRequestDto {\n");
        sb.append("    groupName: ").append(toIndentedString(groupName)).append("\n");
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
