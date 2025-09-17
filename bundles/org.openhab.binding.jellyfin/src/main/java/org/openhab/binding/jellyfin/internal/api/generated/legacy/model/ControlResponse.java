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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ControlResponse
 */
@JsonPropertyOrder({ ControlResponse.JSON_PROPERTY_HEADERS, ControlResponse.JSON_PROPERTY_XML,
        ControlResponse.JSON_PROPERTY_IS_SUCCESSFUL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ControlResponse {
    public static final String JSON_PROPERTY_HEADERS = "Headers";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> headers = new HashMap<>();

    public static final String JSON_PROPERTY_XML = "Xml";
    @org.eclipse.jdt.annotation.NonNull
    private String xml;

    public static final String JSON_PROPERTY_IS_SUCCESSFUL = "IsSuccessful";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isSuccessful;

    public ControlResponse() {
    }

    @JsonCreator
    public ControlResponse(@JsonProperty(JSON_PROPERTY_HEADERS) Map<String, String> headers) {
        this();
        this.headers = headers;
    }

    /**
     * Get headers
     * 
     * @return headers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HEADERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getHeaders() {
        return headers;
    }

    public ControlResponse xml(@org.eclipse.jdt.annotation.NonNull String xml) {
        this.xml = xml;
        return this;
    }

    /**
     * Get xml
     * 
     * @return xml
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_XML)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getXml() {
        return xml;
    }

    @JsonProperty(JSON_PROPERTY_XML)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setXml(@org.eclipse.jdt.annotation.NonNull String xml) {
        this.xml = xml;
    }

    public ControlResponse isSuccessful(@org.eclipse.jdt.annotation.NonNull Boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
        return this;
    }

    /**
     * Get isSuccessful
     * 
     * @return isSuccessful
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_SUCCESSFUL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsSuccessful() {
        return isSuccessful;
    }

    @JsonProperty(JSON_PROPERTY_IS_SUCCESSFUL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsSuccessful(@org.eclipse.jdt.annotation.NonNull Boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    /**
     * Return true if this ControlResponse object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ControlResponse controlResponse = (ControlResponse) o;
        return Objects.equals(this.headers, controlResponse.headers) && Objects.equals(this.xml, controlResponse.xml)
                && Objects.equals(this.isSuccessful, controlResponse.isSuccessful);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, xml, isSuccessful);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControlResponse {\n");
        sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
        sb.append("    xml: ").append(toIndentedString(xml)).append("\n");
        sb.append("    isSuccessful: ").append(toIndentedString(isSuccessful)).append("\n");
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

        // add `Headers` to the URL query string
        if (getHeaders() != null) {
            for (String _key : getHeaders().keySet()) {
                joiner.add(String.format("%sHeaders%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, _key, containerSuffix),
                        getHeaders().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getHeaders().get(_key)))));
            }
        }

        // add `Xml` to the URL query string
        if (getXml() != null) {
            joiner.add(String.format("%sXml%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getXml()))));
        }

        // add `IsSuccessful` to the URL query string
        if (getIsSuccessful() != null) {
            joiner.add(String.format("%sIsSuccessful%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsSuccessful()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ControlResponse instance;

        public Builder() {
            this(new ControlResponse());
        }

        protected Builder(ControlResponse instance) {
            this.instance = instance;
        }

        public ControlResponse.Builder headers(Map<String, String> headers) {
            this.instance.headers = headers;
            return this;
        }

        public ControlResponse.Builder xml(String xml) {
            this.instance.xml = xml;
            return this;
        }

        public ControlResponse.Builder isSuccessful(Boolean isSuccessful) {
            this.instance.isSuccessful = isSuccessful;
            return this;
        }

        /**
         * returns a built ControlResponse instance.
         *
         * The builder is not reusable.
         */
        public ControlResponse build() {
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
    public static ControlResponse.Builder builder() {
        return new ControlResponse.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ControlResponse.Builder toBuilder() {
        return new ControlResponse.Builder().headers(getHeaders()).xml(getXml()).isSuccessful(getIsSuccessful());
    }
}
