/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.snapcast.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.snapcast.internal.handler.SnapcastClientHandler;
import org.openhab.binding.snapcast.internal.handler.SnapcastServerHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link SnapcastHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.snapcast", service = ThingHandlerFactory.class)
public class SnapcastHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(SnapcastBindingConstants.THING_TYPE_SERVER, SnapcastBindingConstants.THING_TYPE_CLIENT));

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServices = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SnapcastBindingConstants.THING_TYPE_SERVER.equals(thingTypeUID)) {
            SnapcastServerHandler serverHandler = new SnapcastServerHandler((Bridge) thing);
            activateClientDiscoveryService(serverHandler);
            return serverHandler;
        }

        if (SnapcastBindingConstants.THING_TYPE_CLIENT.equals(thingTypeUID)) {
            return new SnapcastClientHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SnapcastServerHandler) {
            deactivateClientDiscoveryService((SnapcastServerHandler) thingHandler);
        }
        super.removeHandler(thingHandler);
    }

    private synchronized void activateClientDiscoveryService(SnapcastServerHandler serverHandler) {
        ClientDiscoveryService discoveryService = new ClientDiscoveryService(serverHandler);
        discoveryService.activate();

        this.discoveryServices.put(serverHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void deactivateClientDiscoveryService(SnapcastServerHandler serverHandler) {
        ServiceRegistration<?> serviceRegistration = this.discoveryServices.remove(serverHandler.getThing().getUID());
        if (serviceRegistration != null) {
            ClientDiscoveryService discoveryService = (ClientDiscoveryService) bundleContext
                    .getService(serviceRegistration.getReference());

            serviceRegistration.unregister();

            if (discoveryService != null) {
                discoveryService.deactivate();
            }
        }

    }

}
