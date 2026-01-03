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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class CultureDto.
 */
@JsonPropertyOrder({ CultureDto.JSON_PROPERTY_NAME, CultureDto.JSON_PROPERTY_DISPLAY_NAME,
        CultureDto.JSON_PROPERTY_TWO_LETTER_I_S_O_LANGUAGE_NAME,
        CultureDto.JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME,
        CultureDto.JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAMES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CultureDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_DISPLAY_NAME = "DisplayName";
    @org.eclipse.jdt.annotation.Nullable
    private String displayName;

    public static final String JSON_PROPERTY_TWO_LETTER_I_S_O_LANGUAGE_NAME = "TwoLetterISOLanguageName";
    @org.eclipse.jdt.annotation.Nullable
    private String twoLetterISOLanguageName;

    public static final String JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME = "ThreeLetterISOLanguageName";
    @org.eclipse.jdt.annotation.Nullable
    private String threeLetterISOLanguageName;

    public static final String JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAMES = "ThreeLetterISOLanguageNames";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> threeLetterISOLanguageNames = new ArrayList<>();

    public CultureDto() {
    }

    @JsonCreator
    public CultureDto(@JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME) String threeLetterISOLanguageName) {
        this();
        this.threeLetterISOLanguageName = threeLetterISOLanguageName;
    }

    public CultureDto name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name.
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

    public CultureDto displayName(@org.eclipse.jdt.annotation.Nullable String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Gets the display name.
     * 
     * @return displayName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DISPLAY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty(value = JSON_PROPERTY_DISPLAY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplayName(@org.eclipse.jdt.annotation.Nullable String displayName) {
        this.displayName = displayName;
    }

    public CultureDto twoLetterISOLanguageName(@org.eclipse.jdt.annotation.Nullable String twoLetterISOLanguageName) {
        this.twoLetterISOLanguageName = twoLetterISOLanguageName;
        return this;
    }

    /**
     * Gets the name of the two letter ISO language.
     * 
     * @return twoLetterISOLanguageName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TWO_LETTER_I_S_O_LANGUAGE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTwoLetterISOLanguageName() {
        return twoLetterISOLanguageName;
    }

    @JsonProperty(value = JSON_PROPERTY_TWO_LETTER_I_S_O_LANGUAGE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTwoLetterISOLanguageName(@org.eclipse.jdt.annotation.Nullable String twoLetterISOLanguageName) {
        this.twoLetterISOLanguageName = twoLetterISOLanguageName;
    }

    /**
     * Gets the name of the three letter ISO language.
     * 
     * @return threeLetterISOLanguageName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getThreeLetterISOLanguageName() {
        return threeLetterISOLanguageName;
    }

    public CultureDto threeLetterISOLanguageNames(
            @org.eclipse.jdt.annotation.Nullable List<String> threeLetterISOLanguageNames) {
        this.threeLetterISOLanguageNames = threeLetterISOLanguageNames;
        return this;
    }

    public CultureDto addThreeLetterISOLanguageNamesItem(String threeLetterISOLanguageNamesItem) {
        if (this.threeLetterISOLanguageNames == null) {
            this.threeLetterISOLanguageNames = new ArrayList<>();
        }
        this.threeLetterISOLanguageNames.add(threeLetterISOLanguageNamesItem);
        return this;
    }

    /**
     * Get threeLetterISOLanguageNames
     * 
     * @return threeLetterISOLanguageNames
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAMES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getThreeLetterISOLanguageNames() {
        return threeLetterISOLanguageNames;
    }

    @JsonProperty(value = JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAMES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThreeLetterISOLanguageNames(
            @org.eclipse.jdt.annotation.Nullable List<String> threeLetterISOLanguageNames) {
        this.threeLetterISOLanguageNames = threeLetterISOLanguageNames;
    }

    /**
     * Return true if this CultureDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CultureDto cultureDto = (CultureDto) o;
        return Objects.equals(this.name, cultureDto.name) && Objects.equals(this.displayName, cultureDto.displayName)
                && Objects.equals(this.twoLetterISOLanguageName, cultureDto.twoLetterISOLanguageName)
                && Objects.equals(this.threeLetterISOLanguageName, cultureDto.threeLetterISOLanguageName)
                && Objects.equals(this.threeLetterISOLanguageNames, cultureDto.threeLetterISOLanguageNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, displayName, twoLetterISOLanguageName, threeLetterISOLanguageName,
                threeLetterISOLanguageNames);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CultureDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    twoLetterISOLanguageName: ").append(toIndentedString(twoLetterISOLanguageName)).append("\n");
        sb.append("    threeLetterISOLanguageName: ").append(toIndentedString(threeLetterISOLanguageName)).append("\n");
        sb.append("    threeLetterISOLanguageNames: ").append(toIndentedString(threeLetterISOLanguageNames))
                .append("\n");
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

        // add `DisplayName` to the URL query string
        if (getDisplayName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDisplayName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDisplayName()))));
        }

        // add `TwoLetterISOLanguageName` to the URL query string
        if (getTwoLetterISOLanguageName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTwoLetterISOLanguageName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTwoLetterISOLanguageName()))));
        }

        // add `ThreeLetterISOLanguageName` to the URL query string
        if (getThreeLetterISOLanguageName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sThreeLetterISOLanguageName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getThreeLetterISOLanguageName()))));
        }

        // add `ThreeLetterISOLanguageNames` to the URL query string
        if (getThreeLetterISOLanguageNames() != null) {
            for (int i = 0; i < getThreeLetterISOLanguageNames().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sThreeLetterISOLanguageNames%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getThreeLetterISOLanguageNames().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private CultureDto instance;

        public Builder() {
            this(new CultureDto());
        }

        protected Builder(CultureDto instance) {
            this.instance = instance;
        }

        public CultureDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public CultureDto.Builder displayName(String displayName) {
            this.instance.displayName = displayName;
            return this;
        }

        public CultureDto.Builder twoLetterISOLanguageName(String twoLetterISOLanguageName) {
            this.instance.twoLetterISOLanguageName = twoLetterISOLanguageName;
            return this;
        }

        public CultureDto.Builder threeLetterISOLanguageName(String threeLetterISOLanguageName) {
            this.instance.threeLetterISOLanguageName = threeLetterISOLanguageName;
            return this;
        }

        public CultureDto.Builder threeLetterISOLanguageNames(List<String> threeLetterISOLanguageNames) {
            this.instance.threeLetterISOLanguageNames = threeLetterISOLanguageNames;
            return this;
        }

        /**
         * returns a built CultureDto instance.
         *
         * The builder is not reusable.
         */
        public CultureDto build() {
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
    public static CultureDto.Builder builder() {
        return new CultureDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public CultureDto.Builder toBuilder() {
        return new CultureDto.Builder().name(getName()).displayName(getDisplayName())
                .twoLetterISOLanguageName(getTwoLetterISOLanguageName())
                .threeLetterISOLanguageName(getThreeLetterISOLanguageName())
                .threeLetterISOLanguageNames(getThreeLetterISOLanguageNames());
    }
}
