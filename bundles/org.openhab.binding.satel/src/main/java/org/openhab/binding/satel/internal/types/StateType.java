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
package org.openhab.binding.satel.internal.types;

import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base of all kinds of Integra state.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public interface StateType {

    /**
     * Returns Satel command to get current state for this state type.
     *
     * @return command identifier
     */
    byte getRefreshCommand();

    /**
     * Returns number of payload bytes in refresh command.
     *
     * @param extendedCmd if <code>true</code> return number of bytes for extended command
     * @return payload length
     */
    int getPayloadLength(boolean extendedCmd);

    /**
     * Returns object type for this kind of state.
     *
     * @return Integra object type
     */
    ObjectType getObjectType();

    /**
     * Returns state's first byte in the response buffer.
     *
     * @return start byte in the response
     */
    int getStartByte();

    /**
     * Returns number of state bytes in the response buffer.
     *
     * @param extendedCmd if <code>true</code> return number of bytes for extended command
     * @return bytes count in the response
     */
    int getBytesCount(boolean extendedCmd);

    /**
     * Builds bit set based on list of state types. Each bit is addressed by refresh command.
     *
     * @param states list of states
     * @return built bit set
     */
    static BitSet getStatesBitSet(StateType... states) {
        BitSet stateBits = new BitSet();
        for (StateType state : states) {
            stateBits.set(state.getRefreshCommand());
        }
        return stateBits;
    }

    /**
     * Marker instance for lack of state type.
     */
    static final StateType NONE = new StateType() {

        @Override
        public byte getRefreshCommand() {
            throw new UnsupportedOperationException("Illegal use of NONE state type");
        }

        @Override
        public int getPayloadLength(boolean extendedCmd) {
            throw new UnsupportedOperationException("Illegal use of NONE state type");
        }

        @Override
        public ObjectType getObjectType() {
            throw new UnsupportedOperationException("Illegal use of NONE state type");
        }

        @Override
        public int getStartByte() {
            throw new UnsupportedOperationException("Illegal use of NONE state type");
        }

        @Override
        public int getBytesCount(boolean extendedCmd) {
            throw new UnsupportedOperationException("Illegal use of NONE state type");
        }
    };
}
