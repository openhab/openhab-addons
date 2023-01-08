/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.siemenshvac.internal;

import static org.openhab.binding.siemenshvac.SiemensHvacBindingConstants.HVAC_THING_TYPE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.siemenshvac.handler.SiemensHvacHandler;

/**
 * The {@link SiemensHvacHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class SiemensHvacHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(HVAC_THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        SiemensHvacHandler handler;

        handler = new SiemensHvacHandler(thing);

        return handler;

        // return null;
    }

    private synchronized void registerDiscoveryService(
            SiemensHvacHandler bridgeHandler) {/*
                                                * HueLightDiscoveryService discoveryService = new
                                                * HueLightDiscoveryService(bridgeHandler);
                                                * discoveryService.activate();
                                                *
                                                * this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                                                * bundleContext
                                                * .registerService(DiscoveryService.class.getName(), discoveryService,
                                                * new Hashtable<String, Object>()));
                                                */
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        /*
         * if (thingHandler instanceof HueBridgeHandler) {
         * ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
         * if (serviceReg != null) {
         * // remove discovery service, if bridge handler is removed
         * HueLightDiscoveryService service = (HueLightDiscoveryService) bundleContext
         * .getService(serviceReg.getReference());
         * service.deactivate();
         * serviceReg.unregister();
         * discoveryServiceRegs.remove(thingHandler.getThing().getUID());
         * }
         * }
         */
    }

}
