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
 * The branding options.
 */
@JsonPropertyOrder({ BrandingOptions.JSON_PROPERTY_LOGIN_DISCLAIMER, BrandingOptions.JSON_PROPERTY_CUSTOM_CSS,
        BrandingOptions.JSON_PROPERTY_SPLASHSCREEN_ENABLED })

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
}
