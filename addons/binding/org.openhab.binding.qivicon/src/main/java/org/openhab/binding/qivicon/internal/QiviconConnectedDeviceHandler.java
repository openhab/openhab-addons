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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link QiviconConnectedDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Claudius Ellsel - Initial contribution
 */
public class QiviconConnectedDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QiviconConnectedDeviceHandler.class);
    private HttpClient httpClient;
    private Gson gson = new Gson();
    private ESHThing eshThing;

    Bridge bridge = this.getBridge();

    private String networkAddress = "ip";
    private String authKey = "test";

    @Nullable
    private QiviconConfiguration config;
    private List<org.eclipse.smarthome.core.thing.Channel> thingChannels;

    public QiviconConnectedDeviceHandler(Thing thing, HttpClient httpClient, ESHThing eshThing) {
        super(thing);
        this.httpClient = httpClient;
        this.eshThing = eshThing;
        // TODO: The following part is already in the Handler Factory, so probably can be deleted
        if (networkAddress != null && authKey != null) {
            String requestAddress = "http://" + networkAddress + "/rest/things/";
            String restThings;
            try {
                restThings = httpClient.GET(requestAddress).getContentAsString();
                logger.debug("Response: {}", restThings);
                // eshThings = gson.fromJson(restThings, ESHThing[].class);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.debug("Problem with API communication: {}", e);
            }
        } else {
            logger.debug("There are some parameters missing.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (channelUID.getId().equals(CHANNEL_TEMPERATURE)) {
        // TODO: handle command
        logger.debug("Handling Command {}", command.toString());

        apiHelper(networkAddress, authKey, channelUID, command);

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    public void apiHelper(String networkAddress, String authKey, ChannelUID channelUID, Command command) {
        logger.debug("Network address: {}", networkAddress);
        logger.debug("Channel UID: {}", channelUID.getId());
        if (networkAddress != null && authKey != null) {
            String requestAddress = "http://" + networkAddress + "/rest/items/" + channelUID.getId();
            logger.debug("Request address: {}", requestAddress);
            Request request = httpClient.POST(requestAddress);
            request.header(HttpHeader.ACCEPT, "application/json");
            request.header(HttpHeader.CONTENT_TYPE, "text/plain");
            // String authHeader = "Bearer " + authKey;
            // request.header("Authorization", authHeader);
            request.content(new StringContentProvider(command.toString()));
            logger.debug("Request: {}", request.toString());
            try {
                ContentResponse response = request.send();
                logger.debug("Response: {}", response.getContentAsString());
                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    String res = new String(response.getContent());
                    logger.debug("Response: {}", res);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.debug("Error when trying to communicate with the Qivicon REST API: {}", e);
            }
        } else {
            logger.debug("There are some parameters missing.");
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        logger.debug("Initializing Qivicon connected thing");

        config = getConfigAs(QiviconConfiguration.class);

        if (bridge != null) {
            // TODO: This does not work currently
            networkAddress = bridge.getConfiguration().get(PARAMETER_NETWORK_ADDRESS).toString();
            authKey = bridge.getConfiguration().get(PARAMETER_AUTHORIZATION_KEY).toString();
        }

        updateChannels();

        updateStatusHelper();

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    public void updateChannels() {
        for (Channel channel : eshThing.getChannels()) {
            String uId = channel.getUid();
            String itemType = channel.getItemType();
            logger.debug("Channel UID: {}", uId);
            logger.debug("Channel Item Type: {}", itemType);
            if (channel.getLinkedItems() != null) {
                if (itemType != null) {
                    // TODO: This does not work currently, thingChannel seems to stay null
                    org.eclipse.smarthome.core.thing.Channel thingChannel = ChannelBuilder
                            .create(new ChannelUID(uId), itemType).withLabel(channel.getLabel())
                            .withDescription(channel.getDescription()).build();
                    // .withType(new ChannelType(new ChannelTypeUID(uId), false, "Test", "Test"))
                    // ChannelBuilder.create(new ChannelUID(uId), itemType).build();
                    if (thingChannels == null) {
                        // thingChannels.add(thingChannel);
                    } else if (!thingChannels.contains(thingChannel)) {
                        // thingChannels.add(thingChannel);
                    }
                } else {
                    logger.debug("No item type: {}", uId);
                }
            } else {
                logger.debug("No item linked: {}", uId);
            }
        }
        if (thingChannels != null) {
            updateThing(editThing().withChannels(thingChannels).build());
        }
    }

    public void updateStatusHelper() {
        String status = eshThing.getStatusInfo().getStatus();
        logger.debug("JSON Fetched String: {}", status);
        switch (status) {
            case "ONLINE":
                updateStatus(ThingStatus.ONLINE);
                break;
            case "OFFLINE":
                updateStatus(ThingStatus.OFFLINE);
                break;
            case "UNKNOWN":
                updateStatus(ThingStatus.UNKNOWN);
                break;
            default:
                break;
        }
    }
}
