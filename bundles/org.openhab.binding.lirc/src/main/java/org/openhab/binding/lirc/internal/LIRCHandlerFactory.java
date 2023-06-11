/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lirc.internal;

import static org.openhab.binding.lirc.internal.LIRCBindingConstants.THING_TYPE_REMOTE;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.openhab.binding.lirc.internal.discovery.LIRCRemoteDiscoveryService;
import org.openhab.binding.lirc.internal.handler.LIRCBridgeHandler;
import org.openhab.binding.lirc.internal.handler.LIRCRemoteHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link LIRCHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andrew Nagle - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.lirc")
public class LIRCHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return LIRCBindingConstants.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (LIRCBindingConstants.SUPPORTED_BRIDGE_TYPES.contains(thingTypeUID)) {
            LIRCBridgeHandler handler = new LIRCBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_REMOTE)) {
            return new LIRCRemoteHandler(thing);
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (this.discoveryServiceRegs != null) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }

    private synchronized void registerDeviceDiscoveryService(LIRCBridgeHandler handler) {
        LIRCRemoteDiscoveryService discoveryService = new LIRCRemoteDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
