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
package org.openhab.binding.max.internal.factory;

import static org.openhab.binding.max.internal.MaxBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.max.internal.discovery.MaxDeviceDiscoveryService;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.openhab.binding.max.internal.handler.MaxDevicesHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.max")
public class MaxCubeHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(MaxCubeHandlerFactory.class);
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (CUBEBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID cubeBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, cubeBridgeUID, null);
        }
        if (supportsThingType(thingTypeUID)) {
            ThingUID deviceUID = getMaxCubeDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(Thing.PROPERTY_SERIAL_NUMBER);
            return new ThingUID(thingTypeUID, serialNumber);
        }
        return thingUID;
    }

    private ThingUID getMaxCubeDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String serialNumber = (String) configuration.get(Thing.PROPERTY_SERIAL_NUMBER);

        if (thingUID == null) {
            return new ThingUID(thingTypeUID, serialNumber, bridgeUID.getId());
        }
        return thingUID;
    }

    private synchronized void registerDeviceDiscoveryService(MaxCubeBridgeHandler maxCubeBridgeHandler) {
        MaxDeviceDiscoveryService discoveryService = new MaxDeviceDiscoveryService(maxCubeBridgeHandler);
        discoveryService.activate();

        this.discoveryServiceRegs.put(maxCubeBridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof MaxCubeBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                MaxDeviceDiscoveryService service = (MaxDeviceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.deactivate();
                }
            }
        }
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (CUBEBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            MaxCubeBridgeHandler handler = new MaxCubeBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new MaxDevicesHandler(thing);
        } else {
            logger.debug("ThingHandler not found for {}", thingTypeUID);
            return null;
        }
    }

}
