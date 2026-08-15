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
package org.openhab.binding.solaredge.internal;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solaredge.internal.connector.PublicApiV2RequestCounter;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeGenericHandler;
import org.openhab.binding.solaredge.internal.oauth.SolarEdgeOAuthClient;
import org.openhab.binding.solaredge.internal.oauth.SolarEdgeOAuthServlet;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarEdgeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.solaredge")
public class SolarEdgeHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SolarEdgeHandlerFactory.class);

    /**
     * the shared http client
     */
    private final HttpClient httpClient;
    private final StorageService storageService;
    private final SolarEdgeOAuthServlet oAuthServlet;

    @Activate
    public SolarEdgeHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference StorageService storageService, @Reference SolarEdgeOAuthServlet oAuthServlet) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.storageService = storageService;
        this.oAuthServlet = oAuthServlet;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_GENERIC)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            return new SolarEdgeGenericHandler(thing, httpClient, new SolarEdgeOAuthClient(httpClient, storage),
                    new PublicApiV2RequestCounter(storage), oAuthServlet);
        } else {
            logger.warn("Unsupported Thing-Type: {}", thingTypeUID.getAsString());
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SolarEdgeGenericHandler solarEdgeHandler) {
            oAuthServlet.unregister(solarEdgeHandler);
        }
    }
}
