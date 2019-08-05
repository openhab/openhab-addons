/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.spotify.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.spotify.internal.discovery.SpotifyDeviceDiscoveryService;
import org.openhab.binding.spotify.internal.handler.SpotifyBridgeHandler;
import org.openhab.binding.spotify.internal.handler.SpotifyDeviceHandler;
import org.openhab.binding.spotify.internal.handler.SpotifyDynamicStateDescriptionProvider;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SpotifyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Matthew Bowman - Initial contribution
 * @author Hilbrand Bouwkamp - Added registration of discovery service to binding to this class
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.spotify")
@NonNullByDefault
public class SpotifyHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private @NonNullByDefault({}) OAuthFactory oAuthFactory;
    private @NonNullByDefault({}) SpotifyAuthService authService;
    private @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) SpotifyDynamicStateDescriptionProvider spotifyDynamicStateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SpotifyBindingConstants.THING_TYPE_PLAYER.equals(thingTypeUID)
                || SpotifyBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SpotifyBindingConstants.THING_TYPE_PLAYER.equals(thingTypeUID)) {
            final SpotifyBridgeHandler handler = new SpotifyBridgeHandler((Bridge) thing, oAuthFactory, httpClient,
                    spotifyDynamicStateDescriptionProvider);
            authService.addSpotifyAccountHandler(handler);
            registerDiscoveryService(handler);
            return handler;
        }
        if (SpotifyBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new SpotifyDeviceHandler(thing);
        }
        return null;
    }

    /**
     * Registers Spotify Device discovery service to the bridge handler.
     *
     * @param handler handler to register service for
     */
    private synchronized void registerDiscoveryService(SpotifyBridgeHandler handler) {
        final SpotifyDeviceDiscoveryService discoveryService = new SpotifyDeviceDiscoveryService(handler, httpClient);
        final ServiceRegistration<?> serviceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>());

        discoveryService.activate();
        discoveryServiceRegs.put(handler.getThing().getUID(), serviceRegistration);
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SpotifyBridgeHandler) {
            final ThingUID uid = thingHandler.getThing().getUID();
            final ServiceRegistration<?> serviceReg = discoveryServiceRegs.get(uid);

            if (serviceReg != null) {
                final SpotifyDeviceDiscoveryService service = (SpotifyDeviceDiscoveryService) getBundleContext()
                        .getService(serviceReg.getReference());
                // remove discovery service, if bridge handler is removed
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(uid);
                authService.removeSpotifyAccountHandler((SpotifyBridgeHandler) thingHandler);
            }
        }
    }

    @Reference
    protected void setOAuthFactory(OAuthFactory oAuthFactory) {
        this.oAuthFactory = oAuthFactory;
    }

    protected void unsetOAuthFactory(OAuthFactory oAuthFactory) {
        this.oAuthFactory = null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
    }

    @Reference
    protected void setAuthService(SpotifyAuthService service) {
        this.authService = service;
    }

    protected void unsetAuthService(SpotifyAuthService service) {
        this.authService = null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(SpotifyDynamicStateDescriptionProvider provider) {
        this.spotifyDynamicStateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(SpotifyDynamicStateDescriptionProvider provider) {
        this.spotifyDynamicStateDescriptionProvider = null;
    }
}
