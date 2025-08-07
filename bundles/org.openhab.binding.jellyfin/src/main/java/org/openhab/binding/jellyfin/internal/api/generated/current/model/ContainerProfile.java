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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the MediaBrowser.Model.Dlna.ContainerProfile.
 */
@JsonPropertyOrder({ ContainerProfile.JSON_PROPERTY_TYPE, ContainerProfile.JSON_PROPERTY_CONDITIONS,
        ContainerProfile.JSON_PROPERTY_CONTAINER, ContainerProfile.JSON_PROPERTY_SUB_CONTAINER })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ContainerProfile {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private DlnaProfileType type;

    public static final String JSON_PROPERTY_CONDITIONS = "Conditions";
    @org.eclipse.jdt.annotation.NonNull
    private List<ProfileCondition> conditions = new ArrayList<>();

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_SUB_CONTAINER = "SubContainer";
    @org.eclipse.jdt.annotation.NonNull
    private String subContainer;

    public ContainerProfile() {
    }

    public ContainerProfile type(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the MediaBrowser.Model.Dlna.DlnaProfileType which this container must meet.
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
     * Gets or sets the list of MediaBrowser.Model.Dlna.ProfileCondition which this container will be applied to.
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
     * Gets or sets the container(s) which this container must meet.
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

    public ContainerProfile subContainer(@org.eclipse.jdt.annotation.NonNull String subContainer) {
        this.subContainer = subContainer;
        return this;
    }

    /**
     * Gets or sets the sub container(s) which this container must meet.
     * 
     * @return subContainer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUB_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSubContainer() {
        return subContainer;
    }

    @JsonProperty(JSON_PROPERTY_SUB_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubContainer(@org.eclipse.jdt.annotation.NonNull String subContainer) {
        this.subContainer = subContainer;
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
                && Objects.equals(this.container, containerProfile.container)
                && Objects.equals(this.subContainer, containerProfile.subContainer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, conditions, container, subContainer);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ContainerProfile {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
        sb.append("    subContainer: ").append(toIndentedString(subContainer)).append("\n");
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
