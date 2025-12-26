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
 * Class ServiceInfo.
 */
@JsonPropertyOrder({ LiveTvServiceInfo.JSON_PROPERTY_NAME, LiveTvServiceInfo.JSON_PROPERTY_HOME_PAGE_URL,
        LiveTvServiceInfo.JSON_PROPERTY_STATUS, LiveTvServiceInfo.JSON_PROPERTY_STATUS_MESSAGE,
        LiveTvServiceInfo.JSON_PROPERTY_VERSION, LiveTvServiceInfo.JSON_PROPERTY_HAS_UPDATE_AVAILABLE,
        LiveTvServiceInfo.JSON_PROPERTY_IS_VISIBLE, LiveTvServiceInfo.JSON_PROPERTY_TUNERS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LiveTvServiceInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_HOME_PAGE_URL = "HomePageUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String homePageUrl;

    public static final String JSON_PROPERTY_STATUS = "Status";
    @org.eclipse.jdt.annotation.NonNull
    private LiveTvServiceStatus status;

    public static final String JSON_PROPERTY_STATUS_MESSAGE = "StatusMessage";
    @org.eclipse.jdt.annotation.NonNull
    private String statusMessage;

    public static final String JSON_PROPERTY_VERSION = "Version";
    @org.eclipse.jdt.annotation.NonNull
    private String version;

    public static final String JSON_PROPERTY_HAS_UPDATE_AVAILABLE = "HasUpdateAvailable";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasUpdateAvailable;

    public static final String JSON_PROPERTY_IS_VISIBLE = "IsVisible";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isVisible;

    public static final String JSON_PROPERTY_TUNERS = "Tuners";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> tuners;

    public LiveTvServiceInfo() {
    }

    public LiveTvServiceInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public LiveTvServiceInfo homePageUrl(@org.eclipse.jdt.annotation.NonNull String homePageUrl) {
        this.homePageUrl = homePageUrl;
        return this;
    }

    /**
     * Gets or sets the home page URL.
     * 
     * @return homePageUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HOME_PAGE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getHomePageUrl() {
        return homePageUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_HOME_PAGE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHomePageUrl(@org.eclipse.jdt.annotation.NonNull String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    public LiveTvServiceInfo status(@org.eclipse.jdt.annotation.NonNull LiveTvServiceStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Gets or sets the status.
     * 
     * @return status
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public LiveTvServiceStatus getStatus() {
        return status;
    }

    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatus(@org.eclipse.jdt.annotation.NonNull LiveTvServiceStatus status) {
        this.status = status;
    }

    public LiveTvServiceInfo statusMessage(@org.eclipse.jdt.annotation.NonNull String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    /**
     * Gets or sets the status message.
     * 
     * @return statusMessage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STATUS_MESSAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getStatusMessage() {
        return statusMessage;
    }

    @JsonProperty(value = JSON_PROPERTY_STATUS_MESSAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatusMessage(@org.eclipse.jdt.annotation.NonNull String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public LiveTvServiceInfo version(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
        return this;
    }

    /**
     * Gets or sets the version.
     * 
     * @return version
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVersion() {
        return version;
    }

    @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVersion(@org.eclipse.jdt.annotation.NonNull String version) {
        this.version = version;
    }

    public LiveTvServiceInfo hasUpdateAvailable(@org.eclipse.jdt.annotation.NonNull Boolean hasUpdateAvailable) {
        this.hasUpdateAvailable = hasUpdateAvailable;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance has update available.
     * 
     * @return hasUpdateAvailable
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HAS_UPDATE_AVAILABLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasUpdateAvailable() {
        return hasUpdateAvailable;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_UPDATE_AVAILABLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasUpdateAvailable(@org.eclipse.jdt.annotation.NonNull Boolean hasUpdateAvailable) {
        this.hasUpdateAvailable = hasUpdateAvailable;
    }

    public LiveTvServiceInfo isVisible(@org.eclipse.jdt.annotation.NonNull Boolean isVisible) {
        this.isVisible = isVisible;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is visible.
     * 
     * @return isVisible
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_VISIBLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsVisible() {
        return isVisible;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_VISIBLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsVisible(@org.eclipse.jdt.annotation.NonNull Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public LiveTvServiceInfo tuners(@org.eclipse.jdt.annotation.NonNull List<String> tuners) {
        this.tuners = tuners;
        return this;
    }

    public LiveTvServiceInfo addTunersItem(String tunersItem) {
        if (this.tuners == null) {
            this.tuners = new ArrayList<>();
        }
        this.tuners.add(tunersItem);
        return this;
    }

    /**
     * Get tuners
     * 
     * @return tuners
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TUNERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getTuners() {
        return tuners;
    }

    @JsonProperty(value = JSON_PROPERTY_TUNERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTuners(@org.eclipse.jdt.annotation.NonNull List<String> tuners) {
        this.tuners = tuners;
    }

    /**
     * Return true if this LiveTvServiceInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveTvServiceInfo liveTvServiceInfo = (LiveTvServiceInfo) o;
        return Objects.equals(this.name, liveTvServiceInfo.name)
                && Objects.equals(this.homePageUrl, liveTvServiceInfo.homePageUrl)
                && Objects.equals(this.status, liveTvServiceInfo.status)
                && Objects.equals(this.statusMessage, liveTvServiceInfo.statusMessage)
                && Objects.equals(this.version, liveTvServiceInfo.version)
                && Objects.equals(this.hasUpdateAvailable, liveTvServiceInfo.hasUpdateAvailable)
                && Objects.equals(this.isVisible, liveTvServiceInfo.isVisible)
                && Objects.equals(this.tuners, liveTvServiceInfo.tuners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, homePageUrl, status, statusMessage, version, hasUpdateAvailable, isVisible, tuners);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LiveTvServiceInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    homePageUrl: ").append(toIndentedString(homePageUrl)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    statusMessage: ").append(toIndentedString(statusMessage)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    hasUpdateAvailable: ").append(toIndentedString(hasUpdateAvailable)).append("\n");
        sb.append("    isVisible: ").append(toIndentedString(isVisible)).append("\n");
        sb.append("    tuners: ").append(toIndentedString(tuners)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `HomePageUrl` to the URL query string
        if (getHomePageUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHomePageUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHomePageUrl()))));
        }

        // add `Status` to the URL query string
        if (getStatus() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStatus%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
        }

        // add `StatusMessage` to the URL query string
        if (getStatusMessage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStatusMessage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStatusMessage()))));
        }

        // add `Version` to the URL query string
        if (getVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVersion()))));
        }

        // add `HasUpdateAvailable` to the URL query string
        if (getHasUpdateAvailable() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasUpdateAvailable%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasUpdateAvailable()))));
        }

        // add `IsVisible` to the URL query string
        if (getIsVisible() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsVisible%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsVisible()))));
        }

        // add `Tuners` to the URL query string
        if (getTuners() != null) {
            for (int i = 0; i < getTuners().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sTuners%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getTuners().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private LiveTvServiceInfo instance;

        public Builder() {
            this(new LiveTvServiceInfo());
        }

        protected Builder(LiveTvServiceInfo instance) {
            this.instance = instance;
        }

        public LiveTvServiceInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public LiveTvServiceInfo.Builder homePageUrl(String homePageUrl) {
            this.instance.homePageUrl = homePageUrl;
            return this;
        }

        public LiveTvServiceInfo.Builder status(LiveTvServiceStatus status) {
            this.instance.status = status;
            return this;
        }

        public LiveTvServiceInfo.Builder statusMessage(String statusMessage) {
            this.instance.statusMessage = statusMessage;
            return this;
        }

        public LiveTvServiceInfo.Builder version(String version) {
            this.instance.version = version;
            return this;
        }

        public LiveTvServiceInfo.Builder hasUpdateAvailable(Boolean hasUpdateAvailable) {
            this.instance.hasUpdateAvailable = hasUpdateAvailable;
            return this;
        }

        public LiveTvServiceInfo.Builder isVisible(Boolean isVisible) {
            this.instance.isVisible = isVisible;
            return this;
        }

        public LiveTvServiceInfo.Builder tuners(List<String> tuners) {
            this.instance.tuners = tuners;
            return this;
        }

        /**
         * returns a built LiveTvServiceInfo instance.
         *
         * The builder is not reusable.
         */
        public LiveTvServiceInfo build() {
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
    public static LiveTvServiceInfo.Builder builder() {
        return new LiveTvServiceInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LiveTvServiceInfo.Builder toBuilder() {
        return new LiveTvServiceInfo.Builder().name(getName()).homePageUrl(getHomePageUrl()).status(getStatus())
                .statusMessage(getStatusMessage()).version(getVersion()).hasUpdateAvailable(getHasUpdateAvailable())
                .isVisible(getIsVisible()).tuners(getTuners());
    }
}
