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
package org.openhab.binding.solarforecast.internal;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarBridgeHandler;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarPlaneHandler;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastBridgeHandler;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastPlaneHandler;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.PointType;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SolarForecastHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Provide storage towards sc-plane
 */
@NonNullByDefault
@Component(configurationPid = "binding.solarforecast", service = ThingHandlerFactory.class)
public class SolarForecastHandlerFactory extends BaseThingHandlerFactory {
    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;
    private Optional<PointType> location = Optional.empty();
    private Storage<String> storage;

    @Activate
    public SolarForecastHandlerFactory(final @Reference HttpClientFactory hcf, final @Reference LocationProvider lp,
            final @Reference TimeZoneProvider tzp, final @Reference StorageService storageService) {
        timeZoneProvider = tzp;
        httpClient = hcf.getCommonHttpClient();
        Utils.setTimeZoneProvider(tzp);
        PointType pt = lp.getLocation();
        if (pt != null) {
            location = Optional.of(pt);
        }
        storage = storageService.getStorage(SolarForecastBindingConstants.BINDING_ID);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SolarForecastBindingConstants.SUPPORTED_THING_SET.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (FORECAST_SOLAR_SITE.equals(thingTypeUID)) {
            return new ForecastSolarBridgeHandler((Bridge) thing, location);
        } else if (FORECAST_SOLAR_PLANE.equals(thingTypeUID)) {
            return new ForecastSolarPlaneHandler(thing, httpClient);
        } else if (SOLCAST_SITE.equals(thingTypeUID)) {
            return new SolcastBridgeHandler((Bridge) thing, timeZoneProvider);
        } else if (SOLCAST_PLANE.equals(thingTypeUID)) {
            return new SolcastPlaneHandler(thing, httpClient, storage);
        }
        return null;
    }
}
