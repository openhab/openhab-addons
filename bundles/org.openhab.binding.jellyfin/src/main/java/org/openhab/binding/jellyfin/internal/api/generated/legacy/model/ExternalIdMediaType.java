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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The specific media type of an MediaBrowser.Model.Providers.ExternalIdInfo.
 */
public enum ExternalIdMediaType {

    ALBUM("Album"),

    ALBUM_ARTIST("AlbumArtist"),

    ARTIST("Artist"),

    BOX_SET("BoxSet"),

    EPISODE("Episode"),

    MOVIE("Movie"),

    OTHER_ARTIST("OtherArtist"),

    PERSON("Person"),

    RELEASE_GROUP("ReleaseGroup"),

    SEASON("Season"),

    SERIES("Series"),

    TRACK("Track");

    private String value;

    ExternalIdMediaType(String value) {
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
    public static ExternalIdMediaType fromValue(String value) {
        for (ExternalIdMediaType b : ExternalIdMediaType.values()) {
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

        return String.format("%s=%s", prefix, this.toString());
    }
}
