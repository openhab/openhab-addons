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
package org.openhab.binding.ntfy.internal;

import static org.openhab.binding.ntfy.internal.NtfyBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
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
 * The {@link NtfyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ntfy", service = ThingHandlerFactory.class)
public class NtfyHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(NTFY_CONNECTION_THING, NTFY_TOPIC_THING);
    private HttpClient httpClient;
    private WebSocketFactory webSocketFactory;

    /**
     * OSGi activation constructor. Initializes the handler factory with the
     * shared HTTP client and a WebSocketFactory used to create WebSocket clients.
     *
     * @param httpClientFactory factory providing a common {@link HttpClient}
     * @param webSocketFactory factory used to create {@link org.eclipse.jetty.websocket.client.WebSocketClient}
     */
    @Activate
    public NtfyHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference WebSocketFactory webSocketFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (NTFY_CONNECTION_THING.equals(thingTypeUID)) {
            return new NtfyConnectionHandler((Bridge) thing, webSocketFactory);
        }

        if (NTFY_TOPIC_THING.equals(thingTypeUID)) {
            return new NtfyTopicHandler(thing, httpClient);
        }

        return null;
    }
}
