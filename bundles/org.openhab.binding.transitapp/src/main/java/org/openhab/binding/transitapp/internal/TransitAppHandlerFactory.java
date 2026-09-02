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
package org.openhab.binding.transitapp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.handler.TransitAppBridgeHandler;
import org.openhab.binding.transitapp.internal.handler.TransitAppRouteDetailsHandler;
import org.openhab.binding.transitapp.internal.handler.TransitAppStopHandler;
import org.openhab.binding.transitapp.internal.handler.TransitAppTripDetailsHandler;
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
 * The {@link TransitAppHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.transitapp")
public class TransitAppHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;

    @Activate
    public TransitAppHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return TransitAppBindingConstants.THING_TYPE_BRIDGE.equals(thingTypeUID)
                || TransitAppBindingConstants.THING_TYPE_STOP.equals(thingTypeUID)
                || TransitAppBindingConstants.THING_TYPE_ROUTE_DETAILS.equals(thingTypeUID)
                || TransitAppBindingConstants.THING_TYPE_TRIP_DETAILS.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(TransitAppBindingConstants.THING_TYPE_BRIDGE)) {
            return new TransitAppBridgeHandler((Bridge) thing, httpClientFactory.getCommonHttpClient());
        } else if (thingTypeUID.equals(TransitAppBindingConstants.THING_TYPE_STOP)) {
            return new TransitAppStopHandler(thing);
        } else if (thingTypeUID.equals(TransitAppBindingConstants.THING_TYPE_ROUTE_DETAILS)) {
            return new TransitAppRouteDetailsHandler(thing);
        } else if (thingTypeUID.equals(TransitAppBindingConstants.THING_TYPE_TRIP_DETAILS)) {
            return new TransitAppTripDetailsHandler(thing);
        }

        return null;
    }
}
