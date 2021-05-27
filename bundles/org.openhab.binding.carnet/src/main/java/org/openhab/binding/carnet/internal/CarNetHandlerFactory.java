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
package org.openhab.binding.carnet.internal;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper;
import org.openhab.binding.carnet.internal.api.CarNetTokenManager;
import org.openhab.binding.carnet.internal.discovery.CarNetDiscoveryService;
import org.openhab.binding.carnet.internal.handler.CarNetAccountHandler;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.carnet.internal.provider.CarNetChannelTypeProvider;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TimeZoneProvider;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CarNetHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding." + BINDING_ID, service = ThingHandlerFactory.class)
public class CarNetHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(CarNetHandlerFactory.class);

    private final HttpClientFactory httpFactory;
    private final CarNetTextResources resources;
    private final CarNetIChanneldMapper channelIdMapper;
    private final CarNetTokenManager tokenManager;
    private final CarNetChannelTypeProvider channelTypeProvider;
    private final ZoneId zoneId;
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();

    @Activate
    public CarNetHandlerFactory(@Reference TimeZoneProvider tzProvider, @Reference CarNetTextResources resources,
            @Reference CarNetIChanneldMapper channelIdMapper, @Reference CarNetTokenManager tokenManager,
            @Reference CarNetChannelTypeProvider channelTypeProvider, @Reference HttpClientFactory httpFactory) {
        this.resources = resources;
        this.httpFactory = httpFactory;
        this.channelIdMapper = channelIdMapper;
        this.tokenManager = tokenManager;
        this.channelTypeProvider = channelTypeProvider;
        zoneId = tzProvider.getTimeZone();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        try {
            if (THING_TYPE_MYAUDI.equals(thingTypeUID) || THING_TYPE_VW.equals(thingTypeUID)
                    || THING_TYPE_VWID.equals(thingTypeUID) || THING_TYPE_VWGO.equals(thingTypeUID)
                    || THING_TYPE_SKODA.equals(thingTypeUID) || THING_TYPE_SEAT.equals(thingTypeUID)) {
                CarNetAccountHandler handler = new CarNetAccountHandler((Bridge) thing, resources, tokenManager,
                        httpFactory);
                registerDeviceDiscoveryService(handler);
                return handler;
            } else if (THING_TYPE_VEHICLE.equals(thingTypeUID)) {
                return new CarNetVehicleHandler(thing, resources, zoneId, channelIdMapper, channelTypeProvider,
                        httpFactory);
            }
        } catch (CarNetException e) {
            logger.warn("Unable to create thing of type {}", thingTypeUID);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof CarNetAccountHandler) {
            unregisterDeviceDiscoveryService((CarNetAccountHandler) thingHandler);
        }
    }

    private synchronized void registerDeviceDiscoveryService(CarNetAccountHandler bridgeHandler) {
        CarNetDiscoveryService discoveryService = new CarNetDiscoveryService(bridgeHandler, bundleContext.getBundle());
        discoveryService.activate();
        this.discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void unregisterDeviceDiscoveryService(CarNetAccountHandler bridgeHandler) {
        ServiceRegistration<?> serviceRegistration = this.discoveryServiceRegistrations
                .get(bridgeHandler.getThing().getUID());
        if (serviceRegistration != null) {
            CarNetDiscoveryService discoveryService = (CarNetDiscoveryService) bundleContext
                    .getService(serviceRegistration.getReference());
            if (discoveryService != null) {
                discoveryService.deactivate();
            }
            serviceRegistration.unregister();
            discoveryServiceRegistrations.remove(bridgeHandler.getThing().getUID());
        }
    }
}
