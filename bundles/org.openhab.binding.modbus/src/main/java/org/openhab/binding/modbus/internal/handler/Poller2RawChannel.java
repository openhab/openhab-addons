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
package org.openhab.binding.modbus.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Exposes a contiguous Modbus register range as uppercase hexadecimal bytes.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class Poller2RawChannel {
    private final int address;
    private final int length;
    private final PollerChannelState state;

    public Poller2RawChannel(int address, int length, PollerReadFailurePolicy failurePolicy) {
        this.address = address;
        this.length = length;
        this.state = new PollerChannelState(failurePolicy);
    }

    public synchronized State acceptRegisters(ModbusRegisterArray registers, int pollStart) {
        long offset = (long) address - pollStart;
        if (offset < 0 || offset + length > registers.size()) {
            state.acceptSuccessfulState(UnDefType.UNDEF);
            return state.currentState();
        }
        StringBuilder hex = new StringBuilder(length * 4);
        for (int index = 0; index < length; index++) {
            hex.append(String.format("%04X", registers.getRegister((int) offset + index)));
        }
        state.acceptSuccessfulState(new StringType(hex.toString()));
        return state.currentState();
    }

    public synchronized State acceptReadFailure() {
        state.acceptReadFailure();
        return state.currentState();
    }

    public Optional<String> validateReadRange(int pollStart, int pollLength) {
        long pollEnd = (long) pollStart + pollLength - 1;
        long channelEnd = (long) address + length - 1;
        if (address < pollStart || channelEnd > pollEnd) {
            return Optional.of("Address range %d..%d is outside poll window %d..%d".formatted(address, channelEnd,
                    pollStart, pollEnd));
        }
        return Optional.empty();
    }
}
