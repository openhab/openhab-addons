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

import static org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode.TEMPORARY;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.max.internal.messages.constants.ThermostatControlMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thermostate State message
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public class ThermostatStateMsg extends BaseMsg implements BatteryStateMsg, RfErrorStateMsg, ThermostatValveStateMsg,
        ThermostatCommonStateMsg, DesiredTemperatureStateMsg, ActualTemperatureStateMsg {

    private static final int THERMOSTAT_STATE_TIME_PAYLOAD_LEN = 6; /* in bytes */
    private static final int THERMOSTAT_STATE_MEAS_PAYLOAD_LEN = 5; /* in bytes */
    private static final int THERMOSTAT_STATE_SHORT_PAYLOAD_LEN = 3; /* in bytes */

    private final Logger logger = LoggerFactory.getLogger(ThermostatStateMsg.class);

    private @Nullable Double desiredTemperature;
    private int valvePos;
    private boolean dstActive;
    private boolean lanGateway; // unknown what this is for
    private boolean lockedForManualSetPoint;
    private boolean rfError;
    private boolean batteryLow;
    private @Nullable Double measuredTemperature = null;
    private @Nullable GregorianCalendar untilDateTime = null;
    private ThermostatControlMode ctrlMode = ThermostatControlMode.UNKOWN;

    public ThermostatStateMsg(String rawMsg) throws MaxCulProtocolException {
        super(rawMsg);
        logger.debug("{} Payload Len -> {}", this.msgType, this.payload.length);

        if (this.payload.length == THERMOSTAT_STATE_SHORT_PAYLOAD_LEN
                || this.payload.length == THERMOSTAT_STATE_MEAS_PAYLOAD_LEN
                || this.payload.length == THERMOSTAT_STATE_TIME_PAYLOAD_LEN) {
            /* extract control mode */
            ctrlMode = ThermostatControlMode.values()[(this.payload[0] & 0x3)];
            /* extract DST status */
            dstActive = extractBitFromByte(this.payload[0], 3);
            /* extract lanGateway */
            lanGateway = extractBitFromByte(this.payload[0], 4);
            /* extract locked status */
            lockedForManualSetPoint = extractBitFromByte(this.payload[0], 5);
            /* extract rferror status */
            rfError = extractBitFromByte(this.payload[0], 6);
            /* extract battery status */
            batteryLow = extractBitFromByte(this.payload[0], 7);
            /* extract valve position */
            valvePos = this.payload[1];
            /* extract desired temperature information */
            desiredTemperature = (this.payload[2] & 0x7f) / 2.0;

            /* handle longer packet */
            if (this.payload.length == THERMOSTAT_STATE_MEAS_PAYLOAD_LEN) {
                /* extract measured temperature */
                if (ctrlMode != TEMPORARY) {
                    int mTemp = (this.payload[3] & 0x1);
                    mTemp <<= 8;
                    mTemp |= ((this.payload[4]) & 0xff);
                    Double measuredTemperature = mTemp / 10.0; // temperature over 25.5
                    // uses extra bit in
                    // desiredTemperature
                    // byte
                    if (measuredTemperature >= 4.5) {
                        this.measuredTemperature = measuredTemperature;
                    }
                }
            } else if (this.payload.length == THERMOSTAT_STATE_TIME_PAYLOAD_LEN) {
                untilDateTime = extractDate(this.payload[3], this.payload[4], this.payload[5]);
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

    /**
     * Print output as decoded fields
     */
    @Override
    protected void printFormattedPayload() {
        logger.debug("\tDesired Temperature => {}", desiredTemperature);
        logger.debug("\tMeasured Temperature=> {}", measuredTemperature);
        logger.debug("\tValve Position      => {}", valvePos);
        logger.debug("\tControl Mode        => {}", ctrlMode);
        logger.debug("\tDST Active          => {}", dstActive);
        logger.debug("\tLAN Gateway         => {}", lanGateway);
        logger.debug("\tPanel locked        => {}", lockedForManualSetPoint);
        logger.debug("\tRF Error            => {}", rfError);
        logger.debug("\tBattery Low         => {}", batteryLow);
        @Nullable
        GregorianCalendar untilDateTime = this.untilDateTime;
        if (untilDateTime != null) {
            logger.debug("\tUntil DateTime      => {}-{}-{} {}:{}:{}", untilDateTime.get(Calendar.YEAR),
                    (untilDateTime.get(Calendar.MONTH) + 1), untilDateTime.get(Calendar.DAY_OF_MONTH),
                    untilDateTime.get(Calendar.HOUR_OF_DAY), untilDateTime.get(Calendar.MINUTE),
                    untilDateTime.get(Calendar.SECOND));
        }
    }

    @Override
    public @Nullable Double getMeasuredTemperature() {
        return measuredTemperature;
    }

    @Override
    public @Nullable Double getDesiredTemperature() {
        return desiredTemperature;
    }

    @Override
    public ThermostatControlMode getControlMode() {
        return ctrlMode;
    }

    @Override
    public @Nullable GregorianCalendar getUntilDateTime() {
        return untilDateTime;
    }

    @Override
    public int getValvePos() {
        return (valvePos & 0xff);
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
