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
 * Available output states.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.7.0
 */
public enum OutputState implements StateType {
    STATE(0x17);

    private byte refreshCommand;

    OutputState(int refreshCommand) {
        this.refreshCommand = (byte) refreshCommand;
    }

    @Override
    public byte getRefreshCommand() {
        return refreshCommand;
    }

    @Override
    public int getPayloadLength(boolean extendedCmd) {
        return extendedCmd ? 32 : 16;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.OUTPUT;
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
