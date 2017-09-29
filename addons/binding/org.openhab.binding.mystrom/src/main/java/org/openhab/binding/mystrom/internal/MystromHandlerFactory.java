/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom.internal;

import static org.openhab.binding.mystrom.MystromBindingConstants.*;

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
import org.openhab.binding.mystrom.discovery.MystromDiscoveryService;
import org.openhab.binding.mystrom.handler.MystromBridgeHandler;
import org.openhab.binding.mystrom.handler.MystromWifiSwitchHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link MystromHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stéphane Raemy - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.mystrom")
public class MystromHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(MystromHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_BRIDGE,
            THING_TYPE_WIFISWITCH);

    Map<ThingUID, ServiceRegistration<?>> discoveryService = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates a handler for the specific thing. THis also creates the discovery service
     * when the bridge is created.
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_WIFISWITCH)) {
            logger.debug("Create Mystrom Wifi Switch Device Handler");
            return new MystromWifiSwitchHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            logger.debug("Create Mystrom Bridge Handler");
            MystromBridgeHandler handler = new MystromBridgeHandler((Bridge) thing);
            MystromDiscoveryService service = new MystromDiscoveryService(handler);
            service.activate();
            // Register the discovery service.
            discoveryService.put(handler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), service, new Hashtable<String, Object>()));
            return handler;
        }

        return null;
    }

    /**
     * Removes the handler for the specific thing. This also handles disableing the discovery
     * service when the bridge is removed.
     */
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof MystromBridgeHandler) {
            ServiceRegistration<?> reg = discoveryService.get(thingHandler.getThing().getUID());
            if (reg != null) {
                // Unregister the discovery service.
                MystromDiscoveryService service = (MystromDiscoveryService) bundleContext
                        .getService(reg.getReference());
                service.deactivate();
                reg.unregister();
                discoveryService.remove(thingHandler.getThing().getUID());
            }
        }
        super.removeHandler(thingHandler);
    }
}
