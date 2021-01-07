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

import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message class to handle ACK/NACK
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
public class AckMsg extends BaseMsg implements BatteryStateMsg, RfErrorStateMsg, WallThermostatDisplayMeasuredStateMsg,
        ThermostatValveStateMsg, ThermostatCommonStateMsg {

    final static private int ACK_MSG_PAYLOAD_LEN = 2;
    final static private int ACK_MSG_PAYLOAD_MEAS_LEN = 4;
    final static private int ACK_MSG_PAYLOAD_TIME_LEN = 7;
    private boolean isNack;

    private static final Logger logger = LoggerFactory.getLogger(AckMsg.class);

    ThermostatControlMode ctrlMode;
    private boolean dstActive;
    private boolean lanGateway; // unknown what this is for
    private boolean lockedForManualSetPoint;
    private boolean rfError;
    private boolean batteryLow;
    // Heating Thermostat (plus) only
    private int valvePos;
    // Wallmount Thermostat only
    private boolean displayMeasuredTemp;
    private double desiredTemperature;
    private GregorianCalendar untilDateTime = null;

    public AckMsg(String rawMsg) {
        super(rawMsg);
        if (this.payload.length == ACK_MSG_PAYLOAD_LEN || this.payload.length == ACK_MSG_PAYLOAD_MEAS_LEN
                || this.payload.length == ACK_MSG_PAYLOAD_TIME_LEN) {
            isNack = (this.payload[0] == 0x81); // !(this.payload[0] == 0x01);
            /* extract control mode */
            ctrlMode = ThermostatControlMode.values()[(this.payload[0] & 0x3)];
            /* extract DST status */
            dstActive = extractBitFromByte(this.payload[1], 3);
            /* extract lanGateway */
            lanGateway = extractBitFromByte(this.payload[1], 4);
            /* extract locked status */
            lockedForManualSetPoint = extractBitFromByte(this.payload[1], 5);
            /* extract rferror status */
            rfError = extractBitFromByte(this.payload[1], 6);
            /* extract battery status */
            batteryLow = extractBitFromByte(this.payload[1], 7);

            if (this.payload.length == ACK_MSG_PAYLOAD_MEAS_LEN) {
                /*
                 * extract whether wall therm is configured to show measured or
                 * desired temperature
                 */
                displayMeasuredTemp = (this.payload[2] == 0 ? false : true);

                /* extract valve position */
                valvePos = this.payload[2];

                /* extract desired temperature information */
                desiredTemperature = (this.payload[3] & 0x7f) / 2.0;
            }
            if (this.payload.length == ACK_MSG_PAYLOAD_TIME_LEN) {
                untilDateTime = extractDate(this.payload[4], this.payload[5], this.payload[6]);
            }
        } else {
            logger.error("Got {} message with incorrect length!", this.msgType);
        }
    }

    public AckMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr, boolean isNack) {
        super(msgCount, msgFlag, MaxCulMsgType.ACK, groupId, srcAddr, dstAddr);

        byte[] payload = new byte[ACK_MSG_PAYLOAD_LEN];
        payload[0] = 0x01;
        if (isNack) {
            payload[0] |= 0x80; // make 0x81 for NACK
        }
        super.appendPayload(payload);
        this.printDebugPayload();
    }

    private GregorianCalendar extractDate(byte one, byte two, byte three) {
        int day = (one & 0x1F);
        int month = ((two & 0xE0) >> 4) | (three >> 7);
        int year = two & 0x3F;
        int time = three & 0x3F;
        return new GregorianCalendar(year + 2000, month, day, time / 2, (time % 2 == 0) ? 0 : 30);
    }

    public boolean getIsNack() {
        return isNack;
    }

    @Override
    protected void printFormattedPayload() {
        logger.debug("\tIs ACK?                  => {}", (!this.isNack));
        logger.debug("\tDesired Temperature => {}", desiredTemperature);
        logger.debug("\tValve Position      => {}", valvePos);
        logger.debug("\tDisplay meas. temp  => {}", displayMeasuredTemp);
        logger.debug("\tControl Mode        => {}", ctrlMode);
        logger.debug("\tDST Active          => {}", dstActive);
        logger.debug("\tLAN Gateway         => {}", lanGateway);
        logger.debug("\tPanel locked        => {}", lockedForManualSetPoint);
        logger.debug("\tRF Error            => {}", rfError);
        logger.debug("\tBattery Low         => {}", batteryLow);
        if (untilDateTime != null) {
            logger.debug("\tUntil DateTime      => {}-{}-{} {}:{}:{}", untilDateTime.get(Calendar.YEAR),
                    (untilDateTime.get(Calendar.MONTH) + 1), untilDateTime.get(Calendar.DAY_OF_MONTH),
                    untilDateTime.get(Calendar.HOUR_OF_DAY), untilDateTime.get(Calendar.MINUTE),
                    untilDateTime.get(Calendar.SECOND));
        }
    }

    @Override
    public boolean isBatteryLow() {
        return batteryLow;
    }

    @Override
    public boolean isRfError() {
        return rfError;
    }

    @Override
    public boolean isDisplayMeasuredTemp() {
        return displayMeasuredTemp;
    }

    @Override
    public int getValvePos() {
        return valvePos;
    }

    @Override
    public boolean isDstActive() {
        return dstActive;
    }

    @Override
    public boolean isLanGateway() {
        return lanGateway;
    }

    @Override
    public boolean isLockedForManualSetPoint() {
        return lockedForManualSetPoint;
    }
}
