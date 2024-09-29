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
package org.openhab.binding.enocean.internal.eep.A5_38;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
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
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        switch (channelId) {
            case CHANNEL_ROLLERSHUTTER:
                byte db0 = ZERO | SEND_NEW_STATE | TEACHIN_BIT;
                byte db1 = ZERO;
                byte db2 = ZERO;

                byte position;
                byte angle = 0; // for now, no angle configuration supported
                boolean doStop = false;

                if (outputCommand instanceof DecimalType decimalCommand) {
                    position = decimalCommand.byteValue();
                } else if (outputCommand instanceof OnOffType onOffCommand) {
                    position = (byte) ((onOffCommand == OnOffType.ON) ? 0 : 100);
                } else if (outputCommand instanceof StopMoveType) {
                    position = ZERO;
                    doStop = true;
                } else if (outputCommand instanceof UpDownType upDownCommand) {
                    position = (byte) ((upDownCommand == UpDownType.UP) ? 0 : 100);
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
        byte db0 = getDB0();
        boolean paf = getBit(db0, 1);

        if (paf) {
            int bsp = getDB2Value();

            if ((bsp >= 0) && (bsp <= 100)) {
                return new PercentType(bsp);
            }
        }

        return UnDefType.UNDEF;
    }

    protected State getAngleData() {
        byte db0 = getDB0();
        boolean paf = getBit(db0, 1);

        if (paf) {
            byte db1 = getDB1();

            boolean as = getBit(db1, 7);
            int an = (db1 & 0x7F) * 2;

            if ((an >= 0) && (an <= 180)) {
                return new QuantityType<>(as ? an * -1 : an, Units.DEGREE_ANGLE);
            }
        }

        return UnDefType.UNDEF;
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        switch (channelId) {
            case CHANNEL_ROLLERSHUTTER:
                return getPositionData();
            case CHANNEL_ANGLE:
                return getAngleData();
        }

        return UnDefType.UNDEF;
    }
}
