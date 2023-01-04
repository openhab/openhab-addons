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
package org.openhab.binding.daikin.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to create Jetty web clients
 *
 * Some Daikin controllers communicate via https using a custom common name,
 * and they are accessed using an ip address.
 *
 * The core HttpClientFactory creates a HttpClient that will fail because of this.
 * This factory creates a HttpClient with SslContextFactory(true)
 * which will accept any ssl certificate without checking for common name mismatches.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@Component
@NonNullByDefault
public class DaikinHttpClientFactoryImpl implements DaikinHttpClientFactory {

    private final Logger logger = LoggerFactory.getLogger(DaikinHttpClientFactoryImpl.class);

    private @Nullable HttpClient httpClient;

    @Deactivate
    protected void deactivate() {
        if (httpClient != null) {
            try {
                httpClient.stop();
                logger.debug("Daikin http client stopped");
            } catch (Exception e) {
                logger.debug("error while stopping Daikin http client", e);
            }
            httpClient = null;
        }
    }

    @Override
    public @Nullable HttpClient getHttpClient() {
        initialize();
        return httpClient;
    }

    private synchronized void initialize() {
        if (httpClient == null) {
            httpClient = new HttpClient(new SslContextFactory.Client(true));
            try {
                httpClient.start();
                logger.debug("Daikin http client started");
            } catch (Exception e) {
                logger.warn("Could not start Daikin http client", e);
                httpClient = null;
            }
        }
    }
}
