/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway.internal;

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
import org.openhab.binding.zway.handler.ZWayBridgeHandler;
import org.openhab.binding.zway.handler.ZWayZAutomationDeviceHandler;
import org.openhab.binding.zway.handler.ZWayZWaveDeviceHandler;
import org.openhab.binding.zway.internal.discovery.ZWayDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link ZWayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Hecker - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.zway")
public class ZWayHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(
            ZWayBridgeHandler.SUPPORTED_THING_TYPE, ZWayZAutomationDeviceHandler.SUPPORTED_THING_TYPE,
            ZWayZWaveDeviceHandler.SUPPORTED_THING_TYPE);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (ZWayBridgeHandler.SUPPORTED_THING_TYPE.equals(thing.getThingTypeUID())) {
            ZWayBridgeHandler handler = new ZWayBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);

            return handler;
        } else if (ZWayZAutomationDeviceHandler.SUPPORTED_THING_TYPE.equals(thing.getThingTypeUID())) {
            return new ZWayZAutomationDeviceHandler(thing);
        } else if (ZWayZWaveDeviceHandler.SUPPORTED_THING_TYPE.equals(thing.getThingTypeUID())) {
            return new ZWayZWaveDeviceHandler(thing);
        } else {
            return null;
        }
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ZWayBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    private void registerDeviceDiscoveryService(ZWayBridgeHandler handler) {
        ZWayDeviceDiscoveryService discoveryService = new ZWayDeviceDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
