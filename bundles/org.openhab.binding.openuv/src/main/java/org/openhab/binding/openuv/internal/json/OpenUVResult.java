/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link OpenUVResult} is responsible for storing
 * the "result" node from the OpenUV JSON response
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenUVResult {
    private final ZonedDateTime DEFAULT_ZDT = ZonedDateTime.of(LocalDateTime.MIN, ZoneId.systemDefault());
    private double uv;
    private ZonedDateTime uvTime = DEFAULT_ZDT;
    private double uvMax;
    private ZonedDateTime uvMaxTime = DEFAULT_ZDT;
    private double ozone;
    private ZonedDateTime ozoneTime = DEFAULT_ZDT;
    private SafeExposureTime safeExposureTime = new SafeExposureTime();

    public int getUv() {
        return (int) uv;
    }

    public int getUvMax() {
        return (int) uvMax;
    }

    public double getOzone() {
        return ozone;
    }

    public State getUVTime() {
        return uvTime != DEFAULT_ZDT ? new DateTimeType(uvTime.withZoneSameInstant(ZoneId.systemDefault()))
                : UnDefType.NULL;
    }

    public State getUVMaxTime() {
        return uvMaxTime != DEFAULT_ZDT ? new DateTimeType(uvMaxTime.withZoneSameInstant(ZoneId.systemDefault()))
                : UnDefType.NULL;
    }

    public State getOzoneTime() {
        return ozoneTime != DEFAULT_ZDT ? new DateTimeType(ozoneTime.withZoneSameInstant(ZoneId.systemDefault()))
                : UnDefType.NULL;
    }

    public SafeExposureTime getSafeExposureTime() {
        return safeExposureTime;
    }
}
