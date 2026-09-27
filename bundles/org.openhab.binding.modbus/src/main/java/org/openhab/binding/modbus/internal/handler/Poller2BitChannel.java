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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.BitArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Decodes one coil or discrete-input poller-owned Modbus channel.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class Poller2BitChannel {

    private final int address;
    private final PollerChannelState state;

    public Poller2BitChannel(int address, PollerReadFailurePolicy failurePolicy) {
        this.address = address;
        this.state = new PollerChannelState(failurePolicy);
    }

    public synchronized State acceptBits(BitArray bits, int pollStart) {
        long index = (long) address - pollStart;
        if (index < 0 || index > Integer.MAX_VALUE) {
            state.acceptSuccessfulState(UnDefType.UNDEF);
            return state.currentState();
        }
        try {
            state.acceptSuccessfulState(bits.getBit((int) index) ? new DecimalType("1") : DecimalType.ZERO);
        } catch (IndexOutOfBoundsException e) {
            state.acceptSuccessfulState(UnDefType.UNDEF);
        }
        return state.currentState();
    }

    public synchronized State acceptReadFailure() {
        state.acceptReadFailure();
        return state.currentState();
    }
}
