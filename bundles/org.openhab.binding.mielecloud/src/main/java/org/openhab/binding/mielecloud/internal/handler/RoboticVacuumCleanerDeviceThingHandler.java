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
 * ThingHandler implementation for Miele robotic vacuum cleaners.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class RoboticVacuumCleanerDeviceThingHandler extends AbstractMieleThingHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Creates a new {@link RoboticVacuumCleanerDeviceThingHandler}.
     *
     * @param thing The thing to handle.
     */
    public RoboticVacuumCleanerDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (VACUUM_CLEANER_PROGRAM_ACTIVE.equals(channelUID.getId()) && command instanceof StringType) {
            try {
                triggerProgram(Long.parseLong(command.toString()));
            } catch (NumberFormatException e) {
                logger.warn("Failed to activate program: '{}' is not a valid program ID", command.toString());
            }
        }
    }

    @Override
    protected void updateDeviceState(DeviceChannelState device) {
        updateState(channel(VACUUM_CLEANER_PROGRAM_ACTIVE), device.getProgramActiveId());
        updateState(channel(PROGRAM_ACTIVE_RAW), device.getProgramActiveRaw());
        updateState(channel(OPERATION_STATE), device.getOperationState());
        updateState(channel(OPERATION_STATE_RAW), device.getOperationStateRaw());
        updateState(channel(PROGRAM_START_STOP_PAUSE), device.getProgramStartStopPause());
        updateState(channel(POWER_ON_OFF), device.getPowerOnOff());
        updateState(channel(ERROR_STATE), device.getErrorState());
        updateState(channel(INFO_STATE), device.getInfoState());
        updateState(channel(BATTERY_LEVEL), device.getBatteryLevel());
    }

    @Override
    protected void updateTransitionState(TransitionChannelState transition) {
        if (transition.hasFinishedChanged()) {
            updateState(channel(FINISH_STATE), transition.getFinishState());
        }
    }

    @Override
    protected void updateActionState(ActionsChannelState actions) {
        updateState(channel(REMOTE_CONTROL_CAN_BE_STARTED), actions.getRemoteControlCanBeStarted());
        updateState(channel(REMOTE_CONTROL_CAN_BE_STOPPED), actions.getRemoteControlCanBeStopped());
        updateState(channel(REMOTE_CONTROL_CAN_BE_PAUSED), actions.getRemoteControlCanBePaused());
        updateState(channel(REMOTE_CONTROL_CAN_SET_PROGRAM_ACTIVE), actions.getRemoteControlCanSetProgramActive());
    }
}
