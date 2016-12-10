/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.internal.serial;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Telegram} class parses and interpretes binary messages
 * and has some conversion methods to handle the bytes back-and-forth
 * correctly.
 *
 * @author Hauke Fuhrmann - Initial contribution
 */
public class Telegram {

    private Logger logger = LoggerFactory.getLogger(Telegram.class);

    private byte sender;
    private byte receiver;
    private Variable commandVar;
    private byte arg;

    public Telegram(byte sender, byte receiver, byte command, byte arg) {
        this.sender = sender;
        this.receiver = receiver;
        this.commandVar = Variable.get(command);
        this.arg = arg;
    }

    /**
     * Update a given ValloxStore with the information from this telegram and notify
     * a list of listeners.
     *
     * Note that there are quite a few telegrams which are not yet interpreted because
     * they implement quite special and rarely used functions. However, the hooks are prepared
     * here so they can be easily implemented once the corresponding bit-pattern is known. Hence,
     * for some telegram commands there is a corresponding check but no further processing
     * of the value yet. These are marked as TODO. Feel free to contribute implementations.
     *
     * @param vallox
     * @param listener
     */
    void updateStore(ValloxStore vallox, Collection<ValueChangeListener> listener) {

        byte value = arg;

        switch (commandVar) {
            case IOPORT_FANSPEED_RELAYS: {
                // not yet implemented telegram
                // convertIoPortFanSpeedRelays
            }
            case IOPORT_MULTI_PURPOSE_1: {
                vallox.postHeatingOn = convertIoPortMultiPurpose1(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.IoPortMultiPurpose1));
            }
            case IOPORT_MULTI_PURPOSE_2: {
                updateIoPortMultiPurpose2(vallox, listener, value);
            }
            case INSTALLED_CO2_SENSORS: {
                // not yet implemented telegram
                // convertInstalledCO2Sensor
            }
            case CURRENT_INCOMMING: {
                vallox.incommingCurrent = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.IncommingCurrent));
            }
            case LAST_ERROR_NUMBER: {
                vallox.lastErrorNumber = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.LastErrorNumber));
            }
            case POST_HEATING_ON_COUNTER: {
                // not yet implemented telegram
            }
            case POST_HEATING_OFF_TIME: {
                // not yet implemented telegram
            }
            case POST_HEATING_TARGET_VALUE: {
                // not yet implemented telegram
            }
            case FLAGS_1: {
                // not yet implemented telegram
            }
            case FLAGS_2: {
                // not yet implemented telegram
            }
            case FLAGS_3: {
                // not yet implemented telegram
            }
            case FLAGS_4: {
                // not yet implemented telegram
            }
            case FLAGS_5: {
                // not yet implemented telegram
            }
            case FLAGS_6: {
                // not yet implemented telegram
            }
            case FIRE_PLACE_BOOSTER_COUNTER: {
                // not yet implemented telegram
            }
            case MAINTENANCE_MONTH_COUNTER: {
                // not yet implemented telegram
            }
            case FAN_SPEED: {
                vallox.fanSpeed = convertFanSpeed(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.FanSpeed));
            }

            case TEMP_OUTSIDE: {
                vallox.tempOutside = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.TempOutside));
                updateEfficiencies(vallox, listener);
            }
            case TEMP_EXHAUST: {
                vallox.tempExhaust = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.TempExhaust));
                updateEfficiencies(vallox, listener);
            }
            case TEMP_INSIDE: {
                vallox.tempInside = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.TempInside));
                updateEfficiencies(vallox, listener);
            }
            case TEMP_INCOMMING: {
                vallox.tempIncomming = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.TempIncomming));
                updateEfficiencies(vallox, listener);
            }
            case SELECT: {
                updateSelect(vallox, listener, value);
            }
            case HUMIDITY: {
                vallox.humidity = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.Humidity));
            }
            case BASIC_HUMIDITY_LEVEL: {
                vallox.basicHumidityLevel = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.BasicHumidityLevel));
            }
            case HUMIDITY_SENSOR1: {
                vallox.humiditySensor1 = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.HumiditySensor1));
            }
            case HUMIDITY_SENSOR2: {
                vallox.humiditySensor2 = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.HumiditySensor2));
            }
            case CO2_HIGH: {
                vallox.cO2High = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.CO2High));
            }
            case CO2_LOW: {
                vallox.cO2Low = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.CO2Low));
            }
            case CO2_SET_POINT_UPPER: {
                vallox.cO2SetPointHigh = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.CO2SetPointHigh));
            }
            case CO2_SET_POINT_LOWER: {
                vallox.cO2SetPointLow = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.CO2SetPointLow));
            }
            case FAN_SPEED_MAX: {
                vallox.fanSpeedMax = convertFanSpeed(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.FanSpeedMax));
            }
            case FAN_SPEED_MIN: {
                vallox.fanSpeedMin = convertFanSpeed(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.FanSpeedMin));
            }
            case DC_FAN_OUTPUT_ADJUSTMENT: {
                vallox.dCFanOutputAdjustment = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.DCFanOutputAdjustment));
            }
            case DC_FAN_INPUT_ADJUSTMENT: {
                vallox.dCFanInputAdjustment = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.DCFanInputAdjustment));
            }
            case INPUT_FAN_STOP: {
                vallox.inputFanStopThreshold = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.InputFanStopThreshold));
            }
            case HEATING_SET_POINT: {
                vallox.heatingSetPoint = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.HeatingSetPoint));
            }
            case PRE_HEATING_SET_POINT: {
                vallox.preHeatingSetPoint = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.PreHeatingSetPoint));
            }
            case HRC_BYPASS: {
                vallox.hrcBypassThreshold = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.HrcBypassThreshold));
            }
            case CELL_DEFROSTING: {
                vallox.cellDefrostingThreshold = convertTemperature(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.CellDefrostingThreshold));
            }
            case PROGRAM: {
                updateProgram(vallox, listener, value);
            }
            case PROGRAM2: {
                updateProgram2(vallox, listener, value);
            }
            case SERVICE_REMINDER: {
                vallox.serviceReminder = Byte.toUnsignedInt(value);
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.ServiceReminder));
            }
            case UNKNOWN: {
                // do nothing;
            }
            case SUSPEND: {
                // // C02 communication starts: no tx allowed!
                vallox.suspended = true;
            }
            case RESUME: {
                // // C02 communication ends: tx allowed!
                vallox.suspended = false;
            }
            default: {
                logger.debug("Unknown command received: {}", this);
            }
        }
    }

    private void updateIoPortMultiPurpose2(ValloxStore vallox, Collection<ValueChangeListener> listener, byte value) {
        // 1 1 1 1 1 1 1 1 0=0ff 1=on
        // | | | | | | | |
        // | | | | | | | +- 0
        // | | | | | | +--- 1 damper motor position - 0=winter 1=season - readonly
        // | | | | | +----- 2 fault signal relay - 0=open 1=closed - readonly
        // | | | | +------- 3 supply fan - 0=on 1=off
        // | | | +--------- 4 pre-heating - 0=off 1=on - readonly
        // | | +----------- 5 exhaust-fan - 0=on 1=off
        // | +------------- 6 fireplace-booster - 0=open 1=closed - readonly
        // +--------------- 7
        vallox.damperMotorPosition = (value & 0x02) != 0;
        vallox.faultSignalRelayClosed = (value & 0x04) != 0;
        vallox.supplyFanOff = (value & 0x08) != 0;
        vallox.preHeatingOn = (value & 0x10) != 0;
        vallox.exhaustFanOff = (value & 0x20) != 0;
        vallox.firePlaceBoosterClosed = (value & 0x40) != 0;
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.DamperMotorPosition));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.FaultSignalRelayClosed));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.SupplyFanOff));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.PreHeatingOn));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.ExhaustFanOff));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.FirePlaceBoosterClosed));
    }

    private void updateSelect(ValloxStore vallox, Collection<ValueChangeListener> listener, byte value) {
        // 1 1 1 1 1 1 1 1
        // | | | | | | | |
        // | | | | | | | +- 0 Power state
        // | | | | | | +--- 1 CO2 Adjust state
        // | | | | | +----- 2 %RH adjust state
        // | | | | +------- 3 Heating state
        // | | | +--------- 4 Filterguard indicator
        // | | +----------- 5 Heating indicator
        // | +------------- 6 Fault indicator
        // +--------------- 7 service reminder
        vallox.powerState = (value & 0x01) != 0;
        vallox.cO2AdjustState = (value & 0x02) != 0;
        vallox.humidityAdjustState = (value & 0x04) != 0;
        vallox.heatingState = (value & 0x08) != 0;
        vallox.filterGuardIndicator = (value & 0x10) != 0;
        vallox.heatingIndicator = (value & 0x20) != 0;
        vallox.faultIndicator = (value & 0x40) != 0;
        vallox.serviceReminderIndicator = (value & 0x80) != 0;
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.PowerState));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.CO2AdjustState));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.HumidityAdjustState));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.HeatingState));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.FilterGuardIndicator));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.HeatingIndicator));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.FaultIndicator));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.ServiceReminderIndicator));
    }

    private void updateProgram(ValloxStore vallox, Collection<ValueChangeListener> listener, byte value) {
        // 1 1 1 1 1 1 1 1
        // | | | | _______
        // | | | | |
        // | | | | +--- 0-3 set adjustment interval of CO2 and %RH in minutes
        // | | | |
        // | | | |
        // | | | |
        // | | | +--------- 4 automatic RH basic level seeker state
        // | | +----------- 5 boost switch modde (1=boost, 0 = fireplace)
        // | +------------- 6 radiator type 0 = electric, 1 = water
        // +--------------- 7 cascade adjust 0 = off, 1 = on
        vallox.adjustmentIntervalMinutes = value & 0x0F;
        vallox.automaticHumidityLevelSeekerState = (value & 0x10) != 0;
        vallox.boostSwitchMode = (value & 0x20) != 0;
        vallox.radiatorType = (value & 0x40) != 0;
        vallox.cascadeAdjust = (value & 0x80) != 0;
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.AdjustmentIntervalMinutes));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.AutomaticHumidityLevelSeekerState));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.BoostSwitchMode));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.RadiatorType));
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.CascadeAdjust));
    }

    private void updateProgram2(ValloxStore vallox, Collection<ValueChangeListener> listener, byte value) {
        // 1 1 1 1 1 1 1 1
        // | | | | | | | |
        // | | | | | | | +- 0 Function of max speed limit 0 = with adjustment, 1 = always
        // | | | | | | +--- 1
        // | | | | | +----- 2
        // | | | | +------- 3
        // | | | +--------- 4
        // | | +----------- 5
        // | +------------- 6
        // +--------------- 7
        vallox.maxSpeedLimitMode = (value & 0x01) != 0;
        listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.MaxSpeedLimitMode));
    }

    public void updateEfficiencies(ValloxStore vallox, Collection<ValueChangeListener> listener) {
        int maxPossible = vallox.tempInside - vallox.tempOutside;
        if (maxPossible <= 0) {
            vallox.inEfficiency = 100;
            vallox.outEfficiency = 100;
            vallox.averageEfficiency = 100;
        }
        if (maxPossible > 0) {
            double inEfficiency = (vallox.tempIncomming - vallox.tempOutside) * 100.0 / maxPossible;
            if (vallox.inEfficiency != (int) inEfficiency) {
                vallox.inEfficiency = (int) inEfficiency;
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.InEfficiency));
            }

            double outEfficiency = (vallox.tempInside - vallox.tempExhaust) * 100.0 / maxPossible;
            if (vallox.outEfficiency != (int) outEfficiency) {
                vallox.outEfficiency = (int) outEfficiency;
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.OutEfficiency));
            }

            double averageEfficiency = (vallox.inEfficiency + vallox.outEfficiency) / 2;
            if (vallox.averageEfficiency != (int) averageEfficiency) {
                vallox.averageEfficiency = (int) averageEfficiency;
                listener.stream().forEach(l -> l.notifyChanged(ValloxProperty.AverageEfficiency));
            }
        }
    }

    public static byte convertTemperature(byte value) {
        int index = Byte.toUnsignedInt(value);
        return ValloxProtocol.TEMPERATURE_MAPPING[index];
    }

    public static byte convertBackTemperature(byte temperature) {
        byte value = 100;

        for (int i = 0; i < 255; i++) {
            byte valueFromTable = ValloxProtocol.TEMPERATURE_MAPPING[i];
            if (valueFromTable >= temperature) {
                value = (byte) i;
                break;
            }
        }

        return value;
    }

    /**
     * Convert a speed number from 1 to 8 to its hex telegram command.
     * 8 --> 0xFF
     *
     * @param value 1-8
     * @return
     */
    public static byte convertBackFanSpeed(byte value) {
        return ValloxProtocol.FAN_SPEED_MAPPING[value - 1];
    }

    // 0xFF --> 8
    /**
     * Convert a hex telegram command value to its speed number from 1 to 8.
     *
     * @param value
     * @return 1-8
     */
    public static int convertFanSpeed(byte value) {
        int fanSpeed = 0;

        for (byte i = 0; i < 8; i++) {
            if (ValloxProtocol.FAN_SPEED_MAPPING[i] == value) {
                fanSpeed = (byte) (i + 1);
                break;
            }
        }
        return fanSpeed;
    }

    // 1 1 1 1 1 1 1 1
    // | | | | | | | |
    // | | | | | | | +- 0
    // | | | | | | +--- 1
    // | | | | | +----- 2
    // | | | | +------- 3
    // | | | +--------- 4
    // | | +----------- 5 post-heating on - 0=0ff 1=on - readonly
    // | +------------- 6
    // +--------------- 7
    public static boolean convertIoPortMultiPurpose1(byte value) {
        return (value & 0x20) != 0;
    }

    protected static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Get a human readable string-representation in hex for an input
     * byte.
     *
     * @param byte
     * @return
     */
    public static String byteToHex(byte b) {
        int v = b & 0xFF;
        char c1 = HEX_ARRAY[v >>> 4];
        char c2 = HEX_ARRAY[v & 0x0F];
        return "" + c1 + c2;
    }

    /**
     * Get a human readable string-representation in hex for an input
     * binary array of bytes.
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public String toString() {
        String variableString = commandVar.toString();
        return String.format("Telegram [sender=%s, receiver=%s, command=%s, arg=%s, variable=%s]", byteToHex(sender),
                byteToHex(receiver), byteToHex(commandVar.getKey()), byteToHex(arg), variableString);
    }

}
