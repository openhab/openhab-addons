/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.blink.internal;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.handler.AccountHandler;
import org.openhab.binding.blink.internal.handler.CameraHandler;
import org.openhab.binding.blink.internal.handler.NetworkHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
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
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link BlinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthias Oesterheld - Initial contribution
 * @author Robert T. Brown (-rb) - support Blink Authentication changes in 2025 (OAUTHv2)
 */
@NonNullByDefault
@Component(configurationPid = "binding.blink", service = ThingHandlerFactory.class)
public class BlinkHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BlinkHandlerFactory.class);
    static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_CAMERA,
            THING_TYPE_NETWORK);

    private final Gson gson;
    private final HttpService httpService;
    private final HttpClientFactory httpClientFactory;
    private final NetworkAddressService networkAddressService;
    private final StorageService storageService;

    @Activate
    public BlinkHandlerFactory(@Reference HttpService httpService, @Reference HttpClientFactory httpClientFactory,
            @Reference NetworkAddressService networkAddressService, @Reference StorageService storageService) {
        this.gson = createGson();
        this.httpService = httpService;
        this.httpClientFactory = httpClientFactory;
        this.networkAddressService = networkAddressService;
        this.storageService = storageService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new AccountHandler((Bridge) thing, getBundleContext(), storageService, httpClientFactory, gson);
        } else if (THING_TYPE_CAMERA.equals(thingTypeUID)) {
            return new CameraHandler(thing, httpService, networkAddressService, httpClientFactory, gson);
        } else if (THING_TYPE_NETWORK.equals(thingTypeUID)) {
            try {
                return new NetworkHandler(thing, httpClientFactory, gson);
            } catch (IllegalArgumentException e) {
                logger.error("Error registering thing handler for {}: {}", thing.getUID().getAsString(),
                        e.getMessage());
            }
        } else {
            logger.error("ThingHandler not found for {}", thingTypeUID);
        }
        return null;
    }

    private static Gson createGson() {
        JsonDeserializer<OffsetDateTime> offsetDateTimeJsonDeserializer = (json, type, context) -> OffsetDateTime
                .parse(json.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, offsetDateTimeJsonDeserializer).create();
    }
}
