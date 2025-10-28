/**
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
package org.openhab.binding.ferroamp.internal.handler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ferroamp.internal.FerroampBindingConstants;
import org.openhab.binding.ferroamp.internal.api.DataType;
import org.openhab.binding.ferroamp.internal.api.FerroAmpUpdateListener;
import org.openhab.binding.ferroamp.internal.api.FerroampMqttCommunication;
import org.openhab.binding.ferroamp.internal.config.ChannelMapping;
import org.openhab.binding.ferroamp.internal.config.FerroampConfiguration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FerroampHandler} is responsible for handling the values sent to and from the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class FerroampHandler extends BaseThingHandler implements FerroAmpUpdateListener {
    private final static Logger logger = LoggerFactory.getLogger(FerroampHandler.class);
    private FerroampConfiguration ferroampConfig = new FerroampConfiguration();
    private @Nullable FerroampMqttCommunication ferroampMqttCommunication;
    private @Nullable ScheduledFuture<?> pollTask;

    public FerroampHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FerroampMqttCommunication ferroampMqttCommunication = this.ferroampMqttCommunication;
        if (ferroampMqttCommunication == null) {
            logger.warn("FerroampMqttCommunication is not initialized");
            return;
        }

        String commandType = "";

        // TODO this might be better modeled into one channel with 3 values/modes
        switch (channelUID.getId()) {
            case FerroampBindingConstants.CHANNEL_REQUEST_CHARGE:
                commandType = "charge";
                break;
            case FerroampBindingConstants.CHANNEL_REQUEST_DISCHARGE:
                commandType = "discharge";
                break;
            case FerroampBindingConstants.CHANNEL_REQUEST_AUTO:
                commandType = "auto";
                break;
            default:
                logger.warn("Unknown channel: {}", channelUID.getId());
                return;
        }
        String requestCommand = "{\"" + "transId" + "\":\"" + UUID.randomUUID().toString() + "\",\"cmd\":{\"name\":\""
                + commandType + "\",\"arg\":\"" + command.toString() + "\"}}";
        ferroampMqttCommunication.sendPublishedTopic(requestCommand);
    }

    @Override
    public void initialize() {
        // Set configuration parameters
        ferroampConfig = getConfigAs(FerroampConfiguration.class);
        if (ferroampConfig.hostName.isBlank() || ferroampConfig.password.isBlank()
                || ferroampConfig.userName.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);

        // TODO handle the case where user or pass is not valid
        ferroampMqttCommunication = new FerroampMqttCommunication(ferroampConfig.userName, ferroampConfig.password,
                ferroampConfig.hostName, FerroampBindingConstants.BROKER_PORT);

        pollTask = scheduler.scheduleWithFixedDelay(this::connectionTask, 60, ferroampConfig.refreshInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> pollTask = this.pollTask;
        if (pollTask != null) {
            pollTask.cancel(true);
            this.pollTask = null;
        }
    }

    private void connectionTask() {
        FerroampMqttCommunication ferroampConnection = this.ferroampMqttCommunication;
        if (ferroampConnection == null) {
            logger.warn("FerroampMqttCommunication is not initialized");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
            return;
        }
        if (!ferroampConnection.isConnected()) {
            logger.debug("FerroampMqttCommunication is not connected, trying to reconnect");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            ferroampConnection.start();
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onFerroAmpUpdate(DataType type, Map<String, @Nullable String> keyValueMap) {

        if (type == DataType.EHUB) {
            for (ChannelMapping mapping : ChannelMapping.getChannelConfigurationEhub()) {
                State newState = StateHelper.convertToState(mapping, keyValueMap.get(mapping.jsonPath));
                updateState("ehub" + mapping.id, newState);
            }
        } else if (type == DataType.SSO) {
            // TODO the SSO need to have a consistent ordering of the SSO's (by some key?), so that the channel id's are
            // always the same

            int ssoNumber = keyValueMap.keySet().stream().map(k -> k.split("-", 2)[0]).filter(s -> s.matches("\\d+"))
                    .mapToInt(Integer::parseInt).max().orElse(-1) + 1;
            for (int ssoIndex = 0; ssoIndex < ssoNumber; ssoIndex++) {
                for (ChannelMapping mapping : ChannelMapping.getSSOMapping()) {
                    State newState = StateHelper.convertToState(mapping,
                            keyValueMap.get(ssoIndex + "-" + mapping.jsonPath));
                    updateState("sso-" + (ssoIndex + 1) + "#" + mapping.id, newState);
                }
            }
        } else if (type == DataType.ESO) {
            for (ChannelMapping mapping : ChannelMapping.getESOMapping()) {
                State newState = StateHelper.convertToState(mapping, keyValueMap.get(mapping.jsonPath));
                updateState("eso#" + mapping.id, newState);
            }
        } else if (type == DataType.ESM) {
            for (ChannelMapping mapping : ChannelMapping.getESMMapping()) {
                State newState = StateHelper.convertToState(mapping, keyValueMap.get(mapping.jsonPath));
                updateState("esm#" + mapping.id, newState);
            }
        } else {
            logger.warn("Received unknown FerroAmp update type: {}", type);
        }
    }
}