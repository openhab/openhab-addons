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
package org.openhab.binding.openweathermap.internal.factory;

import static org.openhab.binding.openweathermap.internal.OpenWeatherMapBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.openweathermap.internal.discovery.OpenWeatherMapDiscoveryService;
import org.openhab.binding.openweathermap.internal.handler.AbstractOpenWeatherMapHandler;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapAPIHandler;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapAirPollutionHandler;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapOneCallHandler;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapOneCallHistoryHandler;
import org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapWeatherAndForecastHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link OpenWeatherMapHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.openweathermap")
public class OpenWeatherMapHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.concat(OpenWeatherMapAPIHandler.SUPPORTED_THING_TYPES.stream(),
                    AbstractOpenWeatherMapHandler.SUPPORTED_THING_TYPES.stream()).collect(Collectors.toSet()));

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private final TranslationProvider i18nProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public OpenWeatherMapHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference LocaleProvider localeProvider, final @Reference LocationProvider locationProvider,
            final @Reference TranslationProvider i18nProvider, final @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.localeProvider = localeProvider;
        this.locationProvider = locationProvider;
        this.i18nProvider = i18nProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_WEATHER_API.equals(thingTypeUID)) {
            OpenWeatherMapAPIHandler handler = new OpenWeatherMapAPIHandler((Bridge) thing, httpClient, localeProvider);
            // register discovery service
            OpenWeatherMapDiscoveryService discoveryService = new OpenWeatherMapDiscoveryService(handler,
                    locationProvider, localeProvider, i18nProvider);
            discoveryServiceRegs.put(handler.getThing().getUID(),
                    bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, null));
            return handler;
        } else if (THING_TYPE_WEATHER_AND_FORECAST.equals(thingTypeUID)) {
            return new OpenWeatherMapWeatherAndForecastHandler(thing, timeZoneProvider);
        } else if (THING_TYPE_AIR_POLLUTION.equals(thingTypeUID)) {
            return new OpenWeatherMapAirPollutionHandler(thing, timeZoneProvider);
        } else if (THING_TYPE_ONECALL_WEATHER_AND_FORECAST.equals(thingTypeUID)) {
            return new OpenWeatherMapOneCallHandler(thing, timeZoneProvider);
        } else if (THING_TYPE_ONECALL_HISTORY.equals(thingTypeUID)) {
            return new OpenWeatherMapOneCallHistoryHandler(thing, timeZoneProvider);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof OpenWeatherMapAPIHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                OpenWeatherMapDiscoveryService discoveryService = (OpenWeatherMapDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (discoveryService != null) {
                    discoveryService.deactivate();
                }
            }
        }
    }
}
