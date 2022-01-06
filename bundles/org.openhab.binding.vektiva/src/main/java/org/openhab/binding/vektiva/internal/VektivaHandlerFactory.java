/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.vektiva.internal;

import static org.openhab.binding.vektiva.internal.VektivaBindingConstants.THING_TYPE_SMARWI;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.vektiva.internal.handler.VektivaSmarwiHandler;
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
 * The {@link VektivaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.vektiva", service = ThingHandlerFactory.class)
public class VektivaHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SMARWI);

    /**
     * the shared http client
     */
    private @NonNullByDefault({}) HttpClient httpClient;

    /**
     * the shared web socket client
     */
    private @NonNullByDefault({}) WebSocketClient webSocketClient;

    @Activate
    public VektivaHandlerFactory(@Reference HttpClientFactory httpClientFactory,
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

        if (THING_TYPE_SMARWI.equals(thingTypeUID)) {
            return new VektivaSmarwiHandler(thing, httpClient, webSocketClient);
        }

        return null;
    }
}
