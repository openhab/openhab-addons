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
package org.openhab.binding.plex.internal;

import static org.openhab.binding.plex.internal.PlexBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plex.internal.discovery.PlexDiscoveryService;
import org.openhab.binding.plex.internal.handler.PlexPlayerHandler;
import org.openhab.binding.plex.internal.handler.PlexServerHandler;
import org.openhab.core.config.discovery.DiscoveryService;
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
 * The {@link PlexHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
@Component(configurationPid = "binding.plex", service = ThingHandlerFactory.class)
public class PlexHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpClientFactory;
    private @Nullable ServiceRegistration<?> plexDiscoveryServiceRegistration;

    @Activate
    public PlexHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (SUPPORTED_SERVER_THING_TYPES_UIDS.contains(thingTypeUID)) {
            PlexServerHandler handler = new PlexServerHandler((Bridge) thing, httpClientFactory);
            registerPlexDiscoveryService(handler);
            return handler;
        } else if (SUPPORTED_PLAYER_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new PlexPlayerHandler(thing);
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof PlexServerHandler) {
            ServiceRegistration<?> plexDiscoveryServiceRegistration = this.plexDiscoveryServiceRegistration;
            if (plexDiscoveryServiceRegistration != null) {
                // remove discovery service, if bridge handler is removed
                plexDiscoveryServiceRegistration.unregister();
            }
        }
    }

    private void registerPlexDiscoveryService(PlexServerHandler handler) {
        PlexDiscoveryService discoveryService = new PlexDiscoveryService(handler);
        if (bundleContext != null) {
            this.plexDiscoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<>());
        }
    }
}
