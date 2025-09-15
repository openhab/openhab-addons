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
package org.openhab.binding.homekit.internal.factory;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.discovery.HomekitChildDiscoveryService;
import org.openhab.binding.homekit.internal.handler.HomekitBridgeHandler;
import org.openhab.binding.homekit.internal.handler.HomekitDeviceHandler;
import org.openhab.binding.homekit.internal.persistance.HomekitTypeProvider;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates things and thing handlers. Supports HomeKit bridges and accessories.
 * Passes on a {@link HomekitChildDiscoveryService} so that created things can to manage discovery of accessories.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class)
public class HomekitHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_DEVICE);

    private final HttpClientFactory httpClientFactory;
    private final HomekitTypeProvider typeProvider;

    private @Nullable ServiceRegistration<?> discoveryServiceRegistration;
    private @Nullable HomekitChildDiscoveryService discoveryService;

    @Activate
    public HomekitHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference HomekitTypeProvider typeProvider) {
        this.httpClientFactory = httpClientFactory;
        this.typeProvider = typeProvider;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        unregisterDiscoveryService();
        super.deactivate(componentContext);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new HomekitBridgeHandler((Bridge) thing, httpClientFactory, registerDiscoveryService());
        } else if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new HomekitDeviceHandler(thing, httpClientFactory, typeProvider);
        }
        return null;
    }

    /**
     * Registers the AccessoryDiscoveryService if not already registered and returns it.
     *
     * @return the registered AccessoryDiscoveryService
     */
    private HomekitChildDiscoveryService registerDiscoveryService() {
        HomekitChildDiscoveryService service = this.discoveryService;
        if (service == null) {
            service = new HomekitChildDiscoveryService();
            this.discoveryService = service;
        }
        ServiceRegistration<?> registration = this.discoveryServiceRegistration;
        if (registration == null) {
            registration = bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<>());
            this.discoveryServiceRegistration = registration;
        }
        return service;
    }

    /**
     * Unregisters the AccessoryDiscoveryService if it is registered.
     */
    private void unregisterDiscoveryService() {
        ServiceRegistration<?> registration = this.discoveryServiceRegistration;
        if (registration != null) {
            registration.unregister();
        }
        this.discoveryService = null;
        this.discoveryServiceRegistration = null;
    }
}
