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
package org.openhab.binding.unifi.internal;

import static org.openhab.binding.unifi.internal.UniFiBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.unifi.internal.handler.UniFiAccessPointThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiClientThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiControllerThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiPoePortThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiSiteThingHandler;
import org.openhab.binding.unifi.internal.handler.UniFiWlanThingHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpClientInitializationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
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
        httpClient = httpClientFactory.createHttpClient(BINDING_ID, new SslContextFactory.Client(true));
        try {
            httpClient.start();
        } catch (final Exception e) {
            throw new HttpClientInitializationException("Could not start HttpClient", e);
        }
    }

    @Override
    protected void deactivate(final ComponentContext componentContext) {
        try {
            httpClient.stop();
        } catch (final Exception e) {
            // Eat http client stop exception.
        } finally {
            super.deactivate(componentContext);
        }
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return ALL_THING_TYPE_SUPPORTED.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_CONTROLLER.equals(thingTypeUID)) {
            return new UniFiControllerThingHandler((Bridge) thing, httpClient);
        } else if (THING_TYPE_SITE.equals(thingTypeUID)) {
            return new UniFiSiteThingHandler(thing);
        } else if (THING_TYPE_WLAN.equals(thingTypeUID)) {
            return new UniFiWlanThingHandler(thing);
        } else if (THING_TYPE_WIRELESS_CLIENT.equals(thingTypeUID) || THING_TYPE_WIRED_CLIENT.equals(thingTypeUID)) {
            return new UniFiClientThingHandler(thing);
        } else if (THING_TYPE_POE_PORT.equals(thingTypeUID)) {
            return new UniFiPoePortThingHandler(thing);
        } else if (THING_TYPE_ACCESS_POINT.equals(thingTypeUID)) {
            return new UniFiAccessPointThingHandler(thing);
        }
        return null;
    }
}
