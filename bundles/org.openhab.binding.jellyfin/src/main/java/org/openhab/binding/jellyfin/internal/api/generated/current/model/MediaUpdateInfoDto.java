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
 * Media Update Info Dto.
 */
@JsonPropertyOrder({ MediaUpdateInfoDto.JSON_PROPERTY_UPDATES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaUpdateInfoDto {
    public static final String JSON_PROPERTY_UPDATES = "Updates";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaUpdateInfoPathDto> updates = new ArrayList<>();

    public MediaUpdateInfoDto() {
    }

    public MediaUpdateInfoDto updates(@org.eclipse.jdt.annotation.NonNull List<MediaUpdateInfoPathDto> updates) {
        this.updates = updates;
        return this;
    }

    public MediaUpdateInfoDto addUpdatesItem(MediaUpdateInfoPathDto updatesItem) {
        if (this.updates == null) {
            this.updates = new ArrayList<>();
        }
        this.updates.add(updatesItem);
        return this;
    }

    /**
     * Gets or sets the list of updates.
     * 
     * @return updates
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_UPDATES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<MediaUpdateInfoPathDto> getUpdates() {
        return updates;
    }

    @JsonProperty(JSON_PROPERTY_UPDATES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUpdates(@org.eclipse.jdt.annotation.NonNull List<MediaUpdateInfoPathDto> updates) {
        this.updates = updates;
    }

    /**
     * Return true if this MediaUpdateInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaUpdateInfoDto mediaUpdateInfoDto = (MediaUpdateInfoDto) o;
        return Objects.equals(this.updates, mediaUpdateInfoDto.updates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(updates);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaUpdateInfoDto {\n");
        sb.append("    updates: ").append(toIndentedString(updates)).append("\n");
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
