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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Plugin uninstalled message.
 */
@JsonPropertyOrder({ PluginUninstalledMessage.JSON_PROPERTY_DATA, PluginUninstalledMessage.JSON_PROPERTY_MESSAGE_ID,
        PluginUninstalledMessage.JSON_PROPERTY_MESSAGE_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PluginUninstalledMessage {
    public static final String JSON_PROPERTY_DATA = "Data";
    @org.eclipse.jdt.annotation.Nullable
    private PluginInfo data;

    public static final String JSON_PROPERTY_MESSAGE_ID = "MessageId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID messageId;

    public static final String JSON_PROPERTY_MESSAGE_TYPE = "MessageType";
    @org.eclipse.jdt.annotation.Nullable
    private SessionMessageType messageType = SessionMessageType.PACKAGE_UNINSTALLED;

    public PluginUninstalledMessage() {
    }

    @JsonCreator
    public PluginUninstalledMessage(@JsonProperty(JSON_PROPERTY_MESSAGE_TYPE) SessionMessageType messageType) {
        this();
        this.messageType = messageType;
    }

    public PluginUninstalledMessage data(@org.eclipse.jdt.annotation.Nullable PluginInfo data) {
        this.data = data;
        return this;
    }

    /**
     * This is a serializable stub class that is used by the api to provide information about installed plugins.
     * 
     * @return data
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PluginInfo getData() {
        return data;
    }

    @JsonProperty(value = JSON_PROPERTY_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setData(@org.eclipse.jdt.annotation.Nullable PluginInfo data) {
        this.data = data;
    }

    public PluginUninstalledMessage messageId(@org.eclipse.jdt.annotation.Nullable UUID messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Gets or sets the message id.
     * 
     * @return messageId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MESSAGE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getMessageId() {
        return messageId;
    }

    @JsonProperty(value = JSON_PROPERTY_MESSAGE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMessageId(@org.eclipse.jdt.annotation.Nullable UUID messageId) {
        this.messageId = messageId;
    }

    /**
     * The different kinds of messages that are used in the WebSocket api.
     * 
     * @return messageType
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MESSAGE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SessionMessageType getMessageType() {
        return messageType;
    }

    /**
     * Return true if this PluginUninstalledMessage object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PluginUninstalledMessage pluginUninstalledMessage = (PluginUninstalledMessage) o;
        return Objects.equals(this.data, pluginUninstalledMessage.data)
                && Objects.equals(this.messageId, pluginUninstalledMessage.messageId)
                && Objects.equals(this.messageType, pluginUninstalledMessage.messageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, messageId, messageType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PluginUninstalledMessage {\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    messageId: ").append(toIndentedString(messageId)).append("\n");
        sb.append("    messageType: ").append(toIndentedString(messageType)).append("\n");
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

        // add `Data` to the URL query string
        if (getData() != null) {
            joiner.add(getData().toUrlQueryString(prefix + "Data" + suffix));
        }

        // add `MessageId` to the URL query string
        if (getMessageId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMessageId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMessageId()))));
        }

        // add `MessageType` to the URL query string
        if (getMessageType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMessageType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMessageType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PluginUninstalledMessage instance;

        public Builder() {
            this(new PluginUninstalledMessage());
        }

        protected Builder(PluginUninstalledMessage instance) {
            this.instance = instance;
        }

        public PluginUninstalledMessage.Builder data(PluginInfo data) {
            this.instance.data = data;
            return this;
        }

        public PluginUninstalledMessage.Builder messageId(UUID messageId) {
            this.instance.messageId = messageId;
            return this;
        }

        public PluginUninstalledMessage.Builder messageType(SessionMessageType messageType) {
            this.instance.messageType = messageType;
            return this;
        }

        /**
         * returns a built PluginUninstalledMessage instance.
         *
         * The builder is not reusable.
         */
        public PluginUninstalledMessage build() {
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
    public static PluginUninstalledMessage.Builder builder() {
        return new PluginUninstalledMessage.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PluginUninstalledMessage.Builder toBuilder() {
        return new PluginUninstalledMessage.Builder().data(getData()).messageId(getMessageId())
                .messageType(getMessageType());
    }
}
