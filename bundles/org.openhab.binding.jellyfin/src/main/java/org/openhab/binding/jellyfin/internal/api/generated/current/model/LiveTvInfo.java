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
 * LiveTvInfo
 */
@JsonPropertyOrder({ LiveTvInfo.JSON_PROPERTY_SERVICES, LiveTvInfo.JSON_PROPERTY_IS_ENABLED,
        LiveTvInfo.JSON_PROPERTY_ENABLED_USERS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LiveTvInfo {
    public static final String JSON_PROPERTY_SERVICES = "Services";
    @org.eclipse.jdt.annotation.NonNull
    private List<LiveTvServiceInfo> services = new ArrayList<>();

    public static final String JSON_PROPERTY_IS_ENABLED = "IsEnabled";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isEnabled;

    public static final String JSON_PROPERTY_ENABLED_USERS = "EnabledUsers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> enabledUsers = new ArrayList<>();

    public LiveTvInfo() {
    }

    public LiveTvInfo services(@org.eclipse.jdt.annotation.NonNull List<LiveTvServiceInfo> services) {
        this.services = services;
        return this;
    }

    public LiveTvInfo addServicesItem(LiveTvServiceInfo servicesItem) {
        if (this.services == null) {
            this.services = new ArrayList<>();
        }
        this.services.add(servicesItem);
        return this;
    }

    /**
     * Gets or sets the services.
     * 
     * @return services
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERVICES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LiveTvServiceInfo> getServices() {
        return services;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVICES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServices(@org.eclipse.jdt.annotation.NonNull List<LiveTvServiceInfo> services) {
        this.services = services;
    }

    public LiveTvInfo isEnabled(@org.eclipse.jdt.annotation.NonNull Boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is enabled.
     * 
     * @return isEnabled
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsEnabled() {
        return isEnabled;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsEnabled(@org.eclipse.jdt.annotation.NonNull Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public LiveTvInfo enabledUsers(@org.eclipse.jdt.annotation.NonNull List<String> enabledUsers) {
        this.enabledUsers = enabledUsers;
        return this;
    }

    public LiveTvInfo addEnabledUsersItem(String enabledUsersItem) {
        if (this.enabledUsers == null) {
            this.enabledUsers = new ArrayList<>();
        }
        this.enabledUsers.add(enabledUsersItem);
        return this;
    }

    /**
     * Gets or sets the enabled users.
     * 
     * @return enabledUsers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLED_USERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getEnabledUsers() {
        return enabledUsers;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLED_USERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabledUsers(@org.eclipse.jdt.annotation.NonNull List<String> enabledUsers) {
        this.enabledUsers = enabledUsers;
    }

    /**
     * Return true if this LiveTvInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveTvInfo liveTvInfo = (LiveTvInfo) o;
        return Objects.equals(this.services, liveTvInfo.services)
                && Objects.equals(this.isEnabled, liveTvInfo.isEnabled)
                && Objects.equals(this.enabledUsers, liveTvInfo.enabledUsers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(services, isEnabled, enabledUsers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LiveTvInfo {\n");
        sb.append("    services: ").append(toIndentedString(services)).append("\n");
        sb.append("    isEnabled: ").append(toIndentedString(isEnabled)).append("\n");
        sb.append("    enabledUsers: ").append(toIndentedString(enabledUsers)).append("\n");
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

        // add `Services` to the URL query string
        if (getServices() != null) {
            for (int i = 0; i < getServices().size(); i++) {
                if (getServices().get(i) != null) {
                    joiner.add(getServices().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sServices%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `IsEnabled` to the URL query string
        if (getIsEnabled() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsEnabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsEnabled()))));
        }

        // add `EnabledUsers` to the URL query string
        if (getEnabledUsers() != null) {
            for (int i = 0; i < getEnabledUsers().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sEnabledUsers%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getEnabledUsers().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private LiveTvInfo instance;

        public Builder() {
            this(new LiveTvInfo());
        }

        protected Builder(LiveTvInfo instance) {
            this.instance = instance;
        }

        public LiveTvInfo.Builder services(List<LiveTvServiceInfo> services) {
            this.instance.services = services;
            return this;
        }

        public LiveTvInfo.Builder isEnabled(Boolean isEnabled) {
            this.instance.isEnabled = isEnabled;
            return this;
        }

        public LiveTvInfo.Builder enabledUsers(List<String> enabledUsers) {
            this.instance.enabledUsers = enabledUsers;
            return this;
        }

        /**
         * returns a built LiveTvInfo instance.
         *
         * The builder is not reusable.
         */
        public LiveTvInfo build() {
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
    public static LiveTvInfo.Builder builder() {
        return new LiveTvInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LiveTvInfo.Builder toBuilder() {
        return new LiveTvInfo.Builder().services(getServices()).isEnabled(getIsEnabled())
                .enabledUsers(getEnabledUsers());
    }
}
