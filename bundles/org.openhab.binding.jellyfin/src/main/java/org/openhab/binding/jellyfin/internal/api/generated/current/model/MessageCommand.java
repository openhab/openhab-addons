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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * MessageCommand
 */
@JsonPropertyOrder({ MessageCommand.JSON_PROPERTY_HEADER, MessageCommand.JSON_PROPERTY_TEXT,
        MessageCommand.JSON_PROPERTY_TIMEOUT_MS })

public class MessageCommand {
    public static final String JSON_PROPERTY_HEADER = "Header";
    @org.eclipse.jdt.annotation.NonNull
    private String header;

    public static final String JSON_PROPERTY_TEXT = "Text";
    @org.eclipse.jdt.annotation.Nullable
    private String text;

    public static final String JSON_PROPERTY_TIMEOUT_MS = "TimeoutMs";
    @org.eclipse.jdt.annotation.NonNull
    private Long timeoutMs;

    public MessageCommand() {
    }

    public MessageCommand header(@org.eclipse.jdt.annotation.NonNull String header) {
        this.header = header;
        return this;
    }

    /**
     * Get header
     * 
     * @return header
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HEADER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getHeader() {
        return header;
    }

    @JsonProperty(JSON_PROPERTY_HEADER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeader(@org.eclipse.jdt.annotation.NonNull String header) {
        this.header = header;
    }

    public MessageCommand text(@org.eclipse.jdt.annotation.Nullable String text) {
        this.text = text;
        return this;
    }

    /**
     * Get text
     * 
     * @return text
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_TEXT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getText() {
        return text;
    }

    @JsonProperty(JSON_PROPERTY_TEXT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setText(@org.eclipse.jdt.annotation.Nullable String text) {
        this.text = text;
    }

    public MessageCommand timeoutMs(@org.eclipse.jdt.annotation.NonNull Long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    /**
     * Get timeoutMs
     * 
     * @return timeoutMs
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TIMEOUT_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    @JsonProperty(JSON_PROPERTY_TIMEOUT_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeoutMs(@org.eclipse.jdt.annotation.NonNull Long timeoutMs) {
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
}
