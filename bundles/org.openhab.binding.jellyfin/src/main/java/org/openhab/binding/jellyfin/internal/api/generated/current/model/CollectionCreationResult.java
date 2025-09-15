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
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * CollectionCreationResult
 */
@JsonPropertyOrder({ CollectionCreationResult.JSON_PROPERTY_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CollectionCreationResult {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public CollectionCreationResult() {
    }

    public CollectionCreationResult id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    /**
     * Return true if this CollectionCreationResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CollectionCreationResult collectionCreationResult = (CollectionCreationResult) o;
        return Objects.equals(this.id, collectionCreationResult.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CollectionCreationResult {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(
                    String.format("%sId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private CollectionCreationResult instance;

        public Builder() {
            this(new CollectionCreationResult());
        }

        protected Builder(CollectionCreationResult instance) {
            this.instance = instance;
        }

        public CollectionCreationResult.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        /**
         * returns a built CollectionCreationResult instance.
         *
         * The builder is not reusable.
         */
        public CollectionCreationResult build() {
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
    public static CollectionCreationResult.Builder builder() {
        return new CollectionCreationResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public CollectionCreationResult.Builder toBuilder() {
        return new CollectionCreationResult.Builder().id(getId());
    }
}
