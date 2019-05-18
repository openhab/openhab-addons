/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.etherrain.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.etherrain.EtherRainBindingConstants;
import org.openhab.binding.etherrain.internal.EtherRainConfiguration;
import org.openhab.binding.etherrain.internal.api.EtherRainCommunication;
import org.openhab.binding.etherrain.internal.api.EtherRainStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EtherRainHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
public class EtherRainHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EtherRainHandler.class);

    private EtherRainCommunication device = null;
    private boolean connected = false;

    @Nullable
    private ScheduledFuture<?> updateJob;

    /*
     * Constructor class. Only call the parent constructor
     */
    public EtherRainHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command.toString().equals("REFRESH")) {
            updateBridge();
            return;
        }

        else if (channelUID.getId().equals(EtherRainBindingConstants.CHANNEL_ID_EXECUTE)) {
            execute();
            updateState(EtherRainBindingConstants.CHANNEL_ID_EXECUTE, OnOffType.OFF);
        }

        else if (channelUID.getId().equals(EtherRainBindingConstants.CHANNEL_ID_CLEAR)) {
            clear();
            updateState(EtherRainBindingConstants.CHANNEL_ID_CLEAR, OnOffType.OFF);
        } else {
            logger.warn("Unhandled command: " + command.toFullString());
        }

    }

    private boolean connectBridge() {
        EtherRainConfiguration config = getConfigAs(EtherRainConfiguration.class);

        logger.debug(
                "Attempting to connect to Etherrain wtih config = (Hostname: {}, Port: {}, Password: {}, Refresh: {}).",
                config.hostname, config.port, config.password, config.refresh);

        device = new EtherRainCommunication(config.hostname, config.port, config.password);

        try {
            EtherRainStatusResponse response = device.commandStatus();
            if (response == null) {
                logger.debug("Command Status returned null");
                device = null;
                updateStatus(ThingStatus.OFFLINE);
                return false;
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create a connection to the EtherRain");
            logger.debug("Could not open API connection to the EtherRain device. Exception received: {}", e.toString());
            device = null;
            updateStatus(ThingStatus.OFFLINE);
            return false;
        }

        updateStatus(ThingStatus.ONLINE);

        return true;
    }

    private void startUpdateJob() {
        logger.debug("Starting Etherrain Update Job");
        updateJob = scheduler.scheduleWithFixedDelay(() -> {
            updateBridge();
        }, getConfigAs(EtherRainConfiguration.class).refresh, getConfigAs(EtherRainConfiguration.class).refresh,
                TimeUnit.SECONDS);

        logger.debug("EtherRain sucessfully initialized. Starting status poll at: "
                + getConfigAs(EtherRainConfiguration.class).refresh);

    }

    @SuppressWarnings("null")
    private void stopUpdateJob() {
        logger.debug("Stopping Etherrain Update Job");
        if (updateJob != null && !updateJob.isDone()) {
            updateJob.cancel(false);
        }

        updateJob = null;
    }

    private boolean updateBridge() {

        if (!connected || device == null) {
            connected = connectBridge();
            if (!connected || device == null) {
                connected = false;
                device = null;
                logger.debug("Could not connect to Etherrain device.");
                return false;
            }
        }

        EtherRainStatusResponse response;

        try {
            response = device.commandStatus();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create a connection to the EtherRain");
            logger.debug("Could not open API connection to the EtherRain device. Exception received: {}", e.toString());
            device = null;
            return false;
        }

        switch (response.getOperatingStatus()) {
            case STATUS_READY:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_STATUS, new StringType("READY"));
                break;
            case STATUS_WAITING:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_STATUS, new StringType("WAITING"));
                break;
            case STATUS_BUSY:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_STATUS, new StringType("BUSY"));
                break;
        }

        switch (response.getLastCommandStatus()) {
            case STATUS_OK:
                updateState(EtherRainBindingConstants.CHANNEL_ID_COMMAND_STATUS, new StringType("OK"));
                break;
            case STATUS_ERROR:
                updateState(EtherRainBindingConstants.CHANNEL_ID_COMMAND_STATUS, new StringType("ERROR"));
                break;
            case STATUS_UNATHORIZED:
                updateState(EtherRainBindingConstants.CHANNEL_ID_COMMAND_STATUS, new StringType("UNATHORIZED"));
                break;
        }

        switch (response.getLastCommandResult()) {
            case RESULT_OK:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_RESULT, new StringType("OK"));
                break;
            case RESULT_INTERRUPTED_RAIN:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_RESULT, new StringType("RAIN INTERRUPTED"));
                break;
            case RESULT_INTERUPPTED_SHORT:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_RESULT,
                        new StringType("INTERRUPPTED SHORT"));
                break;
            case RESULT_INCOMPLETE:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_RESULT, new StringType("DID NOT COMPLETE"));
                break;
        }

        updateState(EtherRainBindingConstants.CHANNEL_ID_RELAY_INDEX, new DecimalType(response.getLastActiveValue()));

        OnOffType rs = OnOffType.OFF;

        if (response.isRainSensor()) {
            rs = OnOffType.ON;
        }

        updateState(EtherRainBindingConstants.CHANNEL_ID_SENSOR_RAIN, rs);

        logger.debug("Completed Etherrain Update");

        return true;
    }

    private boolean execute() {
        if (device != null) {
            EtherRainConfiguration config = getConfigAs(EtherRainConfiguration.class);
            device.commandIrrigate(config.programDelay, EtherRainConfiguration.zoneOnTime1,
                    EtherRainConfiguration.zoneOnTime2, EtherRainConfiguration.zoneOnTime3,
                    EtherRainConfiguration.zoneOnTime4, EtherRainConfiguration.zoneOnTime5,
                    EtherRainConfiguration.zoneOnTime6, EtherRainConfiguration.zoneOnTime7,
                    EtherRainConfiguration.zoneOnTime8);
            updateBridge();
        }

        return true;
    }

    private boolean clear() {
        if (device != null) {
            device.commandClear();
            updateBridge();
        }

        return true;
    }

    @Override
    public void initialize() {
        startUpdateJob();
    }

    @Override
    public void dispose() {
        stopUpdateJob();
    }

}
