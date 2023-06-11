/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.spotify.internal.handler.SpotifyBridgeHandler;
import org.openhab.binding.spotify.internal.handler.SpotifyDeviceHandler;
import org.openhab.binding.spotify.internal.handler.SpotifyDynamicStateDescriptionProvider;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
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

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final SpotifyAuthService authService;
    private final SpotifyDynamicStateDescriptionProvider spotifyDynamicStateDescriptionProvider;

    @Activate
    public SpotifyHandlerFactory(@Reference OAuthFactory oAuthFactory,
            @Reference final HttpClientFactory httpClientFactory, @Reference SpotifyAuthService authService,
            @Reference SpotifyDynamicStateDescriptionProvider spotifyDynamicStateDescriptionProvider) {
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.authService = authService;
        this.spotifyDynamicStateDescriptionProvider = spotifyDynamicStateDescriptionProvider;
    }

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
            return handler;
        }
        if (SpotifyBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new SpotifyDeviceHandler(thing);
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SpotifyBridgeHandler) {
            authService.removeSpotifyAccountHandler((SpotifyBridgeHandler) thingHandler);
        }
    }
}
