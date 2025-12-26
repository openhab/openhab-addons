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
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the MediaBrowser.Model.Dlna.CodecProfile.
 */
@JsonPropertyOrder({ CodecProfile.JSON_PROPERTY_TYPE, CodecProfile.JSON_PROPERTY_CONDITIONS,
        CodecProfile.JSON_PROPERTY_APPLY_CONDITIONS, CodecProfile.JSON_PROPERTY_CODEC,
        CodecProfile.JSON_PROPERTY_CONTAINER, CodecProfile.JSON_PROPERTY_SUB_CONTAINER })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CodecProfile {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private CodecType type;

    public static final String JSON_PROPERTY_CONDITIONS = "Conditions";
    @org.eclipse.jdt.annotation.NonNull
    private List<ProfileCondition> conditions = new ArrayList<>();

    public static final String JSON_PROPERTY_APPLY_CONDITIONS = "ApplyConditions";
    @org.eclipse.jdt.annotation.NonNull
    private List<ProfileCondition> applyConditions = new ArrayList<>();

    public static final String JSON_PROPERTY_CODEC = "Codec";
    @org.eclipse.jdt.annotation.NonNull
    private String codec;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_SUB_CONTAINER = "SubContainer";
    @org.eclipse.jdt.annotation.NonNull
    private String subContainer;

    public CodecProfile() {
    }

    public CodecProfile type(@org.eclipse.jdt.annotation.NonNull CodecType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the MediaBrowser.Model.Dlna.CodecType which this container must meet.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public CodecType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull CodecType type) {
        this.type = type;
    }

    public CodecProfile conditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public CodecProfile addConditionsItem(ProfileCondition conditionsItem) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(conditionsItem);
        return this;
    }

    /**
     * Gets or sets the list of MediaBrowser.Model.Dlna.ProfileCondition which this profile must meet.
     * 
     * @return conditions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CONDITIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ProfileCondition> getConditions() {
        return conditions;
    }

    @JsonProperty(value = JSON_PROPERTY_CONDITIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> conditions) {
        this.conditions = conditions;
    }

    public CodecProfile applyConditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> applyConditions) {
        this.applyConditions = applyConditions;
        return this;
    }

    public CodecProfile addApplyConditionsItem(ProfileCondition applyConditionsItem) {
        if (this.applyConditions == null) {
            this.applyConditions = new ArrayList<>();
        }
        this.applyConditions.add(applyConditionsItem);
        return this;
    }

    /**
     * Gets or sets the list of MediaBrowser.Model.Dlna.ProfileCondition to apply if this profile is met.
     * 
     * @return applyConditions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_APPLY_CONDITIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ProfileCondition> getApplyConditions() {
        return applyConditions;
    }

    @JsonProperty(value = JSON_PROPERTY_APPLY_CONDITIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setApplyConditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> applyConditions) {
        this.applyConditions = applyConditions;
    }

    public CodecProfile codec(@org.eclipse.jdt.annotation.NonNull String codec) {
        this.codec = codec;
        return this;
    }

    /**
     * Gets or sets the codec(s) that this profile applies to.
     * 
     * @return codec
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCodec() {
        return codec;
    }

    @JsonProperty(value = JSON_PROPERTY_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodec(@org.eclipse.jdt.annotation.NonNull String codec) {
        this.codec = codec;
    }

    public CodecProfile container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Gets or sets the container(s) which this profile will be applied to.
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContainer() {
        return container;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
    }

    public CodecProfile subContainer(@org.eclipse.jdt.annotation.NonNull String subContainer) {
        this.subContainer = subContainer;
        return this;
    }

    /**
     * Gets or sets the sub-container(s) which this profile will be applied to.
     * 
     * @return subContainer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUB_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSubContainer() {
        return subContainer;
    }

    @JsonProperty(value = JSON_PROPERTY_SUB_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubContainer(@org.eclipse.jdt.annotation.NonNull String subContainer) {
        this.subContainer = subContainer;
    }

    /**
     * Return true if this CodecProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CodecProfile codecProfile = (CodecProfile) o;
        return Objects.equals(this.type, codecProfile.type) && Objects.equals(this.conditions, codecProfile.conditions)
                && Objects.equals(this.applyConditions, codecProfile.applyConditions)
                && Objects.equals(this.codec, codecProfile.codec)
                && Objects.equals(this.container, codecProfile.container)
                && Objects.equals(this.subContainer, codecProfile.subContainer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, conditions, applyConditions, codec, container, subContainer);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CodecProfile {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
        sb.append("    applyConditions: ").append(toIndentedString(applyConditions)).append("\n");
        sb.append("    codec: ").append(toIndentedString(codec)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `Conditions` to the URL query string
        if (getConditions() != null) {
            for (int i = 0; i < getConditions().size(); i++) {
                if (getConditions().get(i) != null) {
                    joiner.add(getConditions().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sConditions%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `ApplyConditions` to the URL query string
        if (getApplyConditions() != null) {
            for (int i = 0; i < getApplyConditions().size(); i++) {
                if (getApplyConditions().get(i) != null) {
                    joiner.add(getApplyConditions().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sApplyConditions%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `Codec` to the URL query string
        if (getCodec() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCodec()))));
        }

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format(Locale.ROOT, "%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        // add `SubContainer` to the URL query string
        if (getSubContainer() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSubContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSubContainer()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private CodecProfile instance;

        public Builder() {
            this(new CodecProfile());
        }

        protected Builder(CodecProfile instance) {
            this.instance = instance;
        }

        public CodecProfile.Builder type(CodecType type) {
            this.instance.type = type;
            return this;
        }

        public CodecProfile.Builder conditions(List<ProfileCondition> conditions) {
            this.instance.conditions = conditions;
            return this;
        }

        public CodecProfile.Builder applyConditions(List<ProfileCondition> applyConditions) {
            this.instance.applyConditions = applyConditions;
            return this;
        }

        public CodecProfile.Builder codec(String codec) {
            this.instance.codec = codec;
            return this;
        }

        public CodecProfile.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        public CodecProfile.Builder subContainer(String subContainer) {
            this.instance.subContainer = subContainer;
            return this;
        }

        /**
         * returns a built CodecProfile instance.
         *
         * The builder is not reusable.
         */
        public CodecProfile build() {
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
    public static CodecProfile.Builder builder() {
        return new CodecProfile.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public CodecProfile.Builder toBuilder() {
        return new CodecProfile.Builder().type(getType()).conditions(getConditions())
                .applyConditions(getApplyConditions()).codec(getCodec()).container(getContainer())
                .subContainer(getSubContainer());
    }
}
