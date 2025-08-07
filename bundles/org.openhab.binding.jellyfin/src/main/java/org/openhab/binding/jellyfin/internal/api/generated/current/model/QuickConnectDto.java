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
 * The quick connect request body.
 */
@JsonPropertyOrder({ QuickConnectDto.JSON_PROPERTY_SECRET })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QuickConnectDto {
    public static final String JSON_PROPERTY_SECRET = "Secret";
    @org.eclipse.jdt.annotation.Nullable
    private String secret;

    public QuickConnectDto() {
    }

    public QuickConnectDto secret(@org.eclipse.jdt.annotation.Nullable String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * Gets or sets the quick connect secret.
     * 
     * @return secret
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_SECRET)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSecret() {
        return secret;
    }

    @JsonProperty(JSON_PROPERTY_SECRET)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSecret(@org.eclipse.jdt.annotation.Nullable String secret) {
        this.secret = secret;
    }

    /**
     * Return true if this QuickConnectDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuickConnectDto quickConnectDto = (QuickConnectDto) o;
        return Objects.equals(this.secret, quickConnectDto.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secret);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QuickConnectDto {\n");
        sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
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
