/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal;

import static org.openhab.binding.satel.SatelBindingConstants.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.satel.handler.Ethm1BridgeHandler;
import org.openhab.binding.satel.handler.IntRSBridgeHandler;
import org.openhab.binding.satel.handler.SatelBridgeHandler;
import org.openhab.binding.satel.handler.SatelOutputHandler;
import org.openhab.binding.satel.handler.SatelPartitionHandler;
import org.openhab.binding.satel.handler.SatelShutterHandler;
import org.openhab.binding.satel.handler.SatelSystemHandler;
import org.openhab.binding.satel.handler.SatelZoneHandler;
import org.openhab.binding.satel.internal.config.SatelThingConfig;
import org.openhab.binding.satel.internal.discovery.SatelDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link SatelHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.satel")
public class SatelHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPES_UIDS, DEVICE_THING_TYPES_UIDS, VIRTUAL_THING_TYPES_UIDS)
            .flatMap(uids -> uids.stream()).collect(Collectors.toSet());

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new ConcurrentHashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (thingUID == null) {
            if (DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
                thingUID = getDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            } else if (VIRTUAL_THING_TYPES_UIDS.contains(thingTypeUID)) {
                thingUID = new ThingUID(thingTypeUID, bridgeUID.getId());
            }
        }
        return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Ethm1BridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            SatelBridgeHandler bridgeHandler = new Ethm1BridgeHandler((Bridge) thing);
            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (IntRSBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            SatelBridgeHandler bridgeHandler = new IntRSBridgeHandler((Bridge) thing);
            registerDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (SatelZoneHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelZoneHandler(thing);
        } else if (SatelOutputHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelOutputHandler(thing);
        } else if (SatelPartitionHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelPartitionHandler(thing);
        } else if (SatelShutterHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelShutterHandler(thing);
        } else if (SatelSystemHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new SatelSystemHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        super.removeHandler(thingHandler);

        ServiceRegistration<?> discoveryServiceRegistration = discoveryServiceRegistrations
                .remove(thingHandler.getThing().getUID());
        if (discoveryServiceRegistration != null) {
            discoveryServiceRegistration.unregister();
        }
    }

    private void registerDiscoveryService(SatelBridgeHandler bridgeHandler) {
        SatelDeviceDiscoveryService discoveryService = new SatelDeviceDiscoveryService(bridgeHandler,
                (thingTypeUID) -> getThingTypeByUID(thingTypeUID));
        ServiceRegistration<?> discoveryServiceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), discoveryServiceRegistration);
    }

    private ThingUID getDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String deviceId;
        if (THING_TYPE_SHUTTER.equals(thingTypeUID)) {
            deviceId = String.format("%s-%s", configuration.get(SatelThingConfig.UP_ID),
                    configuration.get(SatelThingConfig.DOWN_ID));
        } else {
            deviceId = String.valueOf(configuration.get(SatelThingConfig.ID));
        }
        return new ThingUID(thingTypeUID, deviceId, bridgeUID.getId());
    }

}
