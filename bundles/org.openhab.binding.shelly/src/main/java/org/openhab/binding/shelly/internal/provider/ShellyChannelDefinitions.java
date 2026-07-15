/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyEMNCurrentSettings;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyEMNCurrentStatus;
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
import org.openhab.binding.shelly.internal.handler.ShellyComponents;
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
 * The {@link #CHANNEL_DEFINITIONS} defines channel information for dynamically created channels. Those will be
 * added on the first thing status update
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = ShellyChannelDefinitions.class)
public class ShellyChannelDefinitions {

    public static final String ITEMT_STRING = "String"; // Text, event types, modes
    public static final String ITEMT_NUMBER = "Number"; // Plain dimensionless number
    public static final String ITEMT_SWITCH = "Switch"; // On/Off
    public static final String ITEMT_CONTACT = "Contact"; // Contact state Door/window sensors
    public static final String ITEMT_ROLLER = "Rollershutter"; // Roller shutter control
    public static final String ITEMT_DIMMER = "Dimmer"; // Brightness (0–100%)
    public static final String ITEMT_LOCATION = "Location";
    public static final String ITEMT_DATETIME = "DateTime";
    public static final String ITEMT_TEMP = "Number:Temperature"; // Temperature with unit
    public static final String ITEMT_LUX = "Number:Illuminance";
    public static final String ITEMT_POWER = "Number:Power"; // Watts
    public static final String ITEMT_ENERGY = "Number:Energy"; // kWh
    public static final String ITEMT_VOLT = "Number:ElectricPotential"; // Volts
    public static final String ITEMT_AMP = "Number:ElectricCurrent"; // Amperes
    public static final String ITEMT_FREQ = "Number:Frequency";
    public static final String ITEMT_ANGLE = "Number:Angle"; // Degrees (tilt, rotation)
    public static final String ITEMT_DISTANCE = "Number:Length"; // Meters
    public static final String ITEMT_SPEED = "Number:Speed";
    public static final String ITEMT_VOLUME = "Number:Volume";
    public static final String ITEMT_TIME = "Number:Time"; // Seconds
    public static final String ITEMT_PERCENT = "Number:Dimensionless"; // 0–100% (battery, humidity)

    // shortcuts to avoid line breaks (make code more readable)
    private static final String CHGR_DEVST = CHANNEL_GROUP_DEV_STATUS;
    private static final String CHGR_RELAY = CHANNEL_GROUP_RELAY_CONTROL;
    private static final String CHGR_ROLLER = CHANNEL_GROUP_ROL_CONTROL;
    private static final String CHGR_LIGHT = CHANNEL_GROUP_LIGHT_CONTROL;
    private static final String CHGR_LIGHTCH = CHANNEL_GROUP_LIGHT_CHANNEL;
    private static final String CHGR_STATUS = CHANNEL_GROUP_STATUS;
    private static final String CHGR_METER = CHANNEL_GROUP_METER;
    private static final String CHGR_EMN = CHANNEL_GROUP_NMETER;
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

        /*
         * Channel registry - Design Principle
         *
         * Channels are **not** declared statically in thing-type XML. They are added to the thing
         * programmatically after the first successful device status read.
         *
         * This allows:
         *
         * - Conditional channels based on firmware/settings (e.g. autoOn timer only if the
         * device actually reports autoOn in its settings)
         * - Indexed channels for multi-relay/multi-meter devices (relay1, relay2, meter2)
         * - Add-on sensor channels only when an add-on module is physically connected
         *
         *
         * Channel Registry
         *
         * CHANNEL_DEFINITIONS is a static ChannelMap populated once at OSGi activation.
         * Each entry maps a group#channel key to a ShellyChannel descriptor:
         *
         * new ShellyChannel(translationProvider, groupId, channelId, channelTypeId, itemType)
         *
         * The channelTypeId references either a system channel type (e.g. system:power) or
         * a binding-specific type defined in device.xml (e.g. meterWatts).
         *
         * Dynamic Channel Creation - All createXXX() methods follow the same pattern:
         *
         * public static Map<String, Channel> createRelayChannels(Thing thing,
         * ShellyDeviceProfile profile, ShellySettingsRelay rstatus, int idx) {
         * Map<String, Channel> add = new LinkedHashMap<>();
         * String group = profile.getControlGroup(idx); // "relay1", "relay2", ...
         *
         * // Only add a channel if the device actually reports the field
         * addChannel(thing, add, getBool(rs.isValid), group, CHANNEL_OUTPUT);
         * addChannel(thing, add, rs.autoOn != null, group, CHANNEL_TIMER_AUTOON);
         * addChannel(thing, add, rs.autoOff != null, group, CHANNEL_TIMER_AUTOOFF);
         * ...
         * return add;
         * }
         *
         * addChannel(thing, add, condition, group, channelId) creates a Channel from the
         * type and item type registered in CHANNEL_DEFINITIONS, and inserts it into add only
         * when condition is true and the channel does not already exist on the thing.
         *
         * The collected maps are merged and applied in ShellyBaseHandler.updateChannelDefinitions():
         */

