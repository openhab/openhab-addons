/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foobot.internal;

import static org.openhab.binding.foobot.internal.FoobotBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.foobot.internal.discovery.FoobotAccountDiscoveryService;
import org.openhab.binding.foobot.internal.handler.FoobotAccountHandler;
import org.openhab.binding.foobot.internal.handler.FoobotHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link FoobotHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Divya Chauhan - Initial contribution
 * @author George Katsis - Add Bridge thing type
 */

@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.foobot")
@NonNullByDefault
public class FoobotHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(BRIDGE_TYPE_FOOBOTACCOUNT, THING_TYPE_FOOBOT).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS = Collections.singleton(THING_TYPE_FOOBOT);

    private @NonNullByDefault({}) HttpClient httpClient;

    private Map<ThingUID, ServiceRegistration<DiscoveryService>> discoveryServiceRegistrations = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_FOOBOT)) {
            return new FoobotHandler(thing, httpClient);
        } else if (thingTypeUID.equals(BRIDGE_TYPE_FOOBOTACCOUNT)) {
            FoobotAccountHandler handler = new FoobotAccountHandler((Bridge) thing, httpClient);
            registerAccountDiscoveryService(handler);
            return handler;
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        ServiceRegistration<DiscoveryService> serviceRegistration = discoveryServiceRegistrations
                .get(thingHandler.getThing().getUID());

        // remove discovery service, if bridge handler is removed
        FoobotAccountDiscoveryService service = (FoobotAccountDiscoveryService) bundleContext
                .getService(serviceRegistration.getReference());
        serviceRegistration.unregister();
        if (service != null) {
            service.deactivate();
        }
    }

    private void registerAccountDiscoveryService(FoobotAccountHandler handler) {
        FoobotAccountDiscoveryService discoveryService = new FoobotAccountDiscoveryService(handler);

        ServiceRegistration<DiscoveryService> serviceRegistration = this.bundleContext
                .registerService(DiscoveryService.class, discoveryService, null);

        discoveryServiceRegistrations.put(handler.getThing().getUID(), serviceRegistration);
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }
}
