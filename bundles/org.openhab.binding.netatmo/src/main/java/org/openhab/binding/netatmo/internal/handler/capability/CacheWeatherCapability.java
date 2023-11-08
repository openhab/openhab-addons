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
import java.time.Instant;
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
    private final Duration validity;

    private List<NAObject> buffer = List.of();
    private Instant bufferEol = Instant.MIN;

    public CacheWeatherCapability(CommonInterface handler, Duration validity) {
        super(handler, WeatherApi.class);
        this.validity = validity;
    }

    @Override
    protected synchronized List<NAObject> updateReadings(WeatherApi api) {
        if (bufferEol.isBefore(Instant.now())) {
            logger.debug("Requesting fresh data");
            NAObject data = getFreshData(api);
            buffer = data != null ? List.of(data) : List.of();
            bufferEol = Instant.now().plus(validity);
        } else {
            logger.debug("Serving cached data");
        }

        return buffer;
    }

    protected abstract @Nullable NAObject getFreshData(WeatherApi api);
}
