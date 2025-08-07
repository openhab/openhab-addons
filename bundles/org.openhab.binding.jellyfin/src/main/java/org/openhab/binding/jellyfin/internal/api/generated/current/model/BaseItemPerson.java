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
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This is used by the api to get information about a Person within a BaseItem.
 */
@JsonPropertyOrder({ BaseItemPerson.JSON_PROPERTY_NAME, BaseItemPerson.JSON_PROPERTY_ID,
        BaseItemPerson.JSON_PROPERTY_ROLE, BaseItemPerson.JSON_PROPERTY_TYPE,
        BaseItemPerson.JSON_PROPERTY_PRIMARY_IMAGE_TAG, BaseItemPerson.JSON_PROPERTY_IMAGE_BLUR_HASHES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BaseItemPerson {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_ROLE = "Role";
    @org.eclipse.jdt.annotation.NonNull
    private String role;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private PersonKind type = PersonKind.UNKNOWN;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_TAG = "PrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String primaryImageTag;

    public static final String JSON_PROPERTY_IMAGE_BLUR_HASHES = "ImageBlurHashes";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemPersonImageBlurHashes imageBlurHashes;

    public BaseItemPerson() {
    }

    public BaseItemPerson name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public BaseItemPerson id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the identifier.
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

    public BaseItemPerson role(@org.eclipse.jdt.annotation.NonNull String role) {
        this.role = role;
        return this;
    }

    /**
     * Gets or sets the role.
     * 
     * @return role
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ROLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRole() {
        return role;
    }

    @JsonProperty(JSON_PROPERTY_ROLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRole(@org.eclipse.jdt.annotation.NonNull String role) {
        this.role = role;
    }

    public BaseItemPerson type(@org.eclipse.jdt.annotation.NonNull PersonKind type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PersonKind getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull PersonKind type) {
        this.type = type;
    }

    public BaseItemPerson primaryImageTag(@org.eclipse.jdt.annotation.NonNull String primaryImageTag) {
        this.primaryImageTag = primaryImageTag;
        return this;
    }

    /**
     * Gets or sets the primary image tag.
     * 
     * @return primaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPrimaryImageTag() {
        return primaryImageTag;
    }

    @JsonProperty(JSON_PROPERTY_PRIMARY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String primaryImageTag) {
        this.primaryImageTag = primaryImageTag;
    }

    public BaseItemPerson imageBlurHashes(
            @org.eclipse.jdt.annotation.NonNull BaseItemPersonImageBlurHashes imageBlurHashes) {
        this.imageBlurHashes = imageBlurHashes;
        return this;
    }

    /**
     * Get imageBlurHashes
     * 
     * @return imageBlurHashes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_BLUR_HASHES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BaseItemPersonImageBlurHashes getImageBlurHashes() {
        return imageBlurHashes;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_BLUR_HASHES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageBlurHashes(@org.eclipse.jdt.annotation.NonNull BaseItemPersonImageBlurHashes imageBlurHashes) {
        this.imageBlurHashes = imageBlurHashes;
    }

    /**
     * Return true if this BaseItemPerson object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseItemPerson baseItemPerson = (BaseItemPerson) o;
        return Objects.equals(this.name, baseItemPerson.name) && Objects.equals(this.id, baseItemPerson.id)
                && Objects.equals(this.role, baseItemPerson.role) && Objects.equals(this.type, baseItemPerson.type)
                && Objects.equals(this.primaryImageTag, baseItemPerson.primaryImageTag)
                && Objects.equals(this.imageBlurHashes, baseItemPerson.imageBlurHashes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, role, type, primaryImageTag, imageBlurHashes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BaseItemPerson {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    primaryImageTag: ").append(toIndentedString(primaryImageTag)).append("\n");
        sb.append("    imageBlurHashes: ").append(toIndentedString(imageBlurHashes)).append("\n");
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
