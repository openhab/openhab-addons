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

/**
 * Available partition states.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public enum PartitionState implements StateType {
    ARMED(0x09),
    REALLY_ARMED(0x0a),
    ARMED_MODE_2(0x0b),
    ARMED_MODE_3(0x0c),
    FIRST_CODE_ENTERED(0x0d),
    ENTRY_TIME(0x0e),
    EXIT_TIME_GT_10(0x0f),
    EXIT_TIME_LT_10(0x10),
    TEMPORARY_BLOCKED(0x11),
    BLOCKED_FOR_GUARD(0x12),
    ALARM(0x13),
    FIRE_ALARM(0x14),
    ALARM_MEMORY(0x15),
    FIRE_ALARM_MEMORY(0x16),
    VIOLATED_ZONES(0x25),
    VERIFIED_ALARMS(0x27),
    ARMED_MODE_1(0x2a),
    WARNING_ALARMS(0x2b);

    private byte refreshCommand;

    PartitionState(int refreshCommand) {
        this.refreshCommand = (byte) refreshCommand;
    }

    @Override
    public byte getRefreshCommand() {
        return refreshCommand;
    }

    @Override
    public int getPayloadLength(boolean extendedCmd) {
        return 4;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.PARTITION;
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
