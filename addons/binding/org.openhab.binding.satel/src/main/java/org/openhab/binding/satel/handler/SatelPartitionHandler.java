/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.handler;

import static org.openhab.binding.satel.SatelBindingConstants.THING_TYPE_PARTITION;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.satel.internal.command.ControlObjectCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.types.PartitionControl;
import org.openhab.binding.satel.internal.types.PartitionState;
import org.openhab.binding.satel.internal.types.StateType;

/**
 * The {@link SatelPartitionHandler} is responsible for handling commands, which are
 * sent to one of the channels of a partition device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class SatelPartitionHandler extends SatelThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_PARTITION);

    public SatelPartitionHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected StateType getStateType(String channelId) {
        return PartitionState.valueOf(channelId.toUpperCase());
    }

    @Override
    protected SatelCommand convertCommand(ChannelUID channel, Command command) {
        if (command instanceof OnOffType) {
            boolean switchOn = (command == OnOffType.ON);
            StateType stateType = getStateType(channel.getId());
            byte[] partitions = getObjectBitset(4, thingConfig.getId());
            boolean forceArm = thingConfig.isForceArmingEnabled();
            PartitionControl action = null;
            switch ((PartitionState) stateType) {
                // clear alarms on OFF command
                case ALARM:
                case ALARM_MEMORY:
                case FIRE_ALARM:
                case FIRE_ALARM_MEMORY:
                case VERIFIED_ALARMS:
                case WARNING_ALARMS:
                    action = switchOn ? null : PartitionControl.CLEAR_ALARM;
                    break;

                // arm or disarm, depending on command
                case ARMED:
                case REALLY_ARMED:
                    action = switchOn ? (forceArm ? PartitionControl.FORCE_ARM_MODE_0 : PartitionControl.ARM_MODE_0)
                            : PartitionControl.DISARM;
                    break;
                case ARMED_MODE_1:
                    action = switchOn ? (forceArm ? PartitionControl.FORCE_ARM_MODE_1 : PartitionControl.ARM_MODE_1)
                            : PartitionControl.DISARM;
                    break;
                case ARMED_MODE_2:
                    action = switchOn ? (forceArm ? PartitionControl.FORCE_ARM_MODE_2 : PartitionControl.ARM_MODE_2)
                            : PartitionControl.DISARM;
                    break;
                case ARMED_MODE_3:
                    action = switchOn ? (forceArm ? PartitionControl.FORCE_ARM_MODE_3 : PartitionControl.ARM_MODE_3)
                            : PartitionControl.DISARM;
                    break;

                // do nothing for other types of state
                default:
                    break;
            }

            if (action != null) {
                return new ControlObjectCommand(action, partitions, bridgeHandler.getUserCode(), scheduler);
            }
        }

        return null;
    }

}
