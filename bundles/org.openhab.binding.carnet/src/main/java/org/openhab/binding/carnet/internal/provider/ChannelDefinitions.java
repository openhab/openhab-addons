/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.provider;

import static org.openhab.binding.carnet.internal.BindingConstants.*;
import static org.openhab.binding.carnet.internal.CarUtils.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.TextResources;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus.CNStoredVehicleDataResponse.CNVehicleData.CNStatusData.CNStatusField;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChannelDefinitions} maps status value IDs from the API to channel definitions.
 *
 * @author Markus Michels - Initial contribution
 * @author Lorenzo Bernardi - Additional contribution
 *
 */
@NonNullByDefault
@Component(service = ChannelDefinitions.class)
public class ChannelDefinitions {
    private final Logger logger = LoggerFactory.getLogger(ChannelDefinitions.class);
    private static final Map<String, ChannelIdMapEntry> CHANNEL_DEFINITIONS = new ConcurrentHashMap<>();
    private final TextResources resources;

    public class ChannelIdMapEntry {
        private final TextResources resources;

        public String id = "";
        public String symbolicName = "";
        public String channelName = "";
        public String itemType = "";
        public String groupName = "";
        public String groupPrefix = "";

        public boolean disabled = false;
        public boolean advanced = false;
        public boolean readOnly = true;
        public String options = "";
        public @Nullable Unit<?> fromUnit;
        public @Nullable Unit<?> unit;
        public int min = -1;
        public int max = -1;
        public int step = -1;
        public String pattern = "";

        @Activate
        public ChannelIdMapEntry(@Reference TextResources resources) {
            this.resources = resources;
        }

        public String getGroup() {
            return !groupName.isEmpty() ? groupName : CHANNEL_GROUP_STATUS;
        }

        public String getGroupIndex() {
            if (groupName.isEmpty()) {
                return "";
            }
            char index = groupName.charAt(groupName.length() - 1);
            return Character.isDigit(index) ? String.valueOf(index) : "";
        }

        public ChannelGroupTypeUID getGroupTypeUID() {
            return new ChannelGroupTypeUID(BINDING_ID, groupName);
        }

        public ChannelTypeUID getChannelTypeUID() {
            String groupIndex = ""; // getGroupIndex();
            return new ChannelTypeUID(BINDING_ID, groupIndex.isEmpty() ? channelName : channelName + groupIndex);
        }

        public String getLabel() {
            String key = channelName;
            Character index = channelName.charAt(key.length() - 1);
            if (Character.isDigit(index)) {
                key = key.substring(0, key.length() - 1); // ignore channel index for lookup
            }
            String label = getChannelAttribute(resources, key, "label");
            if (label.isEmpty()) {
                throw new IllegalArgumentException("Missing label in channel definition " + channelName);
            }
            if (groupName.isEmpty()) {
                return label;
            }
            // return groupPrefix.isEmpty() ? label : groupPrefix + getGroupIndex() + "_" + label;
            // if (getGroupIndex().isEmpty()) {
            // return label;
            // }
            // return groupPrefix + getGroupIndex() + "_" + label;
            return groupName + "_" + label.replaceAll("[ \\(\\)]", "");
        }

        public String getDescription() {
            return getChannelAttribute(resources, channelName, "description");
        }

        public String getAdvanced() {
            return getChannelAttribute(resources, channelName, "advanced");
        }

        public String getReadOnly() {
            return getChannelAttribute(resources, channelName, "readonly");
        }

        public Integer getMin() {
            String value = getChannelAttribute(resources, channelName, "min");
            return !value.isEmpty() ? Integer.parseInt(value) : -1;
        }

        public Integer getMax() {
            String value = getChannelAttribute(resources, channelName, "max");
            return !value.isEmpty() ? Integer.parseInt(value) : -1;
        }

        public Integer getStep() {
            String value = getChannelAttribute(resources, channelName, "step");
            return !value.isEmpty() ? Integer.parseInt(value) : -1;
        }

        public String getOptions() {
            return getChannelAttribute(resources, channelName, "options");
        }

        public String getPattern() {
            return getChannelAttribute(resources, channelName, "pattern");
        }
    }

    @Activate
    public ChannelDefinitions(@Reference TextResources resources) {
        this.resources = resources;
        initializeChannelTable();
    }

