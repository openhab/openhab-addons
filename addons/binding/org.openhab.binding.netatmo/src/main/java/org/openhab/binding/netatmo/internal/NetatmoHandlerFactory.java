/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.netatmo.discovery.NetatmoModuleDiscoveryService;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.handler.station.NAMainHandler;
import org.openhab.binding.netatmo.handler.station.NAStationModuleHandler;
import org.openhab.binding.netatmo.handler.thermostat.NAPlugHandler;
import org.openhab.binding.netatmo.handler.thermostat.NATherm1Handler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NetatmoHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(NetatmoHandlerFactory.class);
    private ServiceRegistration<?> discoveryServiceReg;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID));
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(APIBRIDGE_THING_TYPE)) {
            NetatmoBridgeHandler bridgeHandler = new NetatmoBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(MODULE1_THING_TYPE) || thingTypeUID.equals(MODULE2_THING_TYPE)
                || thingTypeUID.equals(MODULE3_THING_TYPE) || thingTypeUID.equals(MODULE4_THING_TYPE)) {
            return new NAStationModuleHandler(thing);
        } else if (thingTypeUID.equals(MAIN_THING_TYPE)) {
            return new NAMainHandler(thing);
        } else if (thingTypeUID.equals(PLUG_THING_TYPE)) {
            return new NAPlugHandler(thing);
        } else if (thingTypeUID.equals(THERM1_THING_TYPE)) {
            return new NATherm1Handler(thing);
        } else {
            logger.warn("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }

    private void registerDeviceDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler) {
        NetatmoModuleDiscoveryService discoveryService = new NetatmoModuleDiscoveryService(netatmoBridgeHandler);
        discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (discoveryServiceReg != null && thingHandler.getThing().getThingTypeUID().equals(APIBRIDGE_THING_TYPE)) {
            discoveryServiceReg.unregister();
            discoveryServiceReg = null;
        }
        super.removeHandler(thingHandler);
    }

}
