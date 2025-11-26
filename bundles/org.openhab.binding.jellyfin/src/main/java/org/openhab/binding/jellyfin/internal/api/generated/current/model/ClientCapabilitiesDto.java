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
 * Client capabilities dto.
 */
@JsonPropertyOrder({ ClientCapabilitiesDto.JSON_PROPERTY_PLAYABLE_MEDIA_TYPES,
        ClientCapabilitiesDto.JSON_PROPERTY_SUPPORTED_COMMANDS,
        ClientCapabilitiesDto.JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL,
        ClientCapabilitiesDto.JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER,
        ClientCapabilitiesDto.JSON_PROPERTY_DEVICE_PROFILE, ClientCapabilitiesDto.JSON_PROPERTY_APP_STORE_URL,
        ClientCapabilitiesDto.JSON_PROPERTY_ICON_URL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ClientCapabilitiesDto {
    public static final String JSON_PROPERTY_PLAYABLE_MEDIA_TYPES = "PlayableMediaTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaType> playableMediaTypes = new ArrayList<>();

    public static final String JSON_PROPERTY_SUPPORTED_COMMANDS = "SupportedCommands";
    @org.eclipse.jdt.annotation.NonNull
    private List<GeneralCommandType> supportedCommands = new ArrayList<>();

    public static final String JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL = "SupportsMediaControl";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsMediaControl;

    public static final String JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER = "SupportsPersistentIdentifier";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsPersistentIdentifier;

    public static final String JSON_PROPERTY_DEVICE_PROFILE = "DeviceProfile";
    @org.eclipse.jdt.annotation.NonNull
    private DeviceProfile deviceProfile;

    public static final String JSON_PROPERTY_APP_STORE_URL = "AppStoreUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String appStoreUrl;

    public static final String JSON_PROPERTY_ICON_URL = "IconUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String iconUrl;

    public ClientCapabilitiesDto() {
    }

    public ClientCapabilitiesDto playableMediaTypes(
            @org.eclipse.jdt.annotation.NonNull List<MediaType> playableMediaTypes) {
        this.playableMediaTypes = playableMediaTypes;
        return this;
    }

    public ClientCapabilitiesDto addPlayableMediaTypesItem(MediaType playableMediaTypesItem) {
        if (this.playableMediaTypes == null) {
            this.playableMediaTypes = new ArrayList<>();
        }
        this.playableMediaTypes.add(playableMediaTypesItem);
        return this;
    }

    /**
     * Gets or sets the list of playable media types.
     * 
     * @return playableMediaTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYABLE_MEDIA_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaType> getPlayableMediaTypes() {
        return playableMediaTypes;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYABLE_MEDIA_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayableMediaTypes(@org.eclipse.jdt.annotation.NonNull List<MediaType> playableMediaTypes) {
        this.playableMediaTypes = playableMediaTypes;
    }

    public ClientCapabilitiesDto supportedCommands(
            @org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands) {
        this.supportedCommands = supportedCommands;
        return this;
    }

    public ClientCapabilitiesDto addSupportedCommandsItem(GeneralCommandType supportedCommandsItem) {
        if (this.supportedCommands == null) {
            this.supportedCommands = new ArrayList<>();
        }
        this.supportedCommands.add(supportedCommandsItem);
        return this;
    }

    /**
     * Gets or sets the list of supported commands.
     * 
     * @return supportedCommands
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTED_COMMANDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<GeneralCommandType> getSupportedCommands() {
        return supportedCommands;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTED_COMMANDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedCommands(@org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands) {
        this.supportedCommands = supportedCommands;
    }

    public ClientCapabilitiesDto supportsMediaControl(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl) {
        this.supportsMediaControl = supportsMediaControl;
        return this;
    }

    /**
     * Gets or sets a value indicating whether session supports media control.
     * 
     * @return supportsMediaControl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsMediaControl() {
        return supportsMediaControl;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsMediaControl(@org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl) {
        this.supportsMediaControl = supportsMediaControl;
    }

    public ClientCapabilitiesDto supportsPersistentIdentifier(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsPersistentIdentifier) {
        this.supportsPersistentIdentifier = supportsPersistentIdentifier;
        return this;
    }

    /**
     * Gets or sets a value indicating whether session supports a persistent identifier.
     * 
     * @return supportsPersistentIdentifier
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsPersistentIdentifier() {
        return supportsPersistentIdentifier;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsPersistentIdentifier(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsPersistentIdentifier) {
        this.supportsPersistentIdentifier = supportsPersistentIdentifier;
    }

    public ClientCapabilitiesDto deviceProfile(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
        return this;
    }

    /**
     * Gets or sets the device profile.
     * 
     * @return deviceProfile
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_PROFILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_PROFILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceProfile(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
    }

    public ClientCapabilitiesDto appStoreUrl(@org.eclipse.jdt.annotation.NonNull String appStoreUrl) {
        this.appStoreUrl = appStoreUrl;
        return this;
    }

    /**
     * Gets or sets the app store url.
     * 
     * @return appStoreUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_APP_STORE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAppStoreUrl() {
        return appStoreUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_APP_STORE_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppStoreUrl(@org.eclipse.jdt.annotation.NonNull String appStoreUrl) {
        this.appStoreUrl = appStoreUrl;
    }

    public ClientCapabilitiesDto iconUrl(@org.eclipse.jdt.annotation.NonNull String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    /**
     * Gets or sets the icon url.
     * 
     * @return iconUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ICON_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getIconUrl() {
        return iconUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_ICON_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIconUrl(@org.eclipse.jdt.annotation.NonNull String iconUrl) {
        this.iconUrl = iconUrl;
    }

    /**
     * Return true if this ClientCapabilitiesDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientCapabilitiesDto clientCapabilitiesDto = (ClientCapabilitiesDto) o;
        return Objects.equals(this.playableMediaTypes, clientCapabilitiesDto.playableMediaTypes)
                && Objects.equals(this.supportedCommands, clientCapabilitiesDto.supportedCommands)
                && Objects.equals(this.supportsMediaControl, clientCapabilitiesDto.supportsMediaControl)
                && Objects.equals(this.supportsPersistentIdentifier, clientCapabilitiesDto.supportsPersistentIdentifier)
                && Objects.equals(this.deviceProfile, clientCapabilitiesDto.deviceProfile)
                && Objects.equals(this.appStoreUrl, clientCapabilitiesDto.appStoreUrl)
                && Objects.equals(this.iconUrl, clientCapabilitiesDto.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playableMediaTypes, supportedCommands, supportsMediaControl, supportsPersistentIdentifier,
                deviceProfile, appStoreUrl, iconUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClientCapabilitiesDto {\n");
        sb.append("    playableMediaTypes: ").append(toIndentedString(playableMediaTypes)).append("\n");
        sb.append("    supportedCommands: ").append(toIndentedString(supportedCommands)).append("\n");
        sb.append("    supportsMediaControl: ").append(toIndentedString(supportsMediaControl)).append("\n");
        sb.append("    supportsPersistentIdentifier: ").append(toIndentedString(supportsPersistentIdentifier))
                .append("\n");
        sb.append("    deviceProfile: ").append(toIndentedString(deviceProfile)).append("\n");
        sb.append("    appStoreUrl: ").append(toIndentedString(appStoreUrl)).append("\n");
        sb.append("    iconUrl: ").append(toIndentedString(iconUrl)).append("\n");
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

        // add `PlayableMediaTypes` to the URL query string
        if (getPlayableMediaTypes() != null) {
            for (int i = 0; i < getPlayableMediaTypes().size(); i++) {
                if (getPlayableMediaTypes().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sPlayableMediaTypes%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getPlayableMediaTypes().get(i)))));
                }
            }
        }

        // add `SupportedCommands` to the URL query string
        if (getSupportedCommands() != null) {
            for (int i = 0; i < getSupportedCommands().size(); i++) {
                if (getSupportedCommands().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sSupportedCommands%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getSupportedCommands().get(i)))));
                }
            }
        }

        // add `SupportsMediaControl` to the URL query string
        if (getSupportsMediaControl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSupportsMediaControl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsMediaControl()))));
        }

        // add `SupportsPersistentIdentifier` to the URL query string
        if (getSupportsPersistentIdentifier() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSupportsPersistentIdentifier%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsPersistentIdentifier()))));
        }

        // add `DeviceProfile` to the URL query string
        if (getDeviceProfile() != null) {
            joiner.add(getDeviceProfile().toUrlQueryString(prefix + "DeviceProfile" + suffix));
        }

        // add `AppStoreUrl` to the URL query string
        if (getAppStoreUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAppStoreUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAppStoreUrl()))));
        }

        // add `IconUrl` to the URL query string
        if (getIconUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIconUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIconUrl()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ClientCapabilitiesDto instance;

        public Builder() {
            this(new ClientCapabilitiesDto());
        }

        protected Builder(ClientCapabilitiesDto instance) {
            this.instance = instance;
        }

        public ClientCapabilitiesDto.Builder playableMediaTypes(List<MediaType> playableMediaTypes) {
            this.instance.playableMediaTypes = playableMediaTypes;
            return this;
        }

        public ClientCapabilitiesDto.Builder supportedCommands(List<GeneralCommandType> supportedCommands) {
            this.instance.supportedCommands = supportedCommands;
            return this;
        }

        public ClientCapabilitiesDto.Builder supportsMediaControl(Boolean supportsMediaControl) {
            this.instance.supportsMediaControl = supportsMediaControl;
            return this;
        }

        public ClientCapabilitiesDto.Builder supportsPersistentIdentifier(Boolean supportsPersistentIdentifier) {
            this.instance.supportsPersistentIdentifier = supportsPersistentIdentifier;
            return this;
        }

        public ClientCapabilitiesDto.Builder deviceProfile(DeviceProfile deviceProfile) {
            this.instance.deviceProfile = deviceProfile;
            return this;
        }

        public ClientCapabilitiesDto.Builder appStoreUrl(String appStoreUrl) {
            this.instance.appStoreUrl = appStoreUrl;
            return this;
        }

        public ClientCapabilitiesDto.Builder iconUrl(String iconUrl) {
            this.instance.iconUrl = iconUrl;
            return this;
        }

        /**
         * returns a built ClientCapabilitiesDto instance.
         *
         * The builder is not reusable.
         */
        public ClientCapabilitiesDto build() {
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
    public static ClientCapabilitiesDto.Builder builder() {
        return new ClientCapabilitiesDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ClientCapabilitiesDto.Builder toBuilder() {
        return new ClientCapabilitiesDto.Builder().playableMediaTypes(getPlayableMediaTypes())
                .supportedCommands(getSupportedCommands()).supportsMediaControl(getSupportsMediaControl())
                .supportsPersistentIdentifier(getSupportsPersistentIdentifier()).deviceProfile(getDeviceProfile())
                .appStoreUrl(getAppStoreUrl()).iconUrl(getIconUrl());
    }
}
