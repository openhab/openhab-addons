/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.internal;

import static org.openhab.binding.digiplex.DigiplexBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.digiplex.discovery.DigiplexDiscoveryService;
import org.openhab.binding.digiplex.handler.DigiplexAreaHandler;
import org.openhab.binding.digiplex.handler.DigiplexBridgeHandler;
import org.openhab.binding.digiplex.handler.DigiplexZoneHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link DigiplexHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Robert Michalak - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.digiplex")
@NonNullByDefault
public class DigiplexHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, @Nullable ServiceRegistration<DiscoveryService>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ZONE)) {
            return new DigiplexZoneHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_AREA)) {
            return new DigiplexAreaHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            DigiplexBridgeHandler bridge = new DigiplexBridgeHandler((Bridge) thing);
            registerDigiplexDiscoveryService(bridge);
            return bridge;
        }

        return null;
    }

    private synchronized void registerDigiplexDiscoveryService(DigiplexBridgeHandler bridge) {
        DigiplexDiscoveryService discoveryService = new DigiplexDiscoveryService(bridge);
        ServiceRegistration<DiscoveryService> serviceRegistration = bundleContext
                .registerService(DiscoveryService.class, discoveryService, new Hashtable<String, Object>());
        discoveryServiceRegs.put(bridge.getThing().getUID(), serviceRegistration);
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof DigiplexBridgeHandler) {
            ServiceRegistration<DiscoveryService> serviceReg = this.discoveryServiceRegs
                    .get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

}
