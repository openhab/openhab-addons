/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CacheWeatherCapability} give the ability to buffer weather related requests and reduce server requests
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class CacheWeatherCapability extends RestCapability<WeatherApi> {
    private final Logger logger = LoggerFactory.getLogger(CacheWeatherCapability.class);
    private final int minValidity;
    private final ChronoUnit unit;

    private List<NAObject> lastResult = List.of();
    private @Nullable ZonedDateTime requestTS;

    public CacheWeatherCapability(CommonInterface handler, int minValidity, ChronoUnit unit) {
        super(handler, WeatherApi.class);
        this.minValidity = minValidity;
        this.unit = unit;
    }

    @Override
    protected List<NAObject> updateReadings(WeatherApi api) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime timestamp = requestTS;

        if (timestamp == null || Duration.between(timestamp, now).get(unit) > minValidity) {
            logger.debug("Requesting fresh data");
            lastResult = getFreshData(api);
            requestTS = now;
        }

        return lastResult;
    }

    protected abstract List<NAObject> getFreshData(WeatherApi api);
}
