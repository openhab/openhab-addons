/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.http;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpClientProvider_VOC} is a *FAKE* HttpClient factory
 * which always serves the same instance (provided by the user).
 * Only the {@link getCommonHttpClient} method is implemented.
 * 
 * @implNote The intent is to allow usage of the full implementation of HTTP methods
 *           implemented by {@link HttpUtil} class, while allowing to override User-Agent header,
 *           which is not possible via the current interface of HttpUtil.
 *           This class was created for the sole purpose of backporting a 'User-Agent' fix (#8554)
 *           to 2.5.x branch, without the need of refactoring VOC binding or openhab-core.
 *           A full/proper change would likely involve changes to HttpClient's interface
 *           ##This should not be merged into OH3##
 *
 * @author Mateusz Bronk - Initial contribution for the purpose of backporting #8554 fix.
 */
@NonNullByDefault
public class HttpClientProvider_VOC implements HttpClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientProvider_VOC.class);
    private final HttpClient httpClient;

    public HttpClientProvider_VOC(final HttpClient theClient) {
        this.httpClient = theClient;
        if (!this.httpClient.isRunning()) {
            try {
                this.httpClient.start();
            } catch (Exception e) {
                logger.error("Could not start Jetty http client", e);
            }
        }
    }

    public void dispose() throws Exception {
        this.httpClient.stop();
    }

    @Override
    public HttpClient createHttpClient(String consumerName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpClient createHttpClient(String consumerName, String endpoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpClient getCommonHttpClient() {
        logger.trace("Using custom HttpClient: {}", this.httpClient);
        return this.httpClient;
    }
}
