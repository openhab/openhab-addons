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
 * Client capabilities dto.
 */
@JsonPropertyOrder({ ClientCapabilitiesDto.JSON_PROPERTY_PLAYABLE_MEDIA_TYPES,
        ClientCapabilitiesDto.JSON_PROPERTY_SUPPORTED_COMMANDS,
        ClientCapabilitiesDto.JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL,
        ClientCapabilitiesDto.JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER,
        ClientCapabilitiesDto.JSON_PROPERTY_DEVICE_PROFILE, ClientCapabilitiesDto.JSON_PROPERTY_APP_STORE_URL,
        ClientCapabilitiesDto.JSON_PROPERTY_ICON_URL })

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
    @JsonProperty(JSON_PROPERTY_PLAYABLE_MEDIA_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<MediaType> getPlayableMediaTypes() {
        return playableMediaTypes;
    }

    @JsonProperty(JSON_PROPERTY_PLAYABLE_MEDIA_TYPES)
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
    @JsonProperty(JSON_PROPERTY_SUPPORTED_COMMANDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<GeneralCommandType> getSupportedCommands() {
        return supportedCommands;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_COMMANDS)
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
    @JsonProperty(JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSupportsMediaControl() {
        return supportsMediaControl;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL)
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
    @JsonProperty(JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getSupportsPersistentIdentifier() {
        return supportsPersistentIdentifier;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER)
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
    @JsonProperty(JSON_PROPERTY_DEVICE_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    @JsonProperty(JSON_PROPERTY_DEVICE_PROFILE)
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
    @JsonProperty(JSON_PROPERTY_APP_STORE_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAppStoreUrl() {
        return appStoreUrl;
    }

    @JsonProperty(JSON_PROPERTY_APP_STORE_URL)
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
    @JsonProperty(JSON_PROPERTY_ICON_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIconUrl() {
        return iconUrl;
    }

    @JsonProperty(JSON_PROPERTY_ICON_URL)
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
}
