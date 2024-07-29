/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;
import static org.openhab.binding.insteon.internal.InsteonBindingLegacyConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.discovery.InsteonDeviceLegacyDiscoveryService;
import org.openhab.binding.insteon.internal.discovery.InsteonDiscoveryService;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonLegacyDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonNetworkHandler;
import org.openhab.binding.insteon.internal.handler.InsteonSceneHandler;
import org.openhab.binding.insteon.internal.handler.X10DeviceHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link InsteonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
@Component(configurationPid = "binding.insteon", service = ThingHandlerFactory.class)
public class InsteonHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Map<ThingUID, ServiceRegistration<?>> serviceRegs = new HashMap<>();

    private final SerialPortManager serialPortManager;
    private final InsteonStateDescriptionProvider stateDescriptionProvider;
    private final StorageService storageService;

    @Activate
    public InsteonHandlerFactory(final @Reference SerialPortManager serialPortManager,
            final @Reference InsteonStateDescriptionProvider stateDescriptionProvider,
            final @Reference StorageService storageService) {
        this.serialPortManager = serialPortManager;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.storageService = storageService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        Set<ThingTypeUID> supportedThings = Stream.concat(InsteonBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream(),
                InsteonBindingLegacyConstants.SUPPORTED_THING_TYPES_UIDS.stream()).collect(Collectors.toSet());
        return supportedThings.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (NETWORK_THING_TYPE.equals(thingTypeUID)) {
            InsteonNetworkHandler insteonNetworkHandler = new InsteonNetworkHandler((Bridge) thing, serialPortManager);
            registerServices(insteonNetworkHandler);

            return insteonNetworkHandler;
        } else if (DEVICE_THING_TYPE.equals(thingTypeUID)) {
            return new InsteonLegacyDeviceHandler(thing);
        } else if (THING_TYPE_HUB1.equals(thingTypeUID) || THING_TYPE_HUB2.equals(thingTypeUID)
                || THING_TYPE_PLM.equals(thingTypeUID)) {
            InsteonBridgeHandler handler = new InsteonBridgeHandler((Bridge) thing, serialPortManager, storageService);
            registerDiscoveryService(handler);
            return handler;
        } else if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new InsteonDeviceHandler(thing, stateDescriptionProvider);
        } else if (THING_TYPE_SCENE.equals(thingTypeUID)) {
            return new InsteonSceneHandler(thing);
        } else if (THING_TYPE_X10.equals(thingTypeUID)) {
            return new X10DeviceHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof InsteonNetworkHandler) {
            ThingUID uid = thingHandler.getThing().getUID();
            ServiceRegistration<?> serviceRegs = this.serviceRegs.remove(uid);
            if (serviceRegs != null) {
                serviceRegs.unregister();
            }

            ServiceRegistration<?> discoveryServiceRegs = this.discoveryServiceRegs.remove(uid);
            if (discoveryServiceRegs != null) {
                discoveryServiceRegs.unregister();
            }
        } else if (thingHandler instanceof InsteonBridgeHandler handler) {
            unregisterDiscoveryService(handler);
        }
    }

    private synchronized void registerDiscoveryService(InsteonBridgeHandler handler) {
        InsteonDiscoveryService service = new InsteonDiscoveryService(handler);
        discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));
    }

    private synchronized void unregisterDiscoveryService(InsteonBridgeHandler handler) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(handler.getThing().getUID());
        if (serviceReg != null) {
            InsteonDiscoveryService service = (InsteonDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            if (service != null) {
                service.deactivate();
            }
            serviceReg.unregister();
        }
    }

    private synchronized void registerServices(InsteonNetworkHandler handler) {
        this.serviceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(InsteonNetworkHandler.class.getName(), handler, new Hashtable<>()));

        InsteonDeviceLegacyDiscoveryService discoveryService = new InsteonDeviceLegacyDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
