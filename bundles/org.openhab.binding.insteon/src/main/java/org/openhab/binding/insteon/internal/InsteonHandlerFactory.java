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
package org.openhab.binding.insteon.internal;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.discovery.InsteonDeviceDiscoveryService;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.handler.InsteonNetworkHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link InsteonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.insteon", service = ThingHandlerFactory.class)
public class InsteonHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(DEVICE_THING_TYPE, NETWORK_THING_TYPE).collect(Collectors.toSet()));

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final Map<ThingUID, ServiceRegistration<?>> serviceRegs = new HashMap<>();

    private @Nullable SerialPortManager serialPortManager;

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (NETWORK_THING_TYPE.equals(thingTypeUID)) {
            InsteonNetworkHandler insteonNetworkHandler = new InsteonNetworkHandler((Bridge) thing, serialPortManager);
            registerServices(insteonNetworkHandler);

            return insteonNetworkHandler;
        } else if (DEVICE_THING_TYPE.equals(thingTypeUID)) {
            return new InsteonDeviceHandler(thing);
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
        }
    }

    private synchronized void registerServices(InsteonNetworkHandler handler) {
        this.serviceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(InsteonNetworkHandler.class.getName(), handler, new Hashtable<>()));

        InsteonDeviceDiscoveryService discoveryService = new InsteonDeviceDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
