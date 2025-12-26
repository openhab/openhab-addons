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
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

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
    @JsonProperty(value = JSON_PROPERTY_REQUEST_RECEPTION_TIME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getRequestReceptionTime() {
        return requestReceptionTime;
    }

    @JsonProperty(value = JSON_PROPERTY_REQUEST_RECEPTION_TIME, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_RESPONSE_TRANSMISSION_TIME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getResponseTransmissionTime() {
        return responseTransmissionTime;
    }

    @JsonProperty(value = JSON_PROPERTY_RESPONSE_TRANSMISSION_TIME, required = false)
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

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `RequestReceptionTime` to the URL query string
        if (getRequestReceptionTime() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRequestReceptionTime%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequestReceptionTime()))));
        }

        // add `ResponseTransmissionTime` to the URL query string
        if (getResponseTransmissionTime() != null) {
            joiner.add(String.format(Locale.ROOT, "%sResponseTransmissionTime%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getResponseTransmissionTime()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UtcTimeResponse instance;

        public Builder() {
            this(new UtcTimeResponse());
        }

        protected Builder(UtcTimeResponse instance) {
            this.instance = instance;
        }

        public UtcTimeResponse.Builder requestReceptionTime(OffsetDateTime requestReceptionTime) {
            this.instance.requestReceptionTime = requestReceptionTime;
            return this;
        }

        public UtcTimeResponse.Builder responseTransmissionTime(OffsetDateTime responseTransmissionTime) {
            this.instance.responseTransmissionTime = responseTransmissionTime;
            return this;
        }

        /**
         * returns a built UtcTimeResponse instance.
         *
         * The builder is not reusable.
         */
        public UtcTimeResponse build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static UtcTimeResponse.Builder builder() {
        return new UtcTimeResponse.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UtcTimeResponse.Builder toBuilder() {
        return new UtcTimeResponse.Builder().requestReceptionTime(getRequestReceptionTime())
                .responseTransmissionTime(getResponseTransmissionTime());
    }
}
