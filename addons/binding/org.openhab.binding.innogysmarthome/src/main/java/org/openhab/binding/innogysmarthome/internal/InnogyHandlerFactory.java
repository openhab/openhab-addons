/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal;

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
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.innogysmarthome.handler.InnogyBridgeHandler;
import org.openhab.binding.innogysmarthome.handler.InnogyDeviceHandler;
import org.openhab.binding.innogysmarthome.internal.discovery.InnogyDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link InnogyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.innogysmarthome")
public class InnogyHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(InnogyBridgeHandler.SUPPORTED_THING_TYPES,
            InnogyDeviceHandler.SUPPORTED_THING_TYPES);

    private final Logger logger = LoggerFactory.getLogger(InnogyHandlerFactory.class);
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (InnogyBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            InnogyBridgeHandler handler = new InnogyBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (InnogyDeviceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            InnogyDeviceHandler handler = new InnogyDeviceHandler(thing);
            return handler;
        } else {
            logger.debug("Unsupported thing {}.", thing.getThingTypeUID());
            return null;
        }
    }

    /**
     * Registers the device discovery service.
     *
     * @param bridgeHandler
     */
    private synchronized void registerDeviceDiscoveryService(InnogyBridgeHandler bridgeHandler) {
        InnogyDeviceDiscoveryService discoveryService = new InnogyDeviceDiscoveryService(bridgeHandler);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof InnogyBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                InnogyDeviceDiscoveryService service = (InnogyDeviceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
