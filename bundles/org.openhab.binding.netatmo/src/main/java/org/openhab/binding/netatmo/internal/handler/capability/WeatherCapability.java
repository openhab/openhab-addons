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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WeatherCapability} give the ability to read weather station API
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class WeatherCapability extends CacheCapability<WeatherApi> {
    private final Logger logger = LoggerFactory.getLogger(WeatherCapability.class);

    public WeatherCapability(CommonInterface handler) {
        super(handler, Duration.ofSeconds(10), WeatherApi.class);
    }

    @Override
    protected List<NAObject> getFreshData(WeatherApi api) {
        try {
            return List.of(owned ? api.getOwnedStationData(handler.getId()) : api.getStationData(handler.getId()));
        } catch (NetatmoException e) {
            logger.warn("Error retrieving weather data '{}': {}", handler.getId(), e.getMessage());
        }
        return List.of();
    }
}
