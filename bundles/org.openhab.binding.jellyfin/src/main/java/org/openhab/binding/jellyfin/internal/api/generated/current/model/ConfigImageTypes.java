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
 * ConfigImageTypes
 */
@JsonPropertyOrder({ ConfigImageTypes.JSON_PROPERTY_BACKDROP_SIZES, ConfigImageTypes.JSON_PROPERTY_BASE_URL,
        ConfigImageTypes.JSON_PROPERTY_LOGO_SIZES, ConfigImageTypes.JSON_PROPERTY_POSTER_SIZES,
        ConfigImageTypes.JSON_PROPERTY_PROFILE_SIZES, ConfigImageTypes.JSON_PROPERTY_SECURE_BASE_URL,
        ConfigImageTypes.JSON_PROPERTY_STILL_SIZES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
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
    @JsonProperty(value = JSON_PROPERTY_BACKDROP_SIZES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getBackdropSizes() {
        return backdropSizes;
    }

    @JsonProperty(value = JSON_PROPERTY_BACKDROP_SIZES, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_BASE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getBaseUrl() {
        return baseUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_BASE_URL, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_LOGO_SIZES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getLogoSizes() {
        return logoSizes;
    }

    @JsonProperty(value = JSON_PROPERTY_LOGO_SIZES, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_POSTER_SIZES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getPosterSizes() {
        return posterSizes;
    }

    @JsonProperty(value = JSON_PROPERTY_POSTER_SIZES, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_PROFILE_SIZES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getProfileSizes() {
        return profileSizes;
    }

    @JsonProperty(value = JSON_PROPERTY_PROFILE_SIZES, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_SECURE_BASE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSecureBaseUrl() {
        return secureBaseUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_SECURE_BASE_URL, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_STILL_SIZES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getStillSizes() {
        return stillSizes;
    }

    @JsonProperty(value = JSON_PROPERTY_STILL_SIZES, required = false)
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

        // add `BackdropSizes` to the URL query string
        if (getBackdropSizes() != null) {
            for (int i = 0; i < getBackdropSizes().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sBackdropSizes%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getBackdropSizes().get(i)))));
            }
        }

        // add `BaseUrl` to the URL query string
        if (getBaseUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sBaseUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBaseUrl()))));
        }

        // add `LogoSizes` to the URL query string
        if (getLogoSizes() != null) {
            for (int i = 0; i < getLogoSizes().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sLogoSizes%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getLogoSizes().get(i)))));
            }
        }

        // add `PosterSizes` to the URL query string
        if (getPosterSizes() != null) {
            for (int i = 0; i < getPosterSizes().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sPosterSizes%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getPosterSizes().get(i)))));
            }
        }

        // add `ProfileSizes` to the URL query string
        if (getProfileSizes() != null) {
            for (int i = 0; i < getProfileSizes().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sProfileSizes%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getProfileSizes().get(i)))));
            }
        }

        // add `SecureBaseUrl` to the URL query string
        if (getSecureBaseUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSecureBaseUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSecureBaseUrl()))));
        }

        // add `StillSizes` to the URL query string
        if (getStillSizes() != null) {
            for (int i = 0; i < getStillSizes().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sStillSizes%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getStillSizes().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private ConfigImageTypes instance;

        public Builder() {
            this(new ConfigImageTypes());
        }

        protected Builder(ConfigImageTypes instance) {
            this.instance = instance;
        }

        public ConfigImageTypes.Builder backdropSizes(List<String> backdropSizes) {
            this.instance.backdropSizes = backdropSizes;
            return this;
        }

        public ConfigImageTypes.Builder baseUrl(String baseUrl) {
            this.instance.baseUrl = baseUrl;
            return this;
        }

        public ConfigImageTypes.Builder logoSizes(List<String> logoSizes) {
            this.instance.logoSizes = logoSizes;
            return this;
        }

        public ConfigImageTypes.Builder posterSizes(List<String> posterSizes) {
            this.instance.posterSizes = posterSizes;
            return this;
        }

        public ConfigImageTypes.Builder profileSizes(List<String> profileSizes) {
            this.instance.profileSizes = profileSizes;
            return this;
        }

        public ConfigImageTypes.Builder secureBaseUrl(String secureBaseUrl) {
            this.instance.secureBaseUrl = secureBaseUrl;
            return this;
        }

        public ConfigImageTypes.Builder stillSizes(List<String> stillSizes) {
            this.instance.stillSizes = stillSizes;
            return this;
        }

        /**
         * returns a built ConfigImageTypes instance.
         *
         * The builder is not reusable.
         */
        public ConfigImageTypes build() {
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
    public static ConfigImageTypes.Builder builder() {
        return new ConfigImageTypes.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ConfigImageTypes.Builder toBuilder() {
        return new ConfigImageTypes.Builder().backdropSizes(getBackdropSizes()).baseUrl(getBaseUrl())
                .logoSizes(getLogoSizes()).posterSizes(getPosterSizes()).profileSizes(getProfileSizes())
                .secureBaseUrl(getSecureBaseUrl()).stillSizes(getStillSizes());
    }
}
