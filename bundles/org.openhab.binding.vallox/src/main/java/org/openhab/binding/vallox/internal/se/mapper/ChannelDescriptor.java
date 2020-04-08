/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vallox.internal.se.mapper;

import static org.openhab.binding.vallox.internal.se.ValloxSEConstants.*;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link ChannelDescriptor} combines all channel information together.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public enum ChannelDescriptor {

    // @formatter:off
    // Null descriptor for error situations
    NULL ("Null", new IntegerChannel(0x00)),

    // FanControls
    FAN_SPEED               (CHANNEL_GROUP_FAN + "fanSpeed", new FanChannel(0x29)),
    FAN_SPEED_MAX           (CHANNEL_GROUP_FAN + "fanSpeedMax", new FanChannel(0xA5)),
    FAN_SPEED_MIN           (CHANNEL_GROUP_FAN + "fanSpeedMin", new FanChannel(0xA9)),
    DC_FAN_INPUT_ADJUSTMENT (CHANNEL_GROUP_FAN + "dcFanInputAdjustment", new IntegerChannel(0xB0)),
    DC_FAN_OUTPUT_ADJUSTMENT(CHANNEL_GROUP_FAN + "dcFanOutputAdjustment", new IntegerChannel(0xB1)),
    SUPPLY_FAN_STATE        (CHANNEL_GROUP_FAN + "supplyFanState", new BooleanChannel(0x08, "ioPortMultiPurpose2")),
    EXHAUST_FAN_STATE       (CHANNEL_GROUP_FAN + "exhaustFanState", new BooleanChannel(0x20, "ioPortMultiPurpose2")),

    // Temperatures
    TEMPERATURE_INSIDE  (CHANNEL_GROUP_TEMPERATURE + "tempInside", new TemperatureChannel(0x34)),
    TEMPERATURE_OUTSIDE (CHANNEL_GROUP_TEMPERATURE + "tempOutside", new TemperatureChannel(0x32)),
    TEMPERATURE_EXHAUST (CHANNEL_GROUP_TEMPERATURE + "tempExhaust", new TemperatureChannel(0x33)),
    TEMPERATURE_INCOMING(CHANNEL_GROUP_TEMPERATURE + "tempIncoming", new TemperatureChannel(0x35)),

    // Efficiencies
    EFFICIENCY_IN       (CHANNEL_GROUP_EFFICIENCY + "inEfficiency", new IntegerChannel(0x00)),
    EFFICIENCY_OUT      (CHANNEL_GROUP_EFFICIENCY + "outEfficiency", new IntegerChannel(0x00)),
    EFFICIENCY_AVERAGE  (CHANNEL_GROUP_EFFICIENCY + "averageEfficiency", new IntegerChannel(0x00)),

    // Settings
    POWER_STATE             (CHANNEL_GROUP_SETTINGS + "powerState", new BooleanChannel(0x01, "select")),
    CO2_ADJUST_STATE        (CHANNEL_GROUP_SETTINGS + "co2AdjustState", new BooleanChannel(0x02, "select")),
    HUMIDITY_ADJUST_STATE   (CHANNEL_GROUP_SETTINGS + "humidityAdjustState", new BooleanChannel(0x04, "select")),
    POST_HEATING_STATE      (CHANNEL_GROUP_SETTINGS + "postHeatingState", new BooleanChannel(0x08, "select")),
    HRC_BYPASS_THRESHOLD    (CHANNEL_GROUP_SETTINGS + "hrcBypassThreshold", new TemperatureChannel(0xAF)),
    INPUT_FAN_STOP_THRESHOLD(CHANNEL_GROUP_SETTINGS + "inputFanStopThreshold", new TemperatureChannel(0xA8)),
    POST_HEATING_SETPOINT   (CHANNEL_GROUP_SETTINGS + "postHeatingSetPoint", new TemperatureChannel(0xA4)),
    PRE_HEATING_SETPOINT    (CHANNEL_GROUP_SETTINGS + "preHeatingSetPoint", new TemperatureChannel(0xA7)),
    POST_HEATING_ON_COUNTER (CHANNEL_GROUP_SETTINGS + "postHeatingOnCounter", new IntegerChannel.Counter(0x55)),
    POST_HEATING_OFF_COUNTER(CHANNEL_GROUP_SETTINGS + "postHeatingOffCounter", new IntegerChannel.Counter(0x56)),
    CO2_SETPOINT            (CHANNEL_GROUP_SETTINGS + "co2SetPoint", new IntegerChannel(0x00)),
    CO2_SETPOINT_HIGH       (CHANNEL_GROUP_SETTINGS + "co2SetPointHigh", new IntegerChannel(0xB3)),
    CO2_SETPOINT_LOW        (CHANNEL_GROUP_SETTINGS + "co2SetPointLow", new IntegerChannel(0xB4)),
    CASCADE_ADJUST          (CHANNEL_GROUP_SETTINGS + "cascadeAdjust", new BooleanChannel(0x80, "program1")),
    ADJUSTMENT_INTERVAL     (CHANNEL_GROUP_SETTINGS + "adjustmentInterval", new BooleanChannel.AdjustmentInterval(0x0F, "program1")),
    MAX_SPEED_LIMIT_MODE    (CHANNEL_GROUP_SETTINGS + "maxSpeedLimitMode", new BooleanChannel(0x01, "program2")),
    BASIC_HUMIDITY_LEVEL    (CHANNEL_GROUP_SETTINGS + "basicHumidityLevel", new IntegerChannel.Humidity(0xAE)),
    BOOST_SWITCH_MODE       (CHANNEL_GROUP_SETTINGS + "boostSwitchMode", new BooleanChannel(0x20, "program1")),
    RADIATOR_TYPE           (CHANNEL_GROUP_SETTINGS + "radiatorType", new BooleanChannel(0x40, "program1")),
    ACTIVATE_FIREPLACE_BOOSTER              (CHANNEL_GROUP_SETTINGS + "activateFirePlaceBooster", new BooleanChannel(0x20, "flags6")),
    AUTOMATIC_HUMIDITY_LEVEL_SEEKER_STATE   (CHANNEL_GROUP_SETTINGS + "automaticHumidityLevelSeekerState", new BooleanChannel(0x10, "program1")),
    PRE_HEATING_STATE                       (CHANNEL_GROUP_SETTINGS + "preHeatingState", new BooleanChannel(0x80, "flags5")),

    // Status
    HIMIDITY            (CHANNEL_GROUP_STATUS + "humidity", new IntegerChannel.Humidity(0x2A)),
    HUMIDITY_SENSOR_1   (CHANNEL_GROUP_STATUS + "humiditySensor1", new IntegerChannel.Humidity(0x2F)),
    HUMIDITY_SENSOR_2   (CHANNEL_GROUP_STATUS + "humiditySensor2", new IntegerChannel.Humidity(0x30)),
    CO2                 (CHANNEL_GROUP_STATUS + "co2", new IntegerChannel(0x00)),
    CO2_HIGH            (CHANNEL_GROUP_STATUS + "co2High", new IntegerChannel(0x2B)),
    CO2_LOW             (CHANNEL_GROUP_STATUS + "co2Low", new IntegerChannel(0x2C)),
    POST_HEATING_INDICATOR  (CHANNEL_GROUP_STATUS + "postHeatingIndicator", new BooleanChannel(0x20, "select")),
    INSTALLED_CO2_SENSRS    (CHANNEL_GROUP_STATUS + "installedCo2Sensors", new StringChannel(0x2D)),
    PRE_HEATING_ON          (CHANNEL_GROUP_STATUS + "preHeatingOn", new BooleanChannel(0x10, "ioPortMultiPurpose2")),
    POST_HEATING_ON         (CHANNEL_GROUP_STATUS + "postHeatingOn", new BooleanChannel(0x20, "ioPortMultiPurpose1")),
    DAMPER_MOTOR_POSITION   (CHANNEL_GROUP_STATUS + "damperMotorPosition", new BooleanChannel(0x02, "ioPortMultiPurpose2")),
    FIREPLACE_BOOSTER_SWITCH(CHANNEL_GROUP_STATUS + "firePlaceBoosterSwitch", new BooleanChannel(0x40, "ioPortMultiPurpose2")),
    ICOMING_CURRENT         (CHANNEL_GROUP_STATUS + "incomingCurrent", new IntegerChannel(0x2E)),
    SLAVE_MASTER_INDICATOR  (CHANNEL_GROUP_STATUS + "slaveMasterIndicator", new BooleanChannel(0x80, "flags4")),
    POST_HEATING_TARGET_VALUE(CHANNEL_GROUP_STATUS + "postHeatingTargetValue", new TemperatureChannel(0x57)),
    FIREPLACE_BOOSTER_ON     (CHANNEL_GROUP_STATUS + "firePlaceBoosterOn", new BooleanChannel(0x40, "flags6")),
    FIREPLACE_BOOSTER_COUNTER(CHANNEL_GROUP_STATUS + "firePlaceBoosterCounter", new IntegerChannel.Counter(0x79)),
    REMOTE_CONTROL_ON        (CHANNEL_GROUP_STATUS + "remoteControlOn", new BooleanChannel(0x10, "flags6")),

    // Maintenance
    FILTER_GUARD_INDICATOR      (CHANNEL_GROUP_MAINTENANCE + "filterGuardIndicator", new BooleanChannel(0x10, "select")),
    SERVICE_REMINDER_INDICATOR  (CHANNEL_GROUP_MAINTENANCE + "serviceReminderIndicator", new BooleanChannel(0x80, "select")),
    MAINTENANCE_MONTH_COUNTER   (CHANNEL_GROUP_MAINTENANCE + "maintenanceMonthCounter", new IntegerChannel.Counter(0xAB)),
    SERVICE_REMINDER            (CHANNEL_GROUP_MAINTENANCE + "serviceReminder", new IntegerChannel(0xA6)),

    // Alarm
    FAULT_INDICATOR              (CHANNEL_GROUP_ALARM + "faultIndicator", new BooleanChannel(0x40, "select")),
    FAULT_SIGNAL_RELAY_CLOSED    (CHANNEL_GROUP_ALARM + "faultSignalRelayClosed", new BooleanChannel(0x04, "ioPortMultiPurpose2")),
    CO2_ALARM                    (CHANNEL_GROUP_ALARM + "co2Alarm", new BooleanChannel(0x40, "flags2")),
    HRC_FREEZING_ALARM           (CHANNEL_GROUP_ALARM + "hrcFreezingAlarm", new BooleanChannel(0x80, "flags2")),
    WATER_RADIATOR_FREEZING_ALARM(CHANNEL_GROUP_ALARM + "waterRadiatorFreezingAlarm", new BooleanChannel(0x10, "flags4")),
    LAST_ERROR_NUMBER            (CHANNEL_GROUP_ALARM + "lastErrorNumber", new IntegerChannel(0x36)),

    // Multiple value channels
    IO_MULTIPURPOSE_1("ioPortMultiPurpose1", new MultipleValueChannel(0x07, Arrays.asList(CHANNEL_GROUP_STATUS + "postHeatingOn"))),

    IO_MULTIPURPOSE_2("ioPortMultiPurpose2", new MultipleValueChannel(0x08, Arrays.asList(CHANNEL_GROUP_STATUS + "damperMotorPosition",
                                                                                                 CHANNEL_GROUP_ALARM  + "faultSignalRelayClosed",
                                                                                                 CHANNEL_GROUP_FAN    + "supplyFanState",
                                                                                                 CHANNEL_GROUP_STATUS + "preHeatingOn",
                                                                                                 CHANNEL_GROUP_FAN    + "exhaustFanState",
                                                                                                 CHANNEL_GROUP_STATUS + "firePlaceBoosterSwitch"))),
    FLAGS_2("flags2", new MultipleValueChannel(0x6D, Arrays.asList(CHANNEL_GROUP_ALARM + "co2Alarm",
                                                                          CHANNEL_GROUP_ALARM + "hrcFreezingAlarm"))),

    FLAGS_4("flags4", new MultipleValueChannel(0x6F, Arrays.asList(CHANNEL_GROUP_ALARM  + "waterRadiatorFreezingAlarm",
                                                                          CHANNEL_GROUP_STATUS + "slaveMasterIndicator"))),

    FLAGS_5("flags5", new MultipleValueChannel(0x70, Arrays.asList(CHANNEL_GROUP_SETTINGS + "preHeatingState"))),

    FLAGS_6("flags6", new MultipleValueChannel(0x71, Arrays.asList(CHANNEL_GROUP_STATUS   + "remoteControlOn",
                                                                          CHANNEL_GROUP_SETTINGS + "activateFirePlaceBooster",
                                                                          CHANNEL_GROUP_STATUS   + "firePlaceBoosterOn"))),

    SELECT("select", new MultipleValueChannel(0xA3, Arrays.asList(CHANNEL_GROUP_SETTINGS + "powerState",
                                                                          CHANNEL_GROUP_SETTINGS + "co2AdjustState",
                                                                          CHANNEL_GROUP_SETTINGS + "humidityAdjustState",
                                                                          CHANNEL_GROUP_SETTINGS + "postHeatingState",
                                                                          CHANNEL_GROUP_MAINTENANCE + "filterGuardIndicator",
                                                                          CHANNEL_GROUP_STATUS      + "postHeatingIndicator",
                                                                          CHANNEL_GROUP_ALARM       + "faultIndicator",
                                                                          CHANNEL_GROUP_MAINTENANCE + "serviceReminderIndicator"))),

    PROGRAM_1("program1", new MultipleValueChannel(0xAA, Arrays.asList(CHANNEL_GROUP_SETTINGS + "adjustmentIntervalMinutes",
                                                                              CHANNEL_GROUP_SETTINGS + "automaticHumidityLevelSeekerState",
                                                                              CHANNEL_GROUP_SETTINGS + "boostSwitchMode",
                                                                              CHANNEL_GROUP_SETTINGS + "radiatorType",
                                                                              CHANNEL_GROUP_SETTINGS + "cascadeAdjust"))),

    PROGRAM_2("program2", new MultipleValueChannel(0xB5, Arrays.asList(CHANNEL_GROUP_SETTINGS + "maxSpeedLimitMode")));
    // @formatter:on

    private final String channelID;
    private final ValloxChannel valloxChannel;

    private ChannelDescriptor(String channelID, ValloxChannel valloxChannel) {
        this.channelID = channelID;
        this.valloxChannel = valloxChannel;
    }

    public String channelID() {
        return channelID;
    }

    public ValloxChannel getValloxChannel() {
        return valloxChannel;
    }

    public static ChannelDescriptor get(String channelID) {
        for (ChannelDescriptor channelDescriptor : values()) {
            if (channelDescriptor.channelID.equals(channelID)) {
                return channelDescriptor;
            }
        }
        return ChannelDescriptor.NULL;
    }

    public static ChannelDescriptor get(byte channelAsByte) {
        for (ChannelDescriptor channelDescriptor : values()) {
            if (channelDescriptor.valloxChannel.variable == channelAsByte) {
                return channelDescriptor;
            }
        }
        return ChannelDescriptor.NULL;
    }

    public static ChannelDescriptor getParentChannel(ChannelDescriptor descriptor) {
        for (ChannelDescriptor channelDescriptor : values()) {
            if (channelDescriptor.equals(descriptor)) {
                return ChannelDescriptor.get(channelDescriptor.getParentChannel());
            }
        }
        return ChannelDescriptor.NULL;
    }

    public byte getVariable() {
        return valloxChannel.getVariable();
    }

    public State convertToState(byte value) {
        return valloxChannel.convertToState(value);
    }

    public String getParentChannel() {
        return valloxChannel.getParentChannel();
    }

    public byte convertFromState(byte value) {
        return valloxChannel.convertFromState(value);
    }

    public Collection<String> getSubChannels() {
        return valloxChannel.getSubChannels();
    }
}
