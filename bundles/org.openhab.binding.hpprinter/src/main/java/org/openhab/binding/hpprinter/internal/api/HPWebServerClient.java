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

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.hpprinter.internal.api.HPServerResult.requestStatus;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link HPEmbeddedWebServerClient} is responsible for handling reading of data from the HP Embedded Web Server
 *
 * @author Stewart Cossey - Initial contribution
 */
public class HPWebServerClient {
    
    enum testWebInterface {
        INVALID,
        STANDARD,
        SECURESOCKETS
    }

    private HttpClient httpClient;
    private String serverAddress;
    public void HPEmbeddedWebServerClient(HttpClient httpClient, String address, Boolean ssl) {
        this.httpClient = httpClient;

        if (ssl) {
            serverAddress = "https://" + address;
        } else {
            serverAddress = "http://" + address;
        }
    }

    public testWebInterface testWebInterface() {        
        return testWebInterface.INVALID;
    }


    public HPServerResult<HPStatus> getStatus() {
        try {
            ContentResponse cr = this.httpClient.GET(serverAddress + HPStatus.endpoint);

            return new HPServerResult<HPStatus>(new HPStatus(new InputSource(new ByteArrayInputStream(cr.getContent()))));
        } catch (TimeoutException ex) {
            return new HPServerResult<HPStatus>(requestStatus.TIMEOUT);
        } catch (InterruptedException | ExecutionException | ParserConfigurationException | SAXException | IOException ex) {
            return new HPServerResult<HPStatus>(requestStatus.ERROR);
        }
    }

    public HPServerResult<HPUsage> getUsage() {
        try {
            ContentResponse cr = this.httpClient.GET(serverAddress + HPUsage.endpoint);

            return new HPServerResult<HPUsage>(new HPUsage(new InputSource(new ByteArrayInputStream(cr.getContent()))));
        } catch (TimeoutException ex) {
            return new HPServerResult<HPUsage>(requestStatus.TIMEOUT);
        } catch (InterruptedException | ExecutionException | ParserConfigurationException | SAXException | IOException ex) {
            return new HPServerResult<HPUsage>(requestStatus.ERROR);
        }
    }

    
}