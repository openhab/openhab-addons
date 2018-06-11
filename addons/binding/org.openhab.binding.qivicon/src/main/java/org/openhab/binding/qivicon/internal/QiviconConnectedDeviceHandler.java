/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.qivicon.internal;

import static org.openhab.binding.qivicon.internal.QiviconBindingConstants.*;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QiviconHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Claudius Ellsel - Initial contribution
 */
public class QiviconConnectedDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QiviconHandler.class);
    private HttpClient httpClient;

    Bridge bridge = this.getBridge();

    private String networkAddress;
    private String authKey;

    @Nullable
    private QiviconConfiguration config;

    public QiviconConnectedDeviceHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (channelUID.getId().equals(CHANNEL_TEMPERATURE)) {
        // TODO: handle command
        logger.debug("Handling Command {}", command.toString());
        try {
            apiHelper(networkAddress, authKey, channelUID, command);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    public void apiHelper(String networkAddress, String authKey, ChannelUID channelUID, Command command)
            throws Exception {
        SslContextFactory sslContextFactory = new SslContextFactory(true);

        HttpClient httpClient = new HttpClient(sslContextFactory);
        httpClient.start();

        logger.debug("Network address: {}", networkAddress);
        logger.debug("Channel UID: {}", channelUID.getId());
        System.setProperty("jsse.enableSNIExtension", "false");
        if (networkAddress != null && authKey != null) {
            String requestAddress = "https://" + networkAddress + "/rest/items/" + channelUID.getId();
            logger.debug("Request address: {}", requestAddress);
            Request request = httpClient.POST(requestAddress);
            request.header(HttpHeader.ACCEPT, "application/json");
            request.header(HttpHeader.CONTENT_TYPE, "text/plain");
            String authHeader = "Bearer " + authKey;
            request.header("Authorization", authHeader);
            request.content(new StringContentProvider(command.toString()));
            logger.debug("Request: {}", request.toString());
            try {
                ContentResponse response = request.send();
                logger.debug("Response: {}", response.getContentAsString());
                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    String res = new String(response.getContent());
                    System.out.println(res);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.debug("Error when trying to communicate with the Qivicon REST API: {}", e);
            }
        } else {
            logger.debug("There are some parameters missing.");
        }
        System.setProperty("jsse.enableSNIExtension", "true");
        httpClient.stop();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Qivicon connected thing");

        config = getConfigAs(QiviconConfiguration.class);

        if (bridge != null) {
            networkAddress = bridge.getConfiguration().get(PARAMETER_NETWORK_ADDRESS).toString();
            authKey = bridge.getConfiguration().get(PARAMETER_AUTHORIZATION_KEY).toString();
        }

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
