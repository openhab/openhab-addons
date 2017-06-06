/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal;

import static org.openhab.binding.mihome.MiHomeBindingConstants.*;

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
import org.openhab.binding.mihome.handler.MiHomeGatewayHandler;
import org.openhab.binding.mihome.handler.MiHomeSubdevicesHandler;
import org.openhab.binding.mihome.internal.discovery.MiHomeSubdeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Sets;

/**
 * The {@link MiHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class MiHomeHandlerFactory extends BaseThingHandlerFactory {

    @SuppressWarnings("rawtypes")
    private Map<ThingUID, ServiceRegistration> discoveryServiceRegs = new HashMap<>();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_GATEWAY,
            THING_TYPE_ENERGY_MONITOR, THING_TYPE_MOTION_SENSOR, THING_TYPE_OPEN_SENSOR);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            MiHomeGatewayHandler gatewayHandler = new MiHomeGatewayHandler((Bridge) thing);
            registerDiscoveryService(gatewayHandler);
            return gatewayHandler;
        } else {
            return new MiHomeSubdevicesHandler(thing);
        }
    }

    private void registerDiscoveryService(MiHomeGatewayHandler gatewayHandler) {
        MiHomeSubdeviceDiscoveryService service = new MiHomeSubdeviceDiscoveryService(gatewayHandler);
        @SuppressWarnings("rawtypes")
        ServiceRegistration serviceReg = bundleContext.registerService(DiscoveryService.class.getName(), service,
                new Hashtable<String, Object>());
        discoveryServiceRegs.put(gatewayHandler.getThing().getUID(), serviceReg);
    }

    private void unregisterDiscoveryService(MiHomeGatewayHandler gatewayHandler) {
        @SuppressWarnings("rawtypes")
        ServiceRegistration serviceReg = discoveryServiceRegs.get(gatewayHandler.getThing().getUID());
        serviceReg.unregister();
        discoveryServiceRegs.remove(gatewayHandler.getThing().getUID());
    }

    @Override
    protected synchronized void removeHandler(ThingHandler handler) {
        if (handler instanceof MiHomeGatewayHandler) {
            unregisterDiscoveryService((MiHomeGatewayHandler) handler);
        }
    }
}
