/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.types;

import java.util.BitSet;

/**
 * Available zone control types.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.9.0
 */
public enum ZoneControl implements ControlType {
    BYPASS(0x86, ZoneState.BYPASS),
    UNBYPASS(0x87, ZoneState.BYPASS),
    ISOLATE(0x90, ZoneState.ISOLATE);

    private byte controlCommand;
    private BitSet stateBits;

    ZoneControl(int controlCommand, ZoneState... controlledStates) {
        this.controlCommand = (byte) controlCommand;
        this.stateBits = StateType.getStatesBitSet(controlledStates);
    }

    @Override
    public byte getControlCommand() {
        return controlCommand;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.ZONE;
    }

    @Override
    public BitSet getControlledStates() {
        return stateBits;
    }

}
