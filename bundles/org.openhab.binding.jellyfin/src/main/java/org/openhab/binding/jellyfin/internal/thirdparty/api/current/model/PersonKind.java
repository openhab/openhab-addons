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
 * The person kind.
 */
public enum PersonKind {

    UNKNOWN("Unknown"),

    ACTOR("Actor"),

    DIRECTOR("Director"),

    COMPOSER("Composer"),

    WRITER("Writer"),

    GUEST_STAR("GuestStar"),

    PRODUCER("Producer"),

    CONDUCTOR("Conductor"),

    LYRICIST("Lyricist"),

    ARRANGER("Arranger"),

    ENGINEER("Engineer"),

    MIXER("Mixer"),

    REMIXER("Remixer"),

    CREATOR("Creator"),

    ARTIST("Artist"),

    ALBUM_ARTIST("AlbumArtist"),

    AUTHOR("Author"),

    ILLUSTRATOR("Illustrator"),

    PENCILLER("Penciller"),

    INKER("Inker"),

    COLORIST("Colorist"),

    LETTERER("Letterer"),

    COVER_ARTIST("CoverArtist"),

    EDITOR("Editor"),

    TRANSLATOR("Translator");

    private String value;

    PersonKind(String value) {
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
    public static PersonKind fromValue(String value) {
        for (PersonKind b : PersonKind.values()) {
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
