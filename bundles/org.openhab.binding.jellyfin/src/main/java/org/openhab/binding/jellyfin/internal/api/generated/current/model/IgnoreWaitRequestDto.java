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
 * Class IgnoreWaitRequestDto.
 */
@JsonPropertyOrder({ IgnoreWaitRequestDto.JSON_PROPERTY_IGNORE_WAIT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class IgnoreWaitRequestDto {
    public static final String JSON_PROPERTY_IGNORE_WAIT = "IgnoreWait";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean ignoreWait;

    public IgnoreWaitRequestDto() {
    }

    public IgnoreWaitRequestDto ignoreWait(@org.eclipse.jdt.annotation.NonNull Boolean ignoreWait) {
        this.ignoreWait = ignoreWait;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the client should be ignored.
     * 
     * @return ignoreWait
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IGNORE_WAIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIgnoreWait() {
        return ignoreWait;
    }

    @JsonProperty(JSON_PROPERTY_IGNORE_WAIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreWait(@org.eclipse.jdt.annotation.NonNull Boolean ignoreWait) {
        this.ignoreWait = ignoreWait;
    }

    /**
     * Return true if this IgnoreWaitRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IgnoreWaitRequestDto ignoreWaitRequestDto = (IgnoreWaitRequestDto) o;
        return Objects.equals(this.ignoreWait, ignoreWaitRequestDto.ignoreWait);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoreWait);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IgnoreWaitRequestDto {\n");
        sb.append("    ignoreWait: ").append(toIndentedString(ignoreWait)).append("\n");
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
