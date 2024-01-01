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
package org.openhab.binding.mielecloud.internal.handler;

import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.handler.channel.ActionsChannelState;
import org.openhab.binding.mielecloud.internal.handler.channel.DeviceChannelState;
import org.openhab.binding.mielecloud.internal.handler.channel.TransitionChannelState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThingHandler implementation for Miele dish warmers.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DishWarmerDeviceThingHandler extends AbstractMieleThingHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Creates a new {@link DishWarmerDeviceThingHandler}.
     *
     * @param thing The thing to handle.
     */
    public DishWarmerDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (DISH_WARMER_PROGRAM_ACTIVE.equals(channelUID.getId()) && command instanceof StringType) {
            try {
                triggerProgram(Long.parseLong(command.toString()));
            } catch (NumberFormatException e) {
                logger.warn("Failed to activate program: '{}' is not a valid program ID", command.toString());
            }
        }
    }

    @Override
    protected void updateDeviceState(DeviceChannelState device) {
        updateState(channel(DISH_WARMER_PROGRAM_ACTIVE), device.getProgramActiveId());
        updateState(channel(PROGRAM_ACTIVE_RAW), device.getProgramActiveRaw());
        updateState(channel(OPERATION_STATE), device.getOperationState());
        updateState(channel(OPERATION_STATE_RAW), device.getOperationStateRaw());
        updateState(channel(POWER_ON_OFF), device.getPowerOnOff());
        updateState(channel(PROGRAM_ELAPSED_TIME), device.getProgramElapsedTime());
        updateState(channel(TEMPERATURE_TARGET), device.getTemperatureTarget());
        updateState(channel(TEMPERATURE_CURRENT), device.getTemperatureCurrent());
        updateState(channel(ERROR_STATE), device.getErrorState());
        updateState(channel(INFO_STATE), device.getInfoState());
        updateState(channel(DOOR_STATE), device.getDoorState());
    }

    @Override
    protected void updateTransitionState(TransitionChannelState transition) {
        updateState(channel(PROGRAM_REMAINING_TIME), transition.getProgramRemainingTime());
        updateState(channel(PROGRAM_PROGRESS), transition.getProgramProgress());
        if (transition.hasFinishedChanged()) {
            updateState(channel(FINISH_STATE), transition.getFinishState());
        }
    }

    @Override
    protected void updateActionState(ActionsChannelState actions) {
        updateState(channel(REMOTE_CONTROL_CAN_BE_SWITCHED_ON), actions.getRemoteControlCanBeSwitchedOn());
        updateState(channel(REMOTE_CONTROL_CAN_BE_SWITCHED_OFF), actions.getRemoteControlCanBeSwitchedOff());
    }
}
