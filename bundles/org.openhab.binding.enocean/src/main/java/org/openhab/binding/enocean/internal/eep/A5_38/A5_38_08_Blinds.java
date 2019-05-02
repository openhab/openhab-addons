/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.A5_38;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_38_08_Blinds extends _4BSMessage {

    static final byte COMMAND_ID = 0x07;
    static final byte POSITION_AND_ANGLE_AVAILABLE = 0x02;
    static final byte SEND_NEW_STATE = 0x04;

    static final byte FUNCTION_STATUS_REQUEST = 0x00;
    static final byte FUNCTION_BLIND_STOPS = 0x01;
    static final byte FUNCTION_BLIND_OPENS = 0x02;
    static final byte FUNCTION_BLIND_CLOSES = 0x03;
    static final byte FUNCTION_BLIND_POSITION_ANGLE = 0x04;
    static final byte FUNCTION_BLIND_OPENS_FOR_TIME = 0x05;
    static final byte FUNCTION_BLIND_CLOSES_FOR_TIME = 0x06;
    static final byte FUNCTION_SET_RUNTIME_PARAMETERS = 0x07;
    static final byte FUNCTION_SET_ANGLE_CONFIGURATIONE = 0x08;
    static final byte FUNCTION_SET_MIN_MAX = 0x09;
    static final byte FUNCTION_SET_SLAT_ANGLE = 0x0A;
    static final byte FUNCTION_SET_POSITION_LOGIC = 0x0B;

    public A5_38_08_Blinds() {
        super();
    }

    public A5_38_08_Blinds(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command outputCommand,
            Map<String, State> currentState, Configuration config) {

        switch (channelId) {
            case CHANNEL_ROLLERSHUTTER:
                byte db0 = Zero | SEND_NEW_STATE | TeachInBit;
                byte db1 = Zero;
                byte db2 = Zero;

                byte position;
                byte angle = 0; // for now, no angle configuration supported
                boolean doStop = false;

                if (outputCommand instanceof DecimalType) {
                    position = ((DecimalType) outputCommand).byteValue();
                } else if (outputCommand instanceof OnOffType) {
                    position = (byte) (((OnOffType) outputCommand == OnOffType.ON) ? 0 : 100);
                } else if (outputCommand instanceof StopMoveType) {
                    position = Zero;
                    doStop = true;
                } else if (outputCommand instanceof UpDownType) {
                    position = (byte) (((UpDownType) outputCommand == UpDownType.UP) ? 0 : 100);
                } else {
                    logger.warn("Unknown command type {}", outputCommand.getClass().getCanonicalName());
                    return;
                }

                if (doStop) {
                    db0 |= FUNCTION_BLIND_STOPS << 4;
                } else if (position <= 0) {
                    db0 |= FUNCTION_BLIND_OPENS << 4;
                } else if (position >= 100) {
                    db0 |= FUNCTION_BLIND_CLOSES << 4;
                } else {
                    db0 |= (FUNCTION_BLIND_POSITION_ANGLE << 4) | POSITION_AND_ANGLE_AVAILABLE;

                    if (angle < 0) {
                        db1 = (byte) ((0x01 << 7) | ((angle / -2) & 0x7F));
                    } else {
                        db1 = (byte) (((angle / 2) & 0x7F));
                    }

                    db2 = position;
                }

                setData(COMMAND_ID, db2, db1, db0);
                return;
            case CHANNEL_ANGLE:
                return; // for now, no angle configuration supported
        }
    }

    protected State getPositionData() {
        byte db0 = getDB_0();
        boolean paf = getBit(db0, 1);

        if (paf) {
            int bsp = getDB_2Value();

            if ((bsp >= 0) && (bsp <= 100)) {
                return new PercentType(bsp);
            }
        }

        return UnDefType.UNDEF;
    }

    protected State getAngleData() {
        byte db0 = getDB_0();
        boolean paf = getBit(db0, 1);

        if (paf) {
            byte db1 = getDB_1();

            boolean as = getBit(db1, 7);
            int an = (db1 & 0x7F) * 2;

            if ((an >= 0) && (an <= 180)) {
                return new QuantityType<>(as ? an * -1 : an, SmartHomeUnits.DEGREE_ANGLE);
            }
        }

        return UnDefType.UNDEF;
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, State currentState, Configuration config) {
        switch (channelId) {
            case CHANNEL_ROLLERSHUTTER:
                return getPositionData();
            case CHANNEL_ANGLE:
                return getAngleData();
        }

        return UnDefType.UNDEF;
    }
}
