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
}
