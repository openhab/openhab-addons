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
 * NotificationOption
 */
@JsonPropertyOrder({ NotificationOption.JSON_PROPERTY_TYPE, NotificationOption.JSON_PROPERTY_DISABLED_MONITOR_USERS,
        NotificationOption.JSON_PROPERTY_SEND_TO_USERS, NotificationOption.JSON_PROPERTY_ENABLED,
        NotificationOption.JSON_PROPERTY_DISABLED_SERVICES, NotificationOption.JSON_PROPERTY_SEND_TO_USER_MODE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class NotificationOption {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private String type;

    public static final String JSON_PROPERTY_DISABLED_MONITOR_USERS = "DisabledMonitorUsers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> disabledMonitorUsers = new ArrayList<>();

    public static final String JSON_PROPERTY_SEND_TO_USERS = "SendToUsers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> sendToUsers = new ArrayList<>();

    public static final String JSON_PROPERTY_ENABLED = "Enabled";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enabled;

    public static final String JSON_PROPERTY_DISABLED_SERVICES = "DisabledServices";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> disabledServices = new ArrayList<>();

    public static final String JSON_PROPERTY_SEND_TO_USER_MODE = "SendToUserMode";
    @org.eclipse.jdt.annotation.NonNull
    private SendToUserType sendToUserMode;

    public NotificationOption() {
    }

    public NotificationOption type(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
    }

    public NotificationOption disabledMonitorUsers(
            @org.eclipse.jdt.annotation.NonNull List<String> disabledMonitorUsers) {
        this.disabledMonitorUsers = disabledMonitorUsers;
        return this;
    }

    public NotificationOption addDisabledMonitorUsersItem(String disabledMonitorUsersItem) {
        if (this.disabledMonitorUsers == null) {
            this.disabledMonitorUsers = new ArrayList<>();
        }
        this.disabledMonitorUsers.add(disabledMonitorUsersItem);
        return this;
    }

    /**
     * Gets or sets user Ids to not monitor (it&#39;s opt out).
     * 
     * @return disabledMonitorUsers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISABLED_MONITOR_USERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getDisabledMonitorUsers() {
        return disabledMonitorUsers;
    }

    @JsonProperty(JSON_PROPERTY_DISABLED_MONITOR_USERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledMonitorUsers(@org.eclipse.jdt.annotation.NonNull List<String> disabledMonitorUsers) {
        this.disabledMonitorUsers = disabledMonitorUsers;
    }

    public NotificationOption sendToUsers(@org.eclipse.jdt.annotation.NonNull List<String> sendToUsers) {
        this.sendToUsers = sendToUsers;
        return this;
    }

    public NotificationOption addSendToUsersItem(String sendToUsersItem) {
        if (this.sendToUsers == null) {
            this.sendToUsers = new ArrayList<>();
        }
        this.sendToUsers.add(sendToUsersItem);
        return this;
    }

    /**
     * Gets or sets user Ids to send to (if SendToUserMode &#x3D;&#x3D; Custom).
     * 
     * @return sendToUsers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SEND_TO_USERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getSendToUsers() {
        return sendToUsers;
    }

    @JsonProperty(JSON_PROPERTY_SEND_TO_USERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSendToUsers(@org.eclipse.jdt.annotation.NonNull List<String> sendToUsers) {
        this.sendToUsers = sendToUsers;
    }

    public NotificationOption enabled(@org.eclipse.jdt.annotation.NonNull Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this MediaBrowser.Model.Notifications.NotificationOption is enabled.
     * 
     * @return enabled
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty(JSON_PROPERTY_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnabled(@org.eclipse.jdt.annotation.NonNull Boolean enabled) {
        this.enabled = enabled;
    }

    public NotificationOption disabledServices(@org.eclipse.jdt.annotation.NonNull List<String> disabledServices) {
        this.disabledServices = disabledServices;
        return this;
    }

    public NotificationOption addDisabledServicesItem(String disabledServicesItem) {
        if (this.disabledServices == null) {
            this.disabledServices = new ArrayList<>();
        }
        this.disabledServices.add(disabledServicesItem);
        return this;
    }

    /**
     * Gets or sets the disabled services.
     * 
     * @return disabledServices
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISABLED_SERVICES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getDisabledServices() {
        return disabledServices;
    }

    @JsonProperty(JSON_PROPERTY_DISABLED_SERVICES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledServices(@org.eclipse.jdt.annotation.NonNull List<String> disabledServices) {
        this.disabledServices = disabledServices;
    }

    public NotificationOption sendToUserMode(@org.eclipse.jdt.annotation.NonNull SendToUserType sendToUserMode) {
        this.sendToUserMode = sendToUserMode;
        return this;
    }

    /**
     * Gets or sets the send to user mode.
     * 
     * @return sendToUserMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SEND_TO_USER_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SendToUserType getSendToUserMode() {
        return sendToUserMode;
    }

    @JsonProperty(JSON_PROPERTY_SEND_TO_USER_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSendToUserMode(@org.eclipse.jdt.annotation.NonNull SendToUserType sendToUserMode) {
        this.sendToUserMode = sendToUserMode;
    }

    /**
     * Return true if this NotificationOption object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationOption notificationOption = (NotificationOption) o;
        return Objects.equals(this.type, notificationOption.type)
                && Objects.equals(this.disabledMonitorUsers, notificationOption.disabledMonitorUsers)
                && Objects.equals(this.sendToUsers, notificationOption.sendToUsers)
                && Objects.equals(this.enabled, notificationOption.enabled)
                && Objects.equals(this.disabledServices, notificationOption.disabledServices)
                && Objects.equals(this.sendToUserMode, notificationOption.sendToUserMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, disabledMonitorUsers, sendToUsers, enabled, disabledServices, sendToUserMode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NotificationOption {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    disabledMonitorUsers: ").append(toIndentedString(disabledMonitorUsers)).append("\n");
        sb.append("    sendToUsers: ").append(toIndentedString(sendToUsers)).append("\n");
        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
        sb.append("    disabledServices: ").append(toIndentedString(disabledServices)).append("\n");
        sb.append("    sendToUserMode: ").append(toIndentedString(sendToUserMode)).append("\n");
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

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format("%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `DisabledMonitorUsers` to the URL query string
        if (getDisabledMonitorUsers() != null) {
            for (int i = 0; i < getDisabledMonitorUsers().size(); i++) {
                joiner.add(String.format("%sDisabledMonitorUsers%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getDisabledMonitorUsers().get(i)))));
            }
        }

        // add `SendToUsers` to the URL query string
        if (getSendToUsers() != null) {
            for (int i = 0; i < getSendToUsers().size(); i++) {
                joiner.add(String.format("%sSendToUsers%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getSendToUsers().get(i)))));
            }
        }

        // add `Enabled` to the URL query string
        if (getEnabled() != null) {
            joiner.add(String.format("%sEnabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnabled()))));
        }

        // add `DisabledServices` to the URL query string
        if (getDisabledServices() != null) {
            for (int i = 0; i < getDisabledServices().size(); i++) {
                joiner.add(String.format("%sDisabledServices%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getDisabledServices().get(i)))));
            }
        }

        // add `SendToUserMode` to the URL query string
        if (getSendToUserMode() != null) {
            joiner.add(String.format("%sSendToUserMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSendToUserMode()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private NotificationOption instance;

        public Builder() {
            this(new NotificationOption());
        }

        protected Builder(NotificationOption instance) {
            this.instance = instance;
        }

        public NotificationOption.Builder type(String type) {
            this.instance.type = type;
            return this;
        }

        public NotificationOption.Builder disabledMonitorUsers(List<String> disabledMonitorUsers) {
            this.instance.disabledMonitorUsers = disabledMonitorUsers;
            return this;
        }

        public NotificationOption.Builder sendToUsers(List<String> sendToUsers) {
            this.instance.sendToUsers = sendToUsers;
            return this;
        }

        public NotificationOption.Builder enabled(Boolean enabled) {
            this.instance.enabled = enabled;
            return this;
        }

        public NotificationOption.Builder disabledServices(List<String> disabledServices) {
            this.instance.disabledServices = disabledServices;
            return this;
        }

        public NotificationOption.Builder sendToUserMode(SendToUserType sendToUserMode) {
            this.instance.sendToUserMode = sendToUserMode;
            return this;
        }

        /**
         * returns a built NotificationOption instance.
         *
         * The builder is not reusable.
         */
        public NotificationOption build() {
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
    public static NotificationOption.Builder builder() {
        return new NotificationOption.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public NotificationOption.Builder toBuilder() {
        return new NotificationOption.Builder().type(getType()).disabledMonitorUsers(getDisabledMonitorUsers())
                .sendToUsers(getSendToUsers()).enabled(getEnabled()).disabledServices(getDisabledServices())
                .sendToUserMode(getSendToUserMode());
    }
}
