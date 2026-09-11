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
package org.openhab.binding.gme.internal;

import static org.openhab.binding.gme.internal.GmeBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.gme.internal.handler.GmeApiBridgeHandler;
import org.openhab.binding.gme.internal.handler.GmePunHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
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
 * The {@link GmeHandlerFactory} creates handlers for GME Things.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.gme", service = ThingHandlerFactory.class)
public class GmeHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_API, THING_TYPE_PUN);

    private final HttpClient httpClient;
    private final StorageService storageService;

    @Activate
    public GmeHandlerFactory(@Reference HttpClientFactory httpClientFactory, @Reference StorageService storageService) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.storageService = storageService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_API.equals(thingTypeUID)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            return new GmeApiBridgeHandler((Bridge) thing, httpClient, storage);
        }

        if (THING_TYPE_PUN.equals(thingTypeUID)) {
            return new GmePunHandler(thing);
        }

        return null;
    }
}
