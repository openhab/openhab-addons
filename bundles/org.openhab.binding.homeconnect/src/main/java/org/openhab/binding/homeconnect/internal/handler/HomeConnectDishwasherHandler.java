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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

/**
 * The {@link HomeConnectDishwasherHandler} is responsible for handling commands, which are
 * sent to one of the channels of a dishwasher.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDishwasherHandler extends AbstractHomeConnectThingHandler {

    public HomeConnectDishwasherHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    protected void configureChannelUpdateHandlers(Map<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_DOOR_STATE, defaultDoorStateChannelUpdateHandler());
        handlers.put(CHANNEL_POWER_STATE, defaultPowerStateChannelUpdateHandler());
        handlers.put(CHANNEL_OPERATION_STATE, defaultOperationStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE, defaultRemoteControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_START_ALLOWANCE_STATE, defaultRemoteStartAllowanceChannelUpdateHandler());
        handlers.put(CHANNEL_SELECTED_PROGRAM_STATE, defaultSelectedProgramStateUpdateHandler());
        handlers.put(CHANNEL_ACTIVE_PROGRAM_STATE, defaultActiveProgramStateUpdateHandler());
        handlers.put(CHANNEL_AMBIENT_LIGHT_STATE, defaultAmbientLightChannelUpdateHandler());
    }

    @Override
    protected void configureEventHandlers(Map<String, EventHandler> handlers) {
        // register default event handlers
        handlers.put(EVENT_DOOR_STATE, defaultDoorStateEventHandler());
        handlers.put(EVENT_REMOTE_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_REMOTE_CONTROL_START_ALLOWED,
                defaultBooleanEventHandler(CHANNEL_REMOTE_START_ALLOWANCE_STATE));
        handlers.put(EVENT_FINISH_IN_RELATIVE, defaultRemainingProgramTimeEventHandler());
        handlers.put(EVENT_REMAINING_PROGRAM_TIME, defaultRemainingProgramTimeEventHandler());
        handlers.put(EVENT_PROGRAM_PROGRESS, defaultPercentQuantityTypeEventHandler(CHANNEL_PROGRAM_PROGRESS_STATE));
        handlers.put(EVENT_SELECTED_PROGRAM, defaultSelectedProgramStateEventHandler());
        handlers.put(EVENT_ACTIVE_PROGRAM, defaultActiveProgramEventHandler());
        handlers.put(EVENT_POWER_STATE, defaultPowerStateEventHandler());
        handlers.put(EVENT_OPERATION_STATE, defaultOperationStateEventHandler());
        handlers.put(EVENT_AMBIENT_LIGHT_STATE, defaultBooleanEventHandler(CHANNEL_AMBIENT_LIGHT_STATE));
        handlers.put(EVENT_AMBIENT_LIGHT_BRIGHTNESS_STATE,
                defaultPercentHandler(CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE));
        handlers.put(EVENT_AMBIENT_LIGHT_COLOR_STATE, defaultAmbientLightColorStateEventHandler());
        handlers.put(EVENT_AMBIENT_LIGHT_CUSTOM_COLOR_STATE, defaultAmbientLightCustomColorStateEventHandler());
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        super.handleCommand(channelUID, command, apiClient);

        handlePowerCommand(channelUID, command, apiClient, STATE_POWER_OFF);

        handleLightCommands(channelUID, command, apiClient);
    }

    @Override
    public String toString() {
        return "HomeConnectDishwasherHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void resetProgramStateChannels(boolean offline) {
        super.resetProgramStateChannels(offline);
        getLinkedChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getLinkedChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getLinkedChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
    }
}
