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

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * MessageCommand
 */
@JsonPropertyOrder({ MessageCommand.JSON_PROPERTY_HEADER, MessageCommand.JSON_PROPERTY_TEXT,
        MessageCommand.JSON_PROPERTY_TIMEOUT_MS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MessageCommand {
    public static final String JSON_PROPERTY_HEADER = "Header";
    @org.eclipse.jdt.annotation.Nullable
    private String header;

    public static final String JSON_PROPERTY_TEXT = "Text";
    @org.eclipse.jdt.annotation.NonNull
    private String text;

    public static final String JSON_PROPERTY_TIMEOUT_MS = "TimeoutMs";
    @org.eclipse.jdt.annotation.Nullable
    private Long timeoutMs;

    public MessageCommand() {
    }

    public MessageCommand header(@org.eclipse.jdt.annotation.Nullable String header) {
        this.header = header;
        return this;
    }

    /**
     * Get header
     * 
     * @return header
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_HEADER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getHeader() {
        return header;
    }

    @JsonProperty(value = JSON_PROPERTY_HEADER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeader(@org.eclipse.jdt.annotation.Nullable String header) {
        this.header = header;
    }

    public MessageCommand text(@org.eclipse.jdt.annotation.NonNull String text) {
        this.text = text;
        return this;
    }

    /**
     * Get text
     * 
     * @return text
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TEXT, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getText() {
        return text;
    }

    @JsonProperty(value = JSON_PROPERTY_TEXT, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setText(@org.eclipse.jdt.annotation.NonNull String text) {
        this.text = text;
    }

    public MessageCommand timeoutMs(@org.eclipse.jdt.annotation.Nullable Long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    /**
     * Get timeoutMs
     * 
     * @return timeoutMs
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TIMEOUT_MS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getTimeoutMs() {
        return timeoutMs;
    }

    @JsonProperty(value = JSON_PROPERTY_TIMEOUT_MS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeoutMs(@org.eclipse.jdt.annotation.Nullable Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Return true if this MessageCommand object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MessageCommand messageCommand = (MessageCommand) o;
        return Objects.equals(this.header, messageCommand.header) && Objects.equals(this.text, messageCommand.text)
                && Objects.equals(this.timeoutMs, messageCommand.timeoutMs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, text, timeoutMs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MessageCommand {\n");
        sb.append("    header: ").append(toIndentedString(header)).append("\n");
        sb.append("    text: ").append(toIndentedString(text)).append("\n");
        sb.append("    timeoutMs: ").append(toIndentedString(timeoutMs)).append("\n");
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

        // add `Header` to the URL query string
        if (getHeader() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sHeader%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHeader()))));
        }

        // add `Text` to the URL query string
        if (getText() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sText%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getText()))));
        }

        // add `TimeoutMs` to the URL query string
        if (getTimeoutMs() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTimeoutMs%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTimeoutMs()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MessageCommand instance;

        public Builder() {
            this(new MessageCommand());
        }

        protected Builder(MessageCommand instance) {
            this.instance = instance;
        }

        public MessageCommand.Builder header(String header) {
            this.instance.header = header;
            return this;
        }

        public MessageCommand.Builder text(String text) {
            this.instance.text = text;
            return this;
        }

        public MessageCommand.Builder timeoutMs(Long timeoutMs) {
            this.instance.timeoutMs = timeoutMs;
            return this;
        }

        /**
         * returns a built MessageCommand instance.
         *
         * The builder is not reusable.
         */
        public MessageCommand build() {
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
    public static MessageCommand.Builder builder() {
        return new MessageCommand.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MessageCommand.Builder toBuilder() {
        return new MessageCommand.Builder().header(getHeader()).text(getText()).timeoutMs(getTimeoutMs());
    }
}
