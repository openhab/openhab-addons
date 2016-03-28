/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal;

import static org.openhab.binding.squeezebox.SqueezeBoxBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.squeezebox.discovery.SqueezeBoxPlayerDiscoveryParticipant;
import org.openhab.binding.squeezebox.handler.SqueezeBoxPlayerHandler;
import org.openhab.binding.squeezebox.handler.SqueezeBoxServerHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.google.common.collect.Sets;

/**
 * The {@link SqueezeBoxHandlerFactory} is responsible for creating things and
 * thing handlers.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class SqueezeBoxHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(SqueezeBoxHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
            SqueezeBoxServerHandler.SUPPORTED_THING_TYPES_UIDS, SqueezeBoxPlayerHandler.SUPPORTED_THING_TYPES_UIDS);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SQUEEZEBOXPLAYER_THING_TYPE)) {
            return new SqueezeBoxPlayerHandler(thing);
        }

        if (thingTypeUID.equals(SQUEEZEBOXSERVER_THING_TYPE)) {
            logger.trace("Returning handler for bridge thing {}", thing);
            SqueezeBoxServerHandler handler = new SqueezeBoxServerHandler((Bridge) thing);
            registerSqueezeBoxPlayerDiscoveryService(handler);
            return handler;
        }

        return null;
    }

    /**
     * Adds SqueezeBoxServerHandlers to the discovery service to find SqueezeBox
     * Players
     * 
     * @param squeezeBoxServerHandler
     */
    private synchronized void registerSqueezeBoxPlayerDiscoveryService(SqueezeBoxServerHandler squeezeBoxServerHandler) {
        SqueezeBoxPlayerDiscoveryParticipant discoveryService = new SqueezeBoxPlayerDiscoveryParticipant(
                squeezeBoxServerHandler);
        squeezeBoxServerHandler.registerSqueezeBoxPlayerListener(discoveryService);
        this.discoveryServiceRegs.put(squeezeBoxServerHandler.getThing().getUID(), bundleContext.registerService(
                DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SqueezeBoxServerHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
        if (thingHandler instanceof SqueezeBoxPlayerHandler) {
            SqueezeBoxServerHandler bridge = ((SqueezeBoxPlayerHandler) thingHandler).getSqueezeBoxServerHandler();
            if (bridge != null) {
                bridge.removePlayerCache(((SqueezeBoxPlayerHandler) thingHandler).getMac());
            }
        }
    }
}
