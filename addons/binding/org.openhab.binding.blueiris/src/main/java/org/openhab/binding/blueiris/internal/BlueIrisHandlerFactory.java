/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.internal;

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
import org.openhab.binding.blueiris.BlueIrisBindingConstants;
import org.openhab.binding.blueiris.discovery.BlueIrisDiscoveryService;
import org.openhab.binding.blueiris.handler.BlueIrisBridgeHandler;
import org.openhab.binding.blueiris.handler.BlueIrisCameraHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link BlueIrisHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Bennett - Initial contribution
 */
public class BlueIrisHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(BlueIrisHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .newHashSet(BlueIrisBindingConstants.THING_TYPE_BRIDGE, BlueIrisBindingConstants.THING_TYPE_CAMERA);
    Map<ThingUID, ServiceRegistration<?>> discoveryService = new HashMap<>();

    public BlueIrisHandlerFactory() {
        super();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates the handlers for the blue iris system.
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BlueIrisBindingConstants.THING_TYPE_CAMERA)) {
            return new BlueIrisCameraHandler(thing);
        }
        if (thingTypeUID.equals(BlueIrisBindingConstants.THING_TYPE_BRIDGE)) {
            Bridge bridgeThing = (Bridge) thing;
            BlueIrisBridgeHandler bridge = new BlueIrisBridgeHandler(bridgeThing);
            BlueIrisDiscoveryService service = new BlueIrisDiscoveryService(bridge);
            service.activateBridge();
            // Register the discovery service.
            discoveryService.put(bridge.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), service, new Hashtable<String, Object>()));
            return bridge;
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler handler) {
        if (handler instanceof BlueIrisBridgeHandler) {
            ServiceRegistration<?> reg = discoveryService.get(handler.getThing().getUID());
            if (reg != null) {
                BlueIrisDiscoveryService service = (BlueIrisDiscoveryService) bundleContext
                        .getService(reg.getReference());
                reg.unregister();

                discoveryService.remove(handler.getThing().getUID());
            }
        }
    }
}
