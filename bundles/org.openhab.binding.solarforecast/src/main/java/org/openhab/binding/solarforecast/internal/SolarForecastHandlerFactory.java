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
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.PointType;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarForecastHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.solarforecast", service = ThingHandlerFactory.class)
public class SolarForecastHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(SolarForecastHandlerFactory.class);
    private final TimeZoneProvider timeZoneProvider;
    private final ItemRegistry itemRegistry;
    private final HttpClient httpClient;
    private final PointType location;

    private Optional<QueryablePersistenceService> qps = Optional.empty();

    @Activate
    public SolarForecastHandlerFactory(final @Reference HttpClientFactory hcf, final @Reference LocationProvider lp,
            final @Reference PersistenceServiceRegistry psr, final @Reference ItemRegistry ir,
            final @Reference TimeZoneProvider tzp) {
        itemRegistry = ir;
        timeZoneProvider = tzp;
        httpClient = hcf.getCommonHttpClient();
        PointType pt = lp.getLocation();
        if (pt != null) {
            location = pt;
        } else {
            location = PointType.valueOf("0.0,0.0");
        }

        PersistenceService s = psr.getDefault();
        if (s instanceof QueryablePersistenceService) {
            qps = Optional.of((QueryablePersistenceService) s);
        } else {
            logger.info("Persistence {} cannot be queried. Feature Solcast Tuninng will not work", s);
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SolarForecastBindingConstants.SUPPORTED_THING_SET.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (FORECAST_SOLAR_MULTI_STRING.equals(thingTypeUID)) {
            return new ForecastSolarBridgeHandler((Bridge) thing, location);
        } else if (FORECAST_SOLAR_PART_STRING.equals(thingTypeUID)) {
            return new ForecastSolarPlaneHandler(thing, httpClient);
        } else if (SOLCAST_BRIDGE_STRING.equals(thingTypeUID)) {
            return new SolcastBridgeHandler((Bridge) thing, timeZoneProvider);
        } else if (SOLCAST_PART_STRING.equals(thingTypeUID)) {
            return new SolcastPlaneHandler(thing, httpClient, qps, itemRegistry);
        }
        return null;
    }
}
