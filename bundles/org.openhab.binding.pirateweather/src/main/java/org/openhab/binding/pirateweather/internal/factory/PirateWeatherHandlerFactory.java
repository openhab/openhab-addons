/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.darksky.internal.factory;

import static org.openhab.binding.darksky.internal.DarkSkyBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.darksky.internal.discovery.DarkSkyDiscoveryService;
import org.openhab.binding.darksky.internal.handler.DarkSkyAPIHandler;
import org.openhab.binding.darksky.internal.handler.DarkSkyWeatherAndForecastHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link DarkSkyHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.darksky")
public class DarkSkyHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream
                    .concat(DarkSkyAPIHandler.SUPPORTED_THING_TYPES.stream(),
                            DarkSkyWeatherAndForecastHandler.SUPPORTED_THING_TYPES.stream())
                    .collect(Collectors.toSet()));

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private final TranslationProvider i18nProvider;

    @Activate
    public DarkSkyHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference LocaleProvider localeProvider, final @Reference LocationProvider locationProvider,
            final @Reference TranslationProvider i18nProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.localeProvider = localeProvider;
        this.locationProvider = locationProvider;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_WEATHER_API.equals(thingTypeUID)) {
            DarkSkyAPIHandler handler = new DarkSkyAPIHandler((Bridge) thing, httpClient, localeProvider);
            // register discovery service
            DarkSkyDiscoveryService discoveryService = new DarkSkyDiscoveryService(handler, locationProvider,
                    localeProvider, i18nProvider);
            discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
            return handler;
        } else if (THING_TYPE_WEATHER_AND_FORECAST.equals(thingTypeUID)) {
            return new DarkSkyWeatherAndForecastHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof DarkSkyAPIHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                DarkSkyDiscoveryService discoveryService = (DarkSkyDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (discoveryService != null) {
                    discoveryService.deactivate();
                }
            }
        }
    }
}
