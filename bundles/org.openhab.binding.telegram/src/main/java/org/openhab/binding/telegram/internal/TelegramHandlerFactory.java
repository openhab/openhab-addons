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
package org.openhab.binding.telegram.internal;

import static org.openhab.binding.telegram.internal.TelegramBindingConstants.TELEGRAM_THING;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link TelegramHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * <p>
 * A single {@link TelegramMessageStore} is created per factory (= per binding
 * instance) and shared across all {@link TelegramHandler}s. This is safe
 * because the store is keyed by {@code chatId + replyId}, which is globally
 * unique within one Telegram bot.
 *
 * @author Jens Runge - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.telegram", service = ThingHandlerFactory.class)
public class TelegramHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(TELEGRAM_THING);

    private final HttpClient httpClient;

    /**
     * Shared persistent store for message IDs and callback IDs.
     *
     * <p>
     * Backed by openHAB's {@link StorageService}, which writes entries to disk
     * (MapDB by default). All handlers that belong to this factory share the
     * same store so that multi-thing setups with the same bot still work
     * correctly.
     */
    private final TelegramMessageStore messageStore;

    @Activate
    public TelegramHandlerFactory(@Reference final HttpClientFactory httpClientFactory,
            @Reference final StorageService storageService) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.messageStore = new TelegramMessageStore(storageService);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (TELEGRAM_THING.equals(thingTypeUID)) {
            return new TelegramHandler(thing, httpClient, messageStore);
        }
        return null;
    }
}