        // Device
        CHANNEL_DEFINITIONS
                // Device
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_NAME, "deviceName", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_GATEWAY, "gatewayDevice", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ITEMP, "system:indoor-temperature", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_WAKEUP, "sensorWakeup", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCUWATTS, "meterAccuWatts", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCUMULATEDPOWER, "accumulatedPower", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCUTOTAL, "meterAccuTotal", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_TOTALENERGY, "totalEnergy", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCURETURNED, "meterAccuReturned", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCURETURNEDENERGY, "accumulatedReturnedEnergy",
                        ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_ACCUAPPARENT, "meterAccuApparent", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_VOLTAGE, "supplyVoltage", ITEMT_VOLT))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_CHARGER, "charger", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_LED_STATUS_DISABLE, "ledStatusDisable", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_LED_POWER_DISABLE, "ledPowerDisable", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_SELFTTEST, "selfTest", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_UPTIME, "uptime", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_HEARTBEAT, "heartBeat", ITEMT_DATETIME))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_UPDATE, "updateAvailable", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_CALIBRATED, "calibrated", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_DEVST, CHANNEL_DEVST_FIRMWARE, "deviceFirmware", ITEMT_STRING))

                // Relay
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_OUTPUT_NAME, "outputName", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_RELAY, CHANNEL_OUTPUT, "relayOutput", ITEMT_SWITCH))
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
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_CURRENTPOWER, "currentPower", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_TOTALKWH, "meterTotal", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_TOTALENERGY, "totalEnergy", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_LASTMIN1, "lastPower1", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_ENERGYAVG1MIN, "energyAvg1Min", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_LAST_UPDATE, "lastUpdate", ITEMT_DATETIME))

                // EMeter
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_TOTALRET, "meterReturned", ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_RETURNEDENERGY, "meterReturnedEnergy",
                        ITEMT_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_REACTWATTS, "meterReactive", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_REACTPOWER, "meterReactivePower", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_APPARENT, "meterApparentPower", ITEMT_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_VOLTAGE, "meterVoltage", ITEMT_VOLT))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_CURRENT, "meterCurrent", ITEMT_AMP))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_PFACTOR, "meterPowerFactor", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_FREQUENCY, "meterFrequency", ITEMT_FREQ))

                // 3EM: neutral current (emeter_n)
                .add(new ShellyChannel(m, CHGR_EMN, CHANNEL_NMETER_CURRENT, "ncurrent", ITEMT_AMP))
                .add(new ShellyChannel(m, CHGR_EMN, CHANNEL_NMETER_IXSUM, "ixsum", ITEMT_AMP))
                .add(new ShellyChannel(m, CHGR_EMN, CHANNEL_NMETER_MTRESHHOLD, "nmTreshhold", ITEMT_AMP))
                .add(new ShellyChannel(m, CHGR_EMN, CHANNEL_NMETER_THRESHOLD, "nmThreshold", ITEMT_AMP))
                .add(new ShellyChannel(m, CHGR_EMN, CHANNEL_NMETER_MISMATCH, "nmismatch", ITEMT_SWITCH))

                // Sensors
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_TEMP, "system:indoor-temperature", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_HUM, "system:atmospheric-humidity",
                        ITEMT_PERCENT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_LUX, "sensorLux", ITEMT_LUX))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ILLUM, "sensorIllumination", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_VOLTAGE, "sensorADC", ITEMT_VOLT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_STATE, "sensorContact", ITEMT_CONTACT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_SSTATE, "sensorState", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_TILT, "sensorTilt", ITEMT_ANGLE))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_MOTION, "sensorMotion", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_MOTION_TS, "motionTimestamp", ITEMT_DATETIME))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_MOTION_ACT, "motionActive", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_VIBRATION, "sensorVibration", ITEMT_SWITCH))
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

                // BLU Remote
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_CHANNEL, "sensorChannel", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ROTATIONX, "sensorRotationX", ITEMT_ANGLE))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ROTATIONY, "sensorRotationY", ITEMT_ANGLE))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ROTATIONZ, "sensorRotationZ", ITEMT_ANGLE))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_DIRECTION, "sensorDirection", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_STEPS, "sensorSteps", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_DISTANCE, "sensorDistance", ITEMT_DISTANCE))

                // Button/ix3
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_INPUT, "inputState", ITEMT_SWITCH))
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_STATUS_EVENTTYPE, "lastEvent", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_STATUS_EVENTCOUNT, "eventCount", ITEMT_NUMBER))
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_BUTTON_TRIGGER, "system:button", ITEMT_STRING))
                .add(new ShellyChannel(m, CHGR_STATUS, CHANNEL_LAST_UPDATE, "lastUpdate", ITEMT_DATETIME))

                // Addon with external sensors
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP1, "system:outdoor-temperature", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP2, "system:outdoor-temperature", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP3, "system:outdoor-temperature", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP4, "system:outdoor-temperature", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP5, "system:outdoor-temperature", ITEMT_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENSOR_HUMIDITY, "system:atmospheric-humidity",
                        ITEMT_PERCENT))
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
        String group = substringBefore(channelName, ChannelUID.CHANNEL_GROUP_SEPARATOR);
        String channel = substringAfter(channelName, ChannelUID.CHANNEL_GROUP_SEPARATOR);

        if (group.startsWith(CHANNEL_GROUP_METER)) {
            group = CHANNEL_GROUP_METER; // map meter1..n to meter
        } else if (group.startsWith(CHANNEL_GROUP_RELAY_CONTROL)) {
            group = CHANNEL_GROUP_RELAY_CONTROL; // map meter1..n to meter
        } else if (group.startsWith(CHANNEL_GROUP_LIGHT_CHANNEL)) {
            group = CHANNEL_GROUP_LIGHT_CHANNEL;
        } else if (group.startsWith(CHANNEL_GROUP_STATUS)) {
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

        String channelId = group + ChannelUID.CHANNEL_GROUP_SEPARATOR + channel;
        return CHANNEL_DEFINITIONS.get(channelId);
    }

    /**
     * Auto-create relay channels depending on relay type/mode
     *
     * @return {@code ArrayList<Channel>} of channels to be added to the thing
     */
    public static Map<String, Channel> createDeviceChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsStatus status) {
        Map<String, Channel> add = new LinkedHashMap<>();

        addChannel(thing, add, !profile.fwVersion.isEmpty() || profile.isBlu, CHGR_DEVST, CHANNEL_DEVST_FIRMWARE);

        addChannel(thing, add, profile.settings.name != null, CHGR_DEVST, CHANNEL_DEVST_NAME);
        addChannel(thing, add, !profile.gateway.isEmpty() || profile.isBlu, CHGR_DEVST, CHANNEL_DEVST_GATEWAY);

        if (!profile.isSensor && !profile.isIX
                && ((status.temperature != null && getDouble(status.temperature) != SHELLY_API_INVTEMP)
                        || (status.tmp != null && getDouble(status.tmp.tC) != SHELLY_API_INVTEMP))) {
            // Only some devices report the internal device temp
            boolean hasTemp = !profile.isLight
                    && (status.temperature != null || (status.tmp != null && !profile.isSensor));
            if (hasTemp && profile.isGen2 && (profile.numMeters > 0 && !profile.hasRelays)) // Shely Plus PM Mini
            {
                hasTemp = false;
            }
            addChannel(thing, add, hasTemp, CHGR_DEVST, CHANNEL_DEVST_ITEMP);
        }
        addChannel(thing, add, profile.settings.sleepTime != null, CHGR_SENSOR, CHANNEL_SENSOR_SLEEPTIME);

        // Any multi-meter device (relay or pure meter like ProEM50) gets device-level accumulated channels
        boolean accuChannel = profile.numMeters > 1 && !profile.isRoller && !profile.isRGBW2;
        addChannel(thing, add, accuChannel, CHGR_DEVST, CHANNEL_DEVST_ACCUWATTS);
        addChannel(thing, add, accuChannel, CHGR_DEVST, CHANNEL_DEVST_ACCUTOTAL);
        // Gate returned/apparent totals on the device actually being a dedicated EMeter (3EM or EM50).
        // Relay-PM devices (2PM, Plus 1PM) have status.emeters but never populate totalReturned or
        // apparentPower, so these channels would be phantom (created but permanently UNDEF).
        boolean hasReturnedEnergy = accuChannel && (profile.is3EM || profile.isEM50);
        addChannel(thing, add, hasReturnedEnergy, CHGR_DEVST, CHANNEL_DEVST_ACCURETURNED);
        addChannel(thing, add, hasReturnedEnergy, CHGR_DEVST, CHANNEL_DEVST_ACCUAPPARENT);
        addChannel(thing, add, status.voltage != null || profile.settings.supplyVoltage != null, CHGR_DEVST,
                CHANNEL_DEVST_VOLTAGE);
        addChannel(thing, add,
                profile.status.uptime != null && (!profile.hasBattery || profile.isMotion || profile.isTRV), CHGR_DEVST,
                CHANNEL_DEVST_UPTIME);
        addChannel(thing, add, true, CHGR_DEVST, CHANNEL_DEVST_HEARTBEAT);
        addChannel(thing, add, profile.settings.ledPowerDisable != null, CHGR_DEVST, CHANNEL_LED_POWER_DISABLE);
        addChannel(thing, add, profile.settings.ledStatusDisable != null, CHGR_DEVST, CHANNEL_LED_STATUS_DISABLE); // WiFi
        addChannel(thing, add, profile.settings.calibrated != null, CHGR_DEVST, CHANNEL_DEVST_CALIBRATED);

        if (!profile.isBlu) { // currently not supported for BLU devices
            addChannel(thing, add, true, CHGR_DEVST, CHANNEL_DEVST_UPDATE);
        }
        return add;
    }

    /**
     * Auto-create relay channels depending on relay type/mode
     *
     * @return {@code ArrayList<Channel>} of channels to be added to the thing
     */
    public static Map<String, Channel> createRelayChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsRelay rstatus, int idx) {
        Map<String, Channel> add = new LinkedHashMap<>();
        String group = profile.getControlGroup(idx);

        List<ShellySettingsRelay> relays = profile.settings.relays;
        if (relays != null) {
            ShellySettingsRelay rs = relays.get(idx);
            addChannel(thing, add, rs.isValid == null || rs.isValid, group, CHANNEL_OUTPUT);
            addChannel(thing, add, rs.name != null, group, CHANNEL_OUTPUT_NAME);

            boolean timer = rs.hasTimer != null || rstatus.hasTimer != null; // Dimmer 1/2 have
            addChannel(thing, add, timer, group, CHANNEL_TIMER_ACTIVE);
            addChannel(thing, add, rs.autoOn != null, group, CHANNEL_TIMER_AUTOON);
            addChannel(thing, add, rs.autoOff != null, group, CHANNEL_TIMER_AUTOOFF);
        }

        addAddonChannels(thing, profile, idx, add);

        return add;
    }

    private static void addAddonChannels(final Thing thing, final ShellyDeviceProfile profile, int idx,
            Map<String, Channel> add) {
        // Shelly 1/1PM and Plus 1/1PM Addon
        boolean addon = profile.settings.extSwitch != null && profile.settings.extSwitch.input0 != null
                && idx == getInteger(profile.settings.extSwitch.input0.relayNum);
        if (addon) {
            addChannel(thing, add, addon, CHGR_SENSOR,
                    CHANNEL_ESENSOR_INPUT + (profile.settings.extSwitch.input0.relayNum + 1));
        }
        ShellyStatusSensor.ShellyExtTemperature extTemp = profile.status.extTemperature;
        if (extTemp != null) {
            addChannel(thing, add, extTemp.sensor1 != null, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP1);
            addChannel(thing, add, extTemp.sensor2 != null, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP2);
            addChannel(thing, add, extTemp.sensor3 != null, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP3);
            addChannel(thing, add, extTemp.sensor4 != null, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP4);
            addChannel(thing, add, extTemp.sensor5 != null, CHGR_SENSOR, CHANNEL_ESENSOR_TEMP5);
        }
        ShellyStatusSensor.ShellyExtHumidity extHum = profile.status.extHumidity;
        addChannel(thing, add, extHum != null && extHum.sensor1 != null, CHGR_SENSOR, CHANNEL_ESENSOR_HUMIDITY);

        addChannel(thing, add, profile.status.extVoltage != null, CHGR_SENSOR, CHANNEL_ESENSOR_VOLTAGE);
        addChannel(thing, add, profile.status.extDigitalInput != null, CHGR_SENSOR, CHANNEL_ESENSOR_DIGITALINPUT);
        addChannel(thing, add, profile.status.extAnalogInput != null, CHGR_SENSOR, CHANNEL_ESENSOR_ANALOGINPUT);

        addChannel(thing, add, ShellyComponents.hasAddon(profile.status), CHGR_SENSOR, CHANNEL_LAST_UPDATE);
    }

    public static Map<String, Channel> createDimmerChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsStatus dstatus, int idx) {
        Map<String, Channel> add = new LinkedHashMap<>();
        String group = profile.getControlGroup(idx);

        // Shelly Dimmer has an additional brightness channel
        addChannel(thing, add, profile.isDimmer, group, CHANNEL_BRIGHTNESS);

        List<ShellySettingsDimmer> dimmers = profile.settings.dimmers;
        if (dimmers != null) {
            ShellySettingsDimmer ds = dimmers.get(idx);
            addChannel(thing, add, ds.name != null, group, CHANNEL_OUTPUT_NAME);
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

        List<ShellySettingsRgbwLight> lights = profile.settings.lights;
        if (lights != null) {
            ShellySettingsRgbwLight light = lights.get(idx);
            String whiteGroup = profile.isRGBW2 && !profile.isGen2 ? group : CHANNEL_GROUP_WHITE_CONTROL;
            // Create power channel in color mode and brightness channel in white mode
            addChannel(thing, add, profile.inColor, group, CHANNEL_LIGHT_POWER);
            addChannel(thing, add, light.autoOn != null, group, CHANNEL_TIMER_AUTOON);
            addChannel(thing, add, light.autoOff != null, group, CHANNEL_TIMER_AUTOOFF);
            addChannel(thing, add, status.hasTimer != null, group, CHANNEL_TIMER_ACTIVE);
            addChannel(thing, add, status.brightness != null, whiteGroup, CHANNEL_BRIGHTNESS);
            addChannel(thing, add, status.temp != null, whiteGroup, CHANNEL_COLOR_TEMP);
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
                addChannel(thing, add, !profile.isBlu && !profile.isButton && !profile.isMultiButton, group,
                        CHANNEL_INPUT + suffix);
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

    public static Map<String, Channel> createRollerChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellyRollerStatus roller) {
        Map<String, Channel> add = new LinkedHashMap<>();
        addChannel(thing, add, true, CHGR_ROLLER, CHANNEL_ROL_CONTROL_CONTROL);
        addChannel(thing, add, true, CHGR_ROLLER, CHANNEL_ROL_CONTROL_STATE);
        addChannel(thing, add, true, CHGR_ROLLER, CHANNEL_EVENT_TRIGGER);
        addChannel(thing, add, true, CHGR_ROLLER, CHANNEL_ROL_CONTROL_POS);
        addChannel(thing, add, roller.stopReason != null, CHGR_ROLLER, CHANNEL_ROL_CONTROL_STOPR);
        addChannel(thing, add, roller.safetySwitch != null, CHGR_ROLLER, CHANNEL_ROL_CONTROL_SAFETY);

        ShellyThingInterface handler = (ShellyThingInterface) thing.getHandler();
        if (handler != null) {
            ShellySettingsGlobal settings = handler.getProfile().settings;
            if (getBool(settings.favoritesEnabled) && settings.favorites != null) {
                addChannel(thing, add, true, CHGR_ROLLER, CHANNEL_ROL_CONTROL_FAV);
            }
        }

        addAddonChannels(thing, profile, 0, add);

        return add;
    }

    public static Map<String, Channel> createMeterChannels(Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsMeter meter, String group) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();
        Double[] counters = meter.counters;
        boolean hasCounter = counters != null && counters.length > 0 && counters[0] != null;
        addChannel(thing, newChannels, meter.power != null, group, CHANNEL_METER_CURRENTWATTS);
        addChannel(thing, newChannels, meter.total != null, group, CHANNEL_METER_TOTALKWH);
        // lastPower1 (W, deprecated) and energyAvg1Min (Wh) are always created together. Both channels
        // receive updates so existing items linked to lastPower1 keep working without re-discovery.
        addChannel(thing, newChannels, hasCounter, group, CHANNEL_METER_LASTMIN1);
        addChannel(thing, newChannels, hasCounter, group, CHANNEL_METER_ENERGYAVG1MIN);
        addChannel(thing, newChannels, !newChannels.isEmpty(), group, CHANNEL_LAST_UPDATE);
        return newChannels;
    }

    public static Map<String, Channel> createEMeterChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsEMeter emeter, String group) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();
        // Pure data-driven: create a channel if and only if the device populates the field.
        // Channel creation always runs during the first HTTP poll (full Shelly.GetStatus response),
        // so all fields the device supports are present. WS NotifyStatus pushes arrive after
        // channelsCreated=true and never trigger this path.
        // The totalKWH/totalEnergy and returnedKWH/returnedEnergy channel pairs (deprecated id plus
        // replacement, created together) use profile flags as a safety net: emdata:0 (accumulated
        // totals) arrives as a separate component from em:0 and may be absent on the first HTTP poll
        // for some 3EM/EM50 firmware versions.
        // 3EM always has 3 phases — create all channels unconditionally so phase C channels exist
        // even when the phase is unloaded and the first poll returns null for that field.
        // Pro EM-50 has a variable number of clamps, so remains data-driven.
        boolean always = profile.is3EM;
        addChannel(thing, newChannels, always || emeter.power != null, group, CHANNEL_METER_CURRENTWATTS);
        addChannel(thing, newChannels, always || profile.isEM50 || emeter.total != null, group, CHANNEL_METER_TOTALKWH);
        addChannel(thing, newChannels, always || profile.isEM50 || emeter.totalReturned != null, group,
                CHANNEL_EMETER_TOTALRET);
        // reactiveWatts (deprecated, type meterReactive/W as on the pre-5.2 binding) and reactivePower
        // (type meterReactivePower/VAR) are always created together. The dual-write posts the same VAR
        // state to both; on the old W-based channel it converts 1:1 (same numeric value as before).
        boolean hasReactive = (always && !profile.isGen2) || emeter.reactive != null;
        addChannel(thing, newChannels, hasReactive, group, CHANNEL_EMETER_REACTWATTS);
        addChannel(thing, newChannels, hasReactive, group, CHANNEL_EMETER_REACTPOWER);
        addChannel(thing, newChannels, always || emeter.voltage != null, group, CHANNEL_EMETER_VOLTAGE);
        addChannel(thing, newChannels, always || emeter.current != null, group, CHANNEL_EMETER_CURRENT);
        addChannel(thing, newChannels, (always && profile.isGen2) || emeter.apparentPower != null, group,
                CHANNEL_EMETER_APPARENT);
        addChannel(thing, newChannels, emeter.frequency != null, group, CHANNEL_EMETER_FREQUENCY);
        addChannel(thing, newChannels, always || emeter.pf != null, group, CHANNEL_EMETER_PFACTOR);
        // lastPower1 (W, deprecated) and energyAvg1Min (Wh) are always created together when the device
        // reports last-minute energy. Non-PM Gen2 relays (e.g. Plus 1) omit aenergy entirely — both absent.
        @Nullable
        Double @Nullable [] byMinute = emeter.energyByMinute;
        boolean hasLastMinute = byMinute != null && byMinute.length > 0 && byMinute[0] != null;
        addChannel(thing, newChannels, hasLastMinute, group, CHANNEL_METER_LASTMIN1);
        addChannel(thing, newChannels, hasLastMinute, group, CHANNEL_METER_ENERGYAVG1MIN);
        // Only add lastUpdate if this device actually has meter channels — guards against non-PM Gen2 relay
        // devices (e.g. Plus 1) where isEMeter=true but all emeter fields are permanently null.
        addChannel(thing, newChannels, !newChannels.isEmpty(), group, CHANNEL_LAST_UPDATE);
        return newChannels;
    }

    public static Map<String, Channel> createEMNCurrentChannels(final Thing thing,
            @Nullable ShellyEMNCurrentSettings settings, ShellyEMNCurrentStatus status) {
        String group = CHANNEL_GROUP_NMETER;
        Map<String, Channel> newChannels = new LinkedHashMap<>();
        addChannel(thing, newChannels, status.current != null, group, CHANNEL_NMETER_CURRENT);
        addChannel(thing, newChannels, status.ixsum != null, group, CHANNEL_NMETER_IXSUM);
        addChannel(thing, newChannels, status.mismatch != null, group, CHANNEL_NMETER_MISMATCH);
        // mismatchThreshold only available from Gen1 settings; absent on Gen2
        addChannel(thing, newChannels, settings != null && settings.mismatchThreshold != null, group,
                CHANNEL_NMETER_MTRESHHOLD);
        return newChannels;
    }

    public static Map<String, Channel> createSensorChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellyStatusSensor sdata) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();

        // Sensor data
        addChannel(thing, newChannels, sdata.tmp != null || sdata.thermostats != null, CHANNEL_GROUP_SENSOR,
                CHANNEL_SENSOR_TEMP);
        addChannel(thing, newChannels, sdata.hum != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_HUM);
        addChannel(thing, newChannels, sdata.lux != null && sdata.lux.value != null, CHANNEL_GROUP_SENSOR,
                CHANNEL_SENSOR_LUX);
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

        // Shelly DW/DW2/BLU DW
        // Depending on timing the device only reports tilt or illuminance in the 1st packet only distance or vibration
        // In this case create both channels, also if only one is included in the first packet
        if (sdata.accel != null || (profile.isBlu && profile.isDW && sdata.lux != null)) {
            addChannel(thing, newChannels, sdata.lux != null || sdata.accel.tilt != null, CHANNEL_GROUP_SENSOR,
                    CHANNEL_SENSOR_TILT);
        }

        // Depending on timing Shelly BLU distance reports in the 1st packet only distance or vibration
        // In this case create both channels, also if only one is included in the first packet
        if (sdata.distance != null || profile.isDistance) {
            addChannel(thing, newChannels, sdata.distance != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_DISTANCE);
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

        // BLU Remote
        if (profile.isRemote) {
            addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_CHANNEL);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ROTATIONX);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ROTATIONY);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ROTATIONZ);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_DIRECTION);
            addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STEPS);
        }

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

    public static @Nullable String getReplacementChannelId(String channelId) {
        int groupSeparator = channelId.indexOf(ChannelUID.CHANNEL_GROUP_SEPARATOR);
        if (groupSeparator < 0) {
            return null;
        }
        String channelName = channelId.substring(groupSeparator + 1);
        String replacement = getReplacementChannelName(channelName);
        return replacement != null ? channelId.substring(0, groupSeparator + 1) + replacement : null;
    }

    public static @Nullable String getReplacementChannelName(String channelName) {
        return switch (channelName) {
            case CHANNEL_METER_CURRENTWATTS -> CHANNEL_METER_CURRENTPOWER;
            case CHANNEL_METER_TOTALKWH -> CHANNEL_METER_TOTALENERGY;
            case CHANNEL_EMETER_TOTALRET -> CHANNEL_EMETER_RETURNEDENERGY;
            case CHANNEL_DEVST_ACCUWATTS -> CHANNEL_DEVST_ACCUMULATEDPOWER;
            case CHANNEL_EMETER_REACTWATTS -> CHANNEL_EMETER_REACTPOWER;
            case CHANNEL_DEVST_ACCUTOTAL -> CHANNEL_DEVST_TOTALENERGY;
            // CHANNEL_METER_LASTMIN1 (lastPower1, W) intentionally has NO entry: its replacement
            // energyAvg1Min is Number:Energy (Wh); forwarding the W state via the dual-write would
            // post an incompatible unit to Wh-based items on every poll ("could not be converted
            // to the item unit" warnings). All write sites post both channels explicitly instead.
            case CHANNEL_NMETER_MTRESHHOLD -> CHANNEL_NMETER_THRESHOLD;
            case CHANNEL_DEVST_ACCURETURNED -> CHANNEL_DEVST_ACCURETURNEDENERGY;
            default -> null;
        };
    }

    public static @Nullable Channel createChannel(Thing thing, String channelId) throws IllegalArgumentException {
        String group = substringBefore(channelId, ChannelUID.CHANNEL_GROUP_SEPARATOR);
        String channelName = substringAfter(channelId, ChannelUID.CHANNEL_GROUP_SEPARATOR);
        return createChannel(thing, channelId, group, channelName);
    }

    private static void addChannel(Thing thing, Map<String, Channel> newChannels, boolean supported, String group,
            String channelName) throws IllegalArgumentException {
        if (supported) {
            String channelId = group + ChannelUID.CHANNEL_GROUP_SEPARATOR + channelName;
            Channel channel = createChannel(thing, channelId, group, channelName);
            if (channel != null) {
                newChannels.put(channelId, channel);
                String replacement = getReplacementChannelName(channelName);
                if (replacement != null) {
                    addChannel(thing, newChannels, true, group, replacement);
                }
            }
        }
    }

    private static @Nullable Channel createChannel(Thing thing, String channelId, String group, String channelName)
            throws IllegalArgumentException {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
        ShellyChannel channelDef = getDefinition(channelId);
        if (channelDef == null) {
            return null;
        }

        ChannelTypeUID channelTypeUID = channelDef.typeId.contains("system:") ? new ChannelTypeUID(channelDef.typeId)
                : new ChannelTypeUID(BINDING_ID, channelDef.typeId);
        ChannelBuilder builder;
        if ("system:button".equalsIgnoreCase(channelDef.typeId)) {
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
        return builder.withType(channelTypeUID).build();
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

    private class ShellyChannel {
        private final ShellyTranslationProvider messages;
        private final String group;
        private final String channel;
        private final String label;
        private final String description;
        private final String itemType;
        private final String typeId;

        public ShellyChannel(ShellyTranslationProvider messages, String group, String channel, String typeId,
                String itemType, String... category) {
            this.messages = messages;
            this.group = group;
            this.channel = channel;
            this.itemType = itemType;
            this.typeId = typeId;

            String text = getText(PREFIX_CHANNEL + typeId.replace(':', '.') + ".label");
            label = text.startsWith(PREFIX_CHANNEL) ? "" : text;

            text = getText(PREFIX_CHANNEL + typeId + ".description");
            description = text.startsWith(PREFIX_CHANNEL) ? "" : text;
        }

        public String getChannelId() {
            return group + ChannelUID.CHANNEL_GROUP_SEPARATOR + channel;
        }

        private String getText(String key) {
            return messages.get(key);
        }
    }

    public static class ChannelMap {
        private final Map<String, ShellyChannel> map = new HashMap<>();

        private ChannelMap add(ShellyChannel def) {
            map.put(def.getChannelId(), def);
            return this;
        }

        public ShellyChannel get(String channelName) throws IllegalArgumentException {
            ShellyChannel def = null;
            if (channelName.contains(ChannelUID.CHANNEL_GROUP_SEPARATOR)) {
                def = map.get(channelName);
                if (def != null) {
                    return def;
                }
            }
            for (HashMap.Entry<String, ShellyChannel> entry : map.entrySet()) {
                if (entry.getValue().channel.contains(ChannelUID.CHANNEL_GROUP_SEPARATOR + channelName)) {
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
