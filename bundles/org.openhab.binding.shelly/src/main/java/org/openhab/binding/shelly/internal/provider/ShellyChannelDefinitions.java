/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.provider;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.SHELLY_API_INVTEMP;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyRollerStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsDimmer;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsGlobal;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyShortLightStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLightChannel;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ShellyCHANNEL_DEFINITIONSDTO} defines channel information for dynamically created channels. Those will be
 * added on the first thing status update
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = ShellyChannelDefinitions.class)
public class ShellyChannelDefinitions {

    public static final String ITEMT_STRING = "String";
    public static final String ITEMT_NUMBER = "Number";
    public static final String ITEMT_SWITCH = "Switch";
    public static final String ITEMT_CONTACT = "Contact";
    public static final String ITEMT_ROLLER = "Rollershutter";
    public static final String ITEMT_DIMMER = "Dimmer";
    public static final String ITEMT_LOCATION = "Location";
    public static final String ITEMT_DATETIME = "DateTime";
    public static final String ITEMT_TEMP = "Number:Temperature";
    public static final String ITEMT_LUX = "Number:Illuminance";
    public static final String ITEMT_POWER = "Number:Power";
    public static final String ITEMT_ENERGY = "Number:Energy";
    public static final String ITEMT_VOLT = "Number:ElectricPotential";
    public static final String ITEMT_AMP = "Number:ElectricPotential";
    public static final String ITEMT_ANGLE = "Number:Angle";
    public static final String ITEMT_DISTANCE = "Number:Length";
    public static final String ITEMT_SPEED = "Number:Speed";
    public static final String ITEMT_VOLUME = "Number:Volume";
    public static final String ITEMT_TIME = "Number:Time";
    public static final String ITEMT_PERCENT = "Number:Dimensionless";

    // shortcuts to avoid line breaks (make code more readable)
    private static final String CHGR_DEVST = CHANNEL_GROUP_DEV_STATUS;
    private static final String CHGR_RELAY = CHANNEL_GROUP_RELAY_CONTROL;
    private static final String CHGR_ROLLER = CHANNEL_GROUP_ROL_CONTROL;
    private static final String CHGR_LIGHT = CHANNEL_GROUP_LIGHT_CONTROL;
    private static final String CHGR_LIGHTCH = CHANNEL_GROUP_LIGHT_CHANNEL;
    private static final String CHGR_STATUS = CHANNEL_GROUP_STATUS;
    private static final String CHGR_METER = CHANNEL_GROUP_METER;
    private static final String CHGR_SENSOR = CHANNEL_GROUP_SENSOR;
    private static final String CHGR_CONTROL = CHANNEL_GROUP_CONTROL;
    private static final String CHGR_BAT = CHANNEL_GROUP_BATTERY;

    public static final String PREFIX_GROUP = "group-type." + BINDING_ID + ".";
    public static final String PREFIX_CHANNEL = "channel-type." + BINDING_ID + ".";

    public class OptionEntry {
        public ChannelTypeUID uid;
        public String key;
        public String value;

        public OptionEntry(ChannelTypeUID uid, String key, String value) {
            this.uid = uid;
            this.key = key;
            this.value = value;
        }
    }

    private final CopyOnWriteArrayList<OptionEntry> stateOptions = new CopyOnWriteArrayList<>();

    private static final ChannelMap CHANNEL_DEFINITIONS = new ChannelMap();

