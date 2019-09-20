/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.openhab.binding.surepetcare.internal.discovery.SurePetcareDiscoveryService;
import org.openhab.binding.surepetcare.internal.handler.SurePetcareBridgeHandler;
import org.openhab.binding.surepetcare.internal.handler.SurePetcareDeviceHandler;
import org.openhab.binding.surepetcare.internal.handler.SurePetcareHouseholdHandler;
import org.openhab.binding.surepetcare.internal.handler.SurePetcarePetHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.surepetcare")
@NonNullByDefault
public class SurePetcareHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SurePetcareHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private SurePetcareAPIHelper petcareAPI = new SurePetcareAPIHelper();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS, SurePetcareConstants.SUPPORTED_THING_TYPES_UIDS).flatMap(x -> x.stream())
            .collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.debug("supportsThingType - check for thing type: {}", thingTypeUID);
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("createHandler - create handler for {}", thing.toString());
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_HOUSEHOLD)) {
            return new SurePetcareHouseholdHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_HUB_DEVICE)) {
            return new SurePetcareDeviceHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_FLAP_DEVICE)) {
            return new SurePetcareDeviceHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_PET)) {
            return new SurePetcarePetHandler(thing, petcareAPI);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            SurePetcareBridgeHandler handler = new SurePetcareBridgeHandler((Bridge) thing, petcareAPI);
            registerDiscoveryService(handler.getThing().getUID());
            return handler;
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SurePetcareBridgeHandler) {
            unregisterDiscoveryService(thingHandler.getThing().getUID());
        }
    }

    private synchronized void registerDiscoveryService(ThingUID bridgeUID) {
        SurePetcareDiscoveryService discoveryService = new SurePetcareDiscoveryService(bridgeUID, petcareAPI);
        discoveryService.activate(null);
        discoveryServiceRegs.put(bridgeUID, bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<String, Object>()));
    }

    private synchronized void unregisterDiscoveryService(ThingUID bridgeUID) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(bridgeUID);
        SurePetcareDiscoveryService service = (SurePetcareDiscoveryService) bundleContext
                .getService(serviceReg.getReference());
        serviceReg.unregister();
        if (service != null) {
            service.deactivate();
        }
    }
}
