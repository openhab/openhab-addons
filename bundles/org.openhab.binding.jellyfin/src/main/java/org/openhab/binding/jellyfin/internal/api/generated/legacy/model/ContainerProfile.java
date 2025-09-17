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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ContainerProfile
 */
@JsonPropertyOrder({ ContainerProfile.JSON_PROPERTY_TYPE, ContainerProfile.JSON_PROPERTY_CONDITIONS,
        ContainerProfile.JSON_PROPERTY_CONTAINER })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ContainerProfile {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private DlnaProfileType type;

    public static final String JSON_PROPERTY_CONDITIONS = "Conditions";
    @org.eclipse.jdt.annotation.NonNull
    private List<ProfileCondition> conditions;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public ContainerProfile() {
    }

    public ContainerProfile type(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
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
    public DlnaProfileType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
        this.type = type;
    }

    public ContainerProfile conditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public ContainerProfile addConditionsItem(ProfileCondition conditionsItem) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(conditionsItem);
        return this;
    }

    /**
     * Get conditions
     * 
     * @return conditions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONDITIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ProfileCondition> getConditions() {
        return conditions;
    }

    @JsonProperty(JSON_PROPERTY_CONDITIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> conditions) {
        this.conditions = conditions;
    }

    public ContainerProfile container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Get container
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContainer() {
        return container;
    }

    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
    }

    /**
     * Return true if this ContainerProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContainerProfile containerProfile = (ContainerProfile) o;
        return Objects.equals(this.type, containerProfile.type)
                && Objects.equals(this.conditions, containerProfile.conditions)
                && Objects.equals(this.container, containerProfile.container);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, conditions, container);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ContainerProfile {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
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

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format("%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `Conditions` to the URL query string
        if (getConditions() != null) {
            for (int i = 0; i < getConditions().size(); i++) {
                if (getConditions().get(i) != null) {
                    joiner.add(getConditions().get(i).toUrlQueryString(String.format("%sConditions%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format("%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ContainerProfile instance;

        public Builder() {
            this(new ContainerProfile());
        }

        protected Builder(ContainerProfile instance) {
            this.instance = instance;
        }

        public ContainerProfile.Builder type(DlnaProfileType type) {
            this.instance.type = type;
            return this;
        }

        public ContainerProfile.Builder conditions(List<ProfileCondition> conditions) {
            this.instance.conditions = conditions;
            return this;
        }

        public ContainerProfile.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        /**
         * returns a built ContainerProfile instance.
         *
         * The builder is not reusable.
         */
        public ContainerProfile build() {
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
    public static ContainerProfile.Builder builder() {
        return new ContainerProfile.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ContainerProfile.Builder toBuilder() {
        return new ContainerProfile.Builder().type(getType()).conditions(getConditions()).container(getContainer());
    }
}
