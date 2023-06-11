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
package org.openhab.binding.webthing.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link WebThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.webthing", service = ThingHandlerFactory.class)
public class WebThingHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClient httpClient;
    private final WebSocketClient webSocketClient;

    @Activate
    public WebThingHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference WebSocketFactory webSocketFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return WebThingBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        return new WebThingHandler(thing, httpClient, webSocketClient);
    }
}
