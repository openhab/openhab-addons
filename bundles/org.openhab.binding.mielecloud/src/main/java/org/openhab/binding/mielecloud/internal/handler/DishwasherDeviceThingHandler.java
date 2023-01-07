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
package org.openhab.binding.mielecloud.internal.handler;

import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.Channels.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.handler.channel.ActionsChannelState;
import org.openhab.binding.mielecloud.internal.handler.channel.DeviceChannelState;
import org.openhab.binding.mielecloud.internal.handler.channel.TransitionChannelState;
import org.openhab.core.thing.Thing;

/**
 * ThingHandler implementation for the Miele dishwasher devices.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Add channel state wrappers
 * @author Benjamin Bolte - Add info state channel and map signal flags from API
 * @author Björn Lange - Add elapsed time channel
 */
@NonNullByDefault
public class DishwasherDeviceThingHandler extends AbstractMieleThingHandler {
    /**
     * Creates a new {@link DishwasherDeviceThingHandler}.
     *
     * @param thing The thing to handle.
     */
    public DishwasherDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateDeviceState(DeviceChannelState device) {
        updateState(channel(PROGRAM_ACTIVE), device.getProgramActive());
        updateState(channel(PROGRAM_ACTIVE_RAW), device.getProgramActiveRaw());
        updateState(channel(PROGRAM_PHASE), device.getProgramPhase());
        updateState(channel(PROGRAM_PHASE_RAW), device.getProgramPhaseRaw());
        updateState(channel(OPERATION_STATE), device.getOperationState());
        updateState(channel(OPERATION_STATE_RAW), device.getOperationStateRaw());
        updateState(channel(PROGRAM_START_STOP), device.getProgramStartStop());
        updateState(channel(POWER_ON_OFF), device.getPowerOnOff());
        updateState(channel(DELAYED_START_TIME), device.getDelayedStartTime());
        updateState(channel(PROGRAM_ELAPSED_TIME), device.getProgramElapsedTime());
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
        updateState(channel(REMOTE_CONTROL_CAN_BE_STARTED), actions.getRemoteControlCanBeStarted());
        updateState(channel(REMOTE_CONTROL_CAN_BE_STOPPED), actions.getRemoteControlCanBeStopped());
        updateState(channel(REMOTE_CONTROL_CAN_BE_SWITCHED_ON), actions.getRemoteControlCanBeSwitchedOn());
        updateState(channel(REMOTE_CONTROL_CAN_BE_SWITCHED_OFF), actions.getRemoteControlCanBeSwitchedOff());
    }
}
