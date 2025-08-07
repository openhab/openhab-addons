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
 * LiveTvInfo
 */
@JsonPropertyOrder({ LiveTvInfo.JSON_PROPERTY_SERVICES, LiveTvInfo.JSON_PROPERTY_IS_ENABLED,
        LiveTvInfo.JSON_PROPERTY_ENABLED_USERS })

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
    @JsonProperty(JSON_PROPERTY_SERVICES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LiveTvServiceInfo> getServices() {
        return services;
    }

    @JsonProperty(JSON_PROPERTY_SERVICES)
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
    @JsonProperty(JSON_PROPERTY_IS_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    @JsonProperty(JSON_PROPERTY_IS_ENABLED)
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
    @JsonProperty(JSON_PROPERTY_ENABLED_USERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getEnabledUsers() {
        return enabledUsers;
    }

    @JsonProperty(JSON_PROPERTY_ENABLED_USERS)
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
}
