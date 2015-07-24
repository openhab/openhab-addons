/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.netatmo.NetatmoBindingConstants;
import org.openhab.binding.netatmo.discovery.NetatmoModuleDiscoveryService;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
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
        return (NetatmoBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID));
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(NetatmoBindingConstants.APIBRIDGE_THING_TYPE)) {
            NetatmoBridgeHandler handler = new NetatmoBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (thing.getThingTypeUID().equals(NetatmoBindingConstants.MODULE1_THING_TYPE)) {
            return new NetatmoModuleHandler(thing);
        } else if (thing.getThingTypeUID().equals(NetatmoBindingConstants.MODULE3_THING_TYPE)) {
            return new NetatmoModuleHandler(thing);
        } else if (thing.getThingTypeUID().equals(NetatmoBindingConstants.MODULE4_THING_TYPE)) {
            return new NetatmoModuleHandler(thing);
        } else if (thing.getThingTypeUID().equals(NetatmoBindingConstants.MAIN_THING_TYPE)) {
            return new NetatmoDeviceHandler(thing);
        } else {
            logger.debug("ThingHandler not found for {}", thing.getThingTypeUID());
            return null;
        }
    }

    private void registerDeviceDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler) {
        NetatmoModuleDiscoveryService discoveryService = new NetatmoModuleDiscoveryService(netatmoBridgeHandler);
        this.discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (this.discoveryServiceReg != null) {
            discoveryServiceReg.unregister();
            discoveryServiceReg = null;
        }
        super.removeHandler(thingHandler);
    }

}
