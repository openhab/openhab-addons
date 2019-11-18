/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.THING_TYPE_SAMPLE;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoschSHCHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.boschshc", service = ThingHandlerFactory.class)
public class BoschSHCHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BoschSHCHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SAMPLE);
    private @Nullable HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SAMPLE.equals(thingTypeUID)) {

            logger.warn("Thing createHandler - http client is: {}", this.httpClient);

            // TODO Make this an asynchronous request

            // Instantiate and configure the SslContextFactory
            // SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
            SslContextFactory sslContextFactory = new SslContextFactory(true); // Accept all certificates

            // Keystore for managing the keys that have been used to pair with the SHC
            // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
            sslContextFactory.setKeyStorePath("/home/skaestle/projects/smart-home/bosch/keystore");
            sslContextFactory.setKeyStorePassword("123456");

            // Bosch is using a self signed certificate
            sslContextFactory.setTrustAll(true);
            sslContextFactory.setValidateCerts(false);
            sslContextFactory.setValidatePeerCerts(false);
            sslContextFactory.setEndpointIdentificationAlgorithm(null);

            // Instantiate HttpClient with the SslContextFactory
            HttpClient httpClient = new HttpClient(sslContextFactory);

            logger.warn("Starting http client");
            try {
                httpClient.start();
            } catch (Exception e) {
                logger.warn("Failed to start http client", e);
            }
            logger.warn("Sucessfully started http client");

            /*
             * byte[] bytes = ...;
             * ContentResponse response = httpClient.newRequest("http://domain.com/upload")
             * .method(HttpMethod.POST)
             * .content(new BytesContentProvider(bytes), "text/plain")
             * .send();
             *
             * .timeout(10000, TimeUnit.MILLISECONDS)
             */

            ContentResponse contentResponse;
            try {
                logger.warn("Sending http request to Bosch to request rooms");
                contentResponse = httpClient.newRequest("https://192.168.178.128:8444/smarthome/rooms")
                        .header("Content-Type", "application/json").header("Accept", "application/json").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.warn("Response complete: {} - return code: {}", content, contentResponse.getStatus());

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }

            try {
                logger.warn("Sending http request to Bosch to request clients");
                contentResponse = httpClient.newRequest("https://192.168.178.128:8444/smarthome/devices")
                        .header("Content-Type", "application/json").header("Accept", "application/json").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.warn("Response complete: {} - return code: {}", content, contentResponse.getStatus());

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }

            return new BoschSHCHandler(thing);
        }

        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        logger.warn("Setting http client");
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        logger.warn("Unsetting http client");
        this.httpClient = null;
    }

}