    public Map<String, ChannelIdMapEntry> getDefinitions() {
        return CHANNEL_DEFINITIONS;
    }

    /**
     * Lookup channel definition from channel name
     *
     * @param id Channel Id to match
     * @return Returns channel definition
     */
    public @Nullable ChannelIdMapEntry find(String id) {
        for (Map.Entry<String, ChannelIdMapEntry> e : CHANNEL_DEFINITIONS.entrySet()) {
            if (id.startsWith(e.getKey())) {
                return e.getValue();
            }
            ChannelIdMapEntry v = e.getValue();
            if ((!v.symbolicName.isEmpty() && id.startsWith(v.symbolicName))
                    || (!v.channelName.isEmpty() && id.startsWith(v.channelName))) {
                return v;
            }
        }
        return null;
    }

    /**
     * Map Datapoint units returned from the API to OH Units
     *
     * @param field Includes value and unit as returned from API
     * @param definition Channel definition to be updated
     * @return Returns updated definition
     */
    public ChannelIdMapEntry updateDefinition(CNStatusField field, ChannelIdMapEntry definition) {
        String itemType = "";
        Unit<?> unit = null;

        String fieldUnit = getString(field.unit);
        if (fieldUnit.isEmpty() || "null".equalsIgnoreCase(fieldUnit)) {
            return definition;
        }
        if (fieldUnit.contains("%")) {
            itemType = ITEMT_PERCENT;
            unit = Units.PERCENT;
        } else if ("d".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TIME;
            unit = QDAYS;
        } else if ("min".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TIME;
            unit = QMINUTES;
        } else if ("l".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_VOLUME;
            unit = Units.LITRE;
            /*
             * } else if (fieldUnit.equalsIgnoreCase("gal")) {
             * itemType = ITEMT_VOLUME;
             * unit = CustomUnits.GALLON;
             */
        } else if ("dK".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TEMP;
            unit = DKELVIN;
        } else if ("C".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TEMP;
            unit = SIUnits.CELSIUS;
        } else if ("F".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_TEMP;
            unit = ImperialUnits.FAHRENHEIT;
        } else if ("km".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_DISTANCE;
            unit = KILOMETRE;
        } else if ("mi".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_DISTANCE;
            unit = ImperialUnits.MILE;
        } else if ("in".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_DISTANCE;
            unit = ImperialUnits.INCH;
        } else if ("km/h".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_SPEED;
            unit = SIUnits.KILOMETRE_PER_HOUR;
        } else if ("mph".equalsIgnoreCase(fieldUnit) || "mi/h".equalsIgnoreCase(fieldUnit)) {
            itemType = ITEMT_SPEED;
            unit = ImperialUnits.MILES_PER_HOUR;
        } else {
            logger.debug("Field unit unknown: {}", fieldUnit);
        }

        if (unit != null) {
            definition.fromUnit = unit; // needs conversion
        }
        if (!itemType.isEmpty() && !definition.itemType.equals(itemType)) {
            logger.debug("itemType for channel {} differs from sensor value {}", definition.itemType, itemType);
        }
        return definition;
    }

    private ChannelIdMapEntry add(String name, String id, String channel, String itemType, String group,
            @Nullable Unit<?> unit, boolean advanced, boolean readOnly) {
        if (!channel.isEmpty() && (name.isEmpty() || group.isEmpty() || itemType.isEmpty())) {
            logger.warn("Incomplete channel definitoion: SYMNAME={}, group={}, chan={}, type={}", name, group, channel,
                    itemType);
        }
        ChannelIdMapEntry entry = new ChannelIdMapEntry(resources);
        entry.id = id;
        entry.symbolicName = name;
        entry.groupName = group;
        entry.groupPrefix = getGroupAttribute(resources, entry.groupName, "prefix");
        entry.channelName = channel;
        entry.itemType = itemType;
        if (ITEMT_PERCENT.equals(itemType) && (unit == null)) {
            entry.unit = Units.PERCENT;
        } else {
            entry.unit = unit;
        }
        entry.advanced = advanced;
        entry.readOnly = readOnly;

        entry.min = entry.getMin();
        entry.max = entry.getMax();
        entry.pattern = entry.getPattern();
        entry.options = entry.getOptions();

        if (!CHANNEL_DEFINITIONS.containsKey(id)) {
            CHANNEL_DEFINITIONS.put(id, entry);
        }

        return entry;
    }

