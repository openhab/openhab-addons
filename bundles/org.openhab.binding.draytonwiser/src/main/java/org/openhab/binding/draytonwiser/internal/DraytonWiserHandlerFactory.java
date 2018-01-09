/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.internal;

import java.util.HashMap;
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
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.handler.ControllerHandler;
import org.openhab.binding.draytonwiser.handler.HeatHubHandler;
import org.openhab.binding.draytonwiser.handler.RoomHandler;
import org.openhab.binding.draytonwiser.handler.RoomStatHandler;
import org.openhab.binding.draytonwiser.handler.TRVHandler;
import org.openhab.binding.draytonwiser.internal.discovery.DraytonWiserDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link DraytonWiserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andrew Schofield - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.draytonwiser")
@NonNullByDefault
public class DraytonWiserHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = DraytonWiserBindingConstants.SUPPORTED_THING_TYPES_UIDS;

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DraytonWiserBindingConstants.THING_TYPE_BRIDGE)) {
            HeatHubHandler handler = new HeatHubHandler((Bridge) thing);
            registerApplianceDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(DraytonWiserBindingConstants.THING_TYPE_ROOM)) {
            return new RoomHandler(thing);
        } else if (thingTypeUID.equals(DraytonWiserBindingConstants.THING_TYPE_ROOMSTAT)) {
            return new RoomStatHandler(thing);
        } else if (thingTypeUID.equals(DraytonWiserBindingConstants.THING_TYPE_ITRV)) {
            return new TRVHandler(thing);
        } else if (thingTypeUID.equals(DraytonWiserBindingConstants.THING_TYPE_CONTROLLER)) {
            return new ControllerHandler(thing);
        }

        return null;
    }

    private synchronized void registerApplianceDiscoveryService(HeatHubHandler bridgeHandler) {
        DraytonWiserDiscoveryService discoveryService = new DraytonWiserDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HeatHubHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                DraytonWiserDiscoveryService service = (DraytonWiserDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
