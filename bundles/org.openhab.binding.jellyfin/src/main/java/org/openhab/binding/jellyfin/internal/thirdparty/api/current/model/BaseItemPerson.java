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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

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
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private UUID id;

    public static final String JSON_PROPERTY_ROLE = "Role";
    @org.eclipse.jdt.annotation.Nullable
    private String role;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.Nullable
    private PersonKind type = PersonKind.UNKNOWN;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_TAG = "PrimaryImageTag";
    @org.eclipse.jdt.annotation.Nullable
    private String primaryImageTag;

    public static final String JSON_PROPERTY_IMAGE_BLUR_HASHES = "ImageBlurHashes";
    @org.eclipse.jdt.annotation.Nullable
    private BaseItemPersonImageBlurHashes imageBlurHashes;

    public BaseItemPerson() {
    }

    public BaseItemPerson name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
    }

    public BaseItemPerson id(@org.eclipse.jdt.annotation.Nullable UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the identifier.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.Nullable UUID id) {
        this.id = id;
    }

    public BaseItemPerson role(@org.eclipse.jdt.annotation.Nullable String role) {
        this.role = role;
        return this;
    }

    /**
     * Gets or sets the role.
     * 
     * @return role
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ROLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getRole() {
        return role;
    }

    @JsonProperty(value = JSON_PROPERTY_ROLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRole(@org.eclipse.jdt.annotation.Nullable String role) {
        this.role = role;
    }

    public BaseItemPerson type(@org.eclipse.jdt.annotation.Nullable PersonKind type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PersonKind getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.Nullable PersonKind type) {
        this.type = type;
    }

    public BaseItemPerson primaryImageTag(@org.eclipse.jdt.annotation.Nullable String primaryImageTag) {
        this.primaryImageTag = primaryImageTag;
        return this;
    }

    /**
     * Gets or sets the primary image tag.
     * 
     * @return primaryImageTag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPrimaryImageTag() {
        return primaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageTag(@org.eclipse.jdt.annotation.Nullable String primaryImageTag) {
        this.primaryImageTag = primaryImageTag;
    }

    public BaseItemPerson imageBlurHashes(
            @org.eclipse.jdt.annotation.Nullable BaseItemPersonImageBlurHashes imageBlurHashes) {
        this.imageBlurHashes = imageBlurHashes;
        return this;
    }

    /**
     * Get imageBlurHashes
     * 
     * @return imageBlurHashes
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_BLUR_HASHES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemPersonImageBlurHashes getImageBlurHashes() {
        return imageBlurHashes;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_BLUR_HASHES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageBlurHashes(@org.eclipse.jdt.annotation.Nullable BaseItemPersonImageBlurHashes imageBlurHashes) {
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Role` to the URL query string
        if (getRole() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRole%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRole()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `PrimaryImageTag` to the URL query string
        if (getPrimaryImageTag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageTag()))));
        }

        // add `ImageBlurHashes` to the URL query string
        if (getImageBlurHashes() != null) {
            joiner.add(getImageBlurHashes().toUrlQueryString(prefix + "ImageBlurHashes" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BaseItemPerson instance;

        public Builder() {
            this(new BaseItemPerson());
        }

        protected Builder(BaseItemPerson instance) {
            this.instance = instance;
        }

        public BaseItemPerson.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public BaseItemPerson.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public BaseItemPerson.Builder role(String role) {
            this.instance.role = role;
            return this;
        }

        public BaseItemPerson.Builder type(PersonKind type) {
            this.instance.type = type;
            return this;
        }

        public BaseItemPerson.Builder primaryImageTag(String primaryImageTag) {
            this.instance.primaryImageTag = primaryImageTag;
            return this;
        }

        public BaseItemPerson.Builder imageBlurHashes(BaseItemPersonImageBlurHashes imageBlurHashes) {
            this.instance.imageBlurHashes = imageBlurHashes;
            return this;
        }

        /**
         * returns a built BaseItemPerson instance.
         *
         * The builder is not reusable.
         */
        public BaseItemPerson build() {
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
    public static BaseItemPerson.Builder builder() {
        return new BaseItemPerson.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BaseItemPerson.Builder toBuilder() {
        return new BaseItemPerson.Builder().name(getName()).id(getId()).role(getRole()).type(getType())
                .primaryImageTag(getPrimaryImageTag()).imageBlurHashes(getImageBlurHashes());
    }
}
