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
package org.openhab.binding.froniuswattpilot.internal;

import static org.openhab.binding.froniuswattpilot.internal.FroniusWattpilotBindingConstants.*;

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
 * The {@link FroniusWattpilotHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.froniuswattpilot", service = ThingHandlerFactory.class)
public class FroniusWattpilotHandlerFactory extends BaseThingHandlerFactory {
    static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_WATTPILOT);

    private final HttpClient httpClient;

    @Activate
    public FroniusWattpilotHandlerFactory(@Reference final HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.createHttpClient("fronius-wattpilot");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_WATTPILOT.equals(thingTypeUID)) {
            return new FroniusWattpilotHandler(thing, httpClient);
        }

        return null;
    }
}
