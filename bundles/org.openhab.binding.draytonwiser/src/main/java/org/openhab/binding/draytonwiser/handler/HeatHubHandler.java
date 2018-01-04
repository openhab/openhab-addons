/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.handler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeatHubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class HeatHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HeatHubHandler.class);
    private HttpClient httpClient;

    public HeatHubHandler(Bridge thing) {
        super(thing);
        httpClient = new HttpClient();

        try {
            httpClient.start();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (httpClient != null) {
            httpClient.destroy();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (channelUID.getId().equals(CHANNEL_1)) {
        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Drayton Wiser Heat Hub handler");

        try {
            ContentResponse response = sendMessageToHeatHub("data/network/Station", HttpMethod.GET, "");
            if (response.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
            } else if (response.getStatus() == 401) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid authorization token");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Incorrect Heat Hub address");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private ContentResponse sendMessageToHeatHub(String path, HttpMethod method, String content)
            throws InterruptedException, TimeoutException, ExecutionException {
        String address = (String) getConfig().get(DraytonWiserBindingConstants.ADDRESS);
        String authtoken = (String) getConfig().get(DraytonWiserBindingConstants.AUTH_TOKEN);
        StringContentProvider contentProvider = new StringContentProvider(content);
        ContentResponse response = httpClient.newRequest("http://" + address + "/" + path).method(HttpMethod.GET)
                .header("SECRET", authtoken).content(contentProvider).send();
        return response;
    }
}
