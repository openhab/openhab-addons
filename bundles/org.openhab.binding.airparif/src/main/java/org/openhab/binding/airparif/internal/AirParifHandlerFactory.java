/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.airparif.internal;

import static org.openhab.binding.airparif.internal.AirParifBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.deserialization.AirParifDeserializer;
import org.openhab.binding.airparif.internal.handler.AirParifBridgeHandler;
import org.openhab.binding.airparif.internal.handler.LocationHandler;
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
 * The {@link AirParifHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.airparif", service = ThingHandlerFactory.class)
public class AirParifHandlerFactory extends BaseThingHandlerFactory {
    private final AirParifDeserializer deserializer;
    private final HttpClientFactory httpClientFactory;

    @Activate
    public AirParifHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference AirParifDeserializer deserializer) {
        this.httpClientFactory = httpClientFactory;
        this.deserializer = deserializer;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return APIBRIDGE_THING_TYPE.equals(thingTypeUID)
                ? new AirParifBridgeHandler((Bridge) thing, httpClientFactory.getCommonHttpClient(), deserializer)
                : LOCATION_THING_TYPE.equals(thingTypeUID) ? new LocationHandler(thing) : null;
    }
}
