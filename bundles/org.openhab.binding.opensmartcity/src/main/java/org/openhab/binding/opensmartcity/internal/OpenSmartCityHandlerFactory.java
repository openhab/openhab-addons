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
package org.openhab.binding.opensmartcity.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.opensmartcity.internal.discovery.OpenSmartCityWeatherDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
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
 * The {@link OpenSmartCityHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.opensmartcity", service = ThingHandlerFactory.class)
public class OpenSmartCityHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(
            org.openhab.binding.opensmartcity.internal.OpenSmartCityBindingConstants.THING_TYPE_CITY,
            org.openhab.binding.opensmartcity.internal.OpenSmartCityBindingConstants.THING_TYPE_WEATHER);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private final TranslationProvider i18nProvider;

    @Activate
    public OpenSmartCityHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
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

        if (org.openhab.binding.opensmartcity.internal.OpenSmartCityBindingConstants.THING_TYPE_CITY
                .equals(thingTypeUID)) {
            OpenSmartCityCityHandler handler = new OpenSmartCityCityHandler((Bridge) thing, httpClient);
            // register discovery service
            OpenSmartCityWeatherDiscoveryService discoveryService = new OpenSmartCityWeatherDiscoveryService(handler,
                    locationProvider, localeProvider, i18nProvider);
            discoveryServiceRegs.put(handler.getThing().getUID(),
                    bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, null));
            return handler;
        }
        if (org.openhab.binding.opensmartcity.internal.OpenSmartCityBindingConstants.THING_TYPE_WEATHER
                .equals(thingTypeUID)) {
            OpenSmartCityWeatherHandler handler = new OpenSmartCityWeatherHandler(thing);
            return handler;
        }

        return null;
    }
}
