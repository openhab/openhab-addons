/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lghorizon.internal.api.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Response body of {@code GET /personalizationService/v1/customer/{householdId}?with=profiles,devices}.
 *
 * @author Mark Herwege - Initial contribution
 */
public class CustomerDto {

    @SerializedName("customerId")
    public String customerId;

    @SerializedName("countryId")
    public String countryId;

    @SerializedName("cityId")
    public Integer cityId;

    @SerializedName("profiles")
    public List<ProfileDto> profiles;

    @SerializedName("assignedDevices")
    public List<DeviceDto> assignedDevices;

    @Override
    public String toString() {
        return "CustomerDto [customerId=" + customerId + ", countryId=" + countryId + ", cityId=" + cityId
                + ", profiles=" + profiles + ", assignedDevices=" + assignedDevices + "]";
    }

    public static class ProfileDto {
        @SerializedName("profileId")
        public String profileId;

        @SerializedName("name")
        public String name;

        @SerializedName("favoriteChannels")
        public List<String> favoriteChannels;

        @SerializedName("options")
        public OptionsDto options;

        @Override
        public String toString() {
            return "ProfileDto [profileId=" + profileId + ", name=" + name + ", favoriteChannels=" + favoriteChannels
                    + ", options=" + options + "]";
        }

        public static class OptionsDto {
            @SerializedName("lang")
            public String lang;

            @Override
            public String toString() {
                return "OptionsDto [lang=" + lang + "]";
            }
        }
    }

    public static class DeviceDto {
        @SerializedName("deviceId")
        public String deviceId;

        @SerializedName("hashedCPEId")
        public String hashedCpeId;

        @SerializedName("defaultProfileId")
        public String defaultProfileId;

        @SerializedName("deviceType")
        public String deviceType;

        @SerializedName("platformType")
        public String platformType;

        @SerializedName("serialNumber")
        public String serialNumber;

        @SerializedName("wifiMacAddress")
        public String wifiMacAddress;

        @SerializedName("ethernetMacAddress")
        public String ethernetMacAddress;

        @Override
        public String toString() {
            return "DeviceDto [deviceId=" + deviceId + ", hashedCpeId=" + hashedCpeId + ", deviceType=" + deviceType
                    + ", defaultProfileId=" + defaultProfileId + ", platformType=" + platformType + ", settings="
                    + settings + "]";
        }

        @SerializedName("settings")
        public SettingsDto settings;

        public static class SettingsDto {
            @SerializedName("deviceFriendlyName")
            public String deviceFriendlyName;

            @Override
            public String toString() {
                return "SettingsDto [deviceFriendlyName=" + deviceFriendlyName + "]";
            }
        }
    }
}
