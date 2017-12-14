/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal;

import static org.openhab.binding.velbus.VelbusBindingConstants.*;

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
import org.openhab.binding.velbus.handler.VelbusBridgeHandler;
import org.openhab.binding.velbus.handler.VelbusVMB2BLEHandler;
import org.openhab.binding.velbus.handler.VelbusVMB4DCHandler;
import org.openhab.binding.velbus.handler.VelbusVMB4RYLDHandler;
import org.openhab.binding.velbus.handler.VelbusVMB7INHandler;
import org.openhab.binding.velbus.handler.VelbusVMBGPHandler;
import org.openhab.binding.velbus.handler.VelbusVMBGPOHandler;
import org.openhab.binding.velbus.handler.VelbusVMBPBHandler;
import org.openhab.binding.velbus.handler.VelbusVMBPIROHandler;
import org.openhab.binding.velbus.internal.discovery.VelbusThingDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link VelbusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Cedric Boon - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.velbus")
public class VelbusHandlerFactory extends BaseThingHandlerFactory {
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingHandler thingHandler = null;

        if (BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            VelbusBridgeHandler velbusBridgeHandler = new VelbusBridgeHandler((Bridge) thing);
            registerDiscoveryService(velbusBridgeHandler);
            thingHandler = velbusBridgeHandler;
        } else if (VelbusVMB4RYLDHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMB4RYLDHandler(thing);
        } else if (VelbusVMB4DCHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMB4DCHandler(thing);
        } else if (VelbusVMB2BLEHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMB2BLEHandler(thing);
        } else if (VelbusVMBPBHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBPBHandler(thing);
        } else if (VelbusVMBPBHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBPBHandler(thing);
        } else if (VelbusVMBGPHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBGPHandler(thing);
        } else if (VelbusVMBGPOHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBGPOHandler(thing);
        } else if (VelbusVMBPIROHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBPIROHandler(thing);
        } else if (VelbusVMB7INHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMB7INHandler(thing);
        }

        return thingHandler;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof VelbusBridgeHandler) {
            unregisterDiscoveryService((VelbusBridgeHandler) thingHandler);
        }
        super.removeHandler(thingHandler);
    }

    private void registerDiscoveryService(VelbusBridgeHandler bridgeHandler) {
        VelbusThingDiscoveryService discoveryService = new VelbusThingDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private void unregisterDiscoveryService(VelbusBridgeHandler bridgeHandler) {
        ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(bridgeHandler.getThing().getUID());
        if (serviceReg != null) {
            VelbusThingDiscoveryService service = (VelbusThingDiscoveryService) bundleContext
                .getService(serviceReg.getReference());
            if (service != null) {
                service.deactivate();
            }
            serviceReg.unregister();
            discoveryServiceRegs.remove(bridgeHandler.getThing().getUID());
        }
    }
}
