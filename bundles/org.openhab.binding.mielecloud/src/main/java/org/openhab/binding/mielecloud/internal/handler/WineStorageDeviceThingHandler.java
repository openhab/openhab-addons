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
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;

/**
 * ThingHandler implementation for the Miele wine storage devices.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Add channel state wrappers
 * @author Benjamin Bolte - Add info state channel and map signal flags from API
 */
@NonNullByDefault
public class WineStorageDeviceThingHandler extends AbstractMieleThingHandler {
    /**
     * Creates a new {@link WineStorageDeviceThingHandler}.
     *
     * @param thing The thing to handle.
     */
    public WineStorageDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        updateState(channel(REMOTE_CONTROL_CAN_BE_STARTED), OnOffType.OFF);
        updateState(channel(REMOTE_CONTROL_CAN_BE_STOPPED), OnOffType.OFF);
    }

    @Override
    protected void updateDeviceState(DeviceChannelState device) {
        updateState(channel(OPERATION_STATE), device.getOperationState());
        updateState(channel(OPERATION_STATE_RAW), device.getOperationStateRaw());
        updateState(channel(TEMPERATURE_TARGET), device.getWineTemperatureTarget());
        updateState(channel(TEMPERATURE_CURRENT), device.getWineTemperatureCurrent());
        updateState(channel(TOP_TEMPERATURE_TARGET), device.getWineTopTemperatureTarget());
        updateState(channel(TOP_TEMPERATURE_CURRENT), device.getWineTopTemperatureCurrent());
        updateState(channel(MIDDLE_TEMPERATURE_TARGET), device.getWineMiddleTemperatureTarget());
        updateState(channel(MIDDLE_TEMPERATURE_CURRENT), device.getWineMiddleTemperatureCurrent());
        updateState(channel(BOTTOM_TEMPERATURE_TARGET), device.getWineBottomTemperatureTarget());
        updateState(channel(BOTTOM_TEMPERATURE_CURRENT), device.getWineBottomTemperatureCurrent());
        updateState(channel(POWER_ON_OFF), device.getPowerOnOff());
        updateState(channel(ERROR_STATE), device.getErrorState());
        updateState(channel(INFO_STATE), device.getInfoState());
    }

    @Override
    protected void updateTransitionState(TransitionChannelState transition) {
    }

    @Override
    protected void updateActionState(ActionsChannelState actions) {
        updateState(channel(REMOTE_CONTROL_CAN_BE_SWITCHED_ON), actions.getRemoteControlCanBeSwitchedOn());
        updateState(channel(REMOTE_CONTROL_CAN_BE_SWITCHED_OFF), actions.getRemoteControlCanBeSwitchedOff());
    }
}
