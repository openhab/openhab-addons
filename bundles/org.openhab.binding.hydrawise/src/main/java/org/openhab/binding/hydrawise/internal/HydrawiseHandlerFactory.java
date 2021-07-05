/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal;

import static org.openhab.binding.hydrawise.internal.HydrawiseBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hydrawise.internal.discovery.HydrawiseCloudControllerDiscoveryService;
import org.openhab.binding.hydrawise.internal.handler.HydrawiseAccountHandler;
import org.openhab.binding.hydrawise.internal.handler.HydrawiseControllerHandler;
import org.openhab.binding.hydrawise.internal.handler.HydrawiseLocalHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HydrawiseHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.hydrawise", service = ThingHandlerFactory.class)
public class HydrawiseHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_ACCOUNT, THING_TYPE_CONTROLLER, THING_TYPE_LOCAL).collect(Collectors.toSet());

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) OAuthFactory oAuthFactory;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            HydrawiseAccountHandler handler = new HydrawiseAccountHandler((Bridge) thing, httpClient, oAuthFactory);
            registerDiscoveryService(handler);
            return handler;
        }

        if (THING_TYPE_CONTROLLER.equals(thingTypeUID)) {
            return new HydrawiseControllerHandler(thing);
        }

        if (THING_TYPE_LOCAL.equals(thingTypeUID)) {
            return new HydrawiseLocalHandler(thing, httpClient);
        }

        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

    @Reference
    protected void setOAuthFactory(OAuthFactory oAuthFactory) {
        this.oAuthFactory = oAuthFactory;
    }

    protected void unsetOAuthFactory(OAuthFactory oAuthFactory) {
        this.oAuthFactory = null;
    }

    private synchronized void registerDiscoveryService(HydrawiseAccountHandler handler) {
        HydrawiseCloudControllerDiscoveryService discoveryService = new HydrawiseCloudControllerDiscoveryService(
                handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler handler) {
        if (handler instanceof HydrawiseAccountHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(handler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                HydrawiseCloudControllerDiscoveryService service = (HydrawiseCloudControllerDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.deactivate();
                }
            }
        }
    }
}
