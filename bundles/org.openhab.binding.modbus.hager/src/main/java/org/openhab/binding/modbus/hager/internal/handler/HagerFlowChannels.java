/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.hager.internal.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.hager.internal.modbus.ChannelUpdate;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.BitFlag;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HagerFlowChannels} resolves the decoded register values of one Thing to its channels and looks up the
 * register behind a channel.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class HagerFlowChannels {

    private static final Logger LOGGER = LoggerFactory.getLogger(HagerFlowChannels.class);

    private final Map<ChannelKey, ChannelUID> channelUids = new HashMap<>();
    private final Map<String, Register> registers = new HashMap<>();
    private final BiConsumer<ChannelUID, State> stateConsumer;
    private final String thingUid;

    HagerFlowChannels(Thing thing, List<Register> registers, BiConsumer<ChannelUID, State> stateConsumer) {
        this.stateConsumer = stateConsumer;
        this.thingUid = thing.getUID().toString();
        for (Register register : registers) {
            this.registers.put(register.channelId(), register);
            if (!register.bitFlags().isEmpty()) {
                for (BitFlag flag : register.bitFlags()) {
                    this.registers.put(flag.channelId(), register);
                    add(thing, register.groupId(), flag.channelId());
                }
            } else if (register.dataType() != DataType.BITFIELD) {
                add(thing, register.groupId(), register.channelId());
            }
        }
    }

    /**
     * Publishes the given decoded values to their channels.
     *
     * @param updates the decoded values
     */
    void apply(List<ChannelUpdate> updates) {
        for (ChannelUpdate update : updates) {
            ChannelUID channelUID = channelUids.get(new ChannelKey(update.groupId(), update.channelId()));
            if (channelUID == null) {
                LOGGER.debug("[{}] No channel {} in group {}", thingUid, update.channelId(), update.groupId());
                continue;
            }
            stateConsumer.accept(channelUID, update.state());
        }
    }

    /**
     * Looks up the register a channel is decoded from.
     *
     * @param channelId channel id without channel group
     * @return the register, or empty if the channel id is unknown
     */
    Optional<Register> register(String channelId) {
        return Optional.ofNullable(registers.get(channelId));
    }

    private void add(Thing thing, String groupId, String channelId) {
        channelUids.put(new ChannelKey(groupId, channelId), new ChannelUID(thing.getUID(), groupId, channelId));
    }

    private record ChannelKey(String groupId, String channelId) {
    }
}
