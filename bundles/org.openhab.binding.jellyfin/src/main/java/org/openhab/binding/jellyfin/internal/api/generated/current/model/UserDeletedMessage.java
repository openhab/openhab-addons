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

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User deleted message.
 */
@JsonPropertyOrder({ UserDeletedMessage.JSON_PROPERTY_DATA, UserDeletedMessage.JSON_PROPERTY_MESSAGE_ID,
        UserDeletedMessage.JSON_PROPERTY_MESSAGE_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UserDeletedMessage {
    public static final String JSON_PROPERTY_DATA = "Data";
    @org.eclipse.jdt.annotation.NonNull
    private UUID data;

    public static final String JSON_PROPERTY_MESSAGE_ID = "MessageId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID messageId;

    public static final String JSON_PROPERTY_MESSAGE_TYPE = "MessageType";
    @org.eclipse.jdt.annotation.NonNull
    private SessionMessageType messageType = SessionMessageType.USER_DELETED;

    public UserDeletedMessage() {
    }

    @JsonCreator
    public UserDeletedMessage(@JsonProperty(JSON_PROPERTY_MESSAGE_TYPE) SessionMessageType messageType) {
        this();
        this.messageType = messageType;
    }

    public UserDeletedMessage data(@org.eclipse.jdt.annotation.NonNull UUID data) {
        this.data = data;
        return this;
    }

    /**
     * Gets or sets the data.
     * 
     * @return data
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getData() {
        return data;
    }

    @JsonProperty(JSON_PROPERTY_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setData(@org.eclipse.jdt.annotation.NonNull UUID data) {
        this.data = data;
    }

    public UserDeletedMessage messageId(@org.eclipse.jdt.annotation.NonNull UUID messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Gets or sets the message id.
     * 
     * @return messageId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MESSAGE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getMessageId() {
        return messageId;
    }

    @JsonProperty(JSON_PROPERTY_MESSAGE_ID)
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
    @JsonProperty(JSON_PROPERTY_MESSAGE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SessionMessageType getMessageType() {
        return messageType;
    }

    /**
     * Return true if this UserDeletedMessage object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDeletedMessage userDeletedMessage = (UserDeletedMessage) o;
        return Objects.equals(this.data, userDeletedMessage.data)
                && Objects.equals(this.messageId, userDeletedMessage.messageId)
                && Objects.equals(this.messageType, userDeletedMessage.messageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, messageId, messageType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserDeletedMessage {\n");
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
            joiner.add(String.format("%sData%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getData()))));
        }

        // add `MessageId` to the URL query string
        if (getMessageId() != null) {
            joiner.add(String.format("%sMessageId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMessageId()))));
        }

        // add `MessageType` to the URL query string
        if (getMessageType() != null) {
            joiner.add(String.format("%sMessageType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMessageType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UserDeletedMessage instance;

        public Builder() {
            this(new UserDeletedMessage());
        }

        protected Builder(UserDeletedMessage instance) {
            this.instance = instance;
        }

        public UserDeletedMessage.Builder data(UUID data) {
            this.instance.data = data;
            return this;
        }

        public UserDeletedMessage.Builder messageId(UUID messageId) {
            this.instance.messageId = messageId;
            return this;
        }

        public UserDeletedMessage.Builder messageType(SessionMessageType messageType) {
            this.instance.messageType = messageType;
            return this;
        }

        /**
         * returns a built UserDeletedMessage instance.
         *
         * The builder is not reusable.
         */
        public UserDeletedMessage build() {
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
    public static UserDeletedMessage.Builder builder() {
        return new UserDeletedMessage.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UserDeletedMessage.Builder toBuilder() {
        return new UserDeletedMessage.Builder().data(getData()).messageId(getMessageId()).messageType(getMessageType());
    }
}
