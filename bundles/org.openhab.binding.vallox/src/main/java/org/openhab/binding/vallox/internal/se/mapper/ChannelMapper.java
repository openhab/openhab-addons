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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ChannelMapper} maps all channel information together.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class ChannelMapper {

    private static final String CHANNEL_GROUP_FAN = "fanControl#";
    private static final String CHANNEL_GROUP_TEMPERATURE = "temperature#";
    private static final String CHANNEL_GROUP_EFFICIENCY = "efficiency#";
    private static final String CHANNEL_GROUP_SETTINGS = "setting#";
    private static final String CHANNEL_GROUP_STATUS = "status#";
    private static final String CHANNEL_GROUP_MAINTENANCE = "maintenance#";
    private static final String CHANNEL_GROUP_ALARM = "alarm#";

    @SuppressWarnings("serial")
    private static final Map<String, ValloxChannel> VALLOXSE = Collections
            .unmodifiableMap(new HashMap<String, ValloxChannel>() {
                {
            // @formatter:off
                    // FanControls
                    put(CHANNEL_GROUP_FAN + "fanSpeed", new FanChannel((byte) 0x29));
                    put(CHANNEL_GROUP_FAN + "fanSpeedMax", new FanChannel((byte) 0xA5));
                    put(CHANNEL_GROUP_FAN + "fanSpeedMin", new FanChannel((byte) 0xA9));
                    put(CHANNEL_GROUP_FAN + "dcFanInputAdjustment", new IntegerChannel((byte) 0xB0));
                    put(CHANNEL_GROUP_FAN + "dcFanOutputAdjustment", new IntegerChannel((byte) 0xB1));
                    put(CHANNEL_GROUP_FAN + "supplyFanState", new BooleanChannel((byte) 0x08, "ioPortMultiPurpose2"));
                    put(CHANNEL_GROUP_FAN + "exhaustFanState", new BooleanChannel((byte) 0x20, "ioPortMultiPurpose2"));

                    // Temperatures
                    put(CHANNEL_GROUP_TEMPERATURE + "tempInside", new TemperatureChannel((byte) 0x34));
                    put(CHANNEL_GROUP_TEMPERATURE + "tempOutside", new TemperatureChannel((byte) 0x32));
                    put(CHANNEL_GROUP_TEMPERATURE + "tempExhaust", new TemperatureChannel((byte) 0x33));
                    put(CHANNEL_GROUP_TEMPERATURE + "tempIncoming", new TemperatureChannel((byte) 0x35));

                    // Efficiencies
                    put(CHANNEL_GROUP_EFFICIENCY + "inEfficiency", new IntegerChannel((byte) 0x00));
                    put(CHANNEL_GROUP_EFFICIENCY + "outEfficiency", new IntegerChannel((byte) 0x00));
                    put(CHANNEL_GROUP_EFFICIENCY + "averageEfficiency", new IntegerChannel((byte) 0x00));

                    // Settings
                    put(CHANNEL_GROUP_SETTINGS + "powerState", new BooleanChannel((byte) 0x01, "select"));
                    put(CHANNEL_GROUP_SETTINGS + "co2AdjustState", new BooleanChannel((byte) 0x02, "select"));
                    put(CHANNEL_GROUP_SETTINGS + "humidityAdjustState", new BooleanChannel((byte) 0x04, "select"));
                    put(CHANNEL_GROUP_SETTINGS + "postHeatingState", new BooleanChannel((byte) 0x08, "select"));
                    put(CHANNEL_GROUP_SETTINGS + "hrcBypassThreshold", new TemperatureChannel((byte) 0xAF));
                    put(CHANNEL_GROUP_SETTINGS + "inputFanStopThreshold", new TemperatureChannel((byte) 0xA8));
                    put(CHANNEL_GROUP_SETTINGS + "postHeatingSetPoint", new TemperatureChannel((byte) 0xA4));
                    put(CHANNEL_GROUP_SETTINGS + "preHeatingSetPoint", new TemperatureChannel((byte) 0xA7));
                    put(CHANNEL_GROUP_SETTINGS + "postHeatingOnCounter", new IntegerChannel.Counter((byte) 0x55));
                    put(CHANNEL_GROUP_SETTINGS + "postHeatingOffCounter", new IntegerChannel.Counter((byte) 0x56));
                    put(CHANNEL_GROUP_SETTINGS + "co2SetPoint", new IntegerChannel((byte) 0x00));
                    put(CHANNEL_GROUP_SETTINGS + "co2SetPointHigh", new IntegerChannel((byte) 0xB3));
                    put(CHANNEL_GROUP_SETTINGS + "co2SetPointLow", new IntegerChannel((byte) 0xB4));
                    put(CHANNEL_GROUP_SETTINGS + "cascadeAdjust", new BooleanChannel((byte) 0x80, "program1"));
                    put(CHANNEL_GROUP_SETTINGS + "adjustmentIntervalMinutes", new BooleanChannel.AdjustmentInterval((byte) 0x0F, "program1"));
                    put(CHANNEL_GROUP_SETTINGS + "maxSpeedLimitMode", new BooleanChannel((byte) 0x01, "program2"));
                    put(CHANNEL_GROUP_SETTINGS + "basicHumidityLevel", new IntegerChannel.Humidity((byte) 0xAE));
                    put(CHANNEL_GROUP_SETTINGS + "boostSwitchMode", new BooleanChannel((byte) 0x20, "program1"));
                    put(CHANNEL_GROUP_SETTINGS + "radiatorType", new BooleanChannel((byte) 0x40, "program1"));
                    put(CHANNEL_GROUP_SETTINGS + "activateFirePlaceBooster", new BooleanChannel((byte) 0x20, "flags6"));
                    put(CHANNEL_GROUP_SETTINGS + "automaticHumidityLevelSeekerState", new BooleanChannel((byte) 0x10, "program1"));
                    put(CHANNEL_GROUP_SETTINGS + "preHeatingState", new BooleanChannel((byte) 0x80, "flags5"));

                    // Status
                    put(CHANNEL_GROUP_STATUS + "humidity", new IntegerChannel.Humidity((byte) 0x2A));
                    put(CHANNEL_GROUP_STATUS + "humiditySensor1", new IntegerChannel.Humidity((byte) 0x2F));
                    put(CHANNEL_GROUP_STATUS + "humiditySensor2", new IntegerChannel.Humidity((byte) 0x30));
                    put(CHANNEL_GROUP_STATUS + "co2", new IntegerChannel((byte) 0x00));
                    put(CHANNEL_GROUP_STATUS + "co2High", new IntegerChannel((byte) 0x2B));
                    put(CHANNEL_GROUP_STATUS + "co2Low", new IntegerChannel((byte) 0x2C));
                    put(CHANNEL_GROUP_STATUS + "postHeatingIndicator", new BooleanChannel((byte) 0x20, "select"));
                    put(CHANNEL_GROUP_STATUS + "installedCo2Sensors", new StringChannel((byte) 0x2D));
                    put(CHANNEL_GROUP_STATUS + "preHeatingOn", new BooleanChannel((byte) 0x10, "ioPortMultiPurpose2"));
                    put(CHANNEL_GROUP_STATUS + "postHeatingOn", new BooleanChannel((byte) 0x20, "ioPortMultiPurpose1"));
                    put(CHANNEL_GROUP_STATUS + "damperMotorPosition", new BooleanChannel((byte) 0x02, "ioPortMultiPurpose2"));
                    put(CHANNEL_GROUP_STATUS + "firePlaceBoosterSwitch", new BooleanChannel((byte) 0x40, "ioPortMultiPurpose2"));
                    put(CHANNEL_GROUP_STATUS + "incomingCurrent", new IntegerChannel((byte) 0x2E));
                    put(CHANNEL_GROUP_STATUS + "slaveMasterIndicator", new BooleanChannel((byte) 0x80, "flags4"));
                    put(CHANNEL_GROUP_STATUS + "postHeatingTargetValue", new TemperatureChannel((byte) 0x57));
                    put(CHANNEL_GROUP_STATUS + "firePlaceBoosterOn", new BooleanChannel((byte) 0x40, "flags6"));
                    put(CHANNEL_GROUP_STATUS + "firePlaceBoosterCounter", new IntegerChannel((byte) 0x79));
                    put(CHANNEL_GROUP_STATUS + "remoteControlOn", new BooleanChannel((byte) 0x10, "flags6"));

                    // Maintenance
                    put(CHANNEL_GROUP_MAINTENANCE + "filterGuardIndicator", new BooleanChannel((byte) 0x10, "select"));
                    put(CHANNEL_GROUP_MAINTENANCE + "serviceReminderIndicator", new BooleanChannel((byte) 0x80, "select"));
                    put(CHANNEL_GROUP_MAINTENANCE + "maintenanceMonthCounter", new IntegerChannel((byte) 0xAB));
                    put(CHANNEL_GROUP_MAINTENANCE + "serviceReminder", new IntegerChannel((byte) 0xA6));

                    // Alarm
                    put(CHANNEL_GROUP_ALARM + "faultIndicator", new BooleanChannel((byte) 0x40, "select"));
                    put(CHANNEL_GROUP_ALARM + "faultSignalRelayClosed", new BooleanChannel((byte) 0x04, "ioPortMultiPurpose2"));
                    put(CHANNEL_GROUP_ALARM + "co2Alarm", new BooleanChannel((byte) 0x40, "flags2"));
                    put(CHANNEL_GROUP_ALARM + "hrcFreezingAlarm", new BooleanChannel((byte) 0x80, "flags2"));
                    put(CHANNEL_GROUP_ALARM + "waterRadiatorFreezingAlarm", new BooleanChannel((byte) 0x10, "flags4"));
                    put(CHANNEL_GROUP_ALARM + "lastErrorNumber", new IntegerChannel((byte) 0x36));

                    // Multiple value channels
                    put("ioPortMultiPurpose1", new MultipleValueChannel((byte) 0x07, Arrays.asList(CHANNEL_GROUP_STATUS + "postHeatingOn")));

                    put("ioPortMultiPurpose2", new MultipleValueChannel((byte) 0x08, Arrays.asList(CHANNEL_GROUP_STATUS + "damperMotorPosition",
                                                                                                   CHANNEL_GROUP_ALARM  + "faultSignalRelayClosed",
                                                                                                   CHANNEL_GROUP_FAN    + "supplyFanState",
                                                                                                   CHANNEL_GROUP_STATUS + "preHeatingOn",
                                                                                                   CHANNEL_GROUP_FAN    + "exhaustFanState",
                                                                                                   CHANNEL_GROUP_STATUS + "firePlaceBoosterSwitch")));
                    put("flags2", new MultipleValueChannel((byte) 0x6D, Arrays.asList(CHANNEL_GROUP_ALARM + "co2Alarm",
                                                                                      CHANNEL_GROUP_ALARM + "hrcFreezingAlarm")));

                    put("flags4", new MultipleValueChannel((byte) 0x6F, Arrays.asList(CHANNEL_GROUP_ALARM  + "waterRadiatorFreezingAlarm",
                                                                                      CHANNEL_GROUP_STATUS + "slaveMasterIndicator")));

                    put("flags5", new MultipleValueChannel((byte) 0x70, Arrays.asList(CHANNEL_GROUP_SETTINGS + "preHeatingState")));

                    put("flags6", new MultipleValueChannel((byte) 0x71, Arrays.asList(CHANNEL_GROUP_STATUS   + "remoteControlOn",
                                                                                      CHANNEL_GROUP_SETTINGS + "activateFirePlaceBooster",
                                                                                      CHANNEL_GROUP_STATUS   + "firePlaceBoosterOn")));

                    put("select", new MultipleValueChannel((byte) 0xA3, Arrays.asList(CHANNEL_GROUP_SETTINGS + "powerState",
                                                                                      CHANNEL_GROUP_SETTINGS + "co2AdjustState",
                                                                                      CHANNEL_GROUP_SETTINGS + "humidityAdjustState",
                                                                                      CHANNEL_GROUP_SETTINGS + "postHeatingState",
                                                                                      CHANNEL_GROUP_MAINTENANCE + "filterGuardIndicator",
                                                                                      CHANNEL_GROUP_STATUS      + "postHeatingIndicator",
                                                                                      CHANNEL_GROUP_ALARM       + "faultIndicator",
                                                                                      CHANNEL_GROUP_MAINTENANCE + "serviceReminderIndicator")));

                    put("program1", new MultipleValueChannel((byte) 0xAA, Arrays.asList(CHANNEL_GROUP_SETTINGS + "adjustmentIntervalMinutes",
                                                                                        CHANNEL_GROUP_SETTINGS + "automaticHumidityLevelSeekerState",
                                                                                        CHANNEL_GROUP_SETTINGS + "boostSwitchMode",
                                                                                        CHANNEL_GROUP_SETTINGS + "radiatorType",
                                                                                        CHANNEL_GROUP_SETTINGS + "cascadeAdjust")));

                    put("program2", new MultipleValueChannel((byte) 0xB5, Arrays.asList(CHANNEL_GROUP_SETTINGS + "maxSpeedLimitMode")));
                    // @formatter:on
                }
            });

    /**
     * Get {@link ValloxChannel}
     */
    public static ValloxChannel getValloxChannel(String key) {
        return VALLOXSE.get(key);
    }

    /**
     * Get variable as byte for channel
     */
    @SuppressWarnings({ "null", "unused" })
    public static byte getVariable(String key) {
        ValloxChannel vc = getValloxChannel(key);
        if (vc == null) {
            return (byte) 0x00;
        }
        return vc.getVariable();
    }

    /**
     * Get channel for variable
     */
    public static String getChannelForVariable(byte variable) {
        for (String key : VALLOXSE.keySet()) {
            ValloxChannel vc = VALLOXSE.get(key);
            if ((vc.getVariable() == variable) && !(vc instanceof BooleanChannel)) {
                return key;
            }
        }
        return "";
    }
}
