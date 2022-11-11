/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.openuv.internal.json;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OpenUVResult} is responsible for storing the result node from the OpenUV JSON response
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenUVResult {
    private final Logger logger = LoggerFactory.getLogger(OpenUVResult.class);

    public enum FitzpatrickType {
        @SerializedName("st1")
        I, // Fitzpatrick Skin Type I
        @SerializedName("st2")
        II, // Fitzpatrick Skin Type II
        @SerializedName("st3")
        III, // Fitzpatrick Skin Type III
        @SerializedName("st4")
        IV, // Fitzpatrick Skin Type IV
        @SerializedName("st5")
        V, // Fitzpatrick Skin Type V
        @SerializedName("st6")
        VI;// Fitzpatrick Skin Type VI
    }

    private double uv;
    private double uvMax;
    private double ozone;
    private @Nullable ZonedDateTime uvTime;
    private @Nullable ZonedDateTime uvMaxTime;
    private @Nullable ZonedDateTime ozoneTime;
    private Map<FitzpatrickType, @Nullable Integer> safeExposureTime = new HashMap<>();

    public double getUv() {
        return uv;
    }

    public double getUvMax() {
        return uvMax;
    }

    public double getOzone() {
        return ozone;
    }

    private State getValueOrNull(@Nullable ZonedDateTime value) {
        return value == null ? UnDefType.NULL : new DateTimeType(value);
    }

    public State getUVTime() {
        return getValueOrNull(uvTime);
    }

    public State getUVMaxTime() {
        return getValueOrNull(uvMaxTime);
    }

    public State getOzoneTime() {
        return getValueOrNull(ozoneTime);
    }

    public State getSafeExposureTime(String index) {
        try {
            FitzpatrickType value = FitzpatrickType.valueOf(index);
            Integer duration = safeExposureTime.get(value);
            if (duration != null) {
                return QuantityType.valueOf(duration, Units.MINUTE);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Unexpected Fitzpatrick index value '{}' : {}", index, e.getMessage());
        }
        return UnDefType.NULL;
    }
}
