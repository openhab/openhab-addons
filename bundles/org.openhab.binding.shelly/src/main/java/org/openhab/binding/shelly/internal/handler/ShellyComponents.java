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

package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyUtils.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJson.SHELLY_TEMP_CELSIUS;

import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;

/***
 * The{@link ShellyComponents} implements updates for supplemental components
 * Meter will be used by Relay + Light
 * Sensor is also part of Flood, Sense
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyComponents {
    /**
     * Update Meter channel
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     */
    @SuppressWarnings("null")
    public static boolean updateMeters(ShellyBaseHandler th, ShellySettingsStatus status) {
        Validate.notNull(th);
        ShellyDeviceProfile profile = th.getProfile();

        boolean updated = false;
        if (profile.hasMeter && ((status.meters != null) || (status.emeters != null))) {
            if (!profile.isRoller) {
                th.logger.trace("{}: Updating {} {}meter(s)", th.thingName, profile.numMeters,
                        !profile.isEMeter ? "standard " : "e-");

                // In Relay mode we map eacher meter to the matching channel group
                int m = 0;
                if (!profile.isEMeter) {
                    for (ShellySettingsMeter meter : status.meters) {
                        Integer meterIndex = m + 1;
                        if (getBool(meter.is_valid) || profile.isLight) { // RGBW2-white doesn't report das flag
                                                                          // correctly in white mode
                            String groupName = "";
                            if (profile.numMeters > 1) {
                                groupName = CHANNEL_GROUP_METER + meterIndex.toString();
                            } else {
                                groupName = CHANNEL_GROUP_METER;
                            }
                            updated |= th.updateChannel(groupName, CHANNEL_METER_CURRENTWATTS, getDecimal(meter.power));
                            if (meter.total != null) {
                                updated |= th.updateChannel(groupName, CHANNEL_METER_TOTALKWH,
                                        getDecimal(getDouble(meter.total) / (60.0 * 1000.0))); // convert
                                // Watt/Min to
                                // kw/h
                            }
                            if (meter.counters != null) {
                                updated |= th.updateChannel(groupName, CHANNEL_METER_LASTMIN1,
                                        getDecimal(meter.counters[0]));
                                updated |= th.updateChannel(groupName, CHANNEL_METER_LASTMIN2,
                                        getDecimal(meter.counters[1]));
                                updated |= th.updateChannel(groupName, CHANNEL_METER_LASTMIN3,
                                        getDecimal(meter.counters[2]));
                            }
                            updated |= th.updateChannel(groupName, CHANNEL_METER_TIMESTAMP,
                                    new StringType(ShellyHandlerFactory.convertTimestamp(getLong(meter.timestamp))));
                            m++;
                        }
                    }
                } else {
                    for (ShellySettingsEMeter emeter : status.emeters) {
                        Integer meterIndex = m + 1;
                        if (emeter.is_valid) {
                            String groupName = profile.numMeters > 1 ? CHANNEL_GROUP_METER + meterIndex.toString()
                                    : CHANNEL_GROUP_METER;
                            if (emeter.is_valid) {
                                // convert Watt/Hour tok w/h
                                updated |= th.updateChannel(groupName, CHANNEL_METER_CURRENTWATTS,
                                        getDecimal(emeter.power));
                                updated |= th.updateChannel(groupName, CHANNEL_METER_TOTALKWH,
                                        new DecimalType(getDouble(emeter.total) / 1000));
                                updated |= th.updateChannel(groupName, CHANNEL_EMETER_TOTALRET,
                                        new DecimalType(getDouble(emeter.total_returned) / 1000));
                                updated |= th.updateChannel(groupName, CHANNEL_EMETER_REACTWATTS,
                                        getDecimal(emeter.reactive));
                                updated |= th.updateChannel(groupName, CHANNEL_EMETER_VOLTAGE,
                                        getDecimal(emeter.voltage));
                            }
                            m++;
                        }
                    }
                }
            } else {
                // In Roller Mode we accumulate all meters to a single set of meters
                th.logger.trace("{}: Updating roller meter", th.thingName);
                Double currentWatts = 0.0;
                Double totalWatts = 0.0;
                Double lastMin1 = 0.0;
                Double lastMin2 = 0.0;
                Double lastMin3 = 0.0;
                Long timestamp = 0l;
                String groupName = CHANNEL_GROUP_METER;
                for (ShellySettingsMeter meter : status.meters) {
                    if (meter.is_valid) {
                        currentWatts += getDouble(meter.power);
                        totalWatts += getDouble(meter.total);
                        if (meter.counters != null) {
                            lastMin1 += getDouble(meter.counters[0]);
                            lastMin2 += getDouble(meter.counters[1]);
                            lastMin3 += getDouble(meter.counters[2]);
                        }
                        if (getLong(meter.timestamp) > timestamp) {
                            timestamp = getLong(meter.timestamp);
                        }
                    }
                }
                updated |= th.updateChannel(groupName, CHANNEL_METER_LASTMIN1, new DecimalType(lastMin1));
                updated |= th.updateChannel(groupName, CHANNEL_METER_LASTMIN2, new DecimalType(lastMin2));
                updated |= th.updateChannel(groupName, CHANNEL_METER_LASTMIN3, new DecimalType(lastMin3));

                // convert totalWatts into kw/h
                totalWatts = totalWatts / (60.0 * 10000.0);
                updated |= th.updateChannel(groupName, CHANNEL_METER_CURRENTWATTS, new DecimalType(currentWatts));
                updated |= th.updateChannel(groupName, CHANNEL_METER_TOTALKWH, new DecimalType(totalWatts));
                updated |= th.updateChannel(groupName, CHANNEL_METER_TIMESTAMP,
                        new StringType(ShellyHandlerFactory.convertTimestamp(timestamp)));
            }
        }
        return updated;
    }

    /**
     * Update Sensor channel
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     *
     * @throws IOException
     */
    @SuppressWarnings("null")
    public static boolean updateSensors(ShellyBaseHandler th, ShellySettingsStatus status) throws IOException {
        Validate.notNull(th);
        ShellyDeviceProfile profile = th.getProfile();

        boolean updated = false;
        if (profile.isSensor || profile.hasBattery) {
            th.logger.debug("{}: Updating sensor", th.thingName);
            ShellyStatusSensor sdata = th.api.getSensorStatus();
            if (sdata != null) {
                if (getBool(sdata.tmp.is_valid)) {
                    updated |= th.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP,
                            getString(sdata.tmp.units).toUpperCase().equals(SHELLY_TEMP_CELSIUS)
                                    ? getDecimal(sdata.tmp.tC)
                                    : getDecimal(sdata.tmp.tF));
                    updated |= th.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TUNIT,
                            getStringType(sdata.tmp.units));
                    updated |= th.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_HUM, getDecimal(sdata.hum.value));
                }
                if ((sdata.lux != null) && getBool(sdata.lux.is_valid)) {
                    updated |= th.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_LUX, getDecimal(sdata.lux.value));
                }
                if (sdata.flood != null) {
                    updated |= th.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD, getOnOff(sdata.flood));
                    updated |= th.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_RAIN_MODE, getOnOff(sdata.rain_sensor));
                }
                if (sdata.bat != null) {
                    th.logger.trace("{}: Updating battery", th.thingName);
                    updated |= th.updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL,
                            getDecimal(sdata.bat.value));
                    updated |= th.updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LOW,
                            getDouble(sdata.bat.value) < th.config.lowBattery ? OnOffType.ON : OnOffType.OFF);
                    if (sdata.bat.value != null) { // no update for Sense
                        updated |= th.updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_VOLT,
                                getDecimal(sdata.bat.voltage));
                    }
                }
                if (profile.isSense) {
                    updated |= th.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MOTION, getOnOff(sdata.motion));
                    updated |= th.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_CHARGER, getOnOff(sdata.charger));
                }
                if (sdata.act_reasons != null) {
                    String message = "";
                    for (int i = 0; i < sdata.act_reasons.length; i++) {
                        message = "[" + i + "]: " + sdata.act_reasons[i];
                    }
                    th.logger.debug("Last activate reasons: {}", message);
                }
            }
        }
        return updated;
    }
}
