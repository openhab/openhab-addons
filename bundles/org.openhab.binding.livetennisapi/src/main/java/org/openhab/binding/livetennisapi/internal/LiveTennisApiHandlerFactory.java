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
package org.openhab.binding.livetennisapi.internal;

import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.SUPPORTED_THING_TYPES;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.THING_TYPE_ACCOUNT;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.THING_TYPE_PLAYER;
import static org.openhab.binding.livetennisapi.internal.LiveTennisApiBindingConstants.THING_TYPE_TOURNAMENT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.livetennisapi.internal.handler.LiveTennisApiAccountHandler;
import org.openhab.binding.livetennisapi.internal.handler.LiveTennisApiPlayerHandler;
import org.openhab.binding.livetennisapi.internal.handler.LiveTennisApiTournamentHandler;
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
 * The {@link LiveTennisApiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ben - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.livetennisapi", service = ThingHandlerFactory.class)
public class LiveTennisApiHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;

    @Activate
    public LiveTennisApiHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID) && thing instanceof Bridge bridge) {
            return new LiveTennisApiAccountHandler(bridge, httpClientFactory.getCommonHttpClient());
        }
        if (THING_TYPE_PLAYER.equals(thingTypeUID)) {
            return new LiveTennisApiPlayerHandler(thing);
        }
        if (THING_TYPE_TOURNAMENT.equals(thingTypeUID)) {
            return new LiveTennisApiTournamentHandler(thing);
        }
        return null;
    }
}
