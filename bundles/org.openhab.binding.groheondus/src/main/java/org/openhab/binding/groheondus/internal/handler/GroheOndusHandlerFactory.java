/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.groheondus.internal.handler;

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.THING_TYPE_BRIDGE_ACCOUNT;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.THING_TYPE_SENSE;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.THING_TYPE_SENSEGUARD;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groheondus.internal.discovery.GroheOndusDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
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
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.groheondus", service = ThingHandlerFactory.class)
public class GroheOndusHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private StorageService storageService;
    private int thingCounter = 0;

    @Activate
    public GroheOndusHandlerFactory(@Reference StorageService storageService) {
        this.storageService = storageService;
    }

    private static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Arrays.asList(THING_TYPE_SENSEGUARD,
            THING_TYPE_SENSE, THING_TYPE_BRIDGE_ACCOUNT);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE_ACCOUNT.equals(thingTypeUID)) {
            GroheOndusAccountHandler handler = new GroheOndusAccountHandler((Bridge) thing,
                    storageService.getStorage(thing.getUID().toString(), String.class.getClassLoader()));
            onAccountCreated(thing, handler);
            return handler;
        } else if (THING_TYPE_SENSEGUARD.equals(thingTypeUID)) {
            return new GroheOndusSenseGuardHandler(thing, thingCounter++);
        } else if (THING_TYPE_SENSE.equals(thingTypeUID)) {
            return new GroheOndusSenseHandler(thing, thingCounter++);
        }

        return null;
    }

    private void onAccountCreated(Thing thing, GroheOndusAccountHandler handler) {
        registerDeviceDiscoveryService(handler);
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof GroheOndusAccountHandler) {
            ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }

    private synchronized void registerDeviceDiscoveryService(GroheOndusAccountHandler handler) {
        GroheOndusDiscoveryService discoveryService = new GroheOndusDiscoveryService(handler);
        discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
