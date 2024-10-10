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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.discovery.InsteonDiscoveryService;
import org.openhab.binding.insteon.internal.discovery.InsteonLegacyDiscoveryService;
import org.openhab.binding.insteon.internal.handler.InsteonBridgeHandler;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonLegacyDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonLegacyNetworkHandler;
import org.openhab.binding.insteon.internal.handler.InsteonSceneHandler;
import org.openhab.binding.insteon.internal.handler.X10DeviceHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
import org.openhab.core.thing.ThingRegistry;
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

    private final SerialPortManager serialPortManager;
    private final InsteonStateDescriptionProvider stateDescriptionProvider;
    private final StorageService storageService;
    private final ThingManager thingManager;
    private final ThingRegistry thingRegistry;
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Activate
    public InsteonHandlerFactory(final @Reference SerialPortManager serialPortManager,
            final @Reference InsteonStateDescriptionProvider stateDescriptionProvider,
            final @Reference StorageService storageService, final @Reference ThingManager thingManager,
            final @Reference ThingRegistry thingRegistry) {
        this.serialPortManager = serialPortManager;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.storageService = storageService;
        this.thingManager = thingManager;
        this.thingRegistry = thingRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HUB1.equals(thingTypeUID) || THING_TYPE_HUB2.equals(thingTypeUID)
                || THING_TYPE_PLM.equals(thingTypeUID)) {
            InsteonBridgeHandler handler = new InsteonBridgeHandler((Bridge) thing, serialPortManager, storageService,
                    thingRegistry);
            InsteonDiscoveryService service = new InsteonDiscoveryService(handler);
            registerDiscoveryService(handler, service);
            return handler;
        } else if (THING_TYPE_LEGACY_NETWORK.equals(thingTypeUID)) {
            InsteonLegacyNetworkHandler handler = new InsteonLegacyNetworkHandler((Bridge) thing, serialPortManager,
                    thingManager, thingRegistry);
            InsteonLegacyDiscoveryService service = new InsteonLegacyDiscoveryService(handler);
            registerDiscoveryService(handler, service);
            return handler;
        } else if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new InsteonDeviceHandler(thing, stateDescriptionProvider);
        } else if (THING_TYPE_LEGACY_DEVICE.equals(thingTypeUID)) {
            return new InsteonLegacyDeviceHandler(thing);
        } else if (THING_TYPE_SCENE.equals(thingTypeUID)) {
            return new InsteonSceneHandler(thing);
        } else if (THING_TYPE_X10.equals(thingTypeUID)) {
            return new X10DeviceHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler handler) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(handler.getThing().getUID());
        if (serviceReg != null) {
            serviceReg.unregister();
        }
    }

    private synchronized void registerDiscoveryService(ThingHandler handler, DiscoveryService service) {
        discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));
    }
}
