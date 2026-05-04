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
 * The {@link TelegramHandlerFactory} is responsible for creating things and thing handlers.
 *
 * <p>
 * A dedicated {@link TelegramMessageStore} is created for each {@link TelegramHandler}
 * and scoped to that handler's {@link org.openhab.core.thing.ThingUID}. This guarantees
 * that two Telegram Things backed by different bot tokens can never share or overwrite
 * each other's persisted message/callback IDs even if their chat IDs and reply IDs are
 * identical.
 *
 * @author Jens Runge - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.telegram", service = ThingHandlerFactory.class)
public class TelegramHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(TELEGRAM_THING);

    private final HttpClient httpClient;
    private final StorageService storageService;

    @Activate
    public TelegramHandlerFactory(@Reference final HttpClientFactory httpClientFactory,
            @Reference final StorageService storageService) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.storageService = storageService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (!TELEGRAM_THING.equals(thing.getThingTypeUID())) {
            return null;
        }
        // Each handler gets its own store scoped to the Thing UID so that
        // multiple bots with overlapping chatId/replyId values never collide.
        TelegramMessageStore messageStore = new TelegramMessageStore(storageService, thing.getUID());
        return new TelegramHandler(thing, httpClient, messageStore);
    }
}
