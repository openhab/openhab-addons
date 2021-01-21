/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.weather;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.weather.NAMain;
import org.openhab.binding.netatmo.internal.api.weather.WeatherApi;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.handler.energy.NADescriptionProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;

/**
 * {@link NAMainHandler} is the base class for all current Netatmo
 * weather station equipments (both modules and devices)
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NAMainHandler extends NetatmoDeviceHandler {
    private @Nullable WeatherApi api;

    public NAMainHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NADescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider, descriptionProvider);
        api = apiBridge.getRestManager(WeatherApi.class);
    }

    @Override
    protected NAMain updateReadings() throws NetatmoException {
        if (api != null) {
            return api.getStationData(config.id);
        }
        throw new NetatmoException("No restmanager available for Weather access");
    }
}
