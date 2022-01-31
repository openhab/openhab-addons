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
package org.openhab.binding.satel.internal.types;

import java.util.BitSet;

/**
 * Available output control types:
 * <ul>
 * <li>ON - sets an output</li>
 * <li>OFF - resets an output</li>
 * <li>TOGGLE - inverts output state</li>
 * </ul>
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public enum OutputControl implements ControlType {
    ON(0x88),
    OFF(0x89),
    TOGGLE(0x91);

    private byte controlCommand;
    private BitSet stateBits;

    OutputControl(int controlCommand) {
        this.controlCommand = (byte) controlCommand;
        this.stateBits = StateType.getStatesBitSet(OutputState.STATE);
    }

    @Override
    public byte getControlCommand() {
        return controlCommand;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.OUTPUT;
    }

    @Override
    public BitSet getControlledStates() {
        return stateBits;
    }
}
