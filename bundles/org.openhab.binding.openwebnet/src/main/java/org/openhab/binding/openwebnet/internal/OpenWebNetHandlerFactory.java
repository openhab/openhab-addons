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
package org.openhab.binding.openwebnet.internal;

import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.ALL_SUPPORTED_THING_TYPES;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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
import org.openhab.binding.openwebnet.handler.OpenWebNetAutomationHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetBridgeHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetGenericHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetLightingHandler;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.openwebnet", service = ThingHandlerFactory.class)
public class OpenWebNetHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ALL_SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("==OWN:HandlerFactory== createHandler()");
        if (OpenWebNetBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("==OWN:HandlerFactory== creating NEW BRIDGE Handler");
            OpenWebNetBridgeHandler handler = new OpenWebNetBridgeHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (OpenWebNetGenericHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("==OWN:HandlerFactory== creating NEW GENERIC Handler");
            return new OpenWebNetGenericHandler(thing);
        } else if (OpenWebNetLightingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("==OWN:HandlerFactory== creating NEW LIGHTING Handler");
            return new OpenWebNetLightingHandler(thing);
        } else if (OpenWebNetAutomationHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("==OWN:HandlerFactory== creating NEW AUTOMATION Handler");
            return new OpenWebNetAutomationHandler(thing);
        }
        logger.warn("==OWN:HandlerFactory== ThingType: {} is not supported by this binding", thing.getThingTypeUID());
        return null;
    }

    private void registerDiscoveryService(OpenWebNetBridgeHandler bridgeHandler) {
        logger.debug("==OWN:HandlerFactory== registerDiscoveryService()");
        OpenWebNetDeviceDiscoveryService deviceDiscoveryService = new OpenWebNetDeviceDiscoveryService(bridgeHandler);
        bridgeHandler.deviceDiscoveryService = deviceDiscoveryService;
        deviceDiscoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext.registerService(
                DiscoveryService.class.getName(), deviceDiscoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof OpenWebNetBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                OpenWebNetDeviceDiscoveryService service = (OpenWebNetDeviceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
