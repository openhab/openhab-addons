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
package org.openhab.binding.bmwcardata.internal;

import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bmwcardata.internal.handler.BMWCarDataAccountHandler;
import org.openhab.binding.bmwcardata.internal.handler.BMWCarDataVehicleHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
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

import com.google.gson.Gson;

/**
 * The {@link BMWCarDataHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.bmwcardata", service = ThingHandlerFactory.class)
public class BMWCarDataHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final StorageService storageService;
    private final OAuthFactory oAuthFactory;
    private final Gson gson = new Gson();

    @Activate
    public BMWCarDataHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference StorageService storageService, @Reference OAuthFactory oAuthFactory) {
        this.storageService = storageService;
        this.oAuthFactory = oAuthFactory;
        // BMW's 401 responses lack a WWW-Authenticate header, the clients read the status from the resulting failure
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            // shared by all account things, as BMW counts the requests per client, not per thing
            Storage<String> budgetStorage = storageService.getStorage(BINDING_ID + ".budget",
                    String.class.getClassLoader());
            return new BMWCarDataAccountHandler((Bridge) thing, httpClient, gson, storage, budgetStorage, oAuthFactory);
        } else if (THING_TYPE_VEHICLE.equals(thingTypeUID)) {
            return new BMWCarDataVehicleHandler(thing);
        }

        return null;
    }
}
