/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Decodes one numeric poller-owned Modbus channel.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
final class Poller2Channel {

    private final int address;
    private final int subAddress;
    private final ValueType valueType;
    private final PollerChannelState state;

    public Poller2Channel(int address, ValueType valueType, PollerReadFailurePolicy failurePolicy) {
        this.address = address;
        this.subAddress = 0;
        this.valueType = valueType;
        this.state = new PollerChannelState(failurePolicy);
    }

    public Poller2Channel(String address, ValueType valueType, PollerReadFailurePolicy failurePolicy) {
        String[] parts = address.split("\\.", -1);
        if (parts.length > 2) {
            throw new IllegalArgumentException("Invalid Modbus address " + address);
        }
        this.address = Integer.parseInt(parts[0]);
        this.subAddress = parts.length == 2 ? Integer.parseInt(parts[1]) : 0;
        this.valueType = valueType;
        this.state = new PollerChannelState(failurePolicy);
    }

    public synchronized State acceptRegisters(ModbusRegisterArray registers, int pollStart) {
        long registerOffset = (long) address - pollStart;
        long index = valueType.getBits() < Short.SIZE ? registerOffset * (Short.SIZE / valueType.getBits()) + subAddress
                : registerOffset;
        if (index < 0 || index > Integer.MAX_VALUE) {
            state.acceptSuccessfulState(UnDefType.UNDEF);
            return state.currentState();
        }

        Optional<DecimalType> decodedValue;
        try {
            decodedValue = ModbusBitUtilities.extractStateFromRegisters(registers, (int) index, valueType);
        } catch (IllegalArgumentException e) {
            state.acceptSuccessfulState(UnDefType.UNDEF);
            return state.currentState();
        }
        State decoded = decodedValue.isPresent() ? decodedValue.get() : UnDefType.UNDEF;
        state.acceptSuccessfulState(decoded);
        return state.currentState();
    }

    public synchronized State acceptReadFailure() {
        state.acceptReadFailure();
        return state.currentState();
    }
}
