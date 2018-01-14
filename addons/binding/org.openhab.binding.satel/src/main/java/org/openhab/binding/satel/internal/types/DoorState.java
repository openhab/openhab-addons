/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.types;

/**
 * Available door states.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.7.0
 */
public enum DoorState implements StateType {
    OPENED(0x18),
    OPENED_LONG(0x19);

    private byte refreshCommand;

    DoorState(int refreshCommand) {
        this.refreshCommand = (byte) refreshCommand;
    }

    @Override
    public byte getRefreshCommand() {
        return refreshCommand;
    }

    @Override
    public int getPayloadLength(boolean extendedCmd) {
        return 8;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.DOOR;
    }

    @Override
    public int getStartByte() {
        return 0;
    }

    @Override
    public int getBytesCount(boolean extendedCmd) {
        return getPayloadLength(extendedCmd);
    }

}
