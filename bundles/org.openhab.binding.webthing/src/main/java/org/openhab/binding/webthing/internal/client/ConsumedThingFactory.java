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
package org.openhab.binding.webthing.internal.client;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 * Factory to create new instances of the WebThing client-side proxy
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public interface ConsumedThingFactory {

    /**
     * @param webSocketClient the webSocketClient to use
     * @param httpClient the http client to use
     * @param webThingURI the identifier of a WebThing resource
     * @param executor executor
     * @param errorHandler the error handler
     * @return the newly created WebThing
     * @throws IOException if the WebThing can not be connected
     */
    ConsumedThing create(WebSocketClient webSocketClient, HttpClient httpClient, URI webThingURI,
            ScheduledExecutorService executor, Consumer<String> errorHandler) throws IOException;

    /**
     * @return the default instance of the factory
     */
    static ConsumedThingFactory instance() {
        return ConsumedThingImpl::new;
    }
}
