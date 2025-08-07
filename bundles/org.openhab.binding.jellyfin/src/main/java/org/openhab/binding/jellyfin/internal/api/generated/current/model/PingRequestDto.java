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
 * Class PingRequestDto.
 */
@JsonPropertyOrder({ PingRequestDto.JSON_PROPERTY_PING })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PingRequestDto {
    public static final String JSON_PROPERTY_PING = "Ping";
    @org.eclipse.jdt.annotation.NonNull
    private Long ping;

    public PingRequestDto() {
    }

    public PingRequestDto ping(@org.eclipse.jdt.annotation.NonNull Long ping) {
        this.ping = ping;
        return this;
    }

    /**
     * Gets or sets the ping time.
     * 
     * @return ping
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getPing() {
        return ping;
    }

    @JsonProperty(JSON_PROPERTY_PING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPing(@org.eclipse.jdt.annotation.NonNull Long ping) {
        this.ping = ping;
    }

    /**
     * Return true if this PingRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PingRequestDto pingRequestDto = (PingRequestDto) o;
        return Objects.equals(this.ping, pingRequestDto.ping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ping);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PingRequestDto {\n");
        sb.append("    ping: ").append(toIndentedString(ping)).append("\n");
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
