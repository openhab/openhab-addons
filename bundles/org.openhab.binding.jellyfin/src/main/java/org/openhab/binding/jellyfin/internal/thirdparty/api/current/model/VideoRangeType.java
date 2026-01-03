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
 * An enum representing types of video ranges.
 */
public enum VideoRangeType {

    UNKNOWN("Unknown"),

    SDR("SDR"),

    HDR10("HDR10"),

    HLG("HLG"),

    DOVI("DOVI"),

    DOVI_WITH_HDR10("DOVIWithHDR10"),

    DOVI_WITH_HLG("DOVIWithHLG"),

    DOVI_WITH_SDR("DOVIWithSDR"),

    DOVI_WITH_EL("DOVIWithEL"),

    DOVI_WITH_HDR10_PLUS("DOVIWithHDR10Plus"),

    DOVI_WITH_ELHDR10_PLUS("DOVIWithELHDR10Plus"),

    DOVI_INVALID("DOVIInvalid"),

    HDR10_PLUS("HDR10Plus");

    private String value;

    VideoRangeType(String value) {
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
    public static VideoRangeType fromValue(String value) {
        for (VideoRangeType b : VideoRangeType.values()) {
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
