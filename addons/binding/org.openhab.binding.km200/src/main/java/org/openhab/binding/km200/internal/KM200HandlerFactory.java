/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.internal;

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
import org.openhab.binding.km200.discovery.KM200GatewayDiscoveryService;
import org.openhab.binding.km200.handler.KM200GatewayHandler;
import org.openhab.binding.km200.handler.KM200ThingHandler;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Sets;

/**
 * The {@link KM200HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200HandlerFactory extends BaseThingHandlerFactory {

    public final static Set<ThingTypeUID> SUPPORTED_ALL_THING_TYPES_UIDS = Sets
            .union(KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS, KM200ThingHandler.SUPPORTED_THING_TYPES_UIDS);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_ALL_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (KM200GatewayHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            KM200GatewayHandler gatewayHandler = new KM200GatewayHandler((Bridge) thing);
            registerKM200GatewayDiscoveryService(gatewayHandler);
            return gatewayHandler;
        } else if (KM200ThingHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new KM200ThingHandler(thing);
        } else {
            return null;
        }

    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof KM200GatewayHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    /**
     * Adds KM200GatewayHandler to the discovery service to find the KMXXX device
     *
     * @param gatewayHandler
     */
    private synchronized void registerKM200GatewayDiscoveryService(KM200GatewayHandler gatewayHandler) {

        KM200GatewayDiscoveryService discoveryService = new KM200GatewayDiscoveryService(gatewayHandler);
        this.discoveryServiceRegs.put(gatewayHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

}
