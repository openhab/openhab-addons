/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver2.handler;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.yamahareceiver2.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver2.discovery.ZoneDiscoveryService;
import org.openhab.binding.yamahareceiver2.internal.YamahaReceiverState;
import org.openhab.binding.yamahareceiver2.internal.protocol.YamahaReceiverCommunication;
import org.openhab.binding.yamahareceiver2.internal.protocol.YamahaReceiverCommunication.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YamahaReceiverHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Gräff - Initial contribution
 */
public class YamahaReceiverHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(YamahaReceiverHandler.class);
    private String host;
    private int refrehInterval = 0;
    private YamahaReceiverState state = null;
    private Timer refreshTimer;
    private ZoneDiscoveryService zoneDiscoveryService;

    public YamahaReceiverHandler(Thing thing) {
        super(thing);
    }

    /**
     * We handle updates of this thing ourself.
     */
    @Override
    protected void updateThing(Thing thing) {
        this.thing = thing;

        // Check if host configuration has changed
        String host_config = (String) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_HOST_NAME);
        if (host_config != null && !host_config.equals(host)) {
            host = host_config;
            createCommunicationObject();
        }

        // Check if refresh configuration has changed
        Integer interval_config = (Integer) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_REFRESH);
        if (interval_config != null && interval_config != refrehInterval) {
            setupRefreshTimer(60 * 1000);
        }
    }

    /**
     *
     */
    @Override
    public void initialize() {
        Configuration config = thing.getConfiguration();
        host = (String) config.get(YamahaReceiverBindingConstants.CONFIG_HOST_NAME);

        // Case: Lost host name somehow. This thing cannot be used any longer.
        if (host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname not set!");
            return;
        }

        createCommunicationObject();
    }

    /**
     * We create a YamahaReceiverState that handles the current state (loudness, power, input etc)
     * and a communication object.
     */
    private void createCommunicationObject() {
        String zoneName = thing.getProperties().get(YamahaReceiverBindingConstants.CONFIG_ZONE);
        if (zoneName == null) {
            zoneName = YamahaReceiverCommunication.Zone.Main_Zone.name();
        }

        Zone zone = YamahaReceiverCommunication.Zone.valueOf(zoneName);

        state = new YamahaReceiverState(new YamahaReceiverCommunication(host, zone));
        try {
            state.updateDeviceInformation();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }
        updateReceiverState();
        setupRefreshTimer(1000);

        // If we are the main zone, detect other zones now.
        if (zone == Zone.Main_Zone) {
            zoneDiscoveryService = new ZoneDiscoveryService();
            zoneDiscoveryService.start(bundleContext);
            zoneDiscoveryService.detectZones(state, thing.getUID().getId());
        }
    }

    private void setupRefreshTimer(int initial_wait_time) {
        if (state == null) {
            return;
        }

        Integer interval_config = (Integer) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_REFRESH);

        // Default is every 5min
        if (interval_config == null) {
            interval_config = new Integer(5);
        }

        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateReceiverState();
            }
        }, initial_wait_time, 60 * 1000 * interval_config);

        refrehInterval = interval_config;
    }

    private void updateReceiverState() {
        try {
            state.updateState();
            updateStatus(ThingStatus.ONLINE);
            updateState(YamahaReceiverBindingConstants.CHANNEL_POWER, state.isPower() ? OnOffType.ON : OnOffType.OFF);
            updateState(YamahaReceiverBindingConstants.CHANNEL_INPUT, new StringType(state.getInput()));
            updateState(YamahaReceiverBindingConstants.CHANNEL_SURROUND, new StringType(state.getSurroundProgram()));
            updateState(YamahaReceiverBindingConstants.CHANNEL_VOLUME, new DecimalType(state.getVolume()));
            updateState(YamahaReceiverBindingConstants.CHANNEL_MUTE, state.isMute() ? OnOffType.ON : OnOffType.OFF);
            updateState(YamahaReceiverBindingConstants.CHANNEL_NETRADIO_TUNE, new DecimalType(state.netRadioChannel));
            logger.info("State upddated!");
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (state == null) {
            logger.error("Thing not yet initialized!");
            return;
        }

        String id = channelUID.getId();

        try {
            switch (id) {
                case YamahaReceiverBindingConstants.CHANNEL_POWER:
                    state.setPower(((OnOffType) command) == OnOffType.ON);
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_INPUT:
                    state.setInput(((StringType) command).toString());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_SURROUND:
                    state.setSurroundProgram(((StringType) command).toString());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_VOLUME:
                    state.setVolume(((DecimalType) command).floatValue());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_MUTE:
                    state.setMute(((OnOffType) command) == OnOffType.ON);
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_NETRADIO_TUNE:
                    state.setNetRadio(((DecimalType) command).intValue());
                    break;
                default:
                    logger.error("Channel " + id + " not supported!");
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
