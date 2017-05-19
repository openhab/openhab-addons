/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.evohome.EvoHomeBindingConstants;
import org.openhab.binding.evohome.discovery.EvoHomeDiscoveryService;
import org.openhab.binding.evohome.handler.EvoHomeHandler;
import org.openhab.binding.evohome.handler.EvohomeGatewayHandler;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link EvoHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Neil Renaud - Initial contribution
 */
public class EvoHomeHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(EvoHomeBindingConstants.THING_TYPE_EVOHOME_GATEWAY);

    private ServiceRegistration<?> discoveryServiceReg;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(EvoHomeBindingConstants.THING_TYPE_EVOHOME_GATEWAY)) {
            EvohomeGatewayHandler evohomeGatewayHandler = new EvohomeGatewayHandler((Bridge) thing);
            registerDeviceDiscoveryService(evohomeGatewayHandler);
            return evohomeGatewayHandler;
        } else if (thingTypeUID.equals(EvoHomeBindingConstants.THING_TYPE_EVOHOME_RADIATOR_VALVE)) {
            return new EvoHomeHandler(thing);
        }

        return null;
    }

    private void registerDeviceDiscoveryService(EvohomeGatewayHandler evoHomeBridgeHandler) {
        EvoHomeDiscoveryService discoveryService = new EvoHomeDiscoveryService(evoHomeBridgeHandler);

        discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    @Override
    public ThingHandler registerHandler(Thing thing) {
        // TODO Auto-generated method stub
        return super.registerHandler(thing);
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (discoveryServiceReg != null && thingHandler.getThing().getThingTypeUID()
                .equals(EvoHomeBindingConstants.THING_TYPE_EVOHOME_GATEWAY)) {
            discoveryServiceReg.unregister();
            discoveryServiceReg = null;
        }
        super.removeHandler(thingHandler);
    }

}