    public static String getGroupAttribute(TextResources resources, String groupName, String attribute) {
        int len = groupName.length() - 1;
        char index = groupName.charAt(len);
        String groupType = Character.isDigit(index) ? groupName.substring(0, len) : groupName;
        String key = "thing-type." + BINDING_ID + ".vehicle.group." + groupType + "." + attribute;
        String value = resources.getText(key);
        return !value.equals(key) ? value : "";
    }

    /**
     * Return attribute (label, description etc.) from properties file
     *
     * @param attribute Attribute to lookup from i18n properties
     * @return Returns the attribute value as defined in the properties file
     */
    public static String getChannelAttribute(TextResources resources, String channelName, String attribute) {
        String key = "channel-type." + BINDING_ID + "." + channelName + "." + attribute;
        String value = resources.getText(key);
        return !value.equals(key) ? value : "";
    }

    /**
     * The table provides the mapping from the CarNet Datapoint Id to channel definition.
     * Channel label and description is retrieved from i18 to support multi-language resources
     */
    private void initializeChannelTable() {
        // Status
        add("KILOMETER_STATUS", "0x0101010002", "kilometerStatus", ITEMT_DISTANCE, CHANNEL_GROUP_STATUS, KILOMETRE);
        add("TEMPERATURE_OUTSIDE", "0x0301020001", "tempOutside", ITEMT_TEMP, CHANNEL_GROUP_STATUS, SIUnits.CELSIUS);
        add("STATE3_PARKING_LIGHT", "0x0301010001", "parkingLight", ITEMT_SWITCH);
        add("STATE30_PARKING_BRAKE", "0x0301030001", "parkingBrake", ITEMT_SWITCH);
        add("STATE3_SPOILER", "0x0301050011");
        add("POS_SPOILER", "0x0301050012");
        add("STATE1_SERVICE_FLAP", "0x030105000F");
        add("POS_SERVICE_FLAP", "0x0301050010");
        add("CURRENT_SPEED", "0x0301030004", "currentSpeed", ITEMT_SPEED, CHANNEL_GROUP_STATUS,
                SIUnits.KILOMETRE_PER_HOUR);
        add("BEM", "0x0301030003"); // Battery Energy Management, always 0

        // Range
        add("FUEL_LEVEL_PERCENT", "0x030103000A", "fuelPercentage", ITEMT_PERCENT, CHANNEL_GROUP_RANGE, PERCENT);
        add("FUEL_METHOD", "0x030103000B", "fuelMethod", ITEMT_STRING, CHANNEL_GROUP_RANGE, null, true, true); // '0':'measured',
        // '1':'calculated'
        add("TOTAL_RANGE", "0x0301030005", "totalRange", ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE);
        add("PRIMARY_RANGE", "0x0301030006", "primaryRange", ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE, true,
                true);
        add("PRIMARY_FUEL_TYPE", "0x0301030007", "primaryFuelType", ITEMT_NUMBER, CHANNEL_GROUP_RANGE, null, true,
                true);
        add("SECONDARY_RANGE", "0x0301030008", "secondaryRange", ITEMT_DISTANCE, CHANNEL_GROUP_RANGE, KILOMETRE, true,
                true);
        add("SECONDARY_DRIVE", "0x0301030009", "secondaryFuelType", ITEMT_NUMBER, CHANNEL_GROUP_RANGE, null, true,
                true);
        add("CHARGING_LEVEL_PERCENT", "0x0301030002", "chargingLevel", ITEMT_PERCENT, CHANNEL_GROUP_RANGE, PERCENT,
                true, true);
        add("15CNG_LEVEL_IN_PERCENT", "0x030103000D", "gasPercentage", ITEMT_PERCENT, CHANNEL_GROUP_RANGE, PERCENT,
                true, true);

        // Maintenance
        add("MAINT_ALARM_INSPECTION", "0x0203010006", "alarmInspection", ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_DISTANCE_INSPECTION", "0x0203010003", "distanceToInspection", ITEMT_DISTANCE, CHANNEL_GROUP_MAINT,
                KILOMETRE);
        add("MAINT_INSPECTION_TIME", "0x0203010004", "timeToInspection", ITEMT_TIME, CHANNEL_GROUP_MAINT, QDAYS);
        add("MAINT_ALARM_OIL_CHANGE", "0x0203010005", "oilWarningChange", ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_ALARM_OIL_MINIMUM", "0x0204040002", "oilWarningLevel", ITEMT_SWITCH, CHANNEL_GROUP_MAINT);
        add("MAINT_OIL_DISTANCE_CHANGE", "0x0203010001", "distanceOilChange", ITEMT_DISTANCE, CHANNEL_GROUP_MAINT,
                KILOMETRE);
        add("MAINT_OIL_TIME_CHANGE", "0x0203010002", "intervalOilChange", ITEMT_TIME, CHANNEL_GROUP_MAINT, QDAYS);
        add("MAINT_OIL_DIPSTICK", "0x0204040003", "oilPercentage", ITEMT_PERCENT, CHANNEL_GROUP_MAINT, PERCENT);
        add("MAINT_OIL_LEVEL_AMOUNT", "0x0204040001");
        add("MAINT_OIL_LEVEL_DISPLAY", "0x0204040004");
        add("MAINT_AD_BLUE_DISTANCE", "0x02040C0001", "distanceAdBlue", ITEMT_DISTANCE, CHANNEL_GROUP_MAINT, KILOMETRE);

        add("MAINT_MONTHLY_MILEAGE", "0x0203010007", "monthlyMilage", ITEMT_DISTANCE, CHANNEL_GROUP_STATUS, KILOMETRE);

        // Doors/trunk
        add("STATE3_LEFT_FRONT_DOOR", "0x0301040002", "doorFrontLeftState", ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_LEFT_FRONT_DOOR", "0x0301040001", "doorFrontLeftLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_LEFT_FRONT_DOOR", "0x0301040003"/* , "doorFrontLeftSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_RIGHT_FRONT_DOOR", "0x0301040008", "doorFrontRightState", ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_RIGHT_FRONT_DOOR", "0x0301040007", "doorFrontRightLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_RIGHT_FRONT_DOOR", "0x0301040009"/* , "doorFrontRightSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_LEFT_REAR_DOOR", "0x0301040005", "doorRearLeftState", ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_LEFT_REAR_DOOR", "0x0301040004", "doorRearLeftLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_LEFT_REAR_DOOR", "0x0301040006"/* , "doorRearLeftSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_RIGHT_REAR_DOOR", "0x030104000B", "doorRearRightState", ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_RIGHT_REAR_DOOR", "0x030104000A", "doorRearRightLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_RIGHT_REAR_DOOR", "0x030104000C"/* , "doorRearRightSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_TRUNK_LID", "0x030104000E", "trunkLidState", ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK3_HOOD", "0x0301040010", "hoodLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_HOOD", "0x0301040012"/* , "hoodSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_CONVERTIBLE_TOP", "0x0301050009", "covertibleTopState", ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("LOCK2_TRUNK_LID", "0x030104000D", "trunkLidLocked", ITEMT_SWITCH, CHANNEL_GROUP_DOORS);
        add("SAFETY_TRUNK_LID", "0x030104000F"/* , "trunkLidSafety", ITEMT_SWITCH, CHANNEL_GROUP_DOORS */);
        add("STATE3_HOOD", "0x0301040011", "hoodState", ITEMT_CONTACT, CHANNEL_GROUP_DOORS);
        add("POS_CONVERTIBLE_TOP", "0x030105000A", "covertibleTopPos", ITEMT_PERCENT, CHANNEL_GROUP_DOORS);

        // Windows
        add("STATE3_LEFT_FRONT_WINDOW", "0x0301050001", "windowFrontLeftState", ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_LEFT_FRONT_WINDOW", "0x0301050002", "windowFrontLeftPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_LEFT_REAR_WINDOW", "0x0301050003", "windowRearLeftState", ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_LEFT_REAR_WINDOW", "0x0301050004", "windowRearLeftPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_RIGHT_FRONT_WINDOW", "0x0301050005", "windowFrontRightState", ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_RIGHT_FRONT_WINDOW", "0x0301050006", "windowFrontRightPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_RIGHT_REAR_WINDOW", "0x0301050007", "windowRearRightState", ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_RIGHT_REAR_WINDOW", "0x0301050008", "windowRearRightPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_ROOF_FRONT_COVER", "0x030105000B", "roofFrontCoverState", ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_ROOF_FRONT_COVER", "0x030105000C", "roofFrontCoverPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);
        add("STATE3_ROOF_REAR_COVER", "0x030105000D", "roofRearCoverState", ITEMT_CONTACT, CHANNEL_GROUP_WINDOWS);
        add("POS_ROOF_REAR_COVER", "0x030105000E", "roofRearCoverPos", ITEMT_PERCENT, CHANNEL_GROUP_WINDOWS);

        // Tires
        add("STATE1_TPRESS_LEFT_FRONT_CURRENT", "0x0301060001", "tirePresFrontLeft", ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_LEFT_FRONT_DESIRED", "0x0301060002");
        add("STATE1_TPRESS_LEFT_FRONT_TIRE_DIFF", "0x030106000B");
        add("STATE1_TPRESS_LEFT_REAR_CURRENT", "0x0301060003", "tirePresRearLeft", ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_LEFT_REAR_DESIRED", "0x0301060004");
        add("STATE1_TPRESS_LEFT_REAR_TIRE_DIFF", "0x030106000C");
        add("STATE1_TPRESS_RIGHT_FRONT_CURRENT", "0x0301060005", "tirePresFrontRight", ITEMT_SWITCH,
                CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_RIGHT_FRONT_DESIRED", "0x0301060006");
        add("STATE1_TPRESS_RIGHT_FRONT_TIRE_DIFF", "0x030106000D");
        add("STATE1_TPRESS_RIGHT_REAR_CURRENT", "0x0301060007", "tirePresRearRight", ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_RIGHT_REAR_DESIRED", "0x0301060008");
        add("STATE1_TPRESS_RIGHT_REAR_TIRE_DIFF", "0x030106000E");
        add("STATE1_TPRESS_SPARE_CURRENT", "0x0301060009", "tirePresSpare", ITEMT_SWITCH, CHANNEL_GROUP_TIRES);
        add("STATE1_TPRESS_SPARE_DESIRED", "0x030106000A");
        add("STATE1_TPRESS_SPARE_DIFF", "0x030106000F");

        // Misc
        add("UTC_TIME_STATUS", "0x0101010001");
        add("UNKNOWN_0x02040C0002", "0x02040C0002"); // no yet decoded
    }

    private ChannelIdMapEntry add(String name, String id, String channel, String itemType, String group,
            @Nullable Unit<?> unit) {
        boolean advanced = CHANNEL_GROUP_STATUS.equals(group) || CHANNEL_GROUP_WINDOWS.equals(group)
                || CHANNEL_GROUP_DOORS.equals(group) || CHANNEL_GROUP_TIRES.equals(group);
        return add(name, id, channel, itemType, group, unit, advanced, true);
    }

    public ChannelIdMapEntry add(String name, String id, String channelName, String itemType, String groupName) {
        return add(name, id, channelName, itemType, groupName, null);
    }

    public ChannelIdMapEntry add(String name, String id, String channelName, String itemType) {
        return add(name, id, channelName, itemType, CHANNEL_GROUP_STATUS, null, false, true);
    }

    public ChannelIdMapEntry add(String name, String id) {
        return add(name, id, "", "", CHANNEL_GROUP_STATUS, null, false, true);
    }

    public ChannelIdMapEntry add(String group, String channel, String itemType, @Nullable Unit<?> unit,
            boolean advanced, boolean readOnly) {
        return add(mkChannelId(group, channel), channel, channel, itemType, group, unit, advanced, readOnly);
    }

    public void dumpChannelDefinitions() {
        try (FileWriter myWriter = new FileWriter("carnetChannels.MD")) {
            String lastGroup = "";
            for (Map.Entry<String, ChannelIdMapEntry> m : CHANNEL_DEFINITIONS.entrySet()) {
                ChannelIdMapEntry e = m.getValue();
                if (!e.channelName.isEmpty()) {
                    String group = lastGroup.equals(e.groupName) ? "" : e.groupName;
                    String s = String.format("| %-12.12s | %-27.27s | %-20.20s | %-7s | %-95s |\n", group,
                            e.channelName, e.itemType, e.readOnly ? "yes" : "no", e.getDescription());
                    myWriter.write(s);
                    lastGroup = e.groupName;
                }
            }
        } catch (IOException e) {
        }
    }
}
