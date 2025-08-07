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
 * ConfigImageTypes
 */
@JsonPropertyOrder({ ConfigImageTypes.JSON_PROPERTY_BACKDROP_SIZES, ConfigImageTypes.JSON_PROPERTY_BASE_URL,
        ConfigImageTypes.JSON_PROPERTY_LOGO_SIZES, ConfigImageTypes.JSON_PROPERTY_POSTER_SIZES,
        ConfigImageTypes.JSON_PROPERTY_PROFILE_SIZES, ConfigImageTypes.JSON_PROPERTY_SECURE_BASE_URL,
        ConfigImageTypes.JSON_PROPERTY_STILL_SIZES })

public class ConfigImageTypes {
    public static final String JSON_PROPERTY_BACKDROP_SIZES = "BackdropSizes";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> backdropSizes;

    public static final String JSON_PROPERTY_BASE_URL = "BaseUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String baseUrl;

    public static final String JSON_PROPERTY_LOGO_SIZES = "LogoSizes";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> logoSizes;

    public static final String JSON_PROPERTY_POSTER_SIZES = "PosterSizes";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> posterSizes;

    public static final String JSON_PROPERTY_PROFILE_SIZES = "ProfileSizes";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> profileSizes;

    public static final String JSON_PROPERTY_SECURE_BASE_URL = "SecureBaseUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String secureBaseUrl;

    public static final String JSON_PROPERTY_STILL_SIZES = "StillSizes";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> stillSizes;

    public ConfigImageTypes() {
    }

    public ConfigImageTypes backdropSizes(@org.eclipse.jdt.annotation.NonNull List<String> backdropSizes) {
        this.backdropSizes = backdropSizes;
        return this;
    }

    public ConfigImageTypes addBackdropSizesItem(String backdropSizesItem) {
        if (this.backdropSizes == null) {
            this.backdropSizes = new ArrayList<>();
        }
        this.backdropSizes.add(backdropSizesItem);
        return this;
    }

