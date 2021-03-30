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

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectDryerHandler} is responsible for handling commands, which are
 * sent to one of the channels of a dryer.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDryerHandler extends AbstractHomeConnectThingHandler {

    private static final List<String> INACTIVE_STATE = List.of(OPERATION_STATE_INACTIVE, OPERATION_STATE_READY);

    private final Logger logger = LoggerFactory.getLogger(HomeConnectDryerHandler.class);

    public HomeConnectDryerHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    protected void configureChannelUpdateHandlers(Map<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_DOOR_STATE, defaultDoorStateChannelUpdateHandler());
        handlers.put(CHANNEL_OPERATION_STATE, defaultOperationStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE, defaultRemoteControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_START_ALLOWANCE_STATE, defaultRemoteStartAllowanceChannelUpdateHandler());
        handlers.put(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE, defaultLocalControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_ACTIVE_PROGRAM_STATE, defaultActiveProgramStateUpdateHandler());
        handlers.put(CHANNEL_SELECTED_PROGRAM_STATE,
                updateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler());
    }

    @Override
    protected void configureEventHandlers(Map<String, EventHandler> handlers) {
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

        // register dryer specific event handlers
        handlers.put(EVENT_DRYER_DRYING_TARGET,
                event -> getThingChannel(CHANNEL_DRYER_DRYING_TARGET).ifPresent(channel -> updateState(channel.getUID(),
                        event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (isThingReadyToHandleCommand()) {
            super.handleCommand(channelUID, command);
            String operationState = getOperationState();

            // only handle these commands if operation state allows it
            if (operationState != null && INACTIVE_STATE.contains(operationState)) {
                // set drying target option
                if (command instanceof StringType && CHANNEL_DRYER_DRYING_TARGET.equals(channelUID.getId())) {
                    getApiClient().ifPresent(apiClient -> {
                        try {
                            apiClient.setProgramOptions(getThingHaId(), OPTION_DRYER_DRYING_TARGET,
                                    command.toFullString(), null, false, false);
                        } catch (ApplianceOfflineException e) {
                            logger.debug("Could not handle command {}. Appliance offline. thing={}, haId={}, error={}",
                                    command.toFullString(), getThingLabel(), getThingHaId(), e.getMessage());
                            updateStatus(OFFLINE);
                            resetChannelsOnOfflineEvent();
                            resetProgramStateChannels();
                        } catch (CommunicationException e) {
                            logger.debug("Could not handle command {}. API communication problem! haId={}, error={}",
                                    command.toFullString(), getThingHaId(), e.getMessage());
                        } catch (AuthorizationException e) {
                            logger.debug("Could not handle command {}. Authorization problem! haId={}, error={}",
                                    command.toFullString(), getThingHaId(), e.getMessage());

                            handleAuthenticationError(e);
                        }
                    });
                }
            } else {
                logger.debug("Device can not handle command {} in current operation state ({}). thing={}, haId={}",
                        command, operationState, getThingLabel(), getThingHaId());
            }
        }
    }

    @Override
    public String toString() {
        return "HomeConnectDryerHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void resetProgramStateChannels() {
        super.resetProgramStateChannels();
        getThingChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
    }
}
