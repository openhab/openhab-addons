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
package org.openhab.binding.freeboxos.internal;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.handler.ActivePlayerHandler;
import org.openhab.binding.freeboxos.internal.handler.FreeboxOsHandler;
import org.openhab.binding.freeboxos.internal.handler.HostHandler;
import org.openhab.binding.freeboxos.internal.handler.LandlineHandler;
import org.openhab.binding.freeboxos.internal.handler.PlayerHandler;
import org.openhab.binding.freeboxos.internal.handler.RepeaterHandler;
import org.openhab.binding.freeboxos.internal.handler.RevolutionHandler;
import org.openhab.binding.freeboxos.internal.handler.ServerHandler;
import org.openhab.binding.freeboxos.internal.handler.VmHandler;
import org.openhab.binding.freeboxos.internal.handler.WifiHostHandler;
import org.openhab.core.audio.AudioHTTPServer;
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
    private final ApiHandler apiHandler;
    private final @Nullable String ipAddress;

    @Activate
    public FreeboxOsHandlerFactory(final @Reference AudioHTTPServer audioHTTPServer,
            final @Reference NetworkAddressService networkAddressService, final @Reference ApiHandler apiHandler,
            ComponentContext componentContext) {
        super.activate(componentContext);
        this.audioHTTPServer = audioHTTPServer;
        this.apiHandler = apiHandler;

        ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        // Validate bean
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(BRIDGE_TYPE_API)) {
            return new FreeboxOsHandler((Bridge) thing, new FreeboxOsSession(apiHandler));
        } else if (thingTypeUID.equals(THING_TYPE_REVOLUTION)) {
            return new RevolutionHandler(thing, audioHTTPServer, ipAddress, bundleContext);
        } else if (thingTypeUID.equals(THING_TYPE_DELTA)) {
            return new ServerHandler(thing, audioHTTPServer, ipAddress, bundleContext);
        } else if (thingTypeUID.equals(THING_TYPE_ACTIVE_PLAYER)) {
            return new ActivePlayerHandler(thing, audioHTTPServer, ipAddress, bundleContext);
        } else if (thingTypeUID.equals(THING_TYPE_PLAYER)) {
            return new PlayerHandler(thing, audioHTTPServer, ipAddress, bundleContext);
        } else if (thingTypeUID.equals(THING_TYPE_HOST)) {
            return new HostHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WIFI_HOST)) {
            return new WifiHostHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_LANDLINE)) {
            return new LandlineHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_VM)) {
            return new VmHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_REPEATER)) {
            return new RepeaterHandler(thing);
        }
        return null;
    }
}
