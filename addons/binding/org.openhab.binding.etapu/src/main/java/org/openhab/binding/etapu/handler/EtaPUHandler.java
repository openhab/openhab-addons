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

import org.eclipse.smarthome.core.library.types.DecimalType;
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

    /**
     * Constructor
     *
     * @param thing
     */
    public EtaPUHandler(Thing thing) {
        super(thing);

        addReadOnlyChannels();

    }

    private void addReadOnlyChannels() {
        addChannel("kesselstatus", "/user/var/112/10021/0/0/12000");
        addChannel("heizkreisstatus", "/user/var/112/10101/0/0/12090");
        addChannel("stoermeldung", "/user/var/112/10241/0/11149/2001");
        addChannel("entaschentaste", "/user/var/112/10021/0/0/12112");
        addChannel("entaschennachfruehestens", "/user/var/112/10021/0/0/12073");
        addChannel("entaschennachspaetestens", "/user/var/112/10021/0/0/12074");
        addChannel("kuebelleerennach", "/user/var/112/10021/0/0/12120");
        addChannel("silovorrat", "/user/var/112/10201/0/0/12015");
        addChannel("kesselpelletsvorrat", "/user/var/112/10021/0/0/12011");
        addChannel("kesselpelletsfuellen", "/user/var/112/10021/0/0/12071");
        addChannel("betriebssekunden", "/user/var/112/10021/0/0/12153");
        addChannel("gesamtverbrauch", "/user/var/112/10021/0/0/12016");
        addChannel("kgseitwartung", "/user/var/112/10021/0/0/12014");
        addChannel("kgseitentaschung", "/user/var/112/10021/0/0/12012");
        addChannel("hksolltemperatur", "/user/var/112/10101/0/0/12111");
        addChannel("hkvorlaufminus10", "/user/var/112/10101/0/0/12104");
        addChannel("hkvorlaufplus10", "/user/var/112/10101/0/0/12103");
        addChannel("hkvorlaufabsenkung", "/user/var/112/10101/0/0/12107");
        addChannel("heizgrenzetag", "/user/var/112/10101/0/0/12096");
        addChannel("heizgrenzenacht", "/user/var/112/10101/0/0/12097");
        addChannel("aussentemperatur", "/user/var//112/10241/0/0/12197");
    }

    /**
     * This method is not implemented as the current version of the binding only supports read only channels
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only
    }

    /**
     * Initializes the Handler, reads the config and starts a scheduler that starts a refresh of all channels in the
     * configured interval.
     * If configuration is invalid the thing is set to offline.
     * If no channel can be updated successfully the thing is set to offline.
     */
    @Override
    public void initialize() {
        sourceConfig = getConfigAs(SourceConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sourceConfig.ipAddress != null && !sourceConfig.ipAddress.isEmpty()) {
                        refresh();
                        updateStatus(ThingStatus.ONLINE);
                    }
                } catch (Exception e) {

                }
            }

        }, 0, sourceConfig.refreshInterval, TimeUnit.SECONDS);
        if (sourceConfig.ipAddress != null && !sourceConfig.ipAddress.isEmpty()) {
            int channelsupdated = refresh();
            if (channelsupdated > 0) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Es konnten keine Channels abgefragt werden.");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Keine IP Adresse konfiguriert.");
        }
    }

    /**
     * Refreshes all channels and returns the number of channels that have been updated successfully.
     *
     * @return
     */
    private synchronized int refresh() {
        int channelsupdated = 0;
        for (ETAChannel channel : channels) {
            try {
                refreshChannel(channel);
                channelsupdated++;
            } catch (Exception e) {
                logger.warn("Error refreshing channel " + channel.getId() + " of thing " + getThing().getUID(), e);
            }
        }
        return channelsupdated;
    }

    private void refreshChannel(ETAChannel etachannel) {
        sendRequest(etachannel);
        Channel channel = getThing().getChannel(etachannel.getId());
        if (channel != null) {
            State state;
            try {
                Double d = Double.parseDouble(etachannel.getValue().replace(",", "."));
                state = new DecimalType(d);
            } catch (NumberFormatException e) {
                state = new StringType(etachannel.getValue());
            }
            updateState(channel.getUID(), state);
        }
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