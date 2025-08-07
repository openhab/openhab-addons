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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ProblemDetails
 */
@JsonPropertyOrder({ ProblemDetails.JSON_PROPERTY_TYPE, ProblemDetails.JSON_PROPERTY_TITLE,
        ProblemDetails.JSON_PROPERTY_STATUS, ProblemDetails.JSON_PROPERTY_DETAIL,
        ProblemDetails.JSON_PROPERTY_INSTANCE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ProblemDetails {
    public static final String JSON_PROPERTY_TYPE = "type";
    @org.eclipse.jdt.annotation.NonNull
    private String type;

    public static final String JSON_PROPERTY_TITLE = "title";
    @org.eclipse.jdt.annotation.NonNull
    private String title;

    public static final String JSON_PROPERTY_STATUS = "status";
    @org.eclipse.jdt.annotation.NonNull
    private Integer status;

    public static final String JSON_PROPERTY_DETAIL = "detail";
    @org.eclipse.jdt.annotation.NonNull
    private String detail;

    public static final String JSON_PROPERTY_INSTANCE = "instance";
    @org.eclipse.jdt.annotation.NonNull
    private String instance;

    public ProblemDetails() {
    }

    public ProblemDetails type(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
    }

    public ProblemDetails title(@org.eclipse.jdt.annotation.NonNull String title) {
        this.title = title;
        return this;
    }

    /**
     * Get title
     * 
     * @return title
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TITLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTitle() {
        return title;
    }

    @JsonProperty(JSON_PROPERTY_TITLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTitle(@org.eclipse.jdt.annotation.NonNull String title) {
        this.title = title;
    }

    public ProblemDetails status(@org.eclipse.jdt.annotation.NonNull Integer status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     * 
     * @return status
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getStatus() {
        return status;
    }

    @JsonProperty(JSON_PROPERTY_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatus(@org.eclipse.jdt.annotation.NonNull Integer status) {
        this.status = status;
    }

    public ProblemDetails detail(@org.eclipse.jdt.annotation.NonNull String detail) {
        this.detail = detail;
        return this;
    }

    /**
     * Get detail
     * 
     * @return detail
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DETAIL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDetail() {
        return detail;
    }

    @JsonProperty(JSON_PROPERTY_DETAIL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDetail(@org.eclipse.jdt.annotation.NonNull String detail) {
        this.detail = detail;
    }

    public ProblemDetails instance(@org.eclipse.jdt.annotation.NonNull String instance) {
        this.instance = instance;
        return this;
    }

    /**
     * Get instance
     * 
     * @return instance
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_INSTANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getInstance() {
        return instance;
    }

    @JsonProperty(JSON_PROPERTY_INSTANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInstance(@org.eclipse.jdt.annotation.NonNull String instance) {
        this.instance = instance;
    }

    /**
     * A container for additional, undeclared properties.
     * This is a holder for any undeclared properties as specified with
     * the 'additionalProperties' keyword in the OAS document.
     */
    private Map<String, Object> additionalProperties;

    /**
     * Set the additional (undeclared) property with the specified name and value.
     * If the property does not already exist, create it otherwise replace it.
     */
    @JsonAnySetter
    public ProblemDetails putAdditionalProperty(String key, Object value) {
        if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap<>();
        }
        this.additionalProperties.put(key, value);
        return this;
    }

    /**
     * Return the additional (undeclared) property.
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Return the additional (undeclared) property with the specified name.
     */
    public Object getAdditionalProperty(String key) {
        if (this.additionalProperties == null) {
            return null;
        }
        return this.additionalProperties.get(key);
    }

    /**
     * Return true if this ProblemDetails object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProblemDetails problemDetails = (ProblemDetails) o;
        return Objects.equals(this.type, problemDetails.type) && Objects.equals(this.title, problemDetails.title)
                && Objects.equals(this.status, problemDetails.status)
                && Objects.equals(this.detail, problemDetails.detail)
                && Objects.equals(this.instance, problemDetails.instance)
                && Objects.equals(this.additionalProperties, problemDetails.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, status, detail, instance, additionalProperties);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProblemDetails {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    title: ").append(toIndentedString(title)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
        sb.append("    instance: ").append(toIndentedString(instance)).append("\n");
        sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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
