/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.easee.internal.command.account.GetUserTotalConsumption;
import org.openhab.binding.easee.internal.config.EaseeConfiguration;
import org.openhab.binding.easee.internal.connector.CommunicationStatus;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link EaseeUserHandler} represents a user with access to an Easee site.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class EaseeUserHandler extends BaseThingHandler implements EaseeThingHandler, AtomicReferenceTrait {
    private final Logger logger = LoggerFactory.getLogger(EaseeUserHandler.class);

    /**
     * Schedule for polling consumption data
     */
    private final AtomicReference<@Nullable Future<?>> dataPollingJobReference;

    public EaseeUserHandler(Thing thing) {
        super(thing);
        this.dataPollingJobReference = new AtomicReference<>(null);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            logger.debug("bridgeStatusChanged: ONLINE");
            if (isInitialized()) {
                startPolling();
            }
        } else {
            logger.debug("bridgeStatusChanged: NOT ONLINE");
            if (isInitialized()) {
                if (bridgeStatusInfo.getStatus() == ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_BRIDGE);
                }
                stopPolling();
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize User");
        logger.debug("Easee User initialized with id: {}", getId());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_BRIDGE);
        startPolling();
    }

    public String getId() {
        return getConfig().get(EaseeBindingConstants.THING_CONFIG_ID).toString();
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        updateJobReference(dataPollingJobReference, scheduler.scheduleWithFixedDelay(this::pollingRun,
                POLLING_INITIAL_DELAY, getBridgeConfiguration().getSessionDataPollingInterval(), TimeUnit.SECONDS));
    }

    /**
     * Stops the polling.
     */
    private void stopPolling() {
        cancelJobReference(dataPollingJobReference);
    }

    /**
     * Poll the Easee Cloud API one time for user consumption data.
     */
    void pollingRun() {
        String userId = getId();
        logger.debug("polling consumption data for user {}", userId);

        if (getThing().getStatus() == ThingStatus.ONLINE) {
            enqueueCommand(new GetUserTotalConsumption(this, userId, this::updateOnlineStatus));
        }
    }

    /**
     * result processor to handle online status updates
     *
     * @param status of command execution
     * @param jsonObject json response result
     */
    private void updateOnlineStatus(CommunicationStatus status, JsonObject jsonObject) {
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

    @Override
    public void updateChannelStatus(Map<Channel, State> values) {
        logger.debug("Handling user channel update.");

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
    public void dispose() {
        logger.debug("User handler disposed.");
        stopPolling();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void enqueueCommand(EaseeCommand command) {
        EaseeBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.enqueueCommand(command);
        } else {
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
}