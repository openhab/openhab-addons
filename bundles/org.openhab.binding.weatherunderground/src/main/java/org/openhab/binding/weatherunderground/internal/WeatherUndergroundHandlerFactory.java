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
package org.openhab.binding.weatherunderground.internal;

import static org.openhab.binding.weatherunderground.internal.WeatherUndergroundBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.weatherunderground.internal.discovery.WeatherUndergroundDiscoveryService;
import org.openhab.binding.weatherunderground.internal.handler.WeatherUndergroundBridgeHandler;
import org.openhab.binding.weatherunderground.internal.handler.WeatherUndergroundHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.UnitProvider;
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
 * The {@link WeatherUndergroundHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Theo Giovanna - Added a bridge for the API key
 * @author Laurent Garnier - Registration of the discovery service updated
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.weatherunderground")
public class WeatherUndergroundHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS, WeatherUndergroundBindingConstants.SUPPORTED_THING_TYPES_UIDS)
            .flatMap(x -> x.stream()).collect(Collectors.toSet());

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private final UnitProvider unitProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public WeatherUndergroundHandlerFactory(final @Reference LocaleProvider localeProvider,
            final @Reference LocationProvider locationProvider, final @Reference UnitProvider unitProvider,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.localeProvider = localeProvider;
        this.locationProvider = locationProvider;
        this.unitProvider = unitProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_WEATHER)) {
            return new WeatherUndergroundHandler(thing, localeProvider, unitProvider, timeZoneProvider);
        }

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            WeatherUndergroundBridgeHandler handler = new WeatherUndergroundBridgeHandler((Bridge) thing);
            registerDiscoveryService(handler.getThing().getUID());
            return handler;
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof WeatherUndergroundBridgeHandler) {
            unregisterDiscoveryService(thingHandler.getThing().getUID());
        }
    }

    private synchronized void registerDiscoveryService(ThingUID bridgeUID) {
        WeatherUndergroundDiscoveryService discoveryService = new WeatherUndergroundDiscoveryService(bridgeUID,
                localeProvider, locationProvider);
        discoveryService.activate(null);
        discoveryServiceRegs.put(bridgeUID,
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private synchronized void unregisterDiscoveryService(ThingUID bridgeUID) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(bridgeUID);
        if (serviceReg != null) {
            WeatherUndergroundDiscoveryService service = (WeatherUndergroundDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            serviceReg.unregister();
            if (service != null) {
                service.deactivate();
            }
        }
    }
}
