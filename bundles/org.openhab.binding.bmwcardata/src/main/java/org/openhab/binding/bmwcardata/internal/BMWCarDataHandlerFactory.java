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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
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
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link BMWCarDataHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.bmwcardata", service = { ThingHandlerFactory.class,
        BMWCarDataHandlerFactory.class })
public class BMWCarDataHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BMWCarDataHandlerFactory.class);
    private final HttpClient httpClient;
    private final StorageService storageService;
    private final OAuthFactory oAuthFactory;
    private final Gson gson = new Gson();
    private final Set<BMWCarDataAccountHandler> accountHandlers = ConcurrentHashMap.newKeySet();

    @Activate
    public BMWCarDataHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference StorageService storageService, @Reference OAuthFactory oAuthFactory) {
        this.storageService = storageService;
        this.oAuthFactory = oAuthFactory;
        // a dedicated client is required, as BMW answers with 401 responses without WWW-Authenticate header, which
        // the authentication protocol handler of the common client turns into a failure that hides the error code
        HttpClient client = httpClientFactory.createHttpClient(BINDING_ID);
        try {
            client.start();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            throw new IllegalStateException("Could not start HttpClient", e);
        }
        client.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        this.httpClient = client;
    }

    @Deactivate
    public void deactivate() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Failed to stop HttpClient: {}", e.getMessage());
        }
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
            BMWCarDataAccountHandler handler = new BMWCarDataAccountHandler((Bridge) thing, httpClient, gson, storage,
                    oAuthFactory);
            accountHandlers.add(handler);
            return handler;
        } else if (THING_TYPE_VEHICLE.equals(thingTypeUID)) {
            return new BMWCarDataVehicleHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof BMWCarDataAccountHandler accountHandler) {
            accountHandlers.remove(accountHandler);
        }
    }

    /**
     * @return the handlers of the account Things, for the authorization page
     */
    public Set<BMWCarDataAccountHandler> getAccountHandlers() {
        return Set.copyOf(accountHandlers);
    }
}
