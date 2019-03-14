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
package org.openhab.binding.hdl.internal;

import java.math.BigDecimal;
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
import org.openhab.binding.hdl.HdlBindingConstants;
import org.openhab.binding.hdl.internal.discovery.HdlDeviceDiscoveryService;
import org.openhab.binding.hdl.internal.handler.HdlBridgeHandler;
import org.openhab.binding.hdl.internal.handler.HdlHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HdlHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author stigla - Initial contribution
 */
public class HdlHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(HdlHandlerFactory.class);
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (HdlBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            ThingUID hdlBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, hdlBridgeUID, null);// null);
        }

        if (supportsThingType(thingTypeUID)) {
            ThingUID deviceUID = getHdlDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return HdlBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(HdlBindingConstants.PROPERTY_IP);
            thingUID = new ThingUID(thingTypeUID, serialNumber);
        }
        return thingUID;
    }

    private ThingUID getHdlDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        int subNet = ((BigDecimal) configuration.get(HdlBindingConstants.PROPERTY_SUBNET)).intValueExact();
        int deviceID = ((BigDecimal) configuration.get(HdlBindingConstants.PROPERTY_DEVICEID)).intValueExact();

        String serialNumber = Integer.toString(subNet * 1000 + deviceID);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, serialNumber, bridgeUID.getId());
        }
        return thingUID;
    }

    private void registerDeviceDiscoveryService(HdlBridgeHandler HdlBridgeHandler) {
        HdlDeviceDiscoveryService discoveryService = new HdlDeviceDiscoveryService(HdlBridgeHandler);
        discoveryService.activate();

        this.discoveryServiceRegs.put(HdlBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HdlBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                HdlDeviceDiscoveryService service = (HdlDeviceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
        super.removeHandler(thingHandler);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(HdlBindingConstants.THING_TYPE_BRIDGE)) {
            HdlBridgeHandler handler = new HdlBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (supportsThingType(thing.getThingTypeUID())) {
            return new HdlHandler(thing);
        } else {
            logger.debug("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }

}
