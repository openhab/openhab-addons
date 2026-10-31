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
package org.openhab.binding.volvocars.internal;

import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.volvocars.internal.handler.AccountHandler;
import org.openhab.binding.volvocars.internal.handler.VehicleHandler;
import org.openhab.binding.volvocars.internal.servlet.VolvoCarsAuthServlet;
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

/**
 * The {@link VolvoCarsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.volvocars", service = ThingHandlerFactory.class)
public class VolvoCarsHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final Storage<String> storage;
    private final VolvoCarsAuthServlet authServlet;
    private final OAuthFactory oAuthFactory;

    @Activate
    public VolvoCarsHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference StorageService storageService, @Reference VolvoCarsAuthServlet authServlet,
            @Reference OAuthFactory oAuthFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.storage = storageService.getStorage(BINDING_ID, String.class.getClassLoader());
        this.authServlet = authServlet;
        this.oAuthFactory = oAuthFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new AccountHandler((Bridge) thing, httpClient, storage, authServlet, oAuthFactory);
        } else if (THING_TYPE_VEHICLE.equals(thingTypeUID)) {
            return new VehicleHandler(thing);
        }

        return null;
    }
}
