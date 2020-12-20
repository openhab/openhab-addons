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
package org.openhab.binding.homeconnect.internal.factory;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_API_BRIDGE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_COFFEE_MAKER;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_COOKTOP;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_DISHWASHER;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_DRYER;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_FRIDGE_FREEZER;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_HOOD;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_OVEN;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_WASHER;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.THING_TYPE_WASHER_DRYER;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnect.internal.discovery.HomeConnectDiscoveryService;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectBridgeHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectCoffeeMakerHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectCooktopHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectDishwasherHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectDryerHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectFridgeFreezerHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectHoodHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectOvenHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectWasherDryerHandler;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectWasherHandler;
import org.openhab.binding.homeconnect.internal.servlet.HomeConnectServlet;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.discovery.DiscoveryService;
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
 * The {@link HomeConnectHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.homeconnect", service = ThingHandlerFactory.class)
public class HomeConnectHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations;
    private final OAuthFactory oAuthFactory;
    private final HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final HomeConnectServlet homeConnectServlet;

    @Activate
    public HomeConnectHandlerFactory(@Reference OAuthFactory oAuthFactory,
            @Reference HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            @Reference HomeConnectServlet homeConnectServlet) {
        this.oAuthFactory = oAuthFactory;
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
        this.homeConnectServlet = homeConnectServlet;

        discoveryServiceRegistrations = new HashMap<>();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_API_BRIDGE.equals(thingTypeUID)) {
            HomeConnectBridgeHandler bridgeHandler = new HomeConnectBridgeHandler((Bridge) thing, oAuthFactory,
                    homeConnectServlet);

            // configure discovery service
            HomeConnectDiscoveryService discoveryService = new HomeConnectDiscoveryService(bridgeHandler);
            discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));

            return bridgeHandler;
        } else if (THING_TYPE_DISHWASHER.equals(thingTypeUID)) {
            return new HomeConnectDishwasherHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_OVEN.equals(thingTypeUID)) {
            return new HomeConnectOvenHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_WASHER.equals(thingTypeUID)) {
            return new HomeConnectWasherHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_WASHER_DRYER.equals(thingTypeUID)) {
            return new HomeConnectWasherDryerHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_DRYER.equals(thingTypeUID)) {
            return new HomeConnectDryerHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_FRIDGE_FREEZER.equals(thingTypeUID)) {
            return new HomeConnectFridgeFreezerHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_COFFEE_MAKER.equals(thingTypeUID)) {
            return new HomeConnectCoffeeMakerHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_HOOD.equals(thingTypeUID)) {
            return new HomeConnectHoodHandler(thing, dynamicStateDescriptionProvider);
        } else if (THING_TYPE_COOKTOP.equals(thingTypeUID)) {
            return new HomeConnectCooktopHandler(thing, dynamicStateDescriptionProvider);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HomeConnectBridgeHandler) {
            ThingUID bridgeUID = thingHandler.getThing().getUID();

            @Nullable
            ServiceRegistration<?> serviceRegistration = discoveryServiceRegistrations.get(bridgeUID);
            if (serviceRegistration != null) {
                @Nullable
                HomeConnectDiscoveryService service = (HomeConnectDiscoveryService) bundleContext
                        .getService(serviceRegistration.getReference());

                if (service != null) {
                    service.deactivate();
                }
                serviceRegistration.unregister();
            }

            discoveryServiceRegistrations.remove(bridgeUID);
        }
    }
}
