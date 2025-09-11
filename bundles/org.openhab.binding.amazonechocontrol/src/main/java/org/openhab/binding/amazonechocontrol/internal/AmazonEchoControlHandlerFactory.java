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
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.EchoHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.FlashBriefingProfileHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.binding.amazonechocontrol.internal.util.NonNullListTypeAdapterFactory;
import org.openhab.binding.amazonechocontrol.internal.util.SerializeNullTypeAdapterFactory;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link AmazonEchoControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Geramb - Initial contribution
 */
@Component(service = { ThingHandlerFactory.class,
        AmazonEchoControlHandlerFactory.class }, configurationPid = "binding.amazonechocontrol")
@NonNullByDefault
public class AmazonEchoControlHandlerFactory extends BaseThingHandlerFactory {
    private final Set<AccountHandler> accountHandlers = new HashSet<>();
    private final StorageService storageService;

    private final Gson gson;
    private final HttpClient httpClient;
    private final HTTP2Client http2Client;

    private final AmazonEchoControlStateDescriptionProvider amazonEchoControlStateDescriptionProvider;
    private final AmazonEchoControlCommandDescriptionProvider amazonEchoControlCommandDescriptionProvider;

    @Activate
    public AmazonEchoControlHandlerFactory(@Reference StorageService storageService,
            @Reference AmazonEchoControlStateDescriptionProvider dynamicStateDescriptionProvider,
            @Reference HttpClientFactory httpClientFactory,
            @Reference AmazonEchoControlCommandDescriptionProvider amazonEchoControlCommandDescriptionProvider)
            throws Exception {
        this.storageService = storageService;
        this.gson = new GsonBuilder().registerTypeAdapterFactory(new NonNullListTypeAdapterFactory())
                .registerTypeAdapterFactory(new SerializeNullTypeAdapterFactory()).create();
        this.amazonEchoControlStateDescriptionProvider = dynamicStateDescriptionProvider;
        this.amazonEchoControlCommandDescriptionProvider = amazonEchoControlCommandDescriptionProvider;

        this.httpClient = httpClientFactory.createHttpClient("openhab-aec");
        this.http2Client = httpClientFactory.createHttp2Client("openhab-aec", httpClient.getSslContextFactory());
        http2Client.setConnectTimeout(10000);
        http2Client.setIdleTimeout(-1);

        httpClient.start();
        http2Client.start();
    }

    @Deactivate
    @SuppressWarnings("unused")
    public void deactivate() throws Exception {
        http2Client.stop();
        httpClient.stop();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_ECHO_THING_TYPES_UIDS.contains(thingTypeUID)
                || SUPPORTED_SMART_HOME_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            AccountHandler bridgeHandler = new AccountHandler((Bridge) thing, storage, gson, httpClient, http2Client,
                    amazonEchoControlCommandDescriptionProvider);
            accountHandlers.add(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_FLASH_BRIEFING_PROFILE)) {
            Storage<? super Object> storage = storageService.getStorage(thing.getUID().toString());
            return new FlashBriefingProfileHandler(thing, storage, gson);
        } else if (SUPPORTED_ECHO_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new EchoHandler(thing, gson, amazonEchoControlStateDescriptionProvider);
        } else if (SUPPORTED_SMART_HOME_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new SmartHomeDeviceHandler(thing, gson, amazonEchoControlCommandDescriptionProvider,
                    amazonEchoControlStateDescriptionProvider);
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        amazonEchoControlCommandDescriptionProvider.removeCommandDescriptionForThing(thingHandler.getThing().getUID());
        if (thingHandler instanceof AccountHandler) {
            accountHandlers.remove(thingHandler);
        }
    }

    public Set<AccountHandler> getAccountHandlers() {
        return accountHandlers;
    }
}
