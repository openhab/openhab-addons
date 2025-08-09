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
 * Lyric model.
 */
@JsonPropertyOrder({ LyricLine.JSON_PROPERTY_TEXT, LyricLine.JSON_PROPERTY_START })

public class LyricLine {
    public static final String JSON_PROPERTY_TEXT = "Text";
    @org.eclipse.jdt.annotation.NonNull
    private String text;

    public static final String JSON_PROPERTY_START = "Start";
    @org.eclipse.jdt.annotation.NonNull
    private Long start;

    public LyricLine() {
    }

    public LyricLine text(@org.eclipse.jdt.annotation.NonNull String text) {
        this.text = text;
        return this;
    }

    /**
     * Gets the text of this lyric line.
     * 
     * @return text
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getText() {
        return text;
    }

    @JsonProperty(JSON_PROPERTY_TEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setText(@org.eclipse.jdt.annotation.NonNull String text) {
        this.text = text;
    }

    public LyricLine start(@org.eclipse.jdt.annotation.NonNull Long start) {
        this.start = start;
        return this;
    }

    /**
     * Gets the start time in ticks.
     * 
     * @return start
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getStart() {
        return start;
    }

    @JsonProperty(JSON_PROPERTY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStart(@org.eclipse.jdt.annotation.NonNull Long start) {
        this.start = start;
    }

    /**
     * Return true if this LyricLine object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LyricLine lyricLine = (LyricLine) o;
        return Objects.equals(this.text, lyricLine.text) && Objects.equals(this.start, lyricLine.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, start);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LyricLine {\n");
        sb.append("    text: ").append(toIndentedString(text)).append("\n");
        sb.append("    start: ").append(toIndentedString(start)).append("\n");
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
