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
 * Client log document response dto.
 */
@JsonPropertyOrder({ ClientLogDocumentResponseDto.JSON_PROPERTY_FILE_NAME })

public class ClientLogDocumentResponseDto {
    public static final String JSON_PROPERTY_FILE_NAME = "FileName";
    @org.eclipse.jdt.annotation.NonNull
    private String fileName;

    public ClientLogDocumentResponseDto() {
    }

    public ClientLogDocumentResponseDto fileName(@org.eclipse.jdt.annotation.NonNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Gets the resulting filename.
     * 
     * @return fileName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FILE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFileName() {
        return fileName;
    }

    @JsonProperty(JSON_PROPERTY_FILE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFileName(@org.eclipse.jdt.annotation.NonNull String fileName) {
        this.fileName = fileName;
    }

    /**
     * Return true if this ClientLogDocumentResponseDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientLogDocumentResponseDto clientLogDocumentResponseDto = (ClientLogDocumentResponseDto) o;
        return Objects.equals(this.fileName, clientLogDocumentResponseDto.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClientLogDocumentResponseDto {\n");
        sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
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
