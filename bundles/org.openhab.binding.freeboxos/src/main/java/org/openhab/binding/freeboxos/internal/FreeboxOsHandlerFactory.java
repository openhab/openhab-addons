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
package org.openhab.binding.freeboxos.internal;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.freeboxos.internal.handler.ApiBridgeHandler;
import org.openhab.binding.freeboxos.internal.handler.HostHandler;
import org.openhab.binding.freeboxos.internal.handler.LandlineHandler;
import org.openhab.binding.freeboxos.internal.handler.PlayerHandler;
import org.openhab.binding.freeboxos.internal.handler.RepeaterHandler;
import org.openhab.binding.freeboxos.internal.handler.RevolutionHandler;
import org.openhab.binding.freeboxos.internal.handler.ServerHandler;
import org.openhab.binding.freeboxos.internal.handler.VmHandler;
import org.openhab.binding.freeboxos.internal.handler.WifiHostHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
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
 * The {@link FreeboxOsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.freeboxos")
public class FreeboxOsHandlerFactory extends BaseThingHandlerFactory {
    private final AudioHTTPServer audioHTTPServer;
    private final NetworkAddressService networkAddressService;
    private final ZoneId zoneId;
    private final HttpClient httpClient;

    @Activate
    public FreeboxOsHandlerFactory(final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference TimeZoneProvider timeZoneProvider, @Reference HttpClientFactory httpClientFactory,
            ComponentContext componentContext) {
        super.activate(componentContext);
        this.audioHTTPServer = audioHTTPServer;
        this.networkAddressService = networkAddressService;
        this.zoneId = timeZoneProvider.getTimeZone();
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(BRIDGE_TYPE_API)) {
            return new ApiBridgeHandler((Bridge) thing, httpClient);
        } else if (thingTypeUID.equals(THING_TYPE_REVOLUTION)) {
            return new RevolutionHandler(thing, zoneId);
        } else if (thingTypeUID.equals(THING_TYPE_DELTA)) {
            return new ServerHandler(thing, zoneId);
        } else if (thingTypeUID.equals(THING_TYPE_PLAYER)) {
            return new PlayerHandler(thing, zoneId, audioHTTPServer, networkAddressService, bundleContext);
        } else if (thingTypeUID.equals(THING_TYPE_HOST)) {
            return new HostHandler(thing, zoneId);
        } else if (thingTypeUID.equals(THING_TYPE_WIFI_HOST)) {
            return new WifiHostHandler(thing, zoneId);
        } else if (thingTypeUID.equals(THING_TYPE_LANDLINE)) {
            return new LandlineHandler(thing, zoneId);
        } else if (thingTypeUID.equals(THING_TYPE_VM)) {
            return new VmHandler(thing, zoneId);
        } else if (thingTypeUID.equals(THING_TYPE_REPEATER)) {
            return new RepeaterHandler(thing, zoneId);
        }
        return null;
    }
}
