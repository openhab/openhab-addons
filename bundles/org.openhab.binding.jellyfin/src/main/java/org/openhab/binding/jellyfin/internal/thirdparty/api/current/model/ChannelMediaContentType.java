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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets ChannelMediaContentType
 */
public enum ChannelMediaContentType {

    CLIP("Clip"),

    PODCAST("Podcast"),

    TRAILER("Trailer"),

    MOVIE("Movie"),

    EPISODE("Episode"),

    SONG("Song"),

    MOVIE_EXTRA("MovieExtra"),

    TV_EXTRA("TvExtra");

    private String value;

    ChannelMediaContentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ChannelMediaContentType fromValue(String value) {
        for (ChannelMediaContentType b : ChannelMediaContentType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        return String.format(java.util.Locale.ROOT, "%s=%s", prefix, this.toString());
    }
}
