/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.autoblind.internal;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.autoblind.internal.discovery.AutoBlindDiscoveryService;
import org.openhab.binding.autoblind.internal.handler.AutoBlindHubHandler;
import org.openhab.binding.autoblind.internal.handler.AutoBlindShadeHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates thing handlers for AutoBlind hub and shade things.
 *
 * @author Stephen Berg - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.autoblind")
public class AutoBlindHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new ConcurrentHashMap<>();

    @Activate
    public AutoBlindHandlerFactory(@Reference HttpClientFactory httpClientFactory, ComponentContext componentContext) {
        super.activate(componentContext);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return AutoBlindBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (AutoBlindBindingConstants.THING_TYPE_HUB.equals(thingTypeUID)) {
            AutoBlindHubHandler handler = new AutoBlindHubHandler((Bridge) thing,
                    httpClientFactory.getCommonHttpClient());
            ServiceRegistration<?> registration = bundleContext.registerService(DiscoveryService.class.getName(),
                    new AutoBlindDiscoveryService(handler), new Hashtable<>());
            discoveryServiceRegistrations.put(thing.getUID(), registration);
            return handler;
        } else if (AutoBlindBindingConstants.THING_TYPE_SHADE.equals(thingTypeUID)) {
            return new AutoBlindShadeHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        ServiceRegistration<?> registration = discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
        if (registration != null) {
            registration.unregister();
        }
        super.removeHandler(thingHandler);
    }
}
