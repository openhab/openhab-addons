/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Handler class for all Fronius services.
 *
 * @author Gerrit Beine - Initial contribution
 */
public abstract class FroniusBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusBaseThingHandler.class);
    private final String serviceDescription;

    private String hostname;

    public FroniusBaseThingHandler(Thing thing) {
        super(thing);
        serviceDescription = getServiceDescription();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
    }

    @Override
    public void initialize() {

        if (getBridge() == null) {
            logger.error("Initializing {} Service is only supported within a bridge", serviceDescription);
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        final Configuration configuration = getBridge().getConfiguration();
        hostname = configuration.get("hostname").toString();

        logger.debug("Initializing {} Service for {}", serviceDescription, hostname);

        ((FroniusSymoBridgeHandler) getBridge().getHandler()).registerService(this);

        updateStatus(ThingStatus.ONLINE);
    }

    public void refresh(final HttpClient httpClient) throws TimeoutException, InterruptedException, ExecutionException {
        logger.debug("Refresh {} Service", serviceDescription);
        ContentResponse response = httpClient.GET(getServiceUrl());
        logger.debug("Response data {}", response.toString());
        updateData(response.getContentAsString());
    }

    protected String getHostname() {
        return hostname;
    }

    protected abstract void updateData(String data);

    protected abstract String getServiceDescription();

    protected abstract String getServiceUrl();

}
