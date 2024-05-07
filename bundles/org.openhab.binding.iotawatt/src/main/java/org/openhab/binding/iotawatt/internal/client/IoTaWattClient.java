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
import java.util.Objects;
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
import org.openhab.binding.iotawatt.internal.exception.ThingStatusOfflineException;
import org.openhab.binding.iotawatt.internal.model.StatusResponse;
import org.openhab.core.thing.ThingStatusDetail;
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
    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Creates an IoTaWattClient
     * 
     * @param hostname The hostname of the IoTaWatt device to connect to
     * @param httpClient The HttpClient to use
     * @param gson The Gson decoder to use
     */
    public IoTaWattClient(String hostname, HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.hostname = hostname;
        this.gson = gson;
    }

    /**
     * Fetch the current status from the device.
     * The errors are handled by the caller to update the Thing status accordingly.
     * 
     * @throws ThingStatusOfflineException Thrown when ThingStatus needs to be set to offline
     * @return The optional StatusResponse fetched from the device
     */
    public Optional<StatusResponse> fetchStatus() throws ThingStatusOfflineException {
        try {
            final URI uri = new URI(String.format(REQUEST_URL, hostname));
            final Request request = httpClient.newRequest(uri).method(HttpMethod.GET).timeout(10, TimeUnit.SECONDS);
            final ContentResponse response = request.send();
            final String content = response.getContentAsString();
            @Nullable
            final StatusResponse statusResponse = gson.fromJson(content, StatusResponse.class);
            logger.trace("statusResponse: {}", statusResponse);
            if (statusResponse.inputs() == null) {
                logger.error("List of inputs in response from IoTaWatt is null on device {}.", hostname);
            }
            if (statusResponse.outputs() == null) {
                logger.error("List of outputs in response from IoTaWatt is null on device {}.", hostname);
            }
            // noinspection ConstantConditions
            return Optional.ofNullable(statusResponse);
        } catch (InterruptedException e) {
            throw new ThingStatusOfflineException(ThingStatusDetail.NOT_YET_READY);
        } catch (TimeoutException e) {
            throw new ThingStatusOfflineException(ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (URISyntaxException e) {
            throw new ThingStatusOfflineException(ThingStatusDetail.CONFIGURATION_ERROR, getErrorMessage(e));
        } catch (ExecutionException e) {
            logger.debug("Error on getting data from IoTaWatt {}", hostname);
            throw new ThingStatusOfflineException(ThingStatusDetail.NONE, getErrorMessage(e));
        }
    }

    @Nullable
    private String getErrorMessage(Throwable t) {
        final Throwable cause = t.getCause();
        return Objects.requireNonNullElse(cause, t).getMessage();
    }
}
