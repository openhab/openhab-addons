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
package org.openhab.binding.modbus.ankersolix.internal;

import static org.openhab.binding.modbus.ankersolix.internal.AnkerSolixBindingConstants.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the Anker SOLIX Smart Plug device profile.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
public class AnkerSolixSmartPlugHandler extends AbstractAnkerSolixHandler {

    private static final List<PollRange> POLL_RANGES = Arrays.asList(
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 30000, 37),
            new PollRange(ModbusReadFunctionCode.READ_INPUT_REGISTERS, 32768, 5));

    private final Logger logger = LoggerFactory.getLogger(AnkerSolixSmartPlugHandler.class);

    public AnkerSolixSmartPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected List<PollRange> getPollRanges() {
        return POLL_RANGES;
    }

    @Override
    protected void handleDeviceCommand(String channelId, Command command) {
        if (!CHANNEL_POWER_SWITCH.equals(channelId)) {
            return;
        }
        if (!(command instanceof OnOffType onOffType)) {
            logger.warn("Unsupported smart plug command: {}", command);
            return;
        }

        int value = onOffType == OnOffType.ON ? 1 : 0;
        writeInt16Holding(30047, value);
        setShadowState(CHANNEL_POWER_SWITCH, onOffType);
    }

    @Override
    protected void applyStateFromCache() {
        String serialNumber = readString(30005, 12);
        String rawModel = readString(32768, 5);
        updateStringChannel(CHANNEL_DEVICE_MODEL, resolveModelName(rawModel, serialNumber));
        updateStringChannel(CHANNEL_DEVICE_SERIAL_NUMBER, serialNumber);

        updateScaledPowerChannel(CHANNEL_REAL_TIME_POWER, readScaledUInt16(30030, 10));
        updateVoltageChannel(CHANNEL_VOLTAGE, readScaledUInt16(30031, 10));
        updateCurrentChannel(CHANNEL_CURRENT, readScaledUInt16(30032, 100));

        State switchShadow = getShadowState(CHANNEL_POWER_SWITCH);
        if (switchShadow instanceof OnOffType shadowSwitch) {
            updateChannelState(CHANNEL_POWER_SWITCH, shadowSwitch);
            updateChannelState(CHANNEL_SWITCH_STATUS,
                    shadowSwitch == OnOffType.ON ? new StringType("connected") : new StringType("disconnected"));
            return;
        }

        Integer statusRaw = readUInt16(30029);
        if (statusRaw != null) {
            boolean connected = statusRaw == 1;
            updateChannelState(CHANNEL_POWER_SWITCH, connected ? OnOffType.ON : OnOffType.OFF);
            updateChannelState(CHANNEL_SWITCH_STATUS, new StringType(
                    connected ? "connected" : statusRaw == 0 ? "disconnected" : String.valueOf(statusRaw)));
        }
    }
}
