/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal;

import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.nest.NestBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.handler.NestCameraHandler;
import org.openhab.binding.nest.handler.NestSmokeDetectorHandler;
import org.openhab.binding.nest.handler.NestStructureHandler;
import org.openhab.binding.nest.handler.NestThermostatHandler;
import org.openhab.binding.nest.internal.discovery.NestDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * The {@link NestHandlerFactory} is responsible for creating things and thing
 * handlers. It also sets up the discovery service to track things from the bridge
 * when the bridge is created.
 *
 * @author David Bennett - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.nest", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class NestHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_THERMOSTAT,
            THING_TYPE_CAMERA, THING_TYPE_BRIDGE, THING_TYPE_STRUCTURE, THING_TYPE_SMOKE_DETECTOR).collect(toSet());

    private Map<ThingUID, ServiceRegistration<?>> discoveryService = new HashMap<>();

    /**
     * The things this factory supports creating.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates a handler for the specific thing. THis also creates the discovery service
     * when the bridge is created.
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_THERMOSTAT.equals(thingTypeUID)) {
            return new NestThermostatHandler(thing);
        }

        if (THING_TYPE_CAMERA.equals(thingTypeUID)) {
            return new NestCameraHandler(thing);
        }

        if (THING_TYPE_STRUCTURE.equals(thingTypeUID)) {
            return new NestStructureHandler(thing);
        }

        if (THING_TYPE_SMOKE_DETECTOR.equals(thingTypeUID)) {
            return new NestSmokeDetectorHandler(thing);
        }

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            NestBridgeHandler handler = new NestBridgeHandler((Bridge) thing);
            NestDiscoveryService service = new NestDiscoveryService(handler);
            service.activate();
            // Register the discovery service.
            discoveryService.put(handler.getThing().getUID(),
                    bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));
            return handler;
        }

        return null;
    }

    /**
     * Removes the handler for the specific thing. This also handles disableing the discovery
     * service when the bridge is removed.
     */
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
        super.removeHandler(thingHandler);
    }
}
