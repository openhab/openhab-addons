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
 * ProfileCondition
 */
@JsonPropertyOrder({ ProfileCondition.JSON_PROPERTY_CONDITION, ProfileCondition.JSON_PROPERTY_PROPERTY,
        ProfileCondition.JSON_PROPERTY_VALUE, ProfileCondition.JSON_PROPERTY_IS_REQUIRED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ProfileCondition {
    public static final String JSON_PROPERTY_CONDITION = "Condition";
    @org.eclipse.jdt.annotation.Nullable
    private ProfileConditionType condition;

    public static final String JSON_PROPERTY_PROPERTY = "Property";
    @org.eclipse.jdt.annotation.Nullable
    private ProfileConditionValue property;

    public static final String JSON_PROPERTY_VALUE = "Value";
    @org.eclipse.jdt.annotation.Nullable
    private String value;

    public static final String JSON_PROPERTY_IS_REQUIRED = "IsRequired";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isRequired;

    public ProfileCondition() {
    }

    public ProfileCondition condition(@org.eclipse.jdt.annotation.Nullable ProfileConditionType condition) {
        this.condition = condition;
        return this;
    }

    /**
     * Get condition
     * 
     * @return condition
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONDITION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ProfileConditionType getCondition() {
        return condition;
    }

    @JsonProperty(value = JSON_PROPERTY_CONDITION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCondition(@org.eclipse.jdt.annotation.Nullable ProfileConditionType condition) {
        this.condition = condition;
    }

    public ProfileCondition property(@org.eclipse.jdt.annotation.Nullable ProfileConditionValue property) {
        this.property = property;
        return this;
    }

    /**
     * Get property
     * 
     * @return property
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROPERTY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ProfileConditionValue getProperty() {
        return property;
    }

    @JsonProperty(value = JSON_PROPERTY_PROPERTY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProperty(@org.eclipse.jdt.annotation.Nullable ProfileConditionValue property) {
        this.property = property;
    }

    public ProfileCondition value(@org.eclipse.jdt.annotation.Nullable String value) {
        this.value = value;
        return this;
    }

    /**
     * Get value
     * 
     * @return value
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VALUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getValue() {
        return value;
    }

    @JsonProperty(value = JSON_PROPERTY_VALUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValue(@org.eclipse.jdt.annotation.Nullable String value) {
        this.value = value;
    }

    public ProfileCondition isRequired(@org.eclipse.jdt.annotation.Nullable Boolean isRequired) {
        this.isRequired = isRequired;
        return this;
    }

    /**
     * Get isRequired
     * 
     * @return isRequired
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_REQUIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsRequired() {
        return isRequired;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_REQUIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsRequired(@org.eclipse.jdt.annotation.Nullable Boolean isRequired) {
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

        // add `Condition` to the URL query string
        if (getCondition() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCondition%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCondition()))));
        }

        // add `Property` to the URL query string
        if (getProperty() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProperty%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProperty()))));
        }

        // add `Value` to the URL query string
        if (getValue() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sValue%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getValue()))));
        }

        // add `IsRequired` to the URL query string
        if (getIsRequired() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsRequired%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsRequired()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ProfileCondition instance;

        public Builder() {
            this(new ProfileCondition());
        }

        protected Builder(ProfileCondition instance) {
            this.instance = instance;
        }

        public ProfileCondition.Builder condition(ProfileConditionType condition) {
            this.instance.condition = condition;
            return this;
        }

        public ProfileCondition.Builder property(ProfileConditionValue property) {
            this.instance.property = property;
            return this;
        }

        public ProfileCondition.Builder value(String value) {
            this.instance.value = value;
            return this;
        }

        public ProfileCondition.Builder isRequired(Boolean isRequired) {
            this.instance.isRequired = isRequired;
            return this;
        }

        /**
         * returns a built ProfileCondition instance.
         *
         * The builder is not reusable.
         */
        public ProfileCondition build() {
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
    public static ProfileCondition.Builder builder() {
        return new ProfileCondition.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ProfileCondition.Builder toBuilder() {
        return new ProfileCondition.Builder().condition(getCondition()).property(getProperty()).value(getValue())
                .isRequired(getIsRequired());
    }
}