    /**
     * Get backdropSizes
     * 
     * @return backdropSizes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BACKDROP_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getBackdropSizes() {
        return backdropSizes;
    }

    @JsonProperty(JSON_PROPERTY_BACKDROP_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBackdropSizes(@org.eclipse.jdt.annotation.NonNull List<String> backdropSizes) {
        this.backdropSizes = backdropSizes;
    }

    public ConfigImageTypes baseUrl(@org.eclipse.jdt.annotation.NonNull String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Get baseUrl
     * 
     * @return baseUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BASE_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBaseUrl() {
        return baseUrl;
    }

    @JsonProperty(JSON_PROPERTY_BASE_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBaseUrl(@org.eclipse.jdt.annotation.NonNull String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ConfigImageTypes logoSizes(@org.eclipse.jdt.annotation.NonNull List<String> logoSizes) {
        this.logoSizes = logoSizes;
        return this;
    }

    public ConfigImageTypes addLogoSizesItem(String logoSizesItem) {
        if (this.logoSizes == null) {
            this.logoSizes = new ArrayList<>();
        }
        this.logoSizes.add(logoSizesItem);
        return this;
    }

    /**
     * Get logoSizes
     * 
     * @return logoSizes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LOGO_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getLogoSizes() {
        return logoSizes;
    }

    @JsonProperty(JSON_PROPERTY_LOGO_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLogoSizes(@org.eclipse.jdt.annotation.NonNull List<String> logoSizes) {
        this.logoSizes = logoSizes;
    }

    public ConfigImageTypes posterSizes(@org.eclipse.jdt.annotation.NonNull List<String> posterSizes) {
        this.posterSizes = posterSizes;
        return this;
    }

    public ConfigImageTypes addPosterSizesItem(String posterSizesItem) {
        if (this.posterSizes == null) {
            this.posterSizes = new ArrayList<>();
        }
        this.posterSizes.add(posterSizesItem);
        return this;
    }

    /**
     * Get posterSizes
     * 
     * @return posterSizes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_POSTER_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getPosterSizes() {
        return posterSizes;
    }

    @JsonProperty(JSON_PROPERTY_POSTER_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPosterSizes(@org.eclipse.jdt.annotation.NonNull List<String> posterSizes) {
        this.posterSizes = posterSizes;
    }

    public ConfigImageTypes profileSizes(@org.eclipse.jdt.annotation.NonNull List<String> profileSizes) {
        this.profileSizes = profileSizes;
        return this;
    }

    public ConfigImageTypes addProfileSizesItem(String profileSizesItem) {
        if (this.profileSizes == null) {
            this.profileSizes = new ArrayList<>();
        }
        this.profileSizes.add(profileSizesItem);
        return this;
    }

    /**
     * Get profileSizes
     * 
     * @return profileSizes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROFILE_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getProfileSizes() {
        return profileSizes;
    }

    @JsonProperty(JSON_PROPERTY_PROFILE_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProfileSizes(@org.eclipse.jdt.annotation.NonNull List<String> profileSizes) {
        this.profileSizes = profileSizes;
    }

    public ConfigImageTypes secureBaseUrl(@org.eclipse.jdt.annotation.NonNull String secureBaseUrl) {
        this.secureBaseUrl = secureBaseUrl;
        return this;
    }

    /**
     * Get secureBaseUrl
     * 
     * @return secureBaseUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SECURE_BASE_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSecureBaseUrl() {
        return secureBaseUrl;
    }

    @JsonProperty(JSON_PROPERTY_SECURE_BASE_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSecureBaseUrl(@org.eclipse.jdt.annotation.NonNull String secureBaseUrl) {
        this.secureBaseUrl = secureBaseUrl;
    }

    public ConfigImageTypes stillSizes(@org.eclipse.jdt.annotation.NonNull List<String> stillSizes) {
        this.stillSizes = stillSizes;
        return this;
    }

    public ConfigImageTypes addStillSizesItem(String stillSizesItem) {
        if (this.stillSizes == null) {
            this.stillSizes = new ArrayList<>();
        }
        this.stillSizes.add(stillSizesItem);
        return this;
    }

    /**
     * Get stillSizes
     * 
     * @return stillSizes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_STILL_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getStillSizes() {
        return stillSizes;
    }

    @JsonProperty(JSON_PROPERTY_STILL_SIZES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStillSizes(@org.eclipse.jdt.annotation.NonNull List<String> stillSizes) {
        this.stillSizes = stillSizes;
    }

    /**
     * Return true if this ConfigImageTypes object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigImageTypes configImageTypes = (ConfigImageTypes) o;
        return Objects.equals(this.backdropSizes, configImageTypes.backdropSizes)
                && Objects.equals(this.baseUrl, configImageTypes.baseUrl)
                && Objects.equals(this.logoSizes, configImageTypes.logoSizes)
                && Objects.equals(this.posterSizes, configImageTypes.posterSizes)
                && Objects.equals(this.profileSizes, configImageTypes.profileSizes)
                && Objects.equals(this.secureBaseUrl, configImageTypes.secureBaseUrl)
                && Objects.equals(this.stillSizes, configImageTypes.stillSizes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backdropSizes, baseUrl, logoSizes, posterSizes, profileSizes, secureBaseUrl, stillSizes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConfigImageTypes {\n");
        sb.append("    backdropSizes: ").append(toIndentedString(backdropSizes)).append("\n");
        sb.append("    baseUrl: ").append(toIndentedString(baseUrl)).append("\n");
        sb.append("    logoSizes: ").append(toIndentedString(logoSizes)).append("\n");
        sb.append("    posterSizes: ").append(toIndentedString(posterSizes)).append("\n");
        sb.append("    profileSizes: ").append(toIndentedString(profileSizes)).append("\n");
        sb.append("    secureBaseUrl: ").append(toIndentedString(secureBaseUrl)).append("\n");
        sb.append("    stillSizes: ").append(toIndentedString(stillSizes)).append("\n");
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
