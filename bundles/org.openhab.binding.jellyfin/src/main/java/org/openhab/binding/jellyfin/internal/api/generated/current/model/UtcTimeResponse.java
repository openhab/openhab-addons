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
 * Class UtcTimeResponse.
 */
@JsonPropertyOrder({ UtcTimeResponse.JSON_PROPERTY_REQUEST_RECEPTION_TIME,
        UtcTimeResponse.JSON_PROPERTY_RESPONSE_TRANSMISSION_TIME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UtcTimeResponse {
    public static final String JSON_PROPERTY_REQUEST_RECEPTION_TIME = "RequestReceptionTime";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime requestReceptionTime;

    public static final String JSON_PROPERTY_RESPONSE_TRANSMISSION_TIME = "ResponseTransmissionTime";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime responseTransmissionTime;

    public UtcTimeResponse() {
    }

    public UtcTimeResponse requestReceptionTime(
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime requestReceptionTime) {
        this.requestReceptionTime = requestReceptionTime;
        return this;
    }

    /**
     * Gets the UTC time when request has been received.
     * 
     * @return requestReceptionTime
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REQUEST_RECEPTION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getRequestReceptionTime() {
        return requestReceptionTime;
    }

    @JsonProperty(JSON_PROPERTY_REQUEST_RECEPTION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequestReceptionTime(@org.eclipse.jdt.annotation.NonNull OffsetDateTime requestReceptionTime) {
        this.requestReceptionTime = requestReceptionTime;
    }

    public UtcTimeResponse responseTransmissionTime(
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime responseTransmissionTime) {
        this.responseTransmissionTime = responseTransmissionTime;
        return this;
    }

    /**
     * Gets the UTC time when response has been sent.
     * 
     * @return responseTransmissionTime
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RESPONSE_TRANSMISSION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getResponseTransmissionTime() {
        return responseTransmissionTime;
    }

    @JsonProperty(JSON_PROPERTY_RESPONSE_TRANSMISSION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResponseTransmissionTime(
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime responseTransmissionTime) {
        this.responseTransmissionTime = responseTransmissionTime;
    }

    /**
     * Return true if this UtcTimeResponse object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UtcTimeResponse utcTimeResponse = (UtcTimeResponse) o;
        return Objects.equals(this.requestReceptionTime, utcTimeResponse.requestReceptionTime)
                && Objects.equals(this.responseTransmissionTime, utcTimeResponse.responseTransmissionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestReceptionTime, responseTransmissionTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UtcTimeResponse {\n");
        sb.append("    requestReceptionTime: ").append(toIndentedString(requestReceptionTime)).append("\n");
        sb.append("    responseTransmissionTime: ").append(toIndentedString(responseTransmissionTime)).append("\n");
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
