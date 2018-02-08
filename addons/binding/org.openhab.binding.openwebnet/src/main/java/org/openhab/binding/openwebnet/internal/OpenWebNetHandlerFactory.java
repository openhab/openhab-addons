/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
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
import org.openhab.binding.openwebnet.handler.OpenWebNetLightingHandler;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Antoine Laydier - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.openwebnet")
public class OpenWebNetHandlerFactory extends BaseThingHandlerFactory {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(OpenWebNetHandlerFactory.class);

    // Needed for Maven only
    @SuppressWarnings("null")
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(OpenWebNetBridgeHandler.SUPPORTED_THING_TYPES, OpenWebNetLightingHandler.SUPPORTED_THING_TYPES,
                    OpenWebNetAutomationHandler.SUPPORTED_THING_TYPES)
            .flatMap(Set::stream).collect(Collectors.toSet());

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    protected void activate(@NonNull ComponentContext componentContext) {
        super.activate(componentContext);
    };

    @Override
    public boolean supportsThingType(@NonNull ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(@NonNull Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (OpenWebNetBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            logger.debug("createHandler for Bridge");
            OpenWebNetBridgeHandler handler = new OpenWebNetBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        } else if (OpenWebNetLightingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new OpenWebNetLightingHandler(thing);

        } else if (OpenWebNetAutomationHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new OpenWebNetAutomationHandler(thing);
        }
        return null;
    }

    private ThingUID getThingUID(@NonNull ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            @NonNull Configuration configuration, @NonNull ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        } else {
            String macAddress = (String) configuration.get(Thing.PROPERTY_MAC_ADDRESS);
            return new ThingUID(thingTypeUID, macAddress, bridgeUID.getId());
        }
    }

    @Override
    public @Nullable Thing createThing(@NonNull ThingTypeUID thingTypeUID, @NonNull Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (OpenWebNetBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        }
        if (OpenWebNetLightingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            if (bridgeUID == null) {
                throw new IllegalArgumentException("The thing type " + thingTypeUID
                        + " is not supported by the OpenWebNet binding without a Bridge.");
            }
            ThingUID zigbeeOWNLightUID = getThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, zigbeeOWNLightUID, bridgeUID);
        }
        if (OpenWebNetAutomationHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            if (bridgeUID == null) {
                throw new IllegalArgumentException("The thing type " + thingTypeUID
                        + " is not supported by the OpenWebNet binding without a Bridge.");
            }
            ThingUID zigbeeOWNAutomationUID = getThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, zigbeeOWNAutomationUID, bridgeUID);
        }

        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the OpenWebNet binding.");
    }

    @Override
    protected synchronized void removeHandler(@NonNull ThingHandler thingHandler) {
        if (thingHandler instanceof OpenWebNetBridgeHandler) {
            @Nullable
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
                logger.debug("DeviceDiscovery for Bridge unregistered");
            }
        }
    }

    /**
     * Adds OpenWebNetBridgeHandler to the discovery service to find OpenWebNet Devices
     *
     * @param OpenWebNetBridgeHandler
     */
    private synchronized void registerDeviceDiscoveryService(@NonNull OpenWebNetBridgeHandler bridgeHandler) {
        logger.debug("registerDeviceDiscoveryService for {}", bridgeHandler);
        OpenWebNetDeviceDiscoveryService discoveryService = new OpenWebNetDeviceDiscoveryService(bridgeHandler);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

}
