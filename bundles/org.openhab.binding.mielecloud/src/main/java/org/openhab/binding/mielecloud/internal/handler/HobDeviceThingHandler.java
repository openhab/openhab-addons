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
import org.openhab.core.thing.Thing;

/**
 * ThingHandler implementation for the Miele hob devices.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Add channel state wrappers
 * @author Benjamin Bolte - Add plate step, add info state channel and map signal flags from API
 */
@NonNullByDefault
public class HobDeviceThingHandler extends AbstractMieleThingHandler {
    /**
     * Creates a new {@link HobDeviceThingHandler}.
     *
     * @param thing The thing to handle.
     */
    public HobDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateDeviceState(DeviceChannelState device) {
        updateState(channel(OPERATION_STATE), device.getOperationState());
        updateState(channel(OPERATION_STATE_RAW), device.getOperationStateRaw());
        updateState(channel(ERROR_STATE), device.getErrorState());
        updateState(channel(INFO_STATE), device.getInfoState());
        updateState(channel(PLATE_1_POWER_STEP), device.getPlateStep(0));
        updateState(channel(PLATE_1_POWER_STEP_RAW), device.getPlateStepRaw(0));
        updateState(channel(PLATE_2_POWER_STEP), device.getPlateStep(1));
        updateState(channel(PLATE_2_POWER_STEP_RAW), device.getPlateStepRaw(1));
        updateState(channel(PLATE_3_POWER_STEP), device.getPlateStep(2));
        updateState(channel(PLATE_3_POWER_STEP_RAW), device.getPlateStepRaw(2));
        updateState(channel(PLATE_4_POWER_STEP), device.getPlateStep(3));
        updateState(channel(PLATE_4_POWER_STEP_RAW), device.getPlateStepRaw(3));
        updateState(channel(PLATE_5_POWER_STEP), device.getPlateStep(4));
        updateState(channel(PLATE_5_POWER_STEP_RAW), device.getPlateStepRaw(4));
        updateState(channel(PLATE_6_POWER_STEP), device.getPlateStep(5));
        updateState(channel(PLATE_6_POWER_STEP_RAW), device.getPlateStepRaw(5));
    }

    @Override
    protected void updateTransitionState(TransitionChannelState transition) {
        // No state transition required
    }

    @Override
    protected void updateActionState(ActionsChannelState actions) {
        // The Hob device has no trigger actions
    }
}
