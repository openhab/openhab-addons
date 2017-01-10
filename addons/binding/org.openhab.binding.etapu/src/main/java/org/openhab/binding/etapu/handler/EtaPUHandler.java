/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.etapu.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.etapu.SourceConfig;
import org.openhab.binding.etapu.channels.ETAChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EtaPUHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Huber - Initial contribution
 */
public class EtaPUHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(EtaPUHandler.class);
    private SourceConfig sourceConfig;
    // List of all Channel ids
    private final Set<ETAChannel> channels = new HashSet<>();

    public EtaPUHandler(Thing thing) {
        super(thing);
        addChannel("kesselstatus", "/user/var/112/10021/0/0/12000");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only

    }

    @Override
    public void initialize() {

        sourceConfig = getConfigAs(SourceConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            e.getClass().getName() + ":" + e.getMessage());
                    logger.debug("Error refreshing source " + getThing().getUID(), e);
                }
            }

        }, 0, sourceConfig.refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

    }

    private void refresh() {
        for (ETAChannel channel : channels) {
            refreshChannel(channel);
        }
    }

    private void refreshChannel(ETAChannel etachannel) {
        sendRequest(etachannel);
        Channel channel = getThing().getChannel(etachannel.getId());
        State state = new StringType(etachannel.getValue());
        updateState(channel.getUID(), state);
    }

    private void sendRequest(ETAChannel channel) {
        URL etarest;
        try {
            etarest = new URL("http://" + sourceConfig.ipAddress + ":8080" + channel.getUrl());
            URLConnection yc = etarest.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            StringBuffer sb = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            channel.setResponse(sb.toString());
        } catch (IOException e) {
            logger.warn("Unable to connect to web service: " + channel.getUrl());
        }
    }

    private void addChannel(String id, String url) {
        ETAChannel c = new ETAChannel();
        c.setId(id);
        c.setUrl(url);
        channels.add(c);
    }

}