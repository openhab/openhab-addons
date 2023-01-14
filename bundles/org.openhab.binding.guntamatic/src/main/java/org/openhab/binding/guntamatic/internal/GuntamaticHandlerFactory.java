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
package org.openhab.binding.guntamatic.internal;

import static org.openhab.binding.guntamatic.internal.GuntamaticBindingConstants.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link GuntamaticHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Weger Michael - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.guntamatic", service = ThingHandlerFactory.class)
public class GuntamaticHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BIOSTAR, THING_TYPE_BIOSMART,
            THING_TYPE_POWERCHIP, THING_TYPE_POWERCORN, THING_TYPE_BIOCOM, THING_TYPE_PRO, THING_TYPE_THERM,
            THING_TYPE_GENERIC);

    private final HttpClient httpClient;
    private GuntamaticChannelTypeProvider guntamaticChannelTypeProvider;

    @Activate
    public GuntamaticHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference GuntamaticChannelTypeProvider guntamaticChannelTypeProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.guntamaticChannelTypeProvider = guntamaticChannelTypeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        List<String> staticChannelIDs;

        if (THING_TYPE_BIOSTAR.equals(thingTypeUID) || THING_TYPE_POWERCHIP.equals(thingTypeUID)
                || THING_TYPE_POWERCORN.equals(thingTypeUID) || THING_TYPE_BIOCOM.equals(thingTypeUID)
                || THING_TYPE_PRO.equals(thingTypeUID) || THING_TYPE_THERM.equals(thingTypeUID)) {
            staticChannelIDs = STATIC_CHANNEL_IDS;
        } else {
            staticChannelIDs = STATIC_CHANNEL_IDS_WOBOILERAPP;
        }

        if (supportsThingType(thingTypeUID)) {
            return new GuntamaticHandler(thing, httpClient, guntamaticChannelTypeProvider, staticChannelIDs);
        }

        return null;
    }
}
