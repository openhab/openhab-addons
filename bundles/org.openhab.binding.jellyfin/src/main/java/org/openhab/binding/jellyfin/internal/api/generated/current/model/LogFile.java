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

import java.time.OffsetDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * LogFile
 */
@JsonPropertyOrder({ LogFile.JSON_PROPERTY_DATE_CREATED, LogFile.JSON_PROPERTY_DATE_MODIFIED,
        LogFile.JSON_PROPERTY_SIZE, LogFile.JSON_PROPERTY_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LogFile {
    public static final String JSON_PROPERTY_DATE_CREATED = "DateCreated";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateCreated;

    public static final String JSON_PROPERTY_DATE_MODIFIED = "DateModified";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateModified;

    public static final String JSON_PROPERTY_SIZE = "Size";
    @org.eclipse.jdt.annotation.NonNull
    private Long size;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public LogFile() {
    }

    public LogFile dateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    /**
     * Gets or sets the date created.
     * 
     * @return dateCreated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DATE_CREATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getDateCreated() {
        return dateCreated;
    }

    @JsonProperty(JSON_PROPERTY_DATE_CREATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LogFile dateModified(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateModified) {
        this.dateModified = dateModified;
        return this;
    }

    /**
     * Gets or sets the date modified.
     * 
     * @return dateModified
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DATE_MODIFIED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getDateModified() {
        return dateModified;
    }

    @JsonProperty(JSON_PROPERTY_DATE_MODIFIED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateModified(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateModified) {
        this.dateModified = dateModified;
    }

    public LogFile size(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
        return this;
    }

    /**
     * Gets or sets the size.
     * 
     * @return size
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getSize() {
        return size;
    }

    @JsonProperty(JSON_PROPERTY_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSize(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
    }

    public LogFile name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    /**
     * Return true if this LogFile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogFile logFile = (LogFile) o;
        return Objects.equals(this.dateCreated, logFile.dateCreated)
                && Objects.equals(this.dateModified, logFile.dateModified) && Objects.equals(this.size, logFile.size)
                && Objects.equals(this.name, logFile.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateCreated, dateModified, size, name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LogFile {\n");
        sb.append("    dateCreated: ").append(toIndentedString(dateCreated)).append("\n");
        sb.append("    dateModified: ").append(toIndentedString(dateModified)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
