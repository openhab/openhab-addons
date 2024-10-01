/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.visualcrossing.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public record Day(@Nullable String datetime, @Nullable Long datetimeEpoch, @Nullable Double tempmax,
        @Nullable Double tempmin, @Nullable Double temp, @Nullable Double feelslikemax, @Nullable Double feelslikemin,
        @Nullable Double feelslike, @Nullable Double dew, @Nullable Double humidity, @Nullable Double precip,
        @Nullable Double precipprob, @Nullable Double precipcover, @Nullable List<String> preciptype,
        @Nullable Double snow, @Nullable Double snowdepth, @Nullable Double windgust, @Nullable Double windspeed,
        @Nullable Double winddir, @Nullable Double pressure, @Nullable Double cloudcover, @Nullable Double visibility,
        @Nullable Double solarradiation, @Nullable Double solarenergy, @Nullable Double uvindex,
        @Nullable Double severerisk, @Nullable String sunrise, @Nullable Long sunriseEpoch, @Nullable String sunset,
        @Nullable Long sunsetEpoch, @Nullable Double moonphase, @Nullable String conditions,
        @Nullable String description, @Nullable String icon, @Nullable List<String> stations, @Nullable String source,
        @Nullable List<Hour> hours) {
}
