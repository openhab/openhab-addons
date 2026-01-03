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
 * The branding options DTO for API use. This DTO excludes SplashscreenLocation to prevent it from being updated via
 * API.
 */
@JsonPropertyOrder({ BrandingOptionsDto.JSON_PROPERTY_LOGIN_DISCLAIMER, BrandingOptionsDto.JSON_PROPERTY_CUSTOM_CSS,
        BrandingOptionsDto.JSON_PROPERTY_SPLASHSCREEN_ENABLED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BrandingOptionsDto {
    public static final String JSON_PROPERTY_LOGIN_DISCLAIMER = "LoginDisclaimer";
    @org.eclipse.jdt.annotation.Nullable
    private String loginDisclaimer;

    public static final String JSON_PROPERTY_CUSTOM_CSS = "CustomCss";
    @org.eclipse.jdt.annotation.Nullable
    private String customCss;

    public static final String JSON_PROPERTY_SPLASHSCREEN_ENABLED = "SplashscreenEnabled";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean splashscreenEnabled;

    public BrandingOptionsDto() {
    }

    public BrandingOptionsDto loginDisclaimer(@org.eclipse.jdt.annotation.Nullable String loginDisclaimer) {
        this.loginDisclaimer = loginDisclaimer;
        return this;
    }

    /**
     * Gets or sets the login disclaimer.
     * 
     * @return loginDisclaimer
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOGIN_DISCLAIMER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLoginDisclaimer() {
        return loginDisclaimer;
    }

    @JsonProperty(value = JSON_PROPERTY_LOGIN_DISCLAIMER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLoginDisclaimer(@org.eclipse.jdt.annotation.Nullable String loginDisclaimer) {
        this.loginDisclaimer = loginDisclaimer;
    }

    public BrandingOptionsDto customCss(@org.eclipse.jdt.annotation.Nullable String customCss) {
        this.customCss = customCss;
        return this;
    }

    /**
     * Gets or sets the custom CSS.
     * 
     * @return customCss
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CUSTOM_CSS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCustomCss() {
        return customCss;
    }

    @JsonProperty(value = JSON_PROPERTY_CUSTOM_CSS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomCss(@org.eclipse.jdt.annotation.Nullable String customCss) {
        this.customCss = customCss;
    }

    public BrandingOptionsDto splashscreenEnabled(@org.eclipse.jdt.annotation.Nullable Boolean splashscreenEnabled) {
        this.splashscreenEnabled = splashscreenEnabled;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to enable the splashscreen.
     * 
     * @return splashscreenEnabled
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SPLASHSCREEN_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSplashscreenEnabled() {
        return splashscreenEnabled;
    }

    @JsonProperty(value = JSON_PROPERTY_SPLASHSCREEN_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSplashscreenEnabled(@org.eclipse.jdt.annotation.Nullable Boolean splashscreenEnabled) {
        this.splashscreenEnabled = splashscreenEnabled;
    }

    /**
     * Return true if this BrandingOptionsDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BrandingOptionsDto brandingOptionsDto = (BrandingOptionsDto) o;
        return Objects.equals(this.loginDisclaimer, brandingOptionsDto.loginDisclaimer)
                && Objects.equals(this.customCss, brandingOptionsDto.customCss)
                && Objects.equals(this.splashscreenEnabled, brandingOptionsDto.splashscreenEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loginDisclaimer, customCss, splashscreenEnabled);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BrandingOptionsDto {\n");
        sb.append("    loginDisclaimer: ").append(toIndentedString(loginDisclaimer)).append("\n");
        sb.append("    customCss: ").append(toIndentedString(customCss)).append("\n");
        sb.append("    splashscreenEnabled: ").append(toIndentedString(splashscreenEnabled)).append("\n");
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

        // add `LoginDisclaimer` to the URL query string
        if (getLoginDisclaimer() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLoginDisclaimer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLoginDisclaimer()))));
        }

        // add `CustomCss` to the URL query string
        if (getCustomCss() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCustomCss%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCustomCss()))));
        }

        // add `SplashscreenEnabled` to the URL query string
        if (getSplashscreenEnabled() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSplashscreenEnabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSplashscreenEnabled()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BrandingOptionsDto instance;

        public Builder() {
            this(new BrandingOptionsDto());
        }

        protected Builder(BrandingOptionsDto instance) {
            this.instance = instance;
        }

        public BrandingOptionsDto.Builder loginDisclaimer(String loginDisclaimer) {
            this.instance.loginDisclaimer = loginDisclaimer;
            return this;
        }

        public BrandingOptionsDto.Builder customCss(String customCss) {
            this.instance.customCss = customCss;
            return this;
        }

        public BrandingOptionsDto.Builder splashscreenEnabled(Boolean splashscreenEnabled) {
            this.instance.splashscreenEnabled = splashscreenEnabled;
            return this;
        }

        /**
         * returns a built BrandingOptionsDto instance.
         *
         * The builder is not reusable.
         */
        public BrandingOptionsDto build() {
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
    public static BrandingOptionsDto.Builder builder() {
        return new BrandingOptionsDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BrandingOptionsDto.Builder toBuilder() {
        return new BrandingOptionsDto.Builder().loginDisclaimer(getLoginDisclaimer()).customCss(getCustomCss())
                .splashscreenEnabled(getSplashscreenEnabled());
    }
}
