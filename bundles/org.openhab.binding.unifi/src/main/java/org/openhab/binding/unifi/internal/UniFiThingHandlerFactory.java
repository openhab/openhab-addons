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
package org.openhab.binding.unifi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.unifi.internal.handler.UniFiClientThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiControllerThingHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpClientInitializationException;
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
 * The {@link UniFiThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthew Bowman - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.unifi")
@NonNullByDefault
public class UniFiThingHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;

    @Activate
    public UniFiThingHandlerFactory(@Reference final HttpClientFactory httpClientFactory) {
        // [wip] mgb: disabled due to missing common name attributes with certs
        // this.httpClient = httpClientFactory.getCommonHttpClient();
        httpClient = new HttpClient(new SslContextFactory.Client(true));
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new HttpClientInitializationException("Could not start HttpClient", e);
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return UniFiControllerThingHandler.supportsThingType(thingTypeUID)
                || UniFiClientThingHandler.supportsThingType(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (UniFiControllerThingHandler.supportsThingType(thingTypeUID)) {
            return new UniFiControllerThingHandler((Bridge) thing, httpClient);
        } else if (UniFiClientThingHandler.supportsThingType(thingTypeUID)) {
            return new UniFiClientThingHandler(thing);
        }
        return null;
    }
}
