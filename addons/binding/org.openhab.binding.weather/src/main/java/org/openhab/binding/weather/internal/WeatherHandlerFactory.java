/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weather.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.weather.WeatherBindingConstants;
import org.openhab.binding.weather.handler.WeatherBridgeHandler;
import org.openhab.binding.weather.handler.WeatherThingHandler;
import org.openhab.binding.weather.internal.discovery.WeatherDiscoveryHandler;
import org.openhab.binding.weather.internal.metadata.MetadataHandler;
import org.openhab.binding.weather.internal.model.Weather;
import org.openhab.binding.weather.internal.parser.CommonIdHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link WeatherHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Bennett - Initial contribution
 */
public class WeatherHandlerFactory extends BaseThingHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(WeatherHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .newHashSet(WeatherBindingConstants.THING_TYPE_BRIDGE, WeatherBindingConstants.THING_TYPE_FORECAST);

    CommonIdHandler commonIdHandler = new CommonIdHandler();

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        try {
            commonIdHandler.loadMapping();
            MetadataHandler.getInstance().generate(Weather.class);
        } catch (Exception e) {
            logger.error("Error loading common id mapping", e);
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(WeatherBindingConstants.THING_TYPE_BRIDGE)) {
            WeatherBridgeHandler bridge = new WeatherBridgeHandler((Bridge) thing, commonIdHandler);
            WeatherDiscoveryHandler discovery = new WeatherDiscoveryHandler(bridge);
            discoveryServiceRegs.put(bridge.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discovery, new Hashtable<String, Object>()));
            discovery.activate(null);
            return bridge;
        }

        if (thingTypeUID.equals(WeatherBindingConstants.THING_TYPE_FORECAST)) {
            return new WeatherThingHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        super.removeHandler(thingHandler);
        if (thingHandler instanceof WeatherBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                WeatherDiscoveryHandler service = (WeatherDiscoveryHandler) bundleContext
                        .getService(serviceReg.getReference());
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
