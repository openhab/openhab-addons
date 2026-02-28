/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.AdjustableForecastSolarPlaneHandler;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarBridgeHandler;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.ForecastSolarPlaneHandler;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.SmartForecastSolarBridgeHandler;
import org.openhab.binding.solarforecast.internal.forecastsolar.handler.SmartForecastSolarPlaneHandler;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastBridgeHandler;
import org.openhab.binding.solarforecast.internal.solcast.handler.SolcastPlaneHandler;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.PointType;
import org.openhab.core.persistence.PersistenceServiceRegistry;
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
 * @author Bernd Weymann - Forward persistence to forecast.solar plane handler
 * @author Bernd Weymann - Provide available persistence service as configuration options
 */
@NonNullByDefault
@Component(configurationPid = "binding.solarforecast", service = { ThingHandlerFactory.class })

public class SolarForecastHandlerFactory extends BaseThingHandlerFactory {
    private final TimeZoneProvider timeZoneProvider;
    private final HttpClientFactory httpClientFactory;
    private final PersistenceServiceRegistry persistenceRegistry;
    private @Nullable PointType location;
    private Storage<String> storage;

    @Activate
    public SolarForecastHandlerFactory(final @Reference HttpClientFactory hcf, final @Reference LocationProvider lp,
            final @Reference TimeZoneProvider tzp, final @Reference StorageService storageService,
            final @Reference PersistenceServiceRegistry psr) {
        persistenceRegistry = psr;
        timeZoneProvider = tzp;
        httpClientFactory = hcf;
        Utils.setTimeZoneProvider(tzp);
        location = lp.getLocation();
        storage = storageService.getStorage(SolarForecastBindingConstants.BINDING_ID);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SolarForecastBindingConstants.SUPPORTED_THING_SET.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (FORECAST_SOLAR_SITE.equals(thingTypeUID) || ADJUSTABLE_FORECAST_SOLAR_SITE.equals(thingTypeUID)) {
            return new ForecastSolarBridgeHandler((Bridge) thing, location);
        } else if (SMART_FORECAST_SOLAR_SITE.equals(thingTypeUID)) {
            return new SmartForecastSolarBridgeHandler((Bridge) thing, location);
        } else if (FORECAST_SOLAR_PLANE.equals(thingTypeUID)) {
            return new ForecastSolarPlaneHandler(thing, httpClientFactory.getCommonHttpClient());
        } else if (ADJUSTABLE_FORECAST_SOLAR_PLANE.equals(thingTypeUID)) {
            return new AdjustableForecastSolarPlaneHandler(thing, httpClientFactory.getCommonHttpClient(),
                    persistenceRegistry);
        } else if (SMART_FORECAST_SOLAR_PLANE.equals(thingTypeUID)) {
            return new SmartForecastSolarPlaneHandler(thing, httpClientFactory.getCommonHttpClient(),
                    persistenceRegistry);
        } else if (SOLCAST_SITE.equals(thingTypeUID)) {
            return new SolcastBridgeHandler((Bridge) thing, timeZoneProvider);
        } else if (SOLCAST_PLANE.equals(thingTypeUID)) {
            return new SolcastPlaneHandler(thing, httpClientFactory.getCommonHttpClient(), storage);
        }
        return null;
    }
}
