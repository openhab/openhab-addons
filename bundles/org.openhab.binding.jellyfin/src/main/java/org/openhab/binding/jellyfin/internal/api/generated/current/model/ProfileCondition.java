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
 * ProfileCondition
 */
@JsonPropertyOrder({ ProfileCondition.JSON_PROPERTY_CONDITION, ProfileCondition.JSON_PROPERTY_PROPERTY,
        ProfileCondition.JSON_PROPERTY_VALUE, ProfileCondition.JSON_PROPERTY_IS_REQUIRED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ProfileCondition {
    public static final String JSON_PROPERTY_CONDITION = "Condition";
    @org.eclipse.jdt.annotation.NonNull
    private ProfileConditionType condition;

    public static final String JSON_PROPERTY_PROPERTY = "Property";
    @org.eclipse.jdt.annotation.NonNull
    private ProfileConditionValue property;

    public static final String JSON_PROPERTY_VALUE = "Value";
    @org.eclipse.jdt.annotation.NonNull
    private String value;

    public static final String JSON_PROPERTY_IS_REQUIRED = "IsRequired";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isRequired;

    public ProfileCondition() {
    }

    public ProfileCondition condition(@org.eclipse.jdt.annotation.NonNull ProfileConditionType condition) {
        this.condition = condition;
        return this;
    }

    /**
     * Get condition
     * 
     * @return condition
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONDITION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ProfileConditionType getCondition() {
        return condition;
    }

    @JsonProperty(JSON_PROPERTY_CONDITION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCondition(@org.eclipse.jdt.annotation.NonNull ProfileConditionType condition) {
        this.condition = condition;
    }

    public ProfileCondition property(@org.eclipse.jdt.annotation.NonNull ProfileConditionValue property) {
        this.property = property;
        return this;
    }

    /**
     * Get property
     * 
     * @return property
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROPERTY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ProfileConditionValue getProperty() {
        return property;
    }

    @JsonProperty(JSON_PROPERTY_PROPERTY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProperty(@org.eclipse.jdt.annotation.NonNull ProfileConditionValue property) {
        this.property = property;
    }

    public ProfileCondition value(@org.eclipse.jdt.annotation.NonNull String value) {
        this.value = value;
        return this;
    }

    /**
     * Get value
     * 
     * @return value
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getValue() {
        return value;
    }

    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValue(@org.eclipse.jdt.annotation.NonNull String value) {
        this.value = value;
    }

    public ProfileCondition isRequired(@org.eclipse.jdt.annotation.NonNull Boolean isRequired) {
        this.isRequired = isRequired;
        return this;
    }

    /**
     * Get isRequired
     * 
     * @return isRequired
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_REQUIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsRequired() {
        return isRequired;
    }

    @JsonProperty(JSON_PROPERTY_IS_REQUIRED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsRequired(@org.eclipse.jdt.annotation.NonNull Boolean isRequired) {
        this.isRequired = isRequired;
    }

    /**
     * Return true if this ProfileCondition object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProfileCondition profileCondition = (ProfileCondition) o;
        return Objects.equals(this.condition, profileCondition.condition)
                && Objects.equals(this.property, profileCondition.property)
                && Objects.equals(this.value, profileCondition.value)
                && Objects.equals(this.isRequired, profileCondition.isRequired);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, property, value, isRequired);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProfileCondition {\n");
        sb.append("    condition: ").append(toIndentedString(condition)).append("\n");
        sb.append("    property: ").append(toIndentedString(property)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    isRequired: ").append(toIndentedString(isRequired)).append("\n");
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
