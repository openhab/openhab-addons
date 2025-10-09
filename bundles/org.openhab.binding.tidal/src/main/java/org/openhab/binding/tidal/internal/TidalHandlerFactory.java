/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tidal.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tidal.internal.handler.TidalBridgeHandler;
import org.openhab.binding.tidal.internal.handler.TidalDeviceHandler;
import org.openhab.binding.tidal.internal.handler.TidalDynamicStateDescriptionProvider;
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
 * The {@link TidalHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Arnal - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tidal")
@NonNullByDefault
public class TidalHandlerFactory extends BaseThingHandlerFactory {

    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final TidalAuthService authService;
    private final TidalDynamicStateDescriptionProvider tidalDynamicStateDescriptionProvider;

    @Activate
    public TidalHandlerFactory(@Reference OAuthFactory oAuthFactory,
            @Reference final HttpClientFactory httpClientFactory, @Reference TidalAuthService authService,
            @Reference TidalDynamicStateDescriptionProvider tidalDynamicStateDescriptionProvider) {
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.authService = authService;
        this.tidalDynamicStateDescriptionProvider = tidalDynamicStateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return TidalBindingConstants.THING_TYPE_PLAYER.equals(thingTypeUID)
                || TidalBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (TidalBindingConstants.THING_TYPE_PLAYER.equals(thingTypeUID)) {
            final TidalBridgeHandler handler = new TidalBridgeHandler((Bridge) thing, oAuthFactory, httpClient,
                    tidalDynamicStateDescriptionProvider);
            authService.addTidalAccountHandler(handler);
            return handler;
        }
        if (TidalBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new TidalDeviceHandler(thing);
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof TidalBridgeHandler bridgeHandler) {
            authService.removeTidalAccountHandler(bridgeHandler);
        }
    }
}
