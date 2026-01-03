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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * EndPointInfo
 */
@JsonPropertyOrder({ EndPointInfo.JSON_PROPERTY_IS_LOCAL, EndPointInfo.JSON_PROPERTY_IS_IN_NETWORK })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class EndPointInfo {
    public static final String JSON_PROPERTY_IS_LOCAL = "IsLocal";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isLocal;

    public static final String JSON_PROPERTY_IS_IN_NETWORK = "IsInNetwork";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isInNetwork;

    public EndPointInfo() {
    }

    public EndPointInfo isLocal(@org.eclipse.jdt.annotation.Nullable Boolean isLocal) {
        this.isLocal = isLocal;
        return this;
    }

    /**
     * Get isLocal
     * 
     * @return isLocal
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_LOCAL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsLocal() {
        return isLocal;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_LOCAL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsLocal(@org.eclipse.jdt.annotation.Nullable Boolean isLocal) {
        this.isLocal = isLocal;
    }

    public EndPointInfo isInNetwork(@org.eclipse.jdt.annotation.Nullable Boolean isInNetwork) {
        this.isInNetwork = isInNetwork;
        return this;
    }

    /**
     * Get isInNetwork
     * 
     * @return isInNetwork
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_IN_NETWORK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsInNetwork() {
        return isInNetwork;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_IN_NETWORK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsInNetwork(@org.eclipse.jdt.annotation.Nullable Boolean isInNetwork) {
        this.isInNetwork = isInNetwork;
    }

    /**
     * Return true if this EndPointInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndPointInfo endPointInfo = (EndPointInfo) o;
        return Objects.equals(this.isLocal, endPointInfo.isLocal)
                && Objects.equals(this.isInNetwork, endPointInfo.isInNetwork);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLocal, isInNetwork);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EndPointInfo {\n");
        sb.append("    isLocal: ").append(toIndentedString(isLocal)).append("\n");
        sb.append("    isInNetwork: ").append(toIndentedString(isInNetwork)).append("\n");
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

        // add `IsLocal` to the URL query string
        if (getIsLocal() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsLocal%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsLocal()))));
        }

        // add `IsInNetwork` to the URL query string
        if (getIsInNetwork() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsInNetwork%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsInNetwork()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private EndPointInfo instance;

        public Builder() {
            this(new EndPointInfo());
        }

        protected Builder(EndPointInfo instance) {
            this.instance = instance;
        }

        public EndPointInfo.Builder isLocal(Boolean isLocal) {
            this.instance.isLocal = isLocal;
            return this;
        }

        public EndPointInfo.Builder isInNetwork(Boolean isInNetwork) {
            this.instance.isInNetwork = isInNetwork;
            return this;
        }

        /**
         * returns a built EndPointInfo instance.
         *
         * The builder is not reusable.
         */
        public EndPointInfo build() {
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
    public static EndPointInfo.Builder builder() {
        return new EndPointInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public EndPointInfo.Builder toBuilder() {
        return new EndPointInfo.Builder().isLocal(getIsLocal()).isInNetwork(getIsInNetwork());
    }
}
