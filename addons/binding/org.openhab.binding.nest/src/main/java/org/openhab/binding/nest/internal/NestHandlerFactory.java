/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal;

import static org.openhab.binding.nest.NestBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.nest.discovery.NestDiscoveryService;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.handler.NestCameraHandler;
import org.openhab.binding.nest.handler.NestThermostatHandler;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Sets;

/**
 * The {@link NestHandlerFactory} is responsible for creating things and thing
 * handlers. It also sets up the discovery service to track things from the bridge
 * when the bridge is created.
 *
 * @author David Bennett - Initial contribution
 */
public class NestHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_THERMOSTAT,
            THING_TYPE_CAMERA, THING_TYPE_BRIDGE);

    Map<ThingUID, ServiceRegistration<?>> discoveryService = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_THERMOSTAT)) {
            return new NestThermostatHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_CAMERA)) {
            return new NestCameraHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            NestBridgeHandler handler = new NestBridgeHandler((Bridge) thing);
            NestDiscoveryService service = new NestDiscoveryService(handler);
            service.activate();
            // Register the discovery service.
            discoveryService.put(handler.getThing().getUID(), bundleContext
                    .registerService(NestDiscoveryService.class.getName(), service, new Hashtable<String, Object>()));
            return handler;
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NestBridgeHandler) {
            ServiceRegistration<?> reg = discoveryService.get(thingHandler.getThing().getUID());
            if (reg != null) {
                // Unregister the discovery service.
                NestDiscoveryService service = (NestDiscoveryService) bundleContext.getService(reg.getReference());
                service.deactivate();
                reg.unregister();
                discoveryService.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
