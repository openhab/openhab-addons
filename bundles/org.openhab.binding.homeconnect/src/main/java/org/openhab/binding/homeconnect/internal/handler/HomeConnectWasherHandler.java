/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.handler;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_ACTIVE_PROGRAM_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_DOOR_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_LOCAL_CONTROL_ACTIVE_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_OPERATION_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_PROGRAM_PROGRESS_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_REMAINING_PROGRAM_TIME_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_REMOTE_CONTROL_ACTIVE_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_REMOTE_START_ALLOWANCE_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_SELECTED_PROGRAM_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_WASHER_IDOS1;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_WASHER_IDOS2;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_WASHER_SPIN_SPEED;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_WASHER_TEMPERATURE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_ACTIVE_PROGRAM;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_DOOR_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_LOCAL_CONTROL_ACTIVE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_OPERATION_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_PROGRAM_PROGRESS;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_REMAINING_PROGRAM_TIME;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_REMOTE_CONTROL_ACTIVE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_REMOTE_CONTROL_START_ALLOWED;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_SELECTED_PROGRAM;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_WASHER_IDOS_1_DOSING_LEVEL;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_WASHER_IDOS_2_DOSING_LEVEL;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_WASHER_SPIN_SPEED;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_WASHER_TEMPERATURE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.OPERATION_STATE_INACTIVE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.OPERATION_STATE_READY;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.OPTION_WASHER_IDOS_1_DOSING_LEVEL;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.OPTION_WASHER_IDOS_2_DOSING_LEVEL;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.OPTION_WASHER_SPIN_SPEED;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.OPTION_WASHER_TEMPERATURE;
import static org.openhab.core.thing.ThingStatus.OFFLINE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectWasherHandler} is responsible for handling commands, which are
 * sent to one of the channels of a washing machine.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectWasherHandler extends AbstractHomeConnectThingHandler {

    private static final List<String> INACTIVE_STATE = Arrays.asList(OPERATION_STATE_INACTIVE, OPERATION_STATE_READY);

    private final Logger logger = LoggerFactory.getLogger(HomeConnectWasherHandler.class);

    public HomeConnectWasherHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    protected void configureChannelUpdateHandlers(ConcurrentHashMap<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_DOOR_STATE, defaultDoorStateChannelUpdateHandler());
        handlers.put(CHANNEL_OPERATION_STATE, defaultOperationStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE, defaultRemoteControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_START_ALLOWANCE_STATE, defaultRemoteStartAllowanceChannelUpdateHandler());
        handlers.put(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE, defaultLocalControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_ACTIVE_PROGRAM_STATE, defaultActiveProgramStateUpdateHandler());
        handlers.put(CHANNEL_SELECTED_PROGRAM_STATE,
                updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler());

        // register washer specific handlers
        handlers.put(CHANNEL_WASHER_SPIN_SPEED, (channelUID, cache) -> {
            Optional<Channel> channel = getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE);
            if (channel.isPresent()) {
                updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler()
                        .handle(channel.get().getUID(), cache);
            }
        });
        handlers.put(CHANNEL_WASHER_TEMPERATURE, (channelUID, cache) -> {
            Optional<Channel> channel = getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE);
            if (channel.isPresent()) {
                updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler()
                        .handle(channel.get().getUID(), cache);
            }
        });
    }

    @Override
    protected void configureEventHandlers(ConcurrentHashMap<String, EventHandler> handlers) {
        // register default event handlers
        handlers.put(EVENT_DOOR_STATE, defaultDoorStateEventHandler());
        handlers.put(EVENT_REMOTE_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_REMOTE_CONTROL_START_ALLOWED,
                defaultBooleanEventHandler(CHANNEL_REMOTE_START_ALLOWANCE_STATE));
        handlers.put(EVENT_REMAINING_PROGRAM_TIME, defaultRemainingProgramTimeEventHandler());
        handlers.put(EVENT_PROGRAM_PROGRESS, defaultPercentEventHandler(CHANNEL_PROGRAM_PROGRESS_STATE));
        handlers.put(EVENT_LOCAL_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_ACTIVE_PROGRAM, defaultActiveProgramEventHandler());
        handlers.put(EVENT_OPERATION_STATE, defaultOperationStateEventHandler());
        handlers.put(EVENT_SELECTED_PROGRAM, updateProgramOptionsAndSelectedProgramStateEventHandler());

        // register washer specific event handlers
        handlers.put(EVENT_WASHER_TEMPERATURE,
                event -> getThingChannel(CHANNEL_WASHER_TEMPERATURE).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
        handlers.put(EVENT_WASHER_SPIN_SPEED,
                event -> getThingChannel(CHANNEL_WASHER_SPIN_SPEED).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
        handlers.put(EVENT_WASHER_IDOS_1_DOSING_LEVEL,
                event -> getThingChannel(CHANNEL_WASHER_IDOS1).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
        handlers.put(EVENT_WASHER_IDOS_2_DOSING_LEVEL,
                event -> getThingChannel(CHANNEL_WASHER_IDOS2).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (isThingReadyToHandleCommand()) {
            super.handleCommand(channelUID, command);
            @Nullable
            String operationState = getOperationState();
            Optional<HomeConnectApiClient> apiClient = getApiClient();

            try {
                // only handle these commands if operation state allows it
                if (operationState != null && INACTIVE_STATE.contains(operationState) && apiClient.isPresent()) {
                    // set temperature option
                    if (command instanceof StringType && CHANNEL_WASHER_TEMPERATURE.equals(channelUID.getId())) {
                        apiClient.get().setProgramOptions(getThingHaId(), OPTION_WASHER_TEMPERATURE,
                                command.toFullString(), null, false, false);
                    }

                    // set spin speed option
                    if (command instanceof StringType && CHANNEL_WASHER_SPIN_SPEED.equals(channelUID.getId())) {
                        apiClient.get().setProgramOptions(getThingHaId(), OPTION_WASHER_SPIN_SPEED,
                                command.toFullString(), null, false, false);
                    }

                    // set iDos 1 option
                    if (command instanceof StringType && CHANNEL_WASHER_IDOS1.equals(channelUID.getId())) {
                        apiClient.get().setProgramOptions(getThingHaId(), OPTION_WASHER_IDOS_1_DOSING_LEVEL,
                                command.toFullString(), null, false, false);
                    }

                    // set iDos 2 option
                    if (command instanceof StringType && CHANNEL_WASHER_IDOS2.equals(channelUID.getId())) {
                        apiClient.get().setProgramOptions(getThingHaId(), OPTION_WASHER_IDOS_2_DOSING_LEVEL,
                                command.toFullString(), null, false, false);
                    }
                } else {
                    logger.debug("Device can not handle command {} in current operation state ({}). haId={}", command,
                            operationState, getThingHaId());
                }
            } catch (ApplianceOfflineException e) {
                logger.debug("Could not handle command {}. Appliance offline. thing={}, haId={}, error={}",
                        command.toFullString(), getThingLabel(), getThingHaId(), e.getMessage());
                updateStatus(OFFLINE);
                resetChannelsOnOfflineEvent();
                resetProgramStateChannels();
            } catch (CommunicationException e) {
                logger.debug("Could not handle command {}. API communication problem! thing={}, haId={}, error={}",
                        command.toFullString(), getThingLabel(), getThingHaId(), e.getMessage());
            } catch (AuthorizationException e) {
                logger.debug("Could not handle command {}. Authorization problem! thing={}, haId={}, error={}",
                        command.toFullString(), getThingLabel(), getThingHaId(), e.getMessage());
                handleAuthenticationError(e);
            }
        }
    }

    @Override
    public String toString() {
        return "HomeConnectWasherHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void resetProgramStateChannels() {
        super.resetProgramStateChannels();
        getThingChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_WASHER_TEMPERATURE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_WASHER_SPIN_SPEED).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
    }
}
