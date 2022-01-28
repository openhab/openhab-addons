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
package org.openhab.binding.guntamatic.internal;

import static org.openhab.binding.guntamatic.internal.GuntamaticBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
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

    private List<String> staticChannelIDs;

    @Activate
    public GuntamaticHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference GuntamaticChannelTypeProvider guntamaticChannelTypeProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.guntamaticChannelTypeProvider = guntamaticChannelTypeProvider;
        this.staticChannelIDs = new ArrayList<>(Arrays.asList(CHANNEL_CONTROLPROGRAM, CHANNEL_CONTROLHEATCIRCPROGRAM0,
                CHANNEL_CONTROLHEATCIRCPROGRAM1, CHANNEL_CONTROLHEATCIRCPROGRAM2, CHANNEL_CONTROLHEATCIRCPROGRAM3,
                CHANNEL_CONTROLHEATCIRCPROGRAM4, CHANNEL_CONTROLHEATCIRCPROGRAM5, CHANNEL_CONTROLHEATCIRCPROGRAM6,
                CHANNEL_CONTROLHEATCIRCPROGRAM7, CHANNEL_CONTROLHEATCIRCPROGRAM8, CHANNEL_CONTROLWWHEAT0,
                CHANNEL_CONTROLWWHEAT1, CHANNEL_CONTROLWWHEAT2, CHANNEL_CONTROLEXTRAWWHEAT0,
                CHANNEL_CONTROLEXTRAWWHEAT1, CHANNEL_CONTROLEXTRAWWHEAT2));
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if ((thingTypeUID == THING_TYPE_BIOSTAR) || (thingTypeUID == THING_TYPE_POWERCHIP)
                || (thingTypeUID == THING_TYPE_POWERCORN) || (thingTypeUID == THING_TYPE_BIOCOM)
                || (thingTypeUID == THING_TYPE_PRO) || (thingTypeUID == THING_TYPE_THERM)) {
            staticChannelIDs.add(CHANNEL_CONTROLBOILERAPPROVAL);
        }

        if (supportsThingType(thingTypeUID)) {
            return new GuntamaticHandler(thing, httpClient, guntamaticChannelTypeProvider, staticChannelIDs);
        }

        return null;
    }
}
