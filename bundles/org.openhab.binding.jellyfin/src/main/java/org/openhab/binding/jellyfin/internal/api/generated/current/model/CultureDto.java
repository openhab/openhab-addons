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

public class CultureDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_DISPLAY_NAME = "DisplayName";
    @org.eclipse.jdt.annotation.NonNull
    private String displayName;

    public static final String JSON_PROPERTY_TWO_LETTER_I_S_O_LANGUAGE_NAME = "TwoLetterISOLanguageName";
    @org.eclipse.jdt.annotation.NonNull
    private String twoLetterISOLanguageName;

    public static final String JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME = "ThreeLetterISOLanguageName";
    @org.eclipse.jdt.annotation.NonNull
    private String threeLetterISOLanguageName;

    public static final String JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAMES = "ThreeLetterISOLanguageNames";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> threeLetterISOLanguageNames = new ArrayList<>();

    public CultureDto() {
    }

    @JsonCreator
    public CultureDto(@JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME) String threeLetterISOLanguageName) {
        this();
        this.threeLetterISOLanguageName = threeLetterISOLanguageName;
    }

    public CultureDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name.
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

    public CultureDto displayName(@org.eclipse.jdt.annotation.NonNull String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Gets the display name.
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

    public CultureDto twoLetterISOLanguageName(@org.eclipse.jdt.annotation.NonNull String twoLetterISOLanguageName) {
        this.twoLetterISOLanguageName = twoLetterISOLanguageName;
        return this;
    }

    /**
     * Gets the name of the two letter ISO language.
     * 
     * @return twoLetterISOLanguageName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TWO_LETTER_I_S_O_LANGUAGE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTwoLetterISOLanguageName() {
        return twoLetterISOLanguageName;
    }

    @JsonProperty(JSON_PROPERTY_TWO_LETTER_I_S_O_LANGUAGE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTwoLetterISOLanguageName(@org.eclipse.jdt.annotation.NonNull String twoLetterISOLanguageName) {
        this.twoLetterISOLanguageName = twoLetterISOLanguageName;
    }

    /**
     * Gets the name of the three letter ISO language.
     * 
     * @return threeLetterISOLanguageName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getThreeLetterISOLanguageName() {
        return threeLetterISOLanguageName;
    }

    public CultureDto threeLetterISOLanguageNames(
            @org.eclipse.jdt.annotation.NonNull List<String> threeLetterISOLanguageNames) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAMES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getThreeLetterISOLanguageNames() {
        return threeLetterISOLanguageNames;
    }

    @JsonProperty(JSON_PROPERTY_THREE_LETTER_I_S_O_LANGUAGE_NAMES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThreeLetterISOLanguageNames(
            @org.eclipse.jdt.annotation.NonNull List<String> threeLetterISOLanguageNames) {
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
}
