/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifi.network.internal;

import static org.openhab.binding.unifi.network.internal.UniFiBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.network.internal.handler.UniFiAccessPointThingHandler;
import org.openhab.binding.unifi.network.internal.handler.UniFiClientThingHandler;
import org.openhab.binding.unifi.network.internal.handler.UniFiNetworkThingHandler;
import org.openhab.binding.unifi.network.internal.handler.UniFiPoePortThingHandler;
import org.openhab.binding.unifi.network.internal.handler.UniFiSiteThingHandler;
import org.openhab.binding.unifi.network.internal.handler.UniFiWlanThingHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link UniFiThingHandlerFactory} creates handlers for UniFi Network child things. The controller bridge
 * itself is owned by the shared UniFi parent binding ({@code org.openhab.binding.unifi}), so this factory does
 * not handle {@code unifi:controller}; Network child things attach to the parent bridge and receive a shared
 * authenticated session.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Dan Cunningham - Refactored onto shared parent bridge session
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.unifi.network")
@NonNullByDefault
public class UniFiThingHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return THING_TYPE_SUPPORTED.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_SITE.equals(thingTypeUID)) {
            return new UniFiSiteThingHandler(thing);
        } else if (THING_TYPE_NETWORK.equals(thingTypeUID)) {
            return new UniFiNetworkThingHandler(thing);
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
