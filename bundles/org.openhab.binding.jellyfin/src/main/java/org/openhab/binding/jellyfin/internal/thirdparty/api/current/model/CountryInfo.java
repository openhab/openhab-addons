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
 * Class CountryInfo.
 */
@JsonPropertyOrder({ CountryInfo.JSON_PROPERTY_NAME, CountryInfo.JSON_PROPERTY_DISPLAY_NAME,
        CountryInfo.JSON_PROPERTY_TWO_LETTER_I_S_O_REGION_NAME,
        CountryInfo.JSON_PROPERTY_THREE_LETTER_I_S_O_REGION_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CountryInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_DISPLAY_NAME = "DisplayName";
    @org.eclipse.jdt.annotation.Nullable
    private String displayName;

    public static final String JSON_PROPERTY_TWO_LETTER_I_S_O_REGION_NAME = "TwoLetterISORegionName";
    @org.eclipse.jdt.annotation.Nullable
    private String twoLetterISORegionName;

    public static final String JSON_PROPERTY_THREE_LETTER_I_S_O_REGION_NAME = "ThreeLetterISORegionName";
    @org.eclipse.jdt.annotation.Nullable
    private String threeLetterISORegionName;

    public CountryInfo() {
    }

    public CountryInfo name(@org.eclipse.jdt.annotation.Nullable String name) {
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

    public CountryInfo displayName(@org.eclipse.jdt.annotation.Nullable String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Gets or sets the display name.
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

    public CountryInfo twoLetterISORegionName(@org.eclipse.jdt.annotation.Nullable String twoLetterISORegionName) {
        this.twoLetterISORegionName = twoLetterISORegionName;
        return this;
    }

    /**
     * Gets or sets the name of the two letter ISO region.
     * 
     * @return twoLetterISORegionName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TWO_LETTER_I_S_O_REGION_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTwoLetterISORegionName() {
        return twoLetterISORegionName;
    }

    @JsonProperty(value = JSON_PROPERTY_TWO_LETTER_I_S_O_REGION_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTwoLetterISORegionName(@org.eclipse.jdt.annotation.Nullable String twoLetterISORegionName) {
        this.twoLetterISORegionName = twoLetterISORegionName;
    }

    public CountryInfo threeLetterISORegionName(@org.eclipse.jdt.annotation.Nullable String threeLetterISORegionName) {
        this.threeLetterISORegionName = threeLetterISORegionName;
        return this;
    }

    /**
     * Gets or sets the name of the three letter ISO region.
     * 
     * @return threeLetterISORegionName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_THREE_LETTER_I_S_O_REGION_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getThreeLetterISORegionName() {
        return threeLetterISORegionName;
    }

    @JsonProperty(value = JSON_PROPERTY_THREE_LETTER_I_S_O_REGION_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThreeLetterISORegionName(@org.eclipse.jdt.annotation.Nullable String threeLetterISORegionName) {
        this.threeLetterISORegionName = threeLetterISORegionName;
    }

    /**
     * Return true if this CountryInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CountryInfo countryInfo = (CountryInfo) o;
        return Objects.equals(this.name, countryInfo.name) && Objects.equals(this.displayName, countryInfo.displayName)
                && Objects.equals(this.twoLetterISORegionName, countryInfo.twoLetterISORegionName)
                && Objects.equals(this.threeLetterISORegionName, countryInfo.threeLetterISORegionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, displayName, twoLetterISORegionName, threeLetterISORegionName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CountryInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    twoLetterISORegionName: ").append(toIndentedString(twoLetterISORegionName)).append("\n");
        sb.append("    threeLetterISORegionName: ").append(toIndentedString(threeLetterISORegionName)).append("\n");
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

        // add `TwoLetterISORegionName` to the URL query string
        if (getTwoLetterISORegionName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTwoLetterISORegionName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTwoLetterISORegionName()))));
        }

        // add `ThreeLetterISORegionName` to the URL query string
        if (getThreeLetterISORegionName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sThreeLetterISORegionName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getThreeLetterISORegionName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private CountryInfo instance;

        public Builder() {
            this(new CountryInfo());
        }

        protected Builder(CountryInfo instance) {
            this.instance = instance;
        }

        public CountryInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public CountryInfo.Builder displayName(String displayName) {
            this.instance.displayName = displayName;
            return this;
        }

        public CountryInfo.Builder twoLetterISORegionName(String twoLetterISORegionName) {
            this.instance.twoLetterISORegionName = twoLetterISORegionName;
            return this;
        }

        public CountryInfo.Builder threeLetterISORegionName(String threeLetterISORegionName) {
            this.instance.threeLetterISORegionName = threeLetterISORegionName;
            return this;
        }

        /**
         * returns a built CountryInfo instance.
         *
         * The builder is not reusable.
         */
        public CountryInfo build() {
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
    public static CountryInfo.Builder builder() {
        return new CountryInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public CountryInfo.Builder toBuilder() {
        return new CountryInfo.Builder().name(getName()).displayName(getDisplayName())
                .twoLetterISORegionName(getTwoLetterISORegionName())
                .threeLetterISORegionName(getThreeLetterISORegionName());
    }
}
