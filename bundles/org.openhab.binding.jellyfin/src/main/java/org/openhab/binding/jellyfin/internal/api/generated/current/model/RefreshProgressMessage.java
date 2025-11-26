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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Refresh progress message.
 */
@JsonPropertyOrder({ RefreshProgressMessage.JSON_PROPERTY_DATA, RefreshProgressMessage.JSON_PROPERTY_MESSAGE_ID,
        RefreshProgressMessage.JSON_PROPERTY_MESSAGE_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RefreshProgressMessage {
    public static final String JSON_PROPERTY_DATA = "Data";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> data;

    public static final String JSON_PROPERTY_MESSAGE_ID = "MessageId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID messageId;

    public static final String JSON_PROPERTY_MESSAGE_TYPE = "MessageType";
    @org.eclipse.jdt.annotation.NonNull
    private SessionMessageType messageType = SessionMessageType.REFRESH_PROGRESS;

    public RefreshProgressMessage() {
    }

    @JsonCreator
    public RefreshProgressMessage(@JsonProperty(JSON_PROPERTY_MESSAGE_TYPE) SessionMessageType messageType) {
        this();
        this.messageType = messageType;
    }

    public RefreshProgressMessage data(@org.eclipse.jdt.annotation.NonNull Map<String, String> data) {
        this.data = data;
        return this;
    }

    public RefreshProgressMessage putDataItem(String key, String dataItem) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, dataItem);
        return this;
    }

    /**
     * Gets or sets the data.
     * 
     * @return data
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATA, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getData() {
        return data;
    }

    @JsonProperty(value = JSON_PROPERTY_DATA, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setData(@org.eclipse.jdt.annotation.NonNull Map<String, String> data) {
        this.data = data;
    }

    public RefreshProgressMessage messageId(@org.eclipse.jdt.annotation.NonNull UUID messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Gets or sets the message id.
     * 
     * @return messageId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MESSAGE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getMessageId() {
        return messageId;
    }

    @JsonProperty(value = JSON_PROPERTY_MESSAGE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMessageId(@org.eclipse.jdt.annotation.NonNull UUID messageId) {
        this.messageId = messageId;
    }

    /**
     * The different kinds of messages that are used in the WebSocket api.
     * 
     * @return messageType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MESSAGE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SessionMessageType getMessageType() {
        return messageType;
    }

    /**
     * Return true if this RefreshProgressMessage object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefreshProgressMessage refreshProgressMessage = (RefreshProgressMessage) o;
        return Objects.equals(this.data, refreshProgressMessage.data)
                && Objects.equals(this.messageId, refreshProgressMessage.messageId)
                && Objects.equals(this.messageType, refreshProgressMessage.messageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, messageId, messageType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RefreshProgressMessage {\n");
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
            for (String _key : getData().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sData%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getData().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getData().get(_key)))));
            }
        }

        // add `MessageId` to the URL query string
        if (getMessageId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMessageId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMessageId()))));
        }

        // add `MessageType` to the URL query string
        if (getMessageType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMessageType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMessageType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private RefreshProgressMessage instance;

        public Builder() {
            this(new RefreshProgressMessage());
        }

        protected Builder(RefreshProgressMessage instance) {
            this.instance = instance;
        }

        public RefreshProgressMessage.Builder data(Map<String, String> data) {
            this.instance.data = data;
            return this;
        }

        public RefreshProgressMessage.Builder messageId(UUID messageId) {
            this.instance.messageId = messageId;
            return this;
        }

        public RefreshProgressMessage.Builder messageType(SessionMessageType messageType) {
            this.instance.messageType = messageType;
            return this;
        }

        /**
         * returns a built RefreshProgressMessage instance.
         *
         * The builder is not reusable.
         */
        public RefreshProgressMessage build() {
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
    public static RefreshProgressMessage.Builder builder() {
        return new RefreshProgressMessage.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public RefreshProgressMessage.Builder toBuilder() {
        return new RefreshProgressMessage.Builder().data(getData()).messageId(getMessageId())
                .messageType(getMessageType());
    }
}
