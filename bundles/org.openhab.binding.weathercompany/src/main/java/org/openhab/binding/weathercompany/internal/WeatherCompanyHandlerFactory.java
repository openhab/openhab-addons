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
package org.openhab.binding.weathercompany.internal;

import static org.openhab.binding.weathercompany.internal.WeatherCompanyBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.weathercompany.internal.discovery.WeatherCompanyDiscoveryService;
import org.openhab.binding.weathercompany.internal.handler.WeatherCompanyBridgeHandler;
import org.openhab.binding.weathercompany.internal.handler.WeatherCompanyForecastHandler;
import org.openhab.binding.weathercompany.internal.handler.WeatherCompanyObservationsHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.i18n.UnitProvider;
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
 * The {@link WeatherCompanyHandlerFactory} is responsible for creating thing handlers.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.weathercompany", service = ThingHandlerFactory.class)
public class WeatherCompanyHandlerFactory extends BaseThingHandlerFactory {
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private TimeZoneProvider timeZoneProvider;
    private UnitProvider unitProvider;
    private HttpClient httpClient;
    private LocationProvider locationProvider;
    private LocaleProvider localeProvider;
    private TranslationProvider i18nProvider;

    @Activate
    public WeatherCompanyHandlerFactory(@Reference TimeZoneProvider timeZoneProvider,
            @Reference UnitProvider unitProvider, @Reference HttpClientFactory httpClientFactory,
            @Reference LocationProvider locationProvider, @Reference LocaleProvider localeProvider,
            @Reference TranslationProvider i18nProvider) {
        this.timeZoneProvider = timeZoneProvider;
        this.unitProvider = unitProvider;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.locationProvider = locationProvider;
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_WEATHER_FORECAST.equals(thingTypeUID)) {
            return new WeatherCompanyForecastHandler(thing, timeZoneProvider, httpClient, unitProvider, localeProvider);
        } else if (THING_TYPE_WEATHER_OBSERVATIONS.equals(thingTypeUID)) {
            return new WeatherCompanyObservationsHandler(thing, timeZoneProvider, httpClient, unitProvider,
                    localeProvider);
        } else if (SUPPORTED_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            WeatherCompanyBridgeHandler handler = new WeatherCompanyBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof WeatherCompanyBridgeHandler) {
            ThingUID thingUID = thingHandler.getThing().getUID();
            unregisterDeviceDiscoveryService(thingUID);
        }
        super.removeHandler(thingHandler);
    }

    private synchronized void registerDeviceDiscoveryService(WeatherCompanyBridgeHandler bridgeHandler) {
        WeatherCompanyDiscoveryService discoveryService = new WeatherCompanyDiscoveryService(bridgeHandler,
                locationProvider, localeProvider, i18nProvider);
        discoveryService.activate(null);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private void unregisterDeviceDiscoveryService(ThingUID bridgeUID) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(bridgeUID);
        if (serviceReg != null) {
            WeatherCompanyDiscoveryService discoveryService = (WeatherCompanyDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            serviceReg.unregister();
            if (discoveryService != null) {
                discoveryService.deactivate();
            }
        }
    }
}
