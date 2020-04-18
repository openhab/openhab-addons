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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyControlRoller;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusRelay;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.util.ShellyTranslationProvider;

/**
 * The {@link ShellyChannelDefinitionsDTO} defines channel information for dynamically created channels. Those will be
 * added on the first thing status update
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelDefinitionsDTO {

    private static final ChannelMap channelDefinitions = new ChannelMap();

    private static final String CHGR_METER = CHANNEL_GROUP_METER;
    private static final String CHGR_SENSOR = CHANNEL_GROUP_SENSOR;
    private static final String CHGR_BAT = CHANNEL_GROUP_BATTERY;

    public static final String ITEM_TYPE_NUMBER = "Number";
    public static final String ITEM_TYPE_STRING = "String";
    public static final String ITEM_TYPE_DATETIME = "DateTime";
    public static final String ITEM_TYPE_TEMP = "Number:Temperature";
    public static final String ITEM_TYPE_LUX = "Number:Illuminance";
    public static final String ITEM_TYPE_POWER = "Number:Power";
    public static final String ITEM_TYPE_ENERGY = "Number:Energy";
    public static final String ITEM_TYPE_VOLT = "Number:ElectricPotential";
    public static final String ITEM_TYPE_AMP = "Number:ElectricPotential";
    public static final String ITEM_TYPE_PERCENT = "Number:Dimensionless";
    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_CONTACT = "Contact";

    public static final String PREFIX_GROUP = "definitions.shelly.group.";
    public static final String PREFIX_CHANNEL = "channel-type.shelly.";
    public static final String SUFFIX_LABEL = ".label";
    public static final String SUFFIX_DESCR = ".description";

    public ShellyChannelDefinitionsDTO(ShellyTranslationProvider m) {
        // Device: Internal Temp
        channelDefinitions
                // Device
                .add(new ShellyChannel(m, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP, "deviceTemp", ITEM_TYPE_TEMP))
                .add(new ShellyChannel(m, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_WAKEUP, "sensorWakeup",
                        ITEM_TYPE_STRING))
                .add(new ShellyChannel(m, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUWATTS, "meterAccuWatts",
                        ITEM_TYPE_POWER))
                .add(new ShellyChannel(m, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUTOTAL, "meterAccuTotal",
                        ITEM_TYPE_POWER))
                .add(new ShellyChannel(m, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCURETURNED, "meterAccuReturned",
                        ITEM_TYPE_POWER))
                .add(new ShellyChannel(m, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CHARGER, "charger", ITEM_TYPE_SWITCH))

                // RGBW2
                .add(new ShellyChannel(m, CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_INPUT, "inputState", ITEM_TYPE_SWITCH))

                // Power Meter
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_CURRENTWATTS, "meterWatts", ITEM_TYPE_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_TOTALKWH, "meterTotal", ITEM_TYPE_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_LASTMIN1, "lastPower1", ITEM_TYPE_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_LASTMIN2, "lastPower2", ITEM_TYPE_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_METER_LASTMIN3, "lastPower3", ITEM_TYPE_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_LAST_UPDATE, "lastUpdate", ITEM_TYPE_DATETIME))

                // EMeter
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_TOTALRET, "meterReturned", ITEM_TYPE_ENERGY))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_REACTWATTS, "meterReactive", ITEM_TYPE_POWER))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_VOLTAGE, "meterVoltage", ITEM_TYPE_VOLT))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_CURRENT, "meterCurrent", ITEM_TYPE_AMP))
                .add(new ShellyChannel(m, CHGR_METER, CHANNEL_EMETER_PFACTOR, "meterPowerFactor", ITEM_TYPE_NUMBER))

                // Sensors
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_TEMP, "sensorTemp", ITEM_TYPE_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_HUM, "sensorHumidity", ITEM_TYPE_PERCENT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_LUX, "sensorLux", ITEM_TYPE_LUX))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ILLUM, "sensorIllumination", ITEM_TYPE_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_VIBRATION, "sensorVibration", ITEM_TYPE_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_TILT, "sensorTilt", ITEM_TYPE_NUMBER))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_FLOOD, "sensorFlood", ITEM_TYPE_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_SMOKE, "sensorSmoke", ITEM_TYPE_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_STATE, "sensorState", ITEM_TYPE_CONTACT))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_MOTION, "sensorMotion", ITEM_TYPE_SWITCH))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_SENSOR_ERROR, "sensorError", ITEM_TYPE_STRING))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_LAST_UPDATE, "lastUpdate", ITEM_TYPE_DATETIME))

                // Addon with external sensors
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENDOR_TEMP1, "sensorExtTemp", ITEM_TYPE_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENDOR_TEMP2, "sensorExtTemp", ITEM_TYPE_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENDOR_TEMP3, "sensorExtTemp", ITEM_TYPE_TEMP))
                .add(new ShellyChannel(m, CHGR_SENSOR, CHANNEL_ESENDOR_HUMIDITY, "sensorExtHum", ITEM_TYPE_PERCENT))

                // Battery
                .add(new ShellyChannel(m, CHGR_BAT, CHANNEL_SENSOR_BAT_LEVEL, "system:battery-level",
                        ITEM_TYPE_PERCENT))
                .add(new ShellyChannel(m, CHGR_BAT, CHANNEL_SENSOR_BAT_LOW, "system:low-battery", ITEM_TYPE_SWITCH))
                .add(new ShellyChannel(m, CHGR_BAT, CHANNEL_SENSOR_BAT_VOLT, "batVoltage", ITEM_TYPE_VOLT));
    }

    public static ShellyChannel getDefinition(String channelName) throws IllegalArgumentException {
        String group = StringUtils.substringBefore(channelName, "#");
        String channel = StringUtils.substringAfter(channelName, "#");
        if (group.contains(CHANNEL_GROUP_METER)) {
            group = CHANNEL_GROUP_METER; // map meter1..n to meter
        } else if (group.contains(CHANNEL_GROUP_RELAY_CONTROL)) {
            group = CHANNEL_GROUP_RELAY_CONTROL; // map meter1..n to meter
        } else if (group.contains(CHANNEL_GROUP_LIGHT_CHANNEL)) {
            group = CHANNEL_GROUP_LIGHT_CHANNEL;
        }
        String channelId = group + "#" + channel;
        return channelDefinitions.get(channelId);
    }

    /**
     * Auto-create relay channels depending on relay type/mode
     *
     * @return ArrayList<Channel> of channels to be added to the thing
     */
    public static Map<String, Channel> createDeviceChannels(final Thing thing, final ShellyDeviceProfile profile,
            final ShellySettingsStatus status) {
        Map<String, Channel> add = new LinkedHashMap<>();
        if (!profile.isSensor) {
            // Only some devices report the internal device temp
            addChannel(thing, add, (status.tmp != null) || (status.temperature != null), CHANNEL_GROUP_DEV_STATUS,
                    CHANNEL_DEVST_ITEMP);
        }

        // RGBW2
        addChannel(thing, add, status.input != null, CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_INPUT);

        // If device has more than 1 meter the channel accumulatedWatts receives the accumulated value
        boolean accuChannel = (((status.meters != null) && (status.meters.size() > 1) && !profile.isRoller
                && !profile.isRGBW2) || ((status.emeters != null && status.emeters.size() > 1)));
        addChannel(thing, add, accuChannel, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUWATTS);
        addChannel(thing, add, accuChannel, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ACCUTOTAL);
        addChannel(thing, add, accuChannel && (status.emeters != null), CHANNEL_GROUP_DEV_STATUS,
                CHANNEL_DEVST_ACCURETURNED);

        return add;
    }

    /**
     * Auto-create relay channels depending on relay type/mode
     *
     * @return ArrayList<Channel> of channels to be added to the thing
     */
    public static Map<String, Channel> createRelayChannels(final Thing thing, final ShellyStatusRelay relays) {
        Map<String, Channel> add = new LinkedHashMap<>();

        // Shelly 1/1PM Addon
        if (relays.extTemperature != null) {
            addChannel(thing, add, relays.extTemperature.sensor1 != null, CHGR_SENSOR, CHANNEL_ESENDOR_TEMP1);
            addChannel(thing, add, relays.extTemperature.sensor2 != null, CHGR_SENSOR, CHANNEL_ESENDOR_TEMP2);
            addChannel(thing, add, relays.extTemperature.sensor3 != null, CHGR_SENSOR, CHANNEL_ESENDOR_TEMP3);
        }
        if (relays.extHumidity != null) {
            addChannel(thing, add, relays.extHumidity.sensor1 != null, CHGR_SENSOR, CHANNEL_ESENDOR_HUMIDITY);
        }
        return add;
    }

    public static Map<String, Channel> createRollerChannels(Thing thing, final ShellyControlRoller roller) {
        Map<String, Channel> add = new LinkedHashMap<>();

        // No dynamic channels so far, maybe added in the future

        return add;
    }

    public static Map<String, Channel> createMeterChannels(Thing thing, final ShellySettingsMeter meter, String group) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();
        addChannel(thing, newChannels, meter.power != null, group, CHANNEL_METER_CURRENTWATTS);
        addChannel(thing, newChannels, meter.total != null, group, CHANNEL_METER_TOTALKWH);
        if (meter.counters != null) {
            addChannel(thing, newChannels, meter.counters[0] != null, group, CHANNEL_METER_LASTMIN1);
            addChannel(thing, newChannels, meter.counters[1] != null, group, CHANNEL_METER_LASTMIN2);
            addChannel(thing, newChannels, meter.counters[2] != null, group, CHANNEL_METER_LASTMIN3);
        }
        addChannel(thing, newChannels, meter.timestamp != null, group, CHANNEL_LAST_UPDATE);
        return newChannels;
    }

    public static Map<String, Channel> createEMeterChannels(final Thing thing, final ShellySettingsEMeter emeter,
            String group) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();
        addChannel(thing, newChannels, emeter.power != null, group, CHANNEL_METER_CURRENTWATTS);
        addChannel(thing, newChannels, emeter.total != null, group, CHANNEL_METER_TOTALKWH);
        addChannel(thing, newChannels, emeter.totalReturned != null, group, CHANNEL_EMETER_TOTALRET);
        addChannel(thing, newChannels, emeter.reactive != null, group, CHANNEL_EMETER_REACTWATTS);
        addChannel(thing, newChannels, emeter.voltage != null, group, CHANNEL_EMETER_VOLTAGE);
        addChannel(thing, newChannels, emeter.current != null, group, CHANNEL_EMETER_CURRENT);
        addChannel(thing, newChannels, emeter.pf != null, group, CHANNEL_EMETER_PFACTOR);
        addChannel(thing, newChannels, true, group, CHANNEL_LAST_UPDATE);
        return newChannels;
    }

    public static Map<String, Channel> createSensorChannels(final Thing thing, final ShellyStatusSensor sdata) {
        Map<String, Channel> newChannels = new LinkedHashMap<>();
        // Sensor data
        addChannel(thing, newChannels, sdata.tmp != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP);
        addChannel(thing, newChannels, sdata.hum != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_HUM);
        addChannel(thing, newChannels, sdata.lux != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_LUX);
        if (sdata.accel != null) {
            addChannel(thing, newChannels, sdata.accel.vibration != null, CHANNEL_GROUP_SENSOR,
                    CHANNEL_SENSOR_VIBRATION);
            addChannel(thing, newChannels, sdata.accel.tilt != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TILT);
        }
        addChannel(thing, newChannels, sdata.flood != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD);
        addChannel(thing, newChannels, sdata.smoke != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD);
        addChannel(thing, newChannels, sdata.lux != null && sdata.lux.illumination != null, CHANNEL_GROUP_SENSOR,
                CHANNEL_SENSOR_ILLUM);
        addChannel(thing, newChannels, sdata.contact != null && sdata.contact.state != null, CHANNEL_GROUP_SENSOR,
                CHANNEL_SENSOR_STATE);
        addChannel(thing, newChannels, sdata.motion != null && sdata.contact.state != null, CHANNEL_GROUP_SENSOR,
                CHANNEL_SENSOR_MOTION);
        addChannel(thing, newChannels, sdata.charger != null, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CHARGER);
        addChannel(thing, newChannels, sdata.sensorError != null, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_ERROR);
        addChannel(thing, newChannels, sdata.actReasons != null, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_WAKEUP);
        addChannel(thing, newChannels, true, CHANNEL_GROUP_SENSOR, CHANNEL_LAST_UPDATE);

        // Battery
        if (sdata.bat != null) {
            addChannel(thing, newChannels, sdata.bat.value != null, CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL);
            addChannel(thing, newChannels, sdata.bat.value != null, CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LOW);
            addChannel(thing, newChannels, sdata.bat.voltage != null, CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_VOLT);
        }
        return newChannels;
    }

    private static void addChannel(Thing thing, Map<String, Channel> newChannels, boolean supported, String group,
            String channelName) throws IllegalArgumentException {
        if (supported) {
            final String channelId = group + "#" + channelName;
            final ShellyChannel channelDef = getDefinition(channelId);
            final ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            final ChannelTypeUID channelTypeUID = channelDef.typeId.contains("system:")
                    ? new ChannelTypeUID(channelDef.typeId)
                    : new ChannelTypeUID(BINDING_ID, channelDef.typeId);

            // Channel channel = ChannelBuilder.create(channelUID, channelId).withType(channelTypeUID)
            // .withLabel(channelDef.label).withDescription(channelDef.description).build();
            Channel channel = ChannelBuilder.create(channelUID, channelDef.itemType).withType(channelTypeUID).build();
            newChannels.put(channelId, channel);
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

        public ShellyChannel(ShellyTranslationProvider messages, String group, String channel, String typeId,
                String itemType, String... category) {
            this.messages = messages;
            this.group = group;
            this.channel = channel;
            this.itemType = itemType;
            this.typeId = typeId;

            groupLabel = getText(PREFIX_GROUP + group + SUFFIX_LABEL);
            groupDescription = getText(PREFIX_GROUP + group + SUFFIX_DESCR);
            label = getText(PREFIX_CHANNEL + channel + SUFFIX_LABEL);
            description = getText(PREFIX_CHANNEL + channel + SUFFIX_DESCR);
        }

        public String getChanneId() {
            return group + "#" + channel;
        }

        private String getText(String key) {
            String text = messages.get(key);
            return text != null ? text : "";
        }
    }

    public static class ChannelMap {
        private final Map<String, ShellyChannel> map = new LinkedHashMap<>();

        private ChannelMap add(ShellyChannel def) {
            map.put(def.getChanneId(), def);
            return this;
        }

        public ShellyChannel get(String channelName) throws IllegalArgumentException {
            ShellyChannel def = null;
            if (channelName.contains("#")) {
                def = map.get(channelName);
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
