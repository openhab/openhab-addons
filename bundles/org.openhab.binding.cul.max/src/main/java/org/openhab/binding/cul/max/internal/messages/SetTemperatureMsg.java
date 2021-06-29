/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.messages;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message class to handle desired temperature updates
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public class SetTemperatureMsg extends BaseMsg implements DesiredTemperatureStateMsg {

    private static final int SET_TEMPERATURE_PAYLOAD_LEN = 1; /* in bytes */
    private static final int SET_TEMPERATURE_UNTIL_PAYLOAD_LEN = 4; /* in bytes */

    private final Logger logger = LoggerFactory.getLogger(SetTemperatureMsg.class);

    private @Nullable Double desiredTemperature;
    private ThermostatControlMode ctrlMode = ThermostatControlMode.UNKOWN;
    private @Nullable GregorianCalendar untilDateTime = null;

    private static final double TEMPERATURE_MAX = 30.5;
    private static final double TEMPERATURE_MIN = 4.5;

    public SetTemperatureMsg(String rawMsg) throws MaxCulProtocolException {
        super(rawMsg);
        logger.debug("{} Payload Len -> {}", this.msgType, this.payload.length);

        if (this.payload.length == SET_TEMPERATURE_PAYLOAD_LEN
                || this.payload.length == SET_TEMPERATURE_UNTIL_PAYLOAD_LEN) {
            /* extract temperature information */
            desiredTemperature = (this.payload[0] & 0x3f) / 2.0;
            /* extract control mode */
            ctrlMode = ThermostatControlMode.values()[(this.payload[0] >> 6) & 0x3];
            if (this.payload.length == SET_TEMPERATURE_UNTIL_PAYLOAD_LEN) {
                untilDateTime = extractDate(this.payload[1], this.payload[2], this.payload[3]);
            }
        } else {
            logger.error("Got {} message with incorrect length!", this.msgType);
        }
    }

    private GregorianCalendar extractDate(byte one, byte two, byte three) {
        int day = (one & 0x1F);
        int month = ((two & 0xE0) >> 4) | (three >> 7);
        int year = two & 0x3F;
        int time = three & 0x3F;
        return new GregorianCalendar(year + 2000, month, day, time / 2, (time % 2 == 0) ? 0 : 30);
    }

    public SetTemperatureMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr,
            double temperature, ThermostatControlMode mode) {
        super(msgCount, msgFlag, MaxCulMsgType.SET_TEMPERATURE, groupId, srcAddr, dstAddr);

        desiredTemperature = temperature;
        ctrlMode = mode;

        if (temperature > TEMPERATURE_MAX) {
            temperature = TEMPERATURE_MAX;
        } else if (temperature < TEMPERATURE_MIN) {
            temperature = TEMPERATURE_MIN;
        }

        byte[] payload = new byte[SET_TEMPERATURE_PAYLOAD_LEN];
        payload[0] = (byte) (temperature * 2.0);
        payload[0] |= ((mode.toByte() & 0x3) << 6);
        super.appendPayload(payload);
    }

    @Override
    public ThermostatControlMode getControlMode() {
        return ctrlMode;
    }

    @Override
    public @Nullable Double getDesiredTemperature() {
        return desiredTemperature;
    }

    @Override
    public @Nullable GregorianCalendar getUntilDateTime() {
        return untilDateTime;
    }

    /**
     * Print output as decoded fields
     */
    @Override
    protected void printFormattedPayload() {
        logger.debug("\tDesired Temperature => {}", desiredTemperature);
        logger.debug("\tControl Mode => {}", ctrlMode);
        @Nullable
        GregorianCalendar untilDateTime = this.untilDateTime;
        if (untilDateTime != null) {
            logger.debug("\tUntil DateTime      => {}-{}-{} {}:{}:{}", untilDateTime.get(Calendar.YEAR),
                    (untilDateTime.get(Calendar.MONTH) + 1), untilDateTime.get(Calendar.DAY_OF_MONTH),
                    untilDateTime.get(Calendar.HOUR_OF_DAY), untilDateTime.get(Calendar.MINUTE),
                    untilDateTime.get(Calendar.SECOND));
        }
    }
}
