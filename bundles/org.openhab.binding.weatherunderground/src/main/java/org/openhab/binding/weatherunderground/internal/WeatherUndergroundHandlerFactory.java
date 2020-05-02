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
package org.openhab.binding.weatherunderground.internal;

import static org.openhab.binding.weatherunderground.internal.WeatherUndergroundBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.weatherunderground.internal.discovery.WeatherUndergroundDiscoveryService;
import org.openhab.binding.weatherunderground.internal.handler.WeatherUndergroundBridgeHandler;
import org.openhab.binding.weatherunderground.internal.handler.WeatherUndergroundHandler;
import org.osgi.framework.ServiceRegistration;
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
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.weatherunderground")
public class WeatherUndergroundHandlerFactory extends BaseThingHandlerFactory {

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private LocaleProvider localeProvider;
    private LocationProvider locationProvider;
    private UnitProvider unitProvider;

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public void unsetLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = null;
    }

    @Reference
    protected void setUnitProvider(final UnitProvider unitProvider) {
        this.unitProvider = unitProvider;
    }

    protected void unsetUnitProvider(final UnitProvider unitProvider) {
        this.unitProvider = null;
    }

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS, WeatherUndergroundBindingConstants.SUPPORTED_THING_TYPES_UIDS)
            .flatMap(x -> x.stream()).collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_WEATHER)) {
            return new WeatherUndergroundHandler(thing, localeProvider, unitProvider);
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
