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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.THING_TYPE_PARTITION;

import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.command.ControlObjectCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.types.PartitionControl;
import org.openhab.binding.satel.internal.types.PartitionState;
import org.openhab.binding.satel.internal.types.StateType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;

/**
 * The {@link SatelPartitionHandler} is responsible for handling commands, which are
 * sent to one of the channels of a partition device.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelPartitionHandler extends SatelStateThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_PARTITION);

    public SatelPartitionHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected StateType getStateType(String channelId) {
        return PartitionState.valueOf(channelId.toUpperCase());
    }

    @Override
    protected Optional<SatelCommand> convertCommand(ChannelUID channel, Command command) {
        if (command instanceof OnOffType) {
            boolean switchOn = (command == OnOffType.ON);
            StateType stateType = getStateType(channel.getId());
            byte[] partitions = getObjectBitset(4, getThingConfig().getId());
            boolean forceArm = getThingConfig().isForceArmingEnabled();
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
                return Optional
                        .of(new ControlObjectCommand(action, partitions, getBridgeHandler().getUserCode(), scheduler));
            }
        }

        return Optional.empty();
    }
}
