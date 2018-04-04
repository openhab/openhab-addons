/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import static org.openhab.binding.plugwise.PlugwiseBindingConstants.*;

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
import org.openhab.binding.plugwise.handler.PlugwiseRelayDeviceHandler;
import org.openhab.binding.plugwise.handler.PlugwiseScanHandler;
import org.openhab.binding.plugwise.handler.PlugwiseSenseHandler;
import org.openhab.binding.plugwise.handler.PlugwiseStickHandler;
import org.openhab.binding.plugwise.handler.PlugwiseSwitchHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * The {@link PlugwiseHandlerFactory} is responsible for creating Plugwise things and thing handlers.
 *
 * @author Wouter Born - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.plugwise", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class PlugwiseHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_STICK)) {
            PlugwiseStickHandler handler = new PlugwiseStickHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_CIRCLE) || thingTypeUID.equals(THING_TYPE_CIRCLE_PLUS)
                || thingTypeUID.equals(THING_TYPE_STEALTH)) {
            return new PlugwiseRelayDeviceHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SCAN)) {
            return new PlugwiseScanHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SENSE)) {
            return new PlugwiseSenseHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new PlugwiseSwitchHandler(thing);
        }

        return null;
    }

    private void registerDiscoveryService(PlugwiseStickHandler handler) {
        PlugwiseThingDiscoveryService discoveryService = new PlugwiseThingDiscoveryService(handler);
        discoveryService.activate();
        this.discoveryServiceRegistrations.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (discoveryServiceRegistrations != null) {
            ServiceRegistration<?> registration = this.discoveryServiceRegistrations
                    .get(thingHandler.getThing().getUID());
            if (registration != null) {
                PlugwiseThingDiscoveryService discoveryService = (PlugwiseThingDiscoveryService) bundleContext
                        .getService(registration.getReference());
                discoveryService.deactivate();
                registration.unregister();
                discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
            }
        }
    }

}
