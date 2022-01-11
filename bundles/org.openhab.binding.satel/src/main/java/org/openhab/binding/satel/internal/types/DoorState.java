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

/**
 * Available door states.
 *
 * @author Krzysztof Goworek - Initial contribution
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
