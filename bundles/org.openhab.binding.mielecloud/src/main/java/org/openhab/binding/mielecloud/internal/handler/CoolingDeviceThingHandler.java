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
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * ThingHandler implementation for the Miele cooling devices.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Add channel state wrappers
 * @author Benjamin Bolte - Add door state and door alarm, add info state channel and map signal flags from API
 */
@NonNullByDefault
public class CoolingDeviceThingHandler extends AbstractMieleThingHandler {
    /**
     * Creates a new {@link CoolingDeviceThingHandler}.
     *
     * @param thing The thing to handle.
     */
    public CoolingDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (!OnOffType.ON.equals(command) && !OnOffType.OFF.equals(command)) {
            return;
        }

        switch (channelUID.getId()) {
            case FRIDGE_SUPER_COOL:
                triggerProcessAction(OnOffType.ON.equals(command) ? ProcessAction.START_SUPERCOOLING
                        : ProcessAction.STOP_SUPERCOOLING);
                break;

            case FREEZER_SUPER_FREEZE:
                triggerProcessAction(OnOffType.ON.equals(command) ? ProcessAction.START_SUPERFREEZING
                        : ProcessAction.STOP_SUPERFREEZING);
                break;
        }
    }

    @Override
    protected void updateDeviceState(DeviceChannelState device) {
        updateState(channel(OPERATION_STATE), device.getOperationState());
        updateState(channel(OPERATION_STATE_RAW), device.getOperationStateRaw());
        updateState(channel(FRIDGE_SUPER_COOL), device.getFridgeSuperCool());
        updateState(channel(FREEZER_SUPER_FREEZE), device.getFreezerSuperFreeze());
        updateState(channel(FRIDGE_TEMPERATURE_TARGET), device.getFridgeTemperatureTarget());
        updateState(channel(FREEZER_TEMPERATURE_TARGET), device.getFreezerTemperatureTarget());
        updateState(channel(FRIDGE_TEMPERATURE_CURRENT), device.getFridgeTemperatureCurrent());
        updateState(channel(FREEZER_TEMPERATURE_CURRENT), device.getFreezerTemperatureCurrent());
        updateState(channel(ERROR_STATE), device.getErrorState());
        updateState(channel(INFO_STATE), device.getInfoState());
        updateState(channel(DOOR_STATE), device.getDoorState());
        updateState(channel(DOOR_ALARM), device.getDoorAlarm());
    }

    @Override
    protected void updateTransitionState(TransitionChannelState transition) {
    }

    @Override
    protected void updateActionState(ActionsChannelState actions) {
        updateState(channel(SUPER_COOL_CAN_BE_CONTROLLED), actions.getSuperCoolCanBeControlled());
        updateState(channel(SUPER_FREEZE_CAN_BE_CONTROLLED), actions.getSuperFreezeCanBeControlled());
    }
}
