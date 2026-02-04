/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.command.charger.ChangeConfiguration;
import org.openhab.binding.easee.internal.command.charger.Charger;
import org.openhab.binding.easee.internal.command.charger.GetConfiguration;
import org.openhab.binding.easee.internal.command.charger.LatestChargingSession;
import org.openhab.binding.easee.internal.command.charger.SendCommand;
import org.openhab.binding.easee.internal.command.charger.SendCommandPauseResume;
import org.openhab.binding.easee.internal.command.charger.SendCommandStartStop;
import org.openhab.binding.easee.internal.connector.CommunicationStatus;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
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
public class EaseeChargerHandler extends EaseeBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EaseeChargerHandler.class);

    /**
     * Schedule for polling live data
     */
    private final AtomicReference<@Nullable Future<?>> dataPollingJobReference;
    private final AtomicReference<@Nullable Future<?>> sessionDataPollingJobReference;

    public EaseeChargerHandler(Thing thing) {
        super(thing);
        this.dataPollingJobReference = new AtomicReference<>(null);
        this.sessionDataPollingJobReference = new AtomicReference<>(null);
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize Charger");
        logger.debug("Easee Charger initialized with id: {}", getId());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_BRIDGE);
        startPolling();

        enqueueCommand(new Charger(this, getId(), this::updatePropertiesAndOnlineStatus));
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
    @Override
    protected void startPolling() {
        updateJobReference(dataPollingJobReference, scheduler.scheduleWithFixedDelay(this::pollingRun,
                POLLING_INITIAL_DELAY, getBridgeConfiguration().getDataPollingInterval(), TimeUnit.SECONDS));

        updateJobReference(sessionDataPollingJobReference, scheduler.scheduleWithFixedDelay(this::sessionDataPollingRun,
                POLLING_INITIAL_DELAY, getBridgeConfiguration().getSessionDataPollingInterval(), TimeUnit.SECONDS));
    }

    /**
     * Stops the polling.
     */
    @Override
    protected void stopPolling() {
        cancelJobReference(dataPollingJobReference);
        cancelJobReference(sessionDataPollingJobReference);
    }

    /**
     * Poll the Easee Cloud API one time.
     */
    void pollingRun() {
        String chargerId = getId();
        logger.debug("polling charger data for {}", chargerId);

        // proceed if charger is online
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            enqueueCommand(new GetConfiguration(this, chargerId, this::updateOnlineStatus));
        }
    }

    /**
     * Poll the Easee Cloud API session data endpoint one time.
     */
    void sessionDataPollingRun() {
        String chargerId = getId();
        logger.debug("polling session data for {}", chargerId);

        // proceed if charger is online
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            enqueueCommand(new LatestChargingSession(this, chargerId, this::updateOnlineStatus));
        }
    }

    /**
     * updates online status depending on online information received from the API. this is called by the SiteState
     * Command which retrieves whole site data inclusing charger online status.
     *
     */
    public void setOnline(boolean isOnline) {
        if (isOnline) {
            super.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, STATUS_NO_CONNECTION);
        }
    }

    @Override
    public EaseeCommand buildEaseeCommand(Command command, Channel channel) {
        String chargerId = getId();

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
}
