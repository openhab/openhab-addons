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
 * Defines the MediaBrowser.Model.Configuration.PathSubstitution.
 */
@JsonPropertyOrder({ PathSubstitution.JSON_PROPERTY_FROM, PathSubstitution.JSON_PROPERTY_TO })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PathSubstitution {
    public static final String JSON_PROPERTY_FROM = "From";
    @org.eclipse.jdt.annotation.NonNull
    private String from;

    public static final String JSON_PROPERTY_TO = "To";
    @org.eclipse.jdt.annotation.NonNull
    private String to;

    public PathSubstitution() {
    }

    public PathSubstitution from(@org.eclipse.jdt.annotation.NonNull String from) {
        this.from = from;
        return this;
    }

    /**
     * Gets or sets the value to substitute.
     * 
     * @return from
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FROM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFrom() {
        return from;
    }

    @JsonProperty(JSON_PROPERTY_FROM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFrom(@org.eclipse.jdt.annotation.NonNull String from) {
        this.from = from;
    }

    public PathSubstitution to(@org.eclipse.jdt.annotation.NonNull String to) {
        this.to = to;
        return this;
    }

    /**
     * Gets or sets the value to substitution with.
     * 
     * @return to
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTo() {
        return to;
    }

    @JsonProperty(JSON_PROPERTY_TO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTo(@org.eclipse.jdt.annotation.NonNull String to) {
        this.to = to;
    }

    /**
     * Return true if this PathSubstitution object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PathSubstitution pathSubstitution = (PathSubstitution) o;
        return Objects.equals(this.from, pathSubstitution.from) && Objects.equals(this.to, pathSubstitution.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PathSubstitution {\n");
        sb.append("    from: ").append(toIndentedString(from)).append("\n");
        sb.append("    to: ").append(toIndentedString(to)).append("\n");
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
