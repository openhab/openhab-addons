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
import static org.openhab.binding.shelly.internal.api.ShellyApiJson.*;

import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyControlRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyInputState;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyShortStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJson.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;

/**
 * The {@link ShellyUpdater} handles mapping of the device status attributes to the channels
 * sent to one of the channels.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyUpdater {

    /**
     * Update Relay/Roller channels
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     *
     * @throws IOException
     */
    @SuppressWarnings("null")
    public static boolean updateRelays(ShellyHandler th, ShellySettingsStatus status) throws IOException {
        Validate.notNull(th);
        Validate.notNull(th.api);
        Validate.notNull(status, "status must not be null!");
        ShellyDeviceProfile profile = th.getProfile();

        boolean updated = false;
        if (profile.hasRelays && !profile.isRoller && !profile.isDimmer) {
            th.logger.debug("{}: Updating {} relay(s)", th.thingName, profile.numRelays);
            int i = 0;
            ShellyStatusRelay rstatus = th.api.getRelayStatus(i);
            if (rstatus != null) {
                for (ShellyShortStatusRelay relay : rstatus.relays) {
                    if ((relay.is_valid == null) || relay.is_valid) {
                        Integer r = i + 1;
                        String groupName = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                                : CHANNEL_GROUP_RELAY_CONTROL + r.toString();

                        updated |= th.updateChannel(groupName, CHANNEL_OUTPUT, getOnOff(relay.ison));
                        updated |= th.updateChannel(groupName, CHANNEL_TIMER_ACTIVE, getOnOff(relay.has_timer));
                        ShellySettingsRelay rsettings = profile.settings.relays.get(i);
                        if (rsettings != null) {
                            updated |= th.updateChannel(groupName, CHANNEL_TIMER_AUTOON, getDecimal(rsettings.auto_on));
                            updated |= th.updateChannel(groupName, CHANNEL_TIMER_AUTOOFF,
                                    getDecimal(rsettings.auto_off));
                        }
                        updated |= th.updateChannel(groupName, CHANNEL_OVERPOWER, getOnOff(relay.overpower));
                        if (status.overtemperature != null) {
                            updated |= th.updateChannel(groupName, CHANNEL_OVERTEMP, getOnOff(status.overtemperature));
                        }
                    }
                    i++;
                }
            }
        }
        if (profile.hasRelays && profile.isRoller && (status.rollers != null)) {
            th.logger.debug("{}: Updating {} rollers", th.thingName, profile.numRollers);
            int i = 0;
            for (ShellySettingsRoller roller : status.rollers) {
                if (roller.is_valid) {
                    ShellyControlRoller control = th.api.getRollerStatus(i);
                    Integer relayIndex = i + 1;
                    String groupName = profile.numRollers == 1 ? CHANNEL_GROUP_ROL_CONTROL
                            : CHANNEL_GROUP_ROL_CONTROL + relayIndex.toString();
                    // updateChannel(groupName, CHANNEL_ROL_CONTROL_CONTROL, new
                    // StringType(getString(control.state)));
                    if (getString(control.state).equals(SHELLY_ALWD_ROLLER_TURN_STOP)) { // only valid in stop state
                        Integer pos = Math.min(getInteger(control.current_pos), SHELLY_MAX_ROLLER_POS); // saw 101
                        updated |= th.updateChannel(groupName, CHANNEL_ROL_CONTROL_CONTROL,
                                new PercentType(SHELLY_MAX_ROLLER_POS - pos));
                        updated |= th.updateChannel(groupName, CHANNEL_ROL_CONTROL_POS, new PercentType(pos));
                        th.scheduledUpdates = 1; // one more poll and then stop
                    }
                    updated |= th.updateChannel(groupName, CHANNEL_ROL_CONTROL_DIR,
                            getStringType(control.last_direction));
                    updated |= th.updateChannel(groupName, CHANNEL_ROL_CONTROL_STOPR,
                            getStringType(control.stop_reason));
                    if (status.overtemperature != null) {
                        updated |= th.updateChannel(groupName, CHANNEL_OVERTEMP, getOnOff(status.overtemperature));
                    }

                    updated |= updateInputs(th, groupName, status);

                    i++;
                }
            }
        }
        return updated;
    }

    /**
     * Update Relay/Roller channels
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     *
     * @throws IOException
     */
    @SuppressWarnings("null")
    public static boolean updateDimmers(ShellyHandler th, ShellySettingsStatus status) throws IOException {
        Validate.notNull(th);
        ShellyDeviceProfile profile = th.getProfile();

        boolean updated = false;
        if (profile.isDimmer) {
            Validate.notNull(status, "status must not be null!");
            Validate.notNull(status.lights, "status.lights must not be null!");
            Validate.notNull(status.tmp, "status.tmp must not be null!");

            th.logger.debug("{}: Updating {} dimmers(s)", th.thingName, status.lights.size());
            updated |= th.updateChannel(CHANNEL_GROUP_DIMMER_STATUS, CHANNEL_DIMMER_LOAD_ERROR,
                    getOnOff(status.loaderror));
            updated |= th.updateChannel(CHANNEL_GROUP_DIMMER_STATUS, CHANNEL_DIMMER_OVERLOAD,
                    getOnOff(status.overload));
            if (status.overtemperature != null) {
                updated |= th.updateChannel(CHANNEL_GROUP_DIMMER_STATUS, CHANNEL_OVERTEMP,
                        getOnOff(status.overtemperature));
            }

            int l = 0;
            for (ShellySettingsLight dimmer : status.lights) {
                Integer r = l + 1;
                String groupName = profile.numRelays <= 1 ? CHANNEL_GROUP_DIMMER_CONTROL
                        : CHANNEL_GROUP_DIMMER_CONTROL + r.toString();
                updated |= th.updateChannel(groupName, CHANNEL_OUTPUT, getOnOff(dimmer.ison));
                updated |= th.updateChannel(groupName, CHANNEL_BRIGHTNESS,
                        new PercentType(getInteger(dimmer.brightness)));

                ShellySettingsDimmer dsettings = profile.settings.dimmers.get(l);
                if (dsettings != null) {
                    updated |= th.updateChannel(groupName, CHANNEL_TIMER_AUTOON, getDecimal(dsettings.auto_on));
                    updated |= th.updateChannel(groupName, CHANNEL_TIMER_AUTOOFF, getDecimal(dsettings.auto_off));
                }

                updated |= updateInputs(th, groupName, status);
                l++;
            }
        }
        return updated;
    }

    @SuppressWarnings("null")
    private static boolean updateInputs(ShellyHandler th, String groupName, ShellySettingsStatus status) {
        boolean updated = false;
        if (status.inputs != null && !th.getProfile().isRoller) {
            th.logger.trace("{}: Updating {} input state(s)", th.thingName, status.inputs.size());
            for (int input = 0; input < status.inputs.size(); input++) {
                ShellyInputState state = status.inputs.get(input);
                String channel = CHANNEL_INPUT + Integer.toString(input + 1);
                th.logger.trace("{}: Updating channel {}.{} with inputs[{}]", th.thingName, groupName, channel, input);
                updated |= th.updateChannel(groupName, channel, state.input == 0 ? OnOffType.OFF : OnOffType.ON);
            }
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
    @SuppressWarnings("null")
    public static boolean updateMeters(ShellyHandler th, ShellySettingsStatus status) {
        Validate.notNull(th);
        ShellyDeviceProfile profile = th.getProfile();

        boolean updated = false;
        if (profile.hasMeter && ((status.meters != null) || (status.emeters != null))) {
            if (!profile.isRoller) {
                th.logger.debug("{}: Updating {} {}meter(s)", th.thingName, profile.numMeters,
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
                th.logger.debug("{}: Updating roller meter", th.thingName);
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
     * Update LED channels
     *
     * @param th Thing Handler instance
     * @param profile ShellyDeviceProfile
     * @param status Last ShellySettingsStatus
     */
    @SuppressWarnings("null")
    public static boolean updateLed(ShellyHandler th, ShellySettingsStatus status) {
        Validate.notNull(th);
        ShellyDeviceProfile profile = th.getProfile();

        boolean updated = false;
        if (profile.hasLed) {
            Validate.notNull(profile.settings.led_status_disable, "LED update: led_status_disable must not be null!");
            Validate.notNull(profile.settings.led_power_disable, "LED update: led_power_disable must not be null!");
            th.logger.debug("{}: LED disabled status: powerLed: {}, : statusLed{}", th.thingName,
                    getBool(profile.settings.led_power_disable), getBool(profile.settings.led_status_disable));
            updated |= th.updateChannel(CHANNEL_GROUP_LED_CONTROL, CHANNEL_LED_STATUS_DISABLE,
                    getOnOff(profile.settings.led_status_disable));
            updated |= th.updateChannel(CHANNEL_GROUP_LED_CONTROL, CHANNEL_LED_POWER_DISABLE,
                    getOnOff(profile.settings.led_power_disable));
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
    public static boolean updateSensors(ShellyHandler th, ShellySettingsStatus status) throws IOException {
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

    /*
     * Helper functions
     */

    public static String mkChannelId(String group, String channel) {
        return group + "#" + channel;
    }

    public static String getString(@Nullable String value) {
        return value != null ? value : "";
    }

    public static Integer getInteger(@Nullable Integer value) {
        return (value != null ? (Integer) value : 0);
    }

    public static Long getLong(@Nullable Long value) {
        return (value != null ? (Long) value : 0);
    }

    public static Double getDouble(@Nullable Double value) {
        return (value != null ? (Double) value : 0);
    }

    public static Boolean getBool(@Nullable Boolean value) {
        return (value != null ? (Boolean) value : false);
    }

    // as State

    public static StringType getStringType(@Nullable String value) {
        return new StringType(value != null ? value : "");
    }

    public static DecimalType getDecimal(@Nullable Double value) {
        return new DecimalType((value != null ? value : 0));
    }

    public static DecimalType getDecimal(@Nullable Integer value) {
        return new DecimalType((value != null ? value : 0));
    }

    public static DecimalType getDecimal(@Nullable Long value) {
        return new DecimalType((value != null ? value : 0));
    }

    public static OnOffType getOnOff(@Nullable Boolean value) {
        return (value != null ? value ? OnOffType.ON : OnOffType.OFF : OnOffType.OFF);
    }

    public static void validateRange(String name, Integer value, Integer min, Integer max) {
        Validate.isTrue((value >= min) && (value <= max),
                "Value " + name + " is out of range (" + min.toString() + "-" + max.toString() + ")");
    }

}
