/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easee.internal.AtomicReferenceTrait;
import org.openhab.binding.easee.internal.EaseeBindingConstants;
import org.openhab.binding.easee.internal.UtilsTrait;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.command.charger.ChangeConfiguration;
import org.openhab.binding.easee.internal.command.charger.ChargerState;
import org.openhab.binding.easee.internal.command.charger.GetConfiguration;
import org.openhab.binding.easee.internal.command.charger.LatestChargingSession;
import org.openhab.binding.easee.internal.command.charger.SendCommand;
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

@NonNullByDefault
public class EaseeChargerHandler extends BaseThingHandler
        implements EaseeThingHandler, AtomicReferenceTrait, UtilsTrait {
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
        logger.debug("Easee Charger initialized with id: {}",
                getConfig().get(EaseeBindingConstants.THING_CONFIG_IDENTIFIER));

        startPolling();
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "waiting for bridge to go online");
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        updateJobReference(dataPollingJobReference, scheduler.scheduleWithFixedDelay(this::pollingRun,
                POLLING_INITIAL_DELAY, getBridgeConfiguration().getDataPollingInterval(), TimeUnit.MINUTES));
    }

    /**
     * Poll the Easee Cloud API one time.
     */
    private void pollingRun() {
        String chargerId = getConfig().get(EaseeBindingConstants.THING_CONFIG_IDENTIFIER).toString();

        logger.debug("polling charger data for {}", chargerId);

        Bridge bridge = getBridge();
        if (bridge != null) {
            switch (bridge.getStatus()) {
                case OFFLINE:
                    updateStatus(bridge.getStatus(), ThingStatusDetail.BRIDGE_OFFLINE);
                    // if bridge is offline we do not need further command processing
                    return;
                default:
                    break;
            }
        }

        ChargerState state = new ChargerState(this, chargerId);
        state.registerResultProcessor(this::updateStatusInfo);
        enqueueCommand(state);

        // proceed if charger is online
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            enqueueCommand(new GetConfiguration(this));
            enqueueCommand(new LatestChargingSession(this));
        }
    }

    /**
     * updates status depending on online information received from the API.
     *
     * @param status
     * @param jsonObject
     */
    private void updateStatusInfo(CommunicationStatus status, JsonObject jsonObject) {
        Boolean isOnline = getAsBool(jsonObject, JSON_KEY_ONLINE);

        if (isOnline == null) {
            super.updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR,
                    "no valid data received this is most likely a configuration error.");
        } else if (isOnline) {
            super.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                    "Charger might have no internet connection or fuse tripped");
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
        logger.debug("Handling channel update.");

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
    public void enqueueCommand(@NonNull EaseeCommand command) {
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
    public @NonNull EaseeConfiguration getBridgeConfiguration() {
        EaseeBridgeHandler bridgeHandler = getBridgeHandler();
        return bridgeHandler == null ? new EaseeConfiguration() : bridgeHandler.getBridgeConfiguration();
    }

    @Override
    public @NonNull EaseeCommand buildEaseeCommand(@NonNull Command command, @NonNull Channel channel) {
        switch (getWriteCommand(channel)) {
            case COMMAND_CHANGE_CONFIGURATION:
                return new ChangeConfiguration(this, channel, command);
            case COMMAND_SEND_COMMAND:
                return new SendCommand(this, channel, command);
            default:
                // this should not happen
                logger.warn("write command '{}' not found for channel '{}'", command.toString(),
                        channel.getUID().getIdWithoutGroup());
                throw new UnsupportedOperationException("write command not found: " + command.toString());
        }
    }

}
