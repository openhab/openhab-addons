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
 * A class for subtitle profile information.
 */
@JsonPropertyOrder({ SubtitleProfile.JSON_PROPERTY_FORMAT, SubtitleProfile.JSON_PROPERTY_METHOD,
        SubtitleProfile.JSON_PROPERTY_DIDL_MODE, SubtitleProfile.JSON_PROPERTY_LANGUAGE,
        SubtitleProfile.JSON_PROPERTY_CONTAINER })

public class SubtitleProfile {
    public static final String JSON_PROPERTY_FORMAT = "Format";
    @org.eclipse.jdt.annotation.NonNull
    private String format;

    public static final String JSON_PROPERTY_METHOD = "Method";
    @org.eclipse.jdt.annotation.NonNull
    private SubtitleDeliveryMethod method;

    public static final String JSON_PROPERTY_DIDL_MODE = "DidlMode";
    @org.eclipse.jdt.annotation.NonNull
    private String didlMode;

    public static final String JSON_PROPERTY_LANGUAGE = "Language";
    @org.eclipse.jdt.annotation.NonNull
    private String language;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public SubtitleProfile() {
    }

    public SubtitleProfile format(@org.eclipse.jdt.annotation.NonNull String format) {
        this.format = format;
        return this;
    }

    /**
     * Gets or sets the format.
     * 
     * @return format
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FORMAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getFormat() {
        return format;
    }

    @JsonProperty(JSON_PROPERTY_FORMAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFormat(@org.eclipse.jdt.annotation.NonNull String format) {
        this.format = format;
    }

    public SubtitleProfile method(@org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod method) {
        this.method = method;
        return this;
    }

    /**
     * Gets or sets the delivery method.
     * 
     * @return method
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SubtitleDeliveryMethod getMethod() {
        return method;
    }

    @JsonProperty(JSON_PROPERTY_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMethod(@org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod method) {
        this.method = method;
    }

    public SubtitleProfile didlMode(@org.eclipse.jdt.annotation.NonNull String didlMode) {
        this.didlMode = didlMode;
        return this;
    }

    /**
     * Gets or sets the DIDL mode.
     * 
     * @return didlMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DIDL_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDidlMode() {
        return didlMode;
    }

    @JsonProperty(JSON_PROPERTY_DIDL_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDidlMode(@org.eclipse.jdt.annotation.NonNull String didlMode) {
        this.didlMode = didlMode;
    }

    public SubtitleProfile language(@org.eclipse.jdt.annotation.NonNull String language) {
        this.language = language;
        return this;
    }

    /**
     * Gets or sets the language.
     * 
     * @return language
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLanguage() {
        return language;
    }

    @JsonProperty(JSON_PROPERTY_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLanguage(@org.eclipse.jdt.annotation.NonNull String language) {
        this.language = language;
    }

    public SubtitleProfile container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Gets or sets the container.
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
     * Return true if this SubtitleProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubtitleProfile subtitleProfile = (SubtitleProfile) o;
        return Objects.equals(this.format, subtitleProfile.format)
                && Objects.equals(this.method, subtitleProfile.method)
                && Objects.equals(this.didlMode, subtitleProfile.didlMode)
                && Objects.equals(this.language, subtitleProfile.language)
                && Objects.equals(this.container, subtitleProfile.container);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, method, didlMode, language, container);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SubtitleProfile {\n");
        sb.append("    format: ").append(toIndentedString(format)).append("\n");
        sb.append("    method: ").append(toIndentedString(method)).append("\n");
        sb.append("    didlMode: ").append(toIndentedString(didlMode)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
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
}
