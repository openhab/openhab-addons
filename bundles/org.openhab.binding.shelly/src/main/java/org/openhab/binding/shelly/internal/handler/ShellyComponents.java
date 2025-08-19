/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyADC;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtTemperature.ShellyShortTemp;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyThermnostat;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/***
 * The{@link ShellyComponents} implements updates for supplemental components
 * Meter will be used by Relay + Light; Sensor is part of H&amp;T, Flood, Door Window, Sense
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyComponents {

    /**
     * Update device status
     *
     * @param thingHandler Thing Handler instance
     * @param status Status message
     */
    public static boolean updateDeviceStatus(ShellyThingInterface thingHandler, ShellySettingsStatus status) {
        ShellyDeviceProfile profile = thingHandler.getProfile();

        if (!profile.gateway.isEmpty()) {
            thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_GATEWAY, getStringType(profile.gateway));
        }
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
        if (status.tmp != null && getBool(status.tmp.isValid) && !thingHandler.getProfile().isSensor
                && status.tmp.tC != SHELLY_API_INVTEMP) {
            thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                    toQuantityType(getDouble(status.tmp.tC), DIGITS_TEMP, SIUnits.CELSIUS));
        } else if (status.temperature != null && status.temperature != SHELLY_API_INVTEMP) {
            thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                    toQuantityType(getDouble(status.temperature), DIGITS_NONE, SIUnits.CELSIUS));
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
        List<ShellySettingsRelay> relays = profile.settings.relays;
        if (relays != null) {
            rsettings = relays.get(id);
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
                            getOpenClosed(getInteger(status.extSwitch.input0.input) == 1));
                }
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
                thingHandler.logger.debug("{}: Update roller position to {}/{}, state={}", thingHandler.thingName, pos,
                        SHELLY_MAX_ROLLER_POS - pos, state);
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
     * @param thingHandler Thing Handler instance
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
                                double kwh = getDouble(meter.total) / 1000 / 60;
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
                    if (status.neutralCurrent != null) {
                        if (!thingHandler.areChannelsCreated()) {
                            thingHandler.updateChannelDefinitions(ShellyChannelDefinitions.createEMNCurrentChannels(
                                    thingHandler.getThing(), profile.settings.neutralCurrent, status.neutralCurrent));
                        }
                        if (getBool(status.neutralCurrent.isValid)) {
                            String ngroup = CHANNEL_GROUP_NMETER;
                            updated |= thingHandler.updateChannel(ngroup, CHANNEL_NMETER_CURRENT, toQuantityType(
                                    getDouble(status.neutralCurrent.current), DIGITS_AMPERE, Units.AMPERE));
                            updated |= thingHandler.updateChannel(ngroup, CHANNEL_NMETER_IXSUM, toQuantityType(
                                    getDouble(status.neutralCurrent.ixsum), DIGITS_AMPERE, Units.AMPERE));
                            updated |= thingHandler.updateChannel(ngroup, CHANNEL_NMETER_MTRESHHOLD,
                                    toQuantityType(getDouble(profile.settings.neutralCurrent.mismatchThreshold),
                                            DIGITS_AMPERE, Units.AMPERE));
                            updated |= thingHandler.updateChannel(ngroup, CHANNEL_NMETER_MISMATCH,
                                    getOnOff(status.neutralCurrent.mismatch));
                        }
                    }

                    for (ShellySettingsEMeter emeter : status.emeters) {
                        if (getBool(emeter.isValid)) {
                            String groupName = profile.getMeterGroup(m);
                            if (!thingHandler.areChannelsCreated()) {
                                thingHandler.updateChannelDefinitions(ShellyChannelDefinitions
                                        .createEMeterChannels(thingHandler.getThing(), profile, emeter, groupName));
                            }

                            // convert Watt/h to KW/h
                            double total = getDouble(emeter.total) / 1000;
                            double totalReturned = getDouble(emeter.totalReturned) / 1000;
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_CURRENTWATTS,
                                    toQuantityType(getDouble(emeter.power), DIGITS_WATT, Units.WATT));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_TOTALKWH,
                                    toQuantityType(total, DIGITS_KWH, Units.KILOWATT_HOUR));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_TOTALRET,
                                    toQuantityType(totalReturned, DIGITS_KWH, Units.KILOWATT_HOUR));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_REACTWATTS,
                                    toQuantityType(getDouble(emeter.reactive), DIGITS_WATT, Units.WATT));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_VOLTAGE,
                                    toQuantityType(getDouble(emeter.voltage), DIGITS_VOLT, Units.VOLT));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_CURRENT,
                                    toQuantityType(getDouble(emeter.current), DIGITS_AMPERE, Units.AMPERE));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_FREQUENCY,
                                    toQuantityType(getDouble(emeter.frequency), DIGITS_FREQUENCY, Units.HERTZ));
                            updated |= thingHandler.updateChannel(groupName, CHANNEL_EMETER_PFACTOR,
                                    toQuantityType(computePF(emeter), Units.PERCENT));

                            accumulatedWatts += getDouble(emeter.power);
                            accumulatedTotal += total;
                            accumulatedReturned += totalReturned;
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
                        toQuantityType(currentWatts, DIGITS_WATT, Units.WATT));
                updated |= thingHandler.updateChannel(groupName, CHANNEL_METER_TOTALKWH,
                        toQuantityType(totalWatts, DIGITS_KWH, Units.KILOWATT_HOUR));

                if (updated && timestamp > 0) {
                    thingHandler.updateChannel(groupName, CHANNEL_LAST_UPDATE,
                            getTimestamp(getString(profile.settings.timezone), timestamp));
                }
            }

            if (!profile.isRoller && !profile.isRGBW2) {
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUWATTS, toQuantityType(
                        status.totalPower != null ? status.totalPower : accumulatedWatts, DIGITS_WATT, Units.WATT));
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUTOTAL,
                        toQuantityType(status.totalCurrent != null ? status.totalCurrent / 1000 : accumulatedTotal,
                                DIGITS_KWH, Units.KILOWATT_HOUR));
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCURETURNED,
                        toQuantityType(status.totalReturned != null ? status.totalReturned / 1000 : accumulatedReturned,
                                DIGITS_KWH, Units.KILOWATT_HOUR));
                thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_TOTALKWH, toQuantityType(
                        status.totalKWH != null ? status.totalKWH / 1000 : 0, DIGITS_KWH, Units.KILOWATT_HOUR));

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
            return emeter.power / Math.sqrt(emeter.power * emeter.power + emeter.reactive * emeter.reactive);
        }
        return 0.0;
    }

    /**
     * Update Sensor channel
     *
     * @param thingHandler Thing Handler instance
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
                        getOpenClosed(getString(sdata.sensor.state).equalsIgnoreCase(SHELLY_API_DWSTATE_OPEN)));
                String sensorError = sdata.sensorError;
                boolean changed = thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ERROR,
                        getStringType(sensorError));
                if (changed && !"0".equals(sensorError)) {
                    thingHandler.postEvent(getString(sdata.sensorError), true);
                }
                updated |= changed;
            }
            if (sdata.tmp != null && getBool(sdata.tmp.isValid)) {
                Double temp = getString(sdata.tmp.units).toUpperCase().equals(SHELLY_TEMP_CELSIUS)
                        ? getDouble(sdata.tmp.tC)
                        : getDouble(sdata.tmp.tF);
                updated |= updateTempChannel(thingHandler, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP,
                        temp.doubleValue(), getString(sdata.tmp.units));
            } else if (status.thermostats != null) {
                // Shelly TRV
                List<ShellyThermnostat> thermostats = profile.settings.thermostats;
                if (thermostats != null) {
                    ShellyThermnostat ps = thermostats.get(0);
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
                        updated |= updateTempChannel(thingHandler, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP,
                                t.tmp.value, t.tmp.units);
                        if (t.targetTemp.unit == null) {
                            t.targetTemp.unit = t.tmp.units;
                        }
                        updated |= updateTempChannel(thingHandler, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SETTEMP,
                                t.targetTemp.value, t.targetTemp.unit);
                    }
                    if (t.pos != null) {
                        updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_POSITION,
                                t.pos != -1 ? toQuantityType(t.pos, DIGITS_NONE, Units.PERCENT) : UnDefType.UNDEF);
                        updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE,
                                getOpenClosed(getDouble(t.pos) > 0));
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
            if (sdata.mute != null) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MUTE, getOnOff(sdata.mute));
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
            if ((sdata.adcs != null) && (!sdata.adcs.isEmpty())) {
                ShellyADC adc = sdata.adcs.get(0);
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VOLTAGE,
                        toQuantityType(getDouble(adc.voltage), 2, Units.VOLT));
            }

            boolean charger = (getInteger(profile.settings.externalPower) == 1) || getBool(sdata.charger);
            if ((profile.settings.externalPower != null) || (sdata.charger != null)) {
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CHARGER,
                        getOnOff(charger));
            }
            if (sdata.bat != null) { // no update for Sense
                updated |= thingHandler.updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL,
                        toQuantityType(getDouble(sdata.bat.value), 0, Units.PERCENT));

                int lowBattery = thingHandler.getThingConfig().lowBattery;
                boolean changed = thingHandler.updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LOW,
                        getOnOff(!charger && getDouble(sdata.bat.value) < lowBattery));
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
                thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_LAST_UPDATE, getTimestamp());
            }
        }

        // Update Add-On channeös
        if (status.extTemperature != null) {
            // Shelly 1/1PM support up to 3 external sensors
            // for whatever reason those are not represented as an array, but 3 elements
            updated |= updateTempChannel(status.extTemperature.sensor1, thingHandler, CHANNEL_ESENSOR_TEMP1);
            updated |= updateTempChannel(status.extTemperature.sensor2, thingHandler, CHANNEL_ESENSOR_TEMP2);
            updated |= updateTempChannel(status.extTemperature.sensor3, thingHandler, CHANNEL_ESENSOR_TEMP3);
            updated |= updateTempChannel(status.extTemperature.sensor4, thingHandler, CHANNEL_ESENSOR_TEMP4);
            updated |= updateTempChannel(status.extTemperature.sensor5, thingHandler, CHANNEL_ESENSOR_TEMP5);
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
            updated |= thingHandler.updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_ANALOGINPUT,
                    toQuantityType(getDouble(status.extAnalogInput.sensor1.percent), DIGITS_PERCENT, Units.PERCENT));
        }

        return updated;
    }

    public static boolean updateRGBW(ShellyThingInterface thingHandler, ShellySettingsStatus orgStatus)
            throws ShellyApiException {
        boolean updated = false;
        ShellyDeviceProfile profile = thingHandler.getProfile();
        if (profile.isRGBW2) {
            if (!thingHandler.areChannelsCreated()) {
                return false;
            }
            ShellySettingsLight light = orgStatus.lights.get(0);
            ShellyColorUtils col = new ShellyColorUtils();
            col.setRGBW(light.red, light.green, light.blue, light.white);
            updated |= thingHandler.updateChannel(CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_RED, col.percentRed);
            updated |= thingHandler.updateChannel(CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_GREEN, col.percentGreen);
            updated |= thingHandler.updateChannel(CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_BLUE, col.percentBlue);
            updated |= thingHandler.updateChannel(CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_WHITE, col.percentWhite);
            updated |= thingHandler.updateChannel(CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_PICKER, col.toHSB());

        }
        return updated;
    }

    public static boolean updateDimmers(ShellyThingInterface thingHandler, ShellySettingsStatus orgStatus)
            throws ShellyApiException {
        boolean updated = false;
        ShellyDeviceProfile profile = thingHandler.getProfile();
        if (profile.isDimmer) {
            // We need to fixup the returned Json: The dimmer returns light[] element, which is ok, but it doesn't have
            // the same structure as lights[] from Bulb,RGBW2 and Duo. The tag gets replaced by dimmers[] so that Gson
            // maps to a different structure (ShellyShortLight).
            Gson gson = new Gson();
            ShellySettingsStatus dstatus = !profile.isGen2
                    ? fromJson(gson, Shelly1ApiJsonDTO.fixDimmerJson(orgStatus.json), ShellySettingsStatus.class)
                    : orgStatus;

            int l = 0;
            for (ShellyShortLightStatus dimmer : dstatus.dimmers) {
                Integer r = l + 1;
                String groupName = profile.numRelays <= 1 ? CHANNEL_GROUP_DIMMER_CONTROL
                        : CHANNEL_GROUP_DIMMER_CONTROL + r.toString();

                if (!thingHandler.areChannelsCreated()) {
                    thingHandler.updateChannelDefinitions(ShellyChannelDefinitions
                            .createDimmerChannels(thingHandler.getThing(), profile, dstatus, l));
                }

                List<ShellySettingsDimmer> dimmers = profile.settings.dimmers;
                if (dimmers != null) {
                    ShellySettingsDimmer ds = dimmers.get(l);
                    if (ds.name != null) {
                        updated |= thingHandler.updateChannel(groupName, CHANNEL_OUTPUT_NAME, getStringType(ds.name));
                    }
                }

                // On a status update we map a dimmer.ison = false to brightness 0 rather than the device's brightness
                // and send an OFF status to the same channel.
                // When the device's brightness is > 0 we send the new value to the channel and an ON command
                if (dimmer.ison != null) {
                    if (dimmer.ison) {
                        updated |= thingHandler.updateChannel(groupName, CHANNEL_BRIGHTNESS + "$Switch", OnOffType.ON);
                        updated |= thingHandler.updateChannel(groupName, CHANNEL_BRIGHTNESS + "$Value",
                                toQuantityType((double) getInteger(dimmer.brightness), DIGITS_NONE, Units.PERCENT));
                    } else {
                        updated |= thingHandler.updateChannel(groupName, CHANNEL_BRIGHTNESS + "$Switch", OnOffType.OFF);
                        updated |= thingHandler.updateChannel(groupName, CHANNEL_BRIGHTNESS + "$Value",
                                toQuantityType(0.0, DIGITS_NONE, Units.PERCENT));
                    }
                }

                if (dimmers != null) {
                    ShellySettingsDimmer dsettings = dimmers.get(l);
                    updated |= thingHandler.updateChannel(groupName, CHANNEL_TIMER_AUTOON,
                            toQuantityType(getDouble(dsettings.autoOn), Units.SECOND));
                    updated |= thingHandler.updateChannel(groupName, CHANNEL_TIMER_AUTOOFF,
                            toQuantityType(getDouble(dsettings.autoOff), Units.SECOND));
                }

                l++;
            }
        }
        return updated;
    }

    public static boolean updateTempChannel(@Nullable ShellyShortTemp sensor, ShellyThingInterface thingHandler,
            String channel) {
        return sensor != null ? updateTempChannel(thingHandler, CHANNEL_GROUP_SENSOR, channel, sensor.tC, "") : false;
    }

    public static boolean updateTempChannel(ShellyThingInterface thingHandler, String group, String channel,
            @Nullable Double temp, @Nullable String unit) {
        if (temp == null || temp == SHELLY_API_INVTEMP) {
            return false;
        }
        return thingHandler.updateChannel(group, channel,
                toQuantityType(convertToC(temp, unit), DIGITS_TEMP, SIUnits.CELSIUS));
    }

    private static Double convertToC(@Nullable Double temp, @Nullable String unit) {
        if (temp == null || temp == SHELLY_API_INVTEMP) {
            return SHELLY_API_INVTEMP;
        }
        if (SHELLY_TEMP_FAHRENHEIT.equalsIgnoreCase(getString(unit))) {
            // convert Fahrenheit to Celsius
            return ImperialUnits.FAHRENHEIT.getConverterTo(SIUnits.CELSIUS).convert(temp).doubleValue();
        }
        return temp;
    }
}
