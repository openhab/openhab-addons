/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyADC;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyThermnostat;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

/***
 * The{@link ShellyComponents} implements updates for supplemental components
 * Meter will be used by Relay + Light; Sensor is part of H&T, Flood, Door Window, Sense
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyComponents {

    /**
     * Update device status
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     */
    public static boolean updateDeviceStatus(ShellyThingInterface thingHandler, ShellySettingsStatus status) {
        ShellyDeviceProfile profile = thingHandler.getProfile();

        if (!thingHandler.areChannelsCreated()) {
            thingHandler.updateChannelDefinitions(ShellyChannelDefinitions.createDeviceChannels(thingHandler.getThing(),
                    thingHandler.getProfile(), status));
        }

        if (getLong(status.uptime) > 10) {
            thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_UPTIME,
                    toQuantityType((double) getLong(status.uptime), DIGITS_NONE, Units.SECOND));
        }

        Integer rssi = getInteger(status.wifiSta.rssi);
        thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_RSSI, mapSignalStrength(rssi));
        if (getDouble(status.temperature) != SHELLY_API_INVTEMP) {
            if (status.tmp != null && !thingHandler.getProfile().isSensor) {
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                        toQuantityType(getDouble(status.tmp.tC), DIGITS_NONE, SIUnits.CELSIUS));
            } else if (status.temperature != null) {
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                        toQuantityType(getDouble(status.temperature), DIGITS_NONE, SIUnits.CELSIUS));
            }
        }
        thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_SLEEPTIME,
                toQuantityType(getInteger(status.sleepTime), Units.SECOND));

        thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_UPDATE, getOnOff(status.hasUpdate));

        if (profile.settings.calibrated != null) {
            thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CALIBRATED,
                    getOnOff(profile.settings.calibrated));
        }

        return false; // device status never triggers update
    }

    public static boolean updateRelay(ShellyBaseHandler thingHandler, ShellySettingsStatus status, int id) {
        ShellyDeviceProfile profile = thingHandler.getProfile();
        ShellySettingsRelay relay = status.relays.get(id);
        ShellySettingsRelay rsettings;
        if (profile.settings.relays != null) {
            rsettings = profile.settings.relays.get(id);
        } else {
            throw new IllegalArgumentException("No relay settings");
        }

        boolean updated = false;
        if (relay.isValid == null || relay.isValid) {
            String groupName = profile.getControlGroup(id);
            updated |= thingHandler.updateChannel(groupName, CHANNEL_OUTPUT_NAME, getStringType(rsettings.name));

            if (getBool(relay.overpower)) {
                thingHandler.postEvent(ALARM_TYPE_OVERPOWER, false);
            }

            updated |= thingHandler.updateChannel(groupName, CHANNEL_OUTPUT, getOnOff(relay.ison));
            updated |= thingHandler.updateChannel(groupName, CHANNEL_TIMER_ACTIVE, getOnOff(relay.hasTimer));
            if (status.extSwitch != null) {
                if (status.extSwitch.input0 != null) {
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_INPUT1,
                            getInteger(status.extSwitch.input0.input) == 1 ? OpenClosedType.OPEN
                                    : OpenClosedType.CLOSED);
                }
            }
            if (status.extTemperature != null) {
                // Shelly 1/1PM support up to 3 external sensors
                // for whatever reason those are not represented as an array, but 3 elements
                if (status.extTemperature.sensor1 != null) {
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP1,
                            toQuantityType(getDouble(status.extTemperature.sensor1.tC), DIGITS_TEMP, SIUnits.CELSIUS));
                }
                if (status.extTemperature.sensor2 != null) {
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP2,
                            toQuantityType(getDouble(status.extTemperature.sensor2.tC), DIGITS_TEMP, SIUnits.CELSIUS));
                }
                if (status.extTemperature.sensor3 != null) {
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP3,
                            toQuantityType(getDouble(status.extTemperature.sensor3.tC), DIGITS_TEMP, SIUnits.CELSIUS));
                }
                if (status.extTemperature.sensor4 != null) {
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP4,
                            toQuantityType(getDouble(status.extTemperature.sensor4.tC), DIGITS_TEMP, SIUnits.CELSIUS));
                }
                if (status.extTemperature.sensor5 != null) {
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP5,
                            toQuantityType(getDouble(status.extTemperature.sensor5.tC), DIGITS_TEMP, SIUnits.CELSIUS));
                }
            }
            if ((status.extHumidity != null) && (status.extHumidity.sensor1 != null)) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_HUMIDITY,
                        toQuantityType(getDouble(status.extHumidity.sensor1.hum), DIGITS_PERCENT, Units.PERCENT));
            }
            if ((status.extVoltage != null) && (status.extVoltage.sensor1 != null)) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_VOLTAGE,
                        toQuantityType(getDouble(status.extVoltage.sensor1.voltage), 4, Units.VOLT));
            }
            if ((status.extDigitalInput != null) && (status.extDigitalInput.sensor1 != null)) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_DIGITALINPUT,
                        getOnOff(status.extDigitalInput.sensor1.state));
            }
            if ((status.extAnalogInput != null) && (status.extAnalogInput.sensor1 != null)) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_ANALOGINPUT, toQuantityType(
                        getDouble(status.extAnalogInput.sensor1.percent), DIGITS_PERCENT, Units.PERCENT));
            }

            // Update Auto-ON/OFF timer
            updated |= thingHandler.updateChannel(groupName, CHANNEL_TIMER_AUTOON,
                    toQuantityType(getDouble(rsettings.autoOn), Units.SECOND));
            updated |= thingHandler.updateChannel(groupName, CHANNEL_TIMER_AUTOOFF,
                    toQuantityType(getDouble(rsettings.autoOff), Units.SECOND));
        }
        return updated;
    }

    public static boolean updateRoller(ShellyBaseHandler thingHandler, ShellyRollerStatus control, int id)
            throws ShellyApiException {
        ShellyDeviceProfile profile = thingHandler.getProfile();
        boolean updated = false;
        if (getBool(control.isValid)) {
            String groupName = profile.getControlGroup(id);
            if (control.name != null) {
                updated |= thingHandler.updateChannel(groupName, CHANNEL_OUTPUT_NAME, getStringType(control.name));
            }

            String state = getString(control.state);
            int pos = -1;
            switch (state) {
                case SHELLY_ALWD_ROLLER_TURN_OPEN:
                    pos = SHELLY_MAX_ROLLER_POS;
                    break;
                case SHELLY_ALWD_ROLLER_TURN_CLOSE:
                    pos = SHELLY_MIN_ROLLER_POS;
                    break;
                case SHELLY_ALWD_ROLLER_TURN_STOP:
                    if (control.currentPos != null) {
                        // only valid in stop state
                        pos = Math.max(SHELLY_MIN_ROLLER_POS, Math.min(control.currentPos, SHELLY_MAX_ROLLER_POS));
                    }
                    break;
            }
            if (pos != -1) {
                updated |= thingHandler.updateChannel(groupName, CHANNEL_ROL_CONTROL_CONTROL,
                        toQuantityType((double) (SHELLY_MAX_ROLLER_POS - pos), Units.PERCENT));
                updated |= thingHandler.updateChannel(groupName, CHANNEL_ROL_CONTROL_POS,
                        toQuantityType((double) pos, Units.PERCENT));
            }

            updated |= thingHandler.updateChannel(groupName, CHANNEL_ROL_CONTROL_STATE, new StringType(state));
            updated |= thingHandler.updateChannel(groupName, CHANNEL_ROL_CONTROL_STOPR,
                    getStringType(control.stopReason));
            updated |= thingHandler.updateChannel(groupName, CHANNEL_ROL_CONTROL_SAFETY,
                    getOnOff(control.safetySwitch));
        }
        return updated;
    }

    /**
     * Update Meter channel
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     */
    public static boolean updateMeters(ShellyThingInterface thingHandler, ShellySettingsStatus status) {
        ShellyDeviceProfile profile = thingHandler.getProfile();

        double accumulatedWatts = 0.0;
        double accumulatedTotal = 0.0;
        double accumulatedReturned = 0.0;

        boolean updated = false;
        // Devices without power meters get no updates
        // We need to differ
        // Roler+RGBW2 have multiple meters -> aggregate consumption to the functional device
        // Meter and EMeter have a different set of channels
        if (status.meters != null || status.emeters != null) {
            if (!profile.isRoller && !profile.isRGBW2) {
                // In Relay mode we map eacher meter to the matching channel group
                int m = 0;
                if (!profile.isEMeter) {
                    for (ShellySettingsMeter meter : status.meters) {
                        if (m >= profile.numMeters) {
                            // Shelly1: reports status.meters[0].is_valid = true, but even doesn't have a meter
                            meter.isValid = false;
                        }
                        if (getBool(meter.isValid) || profile.isLight) { // RGBW2-white doesn't report valid flag
                            // correctly in white mode
                            String groupName = profile.getMeterGroup(m);
                            if (!thingHandler.areChannelsCreated()) {
                                // skip for Shelly Bulb: JSON has a meter, but values don't get updated
                                if (!profile.isBulb) {
                                    thingHandler.updateChannelDefinitions(ShellyChannelDefinitions
                                            .createMeterChannels(thingHandler.getThing(), meter, groupName));
                                }
                            }

                            updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_CURRENTWATTS,
                                    toQuantityType(getDouble(meter.power), DIGITS_WATT, Units.WATT));
                            accumulatedWatts += getDouble(meter.power);

                            // convert Watt/Min to kw/h
                            if (meter.total != null) {
                                double kwh = getDouble(meter.total) / 60 / 1000;
                                updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_TOTALKWH,
                                        toQuantityType(kwh, DIGITS_KWH, Units.KILOWATT_HOUR));
                                accumulatedTotal += kwh;
                            }
                            if (meter.counters != null) {
                                updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_LASTMIN1,
                                        toQuantityType(getDouble(meter.counters[0]), DIGITS_WATT, Units.WATT));
                            }
                            if (meter.timestamp != null) {
                                thingHandler.updateChannel(groupName, CHANNEL_LAST_UPDATE,
                                        getTimestamp(getString(profile.settings.timezone), meter.timestamp));
                            }
                        }
                        m++;
                    }
                } else {
                    for (ShellySettingsEMeter emeter : status.emeters) {
                        if (getBool(emeter.isValid)) {
                            String groupName = profile.getMeterGroup(m);
                            if (!thingHandler.areChannelsCreated()) {
                                thingHandler.updateChannelDefinitions(ShellyChannelDefinitions
                                        .createEMeterChannels(thingHandler.getThing(), emeter, groupName));
                            }

                            // convert Watt/Hour tok w/h
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_CURRENTWATTS,
                                    toQuantityType(getDouble(emeter.power), DIGITS_WATT, Units.WATT));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_TOTALKWH,
                                    toQuantityType(getDouble(emeter.total) / 1000, DIGITS_KWH, Units.KILOWATT_HOUR));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_TOTALRET, toQuantityType(
                                    getDouble(emeter.totalReturned) / 1000, DIGITS_KWH, Units.KILOWATT_HOUR));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_REACTWATTS,
                                    toQuantityType(getDouble(emeter.reactive), DIGITS_WATT, Units.WATT));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_VOLTAGE,
                                    toQuantityType(getDouble(emeter.voltage), DIGITS_VOLT, Units.VOLT));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_CURRENT,
                                    toQuantityType(getDouble(emeter.current), DIGITS_VOLT, Units.AMPERE));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_PFACTOR,
                                    toQuantityType(computePF(emeter), Units.PERCENT));

                            accumulatedWatts += getDouble(emeter.power);
                            accumulatedTotal += getDouble(emeter.total) / 1000;
                            accumulatedReturned += getDouble(emeter.totalReturned) / 1000;
                            if (updated) {
                                thingHandler.updateChannel(groupName, CHANNEL_LAST_UPDATE, getTimestamp());
                            }
                        }
                        m++;
                    }
                }
            } else {
                // In Roller Mode we accumulate all meters to a single set of meters
                double currentWatts = 0.0;
                double totalWatts = 0.0;
                double lastMin1 = 0.0;
                long timestamp = 0l;
                String groupName = CHANNEL_GROUP_METER;

                if (!thingHandler.areChannelsCreated()) {
                    ShellySettingsMeter m = status.meters.get(0);
                    if (getBool(m.isValid)) {
                        // Create channels for 1 Meter
                        thingHandler.updateChannelDefinitions(
                                ShellyChannelDefinitions.createMeterChannels(thingHandler.getThing(), m, groupName));
                    }
                }

                for (ShellySettingsMeter meter : status.meters) {
                    if (getBool(meter.isValid)) {
                        currentWatts += getDouble(meter.power);
                        totalWatts += getDouble(meter.total);
                        if (meter.counters != null) {
                            lastMin1 += getDouble(meter.counters[0]);
                        }
                        if (getLong(meter.timestamp) > timestamp) {
                            timestamp = getLong(meter.timestamp); // newest one
                        }
                    }
                }

                updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_LASTMIN1,
                        toQuantityType(getDouble(lastMin1), DIGITS_WATT, Units.WATT));

                // convert totalWatts into kw/h
                totalWatts = totalWatts / (60.0 * 1000.0);
                updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_CURRENTWATTS,
                        toQuantityType(getDouble(currentWatts), DIGITS_WATT, Units.WATT));
                updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_TOTALKWH,
                        toQuantityType(getDouble(totalWatts), DIGITS_KWH, Units.KILOWATT_HOUR));

                if (updated && timestamp > 0) {
                    thingHandler.updateChannel(groupName, CHANNEL_LAST_UPDATE,
                            getTimestamp(getString(profile.settings.timezone), timestamp));
                }
            }

            if (!profile.isRoller && !profile.isRGBW2) {
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUWATTS,
                        toQuantityType(accumulatedWatts, DIGITS_WATT, Units.WATT));
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUTOTAL,
                        toQuantityType(accumulatedTotal, DIGITS_KWH, Units.KILOWATT_HOUR));
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCURETURNED,
                        toQuantityType(accumulatedReturned, DIGITS_KWH, Units.KILOWATT_HOUR));
            }
        }

        return updated;
    }

    private static Double computePF(ShellySettingsEMeter emeter) {
        if (emeter.pf != null) { // EM3
            return emeter.pf; // take device value
        }

        // EM: compute from provided values
        if (emeter.reactive != null && Math.abs(emeter.power) + Math.abs(emeter.reactive) > 1.5) {
            double pf = emeter.power / Math.sqrt(emeter.power * emeter.power + emeter.reactive * emeter.reactive);
            return pf;
        }
        return 0.0;
    }

    /**
     * Update Sensor channel
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     *
     * @throws ShellyApiException
     */
    public static boolean updateSensors(ShellyThingInterface thingHandler, ShellySettingsStatus status)
            throws ShellyApiException {
        ShellyDeviceProfile profile = thingHandler.getProfile();

        boolean updated = false;
        if (profile.isSensor || profile.hasBattery) {
            ShellyStatusSensor sdata = thingHandler.getApi().getSensorStatus();
            if (!thingHandler.areChannelsCreated()) {
                thingHandler.updateChannelDefinitions(
                        ShellyChannelDefinitions.createSensorChannels(thingHandler.getThing(), profile, sdata));
            }

            updated |= thingHandler.updateWakeupReason(sdata.actReasons);

            if ((sdata.sensor != null) && sdata.sensor.isValid) {
                // Shelly DW: “sensor”:{“state”:“open”, “is_valid”:true},
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE,
                        getString(sdata.sensor.state).equalsIgnoreCase(SHELLY_API_DWSTATE_OPEN) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                String sensorError = sdata.sensorError;
                boolean changed = thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ERROR,
                        getStringType(sensorError));
                if (changed && !"0".equals(sensorError)) {
                    thingHandler.postEvent(getString(sdata.sensorError), true);
                }
                updated |= changed;
            }
            if ((sdata.tmp != null) && getBool(sdata.tmp.isValid)) {
                Double temp = getString(sdata.tmp.units).toUpperCase().equals(SHELLY_TEMP_CELSIUS)
                        ? getDouble(sdata.tmp.tC)
                        : getDouble(sdata.tmp.tF);
                if (getString(sdata.tmp.units).toUpperCase().equals(SHELLY_TEMP_FAHRENHEIT)) {
                    // convert Fahrenheit to Celsius
                    temp = ImperialUnits.FAHRENHEIT.getConverterTo(SIUnits.CELSIUS).convert(temp).doubleValue();
                }
                temp = convertToC(temp, getString(sdata.tmp.units));
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP,
                        toQuantityType(temp.doubleValue(), DIGITS_TEMP, SIUnits.CELSIUS));
            } else if (status.thermostats != null) {
                // Shelly TRV
                if (profile.settings.thermostats != null) {
                    ShellyThermnostat ps = profile.settings.thermostats.get(0);
                    ShellyThermnostat t = status.thermostats.get(0);
                    int bminutes = getInteger(t.boostMinutes) >= 0 ? getInteger(t.boostMinutes)
                            : getInteger(ps.boostMinutes);
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_BCONTROL,
                            getOnOff(getInteger(t.boostMinutes) > 0));
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_BTIMER,
                            toQuantityType((double) bminutes, DIGITS_NONE, Units.MINUTE));
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_MODE, getStringType(
                            getBool(t.targetTemp.enabled) ? SHELLY_TRV_MODE_AUTO : SHELLY_TRV_MODE_MANUAL));
                    int pid = getBool(t.schedule) ? getInteger(t.profile) : 0;
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SCHEDULE,
                            getOnOff(t.schedule));
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE,
                            getStringType(profile.getValueProfile(0, pid)));
                    if (t.tmp != null) {
                        Double temp = convertToC(t.tmp.value, getString(t.tmp.units));
                        updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP,
                                toQuantityType(temp.doubleValue(), DIGITS_TEMP, SIUnits.CELSIUS));
                        temp = convertToC(t.targetTemp.value, getString(t.targetTemp.unit));
                        updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SETTEMP,
                                toQuantityType(t.targetTemp.value, DIGITS_TEMP, SIUnits.CELSIUS));
                    }
                    if (t.pos != null) {
                        updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_POSITION,
                                t.pos != -1 ? toQuantityType(t.pos, DIGITS_NONE, Units.PERCENT) : UnDefType.UNDEF);
                        updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE,
                                getDouble(t.pos) > 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                    }
                }
            }

            if (sdata.hum != null) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_HUM,
                        toQuantityType(getDouble(sdata.hum.value), DIGITS_PERCENT, Units.PERCENT));
            }
            if ((sdata.lux != null) && getBool(sdata.lux.isValid)) {
                // “lux”:{“value”:30, “illumination”: “dark”, “is_valid”:true},
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_LUX,
                        toQuantityType(getDouble(sdata.lux.value), DIGITS_LUX, Units.LUX));
                if (sdata.lux.illumination != null) {
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ILLUM,
                            getStringType(sdata.lux.illumination));
                }
            }
            if (sdata.accel != null) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TILT,
                        toQuantityType(getDouble(sdata.accel.tilt.doubleValue()), DIGITS_NONE, Units.DEGREE_ANGLE));
            }
            if (sdata.flood != null) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD,
                        getOnOff(sdata.flood));
            }
            if (sdata.smoke != null) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_SMOKE,
                        getOnOff(sdata.smoke));
            }
            if (sdata.gasSensor != null) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_SELFTTEST,
                        getStringType(sdata.gasSensor.selfTestState));
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ALARM_STATE,
                        getStringType(sdata.gasSensor.alarmState));
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_SSTATE,
                        getStringType(sdata.gasSensor.sensorState));
            }
            if ((sdata.concentration != null) && sdata.concentration.isValid) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PPM, toQuantityType(
                        getInteger(sdata.concentration.ppm).doubleValue(), DIGITS_NONE, Units.PARTS_PER_MILLION));
            }
            if ((sdata.adcs != null) && (sdata.adcs.size() > 0)) {
                ShellyADC adc = sdata.adcs.get(0);
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VOLTAGE,
                        toQuantityType(getDouble(adc.voltage), 2, Units.VOLT));
            }

            boolean charger = (getInteger(profile.settings.externalPower) == 1) || getBool(sdata.charger);
            if ((profile.settings.externalPower != null) || (sdata.charger != null)) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CHARGER,
                        charger ? OnOffType.ON : OnOffType.OFF);
            }
            if (sdata.bat != null) { // no update for Sense
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL,
                        toQuantityType(getDouble(sdata.bat.value), 0, Units.PERCENT));

                int lowBattery = thingHandler.getThingConfig().lowBattery;
                boolean changed = thingHandler.updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LOW,
                        !charger && getDouble(sdata.bat.value) < lowBattery ? OnOffType.ON : OnOffType.OFF);
                updated |= changed;
                if (!charger && changed && getDouble(sdata.bat.value) < lowBattery) {
                    thingHandler.postEvent(ALARM_TYPE_LOW_BATTERY, false);
                }
            }

            if (sdata.motion != null) { // Shelly Sense
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MOTION,
                        getOnOff(sdata.motion));
            }
            if (sdata.sensor != null) { // Shelly Motion
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MOTION_ACT,
                        getOnOff(sdata.sensor.motionActive));
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MOTION,
                        getOnOff(sdata.sensor.motion));
                long timestamp = getLong(sdata.sensor.motionTimestamp);
                if (timestamp != 0) {
                    updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MOTION_TS,
                            getTimestamp(getString(profile.settings.timezone), timestamp));
                }
            }

            updated |= thingHandler.updateInputs(status);

            if (updated) {
                thingHandler.updateChannel(profile.getControlGroup(0), CHANNEL_LAST_UPDATE, getTimestamp());
            }
        }
        return updated;
    }

    private static Double convertToC(@Nullable Double temp, String unit) {
        if (temp == null) {
            return 0.0;
        }
        if (SHELLY_TEMP_FAHRENHEIT.equalsIgnoreCase(unit)) {
            // convert Fahrenheit to Celsius
            return ImperialUnits.FAHRENHEIT.getConverterTo(SIUnits.CELSIUS).convert(temp).doubleValue();
        }
        return temp;
    }
}
