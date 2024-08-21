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
package org.openhab.binding.onebusaway.internal;

import static org.openhab.binding.onebusaway.internal.OneBusAwayBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.onebusaway.internal.handler.ApiHandler;
import org.openhab.binding.onebusaway.internal.handler.RouteHandler;
import org.openhab.binding.onebusaway.internal.handler.StopHandler;
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
 * The {@link OneBusAwayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Shawn Wilsher - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.onebusaway")
@NonNullByDefault
public class OneBusAwayHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(ApiHandler.SUPPORTED_THING_TYPE, RouteHandler.SUPPORTED_THING_TYPE, StopHandler.SUPPORTED_THING_TYPE)
            .collect(Collectors.toSet()));

    private final HttpClient httpClient;

    @Activate
    public OneBusAwayHandlerFactory(@Reference final HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_API)) {
            return new ApiHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROUTE)) {
            return new RouteHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_STOP)) {
            return new StopHandler((Bridge) thing, httpClient);
        }

        return null;
    }
}
