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
package org.openhab.binding.eltako.internal.handler;

import static org.openhab.binding.eltako.internal.misc.EltakoBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.openhab.binding.eltako.internal.discovery.EltakoDeviceDiscoveryService;
import org.openhab.binding.eltako.internal.misc.EltakoBindingConstants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EltakoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Wenske - Initial contribution
 */
@Component(configurationPid = "binding.eltako", service = ThingHandlerFactory.class)
public class EltakoHandlerFactory extends BaseThingHandlerFactory {

    /**
     * Create list of things which are supported by this binding
     */
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.concat(
            Stream.concat(EltakoFam14BridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    EltakoBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS.stream()),
            EltakoFgw14BridgeHandler.SUPPORTED_THING_TYPES.stream()).collect(Collectors.toSet());

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    /*
     * Reference of serialPortManager used to open serial interface and send/receive data
     */
    @Reference
    SerialPortManager serialPortManager;

    /*
     * Logger instance to create log entries
     */
    private Logger logger = LoggerFactory.getLogger(EltakoHandlerFactory.class);

    /**
     * Public getter method to let framework know which things are available for this binding
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * CreateHandler() method is called in case a new thing should be added (this includes bridge things).
     * It need to return the created instance of the thing.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Log event to console
        logger.debug("Create new handler => {}", thingTypeUID);

        if (THING_TYPE_FAM14.equals(thingTypeUID)) {
            // Create new thing of type bridge using serialPortManager instance
            EltakoFam14BridgeHandler bridgeHandler = new EltakoFam14BridgeHandler((Bridge) thing, serialPortManager);
            // Register device discovery service
            EltakoDeviceDiscoveryService service = registerDeviceDiscoveryService(bridgeHandler);
            // Pass service handle to bridge
            bridgeHandler.setServiceHandle(service);
            // Return
            return bridgeHandler;
        }

        if (THING_TYPE_FGW14.equals(thingTypeUID)) {
            // Create new thing of type bridge using serialPortManager instance
            EltakoFgw14BridgeHandler bridgeHandler = new EltakoFgw14BridgeHandler((Bridge) thing, serialPortManager);
            // Return
            return bridgeHandler;
        }

        if (THING_TYPE_FUD14.equals(thingTypeUID)) {
            // Create new thing of type FUD14
            return new EltakoFud14Handler(thing);
        }

        if (THING_TYPE_FSB14.equals(thingTypeUID)) {
            // Create new thing of type FSB14
            return new EltakoFsb14Handler(thing);
        }

        // Log event to console
        logger.warn("Type is not supported => {}", thingTypeUID);
        return null;
    }

    /**
     * This method is called when a thing handler should be removed.
     */
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        // Log event to console
        logger.debug("Remove handler => {}", thingHandler);
        // Check if discovery services are available
        if (this.discoveryServiceRegs != null) {
            // Check if thing has registered discovery services
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // Get service handle
                EltakoDeviceDiscoveryService service = (EltakoDeviceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                // Safety check
                if (service != null) {
                    // Tell discovery service its bridge will be disposed soon
                    service.deactivate();
                }
                // Unregister discovery service (it will be no longer be available in PaperUI)
                serviceReg.unregister();
                // Remove Service
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        } else {
            // Log event to console
            logger.error("Attempt of removing discovery handler {} failed. Handler not available.", thingHandler);
        }
    }

    /**
     * This method is called in order to create and register the discovery service handler.
     */
    private EltakoDeviceDiscoveryService registerDeviceDiscoveryService(EltakoFam14BridgeHandler handler) {
        // Create new instance of Eltako Discovery Service
        EltakoDeviceDiscoveryService discoveryService = new EltakoDeviceDiscoveryService(handler);
        // Tell discovery service it has been added to bridge
        discoveryService.activate();
        // Register discovery service (it will be available in PaperUI)
        this.discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
        // Log event to console
        logger.debug("Discovery service {} has been registered", discoveryService);
        // Return
        return discoveryService;
    }
}
