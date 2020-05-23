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
package org.openhab.binding.nest.test;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.discovery.NestDiscoveryService;
import org.openhab.binding.nest.internal.handler.NestBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryService;
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
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * The {@link NestTestHandlerFactory} is responsible for creating test things and thing handlers.
 *
 * @author Wouter Born - Increase test coverage
 */
@NonNullByDefault
public class NestTestHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory {

    public static final String REDIRECT_URL_CONFIG_PROPERTY = "redirect.url";

    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final Map<ThingUID, ServiceRegistration<?>> discoveryService = new HashMap<>();

    private String redirectUrl = "http://localhost";

    @Activate
    public NestTestHandlerFactory(@Reference ClientBuilder clientBuilder,
            @Reference SseEventSourceFactory eventSourceFactory) {
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return NestTestBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Activate
    public void activate(ComponentContext componentContext, Map<String, Object> config) {
        super.activate(componentContext);
        modified(config);
    }

    @Modified
    public void modified(Map<String, Object> config) {
        String url = (String) config.get(REDIRECT_URL_CONFIG_PROPERTY);
        if (url != null) {
            this.redirectUrl = url;
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(NestTestBridgeHandler.THING_TYPE_TEST_BRIDGE)) {
            NestTestBridgeHandler handler = new NestTestBridgeHandler((Bridge) thing, clientBuilder, eventSourceFactory,
                    redirectUrl);
            NestDiscoveryService service = new NestDiscoveryService(handler);
            // Register the discovery service.
            discoveryService.put(handler.getThing().getUID(),
                    bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));

            return handler;
        }
        return null;
    }

    /**
     * Removes the handler for the specific thing. This also handles disabling the discovery
     * service when the bridge is removed.
     */
    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NestBridgeHandler) {
            ServiceRegistration<?> registration = discoveryService.get(thingHandler.getThing().getUID());
            if (registration != null) {
                // Unregister the discovery service.
                NestDiscoveryService service = (NestDiscoveryService) bundleContext
                        .getService(registration.getReference());
                service.deactivate();
                registration.unregister();
                discoveryService.remove(thingHandler.getThing().getUID());
            }
        }
        super.removeHandler(thingHandler);
    }
}
