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
 * Class CountryInfo.
 */
@JsonPropertyOrder({ CountryInfo.JSON_PROPERTY_NAME, CountryInfo.JSON_PROPERTY_DISPLAY_NAME,
        CountryInfo.JSON_PROPERTY_TWO_LETTER_I_S_O_REGION_NAME,
        CountryInfo.JSON_PROPERTY_THREE_LETTER_I_S_O_REGION_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CountryInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_DISPLAY_NAME = "DisplayName";
    @org.eclipse.jdt.annotation.NonNull
    private String displayName;

    public static final String JSON_PROPERTY_TWO_LETTER_I_S_O_REGION_NAME = "TwoLetterISORegionName";
    @org.eclipse.jdt.annotation.NonNull
    private String twoLetterISORegionName;

    public static final String JSON_PROPERTY_THREE_LETTER_I_S_O_REGION_NAME = "ThreeLetterISORegionName";
    @org.eclipse.jdt.annotation.NonNull
    private String threeLetterISORegionName;

    public CountryInfo() {
    }

    public CountryInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public CountryInfo displayName(@org.eclipse.jdt.annotation.NonNull String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Gets or sets the display name.
     * 
     * @return displayName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISPLAY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty(JSON_PROPERTY_DISPLAY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplayName(@org.eclipse.jdt.annotation.NonNull String displayName) {
        this.displayName = displayName;
    }

    public CountryInfo twoLetterISORegionName(@org.eclipse.jdt.annotation.NonNull String twoLetterISORegionName) {
        this.twoLetterISORegionName = twoLetterISORegionName;
        return this;
    }

    /**
     * Gets or sets the name of the two letter ISO region.
     * 
     * @return twoLetterISORegionName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TWO_LETTER_I_S_O_REGION_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTwoLetterISORegionName() {
        return twoLetterISORegionName;
    }

    @JsonProperty(JSON_PROPERTY_TWO_LETTER_I_S_O_REGION_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTwoLetterISORegionName(@org.eclipse.jdt.annotation.NonNull String twoLetterISORegionName) {
        this.twoLetterISORegionName = twoLetterISORegionName;
    }

    public CountryInfo threeLetterISORegionName(@org.eclipse.jdt.annotation.NonNull String threeLetterISORegionName) {
        this.threeLetterISORegionName = threeLetterISORegionName;
        return this;
    }

    /**
     * Gets or sets the name of the three letter ISO region.
     * 
     * @return threeLetterISORegionName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_REGION_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getThreeLetterISORegionName() {
        return threeLetterISORegionName;
    }

    @JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_REGION_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThreeLetterISORegionName(@org.eclipse.jdt.annotation.NonNull String threeLetterISORegionName) {
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
}
