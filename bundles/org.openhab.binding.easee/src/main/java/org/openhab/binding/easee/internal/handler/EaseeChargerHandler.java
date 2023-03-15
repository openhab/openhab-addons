/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.easee.internal.handler;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easee.internal.AtomicReferenceTrait;
import org.openhab.binding.easee.internal.EaseeBindingConstants;
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.command.charger.ChangeConfiguration;
import org.openhab.binding.easee.internal.command.charger.Charger;
import org.openhab.binding.easee.internal.command.charger.GetConfiguration;
import org.openhab.binding.easee.internal.command.charger.LatestChargingSession;
import org.openhab.binding.easee.internal.command.charger.SendCommand;
import org.openhab.binding.easee.internal.command.charger.SendCommandPauseResume;
import org.openhab.binding.easee.internal.command.charger.SendCommandStartStop;
import org.openhab.binding.easee.internal.config.EaseeConfiguration;
import org.openhab.binding.easee.internal.connector.CommunicationStatus;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link EaseeChargerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class EaseeChargerHandler extends BaseThingHandler implements EaseeThingHandler, AtomicReferenceTrait {
    private final Logger logger = LoggerFactory.getLogger(EaseeChargerHandler.class);

    /**
     * Schedule for polling live data
     */
    private final AtomicReference<@Nullable Future<?>> dataPollingJobReference;

    public EaseeChargerHandler(Thing thing) {
        super(thing);
        this.dataPollingJobReference = new AtomicReference<>(null);
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize Charger");
        logger.debug("Easee Charger initialized with id: {}", getId());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_BRIDGE);
        startPolling();

        enqueueCommand(new Charger(this, getId(), this::updatePropertiesAndOnlineStatus));
    }

    public String getId() {
        return getConfig().get(EaseeBindingConstants.THING_CONFIG_ID).toString();
    }

    private void updatePropertiesAndOnlineStatus(CommunicationStatus status, JsonObject charger) {
        updateOnlineStatus(status, charger);
        Map<String, String> properties = editProperties();

        String backPlateId = Utils.getAsString(charger.getAsJsonObject(JSON_KEY_BACK_PLATE), JSON_KEY_GENERIC_ID);
        String masterBackPlateId = Utils.getAsString(charger.getAsJsonObject(JSON_KEY_BACK_PLATE),
                JSON_KEY_MASTER_BACK_PLATE_ID);
        if (backPlateId != null && masterBackPlateId != null) {
            if (backPlateId.equals(masterBackPlateId)) {
                properties.put(THING_CONFIG_IS_MASTER, GENERIC_YES);
            } else {
                properties.put(THING_CONFIG_IS_MASTER, GENERIC_NO);
            }
            properties.put(THING_CONFIG_BACK_PLATE_ID, backPlateId);
            properties.put(THING_CONFIG_MASTER_BACK_PLATE_ID, masterBackPlateId);
        }
        String chargerName = Utils.getAsString(charger, JSON_KEY_GENERIC_NAME);
        if (chargerName != null) {
            properties.put(JSON_KEY_GENERIC_NAME, chargerName);
        }
        String circuitId = Utils.getAsString(charger.getAsJsonObject(JSON_KEY_BACK_PLATE), JSON_KEY_CIRCUIT_ID);
        if (circuitId != null) {
            properties.put(JSON_KEY_CIRCUIT_ID, circuitId);
        }

        updateProperties(properties);
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        updateJobReference(dataPollingJobReference, scheduler.scheduleWithFixedDelay(this::pollingRun,
                POLLING_INITIAL_DELAY, getBridgeConfiguration().getDataPollingInterval(), TimeUnit.SECONDS));
    }

    /**
     * Poll the Easee Cloud API one time.
     */
    void pollingRun() {
        String chargerId = getConfig().get(EaseeBindingConstants.THING_CONFIG_ID).toString();
        logger.debug("polling charger data for {}", chargerId);

        // proceed if charger is online
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            enqueueCommand(new GetConfiguration(this, chargerId, this::updateOnlineStatus));
            enqueueCommand(new LatestChargingSession(this, chargerId, this::updateOnlineStatus));
        }
    }

    /**
     * updates online status depending on online information received from the API. this is called by the SiteState
     * Command which retrieves whole site data inclusing charger status.
     *
     */
    public void setOnline(boolean isOnline) {
        if (isOnline) {
            super.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, STATUS_NO_CONNECTION);
        }
    }

    /**
     * result processor to handle online status updates
     *
     * @param status of command execution
     * @param jsonObject json respone result
     */
    protected final void updateOnlineStatus(CommunicationStatus status, JsonObject jsonObject) {
        String msg = Utils.getAsString(jsonObject, JSON_KEY_ERROR_TITLE);
        if (msg == null || msg.isBlank()) {
            msg = status.getMessage();
        }

        switch (status.getHttpCode()) {
            case OK:
            case ACCEPTED:
                super.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                break;
            default:
                super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
        }
    }

    /**
     * Disposes the thing.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        cancelJobReference(dataPollingJobReference);
    }

    /**
     * will update all channels provided in the map
     */
    @Override
    public void updateChannelStatus(Map<Channel, State> values) {
        logger.debug("Handling charger channel update.");

        for (Channel channel : values.keySet()) {
            if (getThing().getChannels().contains(channel)) {
                State value = values.get(channel);
                if (value != null) {
                    logger.debug("Channel is to be updated: {}: {}", channel.getUID().getAsString(), value);
                    updateState(channel.getUID(), value);
                } else {
                    logger.debug("Value is null or not provided by Easee Cloud (channel: {})",
                            channel.getUID().getAsString());
                    updateState(channel.getUID(), UnDefType.UNDEF);
                }
            } else {
                logger.debug("Could not identify channel: {} for model {}", channel.getUID().getAsString(),
                        getThing().getThingTypeUID().getAsString());
            }
        }
    }

    @Override
    public void enqueueCommand(EaseeCommand command) {
        EaseeBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.enqueueCommand(command);
        } else {
            // this should not happen
            logger.warn("no bridge handler found");
        }
    }

    private @Nullable EaseeBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge == null ? null : ((EaseeBridgeHandler) bridge.getHandler());
    }

    @Override
    public EaseeConfiguration getBridgeConfiguration() {
        EaseeBridgeHandler bridgeHandler = getBridgeHandler();
        return bridgeHandler == null ? new EaseeConfiguration() : bridgeHandler.getBridgeConfiguration();
    }

    @Override
    public EaseeCommand buildEaseeCommand(Command command, Channel channel) {
        String chargerId = getConfig().get(EaseeBindingConstants.THING_CONFIG_ID).toString();

        switch (Utils.getWriteCommand(channel)) {
            case COMMAND_CHANGE_CONFIGURATION:
                return new ChangeConfiguration(this, chargerId, channel, command, this::updateOnlineStatus);
            case COMMAND_SEND_COMMAND:
                return new SendCommand(this, chargerId, channel, command, this::updateOnlineStatus);
            case COMMAND_SEND_COMMAND_START_STOP:
                return new SendCommandStartStop(this, chargerId, channel, command, this::updateOnlineStatus);
            case COMMAND_SEND_COMMAND_PAUSE_RESUME:
                return new SendCommandPauseResume(this, chargerId, channel, command, this::updateOnlineStatus);
            default:
                // this should not happen
                logger.error("write command '{}' not found for channel '{}'", command.toString(),
                        channel.getUID().getIdWithoutGroup());
                throw new UnsupportedOperationException(
                        "write command not found for channel: " + channel.getUID().getIdWithoutGroup());
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
