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
package org.openhab.binding.homeconnect.internal.handler;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
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

        // register dryer specific handlers
        handlers.put(CHANNEL_DRYER_DRYING_TARGET,
                getAndUpdateProgramOptionsStateDescriptionsAndSelectedProgramStateUpdateHandler());
    }

    @Override
    protected void configureEventHandlers(Map<String, EventHandler> handlers) {
        // register default event handlers
        handlers.put(EVENT_DOOR_STATE, defaultDoorStateEventHandler());
        handlers.put(EVENT_REMOTE_CONTROL_ACTIVE, updateRemoteControlActiveAndProgramOptionsStateEventHandler());
        handlers.put(EVENT_REMOTE_CONTROL_START_ALLOWED,
                defaultBooleanEventHandler(CHANNEL_REMOTE_START_ALLOWANCE_STATE));
        handlers.put(EVENT_FINISH_IN_RELATIVE, defaultRemainingProgramTimeEventHandler());
        handlers.put(EVENT_REMAINING_PROGRAM_TIME, defaultRemainingProgramTimeEventHandler());
        handlers.put(EVENT_PROGRAM_PROGRESS, defaultPercentQuantityTypeEventHandler(CHANNEL_PROGRAM_PROGRESS_STATE));
        handlers.put(EVENT_LOCAL_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_ACTIVE_PROGRAM, defaultActiveProgramEventHandler());
        handlers.put(EVENT_OPERATION_STATE, defaultOperationStateEventHandler());
        handlers.put(EVENT_SELECTED_PROGRAM, updateProgramOptionsAndSelectedProgramStateEventHandler());

        // register dryer specific event handlers
        handlers.put(EVENT_DRYER_DRYING_TARGET,
                event -> getLinkedChannel(CHANNEL_DRYER_DRYING_TARGET)
                        .ifPresent(channel -> updateState(channel.getUID(),
                                event.getValue() == null ? UnDefType.UNDEF : new StringType(event.getValue()))));
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        super.handleCommand(channelUID, command, apiClient);

        String operationState = getOperationState();

        // only handle these commands if operation state allows it
        if (operationState != null && INACTIVE_STATE.contains(operationState)) {
            // set drying target option
            if (command instanceof StringType && CHANNEL_DRYER_DRYING_TARGET.equals(channelUID.getId())) {
                apiClient.setProgramOptions(getThingHaId(), OPTION_DRYER_DRYING_TARGET, command.toFullString(), null,
                        false, false);
            }
        } else {
            logger.debug("Device can not handle command {} in current operation state ({}). thing={}, haId={}", command,
                    operationState, getThingLabel(), getThingHaId());
        }
    }

    @Override
    public String toString() {
        return "HomeConnectDryerHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void resetProgramStateChannels(boolean offline) {
        super.resetProgramStateChannels(offline);
        getLinkedChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getLinkedChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getLinkedChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        if (offline) {
            getLinkedChannel(CHANNEL_DRYER_DRYING_TARGET).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        }
    }
}
