/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zway.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zway.internal.discovery.ZWayDeviceDiscoveryService;
import org.openhab.binding.zway.internal.handler.ZWayBridgeHandler;
import org.openhab.binding.zway.internal.handler.ZWayZAutomationDeviceHandler;
import org.openhab.binding.zway.internal.handler.ZWayZWaveDeviceHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link ZWayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Hecker - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.zway")
public class ZWayHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(ZWayBridgeHandler.SUPPORTED_THING_TYPE, ZWayZAutomationDeviceHandler.SUPPORTED_THING_TYPE,
                    ZWayZWaveDeviceHandler.SUPPORTED_THING_TYPE).collect(Collectors.toSet()));

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (ZWayBridgeHandler.SUPPORTED_THING_TYPE.equals(thing.getThingTypeUID())) {
            ZWayBridgeHandler handler = new ZWayBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);

            return handler;
        } else if (ZWayZAutomationDeviceHandler.SUPPORTED_THING_TYPE.equals(thing.getThingTypeUID())) {
            return new ZWayZAutomationDeviceHandler(thing);
        } else if (ZWayZWaveDeviceHandler.SUPPORTED_THING_TYPE.equals(thing.getThingTypeUID())) {
            return new ZWayZWaveDeviceHandler(thing);
        } else {
            return null;
        }
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ZWayBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }

    private synchronized void registerDeviceDiscoveryService(ZWayBridgeHandler handler) {
        ZWayDeviceDiscoveryService discoveryService = new ZWayDeviceDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
