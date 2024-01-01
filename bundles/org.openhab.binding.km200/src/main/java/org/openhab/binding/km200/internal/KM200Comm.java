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
package org.openhab.binding.km200.internal;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KM200Comm class does the communication to the device and does any encryption/decryption/converting jobs
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200Comm<KM200BindingProvider> {

    private final Logger logger = LoggerFactory.getLogger(KM200Comm.class);
    private final HttpClient httpClient;
    private final KM200Device remoteDevice;
    private Integer maxNbrRepeats;

    public KM200Comm(KM200Device device, HttpClient httpClient) {
        this.remoteDevice = device;
        maxNbrRepeats = Integer.valueOf(10);
        this.httpClient = httpClient;
    }

    /**
     * This function sets the maximum number of repeats.
     */
    public void setMaxNbrRepeats(Integer maxNbrRepeats) {
        this.maxNbrRepeats = maxNbrRepeats;
    }

    /**
     * This function does the GET http communication to the device
     */
    public byte @Nullable [] getDataFromService(String service) {
        byte[] responseBodyB64 = null;
        int statusCode = 0;

        ContentResponse contentResponse = null;
        logger.debug("Starting receive connection...");

        try {
            // Create an instance of HttpClient.
            for (int i = 0; i < maxNbrRepeats.intValue() && statusCode != HttpStatus.OK_200; i++) {
                contentResponse = httpClient.newRequest(remoteDevice.getIP4Address() + service, 80).scheme("http")
                        .agent("TeleHeater/2.2.3").accept("application/json").method(HttpMethod.GET)
                        .timeout(5, TimeUnit.SECONDS).send();
                // Execute the method.
                statusCode = contentResponse.getStatus();

                // Release the connection.
                switch (statusCode) {
                    case HttpStatus.OK_200:
                        remoteDevice.setCharSet(StandardCharsets.UTF_8.name());
                        responseBodyB64 = contentResponse.getContent();
                        break;
                    case HttpStatus.INTERNAL_SERVER_ERROR_500:
                        /* Unknown problem with the device, wait and try again */
                        logger.debug("HTTP GET failed: 500, internal server error, repeating.. ");
                        Thread.sleep(100L * i + 1);
                        continue;
                    case HttpStatus.FORBIDDEN_403:
                        /* Service is available but not readable, return a byte array with a size of 1 as code */
                        byte[] serviceIsProtected = new byte[1];
                        responseBodyB64 = serviceIsProtected;
                        break;
                    case HttpStatus.NOT_FOUND_404:
                        /* Should only happen on discovery */
                        responseBodyB64 = null;
                        break;
                    default:
                        logger.debug("HTTP GET failed: {}", contentResponse.getReason());
                        responseBodyB64 = null;
                        break;
                }
            }
        } catch (InterruptedException e) {
            logger.debug("Sleep was interrupted: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.debug("Call to {} {} timed out", remoteDevice.getIP4Address(), service);
        } catch (ExecutionException e) {
            logger.debug("Fatal transport error: {}", e.getMessage());
        }
        return responseBodyB64;
    }

    /**
     * This function does the SEND http communication to the device
     */
    public Integer sendDataToService(String service, byte[] data) {
        int rCode = 0;
        ContentResponse contentResponse = null;

        logger.debug("Starting send connection...");
        try {
            for (int i = 0; i < maxNbrRepeats.intValue() && rCode != HttpStatus.NO_CONTENT_204; i++) {
                // Create a method instance.
                contentResponse = httpClient.newRequest("http://" + remoteDevice.getIP4Address() + service)
                        .method(HttpMethod.POST).agent("TeleHeater/2.2.3").accept("application/json")
                        .content(new BytesContentProvider(data)).timeout(5, TimeUnit.SECONDS).send();
                rCode = contentResponse.getStatus();
                switch (rCode) {
                    case HttpStatus.NO_CONTENT_204: // The default return value
                        break;
                    case HttpStatus.LOCKED_423:
                        /* Unknown problem with the device, wait and try again */
                        logger.debug("HTTP POST failed: 423, locked, repeating.. ");
                        Thread.sleep(1000L * i + 1);
                        continue;
                    default:
                        logger.debug("HTTP POST failed: {}", contentResponse.getReason());
                        rCode = 0;
                        break;
                }
            }
        } catch (InterruptedException e) {
            logger.debug("Sleep was interrupted: {}", e.getMessage());
        } catch (ExecutionException e) {
            logger.debug("Fatal transport error: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.debug("Call to {} {} timed out.", remoteDevice.getIP4Address(), service);
        }
        return rCode;
    }
}
