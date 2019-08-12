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
package org.openhab.binding.hpprinter.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.hpprinter.internal.api.HPServerResult.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link HPEmbeddedWebServerClient} is responsible for handling reading of data from the HP Embedded Web Server.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPWebServerClient {
    
    private final Logger logger = LoggerFactory.getLogger(HPWebServerClient.class);

    private @Nullable HttpClient httpClient;
    private String serverAddress = "";

    enum WebInterfaceType {
        INVALID,
        STANDARD,
        SECURESOCKETS
    }

    /**
     * Creates a new HP Web Server Client object.
     * @param httpClient {HttpClient} The HttpClient to use for HTTP requests.
     * @param address The address for the Embedded Web Server.
     * @param ssl {Boolean} Use SSL (HTTPS) connections.
     */
    public HPWebServerClient(@Nullable HttpClient httpClient, String address) {
        this.httpClient = httpClient;

        serverAddress = "http://" + address;

        logger.debug("Create printer connection {}", serverAddress);
    }

    /**
     * Gets the Status information from the Embedded Web Server.
     * @return The status information.
     */
    public HPServerResult<HPStatus> getStatus() {
        try {
            String endpoint = serverAddress + HPStatus.ENDPOINT;
            logger.trace("HTTP Client Status GET {}", endpoint);
            ContentResponse cr = this.httpClient.GET(endpoint);
            logger.trace("HTTP Client Status Result {} Size {}", cr.getStatus(), cr.getContentAsString().length());

            return new HPServerResult<HPStatus>(new HPStatus(new InputSource(new ByteArrayInputStream(cr.getContent()))));
        } catch (TimeoutException ex) {
            logger.trace("HTTP Client Status Timeout Exception {}", ex.getMessage());
            return new HPServerResult<HPStatus>(RequestStatus.TIMEOUT, new HPStatus());
        } catch (InterruptedException | ExecutionException | ParserConfigurationException | SAXException | IOException ex) {
            logger.trace("HTTP Client Status Exception {}", ex.getMessage());
            return new HPServerResult<HPStatus>(RequestStatus.ERROR, new HPStatus());
        }
    }

    public HPServerResult<HPType> getType() {
        try {
            String endpoint = serverAddress + HPType.ENDPOINT;
            logger.trace("HTTP Client Type GET {}", endpoint);
            ContentResponse cr = this.httpClient.GET(endpoint);
            logger.trace("HTTP Client Type Result {} Size {}", cr.getStatus(), cr.getContentAsString().length());

            return new HPServerResult<HPType>(new HPType(new InputSource(new ByteArrayInputStream(cr.getContent()))));
        } catch (TimeoutException ex) {
            logger.trace("HTTP Client Type Timeout Exception {}", ex.getMessage());
            return new HPServerResult<HPType>(RequestStatus.TIMEOUT, new HPType());
        } catch (InterruptedException | ExecutionException | ParserConfigurationException | SAXException | IOException ex) {
            logger.trace("HTTP Client Type Exception {}", ex.getMessage());
            return new HPServerResult<HPType>(RequestStatus.ERROR, new HPType());
        }
    }

    /**
     * Gets the Usage information from the Embedded Web Server.
     * @return The usage information.
     */
    public HPServerResult<HPUsage> getUsage() {
        try {
            String endpoint = serverAddress + HPUsage.ENDPOINT;
            logger.trace("HTTP Client Usage GET {}", endpoint);
            ContentResponse cr = this.httpClient.GET(endpoint);
            logger.trace("HTTP Client Usage Result {} Size {}", cr.getStatus(), cr.getContentAsString().length());

            return new HPServerResult<HPUsage>(new HPUsage(new InputSource(new ByteArrayInputStream(cr.getContent()))));
        } catch (TimeoutException ex) {
            logger.trace("HTTP Client Usage Timeout Exception {}", ex.getMessage());
            return new HPServerResult<HPUsage>(RequestStatus.TIMEOUT, new HPUsage());
        } catch (InterruptedException | ExecutionException | ParserConfigurationException | SAXException | IOException ex) {
            logger.trace("HTTP Client Usage Exception {}", ex.getMessage());
            return new HPServerResult<HPUsage>(RequestStatus.ERROR, new HPUsage());
        }
    }
}
