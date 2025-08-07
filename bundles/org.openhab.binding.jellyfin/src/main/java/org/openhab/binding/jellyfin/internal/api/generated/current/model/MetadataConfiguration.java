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
 * MetadataConfiguration
 */
@JsonPropertyOrder({ MetadataConfiguration.JSON_PROPERTY_USE_FILE_CREATION_TIME_FOR_DATE_ADDED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MetadataConfiguration {
    public static final String JSON_PROPERTY_USE_FILE_CREATION_TIME_FOR_DATE_ADDED = "UseFileCreationTimeForDateAdded";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean useFileCreationTimeForDateAdded;

    public MetadataConfiguration() {
    }

    public MetadataConfiguration useFileCreationTimeForDateAdded(
            @org.eclipse.jdt.annotation.NonNull Boolean useFileCreationTimeForDateAdded) {
        this.useFileCreationTimeForDateAdded = useFileCreationTimeForDateAdded;
        return this;
    }

    /**
     * Get useFileCreationTimeForDateAdded
     * 
     * @return useFileCreationTimeForDateAdded
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USE_FILE_CREATION_TIME_FOR_DATE_ADDED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getUseFileCreationTimeForDateAdded() {
        return useFileCreationTimeForDateAdded;
    }

    @JsonProperty(JSON_PROPERTY_USE_FILE_CREATION_TIME_FOR_DATE_ADDED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUseFileCreationTimeForDateAdded(
            @org.eclipse.jdt.annotation.NonNull Boolean useFileCreationTimeForDateAdded) {
        this.useFileCreationTimeForDateAdded = useFileCreationTimeForDateAdded;
    }

    /**
     * Return true if this MetadataConfiguration object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetadataConfiguration metadataConfiguration = (MetadataConfiguration) o;
        return Objects.equals(this.useFileCreationTimeForDateAdded,
                metadataConfiguration.useFileCreationTimeForDateAdded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(useFileCreationTimeForDateAdded);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MetadataConfiguration {\n");
        sb.append("    useFileCreationTimeForDateAdded: ").append(toIndentedString(useFileCreationTimeForDateAdded))
                .append("\n");
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
