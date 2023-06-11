/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal;

import static org.openhab.binding.semsportal.internal.SEMSPortalBindingConstants.*;

import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.semsportal.internal.discovery.StationDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
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
 * The {@link SEMSPortalHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.semsportal", service = ThingHandlerFactory.class)
public class SEMSPortalHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_STATION, THING_TYPE_PORTAL);
    private HttpClientFactory httpClientFactory;

    @Activate
    public SEMSPortalHandlerFactory(@Reference final HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PORTAL.equals(thingTypeUID)) {
            PortalHandler handler = new PortalHandler((Bridge) thing, httpClientFactory);
            Hashtable<String, Object> dictionary = new Hashtable<>();
            bundleContext.registerService(DiscoveryService.class.getName(), new StationDiscoveryService(handler),
                    dictionary);
            return handler;
        }
        if (THING_TYPE_STATION.equals(thingTypeUID)) {
            return new StationHandler(thing);
        }

        return null;
    }
}
