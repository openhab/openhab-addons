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
package org.openhab.binding.bloomsky.internal.factory;

import static org.openhab.binding.bloomsky.internal.BloomSkyBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bloomsky.internal.discovery.BloomSkyDiscoveryService;
import org.openhab.binding.bloomsky.internal.handler.BloomSkyBridgeHandler;
import org.openhab.binding.bloomsky.internal.handler.BloomSkySKYHandler;
import org.openhab.binding.bloomsky.internal.handler.BloomSkyStormHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BloomSkyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dave J Schoepel - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.bloomsky", service = ThingHandlerFactory.class)
public class BloomSkyHandlerFactory extends BaseThingHandlerFactory {

    private @Nullable ServiceRegistration<?> bloomSkyDiscoveryServiceRegistration;
    private final TimeZoneProvider timeZoneProvider;
    protected final @NonNullByDefault({}) HttpClient httpClient;

    @Activate
    public BloomSkyHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            BloomSkyBridgeHandler handler = new BloomSkyBridgeHandler((Bridge) thing, httpClient);
            // register the discovery service
            registerBloomSkyDiscoveryService(handler);
            return handler;
        }

        if (THING_TYPE_SKY.equals(thingTypeUID)) {
            return new BloomSkySKYHandler(thing, httpClient, timeZoneProvider);
        }

        if (THING_TYPE_STORM.equals(thingTypeUID)) {
            return new BloomSkyStormHandler(thing, httpClient, timeZoneProvider);
        }
        return null;
    }

    private void registerBloomSkyDiscoveryService(BloomSkyBridgeHandler handler) {
        BloomSkyDiscoveryService discoveryService = new BloomSkyDiscoveryService(handler);
        this.bloomSkyDiscoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<>());
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof BloomSkyBridgeHandler) {
            if (bloomSkyDiscoveryServiceRegistration != null) {
                // // remove discovery service, if bridge handler is removed
                bloomSkyDiscoveryServiceRegistration.unregister();

            }
        }
    }
}
