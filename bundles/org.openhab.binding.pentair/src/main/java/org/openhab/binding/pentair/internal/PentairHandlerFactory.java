/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import java.util.HashMap;
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
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.pentair.internal.handler.PentairBaseBridgeHandler;
import org.openhab.binding.pentair.internal.handler.PentairControllerHandler;
import org.openhab.binding.pentair.internal.handler.PentairIPBridgeHandler;
import org.openhab.binding.pentair.internal.handler.PentairIntelliChlorHandler;
import org.openhab.binding.pentair.internal.handler.PentairIntelliFloHandler;
import org.openhab.binding.pentair.internal.handler.PentairSerialBridgeHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jeff James - Initial contribution
 */

@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.pentair")
public class PentairHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(PentairHandlerFactory.class);
    private final SerialPortManager serialPortManager;

    @Activate
    public PentairHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        // Obtain the serial port manager service using an OSGi reference
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegMap = new HashMap<>();
    // Marked as Nullable only to fix incorrect redundant null check complaints from null annotations

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(IP_BRIDGE_THING_TYPE)) {
            PentairIPBridgeHandler bridgeHandler = new PentairIPBridgeHandler((Bridge) thing);

            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(SERIAL_BRIDGE_THING_TYPE)) {
            PentairSerialBridgeHandler bridgeHandler = new PentairSerialBridgeHandler((Bridge) thing,
                    serialPortManager);

            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(CONTROLLER_THING_TYPE)) {
            return new PentairControllerHandler(thing);
        } else if (thingTypeUID.equals(INTELLIFLO_THING_TYPE)) {
            return new PentairIntelliFloHandler(thing);
        } else if (thingTypeUID.equals(INTELLICHLOR_THING_TYPE)) {
            return new PentairIntelliChlorHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof PentairBaseBridgeHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegMap.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                logger.debug("Unregistering discovery service.");
                serviceReg.unregister();
            }
        }
    }

    /**
     * Register a discovery service for a bridge handler.
     *
     * @param bridgeHandler bridge handler for which to register the discovery service
     */
    private synchronized void registerDiscoveryService(PentairBaseBridgeHandler bridgeHandler) {
        logger.debug("Registering discovery service.");
        PentairDiscoveryService discoveryService = new PentairDiscoveryService(bridgeHandler);
        bridgeHandler.setDiscoveryService(discoveryService);
        discoveryServiceRegMap.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, null));
    }
}
