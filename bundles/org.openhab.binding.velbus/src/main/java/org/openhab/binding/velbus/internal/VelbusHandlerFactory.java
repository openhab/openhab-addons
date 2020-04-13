/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.velbus.internal;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

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
import org.openhab.binding.velbus.internal.discovery.VelbusThingDiscoveryService;
import org.openhab.binding.velbus.internal.handler.VelbusBlindsHandler;
import org.openhab.binding.velbus.internal.handler.VelbusBridgeHandler;
import org.openhab.binding.velbus.internal.handler.VelbusDimmerHandler;
import org.openhab.binding.velbus.internal.handler.VelbusRelayHandler;
import org.openhab.binding.velbus.internal.handler.VelbusSensorHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBGPHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBGPOHandler;
import org.openhab.binding.velbus.internal.handler.VelbusVMBPIROHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link VelbusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Cedric Boon - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.velbus")
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
        } else if (VelbusRelayHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusRelayHandler(thing);
        } else if (VelbusDimmerHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusDimmerHandler(thing);
        } else if (VelbusBlindsHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusBlindsHandler(thing);
        } else if (VelbusSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusSensorHandler(thing);
        } else if (VelbusVMBGPHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBGPHandler(thing);
        } else if (VelbusVMBGPOHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBGPOHandler(thing);
        } else if (VelbusVMBPIROHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            thingHandler = new VelbusVMBPIROHandler(thing);
        }

        return thingHandler;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof VelbusBridgeHandler) {
            unregisterDiscoveryService((VelbusBridgeHandler) thingHandler);
        }
    }

    private synchronized void registerDiscoveryService(VelbusBridgeHandler bridgeHandler) {
        VelbusThingDiscoveryService discoveryService = new VelbusThingDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private synchronized void unregisterDiscoveryService(VelbusBridgeHandler bridgeHandler) {
        ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(bridgeHandler.getThing().getUID());
        if (serviceReg != null) {
            VelbusThingDiscoveryService service = (VelbusThingDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            serviceReg.unregister();
            if (service != null) {
                service.deactivate();
            }
        }
    }
}
