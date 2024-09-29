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
package org.openhab.binding.satel.internal.event;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.types.StateType;

/**
 * Event class describing current state of zones/partitions/outputs/doors/troubles.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class IntegraStateEvent implements SatelEvent {

    private byte command;
    private BitSet stateBits;
    private boolean extendedData;
    private Map<StateType, BitSet> stateBitsMap = new HashMap<>();

    /**
     * Constructs new event instance from given state type and state bits.
     *
     * @param command the command byte
     * @param stateBits state bits as byte array
     * @param extendedData whether state bits are for extended command
     */
    public IntegraStateEvent(byte command, byte[] stateBits, boolean extendedData) {
        this.command = command;
        this.stateBits = BitSet.valueOf(stateBits);
        this.extendedData = extendedData;
    }

    /**
     * Checks whether data in the event is valid for given type of state.
     *
     * @param stateType state type
     * @return <code>true</code> if this event is valid for given stae
     */
    public boolean hasDataForState(StateType stateType) {
        return stateType.getRefreshCommand() == this.command;
    }

    /**
     * Returns state bits as {@link BitSet}.
     *
     * @param stateType type of state
     * @return bits for given state
     * @throws IllegalArgumentException when wrong state type given
     */
    public BitSet getStateBits(StateType stateType) {
        if (!hasDataForState(stateType)) {
            throw new IllegalArgumentException("Event does not have data for " + stateType);
        }
        int bitsCount = stateType.getBytesCount(extendedData) * 8;
        // whole payload is a single state
        if (stateType.getStartByte() == 0 && bitsCount == stateBits.size()) {
            return stateBits;
        }
        return Objects.requireNonNull(stateBitsMap.computeIfAbsent(stateType, k -> {
            int startBit = k.getStartByte() * 8;
            return stateBits.get(startBit, startBit + bitsCount);
        }));
    }

    /**
     * Returns <code>true</code> if specified state bit is set for given state.
     *
     * @param stateType type of state
     * @param nbr state bit number
     * @return <code>true</code> if state bit is set
     */
    public boolean isSet(StateType stateType, int nbr) {
        return getStateBits(stateType).get(nbr);
    }

    /**
     * Returns number of state bits that are active for given state.
     *
     * @param stateType type of state
     * @return number of active states
     */
    public int statesSet(StateType stateType) {
        return getStateBits(stateType).cardinality();
    }

    @Override
    public String toString() {
        return String.format("IntegraStateEvent: command = %02X, extended = %b, active = %s", command, extendedData,
                stateBits);
    }
}
