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
package org.openhab.binding.daikin.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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

    private HttpClient httpClient;

    @Activate
    public DaikinHttpClientFactoryImpl(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.createHttpClient(DaikinBindingConstants.BINDING_ID,
                new SslContextFactory.Client(true));
        try {
            httpClient.start();
            logger.debug("Daikin http client started");
        } catch (Exception e) {
            logger.warn("Could not start Daikin http client", e);
        }
    }

    @Deactivate
    protected void deactivate() {
        try {
            httpClient.stop();
            logger.debug("Daikin http client stopped");
        } catch (Exception e) {
            logger.debug("error while stopping Daikin http client", e);
        }
    }

    @Override
    public @Nullable HttpClient getHttpClient() {
        return httpClient;
    }
}
