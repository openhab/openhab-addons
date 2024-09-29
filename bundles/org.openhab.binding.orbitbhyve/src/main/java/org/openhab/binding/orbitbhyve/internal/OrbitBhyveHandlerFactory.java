/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.orbitbhyve.internal;

import static org.openhab.binding.orbitbhyve.internal.OrbitBhyveBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.orbitbhyve.internal.OrbitBhyveBindingConstants.THING_TYPE_SPRINKLER;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.orbitbhyve.internal.handler.OrbitBhyveBridgeHandler;
import org.openhab.binding.orbitbhyve.internal.handler.OrbitBhyveSprinklerHandler;
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
 * The {@link OrbitBhyveHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.orbitbhyve", service = ThingHandlerFactory.class)
public class OrbitBhyveHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_SPRINKLER);

    /**
     * the shared http client
     */
    private HttpClient httpClient;

    /**
     * the shared web socket client
     */
    private WebSocketClient webSocketClient;

    @Activate
    public OrbitBhyveHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference WebSocketFactory webSocketFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new OrbitBhyveBridgeHandler((Bridge) thing, httpClient, webSocketClient);
        }
        if (THING_TYPE_SPRINKLER.equals(thingTypeUID)) {
            return new OrbitBhyveSprinklerHandler(thing);
        }
        return null;
    }
}
