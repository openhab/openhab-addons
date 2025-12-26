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

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Scheduled tasks info stop message.
 */
@JsonPropertyOrder({ ScheduledTasksInfoStopMessage.JSON_PROPERTY_MESSAGE_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ScheduledTasksInfoStopMessage {
    public static final String JSON_PROPERTY_MESSAGE_TYPE = "MessageType";
    @org.eclipse.jdt.annotation.NonNull
    private SessionMessageType messageType = SessionMessageType.SCHEDULED_TASKS_INFO_STOP;

    public ScheduledTasksInfoStopMessage() {
    }

    @JsonCreator
    public ScheduledTasksInfoStopMessage(@JsonProperty(JSON_PROPERTY_MESSAGE_TYPE) SessionMessageType messageType) {
        this();
        this.messageType = messageType;
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
     * Return true if this ScheduledTasksInfoStopMessage object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScheduledTasksInfoStopMessage scheduledTasksInfoStopMessage = (ScheduledTasksInfoStopMessage) o;
        return Objects.equals(this.messageType, scheduledTasksInfoStopMessage.messageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ScheduledTasksInfoStopMessage {\n");
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

        // add `MessageType` to the URL query string
        if (getMessageType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMessageType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMessageType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ScheduledTasksInfoStopMessage instance;

        public Builder() {
            this(new ScheduledTasksInfoStopMessage());
        }

        protected Builder(ScheduledTasksInfoStopMessage instance) {
            this.instance = instance;
        }

        public ScheduledTasksInfoStopMessage.Builder messageType(SessionMessageType messageType) {
            this.instance.messageType = messageType;
            return this;
        }

        /**
         * returns a built ScheduledTasksInfoStopMessage instance.
         *
         * The builder is not reusable.
         */
        public ScheduledTasksInfoStopMessage build() {
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
    public static ScheduledTasksInfoStopMessage.Builder builder() {
        return new ScheduledTasksInfoStopMessage.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ScheduledTasksInfoStopMessage.Builder toBuilder() {
        return new ScheduledTasksInfoStopMessage.Builder().messageType(getMessageType());
    }
}
