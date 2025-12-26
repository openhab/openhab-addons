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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Series timer created message.
 */
@JsonPropertyOrder({ SeriesTimerCreatedMessage.JSON_PROPERTY_DATA, SeriesTimerCreatedMessage.JSON_PROPERTY_MESSAGE_ID,
        SeriesTimerCreatedMessage.JSON_PROPERTY_MESSAGE_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SeriesTimerCreatedMessage {
    public static final String JSON_PROPERTY_DATA = "Data";
    @org.eclipse.jdt.annotation.NonNull
    private TimerEventInfo data;

    public static final String JSON_PROPERTY_MESSAGE_ID = "MessageId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID messageId;

    public static final String JSON_PROPERTY_MESSAGE_TYPE = "MessageType";
    @org.eclipse.jdt.annotation.NonNull
    private SessionMessageType messageType = SessionMessageType.SERIES_TIMER_CREATED;

    public SeriesTimerCreatedMessage() {
    }

    @JsonCreator
    public SeriesTimerCreatedMessage(@JsonProperty(JSON_PROPERTY_MESSAGE_TYPE) SessionMessageType messageType) {
        this();
        this.messageType = messageType;
    }

    public SeriesTimerCreatedMessage data(@org.eclipse.jdt.annotation.NonNull TimerEventInfo data) {
        this.data = data;
        return this;
    }

    /**
     * Gets or sets the data.
     * 
     * @return data
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TimerEventInfo getData() {
        return data;
    }

    @JsonProperty(value = JSON_PROPERTY_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setData(@org.eclipse.jdt.annotation.NonNull TimerEventInfo data) {
        this.data = data;
    }

    public SeriesTimerCreatedMessage messageId(@org.eclipse.jdt.annotation.NonNull UUID messageId) {
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
     * Return true if this SeriesTimerCreatedMessage object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeriesTimerCreatedMessage seriesTimerCreatedMessage = (SeriesTimerCreatedMessage) o;
        return Objects.equals(this.data, seriesTimerCreatedMessage.data)
                && Objects.equals(this.messageId, seriesTimerCreatedMessage.messageId)
                && Objects.equals(this.messageType, seriesTimerCreatedMessage.messageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, messageId, messageType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SeriesTimerCreatedMessage {\n");
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

        private SeriesTimerCreatedMessage instance;

        public Builder() {
            this(new SeriesTimerCreatedMessage());
        }

        protected Builder(SeriesTimerCreatedMessage instance) {
            this.instance = instance;
        }

        public SeriesTimerCreatedMessage.Builder data(TimerEventInfo data) {
            this.instance.data = data;
            return this;
        }

        public SeriesTimerCreatedMessage.Builder messageId(UUID messageId) {
            this.instance.messageId = messageId;
            return this;
        }

        public SeriesTimerCreatedMessage.Builder messageType(SessionMessageType messageType) {
            this.instance.messageType = messageType;
            return this;
        }

        /**
         * returns a built SeriesTimerCreatedMessage instance.
         *
         * The builder is not reusable.
         */
        public SeriesTimerCreatedMessage build() {
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
    public static SeriesTimerCreatedMessage.Builder builder() {
        return new SeriesTimerCreatedMessage.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SeriesTimerCreatedMessage.Builder toBuilder() {
        return new SeriesTimerCreatedMessage.Builder().data(getData()).messageId(getMessageId())
                .messageType(getMessageType());
    }
}
