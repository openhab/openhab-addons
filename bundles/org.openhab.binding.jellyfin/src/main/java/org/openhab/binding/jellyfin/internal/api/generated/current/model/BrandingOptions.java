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
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The branding options.
 */
@JsonPropertyOrder({ BrandingOptions.JSON_PROPERTY_LOGIN_DISCLAIMER, BrandingOptions.JSON_PROPERTY_CUSTOM_CSS,
        BrandingOptions.JSON_PROPERTY_SPLASHSCREEN_ENABLED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BrandingOptions {
    public static final String JSON_PROPERTY_LOGIN_DISCLAIMER = "LoginDisclaimer";
    @org.eclipse.jdt.annotation.NonNull
    private String loginDisclaimer;

    public static final String JSON_PROPERTY_CUSTOM_CSS = "CustomCss";
    @org.eclipse.jdt.annotation.NonNull
    private String customCss;

    public static final String JSON_PROPERTY_SPLASHSCREEN_ENABLED = "SplashscreenEnabled";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean splashscreenEnabled;

    public BrandingOptions() {
    }

    public BrandingOptions loginDisclaimer(@org.eclipse.jdt.annotation.NonNull String loginDisclaimer) {
        this.loginDisclaimer = loginDisclaimer;
        return this;
    }

    /**
     * Gets or sets the login disclaimer.
     * 
     * @return loginDisclaimer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LOGIN_DISCLAIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLoginDisclaimer() {
        return loginDisclaimer;
    }

    @JsonProperty(JSON_PROPERTY_LOGIN_DISCLAIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLoginDisclaimer(@org.eclipse.jdt.annotation.NonNull String loginDisclaimer) {
        this.loginDisclaimer = loginDisclaimer;
    }

    public BrandingOptions customCss(@org.eclipse.jdt.annotation.NonNull String customCss) {
        this.customCss = customCss;
        return this;
    }

    /**
     * Gets or sets the custom CSS.
     * 
     * @return customCss
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CUSTOM_CSS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCustomCss() {
        return customCss;
    }

    @JsonProperty(JSON_PROPERTY_CUSTOM_CSS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomCss(@org.eclipse.jdt.annotation.NonNull String customCss) {
        this.customCss = customCss;
    }

    public BrandingOptions splashscreenEnabled(@org.eclipse.jdt.annotation.NonNull Boolean splashscreenEnabled) {
        this.splashscreenEnabled = splashscreenEnabled;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to enable the splashscreen.
     * 
     * @return splashscreenEnabled
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SPLASHSCREEN_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSplashscreenEnabled() {
        return splashscreenEnabled;
    }

    @JsonProperty(JSON_PROPERTY_SPLASHSCREEN_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSplashscreenEnabled(@org.eclipse.jdt.annotation.NonNull Boolean splashscreenEnabled) {
        this.splashscreenEnabled = splashscreenEnabled;
    }

    /**
     * Return true if this BrandingOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BrandingOptions brandingOptions = (BrandingOptions) o;
        return Objects.equals(this.loginDisclaimer, brandingOptions.loginDisclaimer)
                && Objects.equals(this.customCss, brandingOptions.customCss)
                && Objects.equals(this.splashscreenEnabled, brandingOptions.splashscreenEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loginDisclaimer, customCss, splashscreenEnabled);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BrandingOptions {\n");
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
            joiner.add(String.format("%sLoginDisclaimer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLoginDisclaimer()))));
        }

        // add `CustomCss` to the URL query string
        if (getCustomCss() != null) {
            joiner.add(String.format("%sCustomCss%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCustomCss()))));
        }

        // add `SplashscreenEnabled` to the URL query string
        if (getSplashscreenEnabled() != null) {
            joiner.add(String.format("%sSplashscreenEnabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSplashscreenEnabled()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BrandingOptions instance;

        public Builder() {
            this(new BrandingOptions());
        }

        protected Builder(BrandingOptions instance) {
            this.instance = instance;
        }

        public BrandingOptions.Builder loginDisclaimer(String loginDisclaimer) {
            this.instance.loginDisclaimer = loginDisclaimer;
            return this;
        }

        public BrandingOptions.Builder customCss(String customCss) {
            this.instance.customCss = customCss;
            return this;
        }

        public BrandingOptions.Builder splashscreenEnabled(Boolean splashscreenEnabled) {
            this.instance.splashscreenEnabled = splashscreenEnabled;
            return this;
        }

        /**
         * returns a built BrandingOptions instance.
         *
         * The builder is not reusable.
         */
        public BrandingOptions build() {
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
    public static BrandingOptions.Builder builder() {
        return new BrandingOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BrandingOptions.Builder toBuilder() {
        return new BrandingOptions.Builder().loginDisclaimer(getLoginDisclaimer()).customCss(getCustomCss())
                .splashscreenEnabled(getSplashscreenEnabled());
    }
}
