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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ClientCapabilities
 */
@JsonPropertyOrder({ ClientCapabilities.JSON_PROPERTY_PLAYABLE_MEDIA_TYPES,
        ClientCapabilities.JSON_PROPERTY_SUPPORTED_COMMANDS, ClientCapabilities.JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL,
        ClientCapabilities.JSON_PROPERTY_SUPPORTS_CONTENT_UPLOADING,
        ClientCapabilities.JSON_PROPERTY_MESSAGE_CALLBACK_URL,
        ClientCapabilities.JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER, ClientCapabilities.JSON_PROPERTY_SUPPORTS_SYNC,
        ClientCapabilities.JSON_PROPERTY_DEVICE_PROFILE, ClientCapabilities.JSON_PROPERTY_APP_STORE_URL,
        ClientCapabilities.JSON_PROPERTY_ICON_URL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ClientCapabilities {
    public static final String JSON_PROPERTY_PLAYABLE_MEDIA_TYPES = "PlayableMediaTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> playableMediaTypes;

    public static final String JSON_PROPERTY_SUPPORTED_COMMANDS = "SupportedCommands";
    @org.eclipse.jdt.annotation.NonNull
    private List<GeneralCommandType> supportedCommands;

    public static final String JSON_PROPERTY_SUPPORTS_MEDIA_CONTROL = "SupportsMediaControl";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsMediaControl;

    public static final String JSON_PROPERTY_SUPPORTS_CONTENT_UPLOADING = "SupportsContentUploading";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsContentUploading;

    public static final String JSON_PROPERTY_MESSAGE_CALLBACK_URL = "MessageCallbackUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String messageCallbackUrl;

    public static final String JSON_PROPERTY_SUPPORTS_PERSISTENT_IDENTIFIER = "SupportsPersistentIdentifier";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsPersistentIdentifier;

    public static final String JSON_PROPERTY_SUPPORTS_SYNC = "SupportsSync";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsSync;

    public static final String JSON_PROPERTY_DEVICE_PROFILE = "DeviceProfile";
    @org.eclipse.jdt.annotation.NonNull
    private DeviceProfile deviceProfile;

    public static final String JSON_PROPERTY_APP_STORE_URL = "AppStoreUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String appStoreUrl;

    public static final String JSON_PROPERTY_ICON_URL = "IconUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String iconUrl;

    public ClientCapabilities() {
    }

    public ClientCapabilities playableMediaTypes(@org.eclipse.jdt.annotation.NonNull List<String> playableMediaTypes) {
        this.playableMediaTypes = playableMediaTypes;
        return this;
    }

    public ClientCapabilities addPlayableMediaTypesItem(String playableMediaTypesItem) {
        if (this.playableMediaTypes == null) {
            this.playableMediaTypes = new ArrayList<>();
        }
        this.playableMediaTypes.add(playableMediaTypesItem);
        return this;
    }

    /**
     * Get playableMediaTypes
     * 
     * @return playableMediaTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYABLE_MEDIA_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getPlayableMediaTypes() {
        return playableMediaTypes;
    }

    @JsonProperty(JSON_PROPERTY_PLAYABLE_MEDIA_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayableMediaTypes(@org.eclipse.jdt.annotation.NonNull List<String> playableMediaTypes) {
        this.playableMediaTypes = playableMediaTypes;
    }

    public ClientCapabilities supportedCommands(
            @org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands) {
        this.supportedCommands = supportedCommands;
        return this;
    }

    public ClientCapabilities addSupportedCommandsItem(GeneralCommandType supportedCommandsItem) {
        if (this.supportedCommands == null) {
            this.supportedCommands = new ArrayList<>();
        }
        this.supportedCommands.add(supportedCommandsItem);
        return this;
    }

    /**
     * Get supportedCommands
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

    public ClientCapabilities supportsMediaControl(@org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl) {
        this.supportsMediaControl = supportsMediaControl;
        return this;
    }

    /**
     * Get supportsMediaControl
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

    public ClientCapabilities supportsContentUploading(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsContentUploading) {
        this.supportsContentUploading = supportsContentUploading;
        return this;
    }

    /**
     * Get supportsContentUploading
     * 
     * @return supportsContentUploading
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_CONTENT_UPLOADING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsContentUploading() {
        return supportsContentUploading;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_CONTENT_UPLOADING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsContentUploading(@org.eclipse.jdt.annotation.NonNull Boolean supportsContentUploading) {
        this.supportsContentUploading = supportsContentUploading;
    }

    public ClientCapabilities messageCallbackUrl(@org.eclipse.jdt.annotation.NonNull String messageCallbackUrl) {
        this.messageCallbackUrl = messageCallbackUrl;
        return this;
    }

    /**
     * Get messageCallbackUrl
     * 
     * @return messageCallbackUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MESSAGE_CALLBACK_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMessageCallbackUrl() {
        return messageCallbackUrl;
    }

    @JsonProperty(JSON_PROPERTY_MESSAGE_CALLBACK_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMessageCallbackUrl(@org.eclipse.jdt.annotation.NonNull String messageCallbackUrl) {
        this.messageCallbackUrl = messageCallbackUrl;
    }

    public ClientCapabilities supportsPersistentIdentifier(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsPersistentIdentifier) {
        this.supportsPersistentIdentifier = supportsPersistentIdentifier;
        return this;
    }

    /**
     * Get supportsPersistentIdentifier
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

    public ClientCapabilities supportsSync(@org.eclipse.jdt.annotation.NonNull Boolean supportsSync) {
        this.supportsSync = supportsSync;
        return this;
    }

    /**
     * Get supportsSync
     * 
     * @return supportsSync
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_SYNC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsSync() {
        return supportsSync;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_SYNC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsSync(@org.eclipse.jdt.annotation.NonNull Boolean supportsSync) {
        this.supportsSync = supportsSync;
    }

    public ClientCapabilities deviceProfile(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
        return this;
    }

    /**
     * A MediaBrowser.Model.Dlna.DeviceProfile represents a set of metadata which determines which content a certain
     * device is able to play. &lt;br /&gt; Specifically, it defines the supported &lt;see
     * cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.ContainerProfiles\&quot;&gt;containers&lt;/see&gt; and
     * &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.CodecProfiles\&quot;&gt;codecs&lt;/see&gt;
     * (video and/or audio, including codec profiles and levels) the device is able to direct play (without transcoding
     * or remuxing), as well as which &lt;see
     * cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.TranscodingProfiles\&quot;&gt;containers/codecs to
     * transcode to&lt;/see&gt; in case it isn&#39;t.
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

    public ClientCapabilities appStoreUrl(@org.eclipse.jdt.annotation.NonNull String appStoreUrl) {
        this.appStoreUrl = appStoreUrl;
        return this;
    }

    /**
     * Get appStoreUrl
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

    public ClientCapabilities iconUrl(@org.eclipse.jdt.annotation.NonNull String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    /**
     * Get iconUrl
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
     * Return true if this ClientCapabilities object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientCapabilities clientCapabilities = (ClientCapabilities) o;
        return Objects.equals(this.playableMediaTypes, clientCapabilities.playableMediaTypes)
                && Objects.equals(this.supportedCommands, clientCapabilities.supportedCommands)
                && Objects.equals(this.supportsMediaControl, clientCapabilities.supportsMediaControl)
                && Objects.equals(this.supportsContentUploading, clientCapabilities.supportsContentUploading)
                && Objects.equals(this.messageCallbackUrl, clientCapabilities.messageCallbackUrl)
                && Objects.equals(this.supportsPersistentIdentifier, clientCapabilities.supportsPersistentIdentifier)
                && Objects.equals(this.supportsSync, clientCapabilities.supportsSync)
                && Objects.equals(this.deviceProfile, clientCapabilities.deviceProfile)
                && Objects.equals(this.appStoreUrl, clientCapabilities.appStoreUrl)
                && Objects.equals(this.iconUrl, clientCapabilities.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playableMediaTypes, supportedCommands, supportsMediaControl, supportsContentUploading,
                messageCallbackUrl, supportsPersistentIdentifier, supportsSync, deviceProfile, appStoreUrl, iconUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClientCapabilities {\n");
        sb.append("    playableMediaTypes: ").append(toIndentedString(playableMediaTypes)).append("\n");
        sb.append("    supportedCommands: ").append(toIndentedString(supportedCommands)).append("\n");
        sb.append("    supportsMediaControl: ").append(toIndentedString(supportsMediaControl)).append("\n");
        sb.append("    supportsContentUploading: ").append(toIndentedString(supportsContentUploading)).append("\n");
        sb.append("    messageCallbackUrl: ").append(toIndentedString(messageCallbackUrl)).append("\n");
        sb.append("    supportsPersistentIdentifier: ").append(toIndentedString(supportsPersistentIdentifier))
                .append("\n");
        sb.append("    supportsSync: ").append(toIndentedString(supportsSync)).append("\n");
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
                joiner.add(String.format("%sPlayableMediaTypes%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getPlayableMediaTypes().get(i)))));
            }
        }

        // add `SupportedCommands` to the URL query string
        if (getSupportedCommands() != null) {
            for (int i = 0; i < getSupportedCommands().size(); i++) {
                if (getSupportedCommands().get(i) != null) {
                    joiner.add(String.format("%sSupportedCommands%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getSupportedCommands().get(i)))));
                }
            }
        }

        // add `SupportsMediaControl` to the URL query string
        if (getSupportsMediaControl() != null) {
            joiner.add(String.format("%sSupportsMediaControl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsMediaControl()))));
        }

        // add `SupportsContentUploading` to the URL query string
        if (getSupportsContentUploading() != null) {
            joiner.add(String.format("%sSupportsContentUploading%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsContentUploading()))));
        }

        // add `MessageCallbackUrl` to the URL query string
        if (getMessageCallbackUrl() != null) {
            joiner.add(String.format("%sMessageCallbackUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMessageCallbackUrl()))));
        }

        // add `SupportsPersistentIdentifier` to the URL query string
        if (getSupportsPersistentIdentifier() != null) {
            joiner.add(String.format("%sSupportsPersistentIdentifier%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsPersistentIdentifier()))));
        }

        // add `SupportsSync` to the URL query string
        if (getSupportsSync() != null) {
            joiner.add(String.format("%sSupportsSync%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsSync()))));
        }

        // add `DeviceProfile` to the URL query string
        if (getDeviceProfile() != null) {
            joiner.add(getDeviceProfile().toUrlQueryString(prefix + "DeviceProfile" + suffix));
        }

        // add `AppStoreUrl` to the URL query string
        if (getAppStoreUrl() != null) {
            joiner.add(String.format("%sAppStoreUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAppStoreUrl()))));
        }

        // add `IconUrl` to the URL query string
        if (getIconUrl() != null) {
            joiner.add(String.format("%sIconUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIconUrl()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ClientCapabilities instance;

        public Builder() {
            this(new ClientCapabilities());
        }

        protected Builder(ClientCapabilities instance) {
            this.instance = instance;
        }

        public ClientCapabilities.Builder playableMediaTypes(List<String> playableMediaTypes) {
            this.instance.playableMediaTypes = playableMediaTypes;
            return this;
        }

        public ClientCapabilities.Builder supportedCommands(List<GeneralCommandType> supportedCommands) {
            this.instance.supportedCommands = supportedCommands;
            return this;
        }

        public ClientCapabilities.Builder supportsMediaControl(Boolean supportsMediaControl) {
            this.instance.supportsMediaControl = supportsMediaControl;
            return this;
        }

        public ClientCapabilities.Builder supportsContentUploading(Boolean supportsContentUploading) {
            this.instance.supportsContentUploading = supportsContentUploading;
            return this;
        }

        public ClientCapabilities.Builder messageCallbackUrl(String messageCallbackUrl) {
            this.instance.messageCallbackUrl = messageCallbackUrl;
            return this;
        }

        public ClientCapabilities.Builder supportsPersistentIdentifier(Boolean supportsPersistentIdentifier) {
            this.instance.supportsPersistentIdentifier = supportsPersistentIdentifier;
            return this;
        }

        public ClientCapabilities.Builder supportsSync(Boolean supportsSync) {
            this.instance.supportsSync = supportsSync;
            return this;
        }

        public ClientCapabilities.Builder deviceProfile(DeviceProfile deviceProfile) {
            this.instance.deviceProfile = deviceProfile;
            return this;
        }

        public ClientCapabilities.Builder appStoreUrl(String appStoreUrl) {
            this.instance.appStoreUrl = appStoreUrl;
            return this;
        }

        public ClientCapabilities.Builder iconUrl(String iconUrl) {
            this.instance.iconUrl = iconUrl;
            return this;
        }

        /**
         * returns a built ClientCapabilities instance.
         *
         * The builder is not reusable.
         */
        public ClientCapabilities build() {
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
    public static ClientCapabilities.Builder builder() {
        return new ClientCapabilities.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ClientCapabilities.Builder toBuilder() {
        return new ClientCapabilities.Builder().playableMediaTypes(getPlayableMediaTypes())
                .supportedCommands(getSupportedCommands()).supportsMediaControl(getSupportsMediaControl())
                .supportsContentUploading(getSupportsContentUploading()).messageCallbackUrl(getMessageCallbackUrl())
                .supportsPersistentIdentifier(getSupportsPersistentIdentifier()).supportsSync(getSupportsSync())
                .deviceProfile(getDeviceProfile()).appStoreUrl(getAppStoreUrl()).iconUrl(getIconUrl());
    }
}
