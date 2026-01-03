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
 * Gets or Sets RecommendationType
 */
public enum RecommendationType {

    SIMILAR_TO_RECENTLY_PLAYED("SimilarToRecentlyPlayed"),

    SIMILAR_TO_LIKED_ITEM("SimilarToLikedItem"),

    HAS_DIRECTOR_FROM_RECENTLY_PLAYED("HasDirectorFromRecentlyPlayed"),

    HAS_ACTOR_FROM_RECENTLY_PLAYED("HasActorFromRecentlyPlayed"),

    HAS_LIKED_DIRECTOR("HasLikedDirector"),

    HAS_LIKED_ACTOR("HasLikedActor");

    private String value;

    RecommendationType(String value) {
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
    public static RecommendationType fromValue(String value) {
        for (RecommendationType b : RecommendationType.values()) {
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
