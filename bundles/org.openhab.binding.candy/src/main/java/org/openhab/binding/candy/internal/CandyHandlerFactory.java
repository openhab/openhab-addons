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
package org.openhab.binding.candy.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.candy.internal.client.BiancaAccountClient;
import org.openhab.binding.candy.internal.client.BiancaLocalClient;
import org.openhab.binding.candy.internal.handler.BiancaHandler;
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
 * Creates local Bianca handlers using the shared openHAB HTTP client.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.candy")
public class CandyHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClient httpClient;
    private final HttpClientFactory httpClientFactory;
    private final CandyCommandDescriptionProvider commandDescriptions;

    @Activate
    public CandyHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference CandyCommandDescriptionProvider commandDescriptions) {
        this.commandDescriptions = commandDescriptions;
        this.httpClientFactory = httpClientFactory;
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return CandyBindingConstants.THING_TYPE_BIANCA.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        return supportsThingType(thing.getThingTypeUID()) ? new BiancaHandler(thing, new BiancaLocalClient(httpClient),
                () -> new BiancaAccountClient(httpClientFactory.createHttpClient("candy-account")),
                programs -> commandDescriptions.setPrograms(thing.getUID(), programs)) : null;
    }
}
