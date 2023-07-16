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
package org.openhab.binding.connectedcar.internal.handler;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.discovery.ConnectedCarDiscoveryService;
import org.openhab.binding.connectedcar.internal.provider.CarChannelTypeProvider;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions;
import org.openhab.binding.connectedcar.internal.util.TextResources;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
@Component(configurationPid = "binding." + BINDING_ID, service = ThingHandlerFactory.class)
public class HandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(HandlerFactory.class);

    private final TextResources resources;
    private final ChannelDefinitions channelIdMapper;
    private final IdentityManager tokenManager;
    private final CarChannelTypeProvider channelTypeProvider;
    private final ZoneId zoneId;
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();

    @Activate
    public HandlerFactory(@Reference TimeZoneProvider tzProvider, @Reference TextResources resources,
            @Reference ChannelDefinitions channelIdMapper, @Reference IdentityManager tokenManager,
            @Reference CarChannelTypeProvider channelTypeProvider) {
        this.resources = resources;
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
                    || THING_TYPE_VWID.equals(thingTypeUID) || THING_TYPE_SKODA.equals(thingTypeUID)
                    || THING_TYPE_ENYAK.equals(thingTypeUID) || THING_TYPE_SEAT.equals(thingTypeUID)
                    || THING_TYPE_FORD.equals(thingTypeUID) || THING_TYPE_WECHARGE.equals(thingTypeUID)) {
                AccountHandler handler = new AccountHandler((Bridge) thing, resources, tokenManager);
                registerDeviceDiscoveryService(handler);
                return handler;
            } else if (THING_TYPE_CNVEHICLE.equals(thingTypeUID)) {
                return new CarNetVehicleHandler(thing, resources, zoneId, channelIdMapper, channelTypeProvider);
            } else if (THING_TYPE_IDVEHICLE.equals(thingTypeUID)) {
                return new WeConnectVehicleHandler(thing, resources, zoneId, channelIdMapper, channelTypeProvider);
            } else if (THING_TYPE_SKODAEVEHICLE.equals(thingTypeUID)) {
                return new EnyakVehicleHandler(thing, resources, zoneId, channelIdMapper, channelTypeProvider);
            } else if (THING_TYPE_FORDVEHICLE.equals(thingTypeUID)) {
                return new FordVehicleHandler(thing, resources, zoneId, channelIdMapper, channelTypeProvider);
            } else if (THING_TYPE_WCWALLBOX.equals(thingTypeUID)) {
                return new WeChargeThingHandler(thing, resources, zoneId, channelIdMapper, channelTypeProvider);
            } else {
                logger.warn("HandlerFactory: Unsuppoerted ThingType requested: {}", thingTypeUID);
            }
        } catch (ApiException e) {
            logger.warn("Unable to create thing of type {}", thingTypeUID);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof AccountHandler) {
            unregisterDeviceDiscoveryService((AccountHandler) thingHandler);
        }
    }

    private synchronized void registerDeviceDiscoveryService(AccountHandler bridgeHandler) {
        ConnectedCarDiscoveryService discoveryService = new ConnectedCarDiscoveryService(bridgeHandler,
                bundleContext.getBundle());
        discoveryService.activate();
        this.discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void unregisterDeviceDiscoveryService(AccountHandler bridgeHandler) {
        ServiceRegistration<?> serviceRegistration = this.discoveryServiceRegistrations
                .get(bridgeHandler.getThing().getUID());
        if (serviceRegistration != null) {
            ConnectedCarDiscoveryService discoveryService = (ConnectedCarDiscoveryService) bundleContext
                    .getService(serviceRegistration.getReference());
            if (discoveryService != null) {
                discoveryService.deactivate();
            }
            serviceRegistration.unregister();
            discoveryServiceRegistrations.remove(bridgeHandler.getThing().getUID());
        }
    }
}
