/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link OpenUVResult} is responsible for storing
 * the "result" node from the OpenUV JSON response
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenUVResult {
    private final ZonedDateTime DEFAULT_ZDT = ZonedDateTime.of(LocalDateTime.MIN, ZoneId.systemDefault());
    private DecimalType uv = new DecimalType(0);
    private ZonedDateTime uvTime = DEFAULT_ZDT;
    private DecimalType uvMax = new DecimalType(0);
    private ZonedDateTime uvMaxTime = DEFAULT_ZDT;
    private DecimalType ozone = new DecimalType(0);
    private ZonedDateTime ozoneTime = DEFAULT_ZDT;
    private SafeExposureTime safeExposureTime = new SafeExposureTime();

    public DecimalType getUv() {
        return uv;
    }

    public DecimalType getUvMax() {
        return uvMax;
    }

    public DecimalType getOzone() {
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
