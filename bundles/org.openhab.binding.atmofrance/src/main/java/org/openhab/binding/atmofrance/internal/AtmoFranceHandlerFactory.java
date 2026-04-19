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
package org.openhab.binding.atmofrance.internal;

import static org.openhab.binding.atmofrance.internal.AtmoFranceBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atmofrance.internal.deserialization.AtmoFranceDeserializer;
import org.openhab.binding.atmofrance.internal.handler.AtmoFranceApiHandler;
import org.openhab.binding.atmofrance.internal.handler.CityHandler;
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
 * The {@link AtmoFranceHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.atmofrance", service = ThingHandlerFactory.class)
public class AtmoFranceHandlerFactory extends BaseThingHandlerFactory {

    private final AtmoFranceDeserializer deserializer;
    private final HttpClientFactory httpClientFactory;

    @Activate
    public AtmoFranceHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference AtmoFranceDeserializer deserializer) {
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

        if (THING_TYPE_API.equals(thingTypeUID)) {
            return new AtmoFranceApiHandler((Bridge) thing, httpClientFactory.getCommonHttpClient(), deserializer);
        } else if (THING_TYPE_CITY.equals(thingTypeUID)) {
            return new CityHandler(thing);
        }
        return null;
    }
}
