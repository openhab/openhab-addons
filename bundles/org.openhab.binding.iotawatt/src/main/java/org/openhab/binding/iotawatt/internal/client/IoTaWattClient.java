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
package org.openhab.binding.iotawatt.internal.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.iotawatt.internal.model.StatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Encapsulates the communication with the IoTaWatt device.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class IoTaWattClient {
    private static final String REQUEST_URL = "http://%s/status?state=&inputs=&outputs=";

    private final Logger logger = LoggerFactory.getLogger(IoTaWattClient.class);

    /**
     * The hostname the IoTaWattClient connects to
     */
    public final String hostname;
    private final long requestTimeout;
    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Creates an IoTaWattClient
     * 
     * @param hostname The hostname of the IoTaWatt device to connect to
     * @param httpClient The HttpClient to use
     * @param gson The Gson decoder to use
     */
    public IoTaWattClient(String hostname, long requestTimeout, HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.requestTimeout = requestTimeout;
        this.hostname = hostname;
        this.gson = gson;
    }

    public void start() {
        try {
            httpClient.start();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            logger.warn("Failed to start http client: {}", e.getMessage());
            throw new IllegalStateException("Could not create HttpClient", e);
        }
    }

    public void stop() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.stop()
            logger.warn("Failed to stop http client: {}", e.getMessage());
        }
    }

    /**
     * Fetch the current status from the device.
     * The errors are handled by the caller to update the Thing status accordingly.
     *
     * @throws IoTaWattClientCommunicationException On communication errors
     * @throws IoTaWattClientInterruptedException When sending the request is interrupted
     * @throws IoTaWattClientConfigurationException When the URI is wrong
     * @throws IoTaWattClientException When an unknown error occurs
     * @return The optional StatusResponse fetched from the device
     */
    public Optional<StatusResponse> fetchStatus() throws IoTaWattClientCommunicationException,
            IoTaWattClientInterruptedException, IoTaWattClientException, IoTaWattClientConfigurationException {
        try {
            final URI uri = new URI(String.format(REQUEST_URL, hostname));
            final Request request = httpClient.newRequest(uri).method(HttpMethod.GET).timeout(requestTimeout,
                    TimeUnit.SECONDS);
            final ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new IoTaWattClientCommunicationException("HttpStatus " + response.getStatus());
            }
            final String content = response.getContentAsString();
            @Nullable
            final StatusResponse statusResponse = gson.fromJson(content, StatusResponse.class);
            logger.trace("statusResponse: {}", statusResponse);
            if (statusResponse.inputs() == null) {
                logger.warn("List of inputs in response from IoTaWatt is null on device {}.", hostname);
            }
            if (statusResponse.outputs() == null) {
                logger.warn("List of outputs in response from IoTaWatt is null on device {}.", hostname);
            }
            // noinspection ConstantConditions
            return Optional.ofNullable(statusResponse);
        } catch (InterruptedException e) {
            throw new IoTaWattClientInterruptedException();
        } catch (TimeoutException e) {
            throw new IoTaWattClientCommunicationException();
        } catch (URISyntaxException e) {
            throw new IoTaWattClientConfigurationException(e);
        } catch (ExecutionException e) {
            logger.debug("Error on getting data from IoTaWatt {}", hostname);
            throw new IoTaWattClientException();
        }
    }
}