    @Activate
    public ShellyChannelDefinitions(@Reference ShellyTranslationProvider translationProvider) {
        ShellyTranslationProvider m = translationProvider;

        // Device
        CHANNEL_DEFINITIONS
                // Device
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_NAME, "deviceName", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ITEMP, "deviceTemp", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_WAKEUP, "sensorWakeup", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCUWATTS, "meterAccuWatts", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCUTOTAL, "meterAccuTotal", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCURETURNED, "meterAccuReturned", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_VOLTAGE, "supplyVoltage", ITEMT_VOLT))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_CHARGER, "charger", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_LED_STATUS_DISABLE, "ledStatusDisable", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_LED_POWER_DISABLE, "ledPowerDisable", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_SELFTTEST, "selfTest", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_UPTIME, "uptime", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_HEARTBEAT, "heartBeat", ITEMT_DATETIME))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_UPDATE, "updateAvailable", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_CALIBRATED, "calibrated", ITEMT_SWITCH))

                // Relay
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_OUTPUT_NAME, "outputName", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_OUTPUT, "system:power", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_INPUT, "inputState", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_BUTTON_TRIGGER, "system:button", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_STATUS_EVENTTYPE, "lastEvent", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_STATUS_EVENTCOUNT, "eventCount", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_TIMER_AUTOON, "timerAutoOn", ITEMT_TIME))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_TIMER_AUTOOFF, "timerAutoOff", ITEMT_TIME))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_TIMER_ACTIVE, "timerActive", ITEMT_SWITCH))

                // Dimmer
                .add(new ShellyChannel(m, CHANNEL_GROUP_DIMMER_CONTROL, CHANNEL_BRIGHTNESS, "dimmerBrightness",
                        ITEMT_DIMMER))

                // Roller
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_ROL_CONTROL_CONTROL, "rollerShutter", ITEMT_ROLLER))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_ROL_CONTROL_POS, "rollerPosition", ITEMT_DIMMER))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_ROL_CONTROL_FAV, "rollerFavorite", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_ROL_CONTROL_STATE, "rollerState", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_ROL_CONTROL_STOPR, "rollerStop", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_ROL_CONTROL_SAFETY, "rollerSafety", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_INPUT, "inputState", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_STATUS_EVENTTYPE, "lastEvent", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_STATUS_EVENTCOUNT, "eventCount", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_ROLLER, CHANNEL_EVENT_TRIGGER, "system:button", "system:button"))

                // Bulb/Duo/Vintage
                .add(new ShellyChannel(m, CHGR_LIGHT, CHANNEL_INPUT, "inputState", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_LIGHT, CHANNEL_BUTTON_TRIGGER, "system:button", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_LIGHT, CHANNEL_STATUS_EVENTTYPE, "lastEvent", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_LIGHT, CHANNEL_STATUS_EVENTCOUNT, "eventCount", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_BRIGHTNESS, "whiteBrightness",
                        ITEMT_DIMMER))
                .add(new ShellyChannel(m, CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_COLOR_TEMP, "whiteTemp", ITEMT_DIMMER))

                // RGBW2-color
                .add(new ShellyChannel(m, CHGR_LIGHT, CHANNEL_LIGHT_POWER, "system:power", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_LIGHT, CHANNEL_TIMER_AUTOON, "timerAutoOn", ITEMT_TIME))
                .add(new ShellyChannel(m, CHGR_LIGHT, CHANNEL_TIMER_AUTOOFF, "timerAutoOff", ITEMT_TIME))
                .add(new ShellyChannel(m, CHGR_LIGHT, CHANNEL_TIMER_ACTIVE, "timerActive", ITEMT_SWITCH))
                // RGBW2-white
                .add(new ShellyChannel(m, CHGR_LIGHTCH, CHANNEL_BRIGHTNESS, "whiteBrightness", ITEMT_DIMMER))
                .add(new ShellyChannel(m, CHGR_LIGHTCH, CHANNEL_TIMER_AUTOON, "timerAutoOn", ITEMT_TIME))
                .add(new ShellyChannel(m, CHGR_LIGHTCH, CHANNEL_TIMER_AUTOOFF, "timerAutoOff", ITEMT_TIME))
                .add(new ShellyChannel(m, CHGR_LIGHTCH, CHANNEL_TIMER_ACTIVE, "timerActive", ITEMT_SWITCH))

                // Power Meter
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_CURRENTWATTS, "meterWatts", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_TOTALKWH, "meterTotal", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_LASTMIN1, "lastPower1", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_LAST_UPDATE, "lastUpdate", ITEMT_DATETIME))

                // EMeter
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_TOTALRET, "meterReturned", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_REACTWATTS, "meterReactive", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_VOLTAGE, "meterVoltage", ITEMT_VOLT))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_CURRENT, "meterCurrent", ITEMT_AMP))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_PFACTOR, "meterPowerFactor", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_RESETTOTAL, "meterResetTotals", ITEMT_SWITCH))

                // Sensors
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_TEMP, "sensorTemp", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_HUM, "sensorHumidity", ITEMT_PERCENT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_LUX, "sensorLux", ITEMT_LUX))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ILLUM, "sensorIllumination", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_VOLTAGE, "sensorADC", ITEMT_VOLT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_STATE, "sensorContact", ITEMT_CONTACT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_OPEN, "sensorOpen", ITEMT_CONTACT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_SSTATE, "sensorState", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_TILT, "sensorTilt", ITEMT_ANGLE))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_MOTION, "sensorMotion", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_MOTION_TS, "motionTimestamp", ITEMT_DATETIME))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_MOTION_ACT, "motionActive", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_VIBRATION, "vibration", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_FLOOD, "sensorFlood", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_SMOKE, "sensorSmoke", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_MUTE, "sensorMute", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_PPM, "sensorPPM", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_VALVE, "sensorValve", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ALARM_STATE, "alarmState", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ERROR, "sensorError", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_LAST_UPDATE, "lastUpdate", ITEMT_DATETIME))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_SLEEPTIME, "sensorSleepTime", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSE_KEY, "senseKey", ITEMT_STRING)) // Sense

                // Button/ix3
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_INPUT, "inputState", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_STATUS_EVENTTYPE, "lastEvent", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_STATUS_EVENTCOUNT, "eventCount", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_BUTTON_TRIGGER, "system:button", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_LAST_UPDATE, "lastUpdate", ITEMT_DATETIME))

                // Addon with external sensors
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP1, "sensorExtTemp", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP2, "sensorExtTemp", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP3, "sensorExtTemp", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_HUMIDITY, "sensorExtHum", ITEMT_PERCENT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_VOLTAGE, "sensorExtVolt", ITEMT_VOLT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_INPUT1, "sensorContact", ITEMT_CONTACT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_DIGITALINPUT, "sensorExtDigitalInput",
                        ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_ANALOGINPUT, "sensorExtAnalogInput",
                        ITEMT_PERCENT))

                // Battery
                .add(new ShellyChannel(m, CHGR_BAT, CHANNEL_SENSOR_BAT_LEVEL, "system:battery-level", ITEMT_PERCENT))
                .add(new ShellyChannel(m, CHGR_BAT, CHANNEL_SENSOR_BAT_LOW, "system:low-battery", ITEMT_SWITCH))

                // TRV
                .add(new ShellyChannel(m, CHGR_CONTROL, CHANNEL_CONTROL_POSITION, "sensorPosition", ITEMT_DIMMER))
                .add(new ShellyChannel(m, CHGR_CONTROL, CHANNEL_CONTROL_MODE, "controlMode", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_CONTROL, CHANNEL_CONTROL_PROFILE, "controlProfile", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_CONTROL, CHANNEL_CONTROL_SETTEMP, "targetTemp", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_CONTROL, CHANNEL_CONTROL_BCONTROL, "boostControl", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_CONTROL, CHANNEL_CONTROL_BTIMER, "boostTimer", ITEMT_TIME))
                .add(new ShellyChannel(m, CHGR_CONTROL, CHANNEL_CONTROL_SCHEDULE, "controlSchedule", ITEMT_SWITCH));
    }

    public static @Nullable ShellyChannel getDefinition(String channelName) throws IllegalArgumentException {
        String group = substringBefore(channelName, "#");
        String channel = substringAfter(channelName, "#");

        if (group.contains(CHANNEL_GROUP_METER)) {
            group = CHANNEL_GROUP_METER; // map meter1..n to meter
        } else if (group.contains(CHANNEL_GROUP_RELAY_CONTROL)) {
            group = CHANNEL_GROUP_RELAY_CONTROL; // map meter1..n to meter
        } else if (group.contains(CHANNEL_GROUP_LIGHT_CHANNEL)) {
            group = CHANNEL_GROUP_LIGHT_CHANNEL;
        } else if (group.contains(CHANNEL_GROUP_STATUS)) {
            group = CHANNEL_GROUP_STATUS; // map status1..n to meter
        }

        if (channel.startsWith(CHANNEL_INPUT)) {
            channel = CHANNEL_INPUT;
        } else if (channel.startsWith(CHANNEL_BUTTON_TRIGGER)) {
            channel = CHANNEL_BUTTON_TRIGGER;
        } else if (channel.startsWith(CHANNEL_STATUS_EVENTTYPE)) {
            channel = CHANNEL_STATUS_EVENTTYPE;
        } else if (channel.startsWith(CHANNEL_STATUS_EVENTCOUNT)) {
            channel = CHANNEL_STATUS_EVENTCOUNT;
        }

        String channelId = group + "#" + channel;
        return CHANNEL_DEFINITIONS.get(channelId);
    }

    /**
     * Auto-create relay channels depending on relay type/mode
     *
     * @return ArrayList<Channel> of channels to be added to the thing
     */
    public static Map<String, Channel> createDeviceChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsStatus status) {
        Map<String, Channel> add = new LinkedHashMap<>();

        addChannel(thing, add, profile.settings.name != null, CHGR_DEVST, CHANNEL_DEVST_NAME);

        if (!profile.isSensor && !profile.isIX && status.temperature != null
                && status.temperature != SHELLY_API_INVTEMP) {
            // Only some devices report the internal device temp
            addChannel(thing, add, status.tmp != null || status.temperature != null, CHGR_DEVST, CHANNEL_DEVST_ITEMP);
        }
        addChannel(thing, add, profile.settings.sleepTime != null, CHGR_SENSOR, CHANNEL_SENSOR_SLEEPTIME);

        // If device has more than 1 meter the channel accumulatedWatts receives the accumulated value
        boolean accuChannel = (((status.meters != null) && (status.meters.size() > 1) && !profile.isRoller
                && !profile.isRGBW2) || ((status.emeters != null && status.emeters.size() > 1)));
        addChannel(thing, add, accuChannel, CHGR_DEVST, CHANNEL_DEVST_ACCUWATTS);
        addChannel(thing, add, accuChannel, CHGR_DEVST, CHANNEL_DEVST_ACCUTOTAL);
        addChannel(thing, add, accuChannel && (status.emeters != null), CHGR_DEVST, CHANNEL_DEVST_ACCURETURNED);
        addChannel(thing, add, status.voltage != null || profile.settings.supplyVoltage != null, CHGR_DEVST,
                CHANNEL_DEVST_VOLTAGE);
        addChannel(thing, add,
                profile.status.uptime != null && (!profile.hasBattery || profile.isMotion || profile.isTRV), CHGR_DEVST,
                CHANNEL_DEVST_UPTIME);
        addChannel(thing, add, true, CHGR_DEVST, CHANNEL_DEVST_UPDATE);
        addChannel(thing, add, true, CHGR_DEVST, CHANNEL_DEVST_HEARTBEAT);
        addChannel(thing, add, profile.settings.ledPowerDisable != null, CHGR_DEVST, CHANNEL_LED_POWER_DISABLE);
        addChannel(thing, add, profile.settings.ledStatusDisable != null, CHGR_DEVST, CHANNEL_LED_STATUS_DISABLE); // WiFi
        addChannel(thing, add, profile.settings.calibrated != null, CHGR_DEVST, CHANNEL_DEVST_CALIBRATED);

        return add;
    }

    /**
     * Auto-create relay channels depending on relay type/mode
     *
     * @return ArrayList<Channel> of channels to be added to the thing
     */
    public static Map<String, Channel> createRelayChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsRelay rstatus, int idx) {
        Map<String, Channel> add = new LinkedHashMap<>();
        String group = profile.getControlGroup(idx);

        if (profile.settings.relays != null) {
            ShellySettingsRelay rs = profile.settings.relays.get(idx);
            addChannel(thing, add, rs.ison != null, group, CHANNEL_OUTPUT);
            addChannel(thing, add, rs.name != null, group, CHANNEL_OUTPUT_NAME);

            boolean timer = rs.hasTimer != null || rstatus.hasTimer != null; // Dimmer 1/2 have
            addChannel(thing, add, timer, group, CHANNEL_TIMER_ACTIVE);
            addChannel(thing, add, rs.autoOn != null, group, CHANNEL_TIMER_AUTOON);
            addChannel(thing, add, rs.autoOff != null, group, CHANNEL_TIMER_AUTOOFF);
        }

        // Shelly 1/1PM and Plus 1/1PM Addon
        if (profile.settings.extSwitch != null && profile.settings.extSwitch.input0 != null
                && idx == getInteger(profile.settings.extSwitch.input0.relayNum)) {
            addChannel(thing, add, true, CHGR_SENSOR, CHANNEL_ESENSOR_INPUT + (idx + 1));
        }
        if (profile.status.extTemperature != null) {
            addChannel(thing, add, profile.status.extTemperature.sensor1 != null, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP1);
            addChannel(thing, add, profile.status.extTemperature.sensor2 != null, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP2);
            addChannel(thing, add, profile.status.extTemperature.sensor3 != null, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP3);
        }
        addChannel(thing, add, profile.status.extHumidity != null, CHGR_SENSOR, CHANNEL_ESENSOR_HUMIDITY);
        addChannel(thing, add, profile.status.extVoltage != null, CHGR_SENSOR, CHANNEL_ESENSOR_VOLTAGE);
        addChannel(thing, add, profile.status.extDigitalInput != null, CHGR_SENSOR, CHANNEL_ESENSOR_DIGITALINPUT);
        addChannel(thing, add, profile.status.extAnalogInput != null, CHGR_SENSOR, CHANNEL_ESENSOR_ANALOGINPUT);

        return add;
    }

    public static Map<String, Channel> createDimmerChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsStatus dstatus, int idx) {
        Map<String, Channel> add = new LinkedHashMap<>();
        String group = profile.getControlGroup(idx);

        // Shelly Dimmer has an additional brightness channel
        addChannel(thing, add, profile.isDimmer, group, CHANNEL_BRIGHTNESS);

        if (profile.settings.dimmers != null) {
            ShellySettingsDimmer ds = profile.settings.dimmers.get(idx);
            addChannel(thing, add, ds.autoOn != null, group, CHANNEL_TIMER_AUTOON);
            addChannel(thing, add, ds.autoOff != null, group, CHANNEL_TIMER_AUTOOFF);
            ShellyShortLightStatus dss = dstatus.dimmers.get(idx);
            addChannel(thing, add, dss != null && dss.hasTimer != null, group, CHANNEL_TIMER_ACTIVE);
        }
        return add;
    }

    public static Map<String, Channel> createLightChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellyStatusLightChannel status, int idx) {
        Map<String, Channel> add = new LinkedHashMap<>();
        String group = profile.getControlGroup(idx);

        if (profile.settings.lights != null) {
            ShellySettingsRgbwLight light = profile.settings.lights.get(idx);
            String whiteGroup = profile.isRGBW2 ? group : CHANNEL_GROUP_WHITE_CONTROL;
            // Create power channel in color mode and brightness channel in white mode
            addChannel(thing, add, profile.inColor, group, CHANNEL_LIGHT_POWER);
            addChannel(thing, add, light.autoOn != null, group, CHANNEL_TIMER_AUTOON);
            addChannel(thing, add, light.autoOff != null, group, CHANNEL_TIMER_AUTOOFF);
            addChannel(thing, add, status.hasTimer != null, group, CHANNEL_TIMER_ACTIVE);
            addChannel(thing, add, light.brightness != null, whiteGroup, CHANNEL_BRIGHTNESS);
            addChannel(thing, add, light.temp != null, whiteGroup, CHANNEL_COLOR_TEMP);
        }

        return add;
    }

    public static Map<String, Channel> createInputChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsStatus status) {
        Map<String, Channel> add = new LinkedHashMap<>();
        if (status.inputs != null) {
            // Create channels per input. For devices with more than 1 input (Dimmer, 1L) multiple channel sets are
            // created by adding the index to the channel name
            for (int i = 0; i < profile.numInputs; i++) {
                String group = profile.getInputGroup(i);
                String suffix = profile.getInputSuffix(i); // multi ? String.valueOf(i + 1) : "";
                addChannel(thing, add, true, group, CHANNEL_INPUT + suffix);
                addChannel(thing, add, true, group,
                        (!profile.isRoller ? CHANNEL_BUTTON_TRIGGER + suffix : CHANNEL_EVENT_TRIGGER));
                if (profile.inButtonMode(i)) {
                    ShellyInputState input = status.inputs.get(i);
                    addChannel(thing, add, input.event != null, group, CHANNEL_STATUS_EVENTTYPE + suffix);
                    addChannel(thing, add, input.eventCount != null, group, CHANNEL_STATUS_EVENTCOUNT + suffix);
                }
            }
        } else if (status.input != null) {
            // old RGBW2 firmware
            String group = profile.getInputGroup(0);
            addChannel(thing, add, true, group, CHANNEL_INPUT);
            addChannel(thing, add, true, group, CHANNEL_BUTTON_TRIGGER);
        }
        return add;
    }

    public static Map<String, Channel> createRollerChannels(Thing thing, final ShellyRollerStatus roller) {
        Map<String, Channel> add = new LinkedHashMap<>();
        addChannel(thing, add, true, CHGR_ROLLER, CHANNEL_ROL_CONTROL_CONTROL);
        addChannel(thing, add, true, CHGR_ROLLER, CHANNEL_ROL_CONTROL_STATE);
        addChannel(thing, add, true, CHGR_ROLLER, CHANNEL_EVENT_TRIGGER);
        addChannel(thing, add, roller.currentPos != null, CHGR_ROLLER, CHANNEL_ROL_CONTROL_POS);
        addChannel(thing, add, roller.stopReason != null, CHGR_ROLLER, CHANNEL_ROL_CONTROL_STOPR);
        addChannel(thing, add, roller.safetySwitch != null, CHGR_ROLLER, CHANNEL_ROL_CONTROL_SAFETY);

        ShellyThingInterface handler = (ShellyThingInterface) thing.getHandler();
        if (handler != null) {
            ShellySettingsGlobal settings = handler.getProfile().settings;
            if (getBool(settings.favoritesEnabled) && settings.favorites != null) {
                addChannel(thing, add, roller.currentPos != null, CHGR_ROLLER, CHANNEL_ROL_CONTROL_FAV);
            }
        }
        return add;
    }

    public static Map<String, Channel> createMeterChannels(Thing thing, final ShellySettingsMeter meter, String group) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();
        addChannel(thing, newChannels, meter.power != null, group, CHANNEL_METER_CURRENTWATTS);
        addChannel(thing, newChannels, meter.total != null, group, CHANNEL_METER_TOTALKWH);
        addChannel(thing, newChannels, meter.counters != null && meter.counters[0] != null, group,
                CHANNEL_METER_LASTMIN1);
        addChannel(thing, newChannels, meter.timestamp != null, group, CHANNEL_LAST_UPDATE);
        return newChannels;
    }

    public static Map<String, Channel> createEMeterChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsEMeter emeter, String group) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();
        addChannel(thing, newChannels, emeter.power != null, group, CHANNEL_METER_CURRENTWATTS);
        addChannel(thing, newChannels, emeter.total != null, group, CHANNEL_METER_TOTALKWH);
        addChannel(thing, newChannels, emeter.totalReturned != null, group, CHANNEL_EMETER_TOTALRET);
        addChannel(thing, newChannels, emeter.reactive != null, group, CHANNEL_EMETER_REACTWATTS);
        addChannel(thing, newChannels, emeter.voltage != null, group, CHANNEL_EMETER_VOLTAGE);
        addChannel(thing, newChannels, emeter.current != null, group, CHANNEL_EMETER_CURRENT);
        addChannel(thing, newChannels, emeter.pf != null, group, CHANNEL_EMETER_PFACTOR); // EM has no PF. but power
        addChannel(thing, newChannels, emeter.total != null && profile.numMeters > 1, group, CHANNEL_EMETER_RESETTOTAL); // 3EM
        addChannel(thing, newChannels, true, group, CHANNEL_LAST_UPDATE);
        return newChannels;
    }

    public static Map<String, Channel> createSensorChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellyStatusSensor sdata) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();

        // Sensor data
        addChannel(thing, newChannels, sdata.tmp != null || sdata.thermostats != null, CHANNEL_GROUP_SENSOR,
                CHANNEL_SENSOR_TEMP);
        addChannel(thing, newChannels, sdata.hum != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_HUM);
        addChannel(thing, newChannels, sdata.lux != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_LUX);
        addChannel(thing, newChannels, sdata.lux != null && sdata.lux.illumination != null, CHANNEL_GROUP_SENSOR,
                CHANNEL_SENSOR_ILLUM);
        addChannel(thing, newChannels, sdata.flood != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD);
        addChannel(thing, newChannels, sdata.smoke != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_SMOKE);
        addChannel(thing, newChannels, sdata.mute != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MUTE);
        addChannel(thing, newChannels, profile.settings.externalPower != null || sdata.charger != null, CHGR_DEVST,
                CHANNEL_DEVST_CHARGER);
        addChannel(thing, newChannels, sdata.motion != null || (sdata.sensor != null && sdata.sensor.motion != null),
                CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MOTION);
        if (sdata.sensor != null) { // DW, Sense or Motion
            addChannel(thing, newChannels, sdata.sensor.state != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE); // DW/DW2
            addChannel(thing, newChannels, sdata.sensor.motionActive != null, CHANNEL_GROUP_SENSOR, // Motion
                    CHANNEL_SENSOR_MOTION_ACT);
            addChannel(thing, newChannels, sdata.sensor.motionTimestamp != null, CHANNEL_GROUP_SENSOR, // Motion
                    CHANNEL_SENSOR_MOTION_TS);
            addChannel(thing, newChannels, sdata.sensor.vibration != null, CHANNEL_GROUP_SENSOR,
                    CHANNEL_SENSOR_VIBRATION);
        }
        if (sdata.accel != null) { // DW2
            addChannel(thing, newChannels, sdata.accel.tilt != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TILT);
        }

        // Gas
        if (sdata.gasSensor != null) {
            addChannel(thing, newChannels, sdata.gasSensor.selfTestState != null, CHGR_DEVST, CHANNEL_DEVST_SELFTTEST);
            addChannel(thing, newChannels, sdata.gasSensor.sensorState != null, CHANNEL_GROUP_SENSOR,
                    CHANNEL_SENSOR_SSTATE);
            addChannel(thing, newChannels, sdata.concentration != null && sdata.concentration.ppm != null,
                    CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PPM);
            addChannel(thing, newChannels, sdata.valves != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VALVE);
            addChannel(thing, newChannels, sdata.gasSensor.sensorState != null, CHANNEL_GROUP_SENSOR,
                    CHANNEL_SENSOR_ALARM_STATE);
        }

        // Sense
        addChannel(thing, newChannels, profile.isSense, CHANNEL_GROUP_SENSOR, CHANNEL_SENSE_KEY);

        // UNI
        addChannel(thing, newChannels, sdata.adcs != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VOLTAGE);

        // TRV
        if (profile.isTRV) {
            addChannel(thing, newChannels, true, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SETTEMP);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_BCONTROL);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_BTIMER);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_POSITION);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_MODE);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SCHEDULE);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_OPEN);
        }

        // Battery
        if (sdata.bat != null) {
            addChannel(thing, newChannels, sdata.bat.value != null, CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL);
            addChannel(thing, newChannels, sdata.bat.value != null, CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LOW);
        }

        addChannel(thing, newChannels, sdata.sensorError != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ERROR);
        addChannel(thing, newChannels, sdata.actReasons != null, CHGR_DEVST, CHANNEL_DEVST_WAKEUP);
        addChannel(thing, newChannels, true, profile.isButton ? CHANNEL_GROUP_STATUS : CHANNEL_GROUP_SENSOR,
                CHANNEL_LAST_UPDATE);
        return newChannels;
    }

    public ChannelTypeUID getChannelTypeUID(String channelId) {
        ShellyChannel channelDef = getDefinition(channelId);
        if (channelDef != null) {
            return new ChannelTypeUID(BINDING_ID, channelDef.typeId);
        }
        throw new IllegalArgumentException("Invalid channelId:" + channelId);
    }

    private static void addChannel(Thing thing, Map<String, Channel> newChannels, boolean supported, String group,
            String channelName) throws IllegalArgumentException {
        if (supported) {
            String channelId = group + "#" + channelName;
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            ShellyChannel channelDef = getDefinition(channelId);
            if (channelDef != null) {
                ChannelTypeUID channelTypeUID = channelDef.typeId.contains("system:")
                        ? new ChannelTypeUID(channelDef.typeId)
                        : new ChannelTypeUID(BINDING_ID, channelDef.typeId);
                ChannelBuilder builder;
                if (channelDef.typeId.equalsIgnoreCase("system:button")) {
                    builder = ChannelBuilder.create(channelUID, null).withKind(ChannelKind.TRIGGER);
                } else {
                    builder = ChannelBuilder.create(channelUID, channelDef.itemType);
                }
                if (!channelDef.label.isEmpty()) {
                    char grseq = lastChar(group);
                    char chseq = lastChar(channelName);
                    char sequence = isDigit(chseq) ? chseq : grseq;
                    String label = !isDigit(sequence) ? channelDef.label : channelDef.label + " " + sequence;
                    builder.withLabel(label);
                }
                if (!channelDef.description.isEmpty()) {
                    builder.withDescription(channelDef.description);
                }
                newChannels.put(channelId, builder.withType(channelTypeUID).build());
            }
        }
    }

    public List<StateOption> getStateOptions(ChannelTypeUID uid) {
        List<StateOption> options = new ArrayList<>();
        for (OptionEntry oe : stateOptions) {
            if (oe.uid.equals(uid)) {
                options.add(new StateOption(oe.key, oe.value));
            }
        }
        return options;
    }

    public void addStateOption(String channelId, String key, String value) {
        ChannelTypeUID uid = getChannelTypeUID(channelId);
        stateOptions.addIfAbsent(new OptionEntry(uid, key, value));
    }

    public void clearStateOptions(String channelId) {
        ChannelTypeUID uid = getChannelTypeUID(channelId);
        for (OptionEntry oe : stateOptions) {
            if (oe.uid.equals(uid)) {
                stateOptions.remove(oe);
            }
        }
    }

    public class ShellyChannel {
        private final ShellyTranslationProvider messages;
        public String group = "";
        public String groupLabel = "";
        public String groupDescription = "";

        public String channel = "";
        public String label = "";
        public String description = "";
        public String itemType = "";
        public String typeId = "";
        public String category = "";
        public Set<String> tags = new HashSet<>();
        public @Nullable Unit<?> unit;
        public Optional<Integer> min = Optional.empty();
        public Optional<Integer> max = Optional.empty();
        public Optional<Integer> step = Optional.empty();
        public Optional<String> pattern = Optional.empty();

        public ShellyChannel(ShellyTranslationProvider messages, String group, String channel, String typeId,
                String itemType, String... category) {
            this.messages = messages;
            this.group = group;
            this.channel = channel;
            this.itemType = itemType;
            this.typeId = typeId;

            groupLabel = getText(PREFIX_GROUP + group + ".label");
            if (groupLabel.contains(PREFIX_GROUP)) {
                groupLabel = "";
            }
            groupDescription = getText(PREFIX_GROUP + group + ".description");
            if (groupDescription.contains(PREFIX_GROUP)) {
                groupDescription = "";
            }
            label = getText(PREFIX_CHANNEL + typeId.replace(':', '.') + ".label");
            if (label.contains(PREFIX_CHANNEL)) {
                label = "";
            }
            description = getText(PREFIX_CHANNEL + typeId + ".description");
            if (description.contains(PREFIX_CHANNEL)) {
                description = "";
            }
        }

        public String getChanneId() {
            return group + "#" + channel;
        }

        public String getGroupLabel() {
            return getGroupAttribute("group");
        }

        public String getGroupDescription() {
            return getGroupAttribute("group");
        }

        public String getLabel() {
            return getChannelAttribute("label");
        }

        public String getDescription() {
            return getChannelAttribute("description");
        }

        public boolean getAdvanced() {
            String attr = getChannelAttribute("advanced");
            return attr.isEmpty() ? false : Boolean.valueOf(attr);
        }

        public boolean getReadyOnly() {
            String attr = getChannelAttribute("readOnly");
            return attr.isEmpty() ? false : Boolean.valueOf(attr);
        }

        public String getCategory() {
            return getChannelAttribute("category");
        }

        public String getMin() {
            return getChannelAttribute("min");
        }

        public String getMax() {
            return getChannelAttribute("max");
        }

        public String getStep() {
            return getChannelAttribute("step");
        }

        public String getPattern() {
            return getChannelAttribute("pattern");
        }

        public String getGroupAttribute(String attribute) {
            String key = PREFIX_GROUP + group + "." + attribute;
            String value = messages.getText(key);
            return !value.equals(key) ? value : "";
        }

        public String getChannelAttribute(String attribute) {
            String key = PREFIX_CHANNEL + channel + "." + attribute;
            String value = messages.getText(key);
            return !value.equals(key) ? value : "";
        }

        private String getText(String key) {
            return messages.get(key);
        }
    }

    public static class ChannelMap {
        private final Map<String, ShellyChannel> map = new HashMap<>();

        private ChannelMap add(ShellyChannel def) {
            map.put(def.getChanneId(), def);
            return this;
        }

        public ShellyChannel get(String channelName) throws IllegalArgumentException {
            ShellyChannel def = null;
            if (channelName.contains("#")) {
                def = map.get(channelName);
                if (def != null) {
                    return def;
                }
            }
            for (HashMap.Entry<String, ShellyChannel> entry : map.entrySet()) {
                if (entry.getValue().channel.contains("#" + channelName)) {
                    def = entry.getValue();
                    break;
                }
            }

            if (def == null) {
                throw new IllegalArgumentException("Channel definition for " + channelName + " not found!");
            }

            return def;
        }
    }
}
