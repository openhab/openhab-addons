/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.etherrain.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.etherrain.internal.EtherRainBindingConstants;
import org.openhab.binding.etherrain.internal.EtherRainException;
import org.openhab.binding.etherrain.internal.api.EtherRainCommunication;
import org.openhab.binding.etherrain.internal.api.EtherRainStatusResponse;
import org.openhab.binding.etherrain.internal.config.EtherRainConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EtherRainHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
@NonNullByDefault
public class EtherRainHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EtherRainHandler.class);

    private @Nullable EtherRainCommunication device = null;
    private boolean connected = false;
    private @NonNullByDefault({}) EtherRainConfiguration config = null;

    private @Nullable ScheduledFuture<?> updateJob = null;

    private final HttpClient httpClient;

    /*
     * Constructor class. Only call the parent constructor
     */
    public EtherRainHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
        this.updateJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            scheduler.execute(this::updateBridge);
        } else if (channelUID.getId().equals(EtherRainBindingConstants.CHANNEL_ID_EXECUTE)) {
            execute();
            updateState(EtherRainBindingConstants.CHANNEL_ID_EXECUTE, OnOffType.OFF);
        } else if (channelUID.getId().equals(EtherRainBindingConstants.CHANNEL_ID_CLEAR)) {
            clear();
            updateState(EtherRainBindingConstants.CHANNEL_ID_CLEAR, OnOffType.OFF);
        }
    }

    private boolean connectBridge() {
        logger.debug("Attempting to connect to Etherrain with config = (Host: {}, Port: {}, Refresh: {}).", config.host,
                config.port, config.refresh);

        EtherRainCommunication device = new EtherRainCommunication(config.host, config.port, config.password,
                httpClient);

        try {
            device.commandStatus();
        } catch (EtherRainException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create a connection to the EtherRain");
            logger.debug("Could not open API connection to the EtherRain device. Exception received: {}",
                    e.getMessage());
            this.device = null;
            updateStatus(ThingStatus.OFFLINE);
            return false;
        }
        this.device = device;

        updateStatus(ThingStatus.ONLINE);

        return true;
    }

    private void startUpdateJob() {
        logger.debug("Starting Etherrain Update Job");
        this.updateJob = scheduler.scheduleWithFixedDelay(this::updateBridge, 0, config.refresh, TimeUnit.SECONDS);

        logger.debug("EtherRain successfully initialized. Starting status poll at: {}", config.refresh);
    }

    private void stopUpdateJob() {
        logger.debug("Stopping Etherrain Update Job");

        final ScheduledFuture<?> updateJob = this.updateJob;
        if (updateJob != null && !updateJob.isDone()) {
            updateJob.cancel(false);
        }

        this.updateJob = null;
    }

    @SuppressWarnings("null")
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
        } catch (EtherRainException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create a connection to the EtherRain");
            logger.debug("Could not open API connection to the EtherRain device. Exception received: {}",
                    e.getMessage());
            device = null;
            return false;
        }

        updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_STATUS,
                new StringType(response.getOperatingStatus().name()));

        updateState(EtherRainBindingConstants.CHANNEL_ID_COMMAND_STATUS,
                new StringType(response.getLastCommandStatus().name()));

        switch (response.getLastCommandResult()) {
            case OK:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_RESULT, new StringType("OK"));
                break;
            case RN:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_RESULT, new StringType("RAIN INTERRUPTED"));
                break;
            case SH:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_RESULT,
                        new StringType("INTERRUPPTED SHORT"));
                break;
            case NC:
                updateState(EtherRainBindingConstants.CHANNEL_ID_OPERATING_RESULT, new StringType("DID NOT COMPLETE"));
                break;
        }

        updateState(EtherRainBindingConstants.CHANNEL_ID_RELAY_INDEX, new DecimalType(response.getLastActiveValue()));

        OnOffType rs = OnOffType.from(response.isRainSensor());

        updateState(EtherRainBindingConstants.CHANNEL_ID_SENSOR_RAIN, rs);

        logger.debug("Completed Etherrain Update");

        return true;
    }

    private synchronized boolean execute() {
        EtherRainCommunication device = this.device;

        if (device != null) {
            device.commandIrrigate(config.programDelay, config.zoneOnTime1, config.zoneOnTime2, config.zoneOnTime3,
                    config.zoneOnTime4, config.zoneOnTime5, config.zoneOnTime6, config.zoneOnTime7, config.zoneOnTime8);
            updateBridge();
        }

        return true;
    }

    private boolean clear() {
        EtherRainCommunication device = this.device;
        if (device != null) {
            device.commandClear();
        }

        updateBridge();

        return true;
    }

    @Override
    public void initialize() {
        config = getConfigAs(EtherRainConfiguration.class);
        startUpdateJob();
    }

    @Override
    public void dispose() {
        stopUpdateJob();
    }
}
