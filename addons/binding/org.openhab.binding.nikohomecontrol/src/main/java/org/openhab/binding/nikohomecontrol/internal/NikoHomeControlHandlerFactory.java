/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlHandler;
import org.openhab.binding.nikohomecontrol.internal.discovery.NikoHomeControlDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NikoHomeControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mark Herwege - Initial Contribution
 */

@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.nikohomecontrol")
public class NikoHomeControlHandlerFactory extends BaseThingHandlerFactory {

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (BRIDGE_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            NikoHomeControlBridgeHandler handler = new NikoHomeControlBridgeHandler((Bridge) thing);
            registerNikoHomeControlDiscoveryService(handler);
            return handler;
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new NikoHomeControlHandler(thing);
        }

        return null;
    }

    private void registerNikoHomeControlDiscoveryService(NikoHomeControlBridgeHandler bridgeHandler) {
        NikoHomeControlDiscoveryService nhcDiscoveryService = new NikoHomeControlDiscoveryService(bridgeHandler);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext.registerService(
                DiscoveryService.class.getName(), nhcDiscoveryService, new Hashtable<String, Object>()));
        nhcDiscoveryService.activate();
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NikoHomeControlBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                NikoHomeControlDiscoveryService nhcDiscoveryService = (NikoHomeControlDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                nhcDiscoveryService.deactivate();
                serviceReg.unregister();
                this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
